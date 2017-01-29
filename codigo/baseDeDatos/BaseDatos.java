package baseDeDatos;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Observable;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;

import bloquesYTareas.*;

public class BaseDatos {

	private static BaseDatos instance = null; //Esto es para el singleton
	protected Connection c;
	protected final static String host="localhost";
	protected final static String nombreBD="finaldistribuido";
	protected final static Integer puertoBD=5432;
	protected final static String user="distribuido";
	protected final static String password="sistemas";
	protected static Integer contadorSesiones;	//para que vaya devolviendo numero consecutivos de sesion

	/* Los siguientes objetos son copias en memoria de lo que hay en la base de datos */
	private Map <Integer, Bloque> cacheBloques;
	private Map <Integer, Tarea> cacheTareas;

	private BaseDatos(){
		this.cacheBloques = new HashMap <Integer, Bloque> ();
		this.cacheTareas = new HashMap <Integer, Tarea> ();
		this.conectarse();
		contadorSesiones=0;
	}

	public static BaseDatos getInstance () {
		if(BaseDatos.instance == null) {
			BaseDatos.instance = new BaseDatos();
		}
		return BaseDatos.instance;
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
				contadorSesiones++;
				return contadorSesiones;
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

	public synchronized Tarea getTareaById(int id_tarea) {
		/* Deberia llamarse solo getTarea, pero ya hay un metodo que se llama asi y hace otra cosa */
		Tarea tarea;
		tarea = this.cacheTareas.get(id_tarea);
		if (tarea == null) {
		
			ResultSet res_tarea = null;
			try {
				PreparedStatement stm_tarea = c.prepareStatement(
				"SELECT " +
					"tarea.id_tarea AS id, "+
					"tarea.bloque as bloque, "+
					"tarea.header_bytes as h_bytes, "+
					"estado_tarea.estado as estado "+
				"FROM tarea " +
					"JOIN estado_tarea ON tarea.estado = estado_tarea.id_estado_tarea " +
					"JOIN bloque ON tarea.bloque = bloque.id_bloque " +
					"JOIN estado_bloque ON bloque.estado = estado_bloque.id_estado_bloque "+
				"WHERE " +
					"tarea.id_tarea = ? " +
				"ORDER BY " +
					"id_tarea ASC"
				); 
				stm_tarea.setInt(1, id_tarea);
				stm_tarea.execute();
				res_tarea = stm_tarea.getResultSet ();


				if (res_tarea.next()) {
					byte[] parcial = this.getParcial(id_tarea);
					tarea = new Tarea (
						this.getBloque (res_tarea.getInt("bloque")),
						res_tarea.getBytes("h_bytes"),
						parcial
					);
					tarea.setId (id_tarea);

				String str_estado = res_tarea.getString("estado");
				switch (str_estado) {
						case "pendiente":
							tarea.setEstado(EstadoTarea.pendiente);
							break;
						case "en proceso":
							tarea.setEstado(EstadoTarea.enProceso);
							break;
						case "detenida":
							tarea.setEstado(EstadoTarea.detenida);
							break;
						case "completada":
							tarea.setEstado(EstadoTarea.completada);
							break;
					}
				
					this.cacheTareas.put(id_tarea, tarea);
				}
				
			} catch (Exception e) {
				System.err.println ("Error al recuperar tarea no iniciada");
				e.printStackTrace();
			}
		}
		return tarea;
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

			int id_tarea = res_tarea.getInt("id");
			this.asignarTareaUsuario(id_tarea, idUsuario);
			tarea = this.getTareaById(id_tarea);
			tarea.setEstado(EstadoTarea.enProceso);
	        
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
						"estado_bloque.estado AS estado " +
					"FROM " +
						"bloque " +
						"JOIN estado_bloque ON bloque.estado = estado_bloque.id_estado_bloque " +
					"WHERE " +
						"bloque.id_bloque = ? "
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
					this.cacheBloques.put(id_bloque, bloque);
					bloque.setTareas(this.getTareasPorBloque(id_bloque));
					/* Nota: esto va  a producir que se carguen en memoria todas las tareas del bloque */
				}
			} catch (Exception e) {
				System.err.println ("Error al recuperar bloque: " + e.getMessage());
				e.printStackTrace();
			}
		}
		return bloque;
	}

	public synchronized ArrayList<Bloque> getBloquesNoCompletados() {
		ArrayList<Bloque> lista_bloques = null;
		try {
			PreparedStatement stm = c.prepareStatement (
			"SELECT "+
				"bloque.id_bloque AS id_bloque " +
			"FROM "  +
				"bloque " +
				"JOIN estado_bloque ON bloque.estado = estado_bloque.id_estado_bloque " +
			"WHERE " +
				"estado_bloque.estado <> 'completado' " +
			"ORDER BY bloque.id_bloque ASC "
			);
			stm.execute();
			ResultSet res = stm.getResultSet();
			lista_bloques = new ArrayList <Bloque> ();
			while (res.next()) {
				lista_bloques.add (this.getBloque (res.getInt("id_bloque")));
			}

		} catch (Exception e) {
				System.err.println ("Error al recuperar tareas de bloque: " + e.getMessage());
				e.printStackTrace();
				lista_bloques = null;
		}
		
		return lista_bloques;
	}

	public synchronized ArrayList<Tarea> getTareasPorBloque(int id_bloque) {
		Bloque bloque = this.getBloque (id_bloque);
		ArrayList <Tarea> lista_tareas;
		try {
			PreparedStatement stm = c.prepareStatement(
			"SELECT " +
				"id_tarea " +
			"FROM " +
				"tarea " +
			"WHERE " +
				"bloque = ? " +
			"ORDER BY id_tarea ASC"
			);
			stm.setInt(1, id_bloque);
			ResultSet res = stm.executeQuery();
			lista_tareas = new ArrayList <Tarea> ();
			while (res.next()) {
				lista_tareas.add (this.getTareaById(res.getInt("id_tarea")));
			}
		} catch (Exception e) {
				System.err.println ("Error al recuperar tareas de bloque: " + e.getMessage());
				e.printStackTrace();
				lista_tareas = null;
		}
		
		return lista_tareas;
	}

	public synchronized boolean setParcial(Tarea tarea, Integer idUsuario){
		tarea.setEstado (EstadoTarea.enProceso);
		
		try {
			PreparedStatement stm = c.prepareStatement (
			"UPDATE " +
				"tarea " +
			"SET " +
				"estado = subq.id_estado_tarea " +
			"FROM ( " +
				"SELECT " +
					"id_estado_tarea " +
				"FROM " +
					"estado_tarea " +
				"WHERE " +
					"estado = 'en proceso' " +
				") AS subq " +
			"WHERE " +
				"id_tarea = ?"
			);
		
			stm.setInt (1, tarea.getId());
			if(stm.executeUpdate() <1) {
				return false;
			}
			
			/* ACA TENEMOS QUE HACER EL INSERT O UPDATE EN procesamiento_tarea */
			/* Primero buscamos la id de procesamiento de tarea mas alto para el usuario */
			int id_proc_tarea = this.getIdProcesamiento (tarea.getId(), idUsuario);
			
			if (id_proc_tarea == -1) {
				System.err.println("Error al recuperar id de procesamiento de tarea, id invalido");
				return false;
			}

			stm = c.prepareStatement (
			"UPDATE " +
				"procesamiento_tarea " +
			"SET " +
				"estado = subq.id_estado_tarea, " +
				"parcial = ? " +
			"FROM ( " +
				"SELECT " +
					"id_estado_tarea " +
				"FROM " +
					"estado_tarea " +
				"WHERE " +
					"estado = 'en proceso' " +
				") AS subq " +
			"WHERE " +
				"id_procesamiento_tarea = ? ");
			
			stm.setBytes(1, tarea.getParcial());
			stm.setInt(2, id_proc_tarea);
			
			if(stm.executeUpdate() < 1) {
				return false;
			}
		} catch (Exception e) {
			System.err.println ("Error al guardar parcial en DB: "+e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/* Devuelve el ultimo parcial registrado para una tarea */
	public synchronized byte[] getParcial(int id_tarea) {
		byte[] parcial = null;

		try{
			PreparedStatement stm = c.prepareStatement (
			"SELECT "+
				"parcial "+
			"FROM "+
				"procesamiento_tarea "+
			"WHERE "+
				"tarea = ? "+
			"ORDER BY id_procesamiento_tarea DESC "+
			"LIMIT 1 "
			);
			stm.setInt(1, id_tarea);
			if(stm.execute()) {
				ResultSet res = stm.getResultSet();
				if (res.next()) {
					parcial = res.getBytes("parcial");
				}
			}
		} catch (Exception e) {
			System.err.println("Error al obtener parcial: " + e.getMessage());
			e.printStackTrace();
		}
		/* Si no se encontro parcial, devolvemos [0] */
		if(parcial == null) {
			parcial = new byte[1];
			parcial[0] = (byte) 0x00;
		}
		return parcial;
		
	}

	public synchronized boolean setResultado(Tarea tarea, Integer idUsuario){
		//SETEA EL RESULTADO FINAL EN LA BD
		tarea.setEstado (EstadoTarea.completada);
		this.cacheTareas.put (tarea.getId(), tarea);
		// Si cambio la tarea, el cache del bloque puede quedar desactualizado
		this.cacheBloques.remove (tarea.getBloque().getId());


		try {
			PreparedStatement stm = c.prepareStatement (
			"UPDATE " +
				"tarea " +
			"SET " +
				"estado = subq.id_estado_tarea " +
			"FROM ( " +
				"SELECT " +
					"id_estado_tarea " +
				"FROM " +
					"estado_tarea " +
				"WHERE " +
					"estado = 'completada' " +
				") AS subq " +
			"WHERE " +
				"id_tarea = ?"
			);
		
			stm.setInt (1, tarea.getId());
			if(stm.executeUpdate() <1) {
				return false;
			}
			
			/* ACA TENEMOS QUE HACER EL UPDATE EN procesamiento_tarea */
			/* Primero buscamos la id de procesamiento de tarea mas alto para el usuario */
			int id_proc_tarea = this.getIdProcesamiento (tarea.getId(), idUsuario);
			
			if (id_proc_tarea == -1) {
				System.err.println("Error al recuperar id de procesamiento de tarea, id invalido");
				return false;
			}

			stm = c.prepareStatement (
			"UPDATE " +
				"procesamiento_tarea " +
			"SET " +
				"estado = subq.id_estado_tarea, " +
				"resultado = ? " +
			"FROM ( " +
				"SELECT " +
					"id_estado_tarea " +
				"FROM " +
					"estado_tarea " +
				"WHERE " +
					"estado = 'completada' " +
				") AS subq " +
			"WHERE " +
				"id_procesamiento_tarea = ? ");
			
			stm.setBytes(1, tarea.getResultado());
			stm.setInt(2, id_proc_tarea);
			
			if(stm.executeUpdate() < 1) {
				return false;
			}

			//LLAMA AL METODO ESTAFINALIZADOBLOQUE(), SI ESTE DEVUELVE TRUE CAMBIA ESTADO DEL BLOQUE
			if (this.estaFinalizadoBloque (tarea.getBloque())) {
				tarea.getBloque().setEstado(EstadoBloque.completado);
				stm = c.prepareStatement (
				"UPDATE " +
					"bloque " +
				"SET " +
					"estado = subq.id_estado " +
				"FROM ( " +
					"SELECT " +
						"id_estado_bloque AS id_estado " +
					"FROM " +
						"estado_bloque " +
					"WHERE " +
						"estado = 'completado' " +
				") AS subq " +
				"WHERE id_bloque = ?");
				stm.setInt(1, tarea.getBloque().getId());

				if (stm.executeUpdate() < 1) {
					System.err.println ("Error al establecer estado al bloque como finalizado");
					return false;
				}

			}
		} catch (Exception e) {
			System.err.println ("Error al guardar resultado en DB: "+e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}	

	public synchronized boolean detenerTarea(Tarea tarea, Integer idUsuario){
		try {
			PreparedStatement stm = c.prepareStatement (
			"UPDATE " +
				"tarea " +
			"SET " +
				"estado = subq.id_estado_tarea " +
			"FROM ( " +
				"SELECT " +
					"estado_tarea.id_estado_tarea AS id_estado_tarea " +
				"FROM " +
					"estado_tarea " +
				"WHERE " +
					"estado_tarea.estado = 'detenida' " +
				") AS subq " +
			"WHERE " +
				"id_tarea = ?"
			);
		
			tarea.setEstado(EstadoTarea.detenida);
			stm.setInt (1, tarea.getId());
			if(stm.executeUpdate() <1) {
				return false;
			}
			int id_proc_tarea = this.getIdProcesamiento (tarea.getId(), idUsuario);
			
			if (id_proc_tarea == -1) {
				System.err.println("Error al recuperar id de procesamiento de tarea, id invalido");
				return false;
			}

			stm = c.prepareStatement (
			"UPDATE " +
				"procesamiento_tarea " +
			"SET " +
				"estado = subq.id_estado_tarea " +
			"FROM ( " +
				"SELECT " +
					"id_estado_tarea " +
				"FROM " +
					"estado_tarea " +
				"WHERE " +
					"estado = 'detenida' " +
				") AS subq " +
			"WHERE " +
				"id_procesamiento_tarea = ? ");
			
			stm.setInt(1, id_proc_tarea);
			
			if(stm.executeUpdate() < 1) {
				return false;
			}
	
		tarea.setEstado(EstadoTarea.detenida);
		} catch (Exception e) {
			System.err.println("Error al detener tarea:" + e.getMessage());
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/* Cuando el servidor arranca todas las tareas deben estar pendientes, completadas o detenidas (nunca en proceso) */
	public synchronized boolean detenerTareasEnProceso() {
		try {
			PreparedStatement stm = c.prepareStatement (
			"UPDATE " +
				"tarea " +
			"SET " +
				"estado=subq_detenida.id " +
			"FROM ( " +
				"SELECT " +
					"id_estado_tarea AS id " +
				"FROM " +
					"estado_tarea " +
				"WHERE " +
					"estado = 'en proceso' " +
			") AS subq_proc, " +
			"(" +
					"SELECT " +
					"id_estado_tarea  AS id " +
				"FROM " +
					"estado_tarea " +
				"WHERE " +
					"estado = 'detenida' " +
			") AS subq_detenida " +
			"WHERE " +
				"estado = subq_proc.id" 
			);
			return stm.execute();
		} catch (Exception e) {
			System.err.println("Error al detener tareas en proceso: "+e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	public synchronized int generarBloques (int numBloques, int numTareasPorBloque, int numBytesPorTarea) {
		int id_bloque = -1;

		try {
			PreparedStatement stm_bloque = c.prepareStatement ("INSERT INTO bloque (estado) SELECT id_estado_bloque FROM estado_bloque WHERE estado = 'pendiente'",Statement.RETURN_GENERATED_KEYS);
			PreparedStatement stm_tarea = c.prepareStatement ("INSERT INTO tarea (bloque, header_bytes, estado) SELECT ? as bloque, ? as header_bytes, id_estado_bloque as estado FROM estado_bloque where estado = 'pendiente'",Statement.RETURN_GENERATED_KEYS);
			ResultSet generatedKeys;
			Random random = new Random ();
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
		} catch (Exception e) {
			System.err.println ("Error al generar bloques en la base de datos: " + e.getMessage());
			e.printStackTrace();
		}
		return id_bloque;
	}

	public synchronized boolean generarBloqueReplicado (int id_bloque) {
		try {
			PreparedStatement stm = c.prepareStatement ("INSERT INTO bloque (id_bloque, estado) SELECT ?, id_estado_bloque FROM estado_bloque WHERE estado = 'pendiente'");
			stm.setInt (1, id_bloque);
			return stm.execute();
		} catch (Exception e) {
			System.err.println ("Error al insertar bloque en la base de datos: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	public synchronized boolean generarTareaReplicada (int id_tarea, int id_bloque, byte[] bytes_tarea) {
		try {
			PreparedStatement stm = c.prepareStatement ("INSERT INTO tarea (id_tarea, bloque, header_bytes, estado) SELECT ?, ?, ?, id_estado_tarea FROM estado_tarea WHERE estado = 'pendiente'");
			stm.setInt (1, id_tarea);
			stm.setInt (2, id_bloque);
			stm.setBytes (3, bytes_tarea);
			return stm.execute();
		} catch (Exception e) {
			System.err.println ("Error al insertar tarea en la base de datos: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	private synchronized boolean estaFinalizadoBloque(Bloque bloque){
		//CHEQUEA QUE TODAS LAS TAREAS DEL BLOQUE ESTEN COMPLETAS
		try {
			PreparedStatement stm = c.prepareStatement (
			"SELECT " +
				"COUNT (tarea.id_tarea) AS restantes " +
			"FROM " +
				"tarea " +
				"JOIN estado_tarea ON tarea.estado = estado_tarea.id_estado_tarea "+
			"WHERE " + 
				"tarea.bloque = ? " +
				"AND estado_tarea.estado <> 'completada' "
			);
			stm.setInt (1, bloque.getId());
			stm.execute();
			ResultSet result = stm.getResultSet();
			result.next();
			int restantes = result.getInt("restantes");
			System.out.println ("DEBUG: Restantes: " + restantes);
			return (restantes == 0);
		} catch (Exception e) {
			System.err.println ("Error al intentar determinar si bloque esta finalizado "+e.getMessage());
			e.printStackTrace();
		}
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
				"ORDER BY bloque ASC " +
				"LIMIT 1"
			); 

			stm_tarea.execute();
			res_tarea = stm_tarea.getResultSet ();

			if (res_tarea.next()) {
				System.out.println ("DEBUG: Se ha encontrado una tarea no iniciada dentro de un bloque no iniciado");
			} else {
				res_tarea = null;
			}
			
		} catch (Exception e) {
			System.err.println ("Error al recuperar tarea no iniciada");
			e.printStackTrace();
		}
		return res_tarea;
	}

	private synchronized boolean setEstadoBloque(int id_bloque, String estado) {
		try {
			PreparedStatement stmt_bloque = c.prepareStatement("UPDATE bloque set estado = (SELECT id_estado_bloque FROM estado_bloque WHERE estado = ?) WHERE bloque.id_bloque = ?");
			stmt_bloque.setString(1, estado);
			stmt_bloque.setInt(2, id_bloque);
			if (stmt_bloque.executeUpdate() > 0){
				// Actualizar cache
				Bloque bloque = this.cacheBloques.get(id_bloque);
				if (bloque != null) {
					bloque.setEstado ((estado=="en proceso")?EstadoBloque.enProceso: (estado=="pendiente")?EstadoBloque.pendiente: EstadoBloque.completado);
				}
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
	
	
	public synchronized boolean asignarTareaUsuario (int id_tarea, int id_usuario) {
		try {
			/* Insertamos el procesamiento */

			PreparedStatement stm_procesamiento = c.prepareStatement(
			"INSERT " +
				"INTO procesamiento_tarea (tarea, usuario, parcial, estado) " +
			"SELECT " +
				"? as tarea, " +
				"? as usuario, " +
				"? as parcial, " +
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
			stm_procesamiento.setBytes(3, this.getParcial(id_tarea));
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

	public synchronized boolean asignarTareaUsuarioReplicado (int id_tarea, int id_usuario, int id_procesamiento_tarea) {
			/* Insertamos el procesamiento */
		try {
			PreparedStatement stm_procesamiento = c.prepareStatement(
			"INSERT " +
				"INTO procesamiento_tarea (id_procesamiento_tarea, tarea, usuario, parcial, estado) " +
			"SELECT " +
				"? as id_procesamiento_tarea, "+
				"? as tarea, " +
				"? as usuario, " +
				"? as parcial, " +
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

			stm_procesamiento.setInt(1, id_procesamiento_tarea);
			stm_procesamiento.setInt(2, id_tarea);
			stm_procesamiento.setInt(3, id_usuario);
			stm_procesamiento.setBytes(4, this.getParcial(id_tarea));
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

	public synchronized Usuario getUsuario(String nombreUsuario) {
		Usuario u = null;
		try {
			String query = "SELECT USUARIO.NOMBRE, USUARIO.CONTRASENIA, USUARIO.PUNTOS, USUARIO.ID_USUARIO FROM USUARIO WHERE USUARIO.NOMBRE = ? ";

			PreparedStatement stm = c.prepareStatement(query);
			stm.setString(1, nombreUsuario);
			stm.execute();
			ResultSet rs = stm.getResultSet();
			if (rs.next()) {
				u = new Usuario();
				u.setNombre(rs.getString("nombre"));
				u.setPassword(rs.getString("contrasenia"));
				u.setPuntos(rs.getInt("puntos"));
				u.setId(rs.getInt("id_usuario"));
			}
			
		} catch (SQLException e) {
			System.err.println ("Error al recuperar Usuario");
			e.printStackTrace();
		}

		return u;
	}
	
	public synchronized int getIdProcesamiento (int id_tarea, int id_usuario) {
		int id_procesamiento = -1;
		try {
			PreparedStatement stm = c.prepareStatement (
			"SELECT " +
				"id_procesamiento_tarea " +
			"FROM " +
				"procesamiento_tarea " +
			"WHERE " +
				"tarea = ? " +
				"AND usuario = ? " +
			"ORDER BY id_procesamiento_tarea DESC " +
			"LIMIT 1 "); 
			stm.setInt(1, id_tarea);
			stm.setInt(2, id_usuario);
			stm.execute();
			ResultSet rs = stm.getResultSet();
			if (rs.next()) {
				id_procesamiento = rs.getInt("id_procesamiento_tarea");
			}
		
		} catch (Exception e) {
			System.err.println ("Error al recuperar ID de procesamiento de tarea");
			e.printStackTrace();
		}
		
		return id_procesamiento;
	}
	//METODO PARA SABER QUE USUARIOS INTERVINIERON EN UNA TAREA
	public synchronized ArrayList<ProcesamientoTarea>  getProcesamientos(Integer id_tarea) {
		ArrayList<ProcesamientoTarea> lista_pt;
		ProcesamientoTarea pt;
		BigInteger inicial = new BigInteger("0");
		try {
			PreparedStatement stm = c.prepareStatement (
			"SELECT " +
				"id_procesamiento_tarea, " +
				"tarea, " +
				"usuario, " +
				"parcial, " +
				"resultado " +
			"FROM " +
				"procesamiento_tarea " +
			"WHERE " +
				"tarea = ? " +
			"ORDER BY LENGTH(parcial), parcial"	
			);
			stm.setInt(1, id_tarea);
			ResultSet res = stm.executeQuery();
			lista_pt = new ArrayList<ProcesamientoTarea>();
			while (res.next()) {
				pt = new ProcesamientoTarea();
				pt.setId_procesamiento_tarea(res.getInt("id_procesamiento_tarea"));
				pt.setTarea(res.getInt("tarea"));
				pt.setUsuario(res.getInt("usuario"));
				pt.setParcial(res.getBytes("parcial"));
				pt.setResultado(res.getBytes("resultado"));
				pt.setResta(inicial);
				pt.setTrabajo_Realizado(inicial);
				pt.setParcialCombinaciones(inicial);
				pt.setResultadoCombinaciones(inicial);
				pt.setPuntos(0);
				lista_pt.add(pt);
			}
		} catch (SQLException e) {
			System.err.println ("Error al recuperar los procesamientos de tareas: " + e.getMessage());
			e.printStackTrace();
			lista_pt = null;
		}
		
		return lista_pt;
		
	}
	
	public synchronized boolean actualizarPuntos(Integer id_usuario, Integer puntos) {
		try {
			PreparedStatement stm = c.prepareStatement(
			"UPDATE " + 
				"usuario " + 
			"SET " + 
				"puntos = (SELECT usuario.puntos FROM usuario WHERE usuario.id_usuario = ?) + ? " +
			"WHERE usuario.id_usuario = ?"
			);
			stm.setInt(1, id_usuario);
			stm.setInt(2, puntos);
			stm.setInt(3, id_usuario);
			if(stm.executeUpdate() <1) {
				System.err.println("Error al actualizar los puntos del usuario");
				return false;
			}
		} catch (SQLException e) {
			System.err.println("Error al actualizar los puntos del usuario");
			e.printStackTrace();
		}
		return true;
	}

	public synchronized Replicacion getReplicacion () {
		Replicacion replicacion = new Replicacion ();
		if (replicacion.cargar (this.c)) {
			return replicacion;
		} else {
			return null;
		}
	}

	public synchronized boolean replicar (Replicacion replicacion) {
		if (this.borrarTodo ()) {
			return replicacion.insertarTodo (this.c);
		} else {
			return false;
		}
	}

	public synchronized boolean borrarTodo () {
		this.limpiarCache ();
		try {
			this.c.setAutoCommit (false);
			PreparedStatement dropProcesamientos = this.c.prepareStatement ("DELETE FROM PROCESAMIENTO_TAREA");
			PreparedStatement dropBloques = this.c.prepareStatement ("DELETE FROM BLOQUES");
			PreparedStatement dropTareas = this.c.prepareStatement ("DELETE FROM TAREAS");
			PreparedStatement dropUsuarios = this.c.prepareStatement ("DELETE FROM USUARIOS");
			dropProcesamientos.execute ();
			dropProcesamientos.execute ();
			dropProcesamientos.execute ();
			dropProcesamientos.execute ();
			this.c.commit ();
		} catch (Exception e) {
			try {
				this.c.rollback ();
				this.c.setAutoCommit (true);
			} catch (Exception e2) {}
			return false;
		}
		try {
			this.c.setAutoCommit (true);
		} catch (Exception e) {}
		return true;
	}

	public synchronized void limpiarCache () {
		this.cacheBloques = new HashMap <Integer, Bloque> ();
		this.cacheTareas = new HashMap <Integer, Tarea> ();
	}

	public String cacheInfo () {
		return	
		"Tareas en cache: " + this.cacheTareas.size() +
		"\nBloques en cache: " + this.cacheBloques.size();
	}
}
