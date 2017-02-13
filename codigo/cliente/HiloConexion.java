package cliente;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Observable;

import baseDeDatos.Usuario;
import bloquesYTareas.Tarea;
import mensajes.*;

public class HiloConexion extends Observable implements Runnable {
	
	protected Cliente cliente;
	
	protected Socket socket;
	protected ObjectOutputStream flujoSaliente;  //RECIBIR OBJETOS
	protected ObjectInputStream flujoEntrante;  //ENVIAR OBJETOS
	
	protected Integer idSesion;
	protected Usuario usuario;
	protected static final int socketTimeout = 100;	//tiempo de espera de lectura del socket
	protected Tarea ultimaTarea;
	
	public HiloConexion(Cliente cliente){
		this.cliente=cliente;
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
	
	public boolean conectarse(String ip, Integer puerto, String usuario, String password){
		//hecho rapido para probar que algo ande
		//La idea es que no sea asi
		MensajeLogeo msj = new MensajeLogeo(CodigoMensaje.logeo,null,usuario,password);
		msj.setModoTrabajo(cliente.getModoTrabajo());
		try {
			this.socket = new Socket(ip,puerto);
			//this.socket.setSoTimeout(socketTimeout);
			this.flujoSaliente = new ObjectOutputStream(socket.getOutputStream());
			this.flujoEntrante = new ObjectInputStream(socket.getInputStream());
			this.escribirObjetoSocket(msj);
			MensajeLogeo respuesta=null;
			Object o= this.leerObjetoSocket();
			//si el mensaje que leo es informandome de un servidor de backup todavia tengo q leer el mensaje de logeo
			if(o.getClass().equals(MensajeNotificacion.class)){
				//leo el msj de logeo
				respuesta = (MensajeLogeo) this.leerObjetoSocket();
				MensajeNotificacion msjNotif= (MensajeNotificacion)o;
				this.cliente.actualizarInformacionBackup(msjNotif.getIpBackup(), msjNotif.getPuertoBackup());
			}else{
				respuesta = (MensajeLogeo) o;
			}
			
			if(respuesta.getID_Sesion() == null){
				//error de logeo
				return false;
			}
			this.usuario = respuesta.getUsuarioObject();
			this.idSesion=respuesta.getID_Sesion();
			cliente.setUsuarioACliente(this.usuario);
			cliente.setEstado(EstadoCliente.esperandoTrabajo);
			return true;
		} catch (UnknownHostException e) {
			//no me pude conectar al servidor
			System.out.println("No se pudo encontrar el servidor :(");
		} catch (IOException e) {
			//otro tipo de error, ya sea de comunicacion o al crear los flujos
			System.out.println("Error de entrada/salida");
		} catch (ClassNotFoundException e) {
			//me mandaron algo que no era un objeto de la clase mensaje
			System.out.println("no se pudo intepretar la respuesta del servidor");
		}
		return false;
	}

	@Override
	public void run() {
		cliente.crearGUITrabajo();
		while(cliente.getEstado()!=EstadoCliente.desconectado){
			//mientras no este desconectado
			//leo y recibo mensajes
			try {
				Mensaje mensajeRecibido = (Mensaje) this.leerObjetoSocket();
				switch(mensajeRecibido.getCodigo()){
				case tarea:
					MensajeTarea mensaje = (MensajeTarea) mensajeRecibido;
					this.tarea(mensaje.getTarea());
					break;
				case puntos:
					MensajePuntos msj = (MensajePuntos) mensajeRecibido;
					this.cliente.notificarPuntos(msj);
					break;
				case notificacion:
					MensajeNotificacion mensaje1 = (MensajeNotificacion) mensajeRecibido;
					this.cliente.actualizarInformacionBackup(mensaje1.getIpBackup(), mensaje1.getPuertoBackup());
					break;
				case desconexion:
					setChanged();
					notifyObservers("Desconexion");
					break;
				case retransmision:
					//le reenvio la ultima tarea, haciendole una copia y todo
					System.err.println("le estoy reenviando una tarea");
					this.enviarResultado(ultimaTarea);
					break;
				default:
					break;
				
				}
			} catch (java.net.SocketTimeoutException e) {
			//no hago nada
			}catch (EOFException e ) {
				if(socket.isInputShutdown()){
					System.out.println("socket cerrado");
					setChanged();
					notifyObservers("Desconexion");
				}else{
					//System.out.println("socket abierto todavia");
				}
			}catch(IOException | ClassNotFoundException e){
				//e.printStackTrace(); //TODO hay q borrar
				setChanged();
				notifyObservers("Desconexion");
			}
		}
		this.cerrarConexiones();
	}
	
	private boolean tarea(Tarea tarea){
		//VA A RECIBIR LA TAREA, Y VA A LLAMAR AL METODO TRABAJARDEL CLIENTE, PRIMERO VA A PONER EL ESTADO DEL CLIENTE
		//EN ESPERANDO TRABAJO
		this.cliente.setEstado(EstadoCliente.esperandoTrabajo);
		if (tarea != null) {
			this.cliente.trabajar(tarea);
			return true;
		} else {
			return false;
		}
	}

	public void enviarResultado(Tarea tarea) {

		/* Tenemos que copiar el objeto a otro antes de enviarlo, sino del otro lado se recibe siempre igual a como estaba la primera vez que se envio */
		byte[] copia_parcial = new byte[tarea.getParcial().length];
		System.arraycopy(tarea.getParcial(), 0, copia_parcial, 0, copia_parcial.length);
		byte[] copia_resultado = null;

		if (tarea.getResultado() != null) {
			copia_resultado = new byte[tarea.getResultado().length];
			System.arraycopy(tarea.getResultado(), 0, copia_resultado, 0, copia_resultado.length);
		}

		this.ultimaTarea = new Tarea(
			tarea.getBloque(),
			tarea.getTarea(),
			copia_parcial,
			copia_resultado);
		ultimaTarea.setId(tarea.getId());
		ultimaTarea.setLimiteSuperior(tarea.getLimiteSuperior());
		MensajeTarea mensaje = new MensajeTarea(CodigoMensaje.respuestaTarea,this.idSesion,ultimaTarea);
		try {
			if(this.socket!=null){
				this.escribirObjetoSocket(mensaje);
				
			}
		} catch (IOException e) {
			this.cliente.primarioDesconecto();
		}
	}
	
	public String getIPConexion() {
		InetAddress ip = socket.getInetAddress();
		return ip.toString();
	}
	
	public Integer getPuertoConexion() {
		return socket.getPort();
	}
	
	private void cerrarConexiones(){
		try {	this.flujoEntrante.close(); 	} catch (IOException e) {}
		try { 	this.flujoSaliente.close(); 	} catch (IOException e) {}
		try {	this.socket.close();			} catch (IOException e) {}
		this.socket=null;
	}

	public void enviarDesconexion() {
		Mensaje m = new Mensaje(CodigoMensaje.desconexion,null);
		try {	this.escribirObjetoSocket(m);	} catch (IOException e) {	}
	}

}
