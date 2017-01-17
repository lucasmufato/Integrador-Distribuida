package servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class HiloBackup implements Runnable{
	//SOCKET PARA LA COMUNICACION CON EL REPLICADOR
	protected Socket socket; 
	protected ObjectOutputStream flujoSaliente;  //RECIBIR OBJETOS
	protected ObjectInputStream flujoEntrante;  //ENVIAR OBJETOS


	@Override
	public void run() {
		this.conectarmeReplicador();
		
	}

	public void conectarmeReplicador() {
		try {
			socket = new Socket("127.0.0.1",76167);
			
			//ACA VA A EMPEZAR A RECIBIR MSJ DE ACTUALIZACION A LA BD
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}