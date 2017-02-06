package mensajes.replicacion;

import java.io.Serializable;

public class MensajeVersion extends MensajeReplicacion implements Serializable {
	protected static final long serialVersionUID = 1L;
	
	private long version;
	
	public MensajeVersion (long version) {
		super (CodigoMensajeReplicacion.version);
		this.version = version;
	}

	public long getVersion() {
		return this.version;
	}
}