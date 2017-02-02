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

/*	replicacion	*/

CREATE TABLE TIPO_MENSAJE(
	id_tipo_mensaje serial not null,
	nombre character varying(25) not null,
	primary key(id_tipo_mensaje)
);

CREATE TABLE LOG_REPLICACION(
	nro_version serial not null,
	tipo_mensaje integer not null,
	fecha timestamp default current_timestamp,

	primary key(nro_version),
	CONSTRAINT "fk_tipo_mensaje" FOREIGN key(tipo_mensaje) REFERENCES TIPO_MENSAJE(id_tipo_mensaje)
);

CREATE TABLE REP_ASIGNACION_TAREA_USUARIO(
	fk_tarea INTEGER NOT NULL,
	fk_usuario INTEGER NOT NULL,
	fk_procesamiento_tarea INTEGER NOT NULL,

	FOREIGN KEY(fk_tarea) REFERENCES tarea(id_tarea),
	FOREIGN KEY(fk_usuario) REFERENCES usuario(id_usuario),
	FOREIGN KEY(fk_procesamiento_tarea) REFERENCES procesamiento_tarea(id_procesamiento_tarea)
) INHERITS(log_replicacion);

CREATE TABLE REP_ASIGNACION_PUNTOS(
	puntos INTEGER NOT NULL,
	fk_usuario INTEGER NOT NULL,

	FOREIGN KEY(fk_usuario) REFERENCES usuario(id_usuario)
) INHERITS(log_replicacion);

CREATE TABLE REP_COMPLETITUD_BLOQUE(
	fk_bloque INTEGER NOT NULL,

	FOREIGN KEY(fk_bloque) REFERENCES bloque(id_bloque)
) INHERITS(log_replicacion);


/*	NO ESTABA SEGURO SI ES NECESARIO EL ID DE BLOQUE, ASI Q LO PUSE PERO PUEDE SER NULL	*/
CREATE TABLE REP_DETENCION_TAREA(
	fk_usuario INTEGER NOT NULL,
	fk_tarea INTEGER NOT NULL,
	fk_bloque INTEGER,

	FOREIGN KEY(fk_usuario) REFERENCES usuario(id_usuario),
	FOREIGN KEY(fk_tarea) REFERENCES tarea(id_tarea),
	FOREIGN KEY(fk_bloque) REFERENCES bloque(id_bloque)
) INHERITS(log_replicacion);


CREATE TABLE REP_GENERACION_BLOQUE(
	fk_bloque INTEGER NOT NULL,

	FOREIGN KEY(fk_bloque) REFERENCES bloque(id_bloque)
) INHERITS(log_replicacion);

CREATE TABLE REP_GENERACION_TAREA(
	tarea bytea NOT NULL,
	fk_tarea INTEGER NOT NULL,
	fk_bloque INTEGER NOT NULL,

	FOREIGN KEY(fk_tarea) REFERENCES tarea(id_tarea),
	FOREIGN KEY(fk_bloque) REFERENCES bloque(id_bloque)
) INHERITS(log_replicacion);

CREATE TABLE REP_PARCIAL_TAREA(
	tarea bytea NOT NULL,
	fk_tarea INTEGER NOT NULL,
	fk_bloque INTEGER,
	fk_usuario INTEGER NOT NULL,

	FOREIGN KEY(fk_usuario) REFERENCES usuario(id_usuario),
	FOREIGN KEY(fk_tarea) REFERENCES tarea(id_tarea),
	FOREIGN KEY(fk_bloque) REFERENCES bloque(id_bloque)
) INHERITS(log_replicacion);


CREATE TABLE REP_RESULTADO_TAREA(
	tarea bytea NOT NULL,
	fk_tarea INTEGER NOT NULL,
	fk_bloque INTEGER,
	fk_usuario INTEGER NOT NULL,

	FOREIGN KEY(fk_usuario) REFERENCES usuario(id_usuario),
	FOREIGN KEY(fk_tarea) REFERENCES tarea(id_tarea),
	FOREIGN KEY(fk_bloque) REFERENCES bloque(id_bloque)
) INHERITS(log_replicacion);

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO distribuido;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO distribuido;

