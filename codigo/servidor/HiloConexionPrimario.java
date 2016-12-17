package servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Observable;

import baseDeDatos.BaseDatos;
import bloquesYTareas.Tarea;
import mensajes.*;


public class HiloConexionPrimario extends Observable implements Runnable {

	//conexion con otras clases
	protected Primario servidor;
	protected BaseDatos bd;
	
	//variables para la comunicacion con el cliente
	protected Socket socket;	
	protected ObjectOutputStream flujoSaliente;  //RECIBIR OBJETOS
	protected ObjectInputStream flujoEntrante;  //ENVIAR OBJETOS
	
	//variables relacionadas con el cliente
	//necesitaria ya sea un objeto usuario o el id de sesion para saber a que usuario estoy atendiendo
	protected Tarea tareaEnTrabajo;
	protected Integer idSesion;
	protected Integer idUsuario=1;	//por el momento para completar los metodos
	
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
		bd.conectarse();
		
	}

	@Override
	public void run() {
		//hago el logeo
		if(this.clienteLogeo() ){
			//si el cliente se logra autenticar
			this.intercambioDeTarea();
		}else{
			this.cerrarConexion();
			this.morir();
		}
		
	}
	
	protected void morir(){
		//metodo que borra la relacion entre el servidor y el HiloConexionPrimario y despues llama al garbage colector
		this.servidor.eliminarHiloConexion(this);
		this.servidor=null;
		try {	this.finalize();	} catch (Throwable e) {	}
	}

	protected void cerrarConexion(){
		//metodo que libera recursos y se desconecta
		this.bd=null;
		this.idUsuario=null;
		this.tareaEnTrabajo=null;
		try {	this.flujoEntrante.close(); 	} catch (IOException e) {}
		try { 	this.flujoSaliente.close(); 	} catch (IOException e) {}
		try {	this.socket.close();			} catch (IOException e) {}
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
				this.idSesion = bd.autenticar(usuario, password); //SI DEVUELVE UN NUMERO, ES DECIR DISTINTO DE NULL, SE LOGRO AUTENTICAR BIEN
				MensajeLogeo mensaje;	//mensaje de respuesta
				if(this.idSesion != null){
					System.out.println("Autenticacion de usuario correcta, el numero de la sesion es: " + this.idSesion);
					mensaje  = new MensajeLogeo(CodigoMensaje.logeo,this.idSesion,usuario,password);
					logeado=true;	//bandera para salir del bucle
				}else{
					System.out.println("Error en autenticacion de usuario.");
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
		if(this.enviarNuevaTarea() == false ){
			//si se produjo algun error al enviar la tarea
			return false;
		}
		//DESPUES DEL ENVIARNUEVATAREA EN TAREAENTRABAJO VOY A TENER LA TAREA QUE ME ASIGNARON
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
				this.bd.detenerTarea(this.tareaEnTrabajo, this.idUsuario);
				this.cerrarConexion();
				this.morir();
				return false;
			}
		}
		return false;
	}
	
	protected boolean resultadoFinalTarea(Tarea tarea){
		System.out.println ("[DEBUG] FINAL: "+ hashToString (tarea.getTarea()) + " -> " + hashToString (tarea.getResultado()));
		if (this.servidor.verificarResultado(tarea) == true){
			if (this.bd.setResultado(tarea, idUsuario) == true){
				this.enviarNuevaTarea();
				return true;
			}else{
				//TODO si la BD no lo pudo guardar que hago?
			}
		}else{
			//TODO si no se verifica bien que hago?
		}
		return false;
	}
	
	protected boolean resultadoParcialTarea(Tarea tarea){
		//creo q no haria mas que eso
		System.out.println ("[DEBUG] PARCIAL:  -> " + hashToString (tarea.getParcial()));
		if(this.bd.setParcial(tarea, idUsuario) == true){
			
			return true;
		}else{
			//TODO si sale mal que hago?
			return false;
		}
	}
	
	protected boolean enviarNuevaTarea(){
		//metodo que pide una tarea a la clase BaseDatos y se la envia al cliente
		Tarea tarea = this.bd.getTarea(idUsuario);		//por ahora es un numero inventado lo que le paso
		MensajeTarea mensaje = new MensajeTarea(CodigoMensaje.tarea,this.idSesion,tarea);
		try {
			this.flujoSaliente.writeObject(mensaje);
			this.tareaEnTrabajo = tarea;	//una vez que envie la nueva tarea y no hubo error, digo que esta en trabajo.       
			return true;
		} catch (IOException e) {
			//TODO si sale mal q hago?
			e.printStackTrace();
			return false;
		}
	}
	
	public static String hashToString(byte[] hash) {
		if(hash==null){
			return "";
		}
    	StringBuilder builder = new StringBuilder();
    	for(byte b : hash) {
    	    builder.append(String.format("%02x ", b));
    	}
    	return builder.toString();
	}
	
	//PARA LA VISTA DEL SERVIDOR, LE PASO EL ID Y LA TAREA DE ESTA CONEXION
	public Integer getIdUsuario(){
		return this.idUsuario;
	}
	
	public String getTarea(){
		byte[] tarea = this.tareaEnTrabajo.getTarea();
		return this.hashToString(tarea);
	}
	
	
}//fin de la clase
