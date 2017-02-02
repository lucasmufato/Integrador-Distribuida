package mensajes.replicacion;

import java.io.Serializable;
import baseDeDatos.Replicacion;

public class MensajeBD implements Serializable {
	private static final long SerialVersionUID = 1L;

	private Replicacion replicacion;

	public MensajeBD (Replicacion replicacion) {
		this.replicacion = replicacion;
	}

	public Replicacion getReplicacion () {
		return this.replicacion;
	}
}
