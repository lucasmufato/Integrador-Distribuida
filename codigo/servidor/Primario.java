package servidor;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import baseDeDatos.BaseDatos;
import bloquesYTareas.*;
import mensajes.CodigoMensaje;
import mensajes.MensajePuntos;
import servidor.vista.ServidorVista;


public class Primario implements Runnable {
	
	//variables para la conexion
	protected ServerSocket serverSO;
	private MessageDigest sha256;
	private ArrayList<HiloConexionPrimario> hilosConexiones;
	private Integer puerto;
	private String IP="127.0.0.1"; //PROVISORIA HASTA QUE ESTE EL ARCHIVO DE CONFIGURACION
	private static final Integer tiempoEspera = 1000;		//esta variables sirve para que no se queda trabado para siempre esperando conexiones
	private static final int numTareasPorBloque = 5;
	private static final int numBytesPorTarea = 40;
	
	
	//variables para la vista
	private ServidorVista vista;
	
	//variables de control
	private EstadoServidor estado;
	private ArrayList<Thread> hilos;
	
	//otras variables
	private BaseDatos baseDatos;
	
	
	public Primario(){
		//algo
		this.estado=EstadoServidor.desconectado;
		this.hilosConexiones = new ArrayList<HiloConexionPrimario>();
		this.hilos = new ArrayList<Thread>();
		this.baseDatos= BaseDatos.getInstance();
		this.baseDatos.conectarse();
		this.baseDatos.detenerTareasEnProceso(); // Por si algun cliente no se desconecto bien y quedo la tarea colgada
		this.crearGUI();
		this.CrearReplicador();
		try {
			this.sha256 = MessageDigest.getInstance ("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			/* */
		}
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
		ArrayList<Bloque> bs = baseDatos.getBloquesNoCompletados();
		vista.crearAreadeBloques(bs);
		return false;
	}

	public boolean generarBloque() {
		if (this.baseDatos.generarBloques(1, numTareasPorBloque, numBytesPorTarea)) {
			ArrayList<Bloque> bs = baseDatos.getBloquesNoCompletados();
			this.vista.actualizarAreadeBloques(bs);
			return true;
		} else {
			return false;
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
				HiloConexionPrimario nuevaConexion = new HiloConexionPrimario(this,s,this.baseDatos);
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
			this.puerto=puerto;
			this.serverSO = new ServerSocket(this.puerto);
			this.serverSO.setSoTimeout(tiempoEspera);
			this.vista.MostrarPopUp("Servidor conectado con exito");
			return true;
		} catch (IOException e) {
			this.vista.MostrarPopUp("El puerto ya esta siendo utilizado, ingrese otro");
			return false;
		}
	}
	
	protected boolean CrearReplicador(){
		return false;
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
		this.esperarClientes();		
	}

	public void eliminarHiloConexion(HiloConexionPrimario hiloConexionPrimario) {
		// metodo que saca a un hilo de la lista de conexiones
		this.hilosConexiones.remove(hiloConexionPrimario);
	}
	
	public void calculoPuntos(ArrayList<ProcesamientoTarea> lista_proc) { //RESIVE LISTA POR TAREA
		System.out.println("Parciales sumados a sus combinaciones menores");
		for(int i=0;i<lista_proc.size();i++){
			BigInteger parcial = new BigInteger(1,lista_proc.get(i).getParcial());
			BigInteger combinaciones = this.sumarParcial(lista_proc.get(i).getParcial().length-1);
			lista_proc.get(i).setParcialCombinaciones(parcial.add(combinaciones));
			System.out.println(parcial);
		}
		//HAGO LO MISMO PARA EL RESULTADO
		BigInteger resultado = new BigInteger(1,lista_proc.get(lista_proc.size()-1).getResultado());
		BigInteger combinaciones = this.sumarParcial(lista_proc.get(lista_proc.size()-1).getParcial().length-1);
		lista_proc.get(lista_proc.size()-1).setResultadoCombinaciones(resultado.add(combinaciones));
		
		Integer puntosRepartir = 100; //REPARTIMOS 100 PTOS POR TAREA
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
			System.out.println("Usuario " + lista_proc.get(0).getUsuario() + " resta " + lista_proc.get(0).getResta());
		
			//A CADA USUARIO LE RESTO EL PARCIAL DEL ANTERIOR, MIENTRAS NO SEA EL ULTIMO ELEMENTO
			for(int i=1;i<lista_proc.size();i++) {
				if(lista_proc.size()-1 != i){
					parcial = lista_proc.get(i).getParcialCombinaciones();
					anterior = lista_proc.get(i-1).getParcialCombinaciones();
					resta = parcial.subtract(anterior);
					lista_proc.get(i).setResta(resta);
					System.out.println("Usuario " + lista_proc.get(i).getUsuario() + " resta " + lista_proc.get(i).getResta());
				}
			}	  
			//AL RESULTADO FINAL LE RESTO EL ULTIMO PARCIAL
			parcial = lista_proc.get(lista_proc.size()-1).getResultadoCombinaciones(); //PARCIAL QUEDO CON EL RESULTADO
			anterior = lista_proc.get(lista_proc.size()-2).getParcialCombinaciones();
			resta = parcial.subtract(anterior);
			lista_proc.get(lista_proc.size()-1).setResta(resta);
			System.out.println("Usuario " + lista_proc.get(lista_proc.size()-1).getUsuario() + " resta " + lista_proc.get(lista_proc.size()-1).getResta());
		}else{
			parcial = lista_proc.get(0).getResultadoCombinaciones();
			anterior = new BigInteger("0");
			resta = parcial.subtract(anterior);
			lista_proc.get(0).setResta(resta);
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
		
			
		
		//COMPARO LA LISTA DE USUARIOS NO REPETIDOS CON LA LISTA ORIGINAL
		for(int i = 0; i < usuarios.size(); i++){
			for(int j = i; j < lista_proc.size(); j++){
				if(usuarios.get(i).getUsuario() == lista_proc.get(j).getUsuario()){
					usuarios.get(i).addTrabajo_Realizado(lista_proc.get(j).getResta());
				}
			}
			System.out.println(" Tarea " + usuarios.get(i).getTarea() + " usuario " + usuarios.get(i).getUsuario() + " trabajo realizado " + usuarios.get(i).getTrabajo_Realizado());
		}
		
		
		BigInteger proporcion;
		//AHORA TENGO QUE DIVIDIR EL RESULTADO FINAL / LO HECHO POR CADA USUARIO Y TENGO LA PROPORCION
		for (int i = 0; i < usuarios.size(); i++) {
			proporcion = resultado.divide(usuarios.get(i).getTrabajo_Realizado());
			//ACTUALIZO LOS PUNTOS EN LA BD
			System.out.println("Los puntos a repartir  al usuario " + usuarios.get(i).getUsuario() + " son: " + proporcion.intValue()*puntosRepartir);
			this.baseDatos.actualizarPuntos(usuarios.get(i).getUsuario(), proporcion.intValue()*puntosRepartir);
		}
		
		//NOTIFICO A LOS USUARIOS
		for(int j = 0; j < this.hilosConexiones.size(); j++){
			HiloConexionPrimario hp = this.hilosConexiones.get(j);
			if(hp.usuario.getId() == usuarios.get(j).getUsuario()){
				MensajePuntos msj = new MensajePuntos(CodigoMensaje.puntos,hp.idSesion,usuarios.get(j).getTarea(),puntosRepartir); 
				hp.enviarNotificacionPuntos(msj);
			}
		}
		
	}

	private BigInteger sumarParcial(int i) {
		//g(x) = 256^x + g(x-1), para x > 0
		BigInteger suma = new BigInteger("0");
		int potenciaInt;
		while(i > 0) {
			potenciaInt = (int) Math.pow(256, i);
			suma = suma.add(BigInteger.valueOf(potenciaInt));
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

}