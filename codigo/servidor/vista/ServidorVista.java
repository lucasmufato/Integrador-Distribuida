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
import java.util.Observable;
import java.util.Observer;
import java.awt.event.ActionEvent;
import javax.swing.JTextPane;

import bloquesYTareas.Bloque;
import bloquesYTareas.EstadoTarea;
import bloquesYTareas.Tarea;

import java.awt.Color;
import java.awt.Rectangle;

import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import java.awt.SystemColor;


public class ServidorVista extends JFrame implements Observer{
	
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
	
	public ServidorVista(Primario servidor){
		setTitle("Servidor BitCoin Mining");
		this.servidor=servidor;
		this.setVisible(true);
		this.setResizable(false);
		this.setBounds(250, 250, 538, 321);
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
		lblPuertoDeEscucha.setBounds(78, 43, 144, 14);
		jpanel_servidor.add(lblPuertoDeEscucha);
		
		//TEXFIELD DEL PUERTO
		textFieldPuerto = new JTextField();
		textFieldPuerto.setBounds(281, 39, 172, 23);
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
		btnConectarServidor.setBounds(217, 168, 144, 23);
		jpanel_servidor.add(btnConectarServidor);
		
		//TEXT PANE EN DONDE SE VAN A IR MOSTRANDO MSJs
		textPaneConsola = new JTextPane();
		textPaneConsola.setForeground(Color.GREEN);
		textPaneConsola.setBackground(Color.DARK_GRAY);
		textPaneConsola.setBounds(0, 273, 676, 20);
		jpanel_servidor.add(textPaneConsola);
		
		//------------------------------------------------------------VISTA 2-----------------------------------------------------------		
		jpanel_trabajo = new JPanel();
		jpanel_trabajo.setForeground(Color.BLACK);
		jpanel_trabajo.setBackground(Color.BLACK);
		panel.add(jpanel_trabajo, "");
		jpanel_trabajo.setLayout(null);
		
		textPaneConsolaTrabajo = new JTextPane();
		textPaneConsolaTrabajo.setBackground(Color.DARK_GRAY);
		textPaneConsolaTrabajo.setForeground(Color.GREEN);
		textPaneConsolaTrabajo.setBounds(0, 273, 533, 20);
		jpanel_trabajo.add(textPaneConsolaTrabajo);
		
		lblIPServidor = new JLabel("");
		lblIPServidor.setForeground(Color.RED);
		lblIPServidor.setBounds(10, 0, 148, 14);
		jpanel_trabajo.add(lblIPServidor);
		
		lblPuertoServidor = new JLabel("");
		lblPuertoServidor.setForeground(Color.RED);
		lblPuertoServidor.setBounds(199, 0, 139, 14);
		jpanel_trabajo.add(lblPuertoServidor);
		
		JButton btnDesconectar = new JButton("X");
		btnDesconectar.setBackground(new Color(211, 211, 211));
		btnDesconectar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//LAMARA A UN METODO DESCONECTAR EN LA CLASE PRIMARIO
			}
		});
		btnDesconectar.setBounds(474, 0, 59, 20);
		jpanel_trabajo.add(btnDesconectar);
		
		JLabel lblBloquesYTareas = new JLabel("Bloques y sus tareas :");
		lblBloquesYTareas.setForeground(Color.WHITE);
		lblBloquesYTareas.setBounds(10, 37, 132, 14);
		jpanel_trabajo.add(lblBloquesYTareas); 
		
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

	@Override
	public void update(Observable o, Object arg) {
		Tarea tarea = (Tarea) arg;
		//VOY A SACAR EL ESTADO DE LA TAREA PARA VER QUE COLOR PONGO, Y VOY A SACAR EL ID BLOQUE Y EL ID TAREA
		int id_tarea = tarea.getId();
		Bloque bloque = tarea.getBloque();
		int idBloque = bloque.getId();
		EstadoTarea estado = tarea.getEstado();
	}

}
