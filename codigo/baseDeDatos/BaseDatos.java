package baseDeDatos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BaseDatos {
	
	protected Connection c;
	protected final static String host="localhost";
	protected final static String nombreBD="finaldistribuido";
	protected final static Integer puertoBD=5432;
	protected final static String user="postgres";
	protected final static String password="jasmin";
	
	public BaseDatos(){
	}
	
	public boolean conectarse(){
		try {
	         Class.forName("org.postgresql.Driver");
	         c = DriverManager.getConnection("jdbc:postgresql://"+host+":"+puertoBD+"/"+nombreBD,user, password);
	         System.out.println("LectorBD-: conexión exitosa con la BD");
	         return true;
	      } catch (Exception e) {
	    	 System.out.println("LectorBD-: error al conectar con la BD");
	         e.printStackTrace();
	         return false;
	      }
	}
	
	public Integer autenticar(String usuario, String contraseña){
		//este metodo recibe el nombre de usuario y la contraseÃ±a para hacer el logeo
		//si el logeo no funciona (error de user y password) devuelve nulo, sino crea y devuelve un ID_SESION
		PreparedStatement query;
		ResultSet rs;
		try {
			//A LA  QUERY ME PEDIA PONERLE { PORQUE ES UN ARRAY, SI NO NO ME DEJABA
			query = c.prepareStatement("SELECT * FROM USUARIO S WHERE S.NOMBRE = "+ "'{" + usuario + "}'" +" AND S.CONTRASENIA = "+ "'{" + contraseña + "}'");
			rs = query.executeQuery();
			if (rs.next() == false) {
				//si la primer respuesta ya es nulo es por que el user y contraseÃ±a son incorrectos
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
