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
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.awt.event.ActionEvent;
import javax.swing.JTextPane;

import baseDeDatos.BaseDatos;
import baseDeDatos.Usuario;
import bloquesYTareas.Bloque;
import bloquesYTareas.EstadoTarea;
import bloquesYTareas.Tarea;

import java.awt.Color;
import java.awt.Font;
import java.awt.SystemColor;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;



public class ServidorVista extends JFrame implements Observer{
	
	//variables de la clase
	private static final long serialVersionUID = 1L;
	private Primario servidor;
	private HashMap<String, JLabel> tareas = new HashMap<String, JLabel>();
	
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
	private JLabel lbl2;
	private JLabel lbl3;
	private JLabel lbl4;
	private JLabel lbl5;
	private JLabel lbl6;
	private JLabel lbl7;
	private JLabel lbl8;
	private JLabel lbl9;
	private JLabel lbl10;
	private JLabel lbl11;
	private JLabel lbl12;
	private JLabel lbl13;
	private JLabel lbl14;
	private JLabel lbl15;
	private JLabel lbl16;
	private JTextPane textPaneMsj;
	
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
		jpanel_servidor.setBackground(new java.awt.Color(23, 26, 33));
		panel.add(jpanel_servidor, "");
		jpanel_servidor.setLayout(null);
		
		//LABEL PUERTO DE ESCUCHA
		JLabel lblPuertoDeEscucha = new JLabel("Puerto de escucha:");
		lblPuertoDeEscucha.setFont(new Font("Tahoma", Font.PLAIN, 12));
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
		textPaneConsola.setBackground(new java.awt.Color(209, 218, 225));
		textPaneConsola.setBounds(0, 273, 676, 20);
		jpanel_servidor.add(textPaneConsola);
		
		//------------------------------------------------------------VISTA 2-----------------------------------------------------------		
		jpanel_trabajo = new JPanel();
		jpanel_trabajo.setBackground(new java.awt.Color(23, 26, 33));
		panel.add(jpanel_trabajo, "");
		jpanel_trabajo.setLayout(null);
		
		textPaneConsolaTrabajo = new JTextPane();
		textPaneConsolaTrabajo.setBackground(new java.awt.Color(209, 218, 225));
		textPaneConsolaTrabajo.setForeground(Color.GREEN);
		textPaneConsolaTrabajo.setBounds(0, 273, 533, 20);
		jpanel_trabajo.add(textPaneConsolaTrabajo);
		
		lblIPServidor = new JLabel("");
		lblIPServidor.setForeground(Color.white);
		lblIPServidor.setBounds(10, 0, 148, 14);
		jpanel_trabajo.add(lblIPServidor);
		
		lblPuertoServidor = new JLabel("");
		lblPuertoServidor.setForeground(Color.white);
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
		
		JLabel lblBloquesYTareas = new JLabel("BLOQUES Y SUS TAREAS");
		lblBloquesYTareas.setForeground(new java.awt.Color(189, 187, 185));
		lblBloquesYTareas.setBounds(206, 37, 184, 14);
		jpanel_trabajo.add(lblBloquesYTareas); 
		
		
		textPaneMsj = new JTextPane();
		textPaneMsj.setBounds(10, 132, 512, 130);
		
		JScrollPane scrollBarMsj = new JScrollPane(textPaneMsj);
		scrollBarMsj.setBounds(10, 181, 512, 81);
		jpanel_trabajo.add(scrollBarMsj);
		
				
		//------------------------------------------------------LOS HASH MAP---------------------------------------------------
		JLabel lbl1 = new JLabel("");
		lbl1.setBackground(Color.RED);
		lbl1.setBounds(10, 62, 20, 20);
		jpanel_trabajo.add(lbl1);
		lbl1.setOpaque(true);
		tareas.put("11",lbl1); //AÑADE EL LBL AL BLOQUE 1 CON TAREA 1
		
		lbl2 = new JLabel("");
		lbl2.setOpaque(true);
		lbl2.setBackground(Color.RED);
		lbl2.setBounds(40, 62, 20, 20);
		jpanel_trabajo.add(lbl2);
		tareas.put("12",lbl2);
		
		lbl3 = new JLabel("");
		lbl3.setOpaque(true);
		lbl3.setBackground(Color.RED);
		lbl3.setBounds(70, 62, 20, 20);
		jpanel_trabajo.add(lbl3);
		tareas.put("13",lbl3);
		
		lbl4 = new JLabel("");
		lbl4.setOpaque(true);
		lbl4.setBackground(Color.RED);
		lbl4.setBounds(100, 62, 20, 20);
		jpanel_trabajo.add(lbl4);
		tareas.put("14",lbl4);
		
		lbl5 = new JLabel("");
		lbl5.setOpaque(true);
		lbl5.setBackground(Color.RED);
		lbl5.setBounds(130, 62, 20, 20);
		jpanel_trabajo.add(lbl5);
		tareas.put("15",lbl5);
		
		lbl6 = new JLabel("");
		lbl6.setOpaque(true);
		lbl6.setBackground(Color.RED);
		lbl6.setBounds(160, 62, 20, 20);
		jpanel_trabajo.add(lbl6);
		tareas.put("16",lbl6);
		
