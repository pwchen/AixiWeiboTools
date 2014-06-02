/**
 * �Ѿ�������ֶ�
 * 
 * @param fieldName
 *            �ֶ���
 * @param fieldComments
 *            �ֶ�����
 * @param fieldAlias
 *            �ֶα���
 */
var sFields = new Array();

/**
 * ��ȡ����û�ID�б�
 * 
 * @param userId
 *            �û�ID
 * @param depth
 *            �û�ID
 */
var sUsers = new Array();

/**
 * ��ѡ��Ĺ��˹����б�
 * 
 */
var sFilters = new Array();

/**
 * Map����
 * 
 * @param key
 * @param value
 */
function Map() {
	var struct = function(key, value) {
		this.key = key;
		this.value = value;
	};

	var put = function(key, value) {
		for (var i = 0; i < this.arr.length; i++) {
			if (this.arr[i].key == key) {
				this.arr[i].value = value;
				return;
			}
		}
		this.arr[this.arr.length] = new struct(key, value);
	};

	var get = function(key) {
		for (var i = 0; i < this.arr.length; i++) {
			if (this.arr[i].key == key) {
				return this.arr[i].value;
			}
		}
		return null;
	};

	var remove = function(key) {
		var v;
		for (var i = 0; i < this.arr.length; i++) {
			v = this.arr.pop();
			if (v.key == key) {
				continue;
			}
			this.arr.unshift(v);
		}
	};

	var size = function() {
		return this.arr.length;
	};

	var isEmpty = function() {
		return this.arr.length <= 0;
	};
	this.arr = new Array();
	this.get = get;
	this.put = put;
	this.remove = remove;
	this.size = size;
	this.isEmpty = isEmpty;
}

/**
 * ��ʼ��
 */
$(function() {

	/*
	 * $.ajax({ url : "test.html",// "ZtreeJsonDate.jsp", type : "post", data : {
	 * tableName : "TableJsonDate.jsp" }, datatype : "Json", success :
	 * function(data) { var fieldsdata = eval("(" + data + ")");
	 * initsFields(fieldsdata); }, error : function(xhr, data, ts) {
	 * alert("δ���ӵ����������޷���ȡ����Ϣ��"); } });
	 */

	var fieldsdata = eval("("
			+ "[{columnName:'screenName',inputType:'String',comments:'��Ļ��'},"
			+ "{columnName:'sex',inputType:'String',comments:'�Ա�'},"
			+ "{columnName:'description',inputType:'String',comments:'�û�������Ϣ'},"
			+ "{columnName:'userName',inputType:'String',comments:'�û���'},"
			+ "{columnName:'followNum',inputType:'int',comments:'��ע����'},"
			+ "{columnName:'fansNum',inputType:'int',comments:'��˿���� '},"
			+ "{columnName:'messageNum',inputType:'int',comments:'��Ϣ����'},"
			+ "{columnName:'profileImageUrl',inputType:'String',comments:'ͷ��URL'},"
			+ "{columnName:'isVerified',inputType:'String',comments:'�Ƿ���֤0/1'},"
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
			+ "{columnName:'verifyInfo',inputType:'String',comments:'��֤��Ϣ'}]"
			+ ")");
	initsFields(fieldsdata);
	/**
	 * ��ʼ������UI button
	 */
	$(".mybutton").button();

	$("#sTabs").tabs({
		collapsible : true
	});

});

/**
 * �����û����ֶ�����
 * 
 * @param fieldsdata
 */
function initsFields(fieldsdata) {
	for (var i = 0; i < fieldsdata.length; i++) {
		var fieldMap = new Map;
		fieldMap.put("columnName", fieldsdata[i].columnName);
		fieldMap.put("inputType", fieldsdata[i].inputType);
		fieldMap.put("comments", fieldsdata[i].comments);
		sFields[sFields.length] = fieldMap;
	}
}

/**
 * ��ȡ�����Ϣ
 */
function loadFields(name) {
	var field = null;
	$.ajax({
		url : "loadFields",// ��ȡ�����Ϣ��̨servlet
		type : "post",
		async : false,
		data : {
			name : encodeURI(name)
		},
		datatype : "Json",
		success : function(data) {
			field = eval("(" + data + ")");
		},
		error : function(xhr, data, ts) {
			alert(data);
		}
	});
	if (field != null) {
		return field;
	} else {
		return null;
	}
}

/** **************************************�������û�***************************** */

