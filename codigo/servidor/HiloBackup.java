package servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Observable;

import baseDeDatos.BaseDatos;
import mensajes.replicacion.MensajeAsignacionPuntos;
import mensajes.replicacion.MensajeAsignacionTareaUsuario;
import mensajes.replicacion.MensajeCompletitudBloque;
import mensajes.replicacion.MensajeDetencionTarea;
import mensajes.replicacion.MensajeParcialTarea;
import mensajes.replicacion.MensajeReplicacion;
import mensajes.replicacion.MensajeResultadoTarea;
import mensajes.replicacion.MensajeGeneracionBloque;
import mensajes.replicacion.MensajeGeneracionTarea;

public class HiloBackup extends Observable implements Runnable {
	//SOCKET PARA LA COMUNICACION CON EL REPLICADOR
	private Socket socket; 
	private ObjectOutputStream flujoSaliente;  //RECIBIR OBJETOS
	private ObjectInputStream flujoEntrante;  //ENVIAR OBJETOS

	private boolean conectado;
	private String ip;
	private BaseDatos bd;
	private Backup backup;

	public HiloBackup(Backup backup, String ipPrimario, BaseDatos bd) {
		this.backup = backup;
		this.ip = ipPrimario;
		this.bd = bd;
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
			
			//ACA VA A EMPEZAR A RECIBIR MSJ DE ACTUALIZACION A LA BD
			this.conectado=true;
			String resultado;
			Integer cont = 0;
			while(this.conectado){
				try {
					MensajeReplicacion msj= (MensajeReplicacion)this.flujoEntrante.readObject();
					System.out.println("[DEBUG] Se ha recibido un mensaje del Replicador");
					switch(msj.getCodigo()){
					case asignacionTareaUsuario:
						MensajeAsignacionTareaUsuario msjAsignacionTarea = (MensajeAsignacionTareaUsuario) msj;
						resultado = "Recibi actualización: Asignación de tarea a usuario.";
						System.out.println(resultado);
						//MARCO QUE CAMBIO EL OBJETO
				        setChanged();
				        //NOTIFICO EL CAMBIO
				        notifyObservers(resultado);
				        
				        if(this.bd.asignarTareaUsuarioReplicado(msjAsignacionTarea.getTarea().getId(), msjAsignacionTarea.getUsuario().getId(), msjAsignacionTarea.getIdProcesamiento()) == true){
				        	setChanged();
							notifyObservers(msjAsignacionTarea.getTarea());
				        }
						break;
					case parcialTarea:
						MensajeParcialTarea msjParcial = (MensajeParcialTarea)msj;
						resultado = "Recibi actualización: Resultado parcial de tarea.";
						System.out.println(resultado);
						//MARCO QUE CAMBIO EL OBJETO
				        setChanged();
				        //NOTIFICO EL CAMBIO
				        notifyObservers(resultado);
				        
				        if(this.bd.setParcial(msjParcial.getTarea(), msjParcial.getUsuario().getId()) == true){
							setChanged();
							notifyObservers(msjParcial.getTarea());
						}
						break;
					case resultadoTarea:
						MensajeResultadoTarea msjResultadoTarea = (MensajeResultadoTarea)msj;
						resultado = "Recibi actualización: Resultado final de tarea.";
						System.out.println(resultado);
						//MARCO QUE CAMBIO EL OBJETO
				        setChanged();
				        //NOTIFICO EL CAMBIO
				        notifyObservers(resultado);
						
				        if (this.bd.setResultado(msjResultadoTarea.getTarea(), msjResultadoTarea.getUsuario().getId()) == true){
							setChanged();
							notifyObservers(msjResultadoTarea.getTarea());
							}	
				        break;
					case detencionTarea:
						MensajeDetencionTarea msjDetencionTarea = (MensajeDetencionTarea)msj;
						resultado = "Recibi actualización: Detencion de tarea.";
						System.out.println(resultado);
						//MARCO QUE CAMBIO EL OBJETO
				        setChanged();
				        //NOTIFICO EL CAMBIO
				        notifyObservers(resultado);
				        
				        boolean tareaSeDetuvo = false;
						if (msjDetencionTarea.getUsuario() != null && msjDetencionTarea.getTarea() != null) {
							tareaSeDetuvo = this.bd.detenerTarea(msjDetencionTarea.getTarea(), msjDetencionTarea.getUsuario().getId());
							if (tareaSeDetuvo) {
								setChanged();
								notifyObservers(msjDetencionTarea.getTarea());
							}
						}
						break;
					case completitudBloque:
						MensajeCompletitudBloque msjBloque = (MensajeCompletitudBloque)msj;
						resultado = "Recibi actualización: Bloque completado.";
						System.out.println(resultado);
						//MARCO QUE CAMBIO EL OBJETO
				        setChanged();
				        //NOTIFICO EL CAMBIO
				        notifyObservers(resultado);
				        
				        //
				        //PARA LA BD, CHEQUEA SI ESTA COMPLETO CUANDO RECIBE UN RESULTADO FINAL, CON EL SETRESULTADO
				        //
						break;
					case asignacionPuntos:
						MensajeAsignacionPuntos msjPts = (MensajeAsignacionPuntos)msj;
						resultado = "Recibi actualización: Asignación de puntos a usuario.";
						System.out.println(resultado);
				        setChanged();
				        notifyObservers(resultado);
				        
				        this.bd.actualizarPuntos(msjPts.getUsuario().getId(), msjPts.getPuntos());
						break;
					case generacionBloque:
						MensajeGeneracionBloque msjGenBloque = (MensajeGeneracionBloque) msj;
						this.bd.generarBloqueReplicado (msjGenBloque.getIdBloque());
						resultado = "Recibi actualización: Generacion de bloque.";
						System.out.println(resultado);
						setChanged();
						notifyObservers(resultado);
						
						break;
					case generacionTarea:
						cont++;
						MensajeGeneracionTarea msjTarea = (MensajeGeneracionTarea) msj;
						this.bd.generarTareaReplicada (msjTarea.getIdTarea(), msjTarea.getIdBloque(), msjTarea.getTarea());
						resultado = "Recibi actualización: Generacion de tarea.";
						setChanged();
						notifyObservers(resultado);
						if(cont == this.backup.numTareasPorBloque){
							cont=0;
							
							setChanged();
							notifyObservers(msjTarea);
						}
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