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

import bloquesYTareas.Bloque;
import bloquesYTareas.EstadoTarea;
import bloquesYTareas.Tarea;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import mensajes.replicacion.MensajeGeneracionTarea;

import java.awt.SystemColor;

import javax.swing.JSeparator;

import cliente.vista.ClienteJFrame;

public class ServidorVista extends JFrame implements Observer{
	
	//variables de la clase
	protected static final long serialVersionUID = 1L;
	protected Primario servidor;
	protected HashMap<Integer, JLabel> tareas =  new HashMap<Integer, JLabel>();
	protected HashMap<Integer, JLabel> clientes = new HashMap<Integer,JLabel>();
	
	//variables de la vista
	protected JPanel jpanel_servidor;
	protected JTextField textFieldPuerto;
	protected JButton btnConectarServidor;
	protected JTextPane textPaneConsola;
	protected JPanel panel;
	protected JPanel jpanel_trabajo;
	protected JButton btnGenerarBloques;
	protected JTextPane textPaneConsolaTrabajo;
	protected JLabel lblIPServidor;
	protected JLabel lblPuertoServidor;
	protected JPanel panel_bloques;
	protected JLabel label_logo;
	protected JScrollPane scrollBarBloques;
	protected JLabel servidorLabel;
	protected JLabel lblBloquesTotales;
	protected JLabel lblBloquesCompletados;
	protected JLabel lblBloquesIncompletos;
	private JLabel lblServidorBackup;
	private JLabel lblIpBackup;
	private JLabel lblPuertoBackup;
	private JLabel lblInformacion;
	private JScrollPane scrollBarMsj;
	private JPanel panelClientes;
	
	//TAMANiO
	protected Integer alto = 500;
	protected Integer ancho = 800;
	
	/**
	 * @wbp.parser.constructor
	 */
	public ServidorVista(Primario servidor){
		this.setMinimumSize(new Dimension(this.ancho,this.alto)); 
		setTitle("Servidor BitCoin Mining");
		this.servidor=servidor;
		this.crearVista(false);
	}
	
	public ServidorVista(Backup backup){
		this.setMinimumSize(new Dimension(this.ancho,this.alto)); 
		setTitle("Servidor BitCoin Mining - Backup");
		this.servidor = backup;
		this.crearVista(true);
	}
	
