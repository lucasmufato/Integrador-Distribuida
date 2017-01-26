package mensajes.replicacion;

import java.io.Serializable;

public enum CodigoMensajeReplicacion implements Serializable {
	parcialTarea,
	resultadoTarea,
	completitudBloque,
	asignacionTareaUsuario,
	detencionTarea,
	asignacionPuntos,
	generacionBloque,
	generacionTarea
}
