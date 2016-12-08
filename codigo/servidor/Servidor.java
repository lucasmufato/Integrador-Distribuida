package servidor;

public class Servidor {

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Servidor servidor = new Servidor();
	}
	
	public Servidor(){
		this.buscarServidorPrimario();
	}
	
	public boolean buscarServidorPrimario(){
		//lee el archivo con las ips
		//intenta de contactarse con el servidor primario
		//si no lo logra se crea un servidor primario
		//si se conecta con un servidor primario, crea un servidor backup
		
		this.crearServidorPrimario();
		return false;
	}
	
	public boolean crearServidorPrimario(){
		//crea una instancia de primario
		//le dice que arranque su GUI
		
		/** por ahora queda asi **/
		Primario primario = new Primario();
		primario.crearGUI();
		return false;
	}
	
	public boolean crearServidorBackup(){
		//crea el servidor de backup pasandole como dato la ip y puerto del primario
		return false;
	}
}
