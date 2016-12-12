package servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import mensajes.MensajeLogeo;

import servidor.vista.ServidorVista;

public class Primario implements Runnable {
	
	//variables para la conexion
	protected ServerSocket serverSO;
	private MessageDigest sha256;
	private ArrayList<HiloConexionPrimario> hilosConexiones;
	private Integer puerto;
	private String IP;
	private Integer tiempoEspera = 1000;		//esta variables sirve para que no se queda trabado para siempre esperando conexiones
	
	//variables para la vista
	private ServidorVista vista;
	
	//variables de control
	private EstadoServidor estado;
	
	public Primario(){
		//algo
		this.estado=EstadoServidor.desconectado;
		this.hilosConexiones = new ArrayList<HiloConexionPrimario>();
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
	
	protected boolean esperarClientes(){
		//metodo que se queda esperando conexiones
		this.estado=EstadoServidor.esperandoClientes;
		this.vista.mostrarMsjConsola("esperando conexiones");
		while (this.estado.equals( EstadoServidor.esperandoClientes) ){
			try {
				Socket s = this.serverSO.accept();
				//this.vista.mostrarMsjConsola("Se me conectaron "+s);
				HiloConexionPrimario nuevaConexion = new HiloConexionPrimario(s);
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
	
	public boolean pedirPuerto(){
		try {
			this.serverSO = new ServerSocket(this.puerto);
			this.serverSO.setSoTimeout(this.tiempoEspera);
			return true;
		} catch (IOException e) {
			this.vista.MostrarPopUp("el puerto ya esta siendo utilizado, ingrese otro");
			return false;
		}
	}
	
	protected boolean CrearReplicador(){
		return false;
	}

	protected boolean verificarResultado(byte[] tarea, byte[] n_sequencia, byte[] limite_superior) throws Exception {
		/* Comprueba que el hash de la concatenacion de tarea con n_sequencia es menor a limite_superior */
		/* NO PROBADO */
		byte[] concatenacion = new byte[tarea.length + n_sequencia.length];	
		System.arraycopy(tarea, 0, concatenacion, 0, tarea.length);
		System.arraycopy(n_sequencia, 0, concatenacion, tarea.length, n_sequencia.length);
		byte[] hash = this.sha256.digest (concatenacion);

		if (hash.length != limite_superior.length) {
			throw new Exception ("No se puede comparar hash con limite superior, longitudes incompatibles.");
		}

		boolean es_menor = true;
		int posicion_comparar = 0;

		while (posicion_comparar < hash.length && es_menor) {
			if (hash[posicion_comparar] < limite_superior[posicion_comparar]) {
				posicion_comparar++;
			} else {
				es_menor = false;
			}
		}

		return es_menor;
	}

	@Override
	public void run() {
		this.esperarClientes();		
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