/**
 * ͨ���ļ������û���ȡ���
 */
function addUserFromFile() {
	var fileName = $("#fileName").val();
	if (fileName == "") {
		alert("����ѡ���ļ���");
		return;
	}
	$("#fileName").val("");
	if (fileName.indexOf(".csv", 0) == -1 && fileName.indexOf(".CSV", 0) == -1) {
		alert("�ļ���ʽ����~����֧��csv��ʽ���ݣ�");
		return;
	}
	var text = readFile(fileName);
	var strs = text.split(/\n/g);
	for (var i = 0; i < strs.length; i++) {
		var us = strs[i].split(",");
		if (us == "") {
		} else if (us.length != 2) {
			alert("��" + (i + 1) + "���ݸ�ʽ����");
		} else {
			addUserTosUsers(us[0], us[1]);
		}
	}
}
/**
 * ���user��user�б���
 * 
 * @param userId
 * @param depth
 */
function addUserTosUsers(userId, depth) {
	if (checkUserInSUsers(userId, depth)) {
		return false;
	}
	if (userId == "") {
		alert(userId + ":" + depth + "�û�ID����Ϊ�գ�");
		return false;
	}
	if (depth == "") {
		alert(userId + ":" + depth + "��ȡ��Ȳ���Ϊ�գ�");
		return false;
	} else if (isNaN(depth)) {
		alert(userId + ":" + depth + "��ȡ��ȱ���Ϊ���֣�");
		return false;
	}
	var userMap = new Map();
	userMap.put("userId", userId);// depth
	userMap.put("depth", depth);// depth
	sUsers[sUsers.length] = userMap;
	refreshsUsersDisplay();
}
/**
 * ���ļ�
 */
function readFile(fileName) {
	var fso = new ActiveXObject("Scripting.FileSystemObject");
	var f = fso.OpenTextFile(fileName, 1);
	var s = "";
	while (!f.AtEndOfStream)
		s += f.ReadLine() + "\n";
	f.Close();
	return s;
}

/**
 * ������û�dialog
 */
function openaddcrawleuserdialog() {
	$("#add_userId").val("");
	$("#addcrawleuserdiv").dialog(
			{
				autoOpen : true,
				width : 250,
				height : 160,
				resizable : false,
				buttons : [
						{
							text : "����",
							click : function() {
								var userId = $("#add_userId").val().replace(
										/\s+/g, "");
								$("#add_userId").val(userId);
								var depth = $("#add_depth").val().replace(
										/\s+/g, "");
								$("#add_depth").val(depth);
								if (userId == "") {
									alert("�û�ID����Ϊ�գ�");
									return false;
								}
								if (depth == "") {
									alert("��ȡ��Ȳ���Ϊ�գ�");
									return false;
								} else if (isNaN(depth)) {
									alert("��ȡ��ȱ���Ϊ���֣�");
									return false;
								}
								if (checkUserInSUsers(userId, depth)) {
									return false;
								}
								var userMap = new Map();
								userMap.put("userId", userId);
								userMap.put("depth", depth);
								sUsers[sUsers.length] = userMap;
								alert("�û�"
										+ sUsers[sUsers.length - 1]
												.get("userId")
										+ "��"
										+ sUsers[sUsers.length - 1]
												.get("depth") + " �ѱ��棡�û�����:"
										+ sUsers.length);
								refreshsUsersDisplay();
								$(this).dialog("close");
							}
						}, {
							text : "�ر�",
							click : function() {
								$(this).dialog("close");
							}
						} ],
			});
}

/**
 * ����������
 * 
 * @param no
 */
function deleteUser(no) {
	if (window.confirm('��ȷ��Ҫ�Ӷ�����ɾ�����û���')) {
		var arrlen = sUsers.length;
		for (var i = arrlen - 1; i >= 0; i--) {
			var v = sUsers.pop();
			if (i == no) {
				continue;
			}
			sUsers.unshift(v);
		}
		refreshsUsersDisplay();
	}
}

/**
 * �����û�Id�����ɾ���û�
 * 
 * @param userId
 *            �Ñ�id
 * @param depth
 *            ��ȡ���
 */
function removeFromSUsers(userId, depth) {
	var arrlen = sUsers.length;
	if (userId != null && depth != null) {
		for (var i = 0; i < arrlen; i++) {
			var v = sUsers.pop();
			if (v.get("userId") == userId && v.get("depth") == depth) {
				continue;
			}
			sUsers.unshift(v);
		}
	}
	refreshsUsersDisplay();
}

