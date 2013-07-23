create table bpm_delegate_info(
	id tinyint IDENTITY(1,1) NOT NULL,
	assignee nvarchar(200),
	attorney nvarchar(200),
	start_time datetime,
	end_time datetime,
	process_definition_id nvarchar(100),
	status tinyint,
	primary key (id)
);


create table bpm_delegate_history(
	id tinyint IDENTITY(1,1) NOT NULL,
	assignee nvarchar(200),
	attorney nvarchar(200),
	delegate_time datetime,
	task_id nvarchar(100),
	status tinyint,
	primary key (id)
);


