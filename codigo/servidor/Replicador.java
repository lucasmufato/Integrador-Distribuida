package servidor;

import mensajes.*;
import mensajes.replicacion.*;
import bloquesYTareas.*;
import baseDeDatos.Usuario;
import java.util.Queue;
import java.util.LinkedList;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/* El servidor primario crea un replicador */
/* El replicador espera conexiones del servidor secundario */

public class Replicador extends Thread {
	private static int PUERTO_REPLICADOR = 7567;
	private static int ESPERA_REINTENTO = 1000; // Tiempo de espera para reintentar enviar mensajes a servidor backup
	private ServerSocket serverSocket;
	private Socket socket;
	private ObjectOutputStream flujoSaliente;  //RECIBIR OBJETOS
	private ObjectInputStream flujoEntrante;  //ENVIAR OBJETOS
	private Queue<MensajeReplicacion> cola; // Esta es la cola principal de mensajes
	private Queue<MensajeReplicacion> colaTmp; // Esta cola se utiliza para no bloquear la principal. Todo lo que este en esta cola pasara a la principal en algun momento.
	private Object pausa = new Object ();

	public Replicador () throws IOException {
		this.cola = new LinkedList();
		this.colaTmp = new LinkedList();

		// CREAR UN SOCKET PARA QUE EL SERVIDOR SECUNDARIO SE CONECTE
		this.serverSocket = new ServerSocket (PUERTO_REPLICADOR);
	}
	
	public void replicar (MensajeReplicacion mensaje) {
		/* Guarda un mensaje en la cola */
		synchronized (this.colaTmp) {
			this.colaTmp.add (mensaje);
			synchronized (this.pausa) {
				this.pausa.notifyAll();
			}
		}
	}

	public void replicarParcialTarea (Tarea tarea, Usuario usuario) {
		MensajeReplicacion mensaje = new MensajeParcialTarea (tarea, usuario);
		this.replicar (mensaje);
	}

	public void replicarResultadoTarea (Tarea tarea, Usuario usuario) {
		MensajeReplicacion mensaje = new MensajeResultadoTarea (tarea, usuario);
		this.replicar (mensaje);
	}

	public void replicarCompletitudBloque (Bloque bloque) {
		MensajeReplicacion mensaje = new MensajeCompletitudBloque (bloque);
		this.replicar (mensaje);
	}

	public void replicarAsignacionTareaUsuario (Tarea tarea, Usuario usuario, Integer idProcesamiento) {
		MensajeReplicacion mensaje = new MensajeAsignacionTareaUsuario (tarea, usuario, idProcesamiento);
		this.replicar (mensaje);
	}

	public void replicarDetencionTarea (Tarea tarea, Usuario usuario) {
		MensajeReplicacion mensaje = new MensajeDetencionTarea (tarea, usuario);
		this.replicar (mensaje);
	}

	public void replicarAsignacionPuntos (Integer puntos, Usuario usuario) {
		MensajeReplicacion mensaje = new MensajeAsignacionPuntos (puntos, usuario);
		this.replicar (mensaje);
	}

	public void replicarGeneracionBloque (Bloque bloque) {
		MensajeGeneracionBloque mensajeBloque = new MensajeGeneracionBloque (bloque.getId());
		this.replicar (mensajeBloque);
		
		for (Tarea tarea: bloque.getTareas()) {
			MensajeGeneracionTarea mensajeTarea = new MensajeGeneracionTarea (tarea.getId(), bloque.getId(), tarea.getTarea());
			this.replicar (mensajeTarea);
		}
	}

	@Override
	public void run () {
		System.out.println("[DEBUG] Se ha iniciado la ejecucion del thread replicador");
		try {
			System.out.println("[DEBUG] Se espera que un backup se conecte");
			this.acceptConnection();
			System.out.println("[DEBUG] Un backup se ha conectado");
			while (true) {
				this.volcarColaTmp(); // volcar cola temporaria a cola principal

				synchronized (this.cola) {
					//Procesar mensajes de cola primaria
					MensajeReplicacion mensaje = this.cola.poll();
					while (mensaje != null) {
						boolean enviado = this.enviarMensaje(mensaje);
						while (!enviado) {
							Thread.sleep (ESPERA_REINTENTO);
							enviado = this.enviarMensaje(mensaje);
						}
						mensaje = this.cola.poll();
					}
				}
				if (this.colaTmp.peek() == null) {
					/* Si la cola temporal esta vacia, detenemos la ejecucion */
					synchronized (this.pausa) {
						this.pausa.wait();
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Error al iniciar el replicador " + e.getMessage());
			e.printStackTrace();
		}
	}

	private boolean acceptConnection () {
		try {
			this.socket = this.serverSocket.accept();
			this.flujoEntrante = new ObjectInputStream (this.socket.getInputStream());
			this.flujoSaliente = new ObjectOutputStream (this.socket.getOutputStream());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void volcarColaTmp() {
		synchronized (this.cola) {
			synchronized (this.colaTmp) {
				//Mover mensajes de cola temporal a cola principal
				MensajeReplicacion mensajeTmp = this.colaTmp.poll();
				while (mensajeTmp != null) {
				
					this.cola.add(mensajeTmp);
					mensajeTmp = this.colaTmp.poll();
				}
			}
		}
	}

	private boolean enviarMensaje (MensajeReplicacion mensaje) {
		try {
			this.flujoSaliente.writeObject (mensaje);
		} catch (Exception exception) {
			return false;
		}

		return true;
	}
}
