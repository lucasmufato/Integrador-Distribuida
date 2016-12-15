package servidor.vista;

import javax.swing.JFrame;

import servidor.HiloConexionPrimario;
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

import bloquesYTareas.Tarea;

import java.awt.Color;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;


public class ServidorVista extends JFrame {
	
	//variables de la clase
	private static final long serialVersionUID = 1L;
	private Primario servidor;
	
	//variables de la vista
	private JPanel jpanel_servidor;
	private JTextField textFieldPuerto;
	private JButton btnConectarServidor;
	private JTextPane textPaneConsola;
	private JPanel panel;
	private JPanel jpanel_trabajo;
	private JTextPane textPaneConsolaTrabajo;
	private JLabel lblIPServidor;
	private JLabel lblPuertoServidor;
	private JTextPane textPaneClientes;
	
	public ServidorVista(Primario servidor){
		setTitle("Servidor BitCoin Mining");
		this.servidor=servidor;
		this.setVisible(true);
		this.setResizable(false);
		this.setBounds(250, 250, 500, 250);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		panel = new JPanel();
		getContentPane().add(panel);
		panel.setLayout(new CardLayout(0, 0));
		
		//---------------------------------------------------VISTA 1------------------------------------------------------------
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
		
		//------------------------------------------------------------VISTA 2-----------------------------------------------------------		
		jpanel_trabajo = new JPanel();
		jpanel_trabajo.setBackground(Color.BLACK);
		panel.add(jpanel_trabajo, "");
		jpanel_trabajo.setLayout(null);
		
		textPaneConsolaTrabajo = new JTextPane();
		textPaneConsolaTrabajo.setBackground(Color.DARK_GRAY);
		textPaneConsolaTrabajo.setForeground(Color.GREEN);
		textPaneConsolaTrabajo.setBounds(0, 202, 494, 20);
		jpanel_trabajo.add(textPaneConsolaTrabajo);
		
		lblIPServidor = new JLabel("");
		lblIPServidor.setForeground(Color.RED);
		lblIPServidor.setBounds(92, 0, 95, 14);
		jpanel_trabajo.add(lblIPServidor);
		
		lblPuertoServidor = new JLabel("");
		lblPuertoServidor.setForeground(Color.RED);
		lblPuertoServidor.setBounds(290, 0, 76, 14);
		jpanel_trabajo.add(lblPuertoServidor);
		
		JButton btnDesconectar = new JButton("X");
		btnDesconectar.setBackground(new Color(211, 211, 211));
		btnDesconectar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//LAMARA A UN METODO DESCONECTAR EN LA CLASE PRIMARIO
			}
		});
		btnDesconectar.setBounds(435, 0, 59, 20);
		jpanel_trabajo.add(btnDesconectar);
		
		textPaneClientes = new JTextPane();
		textPaneClientes.setForeground(Color.WHITE);
		textPaneClientes.setBackground(Color.BLACK);
		textPaneClientes.setBounds(0, 38, 494, 153);
		jpanel_trabajo.add(textPaneClientes);
		
		
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
	
	public void mostrarMsjConsolaTrabajo(String msj){
		//DONDE VOY A IR MOSTRANDO MSJS PARA NO USAR LA CONSOLA 
		this.textPaneConsolaTrabajo.setText(msj);
	}

	public void MostrarPopUp(String msj) {
		//este metodo tendria que mostrar un mensaje del tipo PopUp, osea en un cuadro aparte
		JOptionPane.showMessageDialog(null,msj);

	}

	public void mostrarError(String msj) {
		//para mostrar mensajes de error
		JOptionPane.showMessageDialog(null,msj);
	}

	public void crearPanelTrabajo() {
		//CUANDO SE CONCECTA UN CLIENTE, HAGO VISIBLE LA VISTA DE TRABAJO
		jpanel_servidor.setVisible(false);
		jpanel_trabajo.setVisible(true);
		
		//MUESTRO LA IP Y EL PUERTO
		lblIPServidor.setText("Mi IP: " + this.servidor.getIP());
		lblPuertoServidor.setText("Puerto: " + String.valueOf(this.servidor.getPuerto()));
		
		
	}

	//ESTE METODO LO LLAMARIA DESDE PRIMARIO, CADA VEZ QUE CREE UN HILO O DESDE EL HILO MISMO Y LE PASO DIRECTAMENTE ID Y TAREA
	//NO LO LLAMO DE NINGUN LADO PORQUE EXPLOTABA
	public void setNuevaConexion(HiloConexionPrimario nuevaConexion) {
		//MUESTRO EN EL TEXTPANE EL CLIENTE Y LA TAREA DE LA NUEVA CONEXION
		textPaneClientes.setText("ID Cliente :" + String.valueOf(nuevaConexion.getIdUsuario()) + " ---> " + nuevaConexion.getTarea());
	}

	
}
