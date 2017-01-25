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

import servidor.vista.VentanaInicioServidor;

public class Servidor {
	
	private VentanaInicioServidor ventana;
	private static int timeout=2000;
	
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
		
		String ipPrimario=null;
		try {
			this.ventana.agregarLine("buscando un servidor primario en la red local(2 segundos)...");
			ipPrimario=BusquedaUDP.buscarPrimario();
		} catch (SocketException | UnknownHostException e1) {
			this.ventana.agregarLine("no pude buscar el servidor primario en la red local");
		}
		
		if(ipPrimario!=null){
			Integer puerto= BusquedaUDP.puertoPrimario;
			this.ventana.agregarLine("conectadose al servidor primario encontrado en la red local: ip-"+ipPrimario+" puerto-"+puerto);
			this.crearServidorBackup(ipPrimario, puerto.toString());
			return true;
		}
		
		Properties propiedades = new Properties();
	    InputStream entrada = null;
	   
	    try {
	    	
	    	this.ventana.agregarLine("buscando servidores en base al archivo de configuracion");
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
						this.ventana.agregarLine("--- Probando con: "+ip + ":"+puerto);
				    	Socket prueba = new Socket();
				    	prueba.connect(new InetSocketAddress(ip,Integer.parseInt(puerto)), timeout);
				    	this.ventana.agregarLine("El servidor primario ya esta levantado, se creara el backup");
				    	conectado=true;
				       	this.crearServidorBackup(ip, puerto);
				       	this.ventana.cerrarVentana();
						this.ventana=null;
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
				this.ventana.agregarLine("Creando servidor primario.");
				try {
					String ip = (InetAddress.getLocalHost().getHostAddress());
					this.crearServidorPrimario(ip, "5555");
					this.ventana.cerrarVentana();
					this.ventana=null;
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
	    } catch (FileNotFoundException e) {
	    	this.ventana.agregarLine("no se encontro el archivo de configuracion. cerrando programa");
	    	System.err.println("no se encontro el archivo de configuracion. cerrando programa");
	    	return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.ventana.cerrarVentana();
			this.ventana=null;
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

