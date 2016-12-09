package servidor;

import baseDeDatos.BaseDatos;


public class HiloConexionPrimario implements Runnable {
	private BaseDatos bd;
		
	@Override
	public void run() {
		//UNA VEZ QUE EL SE CONECTO UN CLIENTE (Y SE CREO SU HILO) SE VA A CONECTAR CON LA BD, PARA AUTENTICAR
		this.bd = new BaseDatos();
		bd.conectarse();
		String nombre = "usuario";
		String password = "usuario123";
		Integer resultado = bd.autenticar(nombre, password); //SI DEVUELVE UN NUMERO, ES DECIR DISTINTO DE NULL, SE LOGRO AUTENTICAR BIEN
		if(resultado != null){
			System.out.println("Autenticaci�n de usuario correcta, el n�mero de la sesi�n es: " + resultado);
		}else{
			System.out.println("Error en autenticaci�n de usuario.");
		}
	}

}
