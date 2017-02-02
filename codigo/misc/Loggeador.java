package misc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.*;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import baseDeDatos.BaseDatos;
/*
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
*/

import baseDeDatos.Usuario;
import bloquesYTareas.Bloque;
import bloquesYTareas.Tarea;
import cliente.ModoTrabajo;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import mensajes.replicacion.*;

public class Loggeador {
	
	/**
	 * Esta clase se encarga de mantener el/los archivos de log del lado del servidor, ya sea primario o secundario.
	 * Y las estadisticas de las tareas realizadas
	 */	
	
	private static Loggeador logger=null;
	
	private static final String pathlog = "log.csv";
	private static final String pathErrores = "log errores.csv";
	private static final String pathEstadisticas = "Estadisticas tiempo.xls";
	
	private File archivoLog;
	private File archivoErrores;
	private File archivoExcel;
	
	//para el manejo del excel
	private Workbook excel;
	private WritableWorkbook excelEscritura;
	private WritableSheet hojaDatos;
	private Integer ultimaFila;
    //sincronizacion
	private Semaphore semaforo;
	
	//manejo de archivos de logs
	private FileWriter writer;
	private FileWriter errorWriter;
	
	private BaseDatos baseDatos;

	public static void main(String[] args){
		//para pŕobar el logeador
		Loggeador l=Loggeador.getLoggeador();
		Tarea t= new Tarea(new Bloque(1),new byte[1],new byte[2]);
		t.setId(1);
		Usuario u=new Usuario();
		u.setNombre("prueba");
		Instant t1= Instant.now();
		Instant t2= Instant.now();
		Duration d = Duration.between(t1, t2);
		l.guardarTiempo(t, u,d,ModoTrabajo.monoThread, true);
		l.guardarTiempo(t, u,d,ModoTrabajo.monoThread, true);
		l.cerrar();
	}
	
	public static Loggeador getLoggeador(){
		if(logger==null){
			try {
				logger = new Loggeador();
				return logger;
			} catch (IOException e) {
				e.printStackTrace();
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
		
		this.archivoExcel=new File(Loggeador.pathEstadisticas);
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

	public boolean guardar (MensajeReplicacion mensajeReplicacion) {
		switch (mensajeReplicacion.getCodigo()) {
			case parcialTarea :
				this.baseDatos.logMensajeParcialTarea ((MensajeParcialTarea) mensajeReplicacion);
				break;
			case resultadoTarea :
				this.baseDatos.logMensajeResultadoTarea ((MensajeResultadoTarea) mensajeReplicacion);
				break;
			case completitudBloque :
				this.baseDatos.logMensajeCompletitudBloque ((MensajeCompletitudBloque) mensajeReplicacion);
				break;
			case asignacionTareaUsuario :
				this.baseDatos.logMensajeAsignacionTareaUsuario ((MensajeAsignacionTareaUsuario) mensajeReplicacion);
				break;
			case detencionTarea :
				this.baseDatos.logMensajeDetencionTarea ((MensajeDetencionTarea) mensajeReplicacion);
				break;
			case asignacionPuntos :
				this.baseDatos.logMensajeAsignacionPuntos ((MensajeAsignacionPuntos) mensajeReplicacion);
				break;
			case generacionBloque :
				this.baseDatos.logMensajeGeneracionBloque ((MensajeGeneracionBloque) mensajeReplicacion);
				break;
			case generacionTarea :
				this.baseDatos.logMensajeGeneracionTarea ((MensajeGeneracionTarea) mensajeReplicacion);
				break;
		}
		
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
		
		String s="bloque: "+tarea.getBloque().getId()+"- tarea: "+tarea.getId()+"- tiempo: "+tiempo.toString().substring(2)+ "-modo: "+modo.name();
		if(rtaFinal){
			s=s+"- Respuesta Final";
		}else{
			s=s+"- Respuesta Parcial";
		}
		this.guardar("Tiempo", s);
		
		
		try {
			this.excel = Workbook.getWorkbook(this.archivoExcel);
			this.excelEscritura = Workbook.createWorkbook(this.archivoExcel, this.excel);
			this.hojaDatos = this.excelEscritura.getSheet(0); 
			this.ultimaFila=this.hojaDatos.getRows();
			System.out.println("la fila en la q voy a escribir es la nro: "+this.ultimaFila);
			Label l2 = new Label(0,this.ultimaFila,LocalDate.now().toString());
			Label l3 = new Label(1,this.ultimaFila,LocalTime.now().toString());
			Label l4 = new Label(2,this.ultimaFila,tarea.getBloque().getId().toString());
			Label l5 = new Label(3,this.ultimaFila,tarea.getId().toString());
			Label l6 = new Label(4,this.ultimaFila,user.getNombre());
			Label l7 = new Label(5,this.ultimaFila,tiempo.toString());
			Label l8 = new Label(6,this.ultimaFila,modo.name());
			Label l9=null;
			if(rtaFinal){
				l9 = new Label(7,this.ultimaFila,"Final");
			}else{
				l9 = new Label(7,this.ultimaFila,"Incompleto");
			}
			this.hojaDatos.addCell(l2);
			this.hojaDatos.addCell(l3);
			this.hojaDatos.addCell(l4);
			this.hojaDatos.addCell(l5);
			this.hojaDatos.addCell(l6);
			this.hojaDatos.addCell(l7);
			this.hojaDatos.addCell(l8);
			this.hojaDatos.addCell(l9);
			this.excelEscritura.write();
			this.excelEscritura.close();
			this.excel.close();
		} catch (IOException | WriteException | BiffException e) {
			this.guardar(e);
		}
		
		return false;
	}

	public void setBaseDatos (BaseDatos bd) {
		this.baseDatos = bd;
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