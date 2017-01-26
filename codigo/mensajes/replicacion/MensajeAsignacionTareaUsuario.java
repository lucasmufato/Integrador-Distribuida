package mensajes.replicacion;

import java.io.Serializable;
import bloquesYTareas.Tarea;
import baseDeDatos.Usuario;

public class MensajeAsignacionTareaUsuario extends MensajeReplicacion implements Serializable {

	protected static final long serialVersionUID = 1L;
	Tarea tarea;
	Usuario usuario;
	Integer idProcesamiento;
	
	public MensajeAsignacionTareaUsuario (Tarea tarea, Usuario usuario, Integer idProcesamiento) {
		super (CodigoMensajeReplicacion.asignacionTareaUsuario);
		this.tarea = tarea;
		this.usuario = usuario;
		this.idProcesamiento = idProcesamiento;
	}

	public Tarea getTarea () {
		return this.tarea;
	}

	public Usuario getUsuario () {
		return this.usuario;
	}

	public Integer getIdProcesamiento () {
		return this.idProcesamiento;
	}
}