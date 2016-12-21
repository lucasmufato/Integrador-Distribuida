#include <stdio.h>

void fprintByteArray(char bytes[], int longitud, FILE *file) {
	int count = 0;
	while (count < longitud) {
		fprintf(file, "%X ", bytes[count]);
		count++;
	}
}

void printByteArray(char bytes[], int longitud) {
	fprintByteArray(bytes, longitud, stdout);
}
