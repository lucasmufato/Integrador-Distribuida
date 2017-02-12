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
			this.conectado=true;
			this.flujoSaliente.writeObject(backup.getPuerto());
		} catch (IOException e) {
			this.logger.guardar(e);
		}
			//ACA VA A EMPEZAR A RECIBIR MSJ DE ACTUALIZACION A LA BD
			Integer cont = 0;
			Integer id_ultimo_bloque = 0;
			this.enviarVersionDB ();
			while(this.conectado){
				try {
					MensajeReplicacion msj= (MensajeReplicacion)this.flujoEntrante.readObject();
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
						this.backup.vista.actualizarInfoBloques(this.backup.actualizarBloquesVista());
						break;
					case asignacionPuntos:
						this.procesarAsignacionPuntos ((MensajeAsignacionPuntos) msj);
						break;
					case generacionBloque:
						this.procesarGeneracionBloque ((MensajeGeneracionBloque) msj);
						this.backup.vista.actualizarInfoBloques(this.backup.actualizarBloquesVista());
						break;
					case generacionTarea:
						cont++;
						MensajeGeneracionTarea msjTarea = (MensajeGeneracionTarea) msj;
						this.procesarGeneracionTarea (msjTarea);

						if(cont == Backup.numTareasPorBloque || (cont > 1 && id_ultimo_bloque != msjTarea.getIdBloque())){
							cont=0;
							
							setChanged();
							notifyObservers(msjTarea);
						}
						id_ultimo_bloque = msjTarea.getIdBloque();
						break;
					default:
						break;
					}
				this.bd.guardarMensajeReplicado (msj);
				} catch (ClassNotFoundException e) {
					this.logger.guardar(e);
				} catch (IOException e) {
					this.logger.guardar("Backup", "perdi la conexion con el primario, guardando error");
					this.logger.guardar(e);
					this.conectado=false;
				}
			}
			//SI ESTOY ACA ES PORQUE CONECTADO YA ES FALSE
			//EJECUTO METODOS PARA CERRAR LA CONEXION
			this.morir();
			
		
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
		String resultado = "Recibi actualizacion: Asignaci�n de tarea a usuario.";
		this.logger.guardar("HiloBackup", resultado);
		setChanged();
		notifyObservers(resultado);
		
		if(this.bd.asignarTareaUsuarioReplicado(msjAsignacionTarea.getTarea().getId(), msjAsignacionTarea.getUsuario().getId(), msjAsignacionTarea.getIdProcesamiento()) == true){
			setChanged();
			notifyObservers(msjAsignacionTarea.getTarea());
		}
	   }

	private void procesarParcialTarea (MensajeParcialTarea msjParcial) {
		String resultado = "Recibi actualizacion: Resultado parcial de tarea.";
		this.logger.guardar("HiloBackup", resultado);

		if(this.bd.setParcial(msjParcial.getTarea(), msjParcial.getUsuario().getId()) == true){
			setChanged();
			notifyObservers(msjParcial.getTarea());
		}
	}

	private void procesarResultadoTarea (MensajeResultadoTarea msjResultadoTarea) {
		String resultado = "Recibi actualizacion: Resultado final de tarea.";
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
		String resultado = "Recibi actualizacion: Detencion de tarea.";
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
		String resultado = "Recibi actualizacion: Bloque completado.";
		this.logger.guardar("HiloBackup", resultado);
		setChanged();
		notifyObservers(resultado);
		
		//
		//PARA LA BD, CHEQUEA SI ESTA COMPLETO CUANDO RECIBE UN RESULTADO FINAL, CON EL SETRESULTADO
		//
	}

	private void procesarAsignacionPuntos (MensajeAsignacionPuntos msjPts) {
		String resultado = "Recibi actualizacion: actualizacion de puntos a usuario.";
		this.logger.guardar("HiloBackup", resultado);
		setChanged();
		notifyObservers(resultado);
		
		this.bd.actualizarPuntos(msjPts.getUsuario().getId(), msjPts.getPuntos());
	   }

	private void procesarGeneracionBloque (MensajeGeneracionBloque msjGenBloque) {
		String resultado = "Recibi actualizacion: Generacion de bloque.";
		this.logger.guardar("HiloBackup", resultado);

		this.bd.generarBloqueReplicado (msjGenBloque.getIdBloque());
		setChanged();
		notifyObservers(resultado);
	}

	private void procesarGeneracionTarea (MensajeGeneracionTarea msjTarea) {
		String resultado = "Recibi actualizacion: Generacion de tarea.";
		this.logger.guardar("HiloBackup", resultado);

		this.bd.generarTareaReplicada (msjTarea.getIdTarea(), msjTarea.getIdBloque(), msjTarea.getTarea());
		setChanged();
		notifyObservers(resultado);
	}

	private void morir() {
		this.desconectar();
		//RELACION CON BACKUP EN NULL
		this.backup = null;
		
		//LIBERO RESURSOS Y CIERRO SOCKET
		this.bd=null;
		try {	this.flujoEntrante.close(); 	} catch (Exception e) {}
		try { 	this.flujoSaliente.close(); 	} catch (Exception e) {}
		try {	this.socket.close();			} catch (Exception e) {}
				
	}

	public void desconectar() {
		this.conectado = false;
	}

	public boolean isConectado() {
		return conectado;
	}

	public void setConectado(boolean conectado) {
		this.conectado = conectado;
	}

}