/**
 * ����û���
 * 
 * @param fileName
 */
function clearUser() {
	if (window.confirm('��ȷ��Ҫɾ�������û���')) {
		sUsers = new Array();
		refreshsUsersDisplay();
	}
}

/**
 * �����û��Ƿ����û������б���,���ڷ���true
 * 
 * @param userId
 * @param depth
 */
function checkUserInSUsers(userId, depth) {
	var arrlen = sUsers.length;
	if (userId != null && depth != null) {
		for (var i = 0; i < arrlen; i++) {
			if (sUsers[i].get("userId") == userId
					&& sUsers[i].get("depth") == depth) {
				alert("�û�" + userId + "��" + depth + " �Ѵ����û��б��У�");
				return true;
			}
		}
	}
	return false;
}

/**
 * ����������ʾ�ֶ�
 * 
 * @param direction
 *            0�� 1��
 * @param no
 *            sFields�е����
 */
function resortSUsers(direction, no) {
	var temp = sUsers[no];
	if (direction == 0) {
		if (no == (sUsers.length - 1)) {
			alert("�Ѿ����������ˣ�");
		} else {
			sUsers[no] = sUsers[no + 1];
			sUsers[no + 1] = temp;
		}
	} else {
		if (no == 0) {
			alert("�Ѿ����������ˣ�");
		} else {
			sUsers[no] = sUsers[no - 1];
			sUsers[no - 1] = temp;
		}
	}
	refreshsUsersDisplay();
}

/**
 * �༭��ʾ�ֶ�
 */
function editSUsers(no) {
	var userId = sUsers[no].get("userId");
	var depth = sUsers[no].get("depth");
	$("#edit_userId").val(userId);
	$("#edit_depth").val(depth);
	$("#editcrawleuserdiv").dialog(
			{
				autoOpen : true,
				width : 250,
				height : 160,
				resizable : false,
				buttons : [
						{
							text : "����",
							click : function() {
								var userId = $("#edit_userId").val().replace(
										/\s+/g, "");
								$("#userId").val(userId);
								var depth = $("#edit_depth").val().replace(
										/\s+/g, "");
								$("#depth").val(depth);
								if (userId == "") {
									alert("�û�ID����Ϊ�գ�");
									return false;
								}
								if (depth == "") {
									alert("��ȡ��Ȳ���Ϊ�գ�");
									return false;
								} else if (isNaN(depth)) {
									alert("��ȡ��ȱ���Ϊ���֣�");
									return false;
								}
								if (checkUserInSUsers(userId, depth)) {
									return false;
								}
								sUsers[no].put("userId", userId);
								sUsers[no].put("depth", depth);
								alert("�û�" + userId + "��" + depth
										+ " �ѱ��棡�û�����:" + sUsers.length);
								refreshsUsersDisplay();
								$(this).dialog("close");
							}
						}, {
							text : "�ر�",
							click : function() {
								$(this).dialog("close");
							}
						} ],
			});
}

/**
 * ˢ����ѡ����ʾ�ֶ�
 */
function refreshsUsersDisplay() {
	$("#sUsersTable").empty();
	if (sUsers.length < 1) {
		return;
	}
	$("#sUsersTable")
			.append(
					"<tr class='list_table_thead_tr_title'><th width='3%'>���</th><th width='8%'>�û�ID</th><th width='10%'>��ȡ���</th><th width='6%'>����</th></tr>");
	for (var i = 0; i < sUsers.length; i++) {
		var userId = sUsers[i].get("userId");
		var depth = sUsers[i].get("depth");
		var trStr = "";
		trStr += "<tr class='list_table_tbody_tr_p' onmouseover=\"this.style.backgroundColor='#BD66ee';\" onmouseout=\"this.style.backgroundColor='#BDDFFF';\"><td class='list_table_tbody_td'>"
				+ (i + 1)
				+ "</td><td class='list_table_tbody_td'>"
				+ userId
				+ "</td><td class='list_table_tbody_td'>"
				+ depth
				+ "</td><td class='list_table_tbody_td'><a href='#tab-1' onclick='editSUsers("
				+ i
				+ ")'><img border='0' title='�޸�' src='./images/edit.gif'></a><a href='#tab-1' onclick='resortSUsers(1,"
				+ i
				+ ")'><img border='0' title='����' src='./images/up.gif'></a><a href='#tab-1' onclick='resortSUsers(0,"
				+ i
				+ ")'><img border='0' title='����' src='./images/down.gif'></a><a href='#tab-2' onclick='deleteUser("
				+ i
				+ ")'><img border='0' title='ɾ��' src='./images/delete.gif'></td></tr>";
		$("#sUsersTable").append(trStr);
	}

}

