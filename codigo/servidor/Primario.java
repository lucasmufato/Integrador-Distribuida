package servidor;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Enumeration;

import baseDeDatos.BaseDatos;
import bloquesYTareas.*;
import mensajes.CodigoMensaje;
import mensajes.MensajePuntos;
import misc.Loggeador;
import servidor.vista.ServidorVista;


public class Primario implements Runnable {
	
	//variables para la conexion
		protected ServerSocket serverSO;
		protected MessageDigest sha256;
		protected ArrayList<HiloConexionPrimario> hilosConexiones;
		protected Integer puerto;
		protected String IP;
		protected static final Integer tiempoEspera = 1000;		//esta variables sirve para que no se queda trabado para siempre esperando conexiones
		protected static final int numTareasPorBloque = 48;
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
		public Loggeador logger;
		protected String ipBackup;
		protected Integer puertoBackup;
		
		
		public Primario(String ip, String puerto,Loggeador logger){
			this.IP=this.conseguirIP();
			this.estado=EstadoServidor.desconectado;
			this.hilosConexiones = new ArrayList<HiloConexionPrimario>();
			this.hilos = new ArrayList<Thread>();
			this.logger = logger;
			this.conectarseBD();
			this.baseDatos.detenerTareasEnProceso(); // Por si algun cliente no se desconecto bien y quedo la tarea colgada
			this.crearGUI();
			this.vista.setServidorText("Servidor Primario");
			this.logger.guardar("Servidor", "inicio el servicio");
			try {
				this.sha256 = MessageDigest.getInstance ("SHA-256");
			} catch (NoSuchAlgorithmException e) {
				this.logger.guardar(e);
			}
		}
		
		public Primario() {	
		}
		
		protected void conectarseBD() {
			this.baseDatos = BaseDatos.getInstance();
			this.baseDatos.setLogger(this.logger);
			this.baseDatos.conectarse();
			
		}
		
		private boolean crearGUI(){
			//creo la GUI
			this.vista = new ServidorVista(this);
			//despues empiezo a esperar por los clientes
			return true;
		}
		
