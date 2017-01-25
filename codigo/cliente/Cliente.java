package cliente;

import baseDeDatos.Usuario;
import bloquesYTareas.Tarea;
import cliente.vista.ClienteJFrame;
import mensajes.MensajePuntos;
import mineria.*;

public class Cliente {

	private EstadoCliente estado;
	private Integer puntos;
	private ClienteJFrame vista;
	private HiloConexion hiloConexion;
	private Thread hilo;	//es el hiloConexion, es por si le queremos dar algun comando como hilo	
	private HiloMinero hiloMinero;
	private Usuario usuario;
	private ModoTrabajo modoTrabajo;
	
	public static void main(String[] args) {
		//si se llama con argumentos no deberia mostrar interfaz grafica sino por consola
		Cliente cliente = new Cliente();
		cliente.crearGUILogueo();
	}

	public Cliente(){
		this.puntos = 0;
		this.modoTrabajo= ModoTrabajo.multiThread;
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
		this.estado=EstadoCliente.desconectado;
		// this.hiloConexion.desconectarse(); al pedo, con cambiar el estado del cliente el hiloConecion va a terminar solo.
		this.hiloMinero.detener();
		this.liberarRecursos();
		return false;
	}
	
	private void liberarRecursos(){
		this.hilo=null;
		this.hiloConexion=null;
		this.hiloMinero=null;
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
						switch(this.modoTrabajo){
						case monoThread:
							this.hiloMinero = new HiloMineroCPU(this, tarea);
							break;
						case multiThread:
							this.hiloMinero = new HiloMineroGestorMultiCore(this, tarea);
							break;
						case video:
							//TODO	 faltaaaa
							break;
						}
					}else{
						this.hiloMinero.setTarea(tarea);
					}
					this.estado = EstadoCliente.trabajando;
					this.mostrarEstadoCliente("Trabajando en tarea...");
					Thread hilo = new Thread(this.hiloMinero);
					hilo.start();
					return true;
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

	public void mostrarEstadoCliente(String texto){
		this.vista.escribirResultado(texto);
	}
	
	public void notificar (Tarea tarea){
		String texto = "";
		if (tarea.getResultado() == null) {
			texto = "Envio el resultado: PARCIAL " + HiloMinero.hashToString(tarea.getParcial());
		} else {
			texto = "FINAL: " + HiloMinero.hashToString(tarea.getResultado());
		}
		vista.escribirResultado(texto);
		this.hiloConexion.enviarResultado(tarea);
	}
	
	public void setPuntos(Integer puntos) {
		this.puntos += puntos;
	}
	
	public Integer getPuntos() {
		return puntos;
	}
	
	//SI RECIBE UN TRUE ES QUE LA INFO ES DEL PRIMARIO Y SINO DEL BACKUP
	public void actualizarIPConexion(boolean primario) {
		vista.actualizarInfoServidor(primario, hiloConexion.getIPConexion(), hiloConexion.getPuertoConexion());
	}
	
	public void notificarPuntos(MensajePuntos msj) {
		Integer puntos = msj.getPuntos();
		this.setPuntos(puntos);
		System.out.println("los puntos que tengo que notificar a la vista son: " + this.puntos);
		this.vista.actualizarPuntos(this.getPuntos());
	}

	public void primarioDesconecto() {
		// este metodo lo llama el hilo conexion en caso de que se desconecte del hilo primario(de buena o mala manera)
		vista.mostrarMsjPorConsola("el cliente primario se ha desconectado!");
		if(this.tengoServidorBackup() == true){
			vista.mostrarMsjPorConsola("intentando conectar al servidor de backup, mientras sigo procesando la tarea");
			//TODO cuando este lo de backup 
		}else{
			vista.mostrarMsjPorConsola("no tengo informacion del backup, asi que dejo de procesar y quedo en estado muerto");
			this.hiloMinero.detener();
		}
	}

	private boolean tengoServidorBackup() {
		// TODO metodo que chequea si tengo info del servidor backup para conectarme
		return false;
	}
	
	public void setUsuarioACliente(Usuario usuario) {
		this.usuario = usuario;
		this.puntos = usuario.getPuntos();
	}

	public ModoTrabajo getModoTrabajo() {
		return modoTrabajo;
	}

	public void setModoTrabajo(ModoTrabajo modoTrabajo) {
		this.modoTrabajo = modoTrabajo;
	}
	
}

