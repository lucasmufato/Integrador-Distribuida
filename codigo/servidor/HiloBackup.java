package servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Observable;

import baseDeDatos.BaseDatos;
import bloquesYTareas.EstadoTarea;
import mensajes.replicacion.*;
import misc.Loggeador;

public class HiloBackup extends Observable implements Runnable {
	//SOCKET PARA LA COMUNICACION CON EL REPLICADOR
	private Socket socket; 
	private ObjectOutputStream flujoSaliente;  //RECIBIR OBJETOS
	private ObjectInputStream flujoEntrante;  //ENVIAR OBJETOS

	private boolean conectado;
	private String ip;
	private BaseDatos bd;
	private Backup backup;
	private Loggeador logger;
	
	public HiloBackup(Backup backup, String ipPrimario, BaseDatos bd,Loggeador logger) {
		this.backup = backup;
		this.ip = ipPrimario;
		this.bd = bd;
		this.logger=logger;
	}

	@Override
	public void run() {
		this.conectarmeReplicador();
		
	}

	public void conectarmeReplicador() {
		try {
			this.socket = new Socket(this.ip,7567);
			this.flujoSaliente = new ObjectOutputStream (this.socket.getOutputStream());
			this.flujoEntrante = new ObjectInputStream (this.socket.getInputStream());
			System.out.println("[DEBUG] Conectado a Replicador");
			this.logger.guardar("HiloBackup", "conectado con el servidor primario en: "+ip+":"+7567);
			
			//ACA VA A EMPEZAR A RECIBIR MSJ DE ACTUALIZACION A LA BD
			this.conectado=true;
			String resultado;
			Integer cont = 0;
			Integer id_ultimo_bloque = 0;
			this.enviarVersionDB ();
			while(this.conectado){
				try {
					MensajeReplicacion msj= (MensajeReplicacion)this.flujoEntrante.readObject();
					this.bd.guardarMensajeReplicado (msj);
					switch(msj.getCodigo()){
					case asignacionTareaUsuario:
						this.procesarAsignacionTareaUsuario ((MensajeAsignacionTareaUsuario) msj);
						break;
					case parcialTarea:
						this.procesarParcialTarea ((MensajeParcialTarea) msj);
						break;
					case resultadoTarea:
						this.procesarResultadoTarea ((MensajeResultadoTarea) msj);
						break;
					case detencionTarea:
						this.procesarDetencionTarea ((MensajeDetencionTarea) msj);
						break;
					case completitudBloque:
						this.procesarCompletitudBloque ((MensajeCompletitudBloque) msj);
						break;
					case asignacionPuntos:
						this.procesarAsignacionPuntos ((MensajeAsignacionPuntos) msj);
						break;
					case generacionBloque:
						this.procesarGeneracionBloque ((MensajeGeneracionBloque) msj);
						break;
					case generacionTarea:
						cont++;
						MensajeGeneracionTarea msjTarea = (MensajeGeneracionTarea) msj;
						this.procesarGeneracionTarea (msjTarea);

						if(cont == this.backup.numTareasPorBloque || (cont > 1 && id_ultimo_bloque != msjTarea.getIdBloque())){
							cont=0;
							
							setChanged();
							notifyObservers(msjTarea);
						}
						id_ultimo_bloque = msjTarea.getIdBloque();
						break;
				}
				} catch (ClassNotFoundException e) {
					this.logger.guardar(e);
				}
			}
			//SI ESTOY ACA ES PORQUE CONECTADO YA ES FALSE
			//EJECUTO METODOS PARA CERRAR LA CONEXION
			this.morir();
			
		} catch (IOException e) {
			this.logger.guardar(e);
		}
	}

	private void enviarVersionDB () {
		long version = this.bd.getVersion ();
		MensajeVersion msg = new MensajeVersion (version);
		try {
			this.flujoSaliente.writeObject (msg);
		} catch (Exception e) {
			this.logger.guardar(e);
		}
	}

/* procesar mensajes recibidos */

