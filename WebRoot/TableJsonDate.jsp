<%@ page contentType="text/html;charset=GBK"%>
<%@page import="java.net.URLDecoder"%>
<%
	/**
	 *����CreateTableJsonDateService
	 *id:�ֶ�Ψһ��ʶ
	 *comments���ֶ�����
	 *name���ֶ���
	 **/
	String tableName = URLDecoder.decode(
			request.getParameter("tableName"), "utf-8");
	out.write("[{columnName:'screenName',inputType:'String',comments:'��Ļ��'},"
			+ "{columnName:'sex',inputType:'String',comments:'�Ա�'},"
			+ "{columnName:'description',inputType:'String',comments:'�û�������Ϣ'},"
			+ "{columnName:'userName',inputType:'String',comments:'�û���'},"
			+ "{columnName:'followNum',inputType:'int',comments:'��ע���� Ĭ��0'},"
			+ "{columnName:'fansNum',inputType:'int',comments:'��˿���� Ĭ��0'},"
			+ "{columnName:'messageNum',inputType:'int',comments:'��Ϣ���� Ĭ��0'},"
			+ "{columnName:'profileImageUrl',inputType:'String',comments:'ͷ��URL'},"
			+ "{columnName:'isVerified',inputType:'String',comments:'0 ��ͨ�û� 1��֤�û�'},"
			+ "{columnName:'careerInfo',inputType:'String',comments:'ְҵ��Ϣ'},"
			+ "{columnName:'educationInfo',inputType:'String',comments:'������Ϣ'},"
			+ "{columnName:'tag',inputType:'String',comments:'�û���ǩ'},"
			+ "{columnName:'daren',inputType:'String',comments:'�Ƿ����'},"
			+ "{columnName:'birthday',inputType:'String',comments:'����'},"
			+ "{columnName:'qq',inputType:'String',comments:'QQ'},"
			+ "{columnName:'msn',inputType:'String',comments:'MSN'},"
			+ "{columnName:'email',inputType:'String',comments:'EMAIL'},"
			+ "{columnName:'vip',inputType:'String',comments:'�Ƿ��Ա'},"
			+ "{columnName:'region',inputType:'String',comments:'����'},"
			+ "{columnName:'followUserId',inputType:'String',comments:'��ע�б�'},"
			+ "{columnName:'uCreateTime',inputType:'Timestamp',comments:'�û�����ʱ��'},"
			+ "{columnName:'fansUserId',inputType:'String',comments:'��˿�б�'},"
			+ "{columnName:'dengji',inputType:'String',comments:'�ȼ�'},"
			+ "{columnName:'blog',inputType:'String',comments:'����'},"
			+ "{columnName:'domain',inputType:'String',comments:'��������'},"
			+ "{columnName:'verifyInfo',inputType:'String',comments:'��֤��Ϣ'}]");
%>
