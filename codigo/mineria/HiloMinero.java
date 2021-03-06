package mineria;

import bloquesYTareas.Tarea;
import cliente.*;

public abstract class HiloMinero implements Runnable {
	protected static long LAPSO_NOTIFICACION = 2000; //Enviar una notificacion de progreso parcial cada 2 segundos
	protected Cliente cliente; //Cliente al que se notificaran los resultados parciales y finales
	
	private byte[] tarea; //tarea asignada
	private byte[] limite_superior; //limite contra el cual comparar para saber si el resultado es el que esperamos
	protected byte[] inicio; //Puede ser el resultado parcial de otro proceso que no termino
	
	protected Tarea tareaNueva;	//tiene este nombre para que no se solape con la otra "tarea"
	public static boolean trabajando;	//para que el hilo se pueda detener
	
	public HiloMinero(Tarea tarea){
		this.tareaNueva=tarea;
		this.tarea = this.tareaNueva.getTarea();
		this.limite_superior = this.tareaNueva.getLimiteSuperior();
		this.inicio=this.tareaNueva.getParcial();
	}
	
	public HiloMinero() {
	}

	public Cliente getCliente () {
		return this.cliente;
	}

	public void setCliente (Cliente cliente) {
		this.cliente = cliente;
	}
	
	public byte[] getTarea () {
		return this.tarea;
	}

	public void setTarea (byte[] tarea) {
		this.tarea = tarea;
	}

	public byte[] getLimiteSuperior () {
		return this.limite_superior;
	}

	public void setLimiteSuperior (byte[] limite) {
		this.limite_superior = limite;
	}

	public byte[] getInicio () {
		return this.inicio;
	}

	public void setInicio (byte[] inicio) {
		this.inicio = inicio;
	}
	
	public void notificarParcial (byte[] parcial) {
		if (this.cliente != null && this.tarea != null) {
			this.tareaNueva.setParcial(parcial);
			this.tareaNueva.setResultado(null);
			this.cliente.notificar(this.tareaNueva);
		} else {
			/* Si no hay un cliente, lo mostramos por pantalla */
			System.out.println ("PARCIAL: " + hashToString(parcial));
		}
		
	}

	public void notificarResultado (byte[] resultado) {
		if (this.cliente != null && this.tarea != null) {
			this.tareaNueva.setResultado(resultado);
			this.cliente.notificar (this.tareaNueva);
		} else {
			/* Si no hay un cliente, lo mostramos por pantalla */
			System.out.println ("FINAL: " + hashToString(resultado));
		}
	}

	public boolean esMenor (byte[] hash, byte[] limite) {
	
		if (hash.length != limite_superior.length) {
			System.err.println ("No se puede comparar hash con limite superior, longitudes incompatibles.");
			return false;
		}

		int posicion_comparar = 0;

		while (posicion_comparar < limite_superior.length) {
			if ((hash[posicion_comparar]&0xFF) < (limite_superior[posicion_comparar]&0xFF)) {
				/* Si es menor, devolvemos true */
				return true;
			} else if ((hash[posicion_comparar]&0xFF) > (limite_superior[posicion_comparar]&0xFF)) {
				/* Si es mayor, devolvemos false */
				return false;
			} else {
				/* Si es igual, pasamos al proximo byte */
				posicion_comparar++;
			}
		}

		/* Si llegamos hasta aca es porque son iguales */
		return false;
	}

	public static String hashToString(byte[] hash) {
		if(hash==null){
			return "";
		}
    	StringBuilder builder = new StringBuilder();
    	for(byte b : hash) {
    	    builder.append(String.format("%02x ", b));
    	}
    	return builder.toString();
	}

	public void setTarea(Tarea tarea2) {
		this.tareaNueva=tarea2;
		this.tarea = this.tareaNueva.getTarea();
		this.limite_superior = this.tareaNueva.getLimiteSuperior();
		this.inicio=this.tareaNueva.getParcial();
	}

	public void detener() {
		HiloMinero.trabajando=false;
	}

}
