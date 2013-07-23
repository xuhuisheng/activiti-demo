create table bpm_delegate_info(
	id number primary key,
	assignee varchar(200),
	attorney varchar(200),
	start_time timestamp,
	end_time timestamp,
	process_definition_id varchar(100),
	status number
);


create table bpm_delegate_history(
	id number primary key,
	assignee varchar(200),
	attorney varchar(200),
	delegate_time timestamp,
	task_id varchar(100),
	status number
);

CREATE SEQUENCE INDEX_bpm_delegate_info_SEQ INCREMENT BY 1  START WITH 1 NOMAXVALUE NOCYCLE CACHE 10; 
     
create or replace trigger bpm_delegate_info_ID_trigger BEFORE
insert ON  bpm_delegate_info FOR EACH ROW
begin
	select INDEX_bpm_delegate_info_SEQ.nextval into:New.id from dual;
end;

CREATE SEQUENCE INDEX_bpm_delegate_history_SEQ INCREMENT BY 1  START WITH 1 NOMAXVALUE NOCYCLE CACHE 10; 
     
create or replace trigger bpm_delegate_his_ID_trigger BEFORE
insert ON  bpm_delegate_history FOR EACH ROW
begin
	select INDEX_bpm_delegate_history_SEQ.nextval into:New.id from dual;
end;

