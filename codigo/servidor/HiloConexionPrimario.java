package servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import baseDeDatos.BaseDatos;
import bloquesYTareas.Tarea;
import mensajes.*;


public class HiloConexionPrimario implements Runnable {
	
	protected Primario servidor;
	protected BaseDatos bd;
	protected Socket socket;	
	protected ObjectOutputStream flujoSaliente;  //RECIBIR OBJETOS
	protected ObjectInputStream flujoEntrante;  //ENVIAR OBJETOS
	//necesitaria ya sea un objeto usuario o el id de sesion para saber a que usuario estoy atendiendo
	protected Tarea tareaEnTrabajo;
	
	public HiloConexionPrimario(Primario servidor,Socket s) {
		this.servidor=servidor;
		this.socket=s;
		try {
			this.flujoSaliente = new ObjectOutputStream(socket.getOutputStream());
			this.flujoEntrante = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			//si hay error aca cagamos
			e.printStackTrace();
		}
		this.bd = new BaseDatos();
		//bd.conectarse();
		
	}

	@Override
	public void run() {
		//hago el logeo
		if(this.clienteLogeo() ){
			//si el cliente se logra autenticar
			this.intercambioDeTarea();
		}else{
			// si no se logeo muero como hilo
		}
		
	}

	protected boolean clienteLogeo(){
		//metodo que se encarga de intercambiar mensajes hasta que el usuario se logee con exito
		MensajeLogeo msj=null;
		boolean logeado=false;
		try {
			//mientras el usuario no se logee correctamente
			while(!logeado){
				try {
					//leo lo que me manda (el usuario es el primero en mandar algo)
					msj=(MensajeLogeo)this.flujoEntrante.readObject();
				} catch (ClassNotFoundException | IOException e) {
					//exploto todooo
					e.printStackTrace();
					return false;
				}
				//obtengo los datos para la autenticacion
				String usuario = msj.getUsuario();
				String password = msj.getPassword();
				Integer resultado = bd.autenticar(usuario, password); //SI DEVUELVE UN NUMERO, ES DECIR DISTINTO DE NULL, SE LOGRO AUTENTICAR BIEN
				MensajeLogeo mensaje;	//mensaje de respuesta
				if(resultado != null){
					System.out.println("Autenticaci�n de usuario correcta, el n�mero de la sesi�n es: " + resultado);
					mensaje  = new MensajeLogeo(CodigoMensaje.logeo,resultado,usuario,password);
					logeado=true;	//bandera para salir del bucle
				}else{
					System.out.println("Error en autenticaci�n de usuario.");
					mensaje  = new MensajeLogeo(CodigoMensaje.logeo,null,usuario,password);
				}
				try {
					this.flujoSaliente.writeObject(mensaje);
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
			}	
		}catch (Exception e1){
			//si explota por algo que lo muestre y sale por falso
			e1.printStackTrace();
			return false;
		}
		return true;
	}
	
	protected boolean intercambioDeTarea(){
		//este metodo se encarga de enviar la primer tarea a realizar, y despues de eso
		//entra en un bucle en el cual lee respuestas del cliente y las responde conforme sea necesario
		this.enviarNuevaTarea();
		boolean conectado=true;
		while(conectado){
			try {
				Mensaje msj= (Mensaje)this.flujoEntrante.readObject();
				switch(msj.getCodigo()){
				case logeo:
					//
					break;
				case respuestaTarea:
					//tranformo el mensaje leido al tipo de mensaje que necesito
					MensajeTarea mensaje= (MensajeTarea)msj;
					if(mensaje.getTarea().getResultado()!=null){
						//si el mensaje tiene el resultado final
						this.resultadoFinalTarea(mensaje.getTarea());
					}else{
						//si no tiene resultado final entonces tiene un resultado parcial
						this.resultadoParcialTarea(mensaje.getTarea());
					}
					break;
				default:
					new IOException("tipo de mensaje indevido");
					break;
				
				}
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}
	
	protected boolean resultadoFinalTarea(Tarea tarea){
		
		return false;
	}
	
	protected boolean resultadoParcialTarea(Tarea tarea){
		
		return false;
	}
	
	protected boolean enviarNuevaTarea(){
		
		return false;
	}
}