	public void crearVista(boolean secundario) {
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
		textFieldPuerto.setBackground(Color.WHITE);
		textFieldPuerto.setForeground(Color.BLACK);
		textFieldPuerto.setEnabled(true);
		textFieldPuerto.setEditable(true);
		textFieldPuerto.setBounds((this.ancho-180)/2, ((this.alto-20)*2/3), 180, 24);
		jpanel_servidor.add(textFieldPuerto);
		textFieldPuerto.setColumns(10);
		
		if (secundario) {
			textFieldPuerto.setText("6666");
		} else {
			textFieldPuerto.setText("5555");
		}
		
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
		jpanel_trabajo =  new JPanel();
		jpanel_trabajo.setBackground(new java.awt.Color(23, 26, 33));
		panel.add(jpanel_trabajo, "");
		jpanel_trabajo.setLayout(null);
		
		textPaneConsolaTrabajo = new JTextPane();
		textPaneConsolaTrabajo.setBackground(new java.awt.Color(209, 218, 225));
		textPaneConsolaTrabajo.setForeground(Color.DARK_GRAY);
		textPaneConsolaTrabajo.setBounds(0, 452, this.ancho-(800-794), this.alto-(500-20));
		jpanel_trabajo.add(textPaneConsolaTrabajo);
		
		lblIPServidor = new JLabel("IP: ");
		lblIPServidor.setForeground(new java.awt.Color(189, 187, 185));
		lblIPServidor.setBounds(54, 11, this.ancho-(800-148), this.alto-(500-14));
		jpanel_trabajo.add(lblIPServidor);
		
		lblPuertoServidor = new JLabel("Puerto: ");
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
				servidor.desconectarse();
				morir();
				System.exit(0);
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
		
		panelClientes = new JPanel();
		panelClientes.setBackground(Color.LIGHT_GRAY);
		panelClientes.setLayout(null);
		panelClientes.setPreferredSize(new Dimension(this.ancho-(900-512), this.alto-(550-183)));
		
		scrollBarMsj = new JScrollPane(panelClientes);
		

		scrollBarMsj.setBounds(10, 248, this.ancho-(800-512), this.alto-(500-183));
		jpanel_trabajo.add(scrollBarMsj);
		
		btnGenerarBloques = new JButton("Generar bloques de trabajo");
		if(secundario == true){
			btnGenerarBloques.setVisible(false);
		}
		btnGenerarBloques.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				servidor.generarBloque();
			}
		});
		btnGenerarBloques.setBackground(SystemColor.inactiveCaption);
		btnGenerarBloques.setBounds(40, 36, this.ancho-(800-210), this.alto-(500-23));
		jpanel_trabajo.add(btnGenerarBloques);
		
		this.servidorLabel = new JLabel("Tipo de Servidor");
		servidorLabel.setBounds(343, 36, 302, 23);
		servidorLabel.setForeground(new java.awt.Color(189, 187, 185));
		jpanel_trabajo.add(servidorLabel);
		
		lblServidorBackup = new JLabel("Informacion del Servidor Backup:");
		lblServidorBackup.setBounds(546, 78, 250, 15);
		lblServidorBackup.setForeground(new java.awt.Color(189, 187, 185));
		jpanel_trabajo.add(lblServidorBackup);
		
		lblIpBackup = new JLabel("IP:");
		lblIpBackup.setBounds(565, 104, 191, 15);
		lblIpBackup.setForeground(new java.awt.Color(189, 187, 185));
		jpanel_trabajo.add(lblIpBackup);
		
		lblPuertoBackup = new JLabel("Puerto:");
		lblPuertoBackup.setBounds(565, 131, 191, 15);
		lblPuertoBackup.setForeground(new java.awt.Color(189, 187, 185));
		jpanel_trabajo.add(lblPuertoBackup);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(532, 157, 250, 2);
		jpanel_trabajo.add(separator);
		
		lblInformacion = new JLabel("Informacion Estadistica:");
		lblInformacion.setBounds(546, 171, 191, 15);
		jpanel_trabajo.add(lblInformacion);
		lblInformacion.setForeground(new java.awt.Color(189, 187, 185));
		
		lblBloquesTotales = new JLabel("Bloques Totales:");
		lblBloquesTotales.setBounds(565, 199, 191, 15);
		jpanel_trabajo.add(lblBloquesTotales);
		lblBloquesTotales.setForeground(new java.awt.Color(189, 187, 185));
		
		lblBloquesCompletados = new JLabel("Bloques Completados:");
		lblBloquesCompletados.setBounds(565, 226, 191, 15);
		jpanel_trabajo.add(lblBloquesCompletados);
		lblBloquesCompletados.setForeground(new java.awt.Color(189, 187, 185));
		
		lblBloquesIncompletos = new JLabel("Bloques Incompletos:");
		lblBloquesIncompletos.setBounds(565, 253, 191, 15);
		jpanel_trabajo.add(lblBloquesIncompletos);
		lblBloquesIncompletos.setForeground(new java.awt.Color(189, 187, 185));
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(532, 279, 250, 2);
		jpanel_trabajo.add(separator_1);
		
		JLabel labelIcono = new JLabel("");
		try{
			labelIcono.setIcon(new ImageIcon(ClienteJFrame.class.getResource("/cliente/vista/icono.jpg")));
		}catch(Exception e){
			labelIcono.setBackground(Color.WHITE);
			System.out.println("No se encontro la imagen ");
		}
		labelIcono.setHorizontalAlignment(SwingConstants.CENTER);
		labelIcono.setBounds(565, 291, 172, 145);
		jpanel_trabajo.add(labelIcono);
		
		this.revalidate();
		this.repaint();
	}
	
	public boolean iniciarServidor(){
		Thread ser =new Thread(this.servidor);
		ser.start();
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
		//jpanel_trabajo.add(label_logo);
		//label_logo.setBounds((this.ancho-148), 100, 128, 128);
		jpanel_trabajo.setVisible(true);
		
		lblIPServidor.setText("Mi IP: " + this.servidor.getIP());
		lblPuertoServidor.setText("Puerto: " + String.valueOf(this.servidor.getPuerto()));
		
	}
	
	@Override
	public synchronized void update(Observable claseLLamadora, Object objeto) {
		//ya que voy a recibir distintos tipos de objetos, tengo q ver quien la clase llamadora (la que cambio de estado)
		//y que objeto me pasa
		if(objeto.getClass().equals(Tarea.class)){
			//si una tarea cambio de datos cambio de estado
			Tarea tarea = (Tarea) objeto;
			//VOY A SACAR EL ESTADO DE LA TAREA PARA VER QUE COLOR PONGO, Y VOY A SACAR EL ID BLOQUE Y EL ID TAREA
			int id_tarea = tarea.getId();
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
				//si el que me indica que cambio de estado es el hilo primario
				//pregunto si cambio el estado del usuario u otra cosa
				if(objeto instanceof String){
					String o=(String) objeto;
					//saco lo q hay hasta el ; que es el nro de sesion con el q identifico el JLabel
					if(!o.contains(";")){
						this.mostrarMsjConsolaTrabajo(o);
					}else{
						Integer nro= Integer.parseInt( o.substring(0,o.indexOf(";")) );
						int i=o.indexOf(";");
						i++;
						String text = o.substring(i, o.length());
						
						if(this.clientes.containsKey(nro)){
							this.clientes.get(nro).setText(text);
						}else{
							JLabel clienteX = new JLabel(text);
							clienteX.setBounds(5,(this.clientes.size()*20)+10 , 500, 20);
							this.panelClientes.add(clienteX);
							//clienteX.setForeground(Color.LIGHT_GRAY);
							//clienteX.setBackground(new java.awt.Color(27, 40, 56));
							this.clientes.put(nro, clienteX);	
						}
					}
				}else if(objeto instanceof Integer){
					//saco el JLabel
					int o=(int) objeto;
					if(this.clientes.containsKey(o)){
						this.mostrarMsjConsolaTrabajo("Se desconecto un cliente");
						this.clientes.get(o).setVisible(false);
						this.panelClientes.remove(this.clientes.get(o));
						this.clientes.remove(o);
						this.panelClientes.validate();
						this.panelClientes.repaint();
						//reacomodo el resto de los JLabels con un for horrible
						int y=0;
						for(int i=0;i<30;i++){
							JLabel label=this.clientes.get(i);
							if(label!=null){
								label.setBounds(5, (y*20)+10, 500, 20);
								y++;
							}
						}
					}
				}
			}else{
                if(claseLLamadora.getClass().equals(HiloBackup.class)){
                	if(objeto.getClass().equals(MensajeGeneracionTarea.class)){
                		this.actualizarAreadeBloques(this.servidor.obtenerBloquesNoCompletados());
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

	public void actualizarInfoBloques (Integer[] estados) {
		/*lblBloquesTotales
		lblBloquesCompletados 
		lblBloquesIncompletos*/
		this.lblBloquesTotales.setText("Bloques totales: " + estados[0]);
		this.lblBloquesIncompletos.setText("Bloques incompletos: " + estados[1]);
		this.lblBloquesCompletados.setText("Bloques completados: " + estados[2]);
		
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
	
	public void setServidorText(String texto){
		this.servidorLabel.setText(texto);
	}

	public void setTituloVentana (String titulo) {
		this.setTitle (titulo);
	}
	
	private void morir() {
		this.dispose();
		this.setVisible(false);
		this.jpanel_servidor=null;
		this.jpanel_trabajo=null;
		this.servidor=null;
		this.tareas=null;
	}
	
	public void actualizarDatosBackup(String ip, Integer puerto){
		if(ip==null){
			this.lblIpBackup.setText("IP: -");
		}else{
			this.lblIpBackup.setText("IP: "+ip);
		}
		if(puerto==null){
			this.lblPuertoBackup.setText("Puerto: -");
		}else{
			this.lblPuertoBackup.setText("Puerto: "+puerto);
		}
	}
	
	public void setNombrePanelInformacio(String PrimarioOBackup){
		this.lblServidorBackup.setText("Informacion del Servidor "+PrimarioOBackup);
	}

	public void mostrarBotonGenerarBloques () {
		this.btnGenerarBloques.setVisible (true);
	}
}
