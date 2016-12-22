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
