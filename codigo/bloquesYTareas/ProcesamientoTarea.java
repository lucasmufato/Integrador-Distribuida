package bloquesYTareas;

import java.math.BigInteger;

public class ProcesamientoTarea {
	private Integer id_procesamiento_tarea;
	private Integer tarea;
	private Integer usuario;
	private byte[] parcial;
	private byte[] resultado;
	private BigInteger resta;
	private BigInteger trabajoRealizado;
	private BigInteger parcialCombinaciones,resultadoCombinaciones;
	
	public BigInteger getResultadoCombinaciones() {
		return resultadoCombinaciones;
	}

	public void setResultadoCombinaciones(BigInteger resultadoCombinaciones) {
		this.resultadoCombinaciones = resultadoCombinaciones;
	}

	public BigInteger getParcialCombinaciones() {
		return parcialCombinaciones;
	}

	public void setParcialCombinaciones(BigInteger parcialCombinaciones) {
		this.parcialCombinaciones = parcialCombinaciones;
	}

	public BigInteger getTrabajo_Realizado() {
		return this.trabajoRealizado;
	}

	public void setTrabajo_Realizado(BigInteger trabajo_Realizado) {
		this.trabajoRealizado = trabajo_Realizado;
	}
	
	public void addTrabajo_Realizado(BigInteger trabajo_realizado){
		this.trabajoRealizado = this.trabajoRealizado.add(trabajo_realizado);
	}

	public BigInteger getResta() {
		return resta;
	}

	public void setResta(BigInteger resta) {
		this.resta = resta;
	}

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
