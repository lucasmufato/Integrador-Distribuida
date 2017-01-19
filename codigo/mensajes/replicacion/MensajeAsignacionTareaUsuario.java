package mensajes.replicacion;

import java.io.Serializable;
import bloquesYTareas.Tarea;
import baseDeDatos.Usuario;

public class MensajeAsignacionTareaUsuario extends MensajeReplicacion implements Serializable {

	protected static final long serialVersionUID = 1L;
	Tarea tarea;
	Usuario usuario;
	
	public MensajeAsignacionTareaUsuario (Tarea tarea, Usuario usuario) {
		super (CodigoMensajeReplicacion.asignacionTareaUsuario);
		this.tarea = tarea;
		this.usuario = usuario;
	}

	public Tarea getTarea () {
		return this.tarea;
	}

	public Usuario getUsuario () {
		return this.usuario;
	}

}