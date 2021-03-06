package mensajes.replicacion;

import java.io.Serializable;
import bloquesYTareas.Tarea;
import bloquesYTareas.EstadoTarea;
import baseDeDatos.Usuario;

public class MensajeResultadoTarea extends MensajeReplicacion implements Serializable {

	protected static final long serialVersionUID = 1L;
	private Tarea tarea;
	private Usuario usuario;

	public MensajeResultadoTarea (Tarea tarea, Usuario usuario) {
		super (CodigoMensajeReplicacion.resultadoTarea);
		this.tarea = tarea;
		this.usuario = usuario;
		this.tarea.setEstado (EstadoTarea.completada);
	}

	public Tarea getTarea () {
		return this.tarea;
	}

	public Usuario getUsuario () {
		return this.usuario;
	}

}