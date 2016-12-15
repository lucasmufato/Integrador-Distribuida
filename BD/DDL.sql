DROP DATABASE IF EXISTS finaldistribuido;
DROP ROLE IF EXISTS distribuido;

/* creo el usuario con permiso para crear BD */
CREATE ROLE distribuido LOGIN ENCRYPTED PASSWORD 'sistemas' CREATEDB VALID UNTIL 'infinity';

CREATE DATABASE finaldistribuido;

GRANT ALL PRIVILEGES ON DATABASE finaldistribuido TO distribuido;

\connect finaldistribuido;

/* creo la tabla usuario para la version 0.1	*/
CREATE TABLE usuario(
	id_usuario SERIAL NOT NULL,
	nombre varchar(30) NOT NULL,
	contrasenia varchar(30) NOT NULL,
	puntos INTEGER  default 0,

	PRIMARY KEY(id_usuario),
	UNIQUE (nombre)
 
);

CREATE TABLE estado_bloque(
	id_estado_bloque SERIAL NOT NULL,
	estado varchar(20),
	
	PRIMARY KEY (id_estado_bloque)
);

CREATE TABLE bloque(
	id_bloque SERIAL NOT NULL,
	estado INTEGER,
	
	PRIMARY KEY (id_bloque),
	FOREIGN KEY (estado) REFERENCES estado_bloque (id_estado_bloque)
);

CREATE TABLE estado_tarea(
	id_estado_tarea SERIAL NOT NULL,
	estado varchar(20),
	
	PRIMARY KEY (id_estado_tarea)
);

CREATE TABLE tarea(
	id_tarea SERIAL NOT NULL,
	bloque INTEGER,
	header_bytes BYTEA, 
	estado INTEGER,
	
	PRIMARY KEY (id_tarea),
	FOREIGN KEY (bloque) REFERENCES bloque (id_bloque),
	FOREIGN KEY (estado) REFERENCES estado_tarea (id_estado_tarea)
);

CREATE TABLE procesamiento_tarea(
	id_procesamiento_tarea SERIAL NOT NULL,
	tarea INTEGER,
	usuario INTEGER,
	estado INTEGER,
	parcial BYTEA DEFAULT E'\\x00',
	resultado BYTEA DEFAULT NULL,
	
	PRIMARY KEY (id_procesamiento_tarea),
	FOREIGN KEY (usuario) REFERENCES usuario (id_usuario),
	FOREIGN KEY (tarea) REFERENCES tarea (id_tarea),
	FOREIGN KEY (estado) REFERENCES estado_tarea (id_estado_tarea)
);

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO distribuido;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO distribuido;

