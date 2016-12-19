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
import javax.swing.ImageIcon;
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

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

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
	private JLabel lbl2, lbl3, lbl4, lbl5, lbl6, lbl7, lbl8, lbl9, lbl10, lbl11, lbl12, lbl13, lbl14, lbl15, lbl16, lbl17, lbl18, lbl19, lbl20, lbl21, lbl22, lbl23, lbl24, lbl25, lbl26, lbl27, lbl28, lbl29, lbl30, lbl31, lbl32, lbl33, lbl34, lbl35, lbl36, lbl37, lbl38, lbl39, lbl40, lbl41, lbl42, lbl43, lbl44, lbl45, lbl46, lbl47, lbl48, lbl49, lbl50, lbl51, lbl52, lbl53, lbl54, lbl55, lbl56, lbl57, lbl58, lbl59, lbl60, lbl61, lbl62, lbl63, lbl64, lbl65, lbl66, lbl67, lbl68, lbl69, lbl70, lbl71, lbl72, lbl73, lbl74, lbl75, lbl76, lbl77, lbl78, lbl79, lbl80;

	private JTextPane textPaneMsj;
	private JTextPane panel_bloques;
	private JLabel label_logo;

	
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
		lblPuertoDeEscucha.setForeground(new java.awt.Color(189, 187, 185));
		lblPuertoDeEscucha.setBounds(78, 51, 144, 14);
		jpanel_servidor.add(lblPuertoDeEscucha);
		
		//TEXFIELD DEL PUERTO
		textFieldPuerto = new JTextField();
		textFieldPuerto.setBounds(233, 48, 134, 23);
		jpanel_servidor.add(textFieldPuerto);
		textFieldPuerto.setColumns(10);
		
		//CUANDO PRESIONES EL BOTON, VOY A CHEQUEAR QUE EL PUERTO QUE INGRESE ESTE LIBRE
		btnConectarServidor = new JButton("Conectar Servidor");
		btnConectarServidor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					Integer puerto =Integer.parseInt(textFieldPuerto.getText());
					if(puerto<1024){
						throw new NumberFormatException();
					}
					if(servidor.pedirPuerto(puerto)){
						iniciarServidor();
					}
				}catch(NumberFormatException e1){
					//si el numero es muy grande tirar error al parsearlo a Integer
					MostrarPopUp("el numero de puerto no es correcto");
				}
				
			}
		});
		btnConectarServidor.setBounds(78, 161, 144, 23);
		jpanel_servidor.add(btnConectarServidor);
		
		//TEXT PANE EN DONDE SE VAN A IR MOSTRANDO MSJs
		textPaneConsola = new JTextPane();
		textPaneConsola.setForeground(Color.DARK_GRAY);
		textPaneConsola.setBackground(new java.awt.Color(209, 218, 225));
		textPaneConsola.setBounds(0, 273, 676, 20);
		jpanel_servidor.add(textPaneConsola);
		
		label_logo = new JLabel("");
		try {
			label_logo.setIcon(new ImageIcon(ServidorVista.class.getResource("/servidor/vista/icono.jpg")));
		} catch (Exception e) {
			label_logo.setBackground(Color.WHITE);
			System.out.println("No se encontro la imagen ");
		}
		label_logo.setHorizontalAlignment(SwingConstants.CENTER);
		label_logo.setBounds(243, 82, 279, 180);
		jpanel_servidor.add(label_logo);
		
		//------------------------------------------------------------VISTA 2-----------------------------------------------------------		
		jpanel_trabajo = new JPanel();
		jpanel_trabajo.setBackground(new java.awt.Color(23, 26, 33));
		panel.add(jpanel_trabajo, "");
		jpanel_trabajo.setLayout(null);
		
		textPaneConsolaTrabajo = new JTextPane();
		textPaneConsolaTrabajo.setBackground(new java.awt.Color(209, 218, 225));
		textPaneConsolaTrabajo.setForeground(Color.DARK_GRAY);
		textPaneConsolaTrabajo.setBounds(0, 273, 533, 20);
		jpanel_trabajo.add(textPaneConsolaTrabajo);
		
		lblIPServidor = new JLabel("");
		lblIPServidor.setForeground(new java.awt.Color(189, 187, 185));
		lblIPServidor.setBounds(54, 11, 148, 14);
		jpanel_trabajo.add(lblIPServidor);
		
		lblPuertoServidor = new JLabel("");
		lblPuertoServidor.setForeground(new java.awt.Color(189, 187, 185));
		lblPuertoServidor.setBounds(343, 11, 139, 14);
		jpanel_trabajo.add(lblPuertoServidor);
		
		JButton btnDesconectar = new JButton("");
		try{
			btnDesconectar.setIcon(new ImageIcon(ServidorVista.class.getResource("/cliente/vista/logout.jpg")));
		}catch(Exception e){
			btnDesconectar.setBackground(Color.WHITE);
			System.out.println("No se encontro la imagen ");
		}
		btnDesconectar.setBackground(new Color(211, 211, 211));
		btnDesconectar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//LAMARA A UN METODO DESCONECTAR EN LA CLASE PRIMARIO
				System.exit(0);
			}
		});
		btnDesconectar.setBackground(new java.awt.Color(23, 26, 33));
		btnDesconectar.setBounds(492, 11, 30, 30);
		jpanel_trabajo.add(btnDesconectar);
		
		JLabel lblBloquesYTareas = new JLabel("BLOQUES Y SUS TAREAS");
		lblBloquesYTareas.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblBloquesYTareas.setForeground(new java.awt.Color(189, 187, 185));
		lblBloquesYTareas.setBounds(206, 37, 184, 14);
		jpanel_trabajo.add(lblBloquesYTareas); 
		
		
		textPaneMsj = new JTextPane();
		textPaneMsj.setBounds(10, 132, 512, 130);
		
		JScrollPane scrollBarMsj = new JScrollPane(textPaneMsj);
		scrollBarMsj.setBounds(10, 181, 512, 81);
		jpanel_trabajo.add(scrollBarMsj);
		
		panel_bloques = new JTextPane();
		panel_bloques.setForeground(Color.LIGHT_GRAY);
		panel_bloques.setBackground(new java.awt.Color(27, 40, 56));
		panel_bloques.setEditable(false);
		panel_bloques.setLayout(null);
		//panel_bloques.setBounds(10, 62, 512, 112);
		//jpanel_trabajo.add(panel_bloques);
		
		JScrollPane scrollBarBloques = new JScrollPane(panel_bloques);
		scrollBarBloques.setBounds(10, 62, 512, 112);
		jpanel_trabajo.add(scrollBarBloques);
		
		//------------------------------------------------------LOS HASH MAP---------------------------------------------------
		JLabel lbl1 = new JLabel("1");
		lbl1.setBounds(10, 11, 20, 20);
		panel_bloques.add(lbl1);
		lbl1.setBackground(Color.RED);
		lbl1.setForeground(Color.WHITE);
		lbl1.setOpaque(true);
		tareas.put("11",lbl1); //A�ADE EL LBL AL BLOQUE 1 CON TAREA 1
		
		lbl2 = new JLabel("2");
		lbl2.setBounds(40, 11, 20, 20);
		panel_bloques.add(lbl2);
		lbl2.setOpaque(true);
		lbl2.setBackground(Color.RED);
		lbl2.setForeground(Color.WHITE);
		tareas.put("12",lbl2);
		
		lbl3 = new JLabel("3");
		lbl3.setBounds(70, 11, 20, 20);
		panel_bloques.add(lbl3);
		lbl3.setOpaque(true);
		lbl3.setBackground(Color.RED);
		lbl3.setForeground(Color.WHITE);
		tareas.put("13",lbl3);
		
		lbl4 = new JLabel("4");
		lbl4.setBounds(100, 11, 20, 20);
		panel_bloques.add(lbl4);
		lbl4.setOpaque(true);
		lbl4.setBackground(Color.RED);
		lbl4.setForeground(Color.WHITE);
		tareas.put("14",lbl4);
		
		lbl5 = new JLabel("5");
		lbl5.setBounds(130, 11, 20, 20);
		panel_bloques.add(lbl5);
		lbl5.setOpaque(true);
		lbl5.setBackground(Color.RED);
		lbl5.setForeground(Color.WHITE);
		tareas.put("15",lbl5);
		
		lbl6 = new JLabel("6");
		lbl6.setBounds(160, 11, 20, 20);
		panel_bloques.add(lbl6);
		lbl6.setOpaque(true);
		lbl6.setBackground(Color.RED);
		lbl6.setForeground(Color.WHITE);
		tareas.put("16",lbl6);
		
		lbl7 = new JLabel("7");
		lbl7.setBounds(190, 11, 20, 20);
		panel_bloques.add(lbl7);
		lbl7.setOpaque(true);
		lbl7.setBackground(Color.RED);
		lbl7.setForeground(Color.WHITE);
		tareas.put("17",lbl7);
		
		lbl8 = new JLabel("8");
		lbl8.setBounds(220, 11, 20, 20);
		panel_bloques.add(lbl8);
		lbl8.setOpaque(true);
		lbl8.setBackground(Color.RED);
		lbl8.setForeground(Color.WHITE);
		tareas.put("18",lbl8);
		
		lbl9 = new JLabel("9");
		lbl9.setBounds(250, 11, 20, 20);
		panel_bloques.add(lbl9);
		lbl9.setOpaque(true);
		lbl9.setBackground(Color.RED);
		lbl9.setForeground(Color.WHITE);
		tareas.put("19",lbl9);
		
		lbl10 = new JLabel("10");
		lbl10.setBounds(280, 11, 20, 20);
		panel_bloques.add(lbl10);
		lbl10.setOpaque(true);
		lbl10.setBackground(Color.RED);
		lbl10.setForeground(Color.WHITE);
		tareas.put("110",lbl10);
		
		lbl11 = new JLabel("11");
		lbl11.setBounds(310, 11, 20, 20);
		panel_bloques.add(lbl11);
		lbl11.setOpaque(true);
		lbl11.setBackground(Color.RED);
		lbl11.setForeground(Color.WHITE);
		tareas.put("111",lbl11);
		
		lbl12 = new JLabel("12");
		lbl12.setBounds(340, 11, 20, 20);
		panel_bloques.add(lbl12);
		lbl12.setOpaque(true);
		lbl12.setBackground(Color.RED);
		lbl12.setForeground(Color.WHITE);
		tareas.put("112",lbl12);
		
		lbl13 = new JLabel("13");
		lbl13.setBounds(370, 11, 20, 20);
		panel_bloques.add(lbl13);
		lbl13.setOpaque(true);
		lbl13.setBackground(Color.RED);
		lbl13.setForeground(Color.WHITE);
		tareas.put("113",lbl13);
		
		lbl14 = new JLabel("14");
		lbl14.setBounds(400, 11, 20, 20);
		panel_bloques.add(lbl14);
		lbl14.setOpaque(true);
		lbl14.setBackground(Color.RED);
		lbl14.setForeground(Color.WHITE);
		tareas.put("114",lbl14);
		
		lbl15 = new JLabel("15");
		lbl15.setBounds(430, 11, 20, 20);
		panel_bloques.add(lbl15);
		lbl15.setOpaque(true);
		lbl15.setBackground(Color.RED);
		lbl15.setForeground(Color.WHITE);
		tareas.put("115",lbl15);
		
		lbl16 = new JLabel("16");
		lbl16.setBounds(460, 11, 20, 20);
		panel_bloques.add(lbl16);
		lbl16.setOpaque(true);
		lbl16.setBackground(Color.RED);
		lbl16.setForeground(Color.WHITE);
		tareas.put("116",lbl16);
		
		lbl17 = new JLabel("1");
		lbl17.setOpaque(true);
		lbl17.setBackground(Color.RED);
		lbl17.setForeground(Color.WHITE);
		lbl17.setBounds(10, 42, 20, 20);
		panel_bloques.add(lbl17);
		tareas.put("21",lbl17);
		
		lbl18 = new JLabel("2");
		lbl18.setOpaque(true);
		lbl18.setBackground(Color.RED);
		lbl18.setForeground(Color.WHITE);
		lbl18.setBounds(40, 42, 20, 20);
		panel_bloques.add(lbl18);
		tareas.put("22",lbl18);
		
		lbl19 = new JLabel("3");
		lbl19.setOpaque(true);
		lbl19.setBackground(Color.RED);
		lbl19.setForeground(Color.WHITE);
		lbl19.setBounds(70, 42, 20, 20);
		panel_bloques.add(lbl19);
		tareas.put("23",lbl19);
		
		lbl20 = new JLabel("4");
		lbl20.setOpaque(true);
		lbl20.setBackground(Color.RED);
		lbl20.setForeground(Color.WHITE);
		lbl20.setBounds(100, 42, 20, 20);
		panel_bloques.add(lbl20);
		tareas.put("24",lbl20);
		
		lbl21 = new JLabel("5");
		lbl21.setOpaque(true);
		lbl21.setBackground(Color.RED);
		lbl21.setForeground(Color.WHITE);
		lbl21.setBounds(130, 42, 20, 20);
		panel_bloques.add(lbl21);
		tareas.put("25",lbl21);
		
		lbl22 = new JLabel("6");
		lbl22.setOpaque(true);
		lbl22.setBackground(Color.RED);
		lbl22.setForeground(Color.WHITE);
		lbl22.setBounds(160, 42, 20, 20);
		panel_bloques.add(lbl22);
		tareas.put("26", lbl22);
		
		lbl23 = new JLabel("7");
		lbl23.setOpaque(true);
		lbl23.setBackground(Color.RED);
		lbl23.setForeground(Color.WHITE);
		lbl23.setBounds(190, 42, 20, 20);
		panel_bloques.add(lbl23);
		tareas.put("27",lbl23);
		
		lbl24 = new JLabel("8");
		lbl24.setOpaque(true);
		lbl24.setBackground(Color.RED);
		lbl24.setForeground(Color.WHITE);
		lbl24.setBounds(220, 42, 20, 20);
		panel_bloques.add(lbl24);
		tareas.put("28",lbl24);
		
		lbl25 = new JLabel("9");
		lbl25.setOpaque(true);
		lbl25.setBackground(Color.RED);
		lbl25.setForeground(Color.WHITE);
		lbl25.setBounds(250, 42, 20, 20);
		panel_bloques.add(lbl25);
		tareas.put("29",lbl25);
		
		lbl26 = new JLabel("10");
		lbl26.setOpaque(true);
		lbl26.setBackground(Color.RED);
		lbl26.setForeground(Color.WHITE);
		lbl26.setBounds(280, 42, 20, 20);
		panel_bloques.add(lbl26);
		tareas.put("210",lbl26);
		
		lbl27 = new JLabel("11");
		lbl27.setForeground(Color.WHITE);
		lbl27.setOpaque(true);
		lbl27.setBackground(Color.RED);
		lbl27.setBounds(310, 42, 20, 20);
		panel_bloques.add(lbl27);
		tareas.put("211",lbl27);
		
		lbl28 = new JLabel("12");
		lbl28.setForeground(Color.WHITE);
		lbl28.setOpaque(true);
		lbl28.setBackground(Color.RED);
		lbl28.setBounds(340, 42, 20, 20);
		panel_bloques.add(lbl28);
		tareas.put("212",lbl28);
		
		lbl29 = new JLabel("13");
		lbl29.setForeground(Color.WHITE);
		lbl29.setOpaque(true);
		lbl29.setBackground(Color.RED);
		lbl29.setBounds(370, 42, 20, 20);
		panel_bloques.add(lbl29);
		tareas.put("213",lbl29);
		
		lbl30 = new JLabel("14");
		lbl30.setForeground(Color.WHITE);
		lbl30.setOpaque(true);
		lbl30.setBackground(Color.RED);
		lbl30.setBounds(400, 42, 20, 20);
		panel_bloques.add(lbl30);
		tareas.put("214",lbl30);
		
		lbl31 = new JLabel("15");
		lbl31.setForeground(Color.WHITE);
		lbl31.setOpaque(true);
		lbl31.setBackground(Color.RED);
		lbl31.setBounds(430, 42, 20, 20);
		panel_bloques.add(lbl31);
		tareas.put("215",lbl31);
		
		lbl32 = new JLabel("16");
		lbl32.setForeground(Color.WHITE);
		lbl32.setOpaque(true);
		lbl32.setBackground(Color.RED);
		lbl32.setBounds(460, 42, 20, 20);
		panel_bloques.add(lbl32);
		tareas.put("216",lbl32);
		
		lbl33 = new JLabel("1");
		lbl33.setForeground(Color.WHITE);
		lbl33.setOpaque(true);
		lbl33.setBackground(Color.RED);
		lbl33.setBounds(10, 73, 20, 20);
		panel_bloques.add(lbl33);
		tareas.put("31",lbl33);
		
		lbl34 = new JLabel("2");
		lbl34.setOpaque(true);
		lbl34.setBackground(Color.RED);
		lbl34.setForeground(Color.WHITE);
		lbl34.setBounds(40, 73, 20, 20);
		panel_bloques.add(lbl34);
		tareas.put("32",lbl34);
		
		lbl35 = new JLabel("3");
		lbl35.setOpaque(true);
		lbl35.setBackground(Color.RED);
		lbl35.setForeground(Color.WHITE);
		lbl35.setBounds(70, 73, 20, 20);
		panel_bloques.add(lbl35);
		tareas.put("33",lbl35);
		
		lbl36 = new JLabel("4");
		lbl36.setOpaque(true);
		lbl36.setBackground(Color.RED);
		lbl36.setForeground(Color.WHITE);
		lbl36.setBounds(100, 73, 20, 20);
		panel_bloques.add(lbl36);
		tareas.put("34",lbl36);
		
		lbl37 = new JLabel("5");
		lbl37.setOpaque(true);
		lbl37.setBackground(Color.RED);
		lbl37.setForeground(Color.WHITE);
		lbl37.setBounds(130, 73, 20, 20);
		panel_bloques.add(lbl37);
		tareas.put("35",lbl37);
		
		lbl38 = new JLabel("6");
		lbl38.setOpaque(true);
		lbl38.setBackground(Color.RED);
		lbl38.setForeground(Color.WHITE);
		lbl38.setBounds(160, 73, 20, 20);
		panel_bloques.add(lbl38);
		tareas.put("36",lbl38);
		
		lbl39 = new JLabel("7");
		lbl39.setOpaque(true);
		lbl39.setBackground(Color.RED);
		lbl39.setForeground(Color.WHITE);
		lbl39.setBounds(190, 73, 20, 20);
		panel_bloques.add(lbl39);
		tareas.put("37",lbl39);
		
		lbl40 = new JLabel("8");
		lbl40.setOpaque(true);
		lbl40.setBackground(Color.RED);
		lbl40.setForeground(Color.WHITE);
		lbl40.setBounds(220, 73, 20, 20);
		panel_bloques.add(lbl40);
		tareas.put("38",lbl40);
		
		lbl41 = new JLabel("9");
		lbl41.setOpaque(true);
		lbl41.setBackground(Color.RED);
		lbl41.setForeground(Color.WHITE);
		lbl41.setBounds(250, 73, 20, 20);
		panel_bloques.add(lbl41);
		tareas.put("39",lbl41);
		
		lbl42 = new JLabel("10");
		lbl42.setOpaque(true);
		lbl42.setBackground(Color.RED);
		lbl42.setForeground(Color.WHITE);
		lbl42.setBounds(280, 73, 20, 20);
		panel_bloques.add(lbl42);
		tareas.put("310",lbl42);
		
		lbl43 = new JLabel("11");
		lbl43.setOpaque(true);
		lbl43.setBackground(Color.RED);
		lbl43.setForeground(Color.WHITE);
		lbl43.setBounds(310, 73, 20, 20);
		panel_bloques.add(lbl43);
		tareas.put("311",lbl43);
		
		lbl44 = new JLabel("12");
		lbl44.setOpaque(true);
		lbl44.setBackground(Color.RED);
		lbl44.setForeground(Color.WHITE);
		lbl44.setBounds(340, 73, 20, 20);
		panel_bloques.add(lbl44);
		tareas.put("312",lbl44);
		
		lbl45 = new JLabel("13");
		lbl45.setOpaque(true);
		lbl45.setBackground(Color.RED);
		lbl45.setForeground(Color.WHITE);
		lbl45.setBounds(370, 73, 20, 20);
		panel_bloques.add(lbl45);
		tareas.put("313",lbl45);
		
		lbl46 = new JLabel("14");
		lbl46.setOpaque(true);
		lbl46.setBackground(Color.RED);
		lbl46.setForeground(Color.WHITE);
		lbl46.setBounds(400, 73, 20, 20);
		panel_bloques.add(lbl46);
		tareas.put("314",lbl46);
		
		lbl47 = new JLabel("15");
		lbl47.setOpaque(true);
		lbl47.setBackground(Color.RED);
		lbl47.setForeground(Color.WHITE);
		lbl47.setBounds(430, 73, 20, 20);
		panel_bloques.add(lbl47);
		tareas.put("315",lbl47);
		
		lbl48 = new JLabel("16");
		lbl48.setOpaque(true);
		lbl48.setForeground(Color.WHITE);
		lbl48.setBackground(Color.RED);
		lbl48.setBounds(460, 73, 20, 20);
		panel_bloques.add(lbl48);
		tareas.put("316",lbl48);
		
		lbl49 = new JLabel("1");
		lbl49.setOpaque(true);
		lbl49.setBackground(Color.RED);
		lbl49.setForeground(Color.WHITE);
		lbl49.setBounds(10, 103, 20, 20);
		panel_bloques.add(lbl49);
		tareas.put("41",lbl49);
		
		lbl50 = new JLabel("2");
		lbl50.setOpaque(true);
		lbl50.setBackground(Color.RED);
		lbl50.setForeground(Color.WHITE);
		lbl50.setBounds(40, 103, 20, 20);
		panel_bloques.add(lbl50);
		tareas.put("42",lbl50);
		
		lbl51 = new JLabel("3");
		lbl51.setOpaque(true);
		lbl51.setBackground(Color.RED);
		lbl51.setForeground(Color.WHITE);
		lbl51.setBounds(70, 103, 20, 20);
		panel_bloques.add(lbl51);
		tareas.put("43",lbl51);
		
		lbl52 = new JLabel("4");
		lbl52.setOpaque(true);
		lbl52.setBackground(Color.RED);
		lbl52.setForeground(Color.WHITE);
		lbl52.setBounds(100, 103, 20, 20);
		panel_bloques.add(lbl52);
		tareas.put("44",lbl52);
		
		lbl53 = new JLabel("5");
		lbl53.setOpaque(true);
		lbl53.setBackground(Color.RED);
		lbl53.setForeground(Color.WHITE);
		lbl53.setBounds(130, 104, 20, 20);
		panel_bloques.add(lbl53);
		tareas.put("45",lbl53);
		
		lbl54 = new JLabel("6");
		lbl54.setForeground(Color.WHITE);
		lbl54.setOpaque(true);
		lbl54.setBackground(Color.RED);
		lbl54.setBounds(160, 104, 20, 20);
		panel_bloques.add(lbl54);
		tareas.put("46",lbl54);
		
		lbl55 = new JLabel("7");
		lbl55.setForeground(Color.WHITE);
		lbl55.setOpaque(true);
		lbl55.setBackground(Color.RED);
		lbl55.setBounds(190, 104, 20, 20);
		panel_bloques.add(lbl55);
		tareas.put("47",lbl55);
		
		lbl56 = new JLabel("8");
		lbl56.setForeground(Color.WHITE);
		lbl56.setOpaque(true);
		lbl56.setBackground(Color.RED);
		lbl56.setBounds(220, 104, 20, 20);
		panel_bloques.add(lbl56);
		tareas.put("48",lbl56);
		
		lbl57 = new JLabel("9");
		lbl57.setForeground(Color.WHITE);
		lbl57.setOpaque(true);
		lbl57.setBackground(Color.RED);
		lbl57.setBounds(250, 103, 20, 20);
		panel_bloques.add(lbl57);
		tareas.put("49",lbl57);
		
		lbl58 = new JLabel("10");
		lbl58.setForeground(Color.WHITE);
		lbl58.setOpaque(true);
		lbl58.setBackground(Color.RED);
		lbl58.setBounds(280, 104, 20, 20);
		panel_bloques.add(lbl58);
		tareas.put("410",lbl58);
		
		lbl59 = new JLabel("11");
		lbl59.setForeground(Color.WHITE);
		lbl59.setOpaque(true);
		lbl59.setBackground(Color.RED);
		lbl59.setBounds(310, 104, 20, 20);
		panel_bloques.add(lbl59);
		tareas.put("411",lbl59);
		
		lbl60 = new JLabel("12");
		lbl60.setForeground(Color.WHITE);
		lbl60.setOpaque(true);
		lbl60.setBackground(Color.RED);
		lbl60.setBounds(340, 104, 20, 20);
		panel_bloques.add(lbl60);
		tareas.put("412",lbl60);
		
		lbl61 = new JLabel("13");
		lbl61.setForeground(Color.WHITE);
		lbl61.setOpaque(true);
		lbl61.setBackground(Color.RED);
		lbl61.setBounds(370, 103, 20, 20);
		panel_bloques.add(lbl61);
		tareas.put("413",lbl61);
		
		lbl62 = new JLabel("14");
		lbl62.setForeground(Color.WHITE);
		lbl62.setOpaque(true);
		lbl62.setBackground(Color.RED);
		lbl62.setBounds(400, 104, 20, 20);
		panel_bloques.add(lbl62);
		tareas.put("414",lbl62);
		
		lbl63 = new JLabel("15");
		lbl63.setForeground(Color.WHITE);
		lbl63.setOpaque(true);
		lbl63.setBackground(Color.RED);
		lbl63.setBounds(430, 104, 20, 20);
		panel_bloques.add(lbl63);
		tareas.put("415",lbl63);
		
		lbl64 = new JLabel("16");
		lbl64.setForeground(Color.WHITE);
		lbl64.setOpaque(true);
		lbl64.setBackground(Color.RED);
		lbl64.setBounds(460, 104, 20, 20);
		panel_bloques.add(lbl64);
		tareas.put("416",lbl64);
		
		lbl65 = new JLabel("1");
		lbl65.setOpaque(true);
		lbl65.setBackground(Color.RED);
		lbl65.setForeground(Color.WHITE);
		lbl65.setBounds(10, 133, 20, 20);
		panel_bloques.add(lbl65);
		tareas.put("51",lbl65);
		
		lbl66 = new JLabel("2");
		lbl66.setOpaque(true);
		lbl66.setBackground(Color.RED);
		lbl66.setForeground(Color.WHITE);
		lbl66.setBounds(40, 133, 20, 20);
		panel_bloques.add(lbl66);
		tareas.put("52",lbl66);
		
		lbl67 = new JLabel("3");
		lbl67.setOpaque(true);
		lbl67.setBackground(Color.RED);
		lbl67.setForeground(Color.WHITE);
		lbl67.setBounds(70, 133, 20, 20);
		panel_bloques.add(lbl67);
		tareas.put("53",lbl67);
		
		lbl68 = new JLabel("4");
		lbl68.setOpaque(true);
		lbl68.setBackground(Color.RED);
		lbl68.setForeground(Color.WHITE);
		lbl68.setBounds(100, 133, 20, 20);
		panel_bloques.add(lbl68);
		tareas.put("54",lbl68);
		
		lbl69 = new JLabel("5");
		lbl69.setOpaque(true);
		lbl69.setBackground(Color.RED);
		lbl69.setForeground(Color.WHITE);
		lbl69.setBounds(130, 133, 20, 20);
		panel_bloques.add(lbl69);
		tareas.put("55",lbl69);
		
		lbl70 = new JLabel("6");
		lbl70.setOpaque(true);
		lbl70.setBackground(Color.RED);
		lbl70.setForeground(Color.WHITE);
		lbl70.setBounds(160, 133, 20, 20);
		panel_bloques.add(lbl70);
		tareas.put("56",lbl70);
		
		lbl71 = new JLabel("7");
		lbl71.setOpaque(true);
		lbl71.setBackground(Color.RED);
		lbl71.setForeground(Color.WHITE);
		lbl71.setBounds(190, 133, 20, 20);
		panel_bloques.add(lbl71);
		tareas.put("57",lbl71);
		
		lbl72 = new JLabel("8");
		lbl72.setOpaque(true);
		lbl72.setBackground(Color.RED);
		lbl72.setForeground(Color.WHITE);
		lbl72.setBounds(220, 133, 20, 20);
		panel_bloques.add(lbl72);
		tareas.put("58",lbl72);
		
		lbl73 = new JLabel("9");
		lbl73.setOpaque(true);
		lbl73.setBackground(Color.RED);
		lbl73.setForeground(Color.WHITE);
		lbl73.setBounds(250, 133, 20, 20);
		panel_bloques.add(lbl73);
		tareas.put("59",lbl73);
		
		lbl74 = new JLabel("10");
		lbl74.setOpaque(true);
		lbl74.setBackground(Color.RED);
		lbl74.setForeground(Color.WHITE);
		lbl74.setBounds(280, 133, 20, 20);
		panel_bloques.add(lbl74);
		tareas.put("510",lbl74);
		
		lbl75 = new JLabel("11");
		lbl75.setOpaque(true);
		lbl75.setBackground(Color.RED);
		lbl75.setForeground(Color.WHITE);
		lbl75.setBounds(310, 133, 20, 20);
		panel_bloques.add(lbl75);
		tareas.put("511",lbl75);
		
		lbl76 = new JLabel("12");
		lbl76.setOpaque(true);
		lbl76.setBackground(Color.RED);
		lbl76.setForeground(Color.WHITE);
		lbl76.setBounds(340, 133, 20, 20);
		panel_bloques.add(lbl76);
		tareas.put("512",lbl76);
		
		lbl77 = new JLabel("13");
		lbl77.setOpaque(true);
		lbl77.setBackground(Color.RED);
		lbl77.setForeground(Color.WHITE);
		lbl77.setBounds(370, 133, 20, 20);
		panel_bloques.add(lbl77);
		tareas.put("513",lbl77);
		
		lbl78 = new JLabel("14");
		lbl78.setOpaque(true);
		lbl78.setBackground(Color.RED);
		lbl78.setForeground(Color.WHITE);
		lbl78.setBounds(400, 133, 20, 20);
		panel_bloques.add(lbl78);
		tareas.put("514",lbl78);
		
		lbl79 = new JLabel("15");
		lbl79.setOpaque(true);
		lbl79.setBackground(Color.RED);
		lbl79.setForeground(Color.WHITE);
		lbl79.setBounds(430, 133, 20, 20);
		panel_bloques.add(lbl79);
		tareas.put("515",lbl79);
		
		lbl80 = new JLabel("16");
		lbl80.setOpaque(true);
		lbl80.setBackground(Color.RED);
		lbl80.setForeground(Color.WHITE);
		lbl80.setBounds(460, 133, 20, 20);
		panel_bloques.add(lbl80);
		tareas.put("516",lbl80);
		
		panel_bloques.setText("\n\n\n\n\n\n\n\n\n\n TOTAL: 5 BLOQUES");
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
				//PASA A COLOR AZUL
				tareas.get(clave).setBackground(Color.BLUE);
				break;
			case completada:
				//PASA A COLOR VERDE
				tareas.get(clave).setBackground(Color.green);
				break;
			default:
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
				}else{ //SI NO CAMBIO EL ESTADO USUARIO MUESTRO PARCIALES Y FINALES
					String resultado = (String) objeto; 
					textPaneMsj.setText(textPaneMsj.getText() + "\n" + resultado);
				}
			}else{
				//por si despues necesitamos que observe a algo mas
				this.mostrarError("fallo el observador! :(");
			}
		}
		
		
	}

}
