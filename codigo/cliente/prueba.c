#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(void) {

unsigned int byte_read;
char *string, *tok;
int cmd_id;

int len = 64;
string = (char *) malloc(len + 1);


    byte_read = getline(&string,&byte_read, stdin);

    if (byte_read == -1) {
        printf("Error reading input\n");
        free(string);
        exit(0);
        //
    } else {
        printf("echo: %s\n", string);
    }
 
return EXIT_SUCCESS;
}
