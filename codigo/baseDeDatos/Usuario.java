package baseDeDatos;

import java.io.Serializable;

public class Usuario implements Serializable {

	private static final long serialVersionUID = 1L;
	private EstadoUsuario estado;	//si esta conectado, desconectado o no autenticado.
	private Integer id;
	private String nombre;
	private String password;
	private Integer puntos;
	
	public Usuario(){
		this.estado=EstadoUsuario.noLogeado;
	}
	
	public EstadoUsuario getEstado() {
		return estado;
	}
	public void setEstado(EstadoUsuario estado) {
		this.estado = estado;
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Integer getPuntos() {
		return puntos;
	}
	public void setPuntos(Integer puntos) {
		this.puntos = puntos;
	}
	

}
