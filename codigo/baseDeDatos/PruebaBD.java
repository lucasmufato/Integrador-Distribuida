package baseDeDatos;
import bloquesYTareas.*;

public class PruebaBD{
	public static void main (String[] args) {
		BaseDatos bd = new BaseDatos();
		System.out.println ("Generamos un bloque nuevo");
		bd.generarBloques (1, 32, 40);
		
		System.out.println ("Pedimos una tarea para el usuario con ID 1");
		Tarea tarea = bd.getTarea (1);
	}
}
