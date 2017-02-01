package misc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.*;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import baseDeDatos.Usuario;
import bloquesYTareas.Tarea;
import cliente.ModoTrabajo;

public class Loggeador {
	
	/**
	 * Esta clase se encarga de mantener el/los archivos de log del lado del servidor, ya sea primario o secundario.
	 */	
	
	private static Loggeador logger=null;
	
	private static final String pathlog = "log.csv";
	private static final String pathErrores = "log errores.csv";
	
	private File archivoLog;
	private File archivoErrores;
	
	private Semaphore semaforo;
	private FileWriter writer;
	private FileWriter errorWriter;
	
	public static Loggeador getLoggeador(){
		if(logger==null){
			try {
				logger = new Loggeador();
				return logger;
			} catch (IOException e) {
				return null;
			}
		}else{
			return logger;
		}
	}
	
	private Loggeador() throws IOException{
		this.archivoLog = new File(pathlog);
		this.archivoErrores = new File(pathErrores);
		
		if(!this.archivoErrores.exists()){
			//si no existia le pongo la descripcion de los campos
			this.writer = new FileWriter(this.archivoLog,true);
			this.writer.write("fecha, hora, tipo de registro, descripcion "+ System.lineSeparator());
			this.writer.flush();
		}else{
			this.writer = new FileWriter(this.archivoLog,true);
		}
		
		this.errorWriter = new FileWriter(this.archivoErrores,true);
		this.semaforo= new Semaphore(1);
		
	}
	
	public boolean guardar(String tipoRegistro,String informacion){
		
		try {
			this.semaforo.acquire();
		} catch (InterruptedException e1) {
			return false;
		}
		
		LocalDate fecha = LocalDate.now();
		LocalTime hora = LocalTime.now();
		
		try {
			this.writer.write(fecha+" , "+hora+" , " + tipoRegistro + " , " +informacion + System.lineSeparator());
			this.writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		this.semaforo.release();
		return true;
	}
	
	public boolean guardar(Exception e){
		
		System.err.println("Se ha detectado un error. Por favor revise el log. Error: "+e.toString());
		
		try {
			this.semaforo.acquire();
		} catch (InterruptedException e1) {
			return false;
		}
		LocalDate fecha = LocalDate.now();
		LocalTime hora = LocalTime.now();
		
		try {
			this.writer.write(fecha+" , "+hora+" , " + "ERROR" + " , " + e.toString() + System.lineSeparator());
			this.writer.flush();
		} catch (IOException e1) {
			this.semaforo.release();
			return false;
		}
		
		try {
			this.errorWriter.write(fecha+" , "+hora + " , " + e.toString() + System.lineSeparator() );
			this.errorWriter.write("---------> Message: "+e.getMessage() + System.lineSeparator() );
			this.errorWriter.write("---------> Localized Message: "+e.getLocalizedMessage() + System.lineSeparator() );
			this.errorWriter.write("---------> Cause: "+e.getCause() + System.lineSeparator() );
			this.errorWriter.write("---------> Stack Trace: "+ System.lineSeparator() );
			
			for (StackTraceElement s: Arrays.asList( e.getStackTrace() ) ){
				this.errorWriter.write("+++++++++++++> "+s.toString() + System.lineSeparator() );
			}
			this.errorWriter.write( System.lineSeparator() );
			this.errorWriter.flush();
		} catch (IOException e1) {
			this.semaforo.release();
			return false;
		}
		this.semaforo.release();
		return true;
	}
	
	public synchronized boolean guardarTiempo(Tarea tarea,Usuario user, Duration tiempo,ModoTrabajo modo,boolean rtaFinal){
		System.out.println("Logger - guardar tiempo");
		String s="bloque: "+tarea.getBloque().getId()+"- tarea: "+tarea.getId()+"- tiempo: "+tiempo.toString().substring(2)+ "-modo: "+modo.name();
		if(rtaFinal){
			s=s+"- Respuesta Final";
		}else{
			s=s+"- Respuesta Parcial";
		}
		System.out.println(s);
		this.guardar("Tiempo", s);
		
		return false;
	}
	
	public void cerrar(){
		try {
			this.semaforo.release();
			this.semaforo=null;
			
			this.writer.flush();
			this.writer.close();
			this.writer=null;
			

			this.errorWriter.flush();
			this.errorWriter.close();
			this.errorWriter=null;
		} catch (IOException e) {
		}
	}
	
}