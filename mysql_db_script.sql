create table campus.out_box_table
(
	id         bigint auto_increment primary key,
	message    text                 not null,
	send       tinyint(1) default 0 not null,
	created_at datetime             not null
);
