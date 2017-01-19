package mensajes.replicacion;

import java.io.Serializable;

public class MensajeReplicacion implements Serializable {

	protected static final long serialVersionUID = 1L;
	protected CodigoMensajeReplicacion codigo;
	
	public MensajeReplicacion (CodigoMensajeReplicacion codigo) {
		this.codigo = codigo;
	}

	public CodigoMensajeReplicacion getCodigo () {
		return codigo;
	}

	public void setCodigo (CodigoMensajeReplicacion codigo) {
		this.codigo = codigo;
	}

}
