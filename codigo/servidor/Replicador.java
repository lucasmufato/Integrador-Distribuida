package servidor;

import mensajes.replicacion.*;
import misc.Loggeador;
import bloquesYTareas.*;
import baseDeDatos.Usuario;
import baseDeDatos.BaseDatos;
import java.util.Queue;
import java.util.List;
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
	private Loggeador logger;
	private boolean backupEstaConectado = false;
	private BaseDatos baseDatos;
	private Primario primario;
	
	public Replicador (Loggeador logger, BaseDatos baseDatos, Primario primario) throws IOException {
		this.baseDatos = baseDatos;
		this.cola = new LinkedList<MensajeReplicacion>();
		this.colaTmp = new LinkedList<MensajeReplicacion>();
		this.logger=logger;
		this.logger.setBaseDatos (baseDatos);
		this.primario = primario;
		
		// CREAR UN SOCKET PARA QUE EL SERVIDOR SECUNDARIO SE CONECTE
		this.serverSocket = new ServerSocket (PUERTO_REPLICADOR);
		this.logger.guardar("Replicador","cree un socket en el puerto: "+PUERTO_REPLICADOR);
	}
	
	public void replicar (MensajeReplicacion mensaje) {
		/* Guardar mensaje en db/log */
		this.logger.guardar (mensaje, false);

		/* Si hay un backup conectado, encolamos el mensaje para enviarselo */
		if (this.backupEstaConectado) {
			synchronized (this.colaTmp) {
				this.colaTmp.add (mensaje);
				synchronized (this.pausa) {
					this.pausa.notifyAll();
				}
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
		this.logger.guardar("Replicador","Inicio la ejecucion del hilo, espero conexiones.");
		try {
			this.acceptConnection();
			this.backupEstaConectado = true;
			this.logger.guardar("Replicador","Se conecto un Backup");
			this.enviarCambiosPendientesDB();
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
			this.backupEstaConectado = false;
			this.logger.guardar(e);
		}
		this.morir();
	}

	private boolean acceptConnection () {
		try {
			this.socket = this.serverSocket.accept();
			
			this.flujoEntrante = new ObjectInputStream (this.socket.getInputStream());
			this.flujoSaliente = new ObjectOutputStream (this.socket.getOutputStream());
			Integer puerto=(Integer) this.flujoEntrante.readObject();
			this.primario.setIpBackup(socket.getInetAddress().getHostAddress());
			this.primario.setPuertoBackup(puerto);
			this.primario.mandarNotificacionBackup();
		} catch (Exception e) {
			this.logger.guardar(e);
		}
		return true;
	}
	
	private void enviarCambiosPendientesDB (){
		try {
			MensajeVersion msg = (MensajeVersion) this.flujoEntrante.readObject();
			long version = msg.getVersion();
			System.out.println ("DEBUG: Se ha recibido el numero de version "+version);
			List <MensajeReplicacion> cambios = this.baseDatos.getRegistrosCambios (version);
			this.encolarCambios (cambios);
		} catch (Exception e) {
			this.logger.guardar(e);
		}
	}

	private void encolarCambios (List <MensajeReplicacion> cambios) {
		synchronized (this.colaTmp) {
			for (MensajeReplicacion mensaje: cambios) {
				this.colaTmp.add (mensaje);
				synchronized (this.pausa) {
					this.pausa.notifyAll();
				}
			}
		}
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
	
	private void morir(){
		System.out.println("Replicador: muriendo");
		this.primario.setIpBackup(null);
		this.primario.setPuertoBackup(null);
		this.primario.mandarNotificacionBackup();
		this.primario=null;
		this.baseDatos=null;
		try {this.serverSocket.close();	} catch (Exception e) {	}
		try {this.serverSocket=null; 	} catch (Exception e) {	}
		try {this.flujoEntrante.close(); 	} catch (Exception e) {	}
		try {this.flujoEntrante=null; 	} catch (Exception e) {	}
		try {this.flujoSaliente.close(); 	} catch (Exception e) {	}
		try {this.flujoSaliente=null; 	} catch (Exception e) {	}
		try {this.socket.close(); 	} catch (Exception e) {	}
		try {this.socket=null; 	} catch (Exception e) {	}
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
