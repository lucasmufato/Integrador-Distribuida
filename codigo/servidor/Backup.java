package servidor;

public class Backup extends Primario {
	
	
	public Backup(String ip, String puerto) {
		super(ip, puerto);
		HiloBackup conexionBackup = new HiloBackup();
		Thread hilo = new Thread(conexionBackup);
		hilo.start();
	}
	
	public boolean pasarAPrimario(){
		return false;
	}
	
	

}
