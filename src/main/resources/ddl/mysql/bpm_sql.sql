create table bpm_delegate_info(
	id integer  NOT NULL AUTO_INCREMENT,
	assignee varchar(200),
	attorney varchar(200),
	start_time timestamp,
	end_time timestamp,
	process_definition_id varchar(100),
	status integer,
	primary key (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;


create table bpm_delegate_history(
	id integer  NOT NULL AUTO_INCREMENT,
	assignee varchar(200),
	attorney varchar(200),
	delegate_time timestamp,
	task_id varchar(100),
	status integer,
	primary key (id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;