/**
 * �ύ���˹���
 */
function submitUsers() {
	var text = "[";
	for (var i = 0; i < sUsers.length; i++) {
		var userjson = "{";
		userjson += "userId:'" + sUsers[i].get("userId") + "'";
		userjson += ",depth:" + sUsers[i].get("depth") + "},";
		text += userjson;
	}
	text = text.substring(0, text.length) + "]";
	$.ajax({
		type : "post",
		url : "crawler",
		dataType : "text",
		data : {
			type : "task",
			data : text
		},
		success : function(msg) {
			alert(msg);
		}, // �����ɹ���Ĳ�����msg�Ǻ�̨��������ֵ
		error : function(msg) {
			alert("error! " + msg);
		} // �����ɹ�
	});
}
/** **************************************��������***************************** */

/**
 * ��ʼ����ӹ�������dialog
 */
function initaddfiltersdialog() {
	$("#addfieldselect").empty();
	$("#addoperationselect").hide();
	$("#addmatchselect").hide();
	$("#addcomments").text("");
	$("#addinputtype").text("");
	$("<option value='-1'>��ѡ��........</option>").appendTo("#addfieldselect");
	for (var i = 0; i < sFields.length; i++) {
		$(
				"<option value='" + i + "'>" + sFields[i].get("comments")
						+ "</option>").appendTo("#addfieldselect");
	}
	$("#addfiltersdiv").dialog({
		autoOpen : true,
		width : 700,
		height : 150,
		resizable : false,
		buttons : [ {
			text : "����",
			click : function() {
				var selectedId = $("#addfieldselect option:selected").val();
				if (selectedId == -1) {
					alert("��ѡ��һ���ֶ�!");
					return false;
				}
				var columnName = sFields[selectedId].get("columnName");
				var comments = sFields[selectedId].get("comments");
				var inputType = sFields[selectedId].get("inputType");
				var matchMode = $("#addmatchselect option:selected").val();
				var operation = $("#addoperationselect option:selected").val();
				var inputValue = $("#addinputvalue").val();
				if (inputValue == "") {
					alert("������ֵ!");
					return false;
				}
				var filterMap = new Map();
				filterMap.put("columnName", columnName);
				filterMap.put("comments", comments);
				filterMap.put("inputType", inputType);
				filterMap.put("matchMode", matchMode);
				filterMap.put("operation", operation);
				filterMap.put("inputValue", inputValue);
				sFilters[sFilters.length] = filterMap;
				$(this).dialog("close");
				alert("��ӳɹ�!" + sFilters.length);
				refreshSFiltersDisplay();
			}
		}, {
			text : "�ر�",
			click : function() {
				$(this).dialog("close");
			}
		} ]
	});
}

/**
 * ��ӹ���������ѡ���ֶ�����
 * 
 * @param sel
 */
function changfieldselect(sel) {
	var selectedId = sel.options[sel.selectedIndex].value;
	if (selectedId == -1) {
		$("#addcomments").text("");
		$("#addinputtype").text("");
		$("#addoperationselect").hide();
		$("#addmatchselect").hide();
		return false;
	}
	var columnName = sFields[selectedId].get("columnName");
	var inputType = sFields[selectedId].get("inputType");
	$("#addcomments").text(columnName);
	$("#addinputtype").text(inputType);
	if (inputType == "Timestamp") {
		$("#adddateformtext").show();
	} else {
		$("#adddateformtext").hide();
	}
	if (inputType == "String") {
		$("#addoperationselect").empty();
		$("#addoperationselect").show();
		$("#addmatchselect").empty();
		$("#addmatchselect").show();
		$("<option value='like'>like</option>").appendTo("#addoperationselect");
		$(
				"<option  title='ģ��ƥ��' value='fuzzy' selected='selected'>fuzzy</option><option value='left'>left</option><option value='right'>right</option><option value='middle'>middle</option><option value='full'>full</option>")
				.appendTo("#addmatchselect");
		$("#addmatchselect").focus();
	} else {
		$("#addoperationselect").empty();
		$("#addoperationselect").show();
		$("#addmatchselect").hide();
		$(
				"<option	value='=' selected='selected'>&#61;</option><option value='>'>&#62;</option><option value='<'>&#60;</option>")
				.appendTo("#addoperationselect");
		$("#addoperationselect").focus();
	}

}

