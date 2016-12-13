package baseDeDatos;

public class Tarea {
	private Bloque bloque;
	private byte[] tarea;
	private byte[] parcial;
	private byte[] resultado;
	
	public Tarea(Bloque bloque, byte[] tarea, byte[] parcial){
		this.bloque= bloque;
		this.tarea=tarea;
		this.parcial= parcial;
		this.resultado=null;
	}
	
	public Tarea (Bloque bloque, byte[] tarea, byte[] parcial,byte[] resultado){
		this.bloque= bloque;
		this.tarea=tarea;
		this.parcial= parcial;
		this.resultado=resultado;
	}
	
	public Bloque getBloque() {
		return bloque;
	}
	public void setBloque(Bloque bloque) {
		this.bloque = bloque;
	}
	public byte[] getTarea() {
		return tarea;
	}
	public void setTarea(byte[] tarea) {
		this.tarea = tarea;
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
