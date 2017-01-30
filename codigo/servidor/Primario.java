package servidor;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import baseDeDatos.BaseDatos;
import bloquesYTareas.*;
import mensajes.CodigoMensaje;
import mensajes.MensajePuntos;
import servidor.vista.ServidorVista;


public class Primario implements Runnable {
	
	//variables para la conexion
		protected ServerSocket serverSO;
		protected MessageDigest sha256;
		protected ArrayList<HiloConexionPrimario> hilosConexiones;
		protected Integer puerto;
		protected String IP="127.0.0.1"; //PROVISORIA HASTA QUE ESTE EL ARCHIVO DE CONFIGURACION
		protected static final Integer tiempoEspera = 1000;		//esta variables sirve para que no se queda trabado para siempre esperando conexiones
		protected static final int numTareasPorBloque = 4;
		protected static final int numBytesPorTarea = 40;
		
		//variables para la vista
		protected ServidorVista vista;
		
		//variables de control
		protected EstadoServidor estado;
		protected ArrayList<Thread> hilos;
		
		//otras variables
		protected BaseDatos baseDatos;
		protected Replicador replicador;
		protected BusquedaUDP servicioUDP;
		
		public Primario(String ip, String puerto){
			this.setPuerto(Integer.valueOf(puerto));
			this.estado=EstadoServidor.desconectado;
			this.hilosConexiones = new ArrayList<HiloConexionPrimario>();
			this.hilos = new ArrayList<Thread>();
			this.conectarseBD();
			this.baseDatos.detenerTareasEnProceso(); // Por si algun cliente no se desconecto bien y quedo la tarea colgada
			this.crearGUI();
			try {
				this.sha256 = MessageDigest.getInstance ("SHA-256");
			} catch (NoSuchAlgorithmException e) {
				/* */
			}
		}
		
		public Primario() {
			
		}
		
		protected void conectarseBD() {
			this.baseDatos = BaseDatos.getInstance();
			this.baseDatos.conectarse();
			
		}
		
		private boolean crearGUI(){
			//creo la GUI
			this.vista = new ServidorVista(this);
			//despues empiezo a esperar por los clientes
			return false;
		}
		
		private boolean crearGUI2(){
			//creo la GUI 2
			this.vista.crearPanelTrabajo();
			vista.crearAreadeBloques(obtenerBloquesNoCompletados());
			return false;
		}
		
		public ArrayList<Bloque> obtenerBloquesNoCompletados() {
			ArrayList<Bloque> bs = baseDatos.getBloquesNoCompletados();
			return bs;
		}

		public boolean generarBloque() {
			int id_generado = this.baseDatos.generarBloques(1, numTareasPorBloque, numBytesPorTarea);
			if (id_generado != -1) {
				ArrayList<Bloque> bs = baseDatos.getBloquesNoCompletados();
				this.vista.actualizarAreadeBloques(bs);
				this.replicador.replicarGeneracionBloque (this.baseDatos.getBloque(id_generado));
				this.asignarTareasDeNuevoBloque();
				return true;
			} else {
				return false;
			}
		}

		public void asignarTareasDeNuevoBloque () {
			//metodo que busca si tiene clientes inactivos, y les asigna tareas nuevas
			HiloConexionPrimario hilo;
			
			for (int i = 0; i< this.hilosConexiones.size(); i++) {
				hilo = this.hilosConexiones.get(i);
				if (!hilo.estaTrabajando()) {
					System.out.println ("Hilo "+i+" no esta trabajando");
					hilo.enviarNuevaTarea();
				}
			}
		}

		protected boolean esperarClientes(){
			//metodo que se queda esperando conexiones, cuando llegan crea una HiloConexionPrimario, lo agrega a la lista
			// y lo inicia como Thread.
			this.estado=EstadoServidor.esperandoClientes;
			this.vista.mostrarMsjConsola("Esperando conexiones");
			//CREO LA GUI2
			this.crearGUI2();
			
			while (this.estado.equals( EstadoServidor.esperandoClientes) ){
				try {
					Socket s = this.serverSO.accept();
					this.vista.mostrarMsjConsolaTrabajo("Se me conecto "+s);
					HiloConexionPrimario nuevaConexion = new HiloConexionPrimario(this,s,this.baseDatos,this.replicador);
					//UNA VEZ Q CREO LA CONEXION HAGO QUE LA VISTA LA OBSERVE
					nuevaConexion.addObserver(this.vista);
					this.hilosConexiones.add(nuevaConexion);
					Thread hilo = new Thread(nuevaConexion);
					this.hilos.add(hilo);
					hilo.start();	
				} catch (SocketTimeoutException e) {
					//si sale por timeout no hay problema
				} catch (IOException e) {
					//aca si hay problemas
					//cambiamos de estado asi sale del bucle
					this.estado=EstadoServidor.desconectado;
					this.vista.mostrarError(e.getMessage());
					e.printStackTrace();
				}
			}
			System.out.println("ya no espero mas conexiones");
			return false;
		}
		
