package mensajes;

public class MensajeLogeo extends Mensaje {
	
	//clase que se usara para el logeo
	//el cliente envia este mensaje con id_sesion = null, codigo = logeo, y su usuario y password
	//el servidor si autentica bien, responde con este mensaje, con password =null y con numero de id_sesion
	
	private static final long serialVersionUID = 1L;
	protected String usuario;
	protected String password;
	
	public MensajeLogeo(CodigoMensaje codigo,Integer ID_Sesion,String usuario,String password){
		super(codigo,ID_Sesion);
		this.usuario=usuario;
		this.password=password;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
