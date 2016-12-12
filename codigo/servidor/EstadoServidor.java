package servidor;

public enum EstadoServidor {
	desconectado, esperandoClientes, replicandoDelPrimario, Backup
	//replicando del primario se usaria para el caso en el que sea backup y este atrasado con respecto al primario
	//y backup cuando soy backup
}
