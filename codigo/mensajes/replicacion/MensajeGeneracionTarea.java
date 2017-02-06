package mensajes.replicacion;

import java.io.Serializable;


public class MensajeGeneracionTarea extends MensajeReplicacion implements Serializable {
	protected static final long serialVersionUID = 1L;
	private Integer idTarea;
	private Integer idBloque;
	private byte[]  tarea;

	public MensajeGeneracionTarea (Integer idTarea, Integer idBloque, byte[] tarea) {
		super(CodigoMensajeReplicacion.generacionTarea);
		this.idTarea = idTarea;
		this.idBloque = idBloque;
		this.tarea = tarea;
	}

	public Integer getIdTarea () {
		return this.idTarea;
	}
	
	public Integer getIdBloque () {
		return this.idBloque;
	}
	
	public byte[] getTarea () {
		return this.tarea;
	}
}