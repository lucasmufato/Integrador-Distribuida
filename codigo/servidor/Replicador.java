package servidor;

import mensajes.*;
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
	private ServerSocket serverSocket;
	private Socket socket;
	private ObjectOutputStream flujoSaliente;  //RECIBIR OBJETOS
	private ObjectInputStream flujoEntrante;  //ENVIAR OBJETOS
	private Queue<Mensaje> cola; // Esta es la cola principal de mensajes
	private Queue<Mensaje> colaTmp; // Esta cola se utiliza para no bloquear la principal. Todo lo que este en esta cola pasara a la principal en algun momento.

	public Replicador () throws IOException {
		this.cola = new LinkedList();
		this.colaTmp = new LinkedList();

		// CREAR UN SOCKET PARA QUE EL SERVIDOR SECUNDARIO SE CONECTE
		this.serverSocket = new ServerSocket (PUERTO_REPLICADOR);
	}
	
	public void replicar (Mensaje mensaje) {
		/* Guarda un mensaje en la cola */
		synchronized (this.colaTmp) {
			this.colaTmp.add (mensaje);
		}
		this.notify();
	}

	@Override
	public void run () {
		try {
			while (this.acceptConnection()) {
				this.volcarColaTmp(); // volcar cola temporaria a cola principal

				synchronized (this.cola) {
					//Procesar mensajes de cola primaria
					Mensaje mensaje = this.cola.poll();
					while (mensaje != null) {
				
						this.procesarMensaje(mensaje);
						mensaje = this.cola.poll();
					}
				}
				if (this.colaTmp.peek() == null) {
					/* Si la cola temporal esta vacia, detenemos la ejecucion */
					this.wait();
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
				Mensaje mensajeTmp = this.colaTmp.poll();
				while (mensajeTmp != null) {
				
					this.cola.add(mensajeTmp);
					mensajeTmp = this.colaTmp.poll();
				}
			}
		}
	}

	private void procesarMensaje (Mensaje mensaje) {
		// ENVIAR POR SOCKET
	}
}
