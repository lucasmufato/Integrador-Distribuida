package servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Primario {
	
	protected ServerSocket serverSO;
	
	public Primario(){
		//algo
		this.CrearReplicador();
	}
	
	public boolean crearGUI(){
		//creo la GUI
		//despues empiezo a esperar por los clientes
		this.esperarClientes();
		return false;
	}
	
	protected boolean esperarClientes(){
		
		try {
			this.serverSO = new ServerSocket(5555);
			Socket s=this.serverSO.accept();
			System.out.println("se me conectaron "+s);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	protected boolean CrearReplicador(){
		return false;
	}
}