/**
 * �༭��������
 * 
 * @param index
 */
function editSFilters(index) {
	var columnName = sFilters[index].get("columnName");
	var comments = sFilters[index].get("comments");
	var inputType = sFilters[index].get("inputType");
	var operation = sFilters[index].get("operation");
	var matchMode = sFilters[index].get("matchMode");
	var inputValue = sFilters[index].get("inputValue");
	$("#editcolumnname").text(columnName);
	$("#editcomments").text(comments);
	$("#editinputtype").text(inputType);
	$("#editinputvalue").val(inputValue);

	if (inputType == "Timestamp") {
		$("#editdateformtext").show();
	} else {
		$("#editdateformtext").hide();
	}
	if (inputType == "String") {
		$("#editoperationselect").empty();
		$("#editoperationselect").show();
		$("#editmatchselect").empty();
		$("#editmatchselect").show();
		$("<option value='like'>like</option>")
				.appendTo("#editoperationselect");
		$(
				"<option  title='ģ��ƥ��' value='fuzzy'>fuzzy</option><option value='left'>left</option><option value='right'>right</option><option value='middle'>middle</option><option value='full'>full</option>")
				.appendTo("#editmatchselect");
		$("#editmatchselect").val(matchMode);
		$("#editmatchselect").focus();
	} else {
		$("#editoperationselect").empty();
		$("#editoperationselect").show();
		$("#editmatchselect").hide();
		$(
				"<option	value='=' selected='selected'>&#61;</option><option value='>'>&#62;</option><option value='<'>&#60;</option>")
				.appendTo("#editoperationselect");
		$("#editoperationselect").val(operation);
		$("#editoperationselect").focus();
	}
	$("#editfiltersdiv").dialog(
			{
				autoOpen : true,
				width : 700,
				height : 150,
				resizable : false,
				buttons : [
						{
							text : "����",
							click : function() {
								var matchMode = $(
										"#editmatchselect option:selected")
										.val();
								var operation = $(
										"#editoperationselect option:selected")
										.val();
								var inputValue = $("#editinputvalue").val();
								if (inputValue == "") {
									alert("������ֵ!");
									return false;
								}
								sFilters[index].put("matchMode", matchMode);
								sFilters[index].put("operation", operation);
								sFilters[index].put("inputValue", inputValue);
								refreshSFiltersDisplay();
								$(this).dialog("close");
								alert("�ѱ��棡");
							}
						}, {
							text : "�ر�",
							click : function() {
								$(this).dialog("close");
							}
						} ],
			});
}

/**
 * ��sFilters����ɾ����������
 * 
 * @param index
 */
function deleteSFilters(index) {
	if (!confirm("ȷ��ɾ���˹��˹���")) {
		return;
	}
	for (var i = index; i < sFilters.length; i++) {
		sFilters[i] = sFilters[i + 1];
	}
	sFilters.pop();
	refreshSFiltersDisplay();
}

/**
 * ����IDɾ����������
 */
function removeSFilters(tableId) {
	var arrlen = sFilters.length;
	for (var i = 0; i < arrlen; i++) {
		var v = sFilters.pop();
		if (v.get("tableId") == tableId) {
			continue;
		}
		sFilters.unshift(v);
	}
}

/**
 * ˢ�¹�������
 */
