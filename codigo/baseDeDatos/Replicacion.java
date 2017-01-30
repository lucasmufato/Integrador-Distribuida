package baseDeDatos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Replicacion implements Serializable {
	private static final long serialVersionUID = 1L;

	private ArrayList <HashMap> usuarios;
	private ArrayList <HashMap> bloques;
	private ArrayList <HashMap> tareas;
	private ArrayList <HashMap> procesamientoTareas;
	private ArrayList <HashMap> estadoBloques;
	private ArrayList <HashMap> estadoTareas;
	
	public Replicacion () {
		this.usuarios = new ArrayList <HashMap> ();
		this.bloques = new ArrayList <HashMap> ();
		this.tareas = new ArrayList <HashMap> ();
		this.procesamientoTareas = new ArrayList <HashMap> ();
		this.estadoBloques = new ArrayList <HashMap> ();
		this.estadoTareas = new ArrayList <HashMap> ();
	}

	public boolean cargar (Connection db) {
		try {
			this.cargarUsuarios (db);
			this.cargarBloques (db);
			this.cargarTareas (db);
			this.cargarProcesamientoTareas (db);
			this.cargarEstadoBloques (db);
			this.cargarEstadoTareas (db);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void cargarUsuarios (Connection db) throws Exception {
		String query = "SELECT id_usuario, nombre, contrasenia, puntos FROM usuario";
		ResultSet resultado = db.createStatement ().executeQuery (query);
		while (resultado.next()) {
			
		}
		
	}

	public void cargarBloques (Connection db) throws Exception {
		String query = "SELECT id_bloque, estado FROM bloque";
		ResultSet resultado = db.createStatement ().executeQuery (query);
		while (resultado.next()) {
			HashMap <String, Object> bloque = new HashMap <String, Object> ();
			bloque.put ("id_bloque", new Integer (resultado.getInt (1)));
			bloque.put ("estado", new Integer (resultado.getInt (2)));
			this.bloques.add (bloque);
		}
	}

	public void cargarTareas (Connection db) throws Exception {
		String query = "SELECT id_tarea, bloque, header_bytes, estado FROM tarea";
		ResultSet resultado = db.createStatement ().executeQuery (query);
		while (resultado.next()) {
			HashMap <String, Object> tarea = new HashMap <String, Object> ();
			tarea.put ("id_tarea", new Integer (resultado.getInt(1)));
			tarea.put ("bloque", new Integer (resultado.getInt(2)));
			tarea.put ("header_bytes", resultado.getBytes(3));
			tarea.put ("estado", new Integer (resultado.getInt(4)));
			this.tareas.add (tarea);
		}
	}

	public void cargarProcesamientoTareas (Connection db) throws Exception {
		String query = "SELECT id_procesamiento_tarea, tarea, usuario, estado, parcial, resultado from procesamiento_tarea";
		ResultSet resultado = db.createStatement ().executeQuery (query);
		while (resultado.next()) {
			HashMap <String, Object> procesamiento = new HashMap <String, Object> ();
			procesamiento.put ("id_procesamiento_tarea", new Integer (resultado.getInt (1)));
			procesamiento.put ("tarea", new Integer (resultado.getInt (2)));
			procesamiento.put ("usuario", new Integer (resultado.getInt (3)));
			procesamiento.put ("estado", new Integer (resultado.getInt (4)));
			procesamiento.put ("parcial", resultado.getBytes (5));
			procesamiento.put ("resultado", resultado.getBytes (6));
			this.procesamientoTareas.add (procesamiento);
		}
	}

	public void cargarEstadoBloques (Connection db) throws Exception {
		String query = "SELECT id_estado_bloque, estado from estado_bloque";
		ResultSet resultado = db.createStatement ().executeQuery (query);
		while (resultado.next()) {
			HashMap <String, Object> estadoBloque = new HashMap <String, Object> ();
			estadoBloque.put ("id_estado_bloque", new Integer (resultado.getInt(1)));
			estadoBloque.put ("estado", resultado.getString(2));
			this.estadoBloques.add (estadoBloque);
		}
	}

	public void cargarEstadoTareas (Connection db) throws Exception {
		String query = "SELECT id_estado_tarea, estado from estado_tarea";
		ResultSet resultado = db.createStatement ().executeQuery (query);
		while (resultado.next()) {
			HashMap <String, Object> estadoTarea = new HashMap <String, Object> ();
			estadoTarea.put ("id_estado_tarea", new Integer (resultado.getInt(1)));
			estadoTarea.put ("estado", resultado.getString(2));
			this.estadoTareas.add (estadoTarea);
		}
	}

	public boolean insertarTodo (Connection con) {
		try {
			this.insertarEstadoBloques (con);
			this.insertarEstadoTareas (con);
			this.insertarUsuarios (con);
			this.insertarBloques (con);
			this.insertarTareas (con);
			this.insertarProcesamientoTareas (con);
		} catch (Exception e) {
			e.printStackTrace ();
			return false;
		}
		return true;
	}

	public void insertarEstadoBloques (Connection con) throws Exception {
		String query = "INSERT INTO estado_bloque (id_estado_bloque, estado) VALUES (?, ?)";
		PreparedStatement stm = con.prepareStatement (query);
		for (HashMap <String, Object> estadoBloque: this.estadoBloques) {
			stm.setInt (1, (Integer) estadoBloque.get ("id_estado_bloque"));
			stm.setString (2, (String) estadoBloque.get ("estado"));
			stm.execute ();
		}
	}

	public void insertarEstadoTareas (Connection con) throws Exception {
		String query = "INSERT INTO estado_tarea (id_estado_tarea, estado) VALUES (?, ?)";
		PreparedStatement stm = con.prepareStatement (query);
		for (HashMap <String, Object> estadoTarea: this.estadoTareas) {
			stm.setInt (1, (Integer) estadoTarea.get ("id_estado_tarea"));
			stm.setString (2, (String) estadoTarea.get ("estado"));
			stm.execute ();
		}
	}

	public void insertarUsuarios (Connection con) throws Exception {
		String query = "INSERT INTO usuario (id_usuario, nombre, contrasenia, puntos) VALUES (?, ?, ?, ?)";
		PreparedStatement stm = con.prepareStatement (query);
		for (HashMap <String, Object> usuario: this.usuarios) {
			stm.setInt (1, (Integer) usuario.get ("id_usuario"));
			stm.setString (2, (String) usuario.get ("nombre"));
			stm.setString (3, (String) usuario.get ("contrasenia"));
			stm.setInt (4, (Integer) usuario.get ("puntos"));
			stm.execute ();
		}
	}

	public void insertarBloques (Connection con) throws Exception {
		String query = "INSERT INTO bloque (id_bloque, estado) VALUES (?, ?)";
		PreparedStatement stm = con.prepareStatement (query);
		for (HashMap <String, Object> bloque: this.bloques) {
			stm.setInt (1, (Integer) bloque.get ("id_bloque"));
			stm.setInt (2, (Integer) bloque.get ("estado"));
			stm.execute ();
		}
	}

	public void insertarTareas (Connection con) throws Exception {
		String query = "INSERT INTO tarea (id_tarea, bloque, header_bytes, estado) VALUES (?, ?, ?, ?)";
		PreparedStatement stm = con.prepareStatement (query);
		for (HashMap <String, Object> tarea: this.tareas) {
			stm.setInt (1, (Integer) tarea.get("id_tarea"));
			stm.setInt (2, (Integer) tarea.get("bloque"));
			stm.setBytes (3, (byte[]) tarea.get("header_bytes"));
			stm.setInt (4, (Integer) tarea.get("estado"));
			stm.execute ();
		}
	}

	public void insertarProcesamientoTareas (Connection con) throws Exception {
		String query = "INSERT INTO tarea (id_procesamiento_tarea, tarea, usuario, estado, parcial, resultado) VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement stm = con.prepareStatement (query);
		for (HashMap <String, Object> procesamiento: this.procesamientoTareas) {
			stm.setInt (1, (Integer) procesamiento.get("id_procesamiento_tarea"));
			stm.setInt (2, (Integer) procesamiento.get("tarea"));
			stm.setInt (3, (Integer) procesamiento.get("usuario"));
			stm.setInt (4, (Integer) procesamiento.get("estado"));
			stm.setBytes (5, (byte[]) procesamiento.get("parcial"));
			stm.setBytes (6, (byte[]) procesamiento.get("resultado"));
			stm.execute ();
		}
	}

}
