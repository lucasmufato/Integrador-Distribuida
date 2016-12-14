package baseDeDatos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import bloquesYTareas.*;

public class BaseDatos {
	
	protected Connection c;
	protected final static String host="localhost";
	protected final static String nombreBD="finaldistribuido";
	protected final static Integer puertoBD=5432;
	protected final static String user="lucas";
	protected final static String password="lucas";
	
	public BaseDatos(){
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
			//A LA  QUERY ME PEDIA PONERLE { PORQUE ES UN ARRAY, SI NO NO ME DEJABA
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
		//VA A HACER UN SELECT A LA BD PARA OBTENER UNA TAREA, VA A HACER UN INSERT CON PROCESAMINETO_TAREA CARGANDO EL ID USUARIO
		//LA TAREA A BUSCAR VA A SE ALGUNA QUE NO ESTE SIENDO USADA O COMPLETADA
		
		//HAGO NEW TAREA
		
		/** PARA PROBAR	**/
		byte[] tareaResolver = {0x0A, 0x03, 0x0F, 0x0B, 0x0A, 0x03, 0x0F, 0x0B, 0x0A, 0x03, 0x0F, 0x0B, 0x0A, 0x03, 0x0F, 0x0B, 0x0A, 0x03, 0x0F, 0x0B ,0x0A, 0x03, 0x0F,0x0B, 0x0A, 0x03, 0x0F,0x0B, 0x0A, 0x03, 0x0F,0x0B, 0x0A, 0x03, 0x0F,0x0B, 0x0A, 0x03, 0x0F,0x0B};
		byte[] parcial = {0x00};
		byte[] limite_superior = new byte[32];
		Arrays.fill (limite_superior, (byte) 0x00);
		limite_superior[3] = (byte) 0x80; // Esto va a buscar algo que empiece con 3 ceros
		
		Tarea tarea = new Tarea(null,tareaResolver,parcial);
		tarea.SetLimite(3,(byte) 0x80);
		
		return tarea;
		
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
	
	private synchronized boolean estaFinalizadoBloque(){
		//CHEQUEA QUE TODAS LAS TAREAS DEL BLOQUE ESTEN COMPLETAS
		return false;
	}
	
	
}
