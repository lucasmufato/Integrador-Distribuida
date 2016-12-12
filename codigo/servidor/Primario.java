package servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import servidor.vista.ServidorVista;

public class Primario implements Runnable {
	
	//variables para la conexion
	protected ServerSocket serverSO;
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
