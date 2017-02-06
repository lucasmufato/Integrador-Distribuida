package cliente.vista;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.JOptionPane;

import cliente.Cliente;
import cliente.ModoTrabajo;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.CardLayout;
import java.awt.Color;

import javax.swing.JScrollPane;

import java.awt.SystemColor;
import java.awt.Font;
import javax.swing.JDesktopPane;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.JComboBox;

public class ClienteJFrame extends JFrame {
	
	private static final long serialVersionUID = 1L;

	Cliente cliente;
	
	private JTextField jtext_IP;
	private JTextField jtext_puerto;
	private JTextField jtext_usuario;
	private JPasswordField jpassword;
	private JPanel jpanel_trabajo;
	private JPanel jpanel_logeo;
	private JTextPane textPaneConsola;
	private JLabel lblUsuario_1;
	private JLabel lblPuntos;
	private JLabel lblServidorPrimario;
	private JLabel lblDireccionIp;
	private JLabel lblPuerto;
	private JLabel lblServidorBackup; 
	private JLabel lblDireccionIp_1;
	private JLabel lblPuerto_1;
	private JLabel lblModoDeTrabajo;
	private JComboBox<String> modoTrabajoComboBox;
	
	private JTextPane textPane;
	private JLabel lblModo;

	public ClienteJFrame(Cliente c) {
		this.cliente=c;
		this.setTitle("Cliente BitCoin Mining");
		this.setVisible(true);
		this.setResizable(false);
		this.setBounds(250, 250, 580, 330);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//TODO cuando cierre la ventana q llame al metodo cerrar de esta clase
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		getContentPane().add(panel);
		panel.setLayout(new CardLayout(0, 0));
		
//---------------------------------------------------VISTA 1------------------------------------------------------------
		
		jpanel_logeo = new JPanel();
		jpanel_logeo.setBackground(new java.awt.Color(23, 26, 33));
		panel.add(jpanel_logeo, "name_26541168277457");
		jpanel_logeo.setLayout(null);
		
		JLabel lblIpServidor = new JLabel("IP Servidor: ");
		lblIpServidor.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblIpServidor.setForeground(new java.awt.Color(189, 187, 185));
		lblIpServidor.setBounds(25, 45, 114, 15);
		jpanel_logeo.add(lblIpServidor);
		
		jtext_IP = new JTextField();
		jtext_IP.setBounds(129, 42, 150, 19);
		jpanel_logeo.add(jtext_IP);
		jtext_IP.setColumns(10);
		//PONGO EL CAMPO PARA AHORRAR TIEMPO DE PRUEBA
		jtext_IP.setText("127.0.0.1");
		
		JLabel lblPuertoServidor = new JLabel("Puerto Servidor: ");
		lblPuertoServidor.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblPuertoServidor.setForeground(new java.awt.Color(189, 187, 185));
		lblPuertoServidor.setBounds(305, 45, 142, 15);
		jpanel_logeo.add(lblPuertoServidor);
		
		jtext_puerto = new JTextField();
		jtext_puerto.setBounds(445, 42, 86, 19);
		jpanel_logeo.add(jtext_puerto);
		jtext_puerto.setColumns(10);
		//PONGO EL CAMPO PARA AHORRAR TIEMPO DE PRUEBA
		jtext_puerto.setText("5555");
		
		JLabel lblUsuario = new JLabel("Usuario: ");
		lblUsuario.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblUsuario.setForeground(new java.awt.Color(189, 187, 185));
		lblUsuario.setBounds(25, 133, 114, 15);
		jpanel_logeo.add(lblUsuario);
		
		JLabel lblPassword = new JLabel("Password: ");
		lblPassword.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblPassword.setForeground(new java.awt.Color(189, 187, 185));
		lblPassword.setBounds(25, 176, 114, 15);
		jpanel_logeo.add(lblPassword);
		
		jtext_usuario = new JTextField();
		jtext_usuario.setBounds(129, 130, 150, 20);
		jpanel_logeo.add(jtext_usuario);
		jtext_usuario.setColumns(20);
		//PONGO EL CAMPO PARA AHORRAR TIEMPO DE PRUEBA
		jtext_usuario.setText("usuario");
		
		jpassword = new JPasswordField();
		jpassword.setBounds(129, 173, 150, 20);
		jpanel_logeo.add(jpassword);
		//PONGO EL CAMPO PARA AHORRAR TIEMPO DE PRUEBA
		jpassword.setText("usuario123");
		
		JButton btnConectarse = new JButton("Conectarse");
		btnConectarse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				conectarse();
			}
		});
		btnConectarse.setBounds(148, 228, 117, 25);
		jpanel_logeo.add(btnConectarse);
		
		JLabel lblAcaVaEl = new JLabel("");
		try{
			lblAcaVaEl.setIcon(new ImageIcon(ClienteJFrame.class.getResource("/cliente/vista/icono.jpg")));
		}catch(Exception e){
			lblAcaVaEl.setBackground(Color.WHITE);
			System.out.println("No se encontro la imagen ");
		}
		lblAcaVaEl.setHorizontalAlignment(SwingConstants.CENTER);
		lblAcaVaEl.setBounds(305, 71, 228, 187);
		jpanel_logeo.add(lblAcaVaEl);
		
		lblModoDeTrabajo = new JLabel("Modo de Trabajo");
		lblModoDeTrabajo.setBounds(25, 82, 114, 15);
		lblModoDeTrabajo.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblModoDeTrabajo.setForeground(new java.awt.Color(189, 187, 185));
		jpanel_logeo.add(lblModoDeTrabajo);
		
		modoTrabajoComboBox = new JComboBox<String>();
		modoTrabajoComboBox.setBounds(139, 77, 140, 24);
		modoTrabajoComboBox.setFont(new Font("Tahoma", Font.PLAIN, 12));
		modoTrabajoComboBox.addItem(new String("MonoProcesador"));
		modoTrabajoComboBox.addItem(new String("MultiProcesador"));
		
		jpanel_logeo.add(modoTrabajoComboBox);
		
		//------------------------------------------------------------VISTA 2-----------------------------------------------------------		
		
		jpanel_trabajo = new JPanel();
		panel.add(jpanel_trabajo, "name_27913023106346");
		jpanel_trabajo.setLayout(null);
		jpanel_trabajo.setBackground(new java.awt.Color(23, 26, 33));
		
		JButton btnSalir = new JButton("");
		try{
			btnSalir.setIcon(new ImageIcon(ClienteJFrame.class.getResource("/cliente/vista/logout.jpg")));
		}catch(Exception e){
			btnSalir.setBackground(Color.WHITE);
			System.out.println("No se encontro la imagen ");
		}
		btnSalir.setBounds(534, 11, 30, 30);
		jpanel_trabajo.add(btnSalir);
		btnSalir.setBackground(new java.awt.Color(23, 26, 33));
		btnSalir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cerrar();
			}
		});
		
		lblUsuario_1 = new JLabel("");
		lblUsuario_1.setBounds(21, 15, 283, 14);
		jpanel_trabajo.add(lblUsuario_1);
		lblUsuario_1.setForeground(new java.awt.Color(189, 187, 185));
		
		lblPuntos = new JLabel("");
		lblPuntos.setBounds(354, 15, 161, 14);
		jpanel_trabajo.add(lblPuntos);
		lblPuntos.setForeground(new java.awt.Color(189, 187, 185));
		
		JDesktopPane desktopPane = new JDesktopPane();
		desktopPane.setBackground(SystemColor.activeCaptionBorder);
		desktopPane.setBounds(10, 49, 191, 221);
		jpanel_trabajo.add(desktopPane);
		
		lblModo = new JLabel("Modo: ");
		lblModo.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblModo.setBounds(0, 0, 191, 15);
		desktopPane.add(lblModo);
		
		JLabel lblConexionA = new JLabel("Conexi\u00F3n a: ");
		lblConexionA.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblConexionA.setBounds(0, 18, 171, 14);
		desktopPane.add(lblConexionA);
		
		lblServidorPrimario = new JLabel("");
		lblServidorPrimario.setBounds(10, 44, 171, 14);
		desktopPane.add(lblServidorPrimario);
		
		lblDireccionIp = new JLabel("");
		lblDireccionIp.setBounds(10, 69, 171, 14);
		desktopPane.add(lblDireccionIp);
		
		lblPuerto = new JLabel("");
		lblPuerto.setBounds(10, 94, 171, 14);
		desktopPane.add(lblPuerto);
		
		lblServidorBackup = new JLabel("");
		lblServidorBackup.setBounds(10, 136, 171, 14);
		desktopPane.add(lblServidorBackup);
		
		lblDireccionIp_1 = new JLabel("");
		lblDireccionIp_1.setBounds(10, 161, 171, 14);
		desktopPane.add(lblDireccionIp_1);
		
		lblPuerto_1 = new JLabel("");
		lblPuerto_1.setBounds(10, 186, 171, 14);
		desktopPane.add(lblPuerto_1);
		
		textPane = new JTextPane();
		textPane.setBounds(211, 49, 353, 221);
		//jpanel_trabajo.add(textPane);
		
		JScrollPane scrollBar = new JScrollPane(textPane);
		scrollBar.setBounds(211, 49, 353, 221);
		jpanel_trabajo.add(scrollBar);
		textPaneConsola = new JTextPane();
		getContentPane().add(textPaneConsola, BorderLayout.SOUTH);
		textPaneConsola.setBackground(new java.awt.Color(209, 218, 225));
		this.revalidate();
		this.repaint();
		
	}
	
	private void conectarse(){
		try{
			String ip = this.jtext_IP.getText().trim();
			Integer puerto = Integer.parseInt( this.jtext_puerto.getText().trim() );
			String usuario = this.jtext_usuario.getText().trim();
			String password = this.jpassword.getText();
			if(ip.equals("")){
				throw new java.lang.NumberFormatException();
			}
			if(usuario.equals("")){
				throw new java.lang.NumberFormatException();
			}
			if(password.equals("")){
				throw new java.lang.NumberFormatException();
			}
			int modoTrabajo = this.modoTrabajoComboBox.getSelectedIndex();
			switch(modoTrabajo){
				case 0:
					this.cliente.setModoTrabajo(ModoTrabajo.monoThread);
					lblModo.setText("Modo: MonoProcesador");
					break;
				case 1:
					this.cliente.setModoTrabajo(ModoTrabajo.multiThread);
					lblModo.setText("Modo: MultiProcesador");
					break;
				default:
					this.cliente.setModoTrabajo(ModoTrabajo.video);
					lblModo.setText("Modo: PlacaVideo");
					break;					
			}
			this.cliente.conectarseServidorPrimario(ip, puerto, usuario, password);
		}catch(java.lang.NumberFormatException e){
			//error al leer de los campos
			this.mostrarMsjPorConsola("alguno de los campos contiene un error");
		}
		
		
	}

	public void desconectar (String msj) {
		if (msj != null) {
			JOptionPane.showMessageDialog(null, msj, "Desconexion", JOptionPane.ERROR_MESSAGE);
		}
		this.mostrarPanelLogueo();
	}
	
	public void mostrarPanelLogeo(){
		jpanel_trabajo.setVisible(false);
		jpanel_logeo.setVisible(true);
	}
	
	public void mostrarMsjPorConsola(String msj){
		this.textPaneConsola.setText(msj);
		//this.textPaneConsola.setText(msj + textPaneConsola.getText());
	}
	
	public void crearPanelTrabajo() {
		//LUEGO DEL LOGUEO AUTOMATICAMENTE SE ABRE LA SEGUNDA PANTALLA
		String usuario = this.jtext_usuario.getText();
		jpanel_logeo.setVisible(false);
		jpanel_trabajo.setVisible(true);
		
		//MUESTRO EL USUARIO Y LOS PUNTOS PERO LOS PUNTOS DEBERIAN ACTUALIZARSE
		//CADA VEZ QUE SE FINALIZA UN BLOQUE Y EL SERVIDOR ME ENVIE LOS PUNTOS QUE GANE
		lblUsuario_1.setText("Usuario: " + usuario);
		lblPuntos.setText("Puntos: " + cliente.getPuntos());
	}

	public void mostrarPanelLogueo() {
		jpanel_trabajo.setVisible(false);
		jpanel_logeo.setVisible(true);
	}
	
	//ESCRIBIR RESULTADO EN REALIDAD PUEDE ESCRIBIR CUALQUIER COSA AUNQUE NO SEA UN RESULTADO
	public void escribirResultado(String resultado) {
		textPane.setText(textPane.getText() +  resultado + "\n");
	}
	
	public void actualizarPuntos(Integer puntos) {
		lblPuntos.setText("Puntos: " + puntos);
	}
	
	public void actualizarInfoServidor(String ipPrimario, Integer puertoPrimario,String ipSecundario, Integer puertoSecundario) {
		
		//label primario
		if(ipPrimario==null || puertoPrimario==null){
			lblServidorPrimario.setText("Servidor Primario: -");
			lblDireccionIp.setText("Direccion IP: - ");
			lblPuerto.setText("Puerto: - ");
		}else{
			lblServidorPrimario.setText("Servidor Primario: Conectado");
			lblDireccionIp.setText("Direccion IP: " + ipPrimario);
			lblPuerto.setText("Puerto: " + puertoPrimario);
		}
		
		//labels secundario
		if(ipSecundario==null || puertoSecundario==null){
			lblServidorBackup.setText("Servidor Backup: - ");
			lblDireccionIp_1.setText("Direccion IP: - ");
			lblPuerto_1.setText("Puerto: - ");
		}else{
			lblServidorBackup.setText("Servidor Backup: Disponible ");
			lblDireccionIp_1.setText("Direccion IP: "+ipSecundario);
			lblPuerto_1.setText("Puerto: - "+puertoSecundario);
		}
		
		
	}

	private void cerrar() {
		// TODO Auto-generated method stub
		this.cliente.desconectarse();
		this.cliente=null;
		this.dispose();
		System.exit(0);
	}
}
