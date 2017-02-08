package servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Observable;

import baseDeDatos.*;
import bloquesYTareas.EstadoBloque;
import bloquesYTareas.ProcesamientoTarea;
import bloquesYTareas.Tarea;
import cliente.ModoTrabajo;
import mensajes.*;
import servidor.vista.ServidorVista;
import misc.*;


public class HiloConexionPrimario extends Observable implements Runnable, WatchdogListener {
	private static long TIMEOUT_DESCONEXION = 10000; //Si despues de 10 segundos no se recibe un mensaje, cerrar la conexion
	private static int socketTimeout=100;
	private volatile boolean conectado;
	
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
	protected ModoTrabajo modo;
	protected Integer repeticiones;
	
	//variables relacionadas con el watchdog timer (desconecta al cliente si tarda mucho en responder)
	protected Watchdog wdt = null;
	
	//VARIABLE PARA HACER LA REPLICACION
	protected Replicador replicador;
	
	//VARIABLES PARA CALCULAR CUANTO TARDA EL CLIENTE EN DAR LA RESPUESTA FINAL(O PARCIAL SI SE DESCONECTO)
	private Instant tiempoFin = Instant.now();
	private Instant tiempoInicio= Instant.now();
	
	public HiloConexionPrimario(Primario servidor,Socket s,BaseDatos bd,Replicador replicador) {
		this.servidor=servidor;
		this.socket=s;
		this.replicador=replicador;
		this.usuario= new Usuario();
		try {
			this.socket.setSoTimeout(socketTimeout);
			this.flujoSaliente = new ObjectOutputStream(socket.getOutputStream());
			this.flujoEntrante = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			this.servidor.logger.guardar(e);
		}
		this.bd = bd;
		this.repeticiones=0;
	}

	protected Object leerObjetoSocket () throws IOException, ClassNotFoundException {
		Object object;
		synchronized (this.flujoEntrante) {
			object = this.flujoEntrante.readObject ();
		}
		return object;
	}

	protected void escribirObjetoSocket (Object objeto) throws IOException {
		synchronized (this.flujoSaliente) {
			this.flujoSaliente.writeObject (objeto);
		}
	}

	@Override
	public void run() {
		//hago el logeo
		if(this.clienteLogeo() ){
			//si el cliente se logra autenticar comienzo al intercambiar mensajes, enviando tareas, y recibiendo respuestas
			this.intercambioDeTarea();
		}else{
			this.cerrarConexion();
			this.morir();
		}
		
	}
	
	protected void morir(){
		//metodo que borra la relacion entre el servidor y el HiloConexionPrimario y despues llama al garbage colector
		if (this.servidor != null) {
			this.servidor.eliminarHiloConexion(this);
		}
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
					msj=(MensajeLogeo)this.leerObjetoSocket();
				} catch (ClassNotFoundException | IOException e) {
					this.servidor.logger.guardar(e);
					return false;
				}
				//obtengo los datos para la autenticacion
				String usuario = msj.getUsuario();
				String password = msj.getPassword();
				this.idSesion = bd.autenticar(usuario, password); //SI DEVUELVE UN NUMERO, ES DECIR DISTINTO DE NULL, SE LOGRO AUTENTICAR BIEN
				this.modo= msj.getModoTrabajo();
				
