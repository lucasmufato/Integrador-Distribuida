package cliente;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import bloquesYTareas.Tarea;
import mensajes.CodigoMensaje;
import mensajes.*;

public class HiloConexion implements Runnable {
	
	protected Cliente cliente;
	
	protected Socket socket;
	protected ObjectOutputStream flujoSaliente;  //RECIBIR OBJETOS
	protected ObjectInputStream flujoEntrante;  //ENVIAR OBJETOS
	
	protected Integer idSesion;
	
	public HiloConexion(Cliente cliente){
		this.cliente=cliente;
	}
	
	public boolean conectarse(String ip, Integer puerto, String usuario, String password){
		//hecho rapido para probar que algo ande
		//La idea es que no sea asi
		
		Mensaje msj = new MensajeLogeo(CodigoMensaje.logeo,null,usuario,password);

		try {
			this.socket = new Socket(ip,puerto);
			this.flujoSaliente = new ObjectOutputStream(socket.getOutputStream());
			this.flujoEntrante = new ObjectInputStream(socket.getInputStream());
			this.flujoSaliente.writeObject(msj);
			MensajeLogeo respuesta = (MensajeLogeo) this.flujoEntrante.readObject();
			if(respuesta.getID_Sesion() == null){
				//error de logeo
				//TODO informar a la vista
				return false;
			}
			this.idSesion=respuesta.getID_Sesion();
			cliente.setEstado(EstadoCliente.esperandoTrabajo);
			return true;
		} catch (UnknownHostException e) {
			//no me pude conectar al servidor
			e.printStackTrace();
		} catch (IOException e) {
			//otro tipo de error, ya sea de comunicacion o al crear los flujos
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			//me mandaron algo que no era un objeto de la clase mensaje
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public void run() {
		while(cliente.getEstado()!=EstadoCliente.desconectado){
			//mientras no este desconectado
			//leo y recibo mensajes
			
			try {
				Mensaje mensajeRecibido = (Mensaje) this.flujoEntrante.readObject();
				
				switch(mensajeRecibido.getCodigo()){
				case tarea:
					MensajeTarea mensaje = (MensajeTarea) mensajeRecibido;
					this.tarea(mensaje.getTarea());
					break;
				default:
					//aca entran los casos de un mensaje con codigo=logeo y codigo respuestaTarea
					//ambos no deberian suceder
					break;
				
				}
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private boolean tarea(Tarea tarea){
		//VA A RECIBIR LA TAREA, Y VA A LLAMAR AL METODO TRABAJARDEL CLIENTE, PRIMERO VA A PONER EL ESTADO DEL CLIENTE
		//EN ESPERANDO TRABAJO
		this.cliente.setEstado(EstadoCliente.esperandoTrabajo);
		this.cliente.trabajar(tarea);
		return false;
	}

	public void enviarResultado(Tarea tarea) {
		MensajeTarea mensaje = new MensajeTarea(CodigoMensaje.respuestaTarea,this.idSesion,tarea);
		try {
			this.flujoSaliente.writeObject(mensaje);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
