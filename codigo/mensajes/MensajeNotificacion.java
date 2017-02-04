package mensajes;

public class MensajeNotificacion extends Mensaje {

	private static final long serialVersionUID = 1L;
	private String ipBackup;
	private Integer puertoBackup;
	
	public MensajeNotificacion(CodigoMensaje codigo, Integer ID_Sesion, String ipBackup, Integer puertoBackup) {
		super(codigo, ID_Sesion);
		this.ipBackup = ipBackup;
		this.puertoBackup = puertoBackup;
	}

	public String getIpBackup() {
		return ipBackup;
	}

	public void setIpBackup(String ipBackup) {
		this.ipBackup = ipBackup;
	}

	public Integer getPuertoBackup() {
		return puertoBackup;
	}

	public void setPuertoBackup(Integer puertoBackup) {
		this.puertoBackup = puertoBackup;
	}
}
