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
			+ "{columnName:'description',inputType:'String',comments:'�û�����������Ϣ'},"
			+ "{columnName:'userName',inputType:'String',comments:'�û���'},"
			+ "{columnName:'followNum',inputType:'int',comments:'��ע���� Ĭ��0'},"
			+ "{columnName:'fansNum',inputType:'int',comments:'��˿���� Ĭ��0'},"
			+ "{columnName:'messageNum',inputType:'int',comments:'��Ϣ���� Ĭ��0'},"
			+ "{columnName:'profileImageUrl',inputType:'String',comments:'ͷ��URL ��̱�ʶ�ַ���'},"
			+ "{columnName:'isVerified',inputType:'String',comments:'0 ��ͨ�û� 1��֤�û�'},"
			+ "{columnName:'careerInfo',inputType:'String',comments:'ְҵ��Ϣ'},"
			+ "{columnName:'educationInfo',inputType:'String',comments:'������Ϣ'},"
			+ "{columnName:'tag',inputType:'String',comments:'�û���ǩ����\",\"�ŷָ�.'},"
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
		trStr += "<tr class='list_table_tbody_tr_p'><td class='list_table_tbody_td'>"
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
/** **************************************��������***************************** */

/**
 * ��ʼ����ӹ�������dialog
 */