		lbl7 = new JLabel("");
		lbl7.setOpaque(true);
		lbl7.setBackground(Color.RED);
		lbl7.setBounds(190, 62, 20, 20);
		jpanel_trabajo.add(lbl7);
		tareas.put("17",lbl7);
		
		lbl8 = new JLabel("");
		lbl8.setOpaque(true);
		lbl8.setBackground(Color.RED);
		lbl8.setBounds(220, 62, 20, 20);
		jpanel_trabajo.add(lbl8);
		tareas.put("18",lbl8);
		
		lbl9 = new JLabel("");
		lbl9.setOpaque(true);
		lbl9.setBackground(Color.RED);
		lbl9.setBounds(250, 62, 20, 20);
		jpanel_trabajo.add(lbl9);
		tareas.put("19",lbl9);
		
		lbl10 = new JLabel("");
		lbl10.setOpaque(true);
		lbl10.setBackground(Color.RED);
		lbl10.setBounds(280, 62, 20, 20);
		jpanel_trabajo.add(lbl10);
		tareas.put("110",lbl10);
		
		lbl11 = new JLabel("");
		lbl11.setOpaque(true);
		lbl11.setBackground(Color.RED);
		lbl11.setBounds(310, 62, 20, 20);
		jpanel_trabajo.add(lbl11);
		tareas.put("111",lbl11);
		
		lbl12 = new JLabel("");
		lbl12.setOpaque(true);
		lbl12.setBackground(Color.RED);
		lbl12.setBounds(340, 62, 20, 20);
		jpanel_trabajo.add(lbl12);
		tareas.put("112",lbl12);
		
		lbl13 = new JLabel("");
		lbl13.setOpaque(true);
		lbl13.setBackground(Color.RED);
		lbl13.setBounds(370, 62, 20, 20);
		jpanel_trabajo.add(lbl13);
		tareas.put("113",lbl13);
		
		lbl14 = new JLabel("");
		lbl14.setOpaque(true);
		lbl14.setBackground(Color.RED);
		lbl14.setBounds(400, 62, 20, 20);
		jpanel_trabajo.add(lbl14);
		tareas.put("114",lbl14);
		
		lbl15 = new JLabel("");
		lbl15.setOpaque(true);
		lbl15.setBackground(Color.RED);
		lbl15.setBounds(430, 62, 20, 20);
		jpanel_trabajo.add(lbl15);
		tareas.put("115",lbl15);
		
		lbl16 = new JLabel("");
		lbl16.setOpaque(true);
		lbl16.setBackground(Color.RED);
		lbl16.setBounds(460, 62, 20, 20);
		jpanel_trabajo.add(lbl16);
		tareas.put("116",lbl16);
			
		
		
		
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
	public void update(Observable claseLLamadora, Object objeto) {
		//ya que voy a recibir distintos tipos de objetos, tengo q ver quien la clase llamadora (la que cambio de estado)
		//y que objeto me pasa
		if(claseLLamadora.getClass().equals(BaseDatos.class)){	// si claseLLamador es una clase de BaseDatos
			//si la base de datos cambio de estado
			
			Tarea tarea = (Tarea) objeto;
			//VOY A SACAR EL ESTADO DE LA TAREA PARA VER QUE COLOR PONGO, Y VOY A SACAR EL ID BLOQUE Y EL ID TAREA
			int id_tarea = tarea.getId();
			Bloque bloque = tarea.getBloque();
			int idBloque = bloque.getId();
			EstadoTarea estado = tarea.getEstado();
			//ARMO UN STRING QUE VA A GUARDAR LA CLAVE PARA EL HASHMAP (BLOQUE+TAREA)
			String clave = String.valueOf(idBloque) + String.valueOf(id_tarea);
			//VEO A QUE ESTADO CAMBIO
			switch(estado){
			case enProceso:
				//PASA A COLOR CELESTE
				tareas.get(clave).setBackground(Color.CYAN);
				break;
			case detenida:
				//PASA A COLOR AMARILLO
				tareas.get(clave).setBackground(Color.yellow);
				break;
			case completada:
				//PASA A COLOR VERDE
				tareas.get(clave).setBackground(Color.green);
				break;
			}
			
		}else{
			if(claseLLamadora.getClass().equals(HiloConexionPrimario.class)){
				//si el que me indica que cambio de estado es el hilo primario
				//pregunto si cambio el estado del usuario u otra cosa
				if(objeto.getClass().equals(Usuario.class)){
					//si el q cambio de estado es el usuario
					Usuario usuario= (Usuario) objeto;
					switch(usuario.getEstado()){
					case conectado:
							System.out.println("se ha autenticado el usuario: "+usuario.getNombre());
							//habria que actualizar algun label o en lo que se muestre el usuario
						break;
					case noLogeado:
							System.out.println("se a conectado un usuario no autenticado todavia");
						break;
					default:
						break;
					
					}
				}
			}else{
				//por si despues necesitamos que observe a algo mas
				this.mostrarError("fallo el observador! :(");
			}
		}
		
		
	}
}
