package servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import baseDeDatos.BaseDatos;
import mensajes.replicacion.MensajeAsignacionPuntos;
import mensajes.replicacion.MensajeAsignacionTareaUsuario;
import mensajes.replicacion.MensajeCompletitudBloque;
import mensajes.replicacion.MensajeDetencionTarea;
import mensajes.replicacion.MensajeParcialTarea;
import mensajes.replicacion.MensajeReplicacion;
import mensajes.replicacion.MensajeResultadoTarea;

public class HiloBackup implements Runnable{
	//SOCKET PARA LA COMUNICACION CON EL REPLICADOR
	private Socket socket; 
	private ObjectOutputStream flujoSaliente;  //RECIBIR OBJETOS
	private ObjectInputStream flujoEntrante;  //ENVIAR OBJETOS

	private boolean conectado;
	private String ip;
	private BaseDatos bd;

	public HiloBackup(String ipPrimario, BaseDatos bd) {
		// TODO Auto-generated constructor stub
		this.ip = ipPrimario;
		this.bd = bd;
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
						MensajeAsignacionTareaUsuario msjAsignacionTarea = (MensajeAsignacionTareaUsuario)msj;
						break;
					case parcialTarea:
						MensajeParcialTarea msjParcial = (MensajeParcialTarea)msj;
						break;
					case resultadoTarea:
						MensajeResultadoTarea msjResultadoTarea = (MensajeResultadoTarea)msj;
						break;
					case detencionTarea:
						MensajeDetencionTarea msjDetencionTarea = (MensajeDetencionTarea)msj;
						break;
					case completitudBloque:
						MensajeCompletitudBloque msjBloque = (MensajeCompletitudBloque)msj;
						break;
					case asignacionPuntos:
						MensajeAsignacionPuntos msjPts = (MensajeAsignacionPuntos)msj;
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