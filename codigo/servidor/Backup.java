package servidor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import misc.Loggeador;
import servidor.vista.ServidorVista;

public class Backup extends Primario {
	
private String ipPrimario;
private HiloBackup conexionBackup;
private static Integer tiempoEsperaBackup=500;

	public Backup(String ip, String puerto, Loggeador logger) {
		super();
		this.logger=logger;
		this.logger.guardar("Backup","Me creo como backup");
		this.ipPrimario=ip;
		this.conectarseBD();
		try {
			String ipB = (InetAddress.getLocalHost().getHostAddress());
			this.setIP(ipB);
			this.setPuerto(6666);
		} catch (UnknownHostException e) {
			this.logger.guardar(e);
		}
		this.crearGUIBackup();
	}
	
	private void crearGUIBackup() {
		this.vista = new ServidorVista(this);
		this.vista.setServidorText("Servidor de Backup");
	}
	
	private void crearGUIBackup2() {
		this.vista.crearPanelTrabajo();
		this.vista.crearAreadeBloques(super.obtenerBloquesNoCompletados());
	}
	
	/*
	public void esperarActualizaciones() {
		//pase todo al metodo run
	}
	*/
	
	@Override
	public void run(){
		this.crearGUIBackup2();
		//DESPUES DE QUE YA TENGA LA VISTA, VOY A CREAR EL HILO Q SE COMUNICARA CON REPLICADOR
		conexionBackup = new HiloBackup(this, this.ipPrimario, this.baseDatos,this.logger);
		
		conexionBackup.addObserver(this.vista);
		
		Thread hilo = new Thread(conexionBackup);
		hilo.start();
		//lo hago esperar un poquito a que conexionBackup se conecte con el primario y cambie su estado a conectado
		try {
			Thread.sleep(250);
		} catch (InterruptedException e1) {
		}
		while(conexionBackup.isConectado()){
			try {
				Thread.sleep(tiempoEsperaBackup);
			} catch (InterruptedException e) {
				logger.guardar(e);
			}
		}
		this.pasarAPrimario();
		this.vista.setServidorText("Servidor Primario");
		this.esperarClientes();
	}
	
	public boolean pasarAPrimario(){
		//parte que va en el constructor de primario
		this.vista.setServidorText("Pasando a servidor primario");
		this.estado=EstadoServidor.desconectado;
		this.hilosConexiones = new ArrayList<HiloConexionPrimario>();
		this.hilos = new ArrayList<Thread>();
		this.baseDatos.detenerTareasEnProceso();
		this.logger.guardar("Servidor", "inicio el servicio");
		try {
			this.sha256 = MessageDigest.getInstance ("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			this.logger.guardar(e);
		}
		
		//parte que va dentro del run() de primario
		this.servicioUDP= new BusquedaUDP(Integer.valueOf(puerto),this.logger);
		Thread hiloServicioUDP = new Thread(this.servicioUDP);
		hiloServicioUDP.start();
		this.CrearReplicador();
		
		//desecho objetos de backup
		
		return false;
	}
	
	@Override
	public void desconectarse(){
		System.out.println("Desconectando el backup");
		this.estado = EstadoServidor.desconectado;
		//CIERRO LA CONEXION DE HILO BACKUP
		this.conexionBackup.desconectar();
	}

}
