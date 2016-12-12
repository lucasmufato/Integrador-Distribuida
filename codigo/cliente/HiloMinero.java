package cliente;

abstract class HiloMinero implements Runnable {
	protected static long LAPSO_NOTIFICACION = 2000; //Enviar una notificacion de progreso parcial cada 2 segundos
	private Cliente cliente; //Cliente al que se notificaran los resultados parciales y finales
	private byte[] tarea; //tarea asignada
	private byte[] limite_superior; //limite contra el cual comparar para saber si el resultado es el que esperamos
	private byte[] inicio; //Puede ser el resultado parcial de otro proceso que no termino

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
			this.cliente.notificarParcial (this.tarea, parcial);
		}
		
	}

	public void notificarResultado (byte[] resultado) {
		if (this.cliente != null && this.tarea != null) {
			this.cliente.notificarResultado (this.tarea, resultado);
		}
	}

	public boolean esMenor (byte[] hash, byte[] limite) {
	
		if (hash.length != limite_superior.length) {
			System.err.println ("No se puede comparar hash con limite superior, longitudes incompatibles.");
			return false;
		}

		boolean seguir = true;
		int posicion_comparar = 0;

		while (seguir) {
			if (hash[posicion_comparar] < limite_superior[posicion_comparar]) {
				/* Si es menor, devolvemos true */
				return true;
			} else if (hash[posicion_comparar] < limite_superior[posicion_comparar]) {
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

}
