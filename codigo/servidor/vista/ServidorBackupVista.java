package servidor.vista;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import servidor.Backup;
import javax.swing.JLabel;
import java.awt.Color;

public class ServidorBackupVista extends JFrame {

	private Backup servidorB;
	
	private JPanel panel; 
	private JPanel jpanel_servidor;
	private JLabel lblIPServidor;
	private JLabel lblPuertoServidor;
	private JTextPane textPaneConsola;
	
	//TAMANIO
	private Integer alto = 500;
	private Integer ancho = 800;
	
	public ServidorBackupVista(Backup backup) {
		this.setMinimumSize(new Dimension(this.ancho,this.alto)); 
		setTitle("Servidor BitCoin Mining (BACKUP)");
		this.servidorB= backup;
		this.setVisible(true);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		panel = new JPanel();
		getContentPane().add(panel);
		panel.setLayout(new CardLayout(0, 0));
		
		jpanel_servidor = new JPanel();
		jpanel_servidor.setBackground(new java.awt.Color(23, 26, 33));
		panel.add(jpanel_servidor, "");
		jpanel_servidor.setLayout(null);
		
		textPaneConsola = new JTextPane();
		textPaneConsola.setBackground(new java.awt.Color(209, 218, 225));
		textPaneConsola.setForeground(Color.DARK_GRAY);
		textPaneConsola.setBounds(0, 452, this.ancho-(800-794), this.alto-(500-20));
		jpanel_servidor.add(textPaneConsola);
		
		lblIPServidor = new JLabel("");
		lblIPServidor.setForeground(new java.awt.Color(189, 187, 185));
		lblIPServidor.setBounds(10, 11, this.ancho-(800-148), this.alto-(500-14));
		jpanel_servidor.add(lblIPServidor);
		
		lblPuertoServidor = new JLabel("");
		lblPuertoServidor.setForeground(new java.awt.Color(189, 187, 185));
		lblPuertoServidor.setBounds(323, 11, this.ancho-(800-139), this.alto-(500-14));
		jpanel_servidor.add(lblPuertoServidor);
		//MUESTRO LA IP Y EL PUERTO
		lblIPServidor.setText("Mi IP: " + this.servidorB.getIP());
		lblPuertoServidor.setText("Puerto: " + String.valueOf(this.servidorB.getPuerto()));
		
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
				servidorB.desconectarse();
				System.exit(0);
			}
		});
		btnDesconectar.setBackground(new java.awt.Color(23, 26, 33));
		btnDesconectar.setBounds(752, 11, 32, 32);
		jpanel_servidor.add(btnDesconectar);
		this.revalidate();
		this.repaint();
	}
}
