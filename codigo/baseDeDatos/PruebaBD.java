package baseDeDatos;
import bloquesYTareas.*;
import java.util.ArrayList;

public class PruebaBD{
	public static void main (String[] args) {
		BaseDatos bd = new BaseDatos();

		
		System.out.println ("Pedimos una tarea para el usuario con ID 1");
		Tarea tarea = bd.getTarea (1);
		
		if (tarea == null) {
			System.out.println ("Ya no hay tareas por procesar. Generamos un bloque nuevo");
			bd.generarBloques (1, 32, 40);
			tarea = bd.getTarea (1);
			if (tarea == null) {
				System.out.println ("Adios");
				System.exit(1);
			}
		}
		
/*
		System.out.println ("Le cambiamos los bytes de parcial, y llamamos a setParcial en la DB");
		byte[] parcial = new byte[4];
		parcial[0] = (byte) 0x99;
		parcial[1] = (byte) 0x88;
		parcial[2] = (byte) 0x77;
		parcial[3] = (byte) 0x66;
		tarea.setParcial(parcial);
		bd.setParcial(tarea, 1);
*/

		System.out.println ("TAREA:");
		System.out.println ("ID de Tarea: " + tarea.getId());
		System.out.println ("Estado de Tarea: " + tarea.getEstado());
		System.out.println ("Bytes: "+tarea.toString());

		Bloque bloque = tarea.getBloque();
		System.out.println ("\nBLOQUE:");
		System.out.println ("ID de Bloque: " + tarea.getBloque().getId());
		System.out.println ("Estado de Bloque: " + bloque.getEstado());
		
		ArrayList<Tarea> tareas_bloque = bloque.getTareas();
		System.out.println ("\nOTRAS TAREAS EN BLOQUE:");
		for (Tarea t: tareas_bloque) {
			System.out.println ("ID: " + t.getId() + "  Estado: " + t.getEstado() + " <" + t.toString().substring(0, 16)+"..>");
		}

		System.out.println ("\nINFORMACION DE CACHE:");
		System.out.println(bd.cacheInfo());
		
	}
}
