package mensajes.replicacion;

import java.io.Serializable;
import bloquesYTareas.Tarea;
import baseDeDatos.Usuario;

public class MensajeDetencionTarea extends MensajeReplicacion implements Serializable {

	protected static final long serialVersionUID = 1L;
	private Tarea tarea;
	private Usuario usuario;
	
	public MensajeDetencionTarea (Tarea tarea, Usuario usuario) {
		super (CodigoMensajeReplicacion.detencionTarea);
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