		public boolean pedirPuerto(Integer puerto){
			try {
				//this.puerto=puerto;
				if (puerto == 5555) {
					this.serverSO = new ServerSocket(this.puerto);
					this.serverSO.setSoTimeout(tiempoEspera);
					this.vista.MostrarPopUp("Servidor conectado con exito");
				}
				return true;
			} catch (IOException e) {
				this.vista.MostrarPopUp("El puerto ya esta siendo utilizado, ingrese otro");
				return false;
			}
		}
		
		protected boolean CrearReplicador(){
			try {
				replicador = new Replicador();
				replicador.start ();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}

		protected boolean verificarResultado(Tarea tarea) {
			/* Comprueba que el hash de la concatenacion de tarea con n_sequencia es menor a limite_superior */
			/* NO PROBADO */
			byte[] concatenacion = tarea.getTareaRespuesta();
			byte[] hash = this.sha256.digest (concatenacion);

			if (hash.length != tarea.getLimiteSuperior().length) {
				//throw new Exception ("No se puede comparar hash con limite superior, longitudes incompatibles.");
				/** le saco la excepcion ya que es mas facil tratarlo como que da distinto y listo **/
				return false;
			}

			boolean seguir = true;
			int posicion_comparar = 0;	//TODO acomodar

			while (seguir) {
				if (hash[posicion_comparar] < tarea.getLimiteSuperior()[posicion_comparar]) {
					/* Si es menor, devolvemos true */
					return true;
				} else if (hash[posicion_comparar] < tarea.getLimiteSuperior()[posicion_comparar]) {
					/* Si es mayor, devolvemos false */
					return false;
				} else {
					/* Si es igual, pasamos al proximo byte */
					posicion_comparar++;
				}
			}

			/* Si llegamos hasta aca es porque son iguales */
			return false;
		}

		@Override
		public void run() {
			//para el servicio UDP
			this.servicioUDP= new BusquedaUDP(Integer.valueOf(puerto));
			Thread hiloServicioUDP = new Thread(this.servicioUDP);
			hiloServicioUDP.start();
			
			this.CrearReplicador();
			
			this.esperarClientes();		
		}

		public void eliminarHiloConexion(HiloConexionPrimario hiloConexionPrimario) {
			// metodo que saca a un hilo de la lista de conexiones
			this.hilosConexiones.remove(hiloConexionPrimario);
		}
		
		public void calculoPuntos(ArrayList<ProcesamientoTarea> lista_proc) { //RECIBE LISTA POR TAREA
			System.out.println("Parciales sumados a sus combinaciones menores");
			for(int i=0;i<lista_proc.size();i++){
				BigInteger parcial = new BigInteger(1,lista_proc.get(i).getParcial());
				BigInteger combinaciones = this.sumarParcial(lista_proc.get(i).getParcial().length-1);
				lista_proc.get(i).setParcialCombinaciones(parcial.add(combinaciones));
				System.out.println("Parciales " + lista_proc.get(i).getParcialCombinaciones());
			}
			//HAGO LO MISMO PARA EL RESULTADO
			System.out.println(lista_proc.get(lista_proc.size()-1).getResultado() + " Resultdo final");
			BigInteger resultado = new BigInteger(1,lista_proc.get(lista_proc.size()-1).getResultado());//DEL ULTIMO ELEMENTO TOMO EL RESULTADO
			BigInteger combinaciones = this.sumarParcial(lista_proc.get(lista_proc.size()-1).getResultado().length-1);
			lista_proc.get(lista_proc.size()-1).setResultadoCombinaciones(resultado.add(combinaciones));
			System.out.println("Resultado " + lista_proc.get(lista_proc.size()-1).getResultadoCombinaciones());
			
			BigDecimal puntosARepartir = new BigDecimal("100");
			//Integer puntosRepartir = 100; //REPARTIMOS 100 PTOS POR TAREA
			BigInteger parcial, anterior, resta;
			BigInteger inicial = new BigInteger("0");
			ProcesamientoTarea pt, ptAux;
			ArrayList<ProcesamientoTarea> usuarios = new ArrayList<ProcesamientoTarea>();
			Integer cont = 0;
			
			if(lista_proc.size() > 1){
				//PRIMERO TOMO EL PRIMER ELEMENTO DE LA LISTA Y LE RESTO 0
				parcial = lista_proc.get(0).getParcialCombinaciones();
				anterior = new BigInteger("0");
				resta = parcial.subtract(anterior);
				lista_proc.get(0).setResta(resta);
				System.out.println("Usuario " + lista_proc.get(0).getUsuario() + " anterior " + anterior + " pc " + parcial);
				System.out.println("Usuario " + lista_proc.get(0).getUsuario() + " resta " + lista_proc.get(0).getResta());
			
				//A CADA USUARIO LE RESTO EL PARCIAL DEL ANTERIOR, MIENTRAS NO SEA EL ULTIMO ELEMENTO
				for(int i=1;i<lista_proc.size();i++) {
					if(lista_proc.size()-1 != i){
						parcial = lista_proc.get(i).getParcialCombinaciones();
						anterior = lista_proc.get(i-1).getParcialCombinaciones();
						resta = parcial.subtract(anterior);
						lista_proc.get(i).setResta(resta);
						System.out.println("Usuario " + lista_proc.get(i).getUsuario() + " anterior " + anterior + " pc " + parcial);
						System.out.println("Usuario " + lista_proc.get(i).getUsuario() + " resta " + lista_proc.get(i).getResta());
					}
				}	  
				//AL RESULTADO FINAL LE RESTO EL ULTIMO PARCIAL
				parcial = lista_proc.get(lista_proc.size()-1).getResultadoCombinaciones(); //PARCIAL QUEDO CON EL RESULTADO
				anterior = lista_proc.get(lista_proc.size()-2).getParcialCombinaciones();
				resta = parcial.subtract(anterior);
				lista_proc.get(lista_proc.size()-1).setResta(resta);
				System.out.println("Usuario " + lista_proc.get(lista_proc.size()-1).getUsuario() + " anterior " + anterior + " pc " + parcial);
				System.out.println("Usuario " + lista_proc.get(lista_proc.size()-1).getUsuario() + " resta " + lista_proc.get(lista_proc.size()-1).getResta());
			}else{
				parcial = lista_proc.get(0).getResultadoCombinaciones();
				anterior = new BigInteger("0");
				resta = parcial.subtract(anterior);
				lista_proc.get(0).setResta(resta);
				System.out.println("Usuario " + lista_proc.get(0).getUsuario() + " anterior " + anterior + " pc " + parcial);
				System.out.println("Usuario " + lista_proc.get(0).getUsuario() + " resta " + lista_proc.get(0).getResta());
			}
				
			//PRIMERO HAGO UNA LISTA DE USUARIOS NO REPETIDOS,PARA LUEGO SUMAR
			for(int i=0; i < lista_proc.size(); i++){
				pt = lista_proc.get(i);
				for(int j=i+1;j<lista_proc.size();j++){
					ptAux = lista_proc.get(j);
					if(pt.getUsuario() == ptAux.getUsuario()){
						cont++;
					}
				}
				if(cont == 0){//NO TIENE REPETICION 
					//SI ENCUENTRO UN USUARIO Q NO TIENE REPETICION LO AGREGO A LA LISTA USUARIOS
					//LOS UNICOS ATRIBUTOS Q VOY A USAR SON EL ID DE USUARIO, ID TAREA Y EL TRABAJO REALIZADO
					ProcesamientoTarea nuevo = new ProcesamientoTarea();
					nuevo.setUsuario(pt.getUsuario());
					nuevo.setTrabajo_Realizado(inicial);
					nuevo.setTarea(pt.getTarea());
					usuarios.add(nuevo);
					System.out.println("Agrego a la lista a: " + pt.getUsuario() + " por la tarea " + pt.getTarea());
				}
				cont = 0;
			}// CUANDO TERMINA EL FOR DEBERIA TENER UNA LISTA CON LOS ID DE LOS USUARIOS SIN REPETICION
			
			//MUESTRO LOS USUARIOS QUE SE VEAN UNA SOLA VEZ
			for(int i = 0; i < usuarios.size(); i++){
				System.out.println(usuarios.get(i).getUsuario());
			}
			
			//COMPARO LA LISTA DE USUARIOS NO REPETIDOS CON LA LISTA ORIGINAL
			for(int i = 0; i < usuarios.size(); i++){
				for(int j = 0; j < lista_proc.size(); j++){
					//System.out.println("usuarios = " + usuarios.get(i).getUsuario() + " lista_proc usuario " + lista_proc.get(j).getUsuario());
					if(usuarios.get(i).getUsuario() == lista_proc.get(j).getUsuario()){
						//System.out.println("Son iguales los usuarios entonces sumo el trabajo");
						//System.out.println("Trabajo anterior : " + usuarios.get(i).getTrabajo_Realizado());
						usuarios.get(i).addTrabajo_Realizado(lista_proc.get(j).getResta());
						//System.out.println("Trabajo despues de la suma " + usuarios.get(i).getTrabajo_Realizado());
					}
				}
				System.out.println(" Tarea " + usuarios.get(i).getTarea() + " usuario " + usuarios.get(i).getUsuario() + " trabajo realizado " + usuarios.get(i).getTrabajo_Realizado());
			}
			
			Integer p = null;
			BigDecimal resultadoDecimal, trabajoDecimal;
			//AHORA TENGO QUE DIVIDIR EL RESULTADO FINAL / LO HECHO POR CADA USUARIO Y TENGO LA PROPORCION
			for (int i = 0; i < usuarios.size(); i++) {
				System.out.println("Resultado " + lista_proc.get(lista_proc.size()-1).getResultadoCombinaciones());
				//PROPORCION = TRABAJO REALIZADO / RESULTADO
				resultadoDecimal = new BigDecimal(lista_proc.get(lista_proc.size()-1).getResultadoCombinaciones());
				trabajoDecimal = new BigDecimal(usuarios.get(i).getTrabajo_Realizado());
				//proporcion = (usuarios.get(i).getTrabajo_Realizado().divide(lista_proc.get(lista_proc.size()-1).getResultadoCombinaciones())).doubleValue();		
				System.out.println("Trabajo realizado del usuario " + usuarios.get(i).getUsuario() + " es " + usuarios.get(i).getTrabajo_Realizado());
				//ACTUALIZO LOS PUNTOS EN LA BD
				System.out.println("La proporcion del usuario " + usuarios.get(i).getUsuario() + " es " + trabajoDecimal.divide(resultadoDecimal, 2, RoundingMode.HALF_UP));
				p = trabajoDecimal.divide(resultadoDecimal, 2, RoundingMode.HALF_UP).multiply(puntosARepartir).intValue();
				usuarios.get(i).setPuntos(p);//LE SETTEO LOS PTOS A ESE USUARIO
				//System.out.println("Los puntos a repartir  al usuario " + usuarios.get(i).getUsuario() + " son: " + trabajoDecimal.divide(resultadoDecimal, 2, RoundingMode.HALF_UP).multiply(puntosARepartir));
				System.out.println("Los puntos a repartir  al usuario " + usuarios.get(i).getUsuario() + " son: " + p);
				this.baseDatos.actualizarPuntos(usuarios.get(i).getUsuario(), p);
			}
			
			//NOTIFICO A LOS USUARIOS, EN HILOCONEXIONES ESTAN LAS CONEXIONES ACTIVAS
			for(int j = 0; j < this.hilosConexiones.size(); j++){
				HiloConexionPrimario hp = this.hilosConexiones.get(j); //TOMO LA COMEXION
				for(int i=0; i < usuarios.size(); i++){ //RECORRO EL ARRAR DE USUARIO PARA BUSCAR EL DE ESA CONEXION
					if(hp.usuario.getId() == usuarios.get(i).getUsuario()){
						MensajePuntos msj = new MensajePuntos(CodigoMensaje.puntos,hp.idSesion,usuarios.get(i).getTarea(),usuarios.get(i).getPuntos()); 
						hp.enviarNotificacionPuntos(msj);
					}
				}
			}		
		}

		private BigInteger sumarParcial(int i) {
			BigInteger suma = new BigInteger("0");
			BigInteger potencia;
			BigInteger base = new BigInteger("256");
			while (i > 0) {
				potencia =  base.pow(i);
				suma = suma.add(potencia);
				i--;
			}
			
			return suma;
		}
		
		public void desconectarse() {
			System.out.println("desconectando el servidor");
			//metodo que interrumpe a todos los HilosDeConexion, deja de esperar nuevas conexiones y libera recursos
			//cambio la bandera que indica que sigo esperando conexiones
			this.estado = EstadoServidor.desconectado;
			//cierro las conexiones con los clientes (de buena manera)
			for(HiloConexionPrimario h: this.hilosConexiones){
				h.desconectar();
			}
			//libero recursos
			try {	this.serverSO.close();	} catch (IOException e) {	}
			this.serverSO = null;
			this.baseDatos.desconectarse();
			this.baseDatos=null;
			BusquedaUDP.trabajando=false;
		}
		
		
		/** 	SETTERS Y GETTERS		**/
		public Integer getPuerto() {
			return puerto;
		}

		public void setPuerto(Integer puerto) {
			this.puerto = puerto;
		}

		public Integer getTiempoEspera() {
			return tiempoEspera;
		}

		public void setTiempoEspera(Integer tiempoEsperaNuevo) {
			//tiempoEspera = tiempoEsperaNuevo;		//lo comento por que creo q no se usa y la variables es una constante
		}

		public ServidorVista getVista() {
			return vista;
		}

		public void setVista(ServidorVista vista) {
			this.vista = vista;
		}

		public EstadoServidor getEstado() {
			return estado;
		}

		public void setEstado(EstadoServidor estado) {
			this.estado = estado;
		}

		public String getIP() {
			return IP;
		}
		public void setIP(String ip) {
			this.IP = ip;
			
		}

	}