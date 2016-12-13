package mensajes;

import baseDeDatos.Tarea;

public class MensajeTarea extends Mensaje {

	protected Tarea tarea;
	private static final long serialVersionUID = 1L;
	
	public MensajeTarea(CodigoMensaje codigo, Integer ID_Sesion,Tarea tarea) {
		super(codigo, ID_Sesion);
		this.tarea=tarea;
	}
	
	
	
}
