package cliente.vista;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JLayeredPane;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.JTextPane;

import cliente.Cliente;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.CardLayout;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import java.awt.SystemColor;
import java.awt.Font;
import javax.swing.JDesktopPane;

public class ClienteJFrame extends JFrame {
	
	Cliente cliente;
	
	private JTextField jtext_IP;
	private JTextField jtext_puerto;
	private JTextField jtext_usuario;
	private JPasswordField jpassword;
	private JPanel jpanel_trabajo;
	private JPanel jpanel_logeo;
	private JTextPane textPaneConsola;
	private JLabel lblUsuario_1;
	private JTextPane textPane;

	public ClienteJFrame(Cliente c) {
		this.cliente=c;
		this.setTitle("Cliente BitCoin Mining");
		this.setVisible(true);
		this.setResizable(false);
		this.setBounds(250, 250, 580, 330);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
		
		
		//------------------------------------------------------------VISTA 2-----------------------------------------------------------		
		
		jpanel_trabajo = new JPanel();
		panel.add(jpanel_trabajo, "name_27913023106346");
		jpanel_trabajo.setLayout(null);
		jpanel_trabajo.setBackground(new java.awt.Color(23, 26, 33));
		
		JButton btnNewButton = new JButton("X");
		btnNewButton.setBounds(525, 11, 39, 23);
		jpanel_trabajo.add(btnNewButton);
		
		lblUsuario_1 = new JLabel("Usuario: ");
		lblUsuario_1.setBounds(10, 15, 283, 14);
		jpanel_trabajo.add(lblUsuario_1);
		lblUsuario_1.setForeground(new java.awt.Color(189, 187, 185));
		
		JLabel lblPuntos = new JLabel("Puntos: ");
		lblPuntos.setBounds(354, 15, 161, 14);
		jpanel_trabajo.add(lblPuntos);
		lblUsuario_1.setForeground(new java.awt.Color(189, 187, 185));
		
		JDesktopPane desktopPane = new JDesktopPane();
		desktopPane.setBackground(SystemColor.activeCaptionBorder);
		desktopPane.setBounds(10, 49, 191, 221);
		jpanel_trabajo.add(desktopPane);
		
		JLabel lblConexinA = new JLabel("Conexi\u00F3n a: ");
		lblConexinA.setBounds(10, 11, 171, 14);
		desktopPane.add(lblConexinA);
		
		JLabel lblServidorPrimario = new JLabel("Servidor Primario: ");
		lblServidorPrimario.setBounds(10, 44, 171, 14);
		desktopPane.add(lblServidorPrimario);
		
		JLabel lblDireccinIp = new JLabel("Direcci\u00F3n IP: ");
		lblDireccinIp.setBounds(10, 69, 171, 14);
		desktopPane.add(lblDireccinIp);
		
		JLabel lblPuerto = new JLabel("Puerto: ");
		lblPuerto.setBounds(10, 94, 171, 14);
		desktopPane.add(lblPuerto);
		
		JLabel lblServidorBackup = new JLabel("Servidor Backup: ");
		lblServidorBackup.setBounds(10, 136, 171, 14);
		desktopPane.add(lblServidorBackup);
		
		JLabel lblDireccinIp_1 = new JLabel("Direcci\u00F3n IP: ");
		lblDireccinIp_1.setBounds(10, 161, 171, 14);
		desktopPane.add(lblDireccinIp_1);
		
		JLabel lblPuerto_1 = new JLabel("Puerto: ");
		lblPuerto_1.setBounds(10, 186, 171, 14);
		desktopPane.add(lblPuerto_1);
		
		JLabel lblV = new JLabel("V");
		lblV.setBounds(102, 44, 46, 14);
		desktopPane.add(lblV);
		
		JLabel lblX = new JLabel("X");
		lblX.setBounds(102, 136, 46, 14);
		desktopPane.add(lblX);
		
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
		String ip = this.jtext_IP.getText().trim();
		Integer puerto = Integer.parseInt( this.jtext_puerto.getText().trim() );
		String usuario = this.jtext_usuario.getText().trim();
		String password = this.jpassword.getText();
		this.cliente.conectarseServidorPrimario(ip, puerto, usuario, password);
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
		
		//MUESTRO EL USUARIO Y LOS PUNTOS
		lblUsuario_1.setText(lblUsuario_1.getText() + usuario);
	}
	
	public void escribirResultado(String resultado) {
		textPane.setText(textPane.getText() +  resultado + "\n");
	}
}
