package mensajes.replicacion;

import java.io.Serializable;
import bloquesYTareas.Tarea;
import baseDeDatos.Usuario;

public class MensajeParcialTarea extends MensajeReplicacion implements Serializable {

	protected static final long serialVersionUID = 1L;
	private Tarea tarea;
	private Usuario usuario;
	
	public MensajeParcialTarea (Tarea tarea, Usuario usuario) {
		super (CodigoMensajeReplicacion.parcialTarea);
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