	private void procesarAsignacionTareaUsuario (MensajeAsignacionTareaUsuario msjAsignacionTarea) {
		String resultado = "Recibi actualizaci�n: Asignaci�n de tarea a usuario.";
		this.logger.guardar("HiloBackup", resultado);
		setChanged();
		notifyObservers(resultado);
		
		if(this.bd.asignarTareaUsuarioReplicado(msjAsignacionTarea.getTarea().getId(), msjAsignacionTarea.getUsuario().getId(), msjAsignacionTarea.getIdProcesamiento()) == true){
			setChanged();
			notifyObservers(msjAsignacionTarea.getTarea());
		}
	   }

	private void procesarParcialTarea (MensajeParcialTarea msjParcial) {
		String resultado = "Recibi actualizaci�n: Resultado parcial de tarea.";
		this.logger.guardar("HiloBackup", resultado);

		if(this.bd.setParcial(msjParcial.getTarea(), msjParcial.getUsuario().getId()) == true){
			setChanged();
			notifyObservers(msjParcial.getTarea());
		}
	}

	private void procesarResultadoTarea (MensajeResultadoTarea msjResultadoTarea) {
		String resultado = "Recibi actualizaci�n: Resultado final de tarea.";
		this.logger.guardar("HiloBackup", resultado);
		setChanged();
		notifyObservers(resultado);
		
		if (this.bd.setResultado(msjResultadoTarea.getTarea(), msjResultadoTarea.getUsuario().getId()) == true){
			setChanged();
			msjResultadoTarea.getTarea().setEstado (EstadoTarea.completada);
			notifyObservers(msjResultadoTarea.getTarea());
			}	
		}

	private void procesarDetencionTarea (MensajeDetencionTarea msjDetencionTarea) {
		String resultado = "Recibi actualizaci�n: Detencion de tarea.";
		this.logger.guardar("HiloBackup", resultado);
		setChanged();
		notifyObservers(resultado);
		
		boolean tareaSeDetuvo = false;
		if (msjDetencionTarea.getUsuario() != null && msjDetencionTarea.getTarea() != null) {
			tareaSeDetuvo = this.bd.detenerTarea(msjDetencionTarea.getTarea(), msjDetencionTarea.getUsuario().getId());
			if (tareaSeDetuvo) {
				setChanged();
				notifyObservers(msjDetencionTarea.getTarea());
			}
		}
	}

	private void procesarCompletitudBloque (MensajeCompletitudBloque msjBloque) {
		String resultado = "Recibi actualizaci�n: Bloque completado.";
		this.logger.guardar("HiloBackup", resultado);
		setChanged();
		notifyObservers(resultado);
		
		//
		//PARA LA BD, CHEQUEA SI ESTA COMPLETO CUANDO RECIBE UN RESULTADO FINAL, CON EL SETRESULTADO
		//
	}

	private void procesarAsignacionPuntos (MensajeAsignacionPuntos msjPts) {
		String resultado = "Recibi actualizaci�n: Asignaci�n de puntos a usuario.";
		this.logger.guardar("HiloBackup", resultado);
		setChanged();
		notifyObservers(resultado);
		
		this.bd.actualizarPuntos(msjPts.getUsuario().getId(), msjPts.getPuntos());
	   }

	private void procesarGeneracionBloque (MensajeGeneracionBloque msjGenBloque) {
		String resultado = "Recibi actualizaci�n: Generacion de bloque.";
		this.logger.guardar("HiloBackup", resultado);

		this.bd.generarBloqueReplicado (msjGenBloque.getIdBloque());
		setChanged();
		notifyObservers(resultado);
	}

	private void procesarGeneracionTarea (MensajeGeneracionTarea msjTarea) {
		String resultado = "Recibi actualizaci�n: Generacion de tarea.";
		this.logger.guardar("HiloBackup", resultado);

		this.bd.generarTareaReplicada (msjTarea.getIdTarea(), msjTarea.getIdBloque(), msjTarea.getTarea());
		setChanged();
		notifyObservers(resultado);
	}

	private void morir() {
		//RELACION CON BACKUP EN NULL
		this.backup = null;
		
		//LIBERO RESURSOS Y CIERRO SOCKET
		this.bd=null;
		try {	this.flujoEntrante.close(); 	} catch (IOException e) {}
		try { 	this.flujoSaliente.close(); 	} catch (IOException e) {}
		try {	this.socket.close();			} catch (IOException e) {}
				
	}

	public void desconectar() {
		// TODO Auto-generated method stub
		this.conectado = false;
	}

}