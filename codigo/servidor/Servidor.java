package servidor;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Properties;

import misc.Loggeador;
import servidor.vista.VentanaInicioServidor;

public class Servidor {
	
	private VentanaInicioServidor ventana;
	private static int timeout=2000;
	public Loggeador logger;
	
	
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Servidor servidor = new Servidor();
	}
	
	public Servidor(){
		this.ventana= new VentanaInicioServidor();
		this.buscarServidorPrimario();
	}
	
	public boolean buscarServidorPrimario(){
		//busca por broadcast en la red, si no tiene respuesta
		//lee el archivo con las ips,intenta de contactarse con el servidor primario
		//si no lo logra se crea un servidor primario,si se conecta con un servidor primario, crea un servidor backup
		
		this.logger= Loggeador.getLoggeador();
		if(this.logger==null){
			this.ventana.agregarLine("No pude iniciar los archivos de Log, cerrando el programa");
			System.err.println("No pude iniciar los archivos de Log, cerrando el programa");
	    	return false;
		}
		
		String ipPrimario=null;
		try {
			
			this.ventana.agregarLine("Buscando un servidor primario en la red local(2 segundos)...");
			this.logger.guardar("inicio","Buscando un servidor primario en la red local");
			ipPrimario=BusquedaUDP.buscarPrimario(logger);
		} catch (SocketException | UnknownHostException e1) {
			this.ventana.agregarLine("No pude buscar el servidor primario en la red local");
			this.logger.guardar(e1);
		}
		
		if(ipPrimario!=null){
			Integer puerto= BusquedaUDP.puertoPrimario;
			this.ventana.agregarLine("Conectadose al servidor primario encontrado en la red local: ip-"+ipPrimario+" puerto-"+puerto);
			this.logger.guardar("inicio","Conectadose al servidor primario encontrado en la red local: ip-"+ipPrimario+" puerto-"+puerto);
			this.crearServidorBackup(ipPrimario, puerto.toString());
			try {
				Thread.sleep(2000);
				this.ventana.cerrarVentana();
				this.ventana=null;
			} catch (InterruptedException e) {
				this.logger.guardar(e);
			}
			return true;
		}
		
		Properties propiedades = new Properties();
	    InputStream entrada = null;
	   
	    try {
	    	this.logger.guardar("inicio","Buscando servidores en base al archivo de configuracion");
	    	this.ventana.agregarLine("Buscando servidores en base al archivo de configuracion");
			entrada = new FileInputStream("configuracion.properties");
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
						this.logger.guardar("inicio","Probando con: "+ip + ":"+puerto);
						this.ventana.agregarLine("--- Probando con: "+ip + ":"+puerto);
				    	Socket prueba = new Socket();
				    	prueba.connect(new InetSocketAddress(ip,Integer.parseInt(puerto)), timeout);
				    	this.ventana.agregarLine("El servidor primario ya esta levantado, se creara el backup");
				    	this.logger.guardar("inicio","Encontre un servidor primario en ->"+ip + ":"+puerto+ "creando servidor backup");
				    	conectado=true;
				       	this.crearServidorBackup(ip, puerto);
				       	this.ventana.cerrarVentana();
				       	this.ventana=null;
				    }catch (Exception e){
				    	this.logger.guardar("inicio","No tuve respuesta desde: "+ip + ":"+puerto);
				    }
				}
				nro++;
			}
			if(conectado==false && hayIP==false){
				this.logger.guardar("inicio","No encontre Servidores primarios. Creando uno.");
				this.ventana.agregarLine("Creando servidor primario.");
				try {
					String ip = (InetAddress.getLocalHost().getHostAddress());
					this.crearServidorPrimario(ip, "5555");
					this.ventana.cerrarVentana();
					this.ventana=null;
				} catch (UnknownHostException e1) {
					this.logger.guardar(e1);
				}
			}
	    } catch (FileNotFoundException e) {
	    	this.ventana.agregarLine("No se encontro el archivo de configuracion. Cerrando programa");
	    	//System.err.println("No se encontro el archivo de configuracion. Cerrando programa");
	    	this.logger.guardar(e);
	    	return false;
		} catch (IOException e) {
			this.logger.guardar(e);
			e.printStackTrace();
			this.ventana.cerrarVentana();
			this.ventana=null;
		}
		return false;
	}
	
	public boolean crearServidorPrimario(String ip, String puerto){
		//crea una instancia de primario		
		new Primario(ip,puerto,this.logger);
		return true;
	}
	
	public boolean crearServidorBackup(String ip, String puerto){
		//recibe ip y puerto del backup
		new Backup(ip,puerto,this.logger);
		return false;
	}
}