		private boolean crearGUI2(){
			//creo la GUI 2
			this.vista.crearPanelTrabajo();
			vista.crearAreadeBloques(obtenerBloquesNoCompletados());
			return true;
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
					hilo.enviarNuevaTarea();
				}
			}
		}

		protected boolean esperarClientes(){
			//metodo que se queda esperando conexiones, cuando llegan crea una HiloConexionPrimario, lo agrega a la lista
			// y lo inicia como Thread.
			this.estado=EstadoServidor.esperandoClientes;
			this.logger.guardar("Servidor", "Esperando conexiones");
			this.vista.mostrarMsjConsola("Esperando conexiones");
			
			while (this.estado.equals( EstadoServidor.esperandoClientes) ){
				try {
					
					Socket s = this.serverSO.accept();
					this.vista.mostrarMsjConsolaTrabajo("Se me conecto "+s);
					this.logger.guardar("Conexiones", "Se conecto: "+s.getRemoteSocketAddress() );
					HiloConexionPrimario nuevaConexion = new HiloConexionPrimario(this,s,this.baseDatos,this.replicador);
					//UNA VEZ Q CREO LA CONEXION HAGO QUE LA VISTA LA OBSERVE
					nuevaConexion.addObserver(this.vista);
					this.hilosConexiones.add(nuevaConexion);
					Thread hilo = new Thread(nuevaConexion);
					this.hilos.add(hilo);
					hilo.start();
					//si ya tengo info del backup se la envio
					if(this.ipBackup!=null && this.puertoBackup!=null){
						Thread.sleep(100);
						nuevaConexion.enviarIPBackup(this.ipBackup, this.puertoBackup);
					}
				} catch (SocketTimeoutException e) {
					//si sale por timeout no hay problema
				} catch (IOException e) {
					//aca si hay problemas
					//cambiamos de estado asi sale del bucle
					this.estado=EstadoServidor.desconectado;
					this.vista.mostrarError(e.getMessage());
					this.logger.guardar(e);
				} catch (InterruptedException e) {
					//tampoco importa si es interrumpido
				}
			}
			this.logger.guardar("Servidor", "Dejo de esperar conexiones.");
			return false;
		}
		
		public boolean pedirPuerto(Integer puerto){
			try {
				this.puerto=puerto;
				this.serverSO = new ServerSocket(this.puerto);
				this.serverSO.setSoTimeout(tiempoEspera);
				this.vista.MostrarPopUp("Servidor conectado con exito");
				this.logger.guardar("Servidor", "Solicite el puerto: "+this.puerto +" con exito.");
				return true;
			} catch (IOException e) {
				this.logger.guardar(e);
				this.vista.MostrarPopUp("El puerto ya esta siendo utilizado, ingrese otro");
				return false;
			}
		}
		
		protected boolean CrearReplicador(){
			try {
				replicador = new Replicador(this.logger, this.baseDatos, this);
				replicador.start ();
			} catch (Exception e) {
				this.logger.guardar(e);
				return false;
			}
			return true;
		}

		protected boolean verificarResultado(Tarea tarea) {
			/* Comprueba que el hash de la concatenacion de tarea con n_sequencia es menor a limite_superior */
			
			byte[] concatenacion = tarea.getTareaRespuesta();
			byte[] hash = this.sha256.digest (concatenacion);

			if (hash.length != tarea.getLimiteSuperior().length) {
				return false;
			}

			boolean seguir = true;
			int posicion_comparar = 0;

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
			this.servicioUDP= new BusquedaUDP(Integer.valueOf(puerto),this.logger);
			Thread hiloServicioUDP = new Thread(this.servicioUDP);
			hiloServicioUDP.start();
			
			this.CrearReplicador();
			this.crearGUI2();
			this.esperarClientes();
		}

		public void eliminarHiloConexion(HiloConexionPrimario hiloConexionPrimario) {
			// metodo que saca a un hilo de la lista de conexiones
			this.hilosConexiones.remove(hiloConexionPrimario);
		}
		
		public void calculoPuntos(ArrayList<ProcesamientoTarea> lista_proc) { //RECIBE LISTA POR TAREA
			this.logger.guardar("Servidor", "Calculando puntos");
			for(int i=0;i<lista_proc.size();i++){
				BigInteger parcial = new BigInteger(1,lista_proc.get(i).getParcial());
				BigInteger combinaciones = this.sumarParcial(lista_proc.get(i).getParcial().length-1);
				lista_proc.get(i).setParcialCombinaciones(parcial.add(combinaciones));
			}
			//HAGO LO MISMO PARA EL RESULTADO
			BigInteger resultado = new BigInteger(1,lista_proc.get(lista_proc.size()-1).getResultado());//DEL ULTIMO ELEMENTO TOMO EL RESULTADO
			BigInteger combinaciones = this.sumarParcial(lista_proc.get(lista_proc.size()-1).getResultado().length-1);
			lista_proc.get(lista_proc.size()-1).setResultadoCombinaciones(resultado.add(combinaciones));
		
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
				
				//A CADA USUARIO LE RESTO EL PARCIAL DEL ANTERIOR, MIENTRAS NO SEA EL ULTIMO ELEMENTO
				for(int i=1;i<lista_proc.size();i++) {
					if(lista_proc.size()-1 != i){
						parcial = lista_proc.get(i).getParcialCombinaciones();
						anterior = lista_proc.get(i-1).getParcialCombinaciones();
						resta = parcial.subtract(anterior);
						lista_proc.get(i).setResta(resta);
						}
				}	  
				//AL RESULTADO FINAL LE RESTO EL ULTIMO PARCIAL
				parcial = lista_proc.get(lista_proc.size()-1).getResultadoCombinaciones(); //PARCIAL QUEDO CON EL RESULTADO
				anterior = lista_proc.get(lista_proc.size()-2).getParcialCombinaciones();
				resta = parcial.subtract(anterior);
				lista_proc.get(lista_proc.size()-1).setResta(resta);
			}else{
				parcial = lista_proc.get(0).getResultadoCombinaciones();
				anterior = new BigInteger("0");
				resta = parcial.subtract(anterior);
				lista_proc.get(0).setResta(resta);
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
				}
				cont = 0;
			}// CUANDO TERMINA EL FOR DEBERIA TENER UNA LISTA CON LOS ID DE LOS USUARIOS SIN REPETICION
			
			//COMPARO LA LISTA DE USUARIOS NO REPETIDOS CON LA LISTA ORIGINAL
			for(int i = 0; i < usuarios.size(); i++){
				for(int j = 0; j < lista_proc.size(); j++){
					if(usuarios.get(i).getUsuario() == lista_proc.get(j).getUsuario()){
					usuarios.get(i).addTrabajo_Realizado(lista_proc.get(j).getResta());
						}
				}
			}
			
			Integer p = null;
			BigDecimal resultadoDecimal, trabajoDecimal;
			//AHORA TENGO QUE DIVIDIR EL RESULTADO FINAL / LO HECHO POR CADA USUARIO Y TENGO LA PROPORCION
			for (int i = 0; i < usuarios.size(); i++) {
				//PROPORCION = TRABAJO REALIZADO / RESULTADO
				resultadoDecimal = new BigDecimal(lista_proc.get(lista_proc.size()-1).getResultadoCombinaciones());
				trabajoDecimal = new BigDecimal(usuarios.get(i).getTrabajo_Realizado());
				//ACTUALIZO LOS PUNTOS EN LA BD
				p = trabajoDecimal.divide(resultadoDecimal, 2, RoundingMode.HALF_UP).multiply(puntosARepartir).intValue();
				usuarios.get(i).setPuntos(p);//LE SETTEO LOS PTOS A ESE USUARIO
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
			this.logger.guardar("Servidor", "Puntos calculados.");
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
			this.logger.guardar("Servidor", "Desconectando");
			//cierro las conexiones con los clientes (de buena manera)
			for(HiloConexionPrimario h: this.hilosConexiones){
				h.desconectar();
			}
			//libero recursos
			try {	this.serverSO.close();	} catch (Exception e) {	}
			this.serverSO = null;
			this.baseDatos.desconectarse();
			this.baseDatos=null;
			BusquedaUDP.trabajando=false;
		}
		
		public void mandarNotificacionBackup() {
			System.out.println("notifico datos backup: "+this.ipBackup+":"+this.puertoBackup);
			this.vista.actualizarDatosBackup(this.ipBackup, this.puertoBackup);
			for (HiloConexionPrimario hilo : this.hilosConexiones) {
				hilo.enviarIPBackup(this.ipBackup, this.puertoBackup);
			}
		}
		
		public String conseguirIP(){
			try {
				InetAddress candidateAddress = null;
		        for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
		            NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
		            for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
		                InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
		                if (!inetAddr.isLoopbackAddress()) {

		                    if (inetAddr.isSiteLocalAddress()) {
		                        candidateAddress=inetAddr;
		                    }
		                    else if (candidateAddress == null) {
		                        candidateAddress = inetAddr;
		                    }
		                }
		            }
		        }
		        String ipCandidata=candidateAddress.toString();
		        if(ipCandidata.contains("/")){
		        	ipCandidata =ipCandidata.replace("/", "");
		        }
		        return ipCandidata.trim();
			} catch (Exception e2) {
				System.err.println("no pude conseguir mi direcion ip, pongo la de loopback");
				try { return InetAddress.getLocalHost().getHostAddress();	} catch (Exception e) { return "127.0.0.1";	}
			}
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

		public String getIpBackup() {
			return ipBackup;
		}

		public void setIpBackup(String ipBackup) {
			this.ipBackup = ipBackup;
		}

		public Integer getPuertoBackup() {
			return puertoBackup;
		}

		public void setPuertoBackup(Integer puertoBackup) {
			this.puertoBackup = puertoBackup;
		}

	}