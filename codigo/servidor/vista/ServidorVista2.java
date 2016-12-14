package servidor.vista;

import javax.swing.JFrame;

import servidor.Primario;

public class ServidorVista2 extends JFrame{
	//variables de la clase
		private static final long serialVersionUID = 1L;
		private Primario servidor;
	//variables de la vista
		
		public ServidorVista2(Primario servidor){
			setTitle("Servidor BitCoin Mining");
			this.servidor = servidor;
			this.setVisible(true);
		}
}