function refreshSFiltersDisplay() {
	$("#sFiltersTable").empty();
	if (sFilters.length < 1) {
		return;
	}
	$("#sFiltersTable")
			.append(
					"<tr class='list_table_thead_tr_title'><th width='3%'>���</th><th width='10%'>��ǩ</th><th width='10%'>�ֶ���</th><th width='10%'>��������</th><th>������</th><th >ƥ��ģʽ</th><th>����ֵ</th><th width='6%'>����</th></tr>");
	for (var i = 0; i < sFilters.length; i++) {
		var comments = sFilters[i].get("comments");
		var columnName = sFilters[i].get("columnName");
		var inputType = sFilters[i].get("inputType");
		var operation = sFilters[i].get("operation");
		var matchMode = sFilters[i].get("matchMode");
		var inputValue = sFilters[i].get("inputValue");
		var trStr = "<tr class='list_table_tbody_tr_p' onmouseover=\"this.style.backgroundColor='#BD66ee';\" onmouseout=\"this.style.backgroundColor='#BDDFFF';\"><td class='list_table_tbody_td'>"
				+ (i + 1)
				+ "</td><td class='list_table_tbody_td'>"
				+ comments
				+ "</td><td class='list_table_tbody_td'>"
				+ columnName
				+ "</td><td class='list_table_tbody_td'>"
				+ inputType
				+ "</td><td class='list_table_tbody_td'>"
				+ operation
				+ "</td><td class='list_table_tbody_td'>"
				+ matchMode
				+ "</td><td class='list_table_tbody_td'>"
				+ inputValue
				+ "</td><td class='list_table_tbody_td'><a href='#tab-2' onclick='editSFilters("
				+ i
				+ ")'><img border='0' title='�޸�' src='./images/edit.gif'></a><a href='#tab-2' onclick='deleteSFilters("
				+ i
				+ ")'><img border='0' title='ɾ��' src='./images/delete.gif'></a></td></td></tr>";
		$("#sFiltersTable").append(trStr);
	}
}

/**
 * ��չ��˱�
 * 
 * @param fileName
 */
function clearFilter() {
	if (window.confirm('��ȷ��Ҫɾ�����й��˹�����')) {
		sFilters = new Array();
		refreshSFiltersDisplay();
	}
}

/**
 * �ύ���˹���
 */
function submitFilters() {
	var text = "[";
	for (var i = 0; i < sFilters.length; i++) {
		var filterMap = sFilters[i];
		var columnName = filterMap.get("columnName");
		var comments = filterMap.get("comments");
		var inputType = filterMap.get("inputType");
		var matchMode = filterMap.get("matchMode");
		var operation = filterMap.get("operation");
		var inputValue = filterMap.get("inputValue");
		var filterjson = "{";
		filterjson += "columnName:'" + columnName + "',";
		filterjson += "comments:'" + comments + "',";
		filterjson += "inputType:'" + inputType + "',";
		filterjson += "matchMode:'" + matchMode + "',";
		filterjson += "operation:'" + operation + "',";
		filterjson += "inputValue:'" + inputValue + "'},";
		text += filterjson;
	}
	text = text.substring(0, text.length) + "]";
	$.ajax({
		type : "post",
		url : "crawler",
		dataType : "text",
		data : {
			type : "filter",
			data : text
		},
		success : function(msg) {
			alert(msg);
		}, // �����ɹ���Ĳ�����msg�Ǻ�̨��������ֵ
		error : function(msg) {
			alert("error! " + msg);
		} // �����ɹ�
	});
}

/**
 * ��ʼ������
 */
function startCrawler() {
	$.ajax({
		type : "post",
		url : "crawler",
		dataType : "text",
		data : {
			type : "start"
		},
		success : function(msg) {
			alert(msg);
			$("#startcrawler").hide();
			$("#pausecrawler").show();
		}, // �����ɹ���Ĳ�����msg�Ǻ�̨��������ֵ
		error : function(msg) {
			alert("error! " + msg);
		} // �����ɹ�
	});
}

/**
 * ��ͣ����
 */
function pauseCrawler() {
	$.ajax({
		type : "post",
		url : "crawler",
		dataType : "text",
		data : {
			type : "pause"
		},
		success : function(msg) {
			alert(msg);
			$("#pausecrawler").hide();
			$("#resumecrawler").show();
		}, // �����ɹ���Ĳ�����msg�Ǻ�̨��������ֵ
		error : function(msg) {
			alert("error! " + msg);
		} // �����ɹ�
	});
}

/**
 * ��ͣ����
 */
function resumeCrawler() {
	$.ajax({
		type : "post",
		url : "crawler",
		dataType : "text",
		data : {
			type : "resume"
		},
		success : function(msg) {
			alert(msg);
			$("#resumecrawler").hide();
			$("#pausecrawler").show();
		}, // �����ɹ���Ĳ�����msg�Ǻ�̨��������ֵ
		error : function(msg) {
			alert("error! " + msg);
		} // �����ɹ�
	});
}

/**
 * �Զ�ˢ��log
 */
function autoLog() {
	setInterval(showLog, 1000);
}

/**
 * ˢ����־��Ϣ
 */
function showLog() {
	var logtext = "[log]" + new Date().toLocaleTimeString() + "<br>";
	if (logtext.length > 10000) {

	}
	setTimeout($("#showlogdivtext").append(logtext), 1000);
}
