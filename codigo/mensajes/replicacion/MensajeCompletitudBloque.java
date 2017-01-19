package mensajes.replicacion;

import java.io.Serializable;
import bloquesYTareas.Bloque;

public class MensajeCompletitudBloque extends MensajeReplicacion implements Serializable {

	protected static final long serialVersionUID = 1L;
	private Bloque bloque;
	
	public MensajeCompletitudBloque (Bloque bloque) {
		super (CodigoMensajeReplicacion.completitudBloque);
		this.bloque = bloque;
	}

	public Bloque getBloque () {
		return this.bloque;
	}
}