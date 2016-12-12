package servidor.vista;

import javax.swing.JFrame;

import servidor.Primario;

public class ServidorVista extends JFrame {
	
	//variables de la clase
	private static final long serialVersionUID = 1L;
	private Primario servidor;
	
	//variables graficas
	
	public ServidorVista(Primario servidor){
		this.servidor=servidor;
		
		//esto va aca hasta que haya una interfaz usable
		this.servidor.setPuerto(5555);
		if( this.servidor.pedirPuerto() ){
			this.IniciarServidor();
		}
		
	}
	
	
	
	public boolean IniciarServidor(){
		//TODO falta obtener el nro de puerto en el cual vamos a escuchar y pasarselo al servidor
		
		//este metodo deberia ser llamado por algun boton, y recien ahi 
		Thread ser =new Thread(this.servidor);
		ser.start();
		return true;
	}
	
	public void mostrarMsjConsola(String msj){
		//TODO
		// la idea es tener un label o algo en lo cual ir imprimiendo las cosas y no usar
		//por ahora queda asi para ir mostrando algo
		System.out.println(msj);
	}

	public void MostrarPopUp(String msj) {
		//TODO
		//este metodo tendria que mostrar un mensaje del tipo PopUp, osea en un cuadro aparte
		System.err.println(msj);
	}

	public void mostrarError(String msj) {
		System.err.println(msj);
		// TODO para mostrar mensajes de error
		
	}
	
}
