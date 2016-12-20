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
import java.util.ArrayList;
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
import java.awt.Rectangle;

import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class ServidorVista extends JFrame implements Observer{
	
	//variables de la clase
	private static final long serialVersionUID = 1L;
	private Primario servidor;
	//private HashMap<String, JLabel> tareas = new HashMap<String, JLabel>();
	private HashMap<Integer, JLabel> tareas =  new HashMap<Integer, JLabel>();
	
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
			System.out.println("observe un cambio en la BD:");
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
				System.out.println("observe un cambio en HiloConexionPrimario:");
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
				System.out.println("observe un cambio que no es ni de BD ni de HiloPrimarioConexion :O");
				//por si despues necesitamos que observe a algo mas
				this.mostrarError("fallo el observador! :(");
			}
		}
		
	}

	//----------------------------------------------- VISTA 2 -------------------------------------------------------
	public void crearAreadeBloques(ArrayList<Bloque> bs) {
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
		Integer x = 10, y = 11;
		JLabel label;
		Integer cantidadBloques = bs.size();
		Integer cantidadTareas;
		for(int numbloques = 0; numbloques < cantidadBloques; numbloques++) {
			//LA CANTIDAD DE TAREAS DEL BLOQUE "NUMBLOQUES"
			panel_bloques.setText("Bloque " + numbloques+1 + ": \n");
			cantidadTareas = bs.get(numbloques).getTareas().size();
			System.out.println("Bloque " + numbloques + ": ");
			System.out.println(cantidadTareas);
			for (int numtareas = 1; numtareas <= cantidadTareas; numtareas++) {
				//SI LLEGUE A 16 TAREAS TENGO QUE BAJAR DE RENGLON
				if ((numtareas % 16) == 0){
					label = new JLabel();
					label.setOpaque(true);
					label.setBackground(Color.RED);
					label.setBounds(x, y, 20, 20);
					label.setText(""+(numtareas)); 
					panel_bloques.add(label);
					y += 31;
					x = 10;
					panel_bloques.setText("\n\n\n\n");
				} else {
					label = new JLabel();
					label.setOpaque(true);
					label.setBackground(Color.RED);
					label.setBounds(x, y, 20, 20);
					label.setText(""+(numtareas)); 
					panel_bloques.add(label);
					x += 30;
				}
				//GUARDO EN EL HASHMAP, EL ID DE LA TAREA COMO CLAVE
				tareas.put(bs.get(numbloques).getTareas().get(numtareas-1).getId(), label);
			}
			x = 10;
            y += 31;
            panel_bloques.setText("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
		}
	}

}
