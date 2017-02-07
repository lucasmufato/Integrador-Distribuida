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
	
	private String ipPrimario;
	private Integer puertoPrimario;
	
	private String ipBackup;
	private Integer puertoBackup;
	
	public static void main(String[] args) {
		//si se llama con argumentos no deberia mostrar interfaz grafica sino por consola
		Cliente cliente = new Cliente();
		cliente.crearGUILogueo();
	}

	public Cliente(){
		this.puntos = 0;
	}
	
	public boolean crearGUILogueo(){
		this.vista = new ClienteJFrame(this);
		return false;
	}
	
	public boolean crearGUITrabajo(){
		this.vista.crearPanelTrabajo();
		return false;
	}

	public boolean conectarseServidorPrimario(String ip, Integer puerto, String usuario, String password){
		// a este metodo lo llamaria la vista
		this.hiloConexion = new HiloConexion(this);
		if (this.hiloConexion.conectarse(ip, puerto, usuario, password) ){
			//ya que la conexion fue exitosa guardo los datos
			this.ipPrimario=ip;
			this.puertoPrimario=puerto;
			this.usuario= new Usuario();
			this.usuario.setNombre(usuario);
			this.usuario.setPassword(password);
			//inicio el hilo de comunicacion
			hilo = new Thread(hiloConexion);
			hilo.start();
			vista.mostrarMsjPorConsola("Conexion exitosa");
			
			this.vista.actualizarInfoServidor(this.ipPrimario, this.puertoPrimario, null, null);
			return true;
		}else{
			vista.mostrarMsjPorConsola("Error en el logueo, datos incorrectos");
			return false;
		}
	}
	
	public boolean conectarseServidorBackup(){
		this.ipPrimario=this.ipBackup;
		this.puertoPrimario=this.puertoBackup;
		this.ipBackup=null;
		this.puertoBackup=null;
		this.hiloConexion = new HiloConexion(this);
		if( this.hiloConexion.conectarse (this.ipPrimario, this.puertoPrimario, usuario.getNombre(), usuario.getPassword()) ){
			this.vista.actualizarInfoServidor(this.ipPrimario, this.puertoPrimario, null, null);
			hilo = new Thread(hiloConexion);
			hilo.start();
			vista.mostrarMsjPorConsola("Conexion exitosa con el servidor de Backup");
			return true;
		}
		return false;
	}
	
	public boolean desconectarse(){
		//lo llama la vista para cerrar la conexion "bien"
		this.estado=EstadoCliente.desconectado;
		// this.hiloConexion.desconectarse(); al pedo, con cambiar el estado del cliente el hiloConecion va a terminar solo.
		if (this.hiloMinero != null) {
			this.hiloMinero.detener();
		}
		this.liberarRecursos();
		return true;
	}

	public void notificarDesconexion () {
		this.desconectarse();
		this.vista.desconectar ("El servidor ha cerrado la conexion");
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
		if (this.hiloConexion != null) {
			this.hiloConexion.enviarResultado(tarea);
		}
	}
	
	public void setPuntos(Integer puntos) {
		this.puntos += puntos;
	}
	
	public Integer getPuntos() {
		return puntos;
	}
	
	public void notificarPuntos(MensajePuntos msj) {
		Integer puntos = msj.getPuntos();
		this.setPuntos(puntos);
		this.vista.actualizarPuntos(this.getPuntos());
	}

	public void primarioDesconecto() {
		// este metodo lo llama el hilo conexion en caso de que se desconecte del hilo primario(de buena o mala manera)
		this.notificarDesconexion();
		if (hiloMinero != null) {
			this.hiloMinero.detener();
		}
		if(this.tengoServidorBackup() == true){
			vista.mostrarMsjPorConsola("intentando conectar al servidor de backup, mientras sigo procesando la tarea");
			this.conectarseServidorBackup();
		}else{
			vista.mostrarMsjPorConsola("no tengo informacion del backup, asi que dejo de procesar y quedo en estado muerto");
			
		}
		
	}

	private boolean tengoServidorBackup() {
		if(this.puertoBackup==null || this.ipBackup==null){
			return false;
		}
		return true;
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

	public void actualizarInformacionBackup(String ipBackup2, Integer puertoBackup2) {
		this.ipBackup=ipBackup2;
		this.puertoBackup=puertoBackup2;
		System.out.println("datos backup -> ip:"+this.ipBackup+" puerto: "+this.puertoBackup);
		this.vista.actualizarInfoServidor(this.ipPrimario,this.puertoPrimario , this.ipBackup, this.puertoBackup);
	}
	
}