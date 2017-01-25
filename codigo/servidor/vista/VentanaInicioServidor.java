package servidor.vista;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;

import java.awt.BorderLayout;
import java.awt.Color;

public class VentanaInicioServidor extends JFrame {

	//clase que sirve para mostrar lo que hace la clase Servidor.
	
	private static final long serialVersionUID = 1L;
	private JTextArea label;
	private JButton boton;
	
	public VentanaInicioServidor(){
		
		this.label= new JTextArea("Inicializando...");
		this.getContentPane().add(label, BorderLayout.CENTER);
		this.label.setBackground(Color.DARK_GRAY);
		this.label.setForeground(Color.white);
		
		this.boton = new JButton("Cancelar");
		this.getContentPane().add(boton, BorderLayout.SOUTH);
		this.boton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//cierre bastante feo
				System.exit(0);
			}
		});
		
		this.setTitle("Simulacion de Mineria de Bitcoins");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(450, 200);
		this.setLocation(500, 250);
		this.setResizable(false);
		this.setVisible(true);
	}
	
	public void agregarLine(String linea){
		String newline = System.getProperty("line.separator");
		String texto = this.label.getText();
		texto = texto + newline + linea;
		this.label.setText(texto);
	}
	
	public void cerrarVentana(){
		this.dispose();
	}
	
}
