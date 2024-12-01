create table campus.out_box_table
(
	id         bigint auto_increment primary key,
	message    text                 not null,
	send       tinyint(1) default 0 not null,
	created_at datetime             not null
);

-- MySQL에서 binlog가 활성화되어 있는지 확인
SHOW VARIABLES LIKE 'log_bin';

-- binlog 포맷 확인 (ROW 형식이어야 함)
SHOW VARIABLES LIKE 'binlog_format';

-- 디비지움 권한 계정 생성
CREATE USER 'debezium'@'%' IDENTIFIED BY 'dbz123';
GRANT SELECT, RELOAD, SHOW DATABASES, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'debezium'@'%';
FLUSH PRIVILEGES;

-- 권한 확인
SHOW GRANTS FOR 'debezium'@'%';
