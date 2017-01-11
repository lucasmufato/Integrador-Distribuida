package mineria;
import java.util.Arrays;

import bloquesYTareas.Tarea;

public class PruebaMinero {
	public static void main (String[] args) {
		byte[] tareaResolver = {0x0A, 0x03, 0x0F, 0x0B, 0x0A, 0x03, 0x0F, 0x0B, 0x0A, 0x03, 0x0F, 0x0B, 0x0A, 0x03, 0x0F, 0x0B, 0x0A, 0x03, 0x0F, 0x0B ,0x0A, 0x03, 0x0F,0x0B, 0x0A, 0x03, 0x0F,0x0B, 0x0A, 0x03, 0x0F,0x0B, 0x0A, 0x03, 0x0F,0x0B, 0x0A, 0x03, 0x0F,0x0B};
		byte[] parcial = {0x00};
		byte[] limite_superior = new byte[32];
		Arrays.fill (limite_superior, (byte) 0x00);
		limite_superior[3] = (byte) 0x80; // Esto va a buscar algo que empiece con 3 ceros
		
		Tarea tarea = new Tarea(null,tareaResolver,parcial);
		tarea.SetLimite(3,(byte) 0x80);

		//HiloMinero minero = new HiloMineroCPU(null, tareaResolver, parcial, limite_superior);
		//HiloMinero minero = new HiloMineroCPU(tarea);
		//Thread thread = new Thread (minero);
		//thread.start();
		
		HiloMineroGestorMultiCore nombreLargo = new HiloMineroGestorMultiCore(tarea);
		Thread t = new Thread(nombreLargo);
		t.start();
	}
}

