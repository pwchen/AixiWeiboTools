create table T_WEIBO_USER_FILTERLIST
(
  userid NVARCHAR2(100) not null,
  type   NVARCHAR2(1000)
)
tablespace WEIBO_DATA
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    minextents 1
    maxextents unlimited
  );
-- Create/Recreate primary, unique and foreign key constraints 
alter table T_WEIBO_USER_FILTERLIST
  add constraint USERID_KEY primary key (USERID)
  using index 
  tablespace WEIBO_DATA
  pctfree 10
  initrans 2
  maxtrans 255;
