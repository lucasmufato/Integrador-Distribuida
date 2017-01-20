package servidor;

import servidor.vista.ServidorBackupVista;

public class Backup extends Primario {
	
	private ServidorBackupVista vistaB;	
	
	public Backup(String ip, String puerto) {
		super(ip, puerto);
		this.crearGUIBackup();
		//DESPUES DE QUE YA TENGA LA VISTA, VOY A CREAR EL HILO Q SE COMUNICARA CON REPLICADOR
		HiloBackup conexionBackup = new HiloBackup();
		Thread hilo = new Thread(conexionBackup);
		hilo.start();
	}
	
	private void crearGUIBackup() {
		//VA A TENER UNA SOLA VISTA, CON LOS BLOQUE, TAREAS E INFO DE ACTUALIZACION QUE RECIBA
		this.vistaB = new ServidorBackupVista(this);	
	}

	public boolean pasarAPrimario(){
		return false;
	}
	
	

}
