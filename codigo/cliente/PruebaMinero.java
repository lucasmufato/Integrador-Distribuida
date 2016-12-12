package cliente;
import java.util.Arrays;

public class PruebaMinero {
	public static void main (String[] args) {
		byte[] tarea = {0x0A, 0x03, 0x0F};
		byte[] parcial = {0x00};
		byte[] limite_superior = new byte[32];
		Arrays.fill (limite_superior, (byte) 0x00);
		limite_superior[2] = 1; // Esto va a buscar algo que empiece con 3 ceros


		HiloMinero minero = new HiloMineroCPU(null, tarea, parcial, limite_superior);
		Thread thread = new Thread (minero);
		thread.start();
	}
}

