insert into usuario (id_usuario,nombre,contrasenia,puntos) values (1,'usuario','usuario123',0);
insert into usuario (id_usuario, nombre, contrasenia, puntos) values (2, 'lucas', 'mufato', 0);
insert into usuario (id_usuario, nombre, contrasenia, puntos) values (3, 'jasmin', 'paolino', 0);
insert into usuario (id_usuario, nombre, contrasenia, puntos) values (4, 'pablo', 'cabrera', 0);
insert into usuario (id_usuario, nombre, contrasenia, puntos) values (5, 'luz', 'barcena', 0);

/* Estados en los que puede estar una tarea */
insert into estado_tarea (estado) values 
	('pendiente'),
	('en proceso'),
	('detenida'),
	('completada');

/* Estados en los que puede estar un bloque */
insert into estado_bloque (estado) values 
	('pendiente'),
	('en proceso'),
	('completado');

/*	Tipos de mensajes guardados en el log de replicacion, tiene el mismo orden que el ENUM en java*/

INSERT INTO tipo_mensaje(id_tipo_mensaje, nombre) VALUES
	(1, 'parcialTarea'),
	(2, 'resultadoTarea'),
	(3, 'completitudBloque'),
	(4, 'asignacionTareaUsuario'),
	(5, 'detencionTarea'),
	(6, 'asignacionPuntos'),
	(7, 'generacionBloque'),
	(8, 'generacionTarea');
