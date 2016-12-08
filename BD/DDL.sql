/* creo el usuario con permiso para crear BD */
CREATE ROLE distribuido LOGIN ENCRYPTED PASSWORD 'sistemas' CREATEDB VALID UNTIL 'infinity';

CREATE DATABASE finalDistribuido;

/* creo la tabla usuario para la version 0.1	*/
CREATE TABLE usuario(
	id_usuario SERIAL NOT NULL,
	nombre varchar[30] NOT NULL,
	contrasenia varchar(30) NOT NULL,
	puntos INTEGER  default 0,

	PRIMARY KEY(id_usuario),
	UNIQUE (nombre)
 
);
