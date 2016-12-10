package cliente;

import cliente.vista.ClienteJFrame;

public class Cliente {

	private EstadoCliente estado;
	private ClienteJFrame vista;
	private HiloConexion hiloConexion;
	private Thread hilo;	//es el hiloConexion, es por si le queremos dar algun comando como hilo	
	
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
			vista.mostrarMsjPorConsola("conexion exitosa");
			return true;
		}else{
			vista.mostrarMsjPorConsola("");
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
	
	public boolean trabajar(){
		// es llamado por el hilo y le pide resolver la tarea
		return false;
	}

	public EstadoCliente getEstado() {
		return estado;
	}

	public void setEstado(EstadoCliente estado) {
		this.estado = estado;
	}
	
}
