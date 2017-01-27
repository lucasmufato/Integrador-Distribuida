package servidor.vista;

import javax.swing.JFrame;

import servidor.Backup;
import servidor.HiloBackup;
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

import baseDeDatos.Usuario;
import bloquesYTareas.Bloque;
import bloquesYTareas.EstadoTarea;
import bloquesYTareas.Tarea;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.JSpinner;

import mensajes.replicacion.MensajeGeneracionTarea;

import java.awt.SystemColor;

public class ServidorVista extends JFrame implements Observer{
	
	//variables de la clase
	protected static final long serialVersionUID = 1L;
	protected Primario servidor;
	protected Backup backup;
	protected boolean secundario;
	//private HashMap<String, JLabel> tareas = new HashMap<String, JLabel>();
	protected HashMap<Integer, JLabel> tareas =  new HashMap<Integer, JLabel>();
	
	//variables de la vista
	protected JPanel jpanel_servidor;
	protected JTextField textFieldPuerto;
	protected JButton btnConectarServidor;
	protected JTextPane textPaneConsola;
	protected JPanel panel;
	protected JPanel jpanel_trabajo;
	protected JTextPane textPaneConsolaTrabajo;
	protected JLabel lblIPServidor;
	protected JLabel lblPuertoServidor;

	protected JTextPane textPaneMsj;
	protected JPanel panel_bloques;
	protected JLabel label_logo;
	protected JScrollPane scrollBarBloques;

	//TAMA�O
	protected Integer alto = 500;
	protected Integer ancho = 800;
	
	public ServidorVista(Primario servidor){
		this.setMinimumSize(new Dimension(this.ancho,this.alto)); 
		setTitle("Servidor BitCoin Mining");
		this.servidor=servidor;
		this.crearVista(false);
	}
	
	public ServidorVista(Backup backup){
		this.setMinimumSize(new Dimension(this.ancho,this.alto)); 
		setTitle("Servidor Backup BitCoin Mining");
		this.backup = backup;
		this.crearVista(true);
	}
	
