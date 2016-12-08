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

public class ClienteJFrame extends JFrame {
	
	Cliente cliente;
	
	private JTextField jtext_IP;
	private JTextField Jtext_puerto;
	private JTextField jtext_usuario;
	private JPasswordField jpassword;
	private JPanel jpanel_trabajo;
	private JPanel jpanel_logeo;
	private JTextPane textPaneConsola;
	
	public ClienteJFrame(Cliente c) {
		this.cliente=c;
		this.setTitle("Cliente BitCoin Mining");
		this.setVisible(true);
		this.setBounds(250, 250, 500, 250);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		getContentPane().add(panel);
		panel.setLayout(new CardLayout(0, 0));
		
		jpanel_logeo = new JPanel();
		panel.add(jpanel_logeo, "name_26541168277457");
		jpanel_logeo.setLayout(null);
		
		JLabel lblIpServidor = new JLabel("IP servidor");
		lblIpServidor.setBounds(12, 22, 75, 15);
		jpanel_logeo.add(lblIpServidor);
		
		jtext_IP = new JTextField();
		jtext_IP.setBounds(105, 20, 114, 19);
		jpanel_logeo.add(jtext_IP);
		jtext_IP.setColumns(10);
		
		JLabel lblPuertoServidor = new JLabel("Puerto servidor");
		lblPuertoServidor.setBounds(237, 22, 114, 15);
		jpanel_logeo.add(lblPuertoServidor);
		
		Jtext_puerto = new JTextField();
		Jtext_puerto.setBounds(358, 20, 54, 19);
		jpanel_logeo.add(Jtext_puerto);
		Jtext_puerto.setColumns(10);
		
		JLabel lblUsuario = new JLabel("Usuario");
		lblUsuario.setBounds(61, 80, 114, 15);
		jpanel_logeo.add(lblUsuario);
		
		JLabel lblContrasea = new JLabel("Contraseña");
		lblContrasea.setBounds(61, 126, 114, 15);
		jpanel_logeo.add(lblContrasea);
		
		jtext_usuario = new JTextField();
		jtext_usuario.setBounds(193, 78, 158, 19);
		jpanel_logeo.add(jtext_usuario);
		jtext_usuario.setColumns(10);
		
		jpassword = new JPasswordField();
		jpassword.setBounds(193, 124, 158, 19);
		jpanel_logeo.add(jpassword);
		
		JButton btnConectarse = new JButton("Conectarse");
		btnConectarse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				conectarse();
			}
		});
		btnConectarse.setBounds(193, 155, 117, 25);
		jpanel_logeo.add(btnConectarse);
		
		jpanel_trabajo = new JPanel();
		panel.add(jpanel_trabajo, "name_27913023106346");
		jpanel_trabajo.setLayout(null);
		
		
		JLabel lblPantalla = new JLabel("pantalla 2");
		lblPantalla.setBounds(160, 10, 71, 15);
		jpanel_trabajo.add(lblPantalla);
		
		JButton btnVolverA = new JButton("volver a 1");
		btnVolverA.setBounds(385, 143, 101, 25);
		jpanel_trabajo.add(btnVolverA);
		//scrollBar.add(textPaneConsola);
		
		
		btnVolverA.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		
		textPaneConsola = new JTextPane();
		getContentPane().add(textPaneConsola, BorderLayout.SOUTH);
		this.revalidate();
		this.repaint();
		
	}
	
	private void conectarse(){
		String ip = this.jtext_IP.getText().trim();
		Integer puerto = Integer.parseInt( this.Jtext_puerto.getText().trim() );
		String usuario = this.jtext_usuario.getText().trim();
		String password = this.jpassword.getText();
		this.cliente.conectarseServidorPrimario(ip,puerto,usuario,password);
	}
}
