<%@ page contentType="text/html;charset=GBK"%>
<%@page import="java.net.URLDecoder"%>
<%
	/**
	 *����CreateTableJsonDateService
	 *id:�ֶ�Ψһ��ʶ
	 *comments���ֶ�����
	 *name���ֶ���
	 **/
	String id = URLDecoder.decode(request.getParameter("id"), "utf-8");
	String name = URLDecoder.decode(request.getParameter("name"),
			"utf-8");
	out.write("[ { id : 1, name : 'USERID',type : 'VARCHAR2(50)',comments :  '"
			+ name
			+ "ID', },{ id : 2, name : 'USERNAME',type : 'VARCHAR2(50)',comments :  '�û�ȫ��', }, { id : 3, name : 'PASSWORD',type : 'VARCHAR2(50)',comments :  '��½����', }, { id : 4, name : 'DEPT',type : 'VARCHAR2(50)',comments :  '��������', }, { id : 5, name : 'ZW',type : 'VARCHAR2(50)',comments :  'ְλ', }, { id : 6, name : 'ZJLX',type : 'VARCHAR2(50)',comments :  '֤������', }, { id : 7, name : 'ZJHM',type : 'VARCHAR2(50)',comments :  '֤������', }, { id :8, name : 'EMAIL',type : 'VARCHAR2(50)',comments :  '��������', }, { id : 9, name : 'LXDH',type : 'VARCHAR2(50)',comments :  '��ϵ�绰', } ]");
%>
