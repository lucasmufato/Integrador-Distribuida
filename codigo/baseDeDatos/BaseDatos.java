package baseDeDatos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Map;
import java.util.Observable;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;

import bloquesYTareas.*;

public class BaseDatos extends Observable {

	protected Connection c;
	protected final static String host="localhost";
	protected final static String nombreBD="finaldistribuido";
	protected final static Integer puertoBD=5432;
	protected final static String user="distribuido";
	protected final static String password="sistemas";

	/* Los siguientes objetos son copias en memoria de lo que hay en la base de datos */
	private Map <Integer, Bloque> cacheBloques;
	private Map <Integer, Tarea> cacheTareas;

	public BaseDatos(){
		this.cacheBloques = new HashMap <Integer, Bloque> ();
		this.cacheTareas = new HashMap <Integer, Tarea> ();
		this.conectarse();
	}

	public boolean conectarse(){
		try {
	         Class.forName("org.postgresql.Driver");
	         c = DriverManager.getConnection("jdbc:postgresql://"+host+":"+puertoBD+"/"+nombreBD,user, password);
	         return true;
	      } catch (Exception e) {
	         e.printStackTrace();
	         return false;
	      }
	}

	public Integer autenticar(String usuario, String password){
		//este metodo recibe el nombre de usuario y la password para hacer el logeo
		//si el logeo no funciona (error de user y password) devuelve nulo, sino crea y devuelve un ID_SESION
		if(usuario == null || password == null){
			return null;
		}
		if(usuario.equals("") || password.equals("")){
			return null;
		}
		PreparedStatement query;
		ResultSet rs;
		try {
			/* WARNING: Esto permite inyeccion SQL */
			query = c.prepareStatement("SELECT * FROM USUARIO S WHERE S.NOMBRE = "+ "'" + usuario + "'" +" AND S.CONTRASENIA = "+ "'" + password + "'");
			rs = query.executeQuery();
			if (rs.next() == false) {
				//si la primer respuesta ya es nulo es por que el user y password son incorrectos
				return null;
			}else{
				//tendria q ver como crear el ID_SESION
				//por ahora devuelvo un nro y listo
				return 10;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean desconectarse(){
		try {
			c.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public synchronized Tarea getTarea(Integer idUsuario){
		Tarea tarea = null;
		ResultSet res_tarea = null;
		PreparedStatement stm_tarea;

		try {
			/* Primero buscamos tareas detenidas incompletas */
			res_tarea = this.selectTareaDetenida();

			/* Despues, tareas no iniciadas dentro de un bloque iniciado */
			if (res_tarea == null) {
				res_tarea = this.selectTareaNoIniciadaBloqueIniciado();
			}

			/* Finalmente, tareas no iniciadas dentro de un bloque no iniciado, en este caso, marcamos el bloque como 'en proceso' */
			if (res_tarea == null) {
				res_tarea = this.selectTareaNoIniciadaBloqueNoIniciado();
				if(res_tarea != null) {
					/* En este caso, actualizamos el estado del bloque */
					this.setEstadoBloque(res_tarea.getInt("bloque"), "en proceso");
				} else {
					/* Si despues de buscar en los tres casos, no se encontro una tarea significa que todas las tareas estan completados */
					return null;
				}
			}

			this.asignarTareaUsuario(res_tarea.getInt("id"), idUsuario);

			tarea = new Tarea (
				this.getBloque (res_tarea.getInt("bloque")),
				res_tarea.getBytes("h_bytes"),
				res_tarea.getBytes("parcial")
			);
			tarea.setId(res_tarea.getInt("id"));
			tarea.setEstado(EstadoTarea.enProceso);

			//CUANDO SE HAYA ASIGNADO CORRECTAMENTE UN TAREA AL USUARIO SE AVISA AL OBSERVADOR DEL CAMBIO
			//MARCO QUE CAMBIO EL OBJETO
	        setChanged();
	        //NOTIFICO EL CAMBIO
	        notifyObservers(tarea);
	        
			/* TODO: Esto deberia establecerlo el servidor primario, no la base de datos */
			tarea.SetLimite(3, (byte) 0x80);

		} catch (Exception e){
			System.err.println ("Error al recuperar tarea de la base de datos:\n" + e.getMessage());
			e.printStackTrace();
		}
			return tarea;

	}

	public synchronized Bloque getBloque(int id_bloque) {
		/* Busca en la cache, o en la base de datos el bloque con el id especificado */
		Bloque bloque = null;
		bloque = this.cacheBloques.get(id_bloque);
		if (bloque == null)	{
			try {
				/* Select bloque */
				PreparedStatement stm_bloque = c.prepareStatement (
					"SELECT " +
						"estado_bloque.estado AS estado" +
					"FROM " +
						"bloque " +
						"JOIN estado_bloque ON bloque.estado = estado_bloque " +
					"WHERE " +
						"bloque.id_bloque = ?"
				);
				stm_bloque.setInt(1, id_bloque);
				stm_bloque.execute();
				ResultSet res_bloque = stm_bloque.getResultSet();
		
				if (res_bloque.next()) {
					bloque = new Bloque(id_bloque);
					String str_estado = res_bloque.getString ("estado");
					switch (str_estado) {
						case "pendiente":
							bloque.setEstado(EstadoBloque.pendiente);
							break;
						case "en proceso":
							bloque.setEstado(EstadoBloque.enProceso);
							break;
						case "completado":
							bloque.setEstado(EstadoBloque.completado);
							break;
					}
					bloque.setTareas(this.getTareasPorBloque(id_bloque));
					this.cacheBloques.put(id_bloque, bloque);
				}
			} catch (Exception e) {
				System.err.println ("Error al recuperar bloque: " + e.getMessage());
				e.printStackTrace();
			}
		}
		return bloque;
	}

	public synchronized ArrayList<Tarea> getTareasPorBloque(int id_bloque) {
		return null;
	}

	public synchronized boolean setParcial(Tarea tarea, Integer idUsuario){
		return false;
	}

	public synchronized boolean setResultado(Tarea tarea, Integer idUsuario){
		//SETEA EL RESULTADO FINAL EN LA BD
		//LLAMA AL METODO ESTAFINALIZADOBLOQUE(), SI ESTE DEVUELVE TRUE CAMBIA ESTADO DEL BLOQUE
		return false;
	}	

	public synchronized boolean detenerTarea(Tarea tarea, Integer idUsuario){
		return false;
	}

	public synchronized boolean generarBloques (int numBloques, int numTareasPorBloque, int numBytesPorTarea) {
		try {
			PreparedStatement stm_bloque = c.prepareStatement ("INSERT INTO bloque (estado) SELECT id_estado_bloque FROM estado_bloque WHERE estado = 'pendiente'",Statement.RETURN_GENERATED_KEYS);
			PreparedStatement stm_tarea = c.prepareStatement ("INSERT INTO tarea (bloque, header_bytes, estado) SELECT ? as bloque, ? as header_bytes, id_estado_bloque as estado FROM estado_bloque where estado = 'pendiente'",Statement.RETURN_GENERATED_KEYS);
			ResultSet generatedKeys;
			Random random = new Random ();
			int id_bloque;
			byte[] b_tarea;

			/* Genera tareas con bytes aleatorios y las inserta en la base de datos */
			for (int i1=0; i1 < numBloques; i1++) {
				stm_bloque.executeUpdate(); // Asumimos que va a funcionar 
				generatedKeys = stm_bloque.getGeneratedKeys();
				generatedKeys.next();
				id_bloque = generatedKeys.getInt("id_bloque");
				for (int i2=0; i2 < numTareasPorBloque; i2++) {
					b_tarea = new byte[numBytesPorTarea];
					random.nextBytes(b_tarea);
					stm_tarea.clearParameters();
					stm_tarea.setInt(1, id_bloque);
					stm_tarea.setBytes(2, b_tarea);
					stm_tarea.executeUpdate(); // Asumimos que va a funcionar
				}
			}
			return true;
		} catch (Exception e) {
			System.err.println ("Error al generar bloques en la base de datos: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}


	private synchronized boolean estaFinalizadoBloque(){
		//CHEQUEA QUE TODAS LAS TAREAS DEL BLOQUE ESTEN COMPLETAS
		return false;
	}

	private synchronized ResultSet selectTareaDetenida() {
		ResultSet res_tarea = null;

		try {
			PreparedStatement stm_tarea = c.prepareStatement(
				"SELECT " +
					"tarea.id_tarea AS id, " +
					"tarea.bloque as bloque, " +
					"tarea.header_bytes as h_bytes, " +
					"estado_tarea.estado as estado, " +
					"procesamiento_tarea.parcial as parcial " +
				"FROM tarea " +
					"JOIN estado_tarea ON tarea.estado = estado_tarea.id_estado_tarea " +
					"JOIN procesamiento_tarea ON procesamiento_tarea.tarea = tarea.id_tarea " +
					"WHERE estado_tarea.estado = 'detenida' "+
				"ORDER BY parcial DESC " +
				"LIMIT 1"
			);

			stm_tarea.execute();
			res_tarea = stm_tarea.getResultSet();
			if (!res_tarea.next()) {
				res_tarea = null;
			}
		} catch (Exception e) {
			System.err.println ("Error al recuperar tarea detenida");
			e.printStackTrace();
		}
		return res_tarea;
	}

	private synchronized ResultSet selectTareaNoIniciadaBloqueIniciado () {
		ResultSet res_tarea = null;
		try {
			PreparedStatement stm_tarea = c.prepareStatement(
			"SELECT " +
				"tarea.id_tarea AS id, "+
				"tarea.bloque as bloque, "+
				"tarea.header_bytes as h_bytes, "+
				"E'\\\\x00' AS parcial, "+
				"estado_tarea.estado as estado "+
			"FROM tarea " +
				"JOIN estado_tarea ON tarea.estado = estado_tarea.id_estado_tarea " +
				"JOIN bloque ON tarea.bloque = bloque.id_bloque " +
				"JOIN estado_bloque ON bloque.estado = estado_bloque.id_estado_bloque "+
			"WHERE " +
				"estado_bloque.estado = 'en proceso' " +
				"AND estado_tarea.estado = 'pendiente' " +
			"LIMIT 1"
			);

			stm_tarea.execute();
			res_tarea = stm_tarea.getResultSet();

			if (!res_tarea.next()) {
				res_tarea = null;
			}

		} catch (Exception e) {
			System.err.println ("Error al recuperar tarea no iniciada");
			e.printStackTrace();
		}
		return res_tarea;
	}
	
	private synchronized ResultSet selectTareaNoIniciadaBloqueNoIniciado () {
		ResultSet res_tarea = null;
		try {
			PreparedStatement stm_tarea = c.prepareStatement(
				"SELECT " +
					"tarea.id_tarea AS id, "+
					"tarea.bloque as bloque, "+
					"tarea.header_bytes as h_bytes, "+
					"E'\\\\x00' AS parcial, "+
					"estado_tarea.estado as estado "+
				"FROM tarea " +
					"JOIN estado_tarea ON tarea.estado = estado_tarea.id_estado_tarea " +
					"JOIN bloque ON tarea.bloque = bloque.id_bloque " +
					"JOIN estado_bloque ON bloque.estado = estado_bloque.id_estado_bloque "+
				"WHERE " +
					"estado_bloque.estado = 'pendiente' " +
					"AND estado_tarea.estado = 'pendiente' " +
				"LIMIT 1"
			); 

			stm_tarea.execute();
			res_tarea = stm_tarea.getResultSet ();

			if (res_tarea.next()) {
				System.out.println ("DEBUG: Se ha encontrado una tarea no iniciada dentro de un bloque no iniciado");
			}
			
		} catch (Exception e) {
			System.err.println ("Error al recuperar tarea no iniciada");
			e.printStackTrace();
		}
		return res_tarea;
	}

	/* TODO: Reemplazar por metodos de bloque */
	private synchronized boolean setEstadoBloque(int id_bloque, String estado) {
		try {
			PreparedStatement stmt_bloque = c.prepareStatement("UPDATE bloque set estado = (SELECT id_estado_bloque FROM estado_bloque WHERE estado = ?) WHERE bloque.id_bloque = ?");
			stmt_bloque.setString(1, estado);
			stmt_bloque.setInt(2, id_bloque);
			if (stmt_bloque.executeUpdate() > 0){
				System.out.println("DEBUG: se ha cambiado el estado del bloque "+id_bloque+" a 'en proceso'");
				/* TODO: Actualizar en Cache */
			} else {
				System.err.println ("Error al cambiar el estado del bloque "+id_bloque);
				return false;
			}
		} catch (Exception e) {
			System.err.println ("Error al cambiar el estado del bloque "+id_bloque);
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	private synchronized boolean asignarTareaUsuario (int id_tarea, int id_usuario) {
		try {
			/* Insertamos el procesamiento */
			PreparedStatement stm_procesamiento = c.prepareStatement(
			"INSERT " +
				"INTO procesamiento_tarea (tarea, usuario, estado) " +
			"SELECT " +
				"? as tarea, " +
				"? as usuario, " +
				"id_estado_tarea as estado " +
			"FROM estado_tarea " +
				"WHERE estado = 'en proceso'"
			);

			/* Y marcamos la tarea como "en proceso" */
			PreparedStatement stm_update_tarea = c.prepareStatement(
			"UPDATE " +
				"tarea " +
			"SET " +
				"estado=subquery.id_estado_tarea " +
			"FROM ( "+
				"SELECT " +
					"id_estado_tarea " +
				"FROM " +
					"estado_tarea " +
				"WHERE " +
					"estado = 'en proceso' " +
			") AS subquery " +
			"WHERE " +
				"id_tarea = ? "
			);

			stm_procesamiento.setInt(1, id_tarea);
			stm_procesamiento.setInt(2, id_usuario);
			stm_update_tarea.setInt(1, id_tarea);

			if ( (stm_procesamiento.executeUpdate() > 0) && (stm_update_tarea.executeUpdate() > 0)) {
				System.out.println ("DEBUG: Se ha asignado una tarea al usuario");

			} else {
				System.err.println ("Error al asignar una tarea al usuario");
				return false;
			}	
		} catch (Exception e) {
			System.err.println ("Error al asignar una tarea al usuario");
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
}
