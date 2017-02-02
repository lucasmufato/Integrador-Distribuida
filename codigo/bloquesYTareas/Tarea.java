package bloquesYTareas;

import java.io.Serializable;
import java.util.Arrays;

public class Tarea implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	protected Integer id;
	protected Bloque bloque;
	protected byte[] tarea;
	protected byte[] parcial;
	protected byte[] resultado;
	protected byte[] limiteSuperior;
	protected EstadoTarea estado;
	
	public Tarea(Bloque bloque, byte[] tarea, byte[] parcial){
		this.bloque= bloque;
		this.tarea=tarea;
		this.parcial= parcial;
		this.resultado=null;
		this.limiteSuperior = new byte[32];
		Arrays.fill (this.limiteSuperior, (byte) 0x00);
		this.limiteSuperior[3] = (byte) 0x80;
	}

	public Tarea (Bloque bloque, byte[] tarea, byte[] parcial,byte[] resultado){
		this.bloque= bloque;
		this.tarea=tarea;
		this.parcial= parcial;
		this.resultado=resultado;
		this.limiteSuperior = new byte[32];
		Arrays.fill (this.limiteSuperior, (byte) 0x00);
		this.limiteSuperior[3] = (byte) 0x80;
	}
	
	public byte[] getTareaRespuesta(){
		//metodo que devuelve la tarea y la respuesta en un solo byte[]
		byte[] todo = new byte[this.tarea.length+this.resultado.length];
		System.arraycopy(this.tarea, 0, todo, 0, this.tarea.length);
		System.arraycopy(this.resultado, 0, todo, this.tarea.length, this.resultado.length);
		return todo;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
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
	public byte[] getLimiteSuperior() {
		return limiteSuperior;
	}
	public void setLimiteSuperior(byte[] limiteSuperior) {
		this.limiteSuperior = limiteSuperior;
	}
	public void SetLimite(Integer posicionByte, byte byteLimite){
		this.limiteSuperior = new byte[32];
		Arrays.fill (this.limiteSuperior, (byte) 0x00);
		this.limiteSuperior[posicionByte] = byteLimite;
	}
	public EstadoTarea getEstado() {
		synchronized (this.estado) {
			return estado;
		}
	}

	public void setEstado(EstadoTarea estado) {
		this.estado=estado;
	}
	
	public String toString() {
		if(this.tarea == null){
			return "";
		}
    	StringBuilder builder = new StringBuilder();
    	for(byte b : this.tarea) {
    	    builder.append(String.format("%02x ", b));
    	}
    	return builder.toString();
	}
	public static String bytesToString (byte[] bytes) {
	if(bytes == null){
			return "";
		}
    	StringBuilder builder = new StringBuilder();
    	for(byte b : bytes) {
    	    builder.append(String.format("%02x ", b));
    	}
    	return builder.toString();
	}
}
