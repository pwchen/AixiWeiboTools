<%@ page contentType='text/html;charset=GBK'%>
<%
	/**
	 *调用CreateTableJsonDateService
	 *id类别或者表ID
	 *pid父类别ID
	 *name类别或者表名
	 *comments类别或表的描述
	 *nocheck:如果为文件夹true
	 **/
	out.write("[ { id : 1, pId : 0, name : '系统目录', comments : '系统目录' ,open:true}, { id : 11, pId : 1, name : '用户表', comments : '用户表',open:true }, { id : 111, pId : 11, name : 'SystemUserTable', comments :'系统管理员' }, { id : 112, pId : 11, name : 'Customer', comments : '客户' }, { id: 113, pId : 11, name : 'User', comments : '用户' }, { id : 12, pId : 1, name :'功能', comments : '功能', }, { id : 121, pId :12, name : '节点121', comments : '节点121' }, { id : 122, pId : 12, name : '节点122',	comments : '节点122' }, { id : 123, pId : 12, name : '节点123', comments :'节点123' }, { id : 2, pId : 0, name : '广州药监', comments : '广州药监'}, { id : 21, pId : 2, name : '药品监管', comments : '药品监管' ,'nocheck':true}, { id : 211, pId : 21, name : 'YPQY', comments : '药品企业'}, { id : 212, pId : 21, name : 'YPJBXX', comments : '药品基本信息' }, { id : 213, pId: 21, name : 'YPXSXX', comments : '药品销售信息' }, { id : 22, pId : 2, name : '器械监管',	comments : '器械监管'}, { id : 221, pId : 22, name : 'QXQY', comments: '器械企业' }, { id : 222, pId : 22, name : 'QXJCXX', comments : '器械基础信息' }, { id : 223,pId : 22, name : 'QXXSXX', comments : '器械销售信息' } ]");
%>
