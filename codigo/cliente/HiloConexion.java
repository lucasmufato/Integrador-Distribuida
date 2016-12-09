package cliente;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import mensajes.CodigoMensaje;
import mensajes.*;

public class HiloConexion implements Runnable {
	
	protected Socket socket;
	
	public boolean conectarse(String ip, Integer puerto, String usuario, String password){
		//hecho rapido para probar que algo ande
		//La idea es que no sea asi
		
		ObjectOutputStream flujoSaliente; //RECIBIR OBJETOS
		ObjectInputStream flujoEntrante;  //ENVIAR OBJETOS
		Mensaje msj = new MensajeLogeo(CodigoMensaje.logeo,null,usuario,password);

		try {
			this.socket = new Socket(ip,puerto);
			
			//ENVIO EL OBJETO MSJ (PARA MAS ADELANTE CUANDO VEAMOS COMO HACEMOS LO DE LOS MSJs
			/*flujoSaliente = new ObjectOutputStream(socket.getOutputStream());
			flujoSaliente.writeObject(msj);*/
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}