				this.usuario = bd.getUsuario(usuario);
				MensajeLogeo mensaje;	//mensaje de respuesta
				if(this.idSesion != null){
					this.servidor.logger.guardar("Hilo sesion: "+this.idSesion,"autenticacion correcta. user: "+usuario+ ".");
					mensaje  = new MensajeLogeo(CodigoMensaje.logeo,this.idSesion,this.usuario,password);
					logeado=true;	//bandera para salir del bucle
				}else{
					this.servidor.logger.guardar("Hilo sesion: "+this.idSesion,"autenticacion incorrecta para el user: "+usuario+".");
					mensaje  = new MensajeLogeo(CodigoMensaje.logeo,null,usuario,password);
				}
				try {
					this.escribirObjetoSocket(mensaje);
				} catch (IOException e) {
					this.servidor.logger.guardar(e);
					return false;
				}
			}	
		}catch (Exception e1){
			this.servidor.logger.guardar(e1);
			return false;
		}
		return true;
	}
	
	protected boolean intercambioDeTarea(){
		//este metodo se encarga de enviar la primer tarea a realizar, y despues de eso
		//entra en un bucle en el cual lee respuestas del cliente y las responde conforme sea necesario
		if(this.enviarNuevaTarea() == false ){
			//si se produjo algun error al enviar la tarea
			this.servidor.logger.guardar("Hilo sesion: "+this.idSesion,"se produjo un error al enviar la tarea! ");
			return false;
		}
		//DESPUES DEL ENVIARNUEVATAREA EN TAREAENTRABAJO VOY A TENER LA TAREA QUE ME ASIGNARON
		this.conectado=true;
		while(this.conectado){
			try {
				Object o = this.leerObjetoSocket();
				Mensaje msj=null;
				if(o instanceof Mensaje ){
					msj= (Mensaje) o;
				}else{
					if(this.repeticiones>10){
						System.err.println("desconecto al cliente por que siempre me responde mal");
						this.desconectar();
					}
					System.err.println("mensaje desconocido del tipo , pidiendo retransmision");
					msj =new Mensaje(CodigoMensaje.retransmision,null);
					this.escribirObjetoSocket(msj);
					this.repeticiones++;
				}
				
				//Mensaje msj= (Mensaje)this.flujoEntrante.readObject();
				this.kickDog();
				switch(msj.getCodigo()){
				case respuestaTarea:
					//tranformo el mensaje leido al tipo de mensaje que necesito
					MensajeTarea mensaje= (MensajeTarea)msj;
					if(mensaje.getTarea().getResultado()!=null){
						//si el mensaje tiene el resultado final
						this.resultadoFinalTarea(mensaje.getTarea());
						this.servidor.logger.guardar("Tarea","resultado final: "+ hashToString( mensaje.getTarea().getResultado()  )+" para la tarea "+mensaje.getTarea().getId() + "user: "+this.usuario.getNombre());
					}else{
						//si no tiene resultado final entonces tiene un resultado parcial
						this.resultadoParcialTarea(mensaje.getTarea());						
					}
					break;
				case desconexion:
					this.detenerTarea();
					this.cerrarConexion();
					this.morir();
					return true;
				default:
					//throw new IOException("tipo de mensaje indebido");
					
				}
			}catch(java.net.SocketTimeoutException e){
				//entra aca por time out no pasa nada
			} catch (java.io.EOFException e) {
				// Entra por aca cuando el cliente se desconecta correctamente
				System.out.println("El usuario "+this.usuario.getNombre()+" se ha desconectado.");
				this.detenerTarea();
				this.cerrarConexion();
				this.morir();
				return true;
			} catch (ClassNotFoundException | IOException e) {
				
				Loggeador.getLoggeador().guardar(e);
				System.out.println("hubo un error en la conexion con un usuario. Desconectandolo.");
				this.detenerTarea();
				this.cerrarConexion();
				this.morir();
				return false;
			}
		}
		return false;
	}
	
	protected boolean resultadoFinalTarea(Tarea tarea){
		//System.out.println ("[DEBUG] FINAL: "+ hashToString (tarea.getTarea()) + " -> " + hashToString (tarea.getResultado()));
		String resultado;
		resultado = "USUARIO: " + this.usuario.getId() + " TAREA: " + tarea.getId() + " FINAL: " + hashToString (tarea.getResultado());
		
		//MARCO QUE CAMBIO EL OBJETO
        setChanged();
        //NOTIFICO EL CAMBIO
        notifyObservers(resultado);
		if (this.servidor.verificarResultado(tarea) == true){
			this.tiempoFin= Instant.now();
			Duration duracion =Duration.between(this.tiempoInicio, this.tiempoFin);
			this.servidor.logger.guardarTiempo(tareaEnTrabajo, usuario, duracion,this.modo,true);
			//TODO tiempo
			if (this.bd.setResultado(tarea, this.usuario.getId()) == true){
				setChanged();
				notifyObservers(tarea);
				this.replicador.replicarResultadoTarea(tarea, this.usuario);
				this.servidor.logger.guardar("Tarea",resultado);
				//SI ESTA FINALIZADO EL BLOQUE ENTONCES
				if (tarea.getBloque().getEstado() == EstadoBloque.completado) {
					this.servidor.logger.guardar("bloque","El bloque "+tarea.getBloque().getId()+ "se a completado");
					this.replicador.replicarCompletitudBloque(tarea.getBloque());
					ArrayList<Tarea> tareas = this.bd.getTareasPorBloque(tarea.getBloque().getId());
					ArrayList<ProcesamientoTarea> lista_proc;
					//ACA SE LLAMA A GETPROCEDIMIENTOS Y SE LE MANDA UN ID DE TAREA
					for (int i = 0; i < tareas.size(); i++) {
						lista_proc = this.bd.getProcesamientos(tareas.get(i).getId());
						//ACA SE OPERA SOBRE LA LISTA DE ESOS PROCEDIMIENTOS QUE SE OBTUVIERON
						this.servidor.calculoPuntos(lista_proc);
					}
					
				}	
				this.enviarNuevaTarea();
				return true;
			}else{
				try{
					throw new Exception("no se pudo guardar el resultado en la BD");
				}catch(Exception e){
					this.servidor.logger.guardar(e);
				}
			}
		}else{
			try{
				throw new Exception("El servidor no pudo verificar el resultado");
			}catch(Exception e){
				this.servidor.logger.guardar(e);
			}
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
			this.replicador.replicarParcialTarea(tarea, this.usuario);
			this.servidor.logger.guardar("Tarea","resultado parcial: "+ hashToString( tarea.getResultado()  )+" para la tarea "+tarea.getId() + "user: "+this.usuario.getNombre());
			return true;
		}else{
			try{
				throw new Exception("no pude guardar el resultado parcial");
			}catch(Exception e){
				this.servidor.logger.guardar(e);
			}
			return false;
		}
	}
	
	@SuppressWarnings("deprecation")
	protected boolean enviarNuevaTarea(){
		//metodo que pide una tarea a la clase BaseDatos y se la envia al cliente
		System.out.println("el usuario es: " + this.usuario.getNombre());
		Tarea tarea = this.bd.getTarea(this.usuario.getId());
		MensajeTarea mensaje = new MensajeTarea(CodigoMensaje.tarea,this.idSesion,tarea);
		if (tarea == null) {
			setChanged();
			this.wdt.stop();
			this.tareaEnTrabajo = null;
			return false;
		} else {
			int idProcesamiento = this.bd.getIdProcesamiento (tarea.getId(), this.usuario.getId());
			this.replicador.replicarAsignacionTareaUsuario(tarea, this.usuario, idProcesamiento);
			try {
				this.escribirObjetoSocket(mensaje);
				this.tiempoInicio= Instant.now();
				this.tareaEnTrabajo = tarea;	//una vez que envie la nueva tarea y no hubo error, digo que esta en trabajo. 
				this.servidor.logger.guardar("Tarea","al usuario: "+this.usuario.getNombre()+" le asigne la tarea: "+tarea.getId() +" del bloque: "+tarea.getBloque().getId());
				return true;
			} catch (IOException e) {
				//TODO si sale mal q hago?
				this.servidor.logger.guardar(e);
				return false;
			}
		}

	}

	protected boolean detenerTarea() {
		boolean tareaSeDetuvo = false;
		if (this.usuario != null && this.tareaEnTrabajo != null) {
			tareaSeDetuvo = this.bd.detenerTarea(this.tareaEnTrabajo, this.usuario.getId());
			this.tiempoFin= Instant.now();
			Duration duracion =Duration.between(this.tiempoInicio, this.tiempoFin);
			this.servidor.logger.guardarTiempo(tareaEnTrabajo, usuario, duracion,this.modo,false);
			//TODO tiempo
			if (tareaSeDetuvo) {
				this.replicador.replicarDetencionTarea (this.tareaEnTrabajo, this.usuario);
				setChanged();
				this.notifyObservers(this.tareaEnTrabajo);
				this.servidor.logger.guardar("Tarea","se detuvo la tarea: "+this.tareaEnTrabajo.getId() + " del usuario: "+this.usuario.getNombre() );
			}
		}
		
		return tareaSeDetuvo;
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
		this.servidor.logger.guardar("Conexion","Se desconecto al cliente con id " + this.usuario.getId() + " por inactividad");
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

	public void enviarNotificacionPuntos(MensajePuntos msj) {
		try {
			this.replicador.replicarAsignacionPuntos (msj.getPuntos(), this.usuario);
			this.escribirObjetoSocket(msj);
			this.servidor.logger.guardar("Conexion","Se le informo al cliente con id " + this.usuario.getId() + " que se sus puntos son: "+ msj.getPuntos());
		} catch (IOException e) {
			this.servidor.logger.guardar(e);
		}
	}
	
	public void desconectar(){
		//metodo que envia el mensaje de desconexion al cliente
		this.conectado=false;
		Mensaje msj = new Mensaje(CodigoMensaje.desconexion,this.idSesion);
		this.servidor.logger.guardar("Conexion","Desconecto al cliente " + this.usuario.getNombre() );
		try {
			this.escribirObjetoSocket(msj);
		} catch (IOException e) {
			//  no me importa si explota aca
		}
	}

	public boolean estaTrabajando () {
		return (this.tareaEnTrabajo != null);
	}
	
	public void enviarIPBackup(String ip, Integer puerto) {
		MensajeNotificacion mensaje = new MensajeNotificacion(CodigoMensaje.notificacion,this.idSesion,ip, puerto);
		try {
			this.escribirObjetoSocket(mensaje);
		} catch (IOException e) {
			this.servidor.logger.guardar(e);
		}
	}
}//fin de la clase
