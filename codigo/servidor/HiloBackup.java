package servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import mensajes.replicacion.MensajeReplicacion;

public class HiloBackup implements Runnable{
	//SOCKET PARA LA COMUNICACION CON EL REPLICADOR
	protected Socket socket; 
	protected ObjectOutputStream flujoSaliente;  //RECIBIR OBJETOS
	protected ObjectInputStream flujoEntrante;  //ENVIAR OBJETOS

	protected boolean conectado;
	protected String ip;

	public HiloBackup(String ipPrimario) {
		// TODO Auto-generated constructor stub
		this.ip = ipPrimario;
	}

	@Override
	public void run() {
		this.conectarmeReplicador();
		
	}

	public void conectarmeReplicador() {
		try {
			socket = new Socket(this.ip,7567);
			
			//ACA VA A EMPEZAR A RECIBIR MSJ DE ACTUALIZACION A LA BD
			this.conectado=true;
			while(this.conectado){
				try {
					MensajeReplicacion msj= (MensajeReplicacion)this.flujoEntrante.readObject();
					switch(msj.getCodigo()){
						case asignacionTareaUsuario:
							break;
						case parcialTarea:
							break;
						case resultadoTarea:
							break;
						case detencionTarea:
							break;
						case completitudBloque:
							break;
						case asignacionPuntos:
							break;
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//SI ESTOY ACA ES PORQUE CONECTADO YA ES FALSE
			//EJECUTO METODOS PARA CERRAR LA CONEXION
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void desconectar() {
		// TODO Auto-generated method stub
		this.conectado = false;
	}

}