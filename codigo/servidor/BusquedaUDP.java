package servidor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import misc.Loggeador;

public class BusquedaUDP implements Runnable {
	//se usa tanto para buscar como para recibir msj
	public static Integer puertoPrimario=null;
	
	private static Integer puertoServicioUDP = 23456;
	private static String codigoEnvio = "sistemasdistribuidos";
	private static String codigoRespuesta = "programacionparalela";
	
	private static short intentos=3;
	private static int timeout=500;	//para buscar
	private static int timeoutEspera = 5000;	//para esperar conexiones
	
	public static boolean trabajando;
	private Loggeador logger;
	
	public BusquedaUDP(Integer puerto,Loggeador logger){
		BusquedaUDP.puertoPrimario=puerto;
		this.logger=logger;
	}
	
	public static String buscarPrimario(Loggeador logger) throws SocketException, UnknownHostException{
		
		//metodo que envia un broadcast en la red preguntando por un servidor primario.
		//si encuentra al ip le devuelve, sino devuelve null
		
		System.out.println("buscando servidor primario en la red local...");
		
		DatagramSocket socketDatagram= new DatagramSocket();
		socketDatagram.setBroadcast(true);
		socketDatagram.setSoTimeout(timeout);
		String ipRed = InetAddress.getLocalHost().getHostAddress();
		//transformo la ip del servidor a la "IP de la broadcast"(puede no funcionar si la mascara no es de 24)
		String broadcast = ipRed.substring(0,(ipRed.lastIndexOf('.')))+".255";
		byte[] byteEnvio = codigoEnvio.getBytes();
		DatagramPacket p= new DatagramPacket(byteEnvio, byteEnvio.length,InetAddress.getByName(broadcast), puertoServicioUDP);
		byte[] tamanio = new byte[codigoRespuesta.getBytes().length+Integer.BYTES];
		DatagramPacket respuesta = new DatagramPacket(tamanio,tamanio.length);
		boolean respondio=false;
		logger.guardar("BusquedaUDP","voy a mandar "+intentos+" mensajes UDP a la red local");
		short nro=0;
		while(nro<intentos && respondio==false ){
			try {
				//envio el paquete
				socketDatagram.send(p);
				//leo la respuesta
				socketDatagram.receive(respuesta);
			}catch(SocketTimeoutException e){
				//no hago nada
			}catch (IOException e) {
				logger.guardar(e);
				nro=intentos;
			}
			//compruebo que tengo una respuesta
			if(respuesta!=null){
				//transformo los primeros bytes en el codigo de respuesta 
				byte[] parteString=new byte[codigoRespuesta.getBytes().length];
				System.arraycopy(respuesta.getData(), 0, parteString, 0, codigoRespuesta.getBytes().length);
				String msj = new String(parteString);
				if(msj.equals(codigoRespuesta)){
					//si lo que me respondieron es lo que me tienen q responder
					respondio=true;
					String ip=respuesta.getAddress().getHostAddress();
					parteString=new byte[Integer.BYTES];
					System.arraycopy(respuesta.getData(), codigoRespuesta.getBytes().length, parteString, 0, Integer.BYTES);
					BusquedaUDP.puertoPrimario =Integer.parseInt(new String(parteString));
					
					socketDatagram.close();
					System.out.println("encontre un servidor primario en: "+ip);
					logger.guardar("BusquedaUDP","Encontre un servidor primario en: "+ip);
					return ip;
				}
			}else{
				logger.guardar("BusquedaUDP","intento nro: "+intentos+". Sin respuesta");
			}
			nro++;
		}
		socketDatagram.close();
		System.out.println("no encontre un servidor primario en la red local");
		return null;
	}

	@Override
	public void run() {
		// cuando se invoca de esta manera el se queda a la espera de mensajes udp para informar que hay un servidor primario
		BusquedaUDP.trabajando=true;
		this.logger.guardar("BusquedaUDP","Estoy esperando paquetes UDP en el puerto: "+ puertoServicioUDP);
		System.out.println("BusquedaUDP:- estoy esperando paquetes UDP en el puerto: "+ puertoServicioUDP);
		System.out.println("BusquedaUDP:- el servidor primario esta en el puerto: "+puertoPrimario);
		DatagramSocket socket =null;
		try {
			socket = new DatagramSocket(puertoServicioUDP);
			socket.setSoTimeout(timeoutEspera);
		} catch (SocketException e) {
			BusquedaUDP.trabajando=false;
			this.logger.guardar(e);
		}
		while(BusquedaUDP.trabajando){
			DatagramPacket paquete = new DatagramPacket(codigoEnvio.getBytes(),codigoEnvio.getBytes().length);
			try {
				socket.receive(paquete);
				String msj = new String(paquete.getData());
				if(msj.equals(codigoEnvio)){
					//respondo el mensaje
					String ip = paquete.getAddress().getHostAddress();
					int puerto = paquete.getPort();	//puerto al que le voy a responder
					System.out.println("BusquedaUDP:- recibi un mensaje desde la ip: "+ip);
					this.logger.guardar("BusquedaUDP","Recibi un mensaje desde la ip: "+ip);
					
					//paso a rta el codigo de respueta y el puerto del servidor primario
					byte[] rta = new byte[codigoRespuesta.getBytes().length+Integer.BYTES];
					System.arraycopy(codigoRespuesta.getBytes(), 0, rta, 0, codigoRespuesta.getBytes().length);
					System.arraycopy(puertoPrimario.toString().getBytes(), 0, rta, codigoRespuesta.getBytes().length, Integer.BYTES);
					
					DatagramPacket paqueteRespuesta = new DatagramPacket(rta,rta.length,InetAddress.getByName(ip),puerto);
					socket.send(paqueteRespuesta);
				}else{
					System.out.println("BusquedaUDP:-recibi un mensaje no reconocido");
					this.logger.guardar("BusquedaUDP","Recibi un mensaje desconocido desde: "+paquete.getSocketAddress());
				}
			}catch(SocketTimeoutException e){
				//no hago nada
			}catch (IOException e) {
				//e.printStackTrace();
				this.logger.guardar(e);
				BusquedaUDP.trabajando=false;
			}
				
		}
		try {
			socket.close();
			socket=null;
		} catch (Exception e) {	}
	}
	
}