package mensajes.replicacion;

import java.io.Serializable;
import baseDeDatos.Replicacion;

public class MensajeBD implements Serializable {
	private static final SerialVersionUID = 1L;

	private Replicacion replicacion;

	public MensajeBd (Replicacion replicacion) {
		this.replicacion = replicacion;
	}

	public Replicacion getReplicacion () {
		return this.replicacion;
	}
}
