package baseDeDatos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BaseDatos {
	
	protected Connection c;
	protected final static String host="localhost";
	protected final static String nombreBD="";
	protected final static Integer puertoBD=5432;
	protected final static String user="";
	protected final static String password="";
	
	public BaseDatos(){
	}
	
	public boolean conectarse(){
		try {
	         Class.forName("org.postgresql.Driver");
	         c = DriverManager.getConnection("jdbc:postgresql://"+host+":"+puertoBD+"/"+nombreBD,user, password);
	         return true;
	      } catch (Exception e) {
	    	 System.out.println("LectorBD-: error al conectar con la BD");
	         e.printStackTrace();
	         return false;
	      }
	}
	
	public Integer autenticar(String usuario, String contraseña){
		//este metodo recibe el nombre de usuario y la contraseña para hacer el logeo
		//si el logeo no funciona (error de user y password) devuelve nulo, sino crea y devuelve un ID_SESION
		String query = "SELECT * FROM USUARIO S WHERE S.NOMBRE = "+usuario+" AND S.CONTRASENIA = "+contraseña;
		Statement stmt;
		try {
			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery( query );
			rs.next();
			if (rs.isLast()) {
				//si la primer respuesta ya es nulo es por que el user y contraseña son incorrectos
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
		return true;
	}
}
