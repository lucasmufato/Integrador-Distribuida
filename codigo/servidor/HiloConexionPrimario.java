package servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import baseDeDatos.BaseDatos;
import mensajes.*;


public class HiloConexionPrimario implements Runnable {
	
	private BaseDatos bd;
	private Socket socket;	
	protected ObjectOutputStream flujoSaliente;  //RECIBIR OBJETOS
	protected ObjectInputStream flujoEntrante;  //ENVIAR OBJETOS
	
	public HiloConexionPrimario(Socket s) {
		this.socket=s;
		try {
			this.flujoSaliente = new ObjectOutputStream(socket.getOutputStream());
			this.flujoEntrante = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			//si hay error aca cagamos
			e.printStackTrace();
		}
		this.bd = new BaseDatos();
		bd.conectarse();
		
	}

	@Override
	public void run() {
		//UNA VEZ QUE EL SE CONECTO UN CLIENTE (Y SE CREO SU HILO) SE VA A CONECTAR CON LA BD, PARA AUTENTICAR
		MensajeLogeo msj=null;
		try {
			msj=(MensajeLogeo)this.flujoEntrante.readObject();
		} catch (ClassNotFoundException | IOException e) {
			//exploto todooo
			e.printStackTrace();
		}
		String usuario = msj.getUsuario();
		String password = msj.getPassword();
		Integer resultado = bd.autenticar(usuario, password); //SI DEVUELVE UN NUMERO, ES DECIR DISTINTO DE NULL, SE LOGRO AUTENTICAR BIEN
		MensajeLogeo mensaje;
		if(resultado != null){
			System.out.println("Autenticaci�n de usuario correcta, el n�mero de la sesi�n es: " + resultado);
			mensaje  = new MensajeLogeo(CodigoMensaje.logeo,resultado,usuario,password);
		}else{
			System.out.println("Error en autenticaci�n de usuario.");
			mensaje  = new MensajeLogeo(CodigoMensaje.logeo,null,usuario,password);
		}
		try {
			this.flujoSaliente.writeObject(mensaje);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//si esto anterior sale bien le tendria que mandar una tarea.
		
		//CREO UN MSJ TAREA
		//LEO LA TAREA DE LA BD Y SE LA MANDO AL HILO DEL CLIENTE POR EL MISMO SOCKET
		
	}

}
