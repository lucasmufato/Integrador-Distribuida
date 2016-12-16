package cliente;

import bloquesYTareas.Tarea;
import cliente.vista.ClienteJFrame;

public class Cliente {

	private EstadoCliente estado;
	private ClienteJFrame vista;
	private HiloConexion hiloConexion;
	private Thread hilo;	//es el hiloConexion, es por si le queremos dar algun comando como hilo	
	private HiloMinero hiloMinero;
	
	public static void main(String[] args) {
		//si se llama con argumentos no deberia mostrar interfaz grafica sino por consola
		Cliente cliente = new Cliente();
		cliente.crearGUILogueo();
	}

	public Cliente(){
		//vacio por ahora
	}
	
	public boolean crearGUILogueo(){
		this.vista = new ClienteJFrame(this);
		return false;
	}
	
	public boolean crearGUITrabajo(){
		this.vista.crearPanelTrabajo();
		return false;
	}
	
	public boolean crearMenuConsola(){
		//menu para manejarlo por consola
		return false;
	}
	
	public boolean conectarseServidorPrimario(String ip, Integer puerto, String usuario, String password){
		// a este metodo lo llamaria la vista
		this.hiloConexion = new HiloConexion(this);
		if (this.hiloConexion.conectarse(ip, puerto, usuario, password) ){
			hilo = new Thread(hiloConexion);
			hilo.start();
			vista.mostrarMsjPorConsola("Conexion exitosa");
			return true;
		}else{
			vista.mostrarMsjPorConsola("Error en el logueo, datos incorrectos");
			return false;
		}
	}
	
	public boolean conectarseServidorBackup(){
		//este metodo se llamaria automaticamente si no responde el servidor primario
		return false;
	}
	
	public boolean logearse(){
		// lo llama la vista, y este llama al hilo recien creado para que se loguee
		return false;
	}
	
	public boolean desconectarse(){
		//lo llama la vista para cerrar la conexion "bien"
		return false;
	}
	
	public boolean trabajar(Tarea tarea){
		// es llamado por el hilo y le pide resolver la tarea
		synchronized (this.estado) {
			switch (this.estado) {
				case trabajando:
					return false;
				case desconectado:
					return false;
				case logueado:
					return false;
				case esperandoTrabajo:
					if (this.hiloMinero == null) {
						this.hiloMinero = new HiloMineroCPU(this, tarea);
						this.estado = EstadoCliente.trabajando;
						Thread hilo = new Thread(this.hiloMinero);
						hilo.start();
						return true;
					}
					break;
			}
		}
		return false;
	}

	public EstadoCliente getEstado() {
		synchronized (this.estado) {
			return estado;
		}
	}

	public void setEstado(EstadoCliente estado) {
		/*	ME TIRO ERROR AL USARLO PROBARLO CON SOCKETS
		synchronized (this.estado) {
			this.estado = estado;
		}
		*/
		this.estado=estado;
	}

	public void notificar (Tarea tarea){
		vista.escribirResultado("Envio el resultado: PARCIAL " + HiloMinero.hashToString(tarea.getParcial()) + "  FINAL: " +HiloMinero.hashToString(tarea.getResultado()));
		System.out.println("Envio el resultado: PARCIAL "+ HiloMinero.hashToString( tarea.getParcial() )+"  FINAL: " +HiloMinero.hashToString(tarea.getResultado()) );
		this.hiloConexion.enviarResultado(tarea);
	}
	
}
