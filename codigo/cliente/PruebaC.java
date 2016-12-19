package cliente;

import java.io.*;
import java.util.concurrent.*;

public class PruebaC {

	private BlockingQueue<String> m_queue;
	private PrintWriter print_out;
	private BufferedReader bufIn;
	private PrintWriter printOut;
	private Process p;
	private InputStream in;
	private OutputStream out;
	private InputStream err;
	
	public static void main(String[] args){
		new PruebaC();
	}
	
	public PruebaC(){
		crearHiloC();
		comunicarseC();
	}
	
	private boolean crearHiloC(){
		try
	    {
	        Runtime rt = Runtime.getRuntime() ;

	        p = rt.exec("/home/lucas/git/Integrador-Distribuida/codigo/cliente/prueba.exe") ;
	        in = p.getInputStream() ;
	        out = p.getOutputStream ();
	        err = p.getErrorStream();

	        printOut = new PrintWriter(out);

	        m_queue = new ArrayBlockingQueue<String>(10);

	        //send a command to 
	        printOut.println("hola mundo");
	        printOut.flush();

	        //p.destroy() ;
	    }catch(Exception exc){
	        System.out.println("Err " + exc.getMessage());
	    }
		return true;
	}
	
	private boolean comunicarseC(){
		String line;
		boolean bandera= true;
		bufIn = new BufferedReader(new InputStreamReader(in));
	    while (bandera){
	    	try{
	            line = bufIn.readLine();

	            if (line != null){
	                System.out.println("PruebaC-> " +line);
	        	}else{
	        		bandera=false;
	        	}
	        }catch (IOException e){
	            System.out.println("Error readline " + e.getMessage());
	            return false;
	        }
	    }
		return true;
	}
	
}
