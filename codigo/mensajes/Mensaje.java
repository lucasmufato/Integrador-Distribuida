package mensajes;

import java.io.Serializable;

public class Mensaje implements Serializable {
	
	//Esta clase maestra de los mensaje solo tiene el codigo de mensaje y el id de sesion que va en todos los mensajes
	protected static final long serialVersionUID = 3L;
	protected CodigoMensaje codigo;
	protected Integer ID_Sesion;
	
	public Mensaje(CodigoMensaje codigo,Integer ID_Sesion){
		this.codigo=codigo;
		this.ID_Sesion=ID_Sesion;
	}

	public CodigoMensaje getCodigo() {
		return codigo;
	}

	public void setCodigo(CodigoMensaje codigo) {
		this.codigo = codigo;
	}

	public Integer getID_Sesion() {
		return ID_Sesion;
	}

	public void setID_Sesion(Integer iD_Sesion) {
		ID_Sesion = iD_Sesion;
	}
	
}