function initaddfiltersdialog() {
	$("#filtertableselect").empty();
	$("<option value='0'>��ѡ��........</option>").appendTo("#filtertableselect");
	var nodes = zTree.transformToArray(zTree.getNodes());
	if (sTables.size() < 1) {
		alert("�㻹δѡ��һ������!");
		return false;
	}
	for (var i = 0, l = nodes.length; i < l; i++) {
		var node = nodes[i];
		if (node.checked == true) {
			$("<option value='" + node.id + "'>" + node.comments + "</option>")
					.appendTo("#filtertableselect");
		}
	}
	$("#filterfieldselect").empty();
	$("<option value='0'>��ѡ��........</option>").appendTo("#filterfieldselect");
	$("#addfiltersdiv").dialog(
			{
				autoOpen : true,
				width : 600,
				height : 130,
				resizable : false,
				buttons : [
						{
							text : "����",
							click : function() {
								var filterTableId = $(
										"#filtertableselect option:selected")
										.val();
								var filterTableComments = $(
										"#filtertableselect option:selected")
										.html();
								var filterFieldName = $(
										"#filterfieldselect option:selected")
										.val();
								var filterFieldComments = $(
										"#filterfieldselect option:selected")
										.html();
								if (filterTableId == "0"
										|| filterFieldName == "0") {
									alert("��ѡ��");
									return false;
								}
								var filterTableName = sTables
										.get(filterTableId).name;
								var filterOperation = $(
										"#filteroperationselect").val();
								var filterFieldValue = $("#filterfieldvalue")
										.val();
								var filterMap = new Map();
								filterMap.put("tableId", filterTableId);
								filterMap.put("tableName", filterTableName);
								filterMap.put("tableComments",
										filterTableComments);
								filterMap.put("fieldName", filterFieldName);
								filterMap.put("fieldComments",
										filterFieldComments);
								filterMap.put("hidden", false);
								filterMap.put("operation", filterOperation);
								filterMap.put("fieldValue", filterFieldValue);
								sFilters[sFilters.length] = filterMap;
								$(this).dialog("close");
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
function changfiltertableselect(sel) {
	var nodeid = sel.options[sel.selectedIndex].value;
	$("#filterfieldselect").empty();
	if (nodeid == "0") {// ��ѡ��ť
		$("<option>��ѡ��........</option>").appendTo("#filterfieldselect");
		return;
	}
	for (var i = 0; i < sFields.length; i++) {
		var opt = "<option value='" + sFields[i].get("comments") + "'>"
				+ sFields[i].get("columnName") + "</option>";
		$(opt).appendTo("#filterfieldselect");
	}
}

/**
 * �༭��������
 * 
 * @param index
 */
function editSFilters(index) {
	var tableComments = sFilters[index].get("tableComments");
	var fieldComments = sFilters[index].get("fieldComments");
	var operation = sFilters[index].get("operation");
	var fieldValue = sFilters[index].get("fieldValue");
	$("#edit_filters_tablename").html(tableComments);
	$("#edit_filters_fieldcomments").html(fieldComments);
	$("#edit_filters_operationselect option:selected").attr("selected", false);
	$("#edit_filters_operationselect option[value='" + operation + "']").attr(
			"selected", true);
	$("#edit_filters_fieldvalue").val(fieldValue);
	$("#editfiltersdiv")
			.dialog(
					{
						autoOpen : true,
						width : 250,
						height : 170,
						resizable : false,
						buttons : [
								{
									text : "����",
									click : function() {
										sFilters[index]
												.put(
														"operation",
														$(
																"#edit_filters_operationselect")
																.val());
										sFilters[index].put("fieldValue", $(
												"#edit_filters_fieldvalue")
												.val());
										alert("�ѱ��棡");
										refreshSFiltersDisplay();
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
 * ��sFilters����ɾ����������
 * 
 * @param index
 */
function deleteSFilters(index) {
	if (!confirm("ȷ��ɾ���˹���������")) {
		return;
	}
	for (var i = index + 1; i < sFilters.length; i++) {
		sFilters[i] = sFilters[i - 1];
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
 * ������������
 * 
 * @param o
 * @param index
 */
function hideSFilter(o, index) {
	var sFilterMap = sFilters[index];
	sFilterMap.put("hidden", o.checked);
}

function refreshFilterCombination() {
	if (sFilters.length > 1) {
		filtersCombination = "1";
		for (var i = 1; i < sFilters.length; i++) {
			filtersCombination = filtersCombination + " and " + (i + 1);
		}
	} else if (sFilters.length == 1) {
		filtersCombination = "1";
	}
}

/**
 * ˢ�¹�������
 */
function refreshSFiltersDisplay() {
	refreshFilterCombination();
	$("#sFiltersTable").empty();
	if (sFilters.length < 1) {
		return;
	}
	$("#sFiltersTable")
			.append(
					"<tr class='list_table_thead_tr_title'><th width='3%'>���</th><th width='10%'>����</th><th width='10%'>�ֶ���</th><th width='10%'>�����</th><th>�ֶ�ֵ</th><th width='3%'>����</th><th width='6%'>����</th></tr>");
	for (var i = 0; i < sFilters.length; i++) {
		var filterTableTitle = sFilters[i].get("tableComments");
		var filterFieldComments = sFilters[i].get("fieldComments");
		var filterOperation = sFilters[i].get("operation");
		var filterFieldValue = sFilters[i].get("fieldValue");
		var hidden = sFilters[i].get("hidden");
		var checked = "";
		if (hidden == true) {
			checked = "checked='checked'";
		}
		var trStr = "<tr class='list_table_tbody_tr'><td class='list_table_tbody_td'>"
				+ (i + 1)
				+ "</td><td class='list_table_tbody_td'>"
				+ filterTableTitle
				+ "</td><td class='list_table_tbody_td'>"
				+ filterFieldComments
				+ "</td><td class='list_table_tbody_td'>"
				+ filterOperation
				+ "</td><td class='list_table_tbody_td'>"
				+ filterFieldValue
				+ "</td><td class='list_table_tbody_td'><input type='checkbox' "
				+ checked
				+ " onclick='hideSFilter(this, "
				+ i
				+ ")'></td><td class='list_table_tbody_td'><a href='#tab-2' onclick='editSFilters("
				+ i
				+ ")'><img border='0' title='�޸�' src='./images/edit.gif'></a><a href='#tab-2' onclick='deleteSFilters("
				+ i
				+ ")'><img border='0' title='ɾ��' src='./images/delete.gif'></a></td></td></tr>";
		$("#sFiltersTable").append(trStr);
	}
}

/**
 * ����ƥ��ģʽ
 * 
 * @param matchMode
 */
function getMatchModeStr(matchMode) {
	if (matchMode == "fuzzy") {
		return "��ȷ";
	} else if (matchMode == "left") {
		return "��ƥ��";
	} else if (matchMode == "right") {
		return "��ƥ��";
	} else if (matchMode == "middle") {
		return "ģ��ƥ��";
	} else {
		return "ƥ��ģʽ��������";
	}
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
