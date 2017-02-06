package mensajes.replicacion;

import java.io.Serializable;

public class MensajeGeneracionBloque extends MensajeReplicacion implements Serializable {
	protected static final long serialVersionUID = 1L;
	private Integer idBloque;

	public MensajeGeneracionBloque (Integer idBloque) {
		super(CodigoMensajeReplicacion.generacionBloque);
		this.idBloque = idBloque;
	}

	public Integer getIdBloque () {
		return this.idBloque;
	}
	
}