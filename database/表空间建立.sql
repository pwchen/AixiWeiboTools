create temporary tablespace weibo_temp
tempfile 'D:\oracle\oradata\weibo\weibo_temp01.dbf'
size 32m
autoextend on
next 32m maxsize 2048m
extent management local;

create tablespace weibo_data
logging
datafile 'D:\oracle\oradata\weibo\weibo_data01.dbf'
size 32m
autoextend on
next 32m maxsize 4096m
extent management local;


alter user aixi0 identified by aixi0
default tablespace weibo_data
temporary tablespace weibo_temp;

alter user aixi1 identified by aixi1
default tablespace weibo_data
temporary tablespace weibo_temp;
