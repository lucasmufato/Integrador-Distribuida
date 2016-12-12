package servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import mensajes.MensajeLogeo;

public class Primario {
	
	protected ServerSocket serverSO;
	private HiloConexionPrimario hiloConexion;
	private Thread hilo;
	private MessageDigest sha256;
	
	public Primario(){
		//algo
		this.CrearReplicador();
		try {
			this.sha256 = MessageDigest.getInstance ("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			/* */
		}
	}
	
	public boolean crearGUI(){
		//creo la GUI
		//despues empiezo a esperar por los clientes
		this.esperarClientes();
		return false;
	}
	
	protected boolean esperarClientes(){
		
		try {
			this.serverSO = new ServerSocket(5555);
			System.out.println("Soy el servidor primario, estoy a la espera de clientes");
			Socket s=this.serverSO.accept();
			System.out.println("Se me conectaron "+s);
			
			//RECIBO EL MSJ (ESTO PARA MAS ADELANTE CUANDO VEAMOS COMO HACEMOS LO DE LOS MSJs
			/*MensajeLogeo msj;
			ObjectInputStream flujoEntrante; //PARA RECIBIR OBJETOS
			ObjectOutputStream flujoSaliente; //PARA ENVIAR OBJETOS
			flujoEntrante = new ObjectInputStream (s.getInputStream());
			msj = (MensajeLogeo) flujoEntrante.readObject();*/
			
			//CREO UN HILO PARA ESE CLIENTE CONECTADO
			this.hiloConexion = new HiloConexionPrimario();
			hilo = new Thread(hiloConexion);
			hilo.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	protected boolean CrearReplicador(){
		return false;
	}

	protected boolean verificarResultado(byte[] tarea, byte[] n_sequencia, byte[] limite_superior) throws Exception{
		/* Comprueba que el hash de la concatenacion de tarea con n_sequencia es menor a limite_superior */
		/* NO PROBADO */
		byte[] concatenacion = new byte[tarea.length + n_sequencia.length];	
		System.arraycopy(tarea, 0, concatenacion, 0, tarea.length);
		System.arraycopy(n_sequencia, 0, concatenacion, tarea.length, n_sequencia.length);
		byte[] hash = this.sha256.digest (concatenacion);

		if (hash.length != limite_superior.length) {
			throw new Exception ("No se puede comparar hash con limite superior, longitudes incompatibles.");
		}

		boolean es_menor = true;
		int posicion_comparar = 0;

		while (posicion_comparar < hash.length && es_menor) {
			if (hash[posicion_comparar] < limite_superior[posicion_comparar]) {
				posicion_comparar++;
			} else {
				es_menor = false;
			}
		}

		return es_menor;
	}
}
