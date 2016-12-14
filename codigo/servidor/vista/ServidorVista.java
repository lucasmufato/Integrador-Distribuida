package servidor.vista;

import javax.swing.JFrame;

import servidor.Primario;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import java.awt.Color;


public class ServidorVista extends JFrame {
	
	//variables de la clase
	private static final long serialVersionUID = 1L;
	private Primario servidor;
	
	//variables de la vista
	private JPanel jpanel_servidor;
	private JTextField textFieldPuerto;
	private JButton btnConectarServidor;
	private JTextPane textPaneConsola;
	
	public ServidorVista(Primario servidor){
		setTitle("Servidor BitCoin Mining");
		this.servidor=servidor;
		this.setVisible(true);
		this.setBounds(250, 250, 500, 250);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		getContentPane().add(panel);
		panel.setLayout(new CardLayout(0, 0));
		
		jpanel_servidor = new JPanel();
		jpanel_servidor.setBackground(Color.BLACK);
		panel.add(jpanel_servidor, "");
		jpanel_servidor.setLayout(null);
		
		//LABEL PUERTO DE ESCUCHA
		JLabel lblPuertoDeEscucha = new JLabel("Puerto de escucha:");
		lblPuertoDeEscucha.setForeground(Color.WHITE);
		lblPuertoDeEscucha.setBounds(89, 27, 144, 14);
		jpanel_servidor.add(lblPuertoDeEscucha);
		
		//TEXFIELD DEL PUERTO
		textFieldPuerto = new JTextField();
		textFieldPuerto.setBounds(228, 23, 172, 23);
		jpanel_servidor.add(textFieldPuerto);
		textFieldPuerto.setColumns(10);
		
		//CUANDO PRESIONES EL BOTON, VOY A CHEQUEAR QUE EL PUERTO QUE INGRESE ESTE LIBRE
		btnConectarServidor = new JButton("Conectar Servidor");
		btnConectarServidor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(servidor.pedirPuerto(Integer.parseInt(textFieldPuerto.getText()))){
					iniciarServidor();
				}
			}
		});
		btnConectarServidor.setBounds(192, 89, 144, 23);
		jpanel_servidor.add(btnConectarServidor);
		
		//TEXT PANE EN DONDE SE VAN A IR MOSTRANDO MSJs
		textPaneConsola = new JTextPane();
		textPaneConsola.setForeground(Color.GREEN);
		textPaneConsola.setBackground(Color.DARK_GRAY);
		textPaneConsola.setBounds(0, 202, 494, 20);
		jpanel_servidor.add(textPaneConsola);
		
		this.revalidate();
		this.repaint();
	}
	
	public boolean iniciarServidor(){
		//ES LLAMADO POR EL BOTON CONECTAR SERVIDOR 
		Thread ser =new Thread(this.servidor);
		ser.start();
		//UNA VEZ QUE DOY EL START, EL METODO RUN VA A LLAMAR AL ESPERARCLIENTES() DEL PRIMARIO
		return true;
	}
	
	public void mostrarMsjConsola(String msj){
		//DONDE VOY A IR MOSTRANDO MSJS PARA NO USAR LA CONSOLA 
		this.textPaneConsola.setText(msj);
	}

	public void MostrarPopUp(String msj) {
		//este metodo tendria que mostrar un mensaje del tipo PopUp, osea en un cuadro aparte
		JOptionPane.showMessageDialog(null,msj);

	}

	public void mostrarError(String msj) {
		//para mostrar mensajes de error
		JOptionPane.showMessageDialog(null,msj);
	}
}
