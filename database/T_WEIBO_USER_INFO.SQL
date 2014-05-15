-- Create table
create table T_WEIBO_USER_INFO
(
  userid            VARCHAR2(100) not null,
  screen_name       VARCHAR2(100),
  sex               VARCHAR2(10),
  description       VARCHAR2(200),
  username          VARCHAR2(100),
  follow_num        NUMBER default 0,
  fans_num          NUMBER default 0,
  message_num       NUMBER default 0,
  profile_image_url VARCHAR2(400),
  is_verified       VARCHAR2(10) default 0,
  career_info       VARCHAR2(1000),
  education_info    VARCHAR2(1000),
  tag               VARCHAR2(1000),
  create_time       TIMESTAMP(3),
  daren             VARCHAR2(50) default 0,
  birthday          VARCHAR2(250),
  qq                VARCHAR2(250),
  msn               VARCHAR2(250),
  email             VARCHAR2(250),
  vip               VARCHAR2(50) default 0,
  region            VARCHAR2(250),
  follow_userid     CLOB,
  u_create_time     TIMESTAMP(3),
  update_time       TIMESTAMP(3),
  fans_userid       CLOB,
  dengji            VARCHAR2(50),
  blog              VARCHAR2(1000),
  domain            VARCHAR2(1000),
  verify_info       VARCHAR2(1000),
  msg_existent      VARCHAR2(50)
)
tablespace WEIBO_DATA
  pctfree 10
  initrans 1
  maxtrans 255
  storage
  (
    initial 1M
    next 1M
    minextents 1
    maxextents unlimited
  );
-- Add comments to the table 
comment on table T_WEIBO_USER_INFO
  is '微博用户信息';
-- Add comments to the columns 
comment on column T_WEIBO_USER_INFO.userid
  is '主键';
comment on column T_WEIBO_USER_INFO.screen_name
  is '屏幕名';
comment on column T_WEIBO_USER_INFO.sex
  is '性别';
comment on column T_WEIBO_USER_INFO.description
  is '用户自我描述信息';
comment on column T_WEIBO_USER_INFO.username
  is '用户名';
comment on column T_WEIBO_USER_INFO.follow_num
  is '关注数量 默认0';
comment on column T_WEIBO_USER_INFO.fans_num
  is '粉丝数量 默认0';
comment on column T_WEIBO_USER_INFO.message_num
  is '消息数量 默认0';
comment on column T_WEIBO_USER_INFO.profile_image_url
  is '头像URL 最短标识字符串';
comment on column T_WEIBO_USER_INFO.is_verified
  is '0 普通用户 1认证用户';
comment on column T_WEIBO_USER_INFO.career_info
  is '职业信息';
comment on column T_WEIBO_USER_INFO.education_info
  is '教育信息';
comment on column T_WEIBO_USER_INFO.tag
  is '用户标签，用","号分隔.';
comment on column T_WEIBO_USER_INFO.daren
  is '是否达人';
comment on column T_WEIBO_USER_INFO.birthday
  is '生日';
comment on column T_WEIBO_USER_INFO.qq
  is 'QQ';
comment on column T_WEIBO_USER_INFO.msn
  is 'MSN';
comment on column T_WEIBO_USER_INFO.email
  is 'EMAIL';
comment on column T_WEIBO_USER_INFO.vip
  is '是否会员';
comment on column T_WEIBO_USER_INFO.region
  is '地区';
comment on column T_WEIBO_USER_INFO.follow_userid
  is '关注列表';
comment on column T_WEIBO_USER_INFO.u_create_time
  is '用户创建时间';
comment on column T_WEIBO_USER_INFO.fans_userid
  is '粉丝列表';
comment on column T_WEIBO_USER_INFO.dengji
  is '等级';
comment on column T_WEIBO_USER_INFO.blog
  is '博客';
comment on column T_WEIBO_USER_INFO.domain
  is '个性域名';
comment on column T_WEIBO_USER_INFO.verify_info
  is '认证信息';
-- Create/Recreate primary, unique and foreign key constraints 
alter table T_WEIBO_USER_INFO
  add constraint IDPK primary key (USERID)
  using index 
  tablespace WEIBO_DATA
  pctfree 10
  initrans 2
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
