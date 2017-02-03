insert into usuario (id_usuario,nombre,contrasenia,puntos) values (1,'usuario','usuario123',0);
insert into usuario (id_usuario, nombre, contrasenia, puntos) values (2, 'usuario2', 'usuario123', 0);
insert into usuario (id_usuario, nombre, contrasenia, puntos) values (3, 'usuario3', 'usuario123', 0);
insert into usuario (id_usuario, nombre, contrasenia, puntos) values (4, 'usuario4', 'usuario123', 0);

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

/*
para probar la herencia.

Insert into log_replicacion(tipo_mensaje) values (1),(2);


insert into rep_asignacion_tarea_usuario (tipo_mensaje,fk_tarea,fk_usuario,fk_procesamiento_tarea) values (4,1,1,1);

select * from log_replicacion;
select * from rep_asignacion_tarea_usuario;
*/
