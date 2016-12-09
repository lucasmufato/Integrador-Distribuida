package mensajes;

public class MensajeLogeo extends Mensaje {
	
	//clase que se usara para el logeo
	//el cliente envia este mensaje con id_sesion = null, codigo = logeo, y su usuario y contraseÃ±a
	//el servidor si autentica bien, responde con este mensaje, con contraseÃ±a =null y con numero de id_sesion
	
	private static final long serialVersionUID = 1L;
	protected String usuario;
	protected String contraseña;
	
	public MensajeLogeo(CodigoMensaje codigo,Integer ID_Sesion,String usuario,String contraseña){
		super(codigo,ID_Sesion);
		this.usuario=usuario;
		this.contraseña=contraseña;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getContraseña() {
		return contraseña;
	}

	public void setContraseña(String contraseña) {
		this.contraseña = contraseña;
	}
	
	
}
