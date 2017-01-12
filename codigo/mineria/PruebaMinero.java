package mineria;
import java.util.Arrays;

import bloquesYTareas.Tarea;

public class PruebaMinero {
	public static void main (String[] args) {
		byte[] tareaResolver = {0x0B, 0x0A, 0x03, 0x0F,0x0B, 0x0A, 0x03, 0x0F,0x0B};
		byte[] parcial = {0x00};
		byte[] limite_superior = new byte[32];
		Arrays.fill (limite_superior, (byte) 0x00);
		limite_superior[3] = (byte) 0x80; // Esto va a buscar algo que empiece con 3 ceros
		
		Tarea tarea = new Tarea(null,tareaResolver,parcial);
		tarea.SetLimite(3,(byte) 0x80);

		boolean multiple=true;	//para el if de mas abajo, true=multi thread, false=mono thread
		HiloMinero hilo=null;
		
		if (multiple){
			hilo=new HiloMineroGestorMultiCore(null,tarea);
			Thread t = new Thread(hilo);
			t.start();
		}else{
			hilo=new HiloMineroCPU(tarea);
			Thread thread = new Thread (hilo);
			thread.start();
		}
	}
}

