package baseDeDatos;
import bloquesYTareas.*;

public class PruebaBD{
	public static void main (String[] args) {
		BaseDatos bd = new BaseDatos();
		System.out.println ("Generamos un bloque nuevo");
		bd.generarBloques (1, 32, 40);
		
		System.out.println ("Pedimos una tarea para el usuario con ID 1");
		Tarea tarea = bd.getTarea (1);
		
		System.out.println ("Le cambiamos los bytes de parcial, y llamamos a setParcial en la DB");
		byte[] parcial = new byte[4];
		parcial[0] = (byte) 0x99;
		parcial[1] = (byte) 0x88;
		parcial[2] = (byte) 0x77;
		parcial[3] = (byte) 0x66;
		tarea.setParcial(parcial);
		bd.setParcial(tarea, 1);
	}
}
