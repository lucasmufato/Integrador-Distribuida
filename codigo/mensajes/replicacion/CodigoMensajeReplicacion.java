package mensajes.replicacion;

import java.io.Serializable;

public enum CodigoMensajeReplicacion implements Serializable {
	//en la BD estan en este orden, pero las FK empiezan desde 1.
	parcialTarea,
	resultadoTarea,
	completitudBloque,
	asignacionTareaUsuario,
	detencionTarea,
	asignacionPuntos,
	generacionBloque,
	generacionTarea,
	version
}
