package bloquesYTareas;

import java.util.ArrayList;

public class Bloque {
	protected Integer id;
	protected EstadoBloque estado;
	protected ArrayList<Tarea> tareas;
	
	public Bloque(Integer id){
		this.id=id;
		this.estado=EstadoBloque.noTerminado;
		this.tareas = new ArrayList<Tarea>();
	}
	
	public Bloque(Integer id, EstadoBloque estado, ArrayList<Tarea> tareas){
		this.id=id;
		this.estado=estado;
		this.tareas = tareas;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public EstadoBloque getEstado() {
		return estado;
	}

	public void setEstado(EstadoBloque estado) {
		this.estado = estado;
	}

	public ArrayList<Tarea> getTareas() {
		return tareas;
	}

	public void setTareas(ArrayList<Tarea> tareas) {
		this.tareas = tareas;
	}
	
	
	
}
