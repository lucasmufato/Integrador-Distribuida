package mineria;
import java.util.Arrays;

import cliente.*;
import java.util.Date;

import bloquesYTareas.Tarea;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HiloMineroCPU extends HiloMinero {
	private MessageDigest sha256;

	public HiloMineroCPU(Tarea tarea) {
		super(tarea);
		try {
			this.sha256 = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			System.err.println ("ERROR FATAL: No se puede inicializar el minero");
		}
	}

	public HiloMineroCPU(Cliente cliente, Tarea tarea) {
		super(tarea);
		this.setCliente(cliente);
		try {
			this.sha256 = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			System.err.println ("ERROR FATAL: No se puede inicializar el minero");
		}
	}

	@Override
	public void run() {
		boolean encontrado = false;
		byte[] limite = this.getLimiteSuperior();
		byte[] tarea = this.getTarea();
		byte[] inicio = this.getInicio();

		long tiempo_ultima_notificacion, ahora;

		byte[] n_seq = new byte[inicio.length]; // Numero de secuencia actual
		System.arraycopy (inicio, 0, n_seq, 0, n_seq.length);

		byte[] concatenacion = new byte[tarea.length + n_seq.length];	
		System.arraycopy(tarea, 0, concatenacion, 0, tarea.length);
		System.arraycopy(n_seq, 0, concatenacion, tarea.length, n_seq.length);

		/* Antes de arrancar enviamos una notificacion */
		ahora = new Date().getTime();
		tiempo_ultima_notificacion = ahora;
		this.notificarParcial(n_seq);

		byte[] hash;

		HiloMinero.trabajando = true;
		while ((!encontrado) && HiloMinero.trabajando) {
			hash = this.sha256.digest(concatenacion);

			if (esMenor (hash, limite)) {
				System.arraycopy(concatenacion, tarea.length, n_seq, 0, n_seq.length);
				encontrado = true;
				this.notificarResultado (n_seq);


			} else {
				/* sumamos uno a la parte correspondiente al numero de secuencia en la concatencacion */
				int pos = concatenacion.length -1; //Ponemos el puntero en el bit menos significativo
				boolean seguir = true;

				/* Este bucle aumenta el numero de secuencia en la concatenacion */
				/* Nota: para mejorar la performance, solo se vuelca en la variable n_seq cuando se envia notificacion, o cuando se aumenta el tamanio del array. */
				while (seguir) {
					if (concatenacion[pos] != (byte)0xFF) {
						concatenacion[pos]++;
						seguir = false;
					} else {
						concatenacion[pos] = 0x00;
						pos--; //corremos el puntero a un bit mas significativo (hacia la izquierda)
					}

					/* Si llegamos a la parte de la tarea, significa que ya probamos todas los numeros de secuencia con esa cantidad de bits */
					if ((pos+1) <= tarea.length) {
						/* Aumentamos el tamanio del numero de secuencia */	
						n_seq = new byte [n_seq.length+1];
						Arrays.fill (n_seq, (byte) 0x00);

						/* Actualizamos la concatenacion */
						concatenacion = new byte[tarea.length + n_seq.length];	
						System.arraycopy(tarea, 0, concatenacion, 0, tarea.length);
						System.arraycopy(n_seq, 0, concatenacion, tarea.length, n_seq.length);

						/* Ponemos el puntero nuevamente al final */
						pos = concatenacion.length -1;
					}
				}

				/* Si paso mucho tiempo desde la ultima notificacion enviada */
				ahora = new Date().getTime();
				if ((ahora - tiempo_ultima_notificacion) > HiloMinero.LAPSO_NOTIFICACION) {
					/* Guardamos el resultado parcial en n_seq */
					System.arraycopy(concatenacion, tarea.length, n_seq, 0, n_seq.length);
					/* Mandamos la notificacion */
					this.notificarParcial (n_seq);
					tiempo_ultima_notificacion = ahora;
				}
			}
		}
		
	}
}
