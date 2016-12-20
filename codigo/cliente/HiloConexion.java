package cliente;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
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
			cliente.mostrarEstadoCliente("Esperando trabajo");
			return true;
		} catch (UnknownHostException e) {
			//no me pude conectar al servidor
			//e.printStackTrace();
			System.out.println("No se pudo encontrar el servidor :(");
		} catch (IOException e) {
			//otro tipo de error, ya sea de comunicacion o al crear los flujos
			//e.printStackTrace();
			System.out.println("Error de entrada/salida");
		} catch (ClassNotFoundException e) {
			//me mandaron algo que no era un objeto de la clase mensaje
			//e.printStackTrace();
			System.out.println("no se pudo intepretar la respuesta del servidor");
		}
		return false;
	}
	
	@Override
	public void run() {
		while(cliente.getEstado()!=EstadoCliente.desconectado){
			//mientras no este desconectado
			//leo y recibo mensajes
			cliente.crearGUITrabajo();
			//MANDO CON TRUE POR AHORA, PERO EN REALIDAD HABRIA QUE PREGUNTAR SI ES PRIMARIO O BACKUP
			cliente.actualizarIPConexion(true);
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
				this.cliente.setEstado(EstadoCliente.desconectado);
				//e.printStackTrace();
				System.out.println("error al interpretar la respuesta del servidor. Desconectado");
			} catch (IOException e) {
				this.cliente.setEstado(EstadoCliente.desconectado);
				//e.printStackTrace();
				System.out.println("error en la comunicacion con el servidor. Desconectado");
			}
		}
		this.cerrarConexiones();
	}

	private boolean tarea(Tarea tarea){
		//VA A RECIBIR LA TAREA, Y VA A LLAMAR AL METODO TRABAJARDEL CLIENTE, PRIMERO VA A PONER EL ESTADO DEL CLIENTE
		//EN ESPERANDO TRABAJO
		this.cliente.setEstado(EstadoCliente.esperandoTrabajo);
		this.cliente.trabajar(tarea);
		return false;
	}

	public void enviarResultado(Tarea tarea) {

		/* Tenemos que copiar el objeto a otro antes de enviarlo, sino del otro lado se recibe siempre igual a como estaba la primera vez que se envio */
		byte[] copia_parcial = new byte[tarea.getParcial().length];
		System.arraycopy(tarea.getParcial(), 0, copia_parcial, 0, copia_parcial.length);
		byte[] copia_resultado = null;

		if (tarea.getResultado() != null) {
			copia_resultado = new byte[tarea.getResultado().length];
			System.arraycopy(tarea.getParcial(), 0, copia_resultado, 0, copia_resultado.length);
		}

		Tarea copia = new Tarea(
			tarea.getBloque(),
			tarea.getTarea(),
			copia_parcial,
			copia_resultado);
		copia.setId(tarea.getId());
		copia.setLimiteSuperior(tarea.getLimiteSuperior());
		MensajeTarea mensaje = new MensajeTarea(CodigoMensaje.respuestaTarea,this.idSesion,copia);
		try {
			if(this.socket!=null){
				this.flujoSaliente.writeObject(mensaje);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
}
