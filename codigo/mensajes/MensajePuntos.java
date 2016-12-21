package mensajes;

public class MensajePuntos extends Mensaje {

	private static final long serialVersionUID = 1L;
	protected Integer idTarea;
	protected Integer puntos;
	
	public MensajePuntos(CodigoMensaje codigo, Integer ID_Sesion, Integer idTarea, Integer puntos) {
		super(codigo, ID_Sesion);
		this.idTarea = idTarea;
		this.puntos = puntos;

	}

	public Integer getIdTarea() {
		return idTarea;
	}

	public void setIdTarea(Integer idTarea) {
		this.idTarea = idTarea;
	}

	public Integer getPuntos() {
		return puntos;
	}

	public void setPuntos(Integer puntos) {
		this.puntos = puntos;
	}
}