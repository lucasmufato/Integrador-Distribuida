package bloquesYTareas;

public class ProcesamientoTarea {
	private Integer id_procesamiento_tarea;
	private Integer tarea;
	private Integer usuario;
	private byte[] parcial;
	private byte[] resultado;
	
	public Integer getId_procesamiento_tarea() {
		return id_procesamiento_tarea;
	}
	
	public void setId_procesamiento_tarea(Integer id_procesamiento_tarea) {
		this.id_procesamiento_tarea = id_procesamiento_tarea;
	}
	
	public Integer getTarea() {
		return tarea;
	}
	
	public void setTarea(Integer tarea) {
		this.tarea = tarea;
	}
	
	public Integer getUsuario() {
		return usuario;
	}
	
	public void setUsuario(Integer usuario) {
		this.usuario = usuario;
	}
	
	public byte[] getParcial() {
		return parcial;
	}
	
	public void setParcial(byte[] parcial) {
		this.parcial = parcial;
	}
	
	public byte[] getResultado() {
		return resultado;
	}
	
	public void setResultado(byte[] resultado) {
		this.resultado = resultado;
	}
	
	
	
}
