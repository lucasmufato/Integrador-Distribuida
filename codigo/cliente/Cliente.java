package cliente;

import cliente.vista.ClienteJFrame;

public class Cliente {

	private ClienteJFrame vista;
	private HiloConexion hiloConexion;
	
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
		this.hiloConexion = new HiloConexion();
		this.hiloConexion.conectarse(ip, puerto, usuario, password);
		// a este metodo lo llamaria la vista
		return false;
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
	
}
