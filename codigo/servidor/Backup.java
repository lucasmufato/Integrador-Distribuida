package servidor;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import misc.Loggeador;
import servidor.vista.ServidorVista;

public class Backup extends Primario {
	
private String ipPrimario;
private String puertoPrimario;
private HiloBackup conexionBackup;
private static Integer tiempoEsperaBackup=500;

	public Backup(String ip, String puerto, Loggeador logger) {
		super();
		this.logger=logger;
		this.logger.guardar("Backup","Me creo como backup");
		this.IP=this.conseguirIP();
		this.ipPrimario=ip;
		this.puertoPrimario = puerto;
		this.conectarseBD();
		this.crearGUIBackup();
	}
	
	private void crearGUIBackup() {
		this.vista = new ServidorVista(this);
		this.vista.setServidorText("Servidor de Backup");
		this.vista.setNombrePanelInformacio("Primario");
	}
	
	private void crearGUIBackup2() {
		this.vista.crearPanelTrabajo();
		this.vista.crearAreadeBloques(super.obtenerBloquesNoCompletados());
	}
	
	@Override
	public void run(){
		this.crearGUIBackup2();
		//DESPUES DE QUE YA TENGA LA VISTA, VOY A CREAR EL HILO Q SE COMUNICARA CON REPLICADOR
		conexionBackup = new HiloBackup(this, this.ipPrimario, this.baseDatos,this.logger);
		this.vista.actualizarDatosBackup(this.ipPrimario, Integer.parseInt(this.puertoPrimario));
		this.vista.actualizarInfoBloques(this.actualizarBloquesVista());
		conexionBackup.addObserver(this.vista);
		
		Thread hilo = new Thread(conexionBackup);
		hilo.start();
		
		//lo hago esperar un poquito a que conexionBackup se conecte con el primario y cambie su estado a conectado
		try {
			Thread.sleep(350);
		} catch (InterruptedException e1) {
		}
		while(conexionBackup.isConectado()){
			try {
				Thread.sleep(tiempoEsperaBackup);
			} catch (InterruptedException e) {
				logger.guardar(e);
			}
		}
		if( this.pasarAPrimario() ){
			this.vista.setServidorText("Servidor Primario");
			this.esperarClientes();
		}else{
			this.vista.MostrarPopUp("error al cambiar a servidor primario. cerrame");
		}
		
	}
	
	public boolean pasarAPrimario(){
		//parte que va en el constructor de primario
		this.vista.setServidorText("Pasando a servidor primario");
		this.vista.setTitle("Servidor BitCoin Mining");
		this.vista.mostrarBotonGenerarBloques();
		this.estado=EstadoServidor.desconectado;
		this.hilosConexiones = new ArrayList<HiloConexionPrimario>();
		this.hilos = new ArrayList<Thread>();
		this.baseDatos.detenerTareasEnProceso();
		this.logger.guardar("Servidor", "inicio el servicio");
		try {
			this.sha256 = MessageDigest.getInstance ("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			this.logger.guardar(e);
			return false;
		}
		
		//parte que va dentro del run() de primario
		this.servicioUDP= new BusquedaUDP(Integer.valueOf(puerto),this.logger);
		Thread hiloServicioUDP = new Thread(this.servicioUDP);
		hiloServicioUDP.start();
		this.CrearReplicador();
		
		//desecho objetos de backup
		this.conexionBackup=null;
		this.vista.setNombrePanelInformacio("Backup");
		this.vista.actualizarDatosBackup(null, null);
		try {
			this.serverSO.setSoTimeout(Primario.tiempoEspera);
		} catch (IOException e) {
			this.logger.guardar(e);
			return false;
		}
		
		return true;
	}
	
	@Override
	public void desconectarse(){
		System.out.println("Desconectando el backup");
		this.estado = EstadoServidor.desconectado;
		//CIERRO LA CONEXION DE HILO BACKUP
		if(this.conexionBackup!=null){
			this.conexionBackup.desconectar();
		}
		
	}
	
}
