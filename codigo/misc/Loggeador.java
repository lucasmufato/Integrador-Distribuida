package misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import baseDeDatos.Usuario;
import bloquesYTareas.Tarea;
import cliente.ModoTrabajo;
import mensajes.replicacion.*;

public class Loggeador {
	
	/**
	 * Esta clase se encarga de mantener el/los archivos de log del lado del servidor, ya sea primario o secundario.
	 * Y las estadisticas de las tareas realizadas
	 */	
	
	private static Loggeador logger=null;
	
	private static final String pathlog = "log.csv";
	private static final String pathErrores = "log errores.csv";
	private static final String pathEstadisticas = "Estadisticas tiempo.xlsx";
	
	private File archivoLog;
	private File archivoErrores;
	private File archivoExcel;
	
	//para el manejo del excel
	private XSSFWorkbook excel;
    private XSSFSheet hojaDatos;
	private Integer ultimaFila;
    private FileOutputStream excelWriter;
    //sincronizacion
	private Semaphore semaforo;
	
	//manejo de archivos de logs
	private FileWriter writer;
	private FileWriter errorWriter;
	
	public static void main(String[] args){
		Loggeador.getLoggeador();
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
		this.semaforo= new Semaphore(1);
		
		this.archivoExcel= new File(pathEstadisticas);
		if (this.archivoExcel.exists()){
			System.out.println("el archivo existe");
			FileInputStream excelFile = new FileInputStream(this.archivoExcel);
			excelWriter = new FileOutputStream(this.archivoExcel,true);
			//System.out.println("antes de crear elworbok");
			this.excel = new XSSFWorkbook(excelFile);
	        this.hojaDatos = this.excel.getSheet("Datos");
	        this.conseguirUltimaFila();
		}else{
			//TODO aca no entra, si el archivo no esta explota todo
			this.archivoExcel.createNewFile();
			System.out.println("Creando archivo de estadisticas");
			//si no existe el archivo lo creo y creo las cabeceras.
			FileInputStream excelFile = new FileInputStream(this.archivoExcel);
			this.excel = new XSSFWorkbook(excelFile);
	        this.hojaDatos = this.excel.createSheet("Datos");
	        Row row = this.hojaDatos.createRow(0);
	        row.createCell(1).setCellValue("fecha");
	        row.createCell(2).setCellValue("hora");
	        row.createCell(3).setCellValue("Nro bloque");
	        row.createCell(4).setCellValue("Nro tarea");
	        row.createCell(5).setCellValue("Usuario");
	        row.createCell(6).setCellValue("Tiempo");
	        row.createCell(7).setCellValue("Modo Trabajo");
	        row.createCell(8).setCellValue("Respuesta");
	        this.ultimaFila=1;
	        FileOutputStream outputStream = new FileOutputStream(this.archivoExcel);
            this.excel.write(outputStream);
		}
		
        
		
	}
	
	private void conseguirUltimaFila() {
		 Iterator<Row> iterator = this.hojaDatos.iterator();
		 this.ultimaFila=0;
         while (iterator.hasNext()) {
             iterator.next();
             this.ultimaFila++;
         }
         System.out.println("El archivo tiene "+this.ultimaFila+ " filas");
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
		// IMPLEMENTAR !
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
		/*
		String s="bloque: "+tarea.getBloque().getId()+"- tarea: "+tarea.getId()+"- tiempo: "+tiempo.toString().substring(2)+ "-modo: "+modo.name();
		if(rtaFinal){
			s=s+"- Respuesta Final";
		}else{
			s=s+"- Respuesta Parcial";
		}
		System.out.println(s);
		this.guardar("Tiempo", s);
		*/
		System.out.println("la fila en la q voy a escribir es la nro: "+this.ultimaFila);
		Row row = this.hojaDatos.createRow(this.ultimaFila);
		row.createCell(0).setCellValue(LocalDate.now().toString());
		row.createCell(1).setCellValue(LocalTime.now().toString());
		row.createCell(2).setCellValue(tarea.getBloque().getId().toString());
		row.createCell(3).setCellValue(tarea.getId().toString());
		row.createCell(4).setCellValue(user.getNombre());
		row.createCell(5).setCellValue(tiempo.toString());
		row.createCell(6).setCellValue(modo.name());
		if(rtaFinal){
			row.createCell(7).setCellValue("Final");
		}else{
			row.createCell(7).setCellValue("Incompleto");
		}
		
        try {
			this.excel.write(this.excelWriter);
			this.excelWriter.flush();
			this.ultimaFila++;
		} catch (IOException e) {
			this.guardar(e);
		}
		
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
			
			this.excelWriter.flush();
			this.excelWriter.close();
			this.excel.close();
			this.excel=null;
			this.excelWriter=null;
			
		} catch (IOException e) {
		}
	}
	
}