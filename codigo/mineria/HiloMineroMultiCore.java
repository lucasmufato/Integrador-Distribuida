package mineria;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import bloquesYTareas.Tarea;

public class HiloMineroMultiCore extends HiloMinero {
	private MessageDigest sha256;
	protected HiloMineroGestorMultiCore gestor;
	public static int nro=0;
	protected int id;
	
	public HiloMineroMultiCore(Tarea tareaNueva, HiloMineroGestorMultiCore padre) {
		super(tareaNueva);
		this.gestor=padre;
		try {
			this.sha256 = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			System.err.println ("ERROR FATAL: No se puede inicializar el minero");
		}
		this.id=HiloMineroMultiCore.nro++;
	}
	
	@Override
	public void run() {
		//preparo los datos
		byte[] limite = this.getLimiteSuperior();
		byte[] tarea = this.getTarea();
		byte[] parcial = this.getInicio();
		byte[] hashAProbrar= new byte[tarea.length+parcial.length];
		System.arraycopy(tarea, 0, hashAProbrar, 0, tarea.length);
		System.arraycopy(parcial, 0, hashAProbrar, tarea.length, parcial.length);
		
		while(HiloMinero.trabajando){
			//System.out.println("Thread "+this.id+" pruebo con: "+HiloMinero.hashToString(parcial));
			byte []hash = this.sha256.digest(hashAProbrar);
			if(this.esMenor(hash, limite)){
				//encontre el resultado, se lo informo al gestor y salgo del bucle
				HiloMinero.trabajando=false;
				gestor.notificarResultado(parcial);
			}else{
				//si no era la respuesta, le informo al gestor que comprobe ese parcial
				gestor.informarParcial(parcial);				
				// y despues le pido otro parcial
				parcial =this.gestor.dameParcial();
				//preparo el hash a probar devuelta con el nuevo parcial
				hashAProbrar= new byte[tarea.length+parcial.length];
				System.arraycopy(tarea, 0, hashAProbrar, 0, tarea.length);
				System.arraycopy(parcial, 0, hashAProbrar, tarea.length, parcial.length);
				//y lo vuelvo a comprobar dentro del while
			}
		}
		
	}

}
