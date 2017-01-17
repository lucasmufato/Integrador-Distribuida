package servidor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Properties;

public class Servidor {

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Servidor servidor = new Servidor();
	}
	
	public Servidor(){
		this.buscarServidorPrimario();
	}
	
	public boolean buscarServidorPrimario(){
		//lee el archivo con las ips
		//intenta de contactarse con el servidor primario
		//si no lo logra se crea un servidor primario
		//si se conecta con un servidor primario, crea un servidor backup
		
		Properties propiedades = new Properties();
	    InputStream entrada = null;
	    Socket prueba;
	    try {
			entrada = new FileInputStream("configuracion.properties");
			propiedades.load(entrada);
		    
	    	//VOY A INTENTAR CONECTARME AL PRIMARIO
		    String ipA = propiedades.getProperty("IPA");
		    String puertoA = propiedades.getProperty("PUERTOA");
		    
		    String ipB = propiedades.getProperty("IPB");
		    String puertoB = propiedades.getProperty("PUERTOB");
		    
		    //CHEQUEO SI EL SERVIDOR PRIMARIO ESTA ESCUCHANDO EN EL PUERTO
		    try {
		    	prueba = new Socket(ipA,Integer.parseInt(puertoA));	
		    	System.out.println("El servidor primario ya esta levantado, se creara el backup");
		       	this.crearServidorBackup(ipB, puertoB);
		    }catch (ConnectException e){
		    	System.out.println("Se creara el servidor primario");
		    	this.crearServidorPrimario(ipA, puertoA);
		    }
		    
	    } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean crearServidorPrimario(String ip, String puerto){
		//crea una instancia de primario		
		Primario primario = new Primario(ip,puerto);
		return true;
	}
	
	public boolean crearServidorBackup(String ip, String puerto){
		//recibe ip y puerto del backup
		System.out.println("Entre al metodo del servidor backup");
		Backup backup = new Backup(ip,puerto);
		return false;
	}
}
