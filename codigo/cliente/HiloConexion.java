package cliente;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import mensajes.CodigoMensaje;
import mensajes.*;

public class HiloConexion implements Runnable {
	
	protected Socket socket;
	
	public boolean conectarse(String ip, Integer puerto, String usuario, String password){
		//hecho rapido para probar que algo ande
		//La idea es que no sea asi
		
		Mensaje msj = new MensajeLogeo(CodigoMensaje.logeo,null,usuario,password);
		
		try {
			this.socket = new Socket(ip,puerto);
			
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
