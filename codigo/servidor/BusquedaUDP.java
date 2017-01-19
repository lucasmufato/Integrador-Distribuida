package servidor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class BusquedaUDP implements Runnable {
	
	private static Integer puertoServicioUDP = 23456;
	private static String codigoEnvio = "sistemasdistribuidos";
	private static String codigoRespuesta = "programacionparalela";
	
	private static short intentos=3;
	private static int timeout=500;
	private static int timeoutEspera = 5000;
	
	private static boolean trabajando;
	
	public BusquedaUDP(){
	}
	
	public static String buscarPrimario() throws SocketException, UnknownHostException{
		//metodo que envia un broadcast en la red preguntando por un servidor primario.
		//si encuentra al ip le devuelve, sino devuelve null
		DatagramSocket socketDatagram= new DatagramSocket();
		socketDatagram.setBroadcast(true);
		socketDatagram.setSoTimeout(timeout);
		String ipRed = InetAddress.getLocalHost().getHostAddress();
		//transformo la ip del servidor a la "IP de la broadcast"(puede no funcionar si la mascara no es de 24)
		String broadcast = ipRed.substring(0,(ipRed.lastIndexOf('.')))+".255";
		byte[] byteEnvio = codigoEnvio.getBytes();
		DatagramPacket p= new DatagramPacket(byteEnvio, byteEnvio.length,InetAddress.getByName(broadcast), puertoServicioUDP);
		DatagramPacket respuesta = new DatagramPacket(codigoRespuesta.getBytes(),codigoRespuesta.getBytes().length);
		boolean respondio=false;
		
		short nro=0;
		while(nro<intentos && respondio==false ){
			try {
				//envio el paquete
				socketDatagram.send(p);
				//leo la respuesta
				socketDatagram.receive(respuesta);
			} catch (IOException e) {
				// TODO borrar el print antes de enviar
				e.printStackTrace();
			}
			String msj = new String(respuesta.getData());
			if(msj.equals(codigoRespuesta)){
				//si lo que me respondieron es lo que me tienen q responder
				respondio=true;
				String ip=respuesta.getAddress().getHostAddress();
				return ip;
			}
			nro++;
		}
		
		return null;
	}

	@Override
	public void run() {
		// cuando se invoca de esta manera el se queda a la espera de mensajes udp para informar que hay un servidor primario
		BusquedaUDP.trabajando=true;
		DatagramSocket socket =null;
		try {
			socket = new DatagramSocket(puertoServicioUDP);
			socket.setSoTimeout(timeoutEspera);
		} catch (SocketException e) {
			BusquedaUDP.trabajando=false;
		}
		while(BusquedaUDP.trabajando){
			DatagramPacket paquete = new DatagramPacket(codigoEnvio.getBytes(),codigoEnvio.getBytes().length);
			try {
				socket.receive(paquete);
				String msj = new String(paquete.getData());
				if(msj.equals(codigoEnvio)){
					//respondo el mensaje
					String ip = paquete.getAddress().getHostAddress();
					int puerto = paquete.getPort();
					DatagramPacket paqueteRespuesta = new DatagramPacket(codigoRespuesta.getBytes(),codigoRespuesta.getBytes().length,InetAddress.getByName(ip),puerto);
					socket.send(paqueteRespuesta);
				}
			}catch(SocketTimeoutException e){
				//no hago nada
			}catch (IOException e) {
				e.printStackTrace();
				BusquedaUDP.trabajando=false;
			}
				
		}
	}
		
	
	
}
