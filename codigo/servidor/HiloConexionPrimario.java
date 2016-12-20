package servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Observable;

import baseDeDatos.*;
import bloquesYTareas.Tarea;
import mensajes.*;
import servidor.vista.ServidorVista;
import misc.*;


public class HiloConexionPrimario extends Observable implements Runnable, WatchdogListener {
	private static long TIMEOUT_DESCONEXION = 10000; //Si despues de 10 segundos no se recibe un mensaje, cerrar la conexion

	//conexion con otras clases
	protected Primario servidor;
	protected BaseDatos bd;
	protected ServidorVista vista;
	
	//variables para la comunicacion con el cliente
	protected Socket socket;	
	protected ObjectOutputStream flujoSaliente;  //RECIBIR OBJETOS
	protected ObjectInputStream flujoEntrante;  //ENVIAR OBJETOS
	
	//variables relacionadas con el cliente
	//necesitaria ya sea un objeto usuario o el id de sesion para saber a que usuario estoy atendiendo
	protected Tarea tareaEnTrabajo;
	protected Integer idSesion;
	protected Usuario usuario;

	//variables relacionadas con el watchdog timer (desconecta al cliente si tarda mucho en responder)
	protected Watchdog wdt = null;
	
	public HiloConexionPrimario(Primario servidor,Socket s,BaseDatos bd) {
		this.servidor=servidor;
		this.socket=s;
		this.usuario= new Usuario();
		try {
			this.flujoSaliente = new ObjectOutputStream(socket.getOutputStream());
			this.flujoEntrante = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			//si hay error aca cagamos
			e.printStackTrace();
		}
		this.bd = bd;
		
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
		this.usuario=null;
		this.tareaEnTrabajo=null;
		try {	this.flujoEntrante.close(); 	} catch (IOException e) {}
		try { 	this.flujoSaliente.close(); 	} catch (IOException e) {}
		try {	this.socket.close();			} catch (IOException e) {}
		if (this.wdt != null) {
			this.wdt.interrupt();
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
					System.out.println("Error de logeo :(");
					e.printStackTrace();
					return false;
				}
				//obtengo los datos para la autenticacion
				String usuario = msj.getUsuario();
				String password = msj.getPassword();
				this.idSesion = bd.autenticar(usuario, password); //SI DEVUELVE UN NUMERO, ES DECIR DISTINTO DE NULL, SE LOGRO AUTENTICAR BIEN
				
				this.usuario = bd.getUsuario(usuario);
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
				this.kickDog();
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
					throw new IOException("tipo de mensaje indevido");
				
				}
			} catch (ClassNotFoundException | IOException e) {
				//e.printStackTrace();
				System.out.println("hubo un error en la conexion con el usuario: "+this.usuario.getNombre()+". Desconectandolo.");
				this.detenerTarea();
				this.cerrarConexion();
				this.morir();
				return false;
			}
		}
		return false;
	}
	
	protected boolean resultadoFinalTarea(Tarea tarea){
		System.out.println ("[DEBUG] FINAL: "+ hashToString (tarea.getTarea()) + " -> " + hashToString (tarea.getResultado()));
		String resultado;
		resultado = "USUARIO: " + this.usuario.getId() + " TAREA: " + tarea.getId() + " FINAL: " + hashToString (tarea.getResultado());
		//MARCO QUE CAMBIO EL OBJETO
        setChanged();
        //NOTIFICO EL CAMBIO
        notifyObservers(resultado);
		if (this.servidor.verificarResultado(tarea) == true){
			if (this.bd.setResultado(tarea, this.usuario.getId()) == true){
				setChanged();
				notifyObservers(tarea);
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
		String resultado;
		resultado = "USUARIO: " + this.usuario.getId() + " TAREA: " + tarea.getId() + " PARCIAL: " + hashToString (tarea.getParcial());
		//MARCO QUE CAMBIO EL OBJETO
        setChanged();
        //NOTIFICO EL CAMBIO
        notifyObservers(resultado);
		if(this.bd.setParcial(tarea, this.usuario.getId()) == true){
			setChanged();
			notifyObservers(tarea);
			return true;
		}else{
			//TODO si sale mal que hago?
			return false;
		}
	}
	
	protected boolean enviarNuevaTarea(){
		//metodo que pide una tarea a la clase BaseDatos y se la envia al cliente
		System.out.println("el usuario es: " + this.usuario.getNombre());
		Tarea tarea = this.bd.getTarea(this.usuario.getId());
		if (tarea == null) {
			setChanged();
		}
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

	protected boolean detenerTarea() {
		boolean retval = this.bd.detenerTarea(this.tareaEnTrabajo, this.usuario.getId());
		setChanged();
		this.notifyObservers(this.tareaEnTrabajo);
		
		return retval;
	}

	protected void kickDog() {
		if (this.wdt == null) {
			this.wdt = new Watchdog(TIMEOUT_DESCONEXION, this);
			this.wdt.start();
		} else {
			this.wdt.kick();
		}
	}

	@Override
	public synchronized void timeout() {
		setChanged();
		this.notifyObservers("Se desconecto al cliente con id " + this.usuario.getId() + " por inactividad");
		this.detenerTarea();
		this.cerrarConexion();
		this.morir();
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
	
}//fin de la clase
