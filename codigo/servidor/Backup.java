package servidor;

import java.net.InetAddress;
import java.net.UnknownHostException;

import servidor.vista.ServidorVista;

public class Backup extends Primario {
	
private ServidorVista vistaB;	
private String ipPrimario;
private String puertoPrimario;
private HiloBackup conexionBackup;

	public Backup(String ip, String puerto) {
		super();
		this.ipPrimario=ip;
		this.puertoPrimario=puerto;
		try {
			String ipB = (InetAddress.getLocalHost().getHostAddress());
			this.setIP(ipB);
			this.setPuerto(6666);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.crearGUIBackup();
	}
	
	private void crearGUIBackup() {
		this.vistaB = new ServidorVista(this);	
	}
	
	private void crearGUIBackup2() {
		this.vistaB.crearPanelTrabajo();
		//this.vistaB.crearAreadeBloques(super.obtenerBloquesNoCompletados());
	}
	
	public void esperarActualizaciones() {
		this.crearGUIBackup2();
		//DESPUES DE QUE YA TENGA LA VISTA, VOY A CREAR EL HILO Q SE COMUNICARA CON REPLICADOR
		/*conexionBackup = new HiloBackup(this.ipPrimario);
		Thread hilo = new Thread(conexionBackup);
		hilo.start();*/
	}
	
	public boolean pasarAPrimario(){
		return false;
	}
	
	@Override
	public void desconectarse(){
		System.out.println("Desconectando el backup");
		this.estado = EstadoServidor.desconectado;
		//CIERO LA CONEXION DE HILO BACKUP
		this.conexionBackup.desconectar();
	}

}
