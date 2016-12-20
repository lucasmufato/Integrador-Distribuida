package servidor;

import java.io.IOException;
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
import servidor.vista.ServidorVista;


public class Primario implements Runnable {
	
	//variables para la conexion
	protected ServerSocket serverSO;
	private MessageDigest sha256;
	private ArrayList<HiloConexionPrimario> hilosConexiones;
	private Integer puerto;
	private String IP="127.0.0.1"; //PROVISORIA HASTA QUE ESTE EL ARCHIVO DE CONFIGURACION
	private Integer tiempoEspera = 1000;		//esta variables sirve para que no se queda trabado para siempre esperando conexiones
	
	//variables para la vista
	private ServidorVista vista;
	
	//variables de control
	private EstadoServidor estado;
	
	//otras variables
	private BaseDatos baseDatos;
	
	
	public Primario(){
		//algo
		this.estado=EstadoServidor.desconectado;
		this.hilosConexiones = new ArrayList<HiloConexionPrimario>();
		this.baseDatos= BaseDatos.getInstance();
		this.baseDatos.conectarse();
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
		this.baseDatos.addObserver(this.vista);
		//despues empiezo a esperar por los clientes
		return false;
	}
	
	private boolean crearGUI2(){
		//creo la GUI 2
		this.vista.crearPanelTrabajo();
		return false;
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
		return false;
	}
	
	public boolean pedirPuerto(Integer puerto){
		try {
			this.puerto=puerto;
			this.serverSO = new ServerSocket(this.puerto);
			this.serverSO.setSoTimeout(this.tiempoEspera);
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

	public void setTiempoEspera(Integer tiempoEspera) {
		this.tiempoEspera = tiempoEspera;
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
