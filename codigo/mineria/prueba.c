#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(void) {
/*
unsigned int byte_read;
char *string, *tok;
int cmd_id;

int len = 64;
string = (char *) malloc(len + 1);

	printf("primera cosa \n");
    byte_read = getline(&string,&byte_read, stdin);

    if (byte_read == -1) {
        printf("Error reading input\n");
        free(string);
        exit(0);
        //
    } else {
        printf("echo: %s\n", string);
    }
 */
 
	//aca voy a almacenar la tarea que reciba
	char cadenaTarea[11];
	//archivo para usar de debuger
	FILE *archivo = fopen("/home/lucas/git/Integrador-Distribuida/codigo/cliente/debug.txt","w");
	if(archivo == NULL){
		printf("no pude abrir el archivo de debug \n");
	}else{
		printf("pude abrir el archivo de debug \n ");
	}
	
	//el parcial
	char *parcial;
	// tengo q ver como hago para leer el limiteSuperior de java
	fprintf(archivo,"por leer lo que me mandan. \n");
	// voy a leer la cadena que es lo primero que me mandan
	
	fgets(cadenaTarea, sizeof(cadenaTarea),stdin);
	
	fprintf(archivo,"lei: %s \n",cadenaTarea);
	
	printf("Soy C: la cadena que lei es: %s \n",cadenaTarea);
	
	fprintf(archivo,"le respondi a java \n");
	
	//ahora que ya lei la tarea, se que me van a mandar el parcial
	
	//y ahora me van a mandar el limite superior
	
	//ya lei todo, le indico OK a la clase java y llamo a la funcion que hace la operacion en CUDA
	fclose(archivo);
	
return EXIT_SUCCESS;
}
