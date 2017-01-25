package mineria;

import java.util.ArrayList;
import java.util.Arrays;

import bloquesYTareas.Tarea;
import cliente.Cliente;

public class HiloMineroGestorMultiCore extends HiloMinero {
	/*
	 *	esta clase lo que hace es conseguir la cantidad de CPUs de la maquina, y en base a eso crea una cantidad de hilos
	 *	estos procesan el hash cada uno con un parcial distinto,que van tomando y aumentando de este hilo padre. 
	 * 
	 */
	private byte[] parcialComprobado=null;	//aca tengo el ultimo parcial que un hilo me comprobo que no era resultado final
	private byte[] proximoParcial=null;		//aca tengo el parcial que le voy a dar al hilo q me lo pida
	private ArrayList<HiloMinero> hilosHijos;	//clases q hacen los sha256
	private ArrayList<Thread> hilos;	//hilos que se obtiene de cada clase
	
	public HiloMineroGestorMultiCore(Cliente cliente, Tarea tarea){
		super(tarea);
		this.cliente=cliente;
		this.proximoParcial=tarea.getParcial();
		this.parcialComprobado=tarea.getParcial().clone();
		this.hilosHijos= new ArrayList<HiloMinero>();
		this.hilos = new ArrayList<Thread>();
	}
	
	@Override
	public void run() {
		//indico que estoy trabajando
		HiloMinero.trabajando = true;
		//en base a la cantidad de nucleos que obtengo, hago un hilo por cada uno.
		int processors = Runtime.getRuntime().availableProcessors();
		System.out.println("GestorHilos: creo "+processors+" hilos para minear");
		this.notificarParcial(this.parcialComprobado);
		for(int i=0; i<processors;i++){
			HiloMinero hilo = new HiloMineroMultiCore(this.tareaNueva,this);
			hilo.setInicio( this.dameParcial() );
			this.hilosHijos.add(hilo);
			Thread thread = new Thread(hilo);
			this.hilos.add(thread);
			thread.start();
		}
		try {	Thread.sleep(HiloMinero.LAPSO_NOTIFICACION);	} catch (InterruptedException e1) {		}
		while(HiloMinero.trabajando){
			//mientras este trabajando, espero 2 segundo y envio el ultimo parcial comprobado
			try {
				this.notificarParcial(this.parcialComprobado);
				Thread.sleep(HiloMinero.LAPSO_NOTIFICACION);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//ahora por cada hilo que genere espero que terminen.
		for (Thread t: this.hilos){
			try {
				t.join();
			} catch (InterruptedException e) {
				//si soy interrumpido, dejo de esperar
				e.printStackTrace();
				break;
			}
		}
	}
	
	//metodo que guarda el ultimo resultado parcial comprobado!
	public synchronized void informarParcial(byte[] parcial){
		//solo guardo el parcial si es mayor que el anterior que tengo

		//si tiene un tamaño mas grande es por q es mas grande
		if(parcial.length>this.parcialComprobado.length){
			this.parcialComprobado=parcial;
		}else{
			//si es igual de tamaño comparo posicion a posicion
			if(parcial.length==this.parcialComprobado.length){
				int i=0;
				boolean bandera=true;
				while(bandera){
					if((parcial[i]&0xFF) > (this.parcialComprobado[i]&0xFF)){
						bandera=false;
						System.arraycopy(parcial, 0, this.parcialComprobado, 0, parcial.length);
					}else{
						if((parcial[i]&0xFF) < (this.parcialComprobado[i]&0xFF)){
							bandera=false;
						}else{
							i++;
							if(i>=parcial.length){
								bandera=false;
							}
						}
					}
				}
			}
			//si es menor directamente no lo guardo
		}
		
		
	}
	
	//metodo que devuelve el parcial a probar
	public synchronized byte[] dameParcial(){
	
		//hago una copia, la cual es la que voy a devolver, mientras trabajo con la de la clase.
		byte[] parcialADevolver= new byte[this.proximoParcial.length];
		System.arraycopy(this.proximoParcial, 0, parcialADevolver, 0, this.proximoParcial.length);
		
		/**	arranca un terrible copy paste con modificacion al metodo de pablo **/
		
		int pos = this.proximoParcial.length -1; //Ponemos el puntero en el bit menos significativo
		boolean seguir = true;

		/* Este bucle aumenta el numero de secuencia en la concatenacion */
		while (seguir) {
			if (this.proximoParcial[pos] != (byte)0xFF) {
				this.proximoParcial[pos]++;
				seguir = false;
			} else {
				this.proximoParcial[pos] = 0x00;
				pos--; //corremos el puntero a un bit mas significativo (hacia la izquierda)
			}

			/* Si llegamos a la parte de la tarea, significa que ya probamos todas los numeros de secuencia con esa cantidad de bits */
			if (pos < 0) {
				/* Aumentamos el tamanio del numero de secuencia */	
				this.proximoParcial = new byte [this.proximoParcial.length+1];
				Arrays.fill (this.proximoParcial, (byte) 0x00);
				seguir = false;
				pos = this.proximoParcial.length -1;
			}
		}

		return parcialADevolver;
	}

	@Override
	public void setTarea(Tarea tarea){
		this.tareaNueva=tarea;
		this.proximoParcial=tarea.getParcial();
		this.parcialComprobado=tarea.getParcial().clone();
	}
	
}
