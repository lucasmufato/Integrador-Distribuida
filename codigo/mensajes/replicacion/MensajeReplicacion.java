package mensajes.replicacion;

import java.io.Serializable;
import java.util.Date;

public class MensajeReplicacion implements Serializable {

	protected static final long serialVersionUID = 1L;
	protected CodigoMensajeReplicacion codigo;
	protected long nroVersion;
	protected Date fecha;
	
	public MensajeReplicacion (CodigoMensajeReplicacion codigo) {
		this.codigo = codigo;
	}

	public CodigoMensajeReplicacion getCodigo () {
		return codigo;
	}

	public void setCodigo (CodigoMensajeReplicacion codigo) {
		this.codigo = codigo;
	}

	public Date getFecha () {
		return this.fecha;
	}

	public void setFecha (Date fecha) {
		this.fecha = fecha;
	}

	public long getNroVersion () {
		return this.nroVersion;
	}

	public void setNroVersion (long nroVersion) {
		this.nroVersion = nroVersion;
	}
}
