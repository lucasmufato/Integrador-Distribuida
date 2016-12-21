package cliente;

import java.io.*;

public class PruebaC {

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
		//comunicarseC();
		pasarleTarea();
	    p.destroy() ;
	}
	
	private boolean crearHiloC(){
		try
	    {
	        Runtime rt = Runtime.getRuntime() ;

	        p = rt.exec("/home/lucas/git/Integrador-Distribuida/codigo/cliente/prueba.exe") ;
	        //leer del proceso
	        in = p.getInputStream() ;
	        bufIn = new BufferedReader(new InputStreamReader(in));
	        //escribir al proceso
	        out = p.getOutputStream ();
	        printOut = new PrintWriter(out);
	        //errores
	        err = p.getErrorStream();
	    }catch(Exception exc){
	        System.out.println("Err " + exc.getMessage());
	        System.out.println("Err " + exc.getLocalizedMessage());
	        System.out.println("Err " + exc.toString());
	        
	    }
		return true;
	}
	
	private boolean pasarleTarea(){
		byte[] tareaResolver = {0x0A, 0x03, 0x0F, 0x0B, 0x0A, 0x03, 0x0F, 0x0B, 0x0A, 0x03, 0x0F};	//11bytes
		byte[] parcial = {0x00};
		byte[] limite_superior = new byte[32];
		limite_superior[3] = (byte) 0x80;
		try {
			/** 	POR ALGUNA RAZON TENGO Q MANDARLE SI O SI ALGO PRIMERO ANTES DE LEER. SINO SE CUELGA EN LEER, PERO NO LEE NADA	**/
			this.out.write(tareaResolver);
			this.out.flush();
			System.out.println("java: le mande la tarea -> ");
			for (int i=0; i<11;i++){
				System.out.print(tareaResolver[i]+" ");
			}
			System.out.println();
			System.out.println("esperando informacion del archivo de debug.");
			String line = bufIn.readLine();
			System.out.println(line);
			//byte[] respuestaC = new byte[tamañoTarea];
			//in.read(respuestaC);
			line = bufIn.readLine();
			System.out.println(line);
			
			System.out.println("C: lei la tarea -> ");
			for (int i=0; i<11;i++){
				//System.out.println(respuestaC[i]);
			}
			
			//this.out.write(parcial);
			//this.out.write(limite_superior);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		
		return true;
	}
	
	
	private boolean comunicarseC(){
		//send a command to 

        printOut.println("hola mundo");
        printOut.flush();
		String line;
		boolean bandera= true;
		System.out.println("esperando..");
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
