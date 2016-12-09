package mensajes;

public class MensajeLogeo extends Mensaje {
	
	//clase que se usara para el logeo
	//el cliente envia este mensaje con id_sesion = null, codigo = logeo, y su usuario y contraseña
	//el servidor si autentica bien, responde con este mensaje, con contraseña =null y con numero de id_sesion
	
	private static final long serialVersionUID = 1L;
	protected String usuario;
	protected String contrase�a;
	
	public MensajeLogeo(CodigoMensaje codigo,Integer ID_Sesion,String usuario,String contrase�a){
		super(codigo,ID_Sesion);
		this.usuario=usuario;
		this.contrase�a=contrase�a;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getContrase�a() {
		return contrase�a;
	}

	public void setContrase�a(String contrase�a) {
		this.contrase�a = contrase�a;
	}
	
	
}