	public void crearVista(boolean secundario) {
		this.secundario = secundario;
		this.setVisible(true);
		this.setResizable(false);
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
		lblPuertoDeEscucha.setBounds(310, 286, 180, 24);
		jpanel_servidor.add(lblPuertoDeEscucha);
		
		//TEXFIELD DEL PUERTO
		textFieldPuerto = new JTextField();
		textFieldPuerto.setBackground(Color.DARK_GRAY);
		textFieldPuerto.setForeground(Color.BLACK);
		textFieldPuerto.setEnabled(false);
		textFieldPuerto.setEditable(false);
		textFieldPuerto.setBounds((this.ancho-180)/2, ((this.alto-20)*2/3), 180, 24);
		jpanel_servidor.add(textFieldPuerto);
		textFieldPuerto.setColumns(10);
		
		if (secundario) {
			textFieldPuerto.setText("6666");
		} else {
			textFieldPuerto.setText("5555");
		}
		/*
		Properties propiedades = new Properties();
	    InputStream entrada = null;
	    try {
			entrada = new FileInputStream("configuracion.properties");
			propiedades.load(entrada);
			String puertoA = propiedades.getProperty("PUERTOA");
			textFieldPuerto.setText(puertoA);
	    } catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/
		//CUANDO PRESIONES EL BOTON, VOY A CHEQUEAR QUE EL PUERTO QUE INGRESE ESTE LIBRE
		btnConectarServidor = new JButton("Conectar Servidor");
		btnConectarServidor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try{
					Integer puerto =Integer.parseInt(textFieldPuerto.getText());
					if(puerto<1024){
						throw new NumberFormatException();
					}
					if (secundario == false) {
						if(servidor.pedirPuerto(puerto)){
							iniciarServidor();
						}
					} else {
						if (backup.pedirPuerto(puerto)) {
							iniciarServidor();
							
						}
					}
				}catch(NumberFormatException e1){
					//si el numero es muy grande tirar error al parsearlo a Integer
					MostrarPopUp("el numero de puerto no es correcto");
				}
				
			}
		});
		btnConectarServidor.setBounds((this.ancho-180)/2, ((this.alto-20)*2/3)+32, 180, 24);
		jpanel_servidor.add(btnConectarServidor);
		
		//TEXT PANE EN DONDE SE VAN A IR MOSTRANDO MSJs
		textPaneConsola = new JTextPane();
		textPaneConsola.setForeground(Color.DARK_GRAY);
		textPaneConsola.setBackground(new java.awt.Color(209, 218, 225));
		textPaneConsola.setBounds(0, 452, this.ancho-(800-794), this.alto-(500-20));
		jpanel_servidor.add(textPaneConsola);
		
		label_logo = new JLabel("");
		try {
			label_logo.setIcon(new ImageIcon(ServidorVista.class.getResource("/servidor/vista/icono.jpg")));
		} catch (Exception e) {
			label_logo.setBackground(Color.WHITE);
			System.out.println("No se encontro la imagen ");
		}
		label_logo.setHorizontalAlignment(SwingConstants.CENTER);
		label_logo.setBounds((this.ancho-128)/2, ((this.alto-128)/2)-64, 128, 128);
		jpanel_servidor.add(label_logo);
		
		//------------------------------------------------------------VISTA 2-----------------------------------------------------------		
		jpanel_trabajo = new JPanel();
		jpanel_trabajo.setBackground(new java.awt.Color(23, 26, 33));
		panel.add(jpanel_trabajo, "");
		jpanel_trabajo.setLayout(null);
		
		textPaneConsolaTrabajo = new JTextPane();
		textPaneConsolaTrabajo.setBackground(new java.awt.Color(209, 218, 225));
		textPaneConsolaTrabajo.setForeground(Color.DARK_GRAY);
		textPaneConsolaTrabajo.setBounds(0, 452, this.ancho-(800-794), this.alto-(500-20));
		jpanel_trabajo.add(textPaneConsolaTrabajo);
		
		lblIPServidor = new JLabel("");
		lblIPServidor.setForeground(new java.awt.Color(189, 187, 185));
		lblIPServidor.setBounds(54, 11, this.ancho-(800-148), this.alto-(500-14));
		jpanel_trabajo.add(lblIPServidor);
		
		lblPuertoServidor = new JLabel("");
		lblPuertoServidor.setForeground(new java.awt.Color(189, 187, 185));
		lblPuertoServidor.setBounds(343, 11, this.ancho-(800-139), this.alto-(500-14));
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
				if (secundario == false) {
					//LAMARA A UN METODO DESCONECTAR EN LA CLASE PRIMARIO
					servidor.desconectarse();
					System.exit(0);
				}else{
					//LLAMA A UN METODO DESCONECTAR EN LA CLASE BACKUP
					backup.desconectarse();
				}
			}
		});
		btnDesconectar.setBackground(new java.awt.Color(23, 26, 33));
		btnDesconectar.setBounds(752, 11, 32, 32);
		jpanel_trabajo.add(btnDesconectar);
		
		JLabel lblBloquesYTareas = new JLabel("BLOQUES Y SUS TAREAS");
		lblBloquesYTareas.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblBloquesYTareas.setForeground(new java.awt.Color(189, 187, 185));
		lblBloquesYTareas.setBounds(203, 78, this.ancho-(800-184), this.alto-(500-14));
		jpanel_trabajo.add(lblBloquesYTareas); 
		
		
		textPaneMsj = new JTextPane();
		textPaneMsj.setBounds(10, 132, this.ancho-(800-512), this.alto-(500-130));
		
		JScrollPane scrollBarMsj = new JScrollPane(textPaneMsj);
		scrollBarMsj.setBounds(10, 248, this.ancho-(800-512), this.alto-(500-183));
		jpanel_trabajo.add(scrollBarMsj);
		
		JButton btnGenerarBloques = new JButton("Generar bloques de trabajo");
		btnGenerarBloques.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (secundario == false) {
					servidor.generarBloque();
				}
			}
		});
		btnGenerarBloques.setBackground(SystemColor.inactiveCaption);
		btnGenerarBloques.setBounds(40, 36, this.ancho-(800-210), this.alto-(500-23));
		jpanel_trabajo.add(btnGenerarBloques);
		
		JSpinner spinner = new JSpinner();
		spinner.setBounds(462, 36, 60, 20);
		//jpanel_trabajo.add(spinner); // Esto lo escondo por ahora porque todavia no esta implementada la funcionalidad
		
		JLabel lblDificultad = new JLabel("Dificultad : ");
		lblDificultad.setForeground(new java.awt.Color(189, 187, 185));
		lblDificultad.setBounds(353, 40, this.ancho-(800-92), this.alto-(500-14));
		//jpanel_trabajo.add(lblDificultad); // Esto lo escondo por ahora porque todavia no esta implementada la funcionalidad
		
		this.revalidate();
		this.repaint();
	}
	
	public boolean iniciarServidor(){
		if (secundario == false) {
			//ES LLAMADO POR EL BOTON CONECTAR SERVIDOR 
			Thread ser =new Thread(this.servidor);
			ser.start();
			//UNA VEZ QUE DOY EL START, EL METODO RUN VA A LLAMAR AL ESPERARCLIENTES() DEL PRIMARIO
		} else {
			System.out.println("Es backup");
			this.backup.esperarActualizaciones();
		}
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
		panel.remove(jpanel_servidor);
		jpanel_servidor.remove(label_logo);
		jpanel_trabajo.add(label_logo);
		label_logo.setBounds((this.ancho-148), 100, 128, 128);
		jpanel_trabajo.setVisible(true);
		
		//MUESTRO LA IP Y EL PUERTO
		if (secundario == false) {
			lblIPServidor.setText("Mi IP: " + this.servidor.getIP());
			lblPuertoServidor.setText("Puerto: " + String.valueOf(this.servidor.getPuerto()));
		} else {
			lblIPServidor.setText("Mi IP: " + this.backup.getIP());
			lblPuertoServidor.setText("Puerto: " + String.valueOf(this.backup.getPuerto()));	
		}
		
	}
	
	@Override
	public void update(Observable claseLLamadora, Object objeto) {
		//ya que voy a recibir distintos tipos de objetos, tengo q ver quien la clase llamadora (la que cambio de estado)
		//y que objeto me pasa
		if(objeto.getClass().equals(Tarea.class)){
			//si una tarea cambio de datos cambio de estado
			// System.out.println("observe en una tarea:");
			Tarea tarea = (Tarea) objeto;
			//VOY A SACAR EL ESTADO DE LA TAREA PARA VER QUE COLOR PONGO, Y VOY A SACAR EL ID BLOQUE Y EL ID TAREA
			int id_tarea = tarea.getId();
			//Bloque bloque = tarea.getBloque();
			//int idBloque = bloque.getId();
			EstadoTarea estado = tarea.getEstado();
			// La clave del Hashmap es la id de la tarea
			Integer clave = id_tarea;
			//VEO A QUE ESTADO CAMBIO
			try {
				tareas.get(clave).setBackground(colorEstado(estado));
			} catch (Exception e) {
			}
		
		}else{
			if(claseLLamadora.getClass().equals(HiloConexionPrimario.class)){
				// System.out.println("observe un cambio en HiloConexionPrimario:");
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
                if(claseLLamadora.getClass().equals(HiloBackup.class)){
                	System.out.println("Observe un cambio en hilo backup");
                	if(objeto.getClass().equals(MensajeGeneracionTarea.class)){
                		this.actualizarAreadeBloques(this.backup.obtenerBloquesNoCompletados());
                	}else{
                		String resultado = (String) objeto; 
        				textPaneMsj.setText(textPaneMsj.getText() + "\n" + resultado);
                	}
                }else{
                	System.out.println("Fallo observador :O");
                }	
			}
		}
		
	}

	//----------------------------------------------- VISTA 2 -------------------------------------------------------
	public void crearAreadeBloques(ArrayList<Bloque> bs) {
		panel_bloques = new JPanel();
		panel_bloques.setLayout(null);
		panel_bloques.setForeground(Color.LIGHT_GRAY);
		panel_bloques.setBackground(new java.awt.Color(27, 40, 56));
		
		scrollBarBloques = new JScrollPane(panel_bloques);
		scrollBarBloques.setBounds(10, 100, this.ancho-(800-512), this.alto-(500-130));
		Integer x = 10, y = 11;
		JLabel label;
		Integer cantidadBloques = bs.size();
		Integer cantidadTareas;
		for(int numbloques = 0; numbloques < cantidadBloques; numbloques++) {
			//LA CANTIDAD DE TAREAS DEL BLOQUE "NUMBLOQUES"
			JLabel label_bloque = new JLabel ("Bloque " + bs.get(numbloques).getId() + ":");
			panel_bloques.add(label_bloque);
			label_bloque.setBounds (x, y, 120, 12);
			label_bloque.setForeground(Color.LIGHT_GRAY);
			y += 16;
			
			cantidadTareas = bs.get(numbloques).getTareas().size();
			System.out.println("Bloque " + numbloques + ": ");
			System.out.println(cantidadTareas);
			for (int numtareas = 1; numtareas <= cantidadTareas; numtareas++) {
				//SI LLEGUE A 16 TAREAS TENGO QUE BAJAR DE RENGLON
				if ((numtareas % 16) == 0){
					label = new JLabel();
					label.setOpaque(true);
					label.setBackground(colorEstado(bs.get(numbloques).getTareas().get(numtareas-1).getEstado()));
					label.setBounds(x, y, 20, 20);
					label.setText(""+(bs.get(numbloques).getTareas().get(numtareas-1).getId())); 
					label.setFont(new Font("Tahoma", Font.PLAIN, 9));
					panel_bloques.add(label);
					y += 31;
					x = 10;
				} else {
					label = new JLabel();
					label.setOpaque(true); label.setBackground(colorEstado(bs.get(numbloques).getTareas().get(numtareas-1).getEstado()));
					label.setBounds(x, y, 20, 20);
					label.setText(""+(bs.get(numbloques).getTareas().get(numtareas-1).getId())); 
					label.setFont(new Font("Tahoma", Font.PLAIN, 9));
					panel_bloques.add(label);
					x += 30;
				}
				//GUARDO EN EL HASHMAP, EL ID DE LA TAREA COMO CLAVE
				tareas.put(bs.get(numbloques).getTareas().get(numtareas-1).getId(), label);
			}
			x = 10;
            y += 31;
		}
        panel_bloques.setPreferredSize(new Dimension(20*16,y));
		jpanel_trabajo.add(scrollBarBloques);
		jpanel_trabajo.revalidate();
		jpanel_trabajo.repaint();
	}
	public void actualizarAreadeBloques(ArrayList<Bloque> bs){
		mostrarMsjConsolaTrabajo("Se ha creado un nuevo bloque de tareas.");
		tareas.clear(); // Limpiar el hashmap de tareas
		jpanel_trabajo.remove(scrollBarBloques); // Borrar los bloques antiguos
		this.crearAreadeBloques(bs); //Volver a crear el panel de bloques
		mostrarMsjConsolaTrabajo("Actualizada visualizacion de bloques.");
		
	}

	private static Color colorEstado (EstadoTarea estado) { 			switch(estado){
			case enProceso:
				//PASA A COLOR CELESTE
				return (Color.CYAN);
			case detenida:
				//PASA A COLOR AZUL
				return (Color.BLUE);
			case completada:
				//PASA A COLOR VERDE
				return (Color.GREEN);
			default:
				return (Color.RED);
		}
	}
}
