<%@ page contentType="text/html;charset=GBK"%>
<%@page import="java.net.URLDecoder"%>
<%
	/**
	 *调用CreateTableJsonDateService
	 *id:字段唯一标识
	 *comments：字段描述
	 *name：字段名
	 **/
	String tableName = URLDecoder.decode(
			request.getParameter("tableName"), "utf-8");
	out.write("[{columnName:'screenName',inputType:'String',comments:'屏幕名'},"
			+ "{columnName:'sex',inputType:'String',comments:'性别'},"
			+ "{columnName:'description',inputType:'String',comments:'用户描述信息'},"
			+ "{columnName:'userName',inputType:'String',comments:'用户名'},"
			+ "{columnName:'followNum',inputType:'int',comments:'关注数量 默认0'},"
			+ "{columnName:'fansNum',inputType:'int',comments:'粉丝数量 默认0'},"
			+ "{columnName:'messageNum',inputType:'int',comments:'消息数量 默认0'},"
			+ "{columnName:'profileImageUrl',inputType:'String',comments:'头像URL'},"
			+ "{columnName:'isVerified',inputType:'String',comments:'0 普通用户 1认证用户'},"
			+ "{columnName:'careerInfo',inputType:'String',comments:'职业信息'},"
			+ "{columnName:'educationInfo',inputType:'String',comments:'教育信息'},"
			+ "{columnName:'tag',inputType:'String',comments:'用户标签'},"
			+ "{columnName:'daren',inputType:'String',comments:'是否达人'},"
			+ "{columnName:'birthday',inputType:'String',comments:'生日'},"
			+ "{columnName:'qq',inputType:'String',comments:'QQ'},"
			+ "{columnName:'msn',inputType:'String',comments:'MSN'},"
			+ "{columnName:'email',inputType:'String',comments:'EMAIL'},"
			+ "{columnName:'vip',inputType:'String',comments:'是否会员'},"
			+ "{columnName:'region',inputType:'String',comments:'地区'},"
			+ "{columnName:'followUserId',inputType:'String',comments:'关注列表'},"
			+ "{columnName:'uCreateTime',inputType:'Timestamp',comments:'用户创建时间'},"
			+ "{columnName:'fansUserId',inputType:'String',comments:'粉丝列表'},"
			+ "{columnName:'dengji',inputType:'String',comments:'等级'},"
			+ "{columnName:'blog',inputType:'String',comments:'博客'},"
			+ "{columnName:'domain',inputType:'String',comments:'个性域名'},"
			+ "{columnName:'verifyInfo',inputType:'String',comments:'认证信息'}]");
%>
