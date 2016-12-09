package servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import mensajes.MensajeLogeo;

public class Primario {
	
	protected ServerSocket serverSO;
	private HiloConexionPrimario hiloConexion;
	private Thread hilo;
	
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
			System.out.println("Soy el servidor primario, estoy a la espera de clientes");
			Socket s=this.serverSO.accept();
			System.out.println("Se me conectaron "+s);
			
			//RECIBO EL MSJ (ESTO PARA MAS ADELANTE CUANDO VEAMOS COMO HACEMOS LO DE LOS MSJs
			/*MensajeLogeo msj;
			ObjectInputStream flujoEntrante; //PARA RECIBIR OBJETOS
			ObjectOutputStream flujoSaliente; //PARA ENVIAR OBJETOS
			flujoEntrante = new ObjectInputStream (s.getInputStream());
			msj = (MensajeLogeo) flujoEntrante.readObject();*/
			
			//CREO UN HILO PARA ESE CLIENTE CONECTADO
			this.hiloConexion = new HiloConexionPrimario();
			hilo = new Thread(hiloConexion);
			hilo.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	protected boolean CrearReplicador(){
		return false;
	}
}
