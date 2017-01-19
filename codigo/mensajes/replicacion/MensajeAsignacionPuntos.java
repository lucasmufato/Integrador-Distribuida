package mensajes.replicacion;

import java.io.Serializable;
import bloquesYTareas.Tarea;
import baseDeDatos.Usuario;

public class MensajeAsignacionPuntos extends MensajeReplicacion implements Serializable {

	protected static final long serialVersionUID = 1L;
	private Integer puntos;
	private Usuario usuario;
	
	public MensajeAsignacionPuntos (Integer puntos, Usuario usuario) {
		super (CodigoMensajeReplicacion.asignacionPuntos);
		
		this.puntos = puntos;
		this.usuario = usuario;
	}

	public Integer getPuntos () {
		return this.puntos;
	}

	public Usuario getUsuario () {
		return this.usuario;
	}

}