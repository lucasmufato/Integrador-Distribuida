package servidor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Properties;

public class Servidor {

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Servidor servidor = new Servidor();
	}
	
	public Servidor(){
		this.buscarServidorPrimario();
	}
	
	public boolean buscarServidorPrimarioBroadcast(){
		
		return false;
	}
	
	public boolean buscarServidorPrimario(){
		//busca por broadcast en la red, si no tiene respuesta
		//lee el archivo con las ips,intenta de contactarse con el servidor primario
		//si no lo logra se crea un servidor primario,si se conecta con un servidor primario, crea un servidor backup
		
		String ipPrimario=null;
		try {
			ipPrimario=BusquedaUDP.buscarPrimario();
		} catch (SocketException | UnknownHostException e1) {
			System.out.println("no pude buscar el servidor primario en la red local");
		}
		
		if(ipPrimario!=null){
			Integer puerto= BusquedaUDP.puertoPrimario;
			System.out.println("conectadose al servidor primario encontrado en la red local: ip-"+ipPrimario+" puerto-"+puerto);
			this.crearServidorBackup(ipPrimario, puerto.toString());
			return true;
		}
		
		Properties propiedades = new Properties();
	    InputStream entrada = null;
	   
	    try {
	    	
	    	System.out.println("buscando servidores en base al archivo de configuracion");
			entrada = new FileInputStream("C:/Users/jasmin/Documents/Integrador-Distribuida/codigo/servidor/configuracion.properties");
			propiedades.load(entrada);
			
			//leo la lista de ip-puerto hasta que se acabe o me conecte con un servidor primario
			
		    Integer nro=1;
		    boolean hayIP=true;	//para saber cuando se acabaron las ips
		    boolean conectado=false;
		    
			while(hayIP==true && conectado==false){
				String ip = propiedades.getProperty("IP"+nro);
			    String puerto = propiedades.getProperty("PUERTO"+nro);
			    if(ip==null){
			    	hayIP=false;
			    }
			    
			  //CHEQUEO SI EL SERVIDOR PRIMARIO ESTA ESCUCHANDO EN EL PUERTO
				if(hayIP){
					try {
				    	Socket prueba = new Socket(ip,Integer.parseInt(puerto));
				    	System.out.println("El servidor primario ya esta levantado, se creara el backup");
				    	conectado=true;
				       	this.crearServidorBackup(ip, puerto);
				    }catch (Exception e){
				    	/*
				    	System.out.println("Se creara el servidor primario");
				    	this.crearServidorPrimario(ipA, puertoA);
				    	*/
				    }
				}
				nro++;
			}
			if(conectado==false && hayIP==false){
				System.out.println("Creando servidor primario.");
				try {
					String ip = (InetAddress.getLocalHost().getHostAddress());
					this.crearServidorPrimario(ip, "5555");
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
	    } catch (FileNotFoundException e) {
	    	System.err.println("no se encontro el archivo de configuracion. cerrando programa");
	    	return false;
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

