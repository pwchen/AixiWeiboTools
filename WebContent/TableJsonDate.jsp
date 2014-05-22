<%@ page contentType="text/html;charset=GBK"%>
<%@page import="java.net.URLDecoder"%>
<%
	/**
	 *调用CreateTableJsonDateService
	 *id:字段唯一标识
	 *comments：字段描述
	 *name：字段名
	 **/
	String id = URLDecoder.decode(request.getParameter("id"), "utf-8");
	String name = URLDecoder.decode(request.getParameter("name"),
			"utf-8");
	out.write("[ { id : 1, name : 'USERID',type : 'VARCHAR2(50)',comments :  '"
			+ name
			+ "ID', },{ id : 2, name : 'USERNAME',type : 'VARCHAR2(50)',comments :  '用户全名', }, { id : 3, name : 'PASSWORD',type : 'VARCHAR2(50)',comments :  '登陆密码', }, { id : 4, name : 'DEPT',type : 'VARCHAR2(50)',comments :  '所属部门', }, { id : 5, name : 'ZW',type : 'VARCHAR2(50)',comments :  '职位', }, { id : 6, name : 'ZJLX',type : 'VARCHAR2(50)',comments :  '证件类型', }, { id : 7, name : 'ZJHM',type : 'VARCHAR2(50)',comments :  '证件号码', }, { id :8, name : 'EMAIL',type : 'VARCHAR2(50)',comments :  '电子邮箱', }, { id : 9, name : 'LXDH',type : 'VARCHAR2(50)',comments :  '联系电话', } ]");
%>
