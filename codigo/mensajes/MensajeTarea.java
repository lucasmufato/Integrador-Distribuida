package mensajes;

import bloquesYTareas.*;

public class MensajeTarea extends Mensaje {

	protected Tarea tarea;
	private static final long serialVersionUID = 1L;
	
	public MensajeTarea(CodigoMensaje codigo, Integer ID_Sesion,Tarea tarea) {
		super(codigo, ID_Sesion);
		this.tarea=tarea;
	}

	public Tarea getTarea() {
		return tarea;
	}

	public void setTarea(Tarea tarea) {
		this.tarea = tarea;
	}
	
	
	
}
