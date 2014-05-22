/**
 * zTree�ڵ�,����Ԫ����
 * 
 * @id:�����߱�Ψһ��ʶID
 * @comments������������
 * @name�������߱���
 * @pId:�����ID
 * @field:�ֶ�Array
 */
var zNodes;

/**
 * ��߱�ѡ����zTree���󣬲ο�zTree API ��zTreeObj
 */
var zTree;

/**
 * �Ѿ�ѡ��ı����ݽṹ
 * 
 * @key treeNode.id
 * @value treeNode
 */
var sTables = new Map();

/**
 * �������ϵ
 * 
 * @���ϵArray tableConnections[]{conMap}
 *           conMap(leftTableId,rightTableId,leftTableName,rightTableName,leftTableComments,rightTableComments,connection,conFields[]{confieldMap})
 * @�����ֶ�Array conFields[]{confieldMap}
 *            confieldMap(leftFieldName,rightFieldName,leftFieldComments,rightFieldComments)
 */
var tableConnections = new Array();

/**
 * ��ѡ�����ʾ�ֶ�
 * 
 * @param tableId
 *            ��ID
 * @param tableName
 *            ����
 * @param tableComments
 *            ������
 * @param fieldName
 *            �ֶ���
 * @param fieldComments
 *            �ֶ�����
 * @param fieldAlias
 *            �ֶα���
 * @param hidden
 *            �Ƿ�����
 * @param dateType
 *            ��������
 * @param convertType
 *            ��������
 * @param convertExp
 *            ������ʽ
 * @param pattern
 *            ��ʾ��ʽ
 */
var sFields = new Array();

/**
 * ��ѡ��Ĺ�������
 * 
 */
var sFilters = new Array();

/**
 * ������Ϸ�ʽ
 */
var filtersCombination;

/**
 * ��ѡ���ѯ����
 * 
 * @param tableId
 *            ��ID
 * @param tableName
 *            ����
 * @param tableComments
 *            ������
 * @param fieldName
 *            �ֶ���
 * @param fieldComments
 *            �ֶ�����
 * @param fieldAlias
 *            �ֶα���
 * @param hidden
 *            �Ƿ�����
 * 
 */
var sCriterias = new Array();

/**
 * ��ѡ������
 * 
 * @param tableId
 *            ��ID
 * @param tableName
 *            ����
 * @param tableComments
 *            ������
 * @param fieldName
 *            �ֶ���
 * @param fieldComments
 *            �ֶ�����
 * @param fieldAlias
 *            �ֶα���
 * @param order
 *            ����
 * 
 */
var sSorts = new Array();

/**
 * ���ղ�����sql
 */
var sql = "";

/**
 * ztree��������
 */
var setting = {
	check : {
		enable : true
	// �Ƿ����check
	},
	data : {
		key : {
			title : "name", // �����ͣ��ʾJSON����������
			name : "comments"// ����ʾ������
		},
		simpleData : {
			enable : true,
			idKey : "id",
			pIdKey : "pId",
			rootPId : 0
		}
	},
	callback : {// �����¼�callback����
		onCheck : onCheck,
		onDblClick : zTreeOnDblClick
	}
};

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
		for ( var i = 0; i < this.arr.length; i++) {
			if (this.arr[i].key == key) {
				this.arr[i].value = value;
				return;
			}
		}
		this.arr[this.arr.length] = new struct(key, value);
	};

	var get = function(key) {
		for ( var i = 0; i < this.arr.length; i++) {
			if (this.arr[i].key == key) {
				return this.arr[i].value;
			}
		}
		return null;
	};

	var remove = function(key) {
		var v;
		for ( var i = 0; i < this.arr.length; i++) {
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

	/**
	 * չ���߼�����
	 */
	$("#expend_advconfig_div").click(function() {
		$("#expend_advconfig_div").hide();
		$("#hidden_advconfig_div").show();
		$("#adv_config_div").show("clip");
	});

	/**
	 * ���ظ߼�����
	 */
	$("#hidden_advconfig_div").click(function() {
		$("#hidden_advconfig_div").hide();
		$("#expend_advconfig_div").show();
		$("#adv_config_div").hide("blind");
	});

	/**
	 * ��ʼ����ӹ�����ϵ��ť
	 */
	$("#createconnetionbutton").click(function() {
		initconnetiondialog();
	});

	/**
	 * ��ʼ�����α�ֶΰ�ť
	 */
	$("#addpseudofield").click(function() {
		openaddpseudofielddialog();
	});

	/**
	 * ��ʼ����ӹ�����ϵ��ť
	 */
	$("#addfields").click(function() {
		addfields();
	});

	/**
	 * ��ʼ����ӹ�������
	 */
	$("#addfilters").click(function() {
		initaddfiltersdialog();
	});

	/**
	 * �༭����������Ϸ�ʽ
	 */
	$("#editcombination").click(function() {
		editcombination();
	});

	/**
	 * ��ʾsql��ť
	 */
	$("#showsql").click(function() {
		showSql();
		$("#showsqldiv").toggle("blind");
	});

	/**
	 * ��ʼ������UI button
	 */
	$(".mybutton").button();

	/**
	 * ˢ�¹�����ϵ
	 */
	refreshConnectionDisplay();
	//
	// /**
	// * ˢ����ʾ�ֶ�
	// */
	// refreshSFieldsDisplay();
	//
	// /**
	// * ˢ�²�ѯ����
	// */
	// refreshSCriteriasDisplay();

	/**
	 * ����������ѡ����zTree
	 */
	$.ajax({
		url : "ZtreeJsonDate.jsp",
		type : "post",
		data : {
			id : "123"
		},
		datatype : "Json",
		success : function(data) {
			zNodes = eval("(" + data + ")");
			$.fn.zTree.init($("#tableSetZTree"), setting, zNodes);// ��ʼ����
			initZTree();
		},
		error : function(xhr, data, ts) {
			alert("δ���ӵ����������޷���ȡ����Ϣ��");
		}
	});

	$("#sTabs").tabs({
		collapsible : true
	});
});

/**
 * �رձ��Dialogʱȡ��checked��ɾ�����
 */
function setunchecked(id) {
	var node = zTree.getNodeByParam("id", id, null);
	node.checked = false;
	zTree.checkNode(node, false, false);
	zTree.updateNode(node);
	deleterefconection(id);
	sTables.remove(id);
	removeFromSFields(id, null);// ɾ����ʾ�ֶ��иñ�����
	removeFromSCriterias(id, null);// ɾ����ѯ�����иñ�����
	removeFromSSorts(id, null);// ɾ�������иñ�����
	removeSFilters(id);// ɾ�����������иñ�����
}

/**
 * ��ʼ�����zTree
 */
function initZTree(node) {
	zTree = $.fn.zTree.getZTreeObj("tableSetZTree");
	var nodes = node ? [ node ] : zTree.transformToArray(zTree.getNodes());
	for ( var i = 0, l = nodes.length; i < l; i++) {
		var n = nodes[i];
		if (!n.isParent) {
			n.icon = "images/tableico.gif";// ��ʼ��icon,checkbox
			n.nocheck = false;
		} else {
			n.nocheck = true;
		}
		zTree.updateNode(n);
	}
}

/**
 * ��ȡ�����Ϣ
 */
function loadTable(id, name) {
	var field = null;
	$.ajax({
		url : "TableJsonDate.jsp",
		type : "post",
		async : false,
		data : {
			id : encodeURI(id),
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

/**
 * ztree ѡ���¼�,��ӱ��
 */
function onCheck(event, treeId, treeNode) {
	if (treeNode.checked) {
		if (treeNode.field == undefined) {
			var fd = loadTable(treeNode.id, treeNode.name);
			if (fd == null) {
				alert("û�ж�ȡ������Ϣ��");
				return;
			}
			treeNode.field = fd;
		}
		sTables.put(treeNode.id, treeNode);// ����ѡ��ı�������ѡ���Map��
		tableviewiframe.window.openTableDialog(treeNode);
	} else {
		if (!tableviewiframe.window.closeTableDialog(treeNode.id)) {// �ж��Ƿ������ر�dialog
			treeNode.checked = true;
			zTree.updateNode(treeNode);
		}
	}
}

/**
 * ���ڲ��� zTree �����˫��֮����¼��ص�����
 */
function zTreeOnDblClick(event, treeId, treeNode) {
	if (treeNode == null || treeNode.isParent) {
		return;
	}
	if (treeNode.checked == true) {
		treeNode.checked = false;
	} else {
		treeNode.checked = true;
	}
	zTree.updateNode(treeNode);
	onCheck(event, treeId, treeNode);
};

/**
 * ���ѯ���������¼�����
 */
function queryonkeydown(e) {
	var keynum = 0;
	if (window.event) // IE
	{
		keynum = e.keyCode;
	} else if (e.which) // Netscape/Firefox/Opera
	{
		keynum = e.which;
	}
	if (keynum == 13) {
		queryTable();
	}
}

/**
 * �ѱ��
 */
function queryTable() {
	var name = $("#querytext").val();
	if (name == "" || name == null) {
		return false;
	}
	var node = zTree.getNodeByParam("name", name, null);
	if (node == null) {
		node = zTree.getNodeByParam("comments", name, null);
	}
	if (node == null) {
		alert(name + "��δ�ҵ�,ϵͳ�ݲ�֧��ģ��ƥ�䣡");
		return;
	}
	if (node.isParent) {
		zTree.expandNode(node, true, true, true);
	} else {
		zTree.expandNode(node.getParentNode(), true, false, true);
		zTree.selectNode(node);
	}
}

/**
 * ��ʼ��������ϵdialog
 */
function initconnetiondialog() {
	$("#lefttableselect").empty();
	$("<option value='0'>��ѡ��........</option>").appendTo("#lefttableselect");
	$("#righttableselect").empty();
	$("<option value='0'>��ѡ��........</option>").appendTo("#righttableselect");
	if (sTables.size() < 2) {
		alert("���ϵ������Ҫѡ��2����");
		return false;
	}
	if ((sTables.size() - 1) == tableConnections.length) {
		alert(sTables.size() + "���������" + (sTables.size() - 1) + "��������ϵ��");
		return false;
	}
	if (tableConnections.length >= 3) {
		alert("ϵͳ�ݲ�֧��3�����Ϲ�����ϵ��");
		return false;
	}
	if (tableConnections.length > 0) {
		$("#lefttableselect").append(
				"<option value='" + tableConnections[0].get("leftTableId")
						+ "'>" + tableConnections[0].get("leftTableComments")
						+ "</option>");
	}
	for ( var i = 0; i < sTables.arr.length; i++) {
		var node = sTables.arr[i].value;
		if (tableConnections.length == 1
				&& (node.id == tableConnections[0].get("leftTableId") || (node.id == tableConnections[0]
						.get("rightTableId")))) {
			continue;
		}
		if (tableConnections.length == 2
				&& (node.id == tableConnections[1].get("rightTableId")
						|| node.id == tableConnections[0].get("leftTableId") || (node.id == tableConnections[0]
						.get("rightTableId")))) {
			continue;
		}
		$("<option value='" + node.id + "'>" + node.comments + "</option>")
				.appendTo("#righttableselect");
		if (tableConnections.length > 0) {
			continue;
		}
		$("<option value='" + node.id + "'>" + node.comments + "</option>")
				.appendTo("#lefttableselect");
	}
	$("#fieldslist").empty();
	$(
			"<tr id='field1' class='list_table_tbody_tr'><td width='40%' align='center' class='list_table_tbody_td'><select class='leftfieldselect'><option>��ѡ��........</option></select></td><td align='center' width='20%' class='list_table_tbody_td'>=</td><td align='center' width='40%' class='list_table_tbody_td'><select class='rightfieldselect'><option>��ѡ��........</option></select></td></tr>")
			.appendTo("#fieldslist");
	$("#createconnetiondialog").dialog(
			{
				autoOpen : true,
				width : 360,
				height : 270,
				resizable : false,
				buttons : [
						{
							text : "����",
							click : function() {
								var leftTableId = $(
										"#lefttableselect option:selected")
										.val();
								var rightTableId = $(
										"#righttableselect option:selected")
										.val();
								if (leftTableId == "0") {
									alert("��ѡ�����...");
									$("#lefttableselect:focus");
									return false;
								} else if (rightTableId == "0") {
									alert("��ѡ���ұ�...");
									$("#righttableselect:focus");
									return false;
								}
								var result = addconnection();
								if (result == true) {
									$(this).dialog("close");
								} else {
									alert(result);
									return false;
								}
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
 * �رձ��ʱɾ����ϵ
 */
function deleterefconection(id) {
	var v;
	var mlength = tableConnections.length;
	for ( var i = 0; i < mlength; i++) {
		v = tableConnections.pop();
		if (v.get("leftTableId") == id || v.get("rightTableId") == id) {
			continue;
		}
		tableConnections.unshift(v);
	}
	refreshConnectionDisplay();
}

/**
 * ��ӹ�ϵ��tableConnections
 */
function addconnection() {
	var leftTableId = $("#lefttableselect option:selected").val();
	var rightTableId = $("#righttableselect option:selected").val();
	if (leftTableId == rightTableId) {
		return "��������ұ���ͬ��";
	}
	var connection = $("#tableconnection option:selected").val();
	var leftTableName = zTree.getNodeByParam("id", leftTableId, null).name;
	var rightTableName = zTree.getNodeByParam("id", rightTableId, null).name;
	var leftTableComments = zTree.getNodeByParam("id", leftTableId, null).comments;
	var rightTableComments = zTree.getNodeByParam("id", rightTableId, null).comments;
	var conMap = new Map();
	conMap.put("leftTableId", leftTableId);// ���ID
	conMap.put("rightTableId", rightTableId);// �ұ�ID
	conMap.put("leftTableName", leftTableName);// ���name
	conMap.put("rightTableName", rightTableName);// �ұ�name
	conMap.put("leftTableComments", leftTableComments);// ���comments
	conMap.put("rightTableComments", rightTableComments);// �ұ�comments
	conMap.put("connection", connection);// ������ϵ
	var conFields = new Array();
	for ( var i = 1; i < 6; i++) {
		var leftFieldName = $(
				"#field" + i + ">td>.leftfieldselect option:selected").val();
		var rightFieldName = $(
				"#field" + i + ">td>.rightfieldselect option:selected").val();
		var leftFieldComments = $(
				"#field" + i + ">td>.leftfieldselect option:selected").text();
		var rightFieldComments = $(
				"#field" + i + ">td>.rightfieldselect option:selected").text();
		if (leftFieldName != null && leftFieldName != null) {
			var conFieldMap = new Map();
			conFieldMap.put("leftFieldName", leftFieldName);// ������ֶ���
			conFieldMap.put("rightFieldName", rightFieldName);// �ҹ����ֶ���
			conFieldMap.put("leftFieldComments", leftFieldComments);// ������ֶ�����
			conFieldMap.put("rightFieldComments", rightFieldComments);// �ҹ����ֶ�����
			conFields.push(conFieldMap);
		}
	}
	conMap.put("conFields", conFields);// �����ֶ�
	tableConnections[tableConnections.length] = conMap;
	refreshConnectionDisplay();
	return true;
}

/**
 * ˢ�¹�����ϵ��ʾ
 */
function refreshConnectionDisplay() {
	for ( var i = 0; i < 3; i++) {
		var condesctr = "<td width='2%'>"
				+ (i + 1)
				+ "</td><td width='15%'>&nbsp;</td><td width='10%'> </td><td width='15%'> </td><td width='58%'> </td>";
		$("#connectiondesc" + (i + 1)).empty().append(condesctr);
	}
	for ( var i = 0; i < tableConnections.length; i++) {
		var conMap = tableConnections[i];
		var leftTableComments = conMap.get("leftTableComments");
		var connection = conMap.get("connection");
		var rightTableComments = conMap.get("rightTableComments");
		var conFields = conMap.get("conFields");
		var conFieldsStr = "";
		for ( var j = 0; j < conFields.length; j++) {
			conFieldsStr = conFieldsStr + leftTableComments + "."
					+ conFields[j].get("leftFieldComments") + "="
					+ rightTableComments + "."
					+ conFields[j].get("rightFieldComments");
		}
		var condesctr = "<td width='2%'>" + (i + 1) + "</td><td width='15%'>"
				+ leftTableComments + "</td><td width='10%'>" + connection
				+ "</td><td width='15%'>" + rightTableComments
				+ "</td><td><div  style='height:18px;overflow:hidden;' title='"
				+ conFieldsStr + "'> " + conFieldsStr + "</div></td>";
		$("#connectiondesc" + (i + 1)).empty().append(condesctr);
	}
}

/**
 * ��ӹ����ֶ�
 */
function addfields() {
	if ($("#field2").html() == null) {
		$("#field1").clone().attr("id", "field2").appendTo("#fieldslist");
	} else if ($("#field3").html() == null) {
		$("#field1").clone().attr("id", "field3").appendTo("#fieldslist");
	} else if ($("#field4").html() == null) {
		$("#field1").clone().attr("id", "field4").appendTo("#fieldslist");
	} else if ($("#field5").html() == null) {
		$("#field1").clone().attr("id", "field5").appendTo("#fieldslist");
	} else {
		alert("���ֻ��ѡ��5�������ֶΣ�");
	}
}

/**
 * ��������ѡ���¼�
 */
function changleftselect(sel) {
	var nodeid = sel.options[sel.selectedIndex].value;
	$(".leftfieldselect").empty();
	if (nodeid == "0") {// ��ѡ��ť
		$("<option>��ѡ��........</option>").appendTo(".leftfieldselect");
		return;
	}
	var fieldsel = sTables.get(nodeid).field;
	// $("#leftfieldselect1").append("<option>��ѡ��...</option>");
	for ( var i = 0; i < fieldsel.length; i++) {
		var opt = "<option value='" + fieldsel[i].name + "'>"
				+ fieldsel[i].comments + "</option>";
		$(opt).appendTo(".leftfieldselect");
	}
}

/**
 * �Ҳ������ѡ���¼�
 */
function changrightselect(sel) {
	var nodeid = sel.options[sel.selectedIndex].value;
	$(".rightfieldselect").empty();
	if (nodeid == "0") {// ��ѡ��ť
		$("<option>��ѡ��........</option>").appendTo(".rightfieldselect");
		return;
	}
	var fieldsel = sTables.get(nodeid).field;
	// $("#rightfieldselect1").append("<option>��ѡ��...</option>");
	for ( var i = 0; i < fieldsel.length; i++) {
		var opt = "<option value='" + fieldsel[i].name + "'>"
				+ fieldsel[i].comments + "</option>";
		$(opt).appendTo(".rightfieldselect");
	}
}

/**
 * ��ʾ������������checkBox�¼�
 * 
 * @param checked
 *            checked
 * @param checkBox����id
 */
function fcheck(checked, id) {
	var idstrarr = id.split("_");
	var tableId = idstrarr[1];
	var fieldName = idstrarr[2];
	if ("d" == idstrarr[0]) {// ��ʾ�ֶ�
		if (checked) {
			addToSFields(tableId, fieldName);
		} else {
			removeFromSFields(tableId, fieldName);
		}
		$("#sTabs").tabs("option", "selected", 0);
	} else if ("c" == idstrarr[0]) {// ��ѯ����
		if (checked) {
			addToSCriterias(tableId, fieldName);
		} else {
			removeFromSCriterias(tableId, fieldName);
		}
		$("#sTabs").tabs("option", "selected", 2);
	} else if ("s" == idstrarr[0]) {// ����
		if (checked) {
			addToSSorts(tableId, fieldName);
		} else {
			removeFromSSorts(tableId, fieldName);
		}
		$("#sTabs").tabs("option", "selected", 3);

	}
}

/**
 * �򿪸�������dialog
 */
function opencomplexinput(o) {
	$("#complexinputdiv").dialog(
			{
				autoOpen : true,
				width : 250,
				height : 140,
				resizable : false,
				buttons : [
						{
							text : "����",
							click : function() {
								var $v = $(o);
								var str = "#{" + $("#complexinputselect").val()
										+ "." + $("#complexinputval").val()
										+ "}";
								$v.prev().prev().val(str);
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

/** **************************************��ʾ�ֶ�***************************** */
/**
 * ����ֶε���ʾ�ֶ�sFields
 * 
 * @param tableId
 *            ��ID
 * 
 * @param fieldName
 *            �ֶ���
 */
function addToSFields(tableId, fieldName) {
	var sNode = sTables.get(tableId);
	var sFieldMap = new Map();
	sFieldMap.put("tableId", tableId);
	sFieldMap.put("tableName", sNode.name);
	sFieldMap.put("tableComments", sNode.comments);
	sFieldMap.put("fieldName", fieldName);
	sFieldMap.put("hidden", false);
	sFieldMap.put("convertType", "");
	sFieldMap.put("convertExp", "");
	sFieldMap.put("pattern", "");
	for ( var i = 0; i < sNode.field.length; i++) {
		if (sNode.field[i].name == fieldName) {
			sFieldMap.put("fieldComments", sNode.field[i].comments);
			sFieldMap.put("fieldAlias", sNode.field[i].comments);
			sFieldMap.put("dateType", sNode.field[i].type);
			break;
		}
	}
	sFields[sFields.length] = sFieldMap;
	refreshSFieldsDisplay();
}

/**
 * �����α�ֶ�dialog
 */
function openaddpseudofielddialog() {
	$(".change_convertExp").hide();
	$("#addpseudofielddiv")
			.dialog(
					{
						autoOpen : true,
						width : 250,
						height : 240,
						resizable : false,
						buttons : [
								{
									text : "����",
									click : function() {
										if ($("#pseudo_fieldAlias").val() == "") {
											alert("��ʾ������Ϊ�գ�");
											return false;
										}
										if ($(
												"#pseudo_convertType option:selected")
												.val() != ""
												&& $("#pseudo_convertExp")
														.val() == "") {
											alert("������ʽ����Ϊ�գ�");
											return false;
										}
										var sFieldMap = new Map();
										sFieldMap.put("tableId", "α�ֶ�");
										sFieldMap.put("tableComments", "α�ֶ�");
										sFieldMap.put("fieldName", $(
												"#pseudo_fieldName").val());
										sFieldMap.put("hidden", false);
										sFieldMap.put("fieldComments", "α�ֶ�");
										sFieldMap.put("dateType", "α�ֶ�");
										sFieldMap.put("fieldAlias", $(
												"#pseudo_fieldAlias").val());
										sFieldMap
												.put(
														"convertType",
														$(
																"#pseudo_convertType option:selected")
																.val());
										sFieldMap.put("convertExp", $(
												"#pseudo_convertExp").html());
										sFieldMap.put("pattern", $(
												"#pseudo_pattern").val());
										sFields[sFields.length] = sFieldMap;
										alert("�ѱ��棡");
										refreshSFieldsDisplay();
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
 * ����ѡ�����ʾ�ֶ���ɾ���ֶ�
 * 
 * @param tableId
 *            ��id
 * @param fieldName
 *            �ֶ�����Ϊnull��ɾ���˱�������ʾ�ֶ�
 */
function removeFromSFields(tableId, fieldName) {
	var arrlen = sFields.length;
	if (fieldName == null) {
		for ( var i = 0; i < arrlen; i++) {
			var v = sFields.pop();
			if (v.get("tableId") == tableId) {
				continue;
			}
			sFields.unshift(v);
		}
	} else {
		for ( var i = 0; i < arrlen; i++) {
			var v = sFields.pop();
			if (v.get("tableId") == tableId && v.get("fieldName") == fieldName) {
				continue;
			}
			sFields.unshift(v);
		}
	}
	refreshSFieldsDisplay();
}

/**
 * ����������ʾ�ֶ�
 * 
 * @param direction
 *            0�� 1��
 * @param no
 *            sFields�е����
 */
function resortSFields(direction, no) {
	var temp = sFields[no];
	if (direction == 0) {
		if (no == (sFields.length - 1)) {
			alert("�Ѿ����������ˣ�");
		} else {
			sFields[no] = sFields[no + 1];
			sFields[no + 1] = temp;
		}
	} else {
		if (no == 0) {
			alert("�Ѿ����������ˣ�");
		} else {
			sFields[no] = sFields[no - 1];
			sFields[no - 1] = temp;
		}
	}
	refreshSFieldsDisplay();
}

/**
 * ������ʾ�ֶ�
 * 
 * @param o
 *            checkbox����
 * @param no
 *            sFields�е����
 */
function hideSField(o, no) {
	var sFieldMap = sFields[no];
	sFieldMap.put("hidden", o.checked);
}

/**
 * �ı䷭������ʱ������
 * 
 * @param sel
 */
function changconvertTypesel(sel) {
	if (sel[sel.selectedIndex].value == "") {
		$(".change_convertExp").hide();
	} else {
		$(".change_convertExp").show();
	}
}

/**
 * �༭��ʾ�ֶ�
 */
function editSFields(no) {
	var tableComments = sFields[no].get("tableComments");
	// var dateType = sFields[no].get("dateType");
	var fieldComments = sFields[no].get("fieldComments");
	var fieldAlias = sFields[no].get("fieldAlias");
	// var hidden = sFields[no].get("hidden");
	var convertType = sFields[no].get("convertType");
	if (convertType == "") {
		$(".change_convertExp").hide();
	} else {
		$(".change_convertExp").show();
	}
	var convertExp = sFields[no].get("convertExp");
	var pattern = sFields[no].get("pattern");
	$("#edit_tableComments").html(tableComments);
	$("#edit_fieldComments").html(fieldComments);
	$("#edit_fieldAlias").val(fieldAlias);
	$("#edit_convertType option:selected").attr("selected", false);
	$("#edit_convertType option[value='" + convertType + "']").attr("selected",
			true);
	$("#edit_convertExp").html(convertExp);
	$("#edit_pattern").val(pattern);
	$("#editsfield").dialog(
			{
				autoOpen : true,
				width : 250,
				height : 260,
				resizable : false,
				buttons : [
						{
							text : "����",
							click : function() {
								if ($("#edit_convertType option:selected")
										.val() != ""
										&& $("#edit_convertExp").val() == "") {
									alert("������ʽ����Ϊ�գ�");
									return false;
								}
								sFields[no].put("fieldAlias", $(
										"#edit_fieldAlias").val());
								sFields[no].put("convertType", $(
										"#edit_convertType option:selected")
										.val());
								if ($("#edit_convertType option:selected")
										.val() == "") {
									sFields[no].put("convertExp", "");
								} else {
									sFields[no].put("convertExp", $(
											"#edit_convertExp").html());
								}
								sFields[no].put("pattern", $("#edit_pattern")
										.val());
								alert("�ѱ��棡");
								refreshSFieldsDisplay();
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
 * �༭α�ֶ�
 */
function editPseudoField(no) {
	var fieldName = sFields[no].get("fieldName");
	var fieldAlias = sFields[no].get("fieldAlias");
	var convertType = sFields[no].get("convertType");
	var convertExp = sFields[no].get("convertExp");
	if (convertExp == "") {
		$(".change_convertExp").hide();
	} else {
		$(".change_convertExp").show();
	}
	var pattern = sFields[no].get("pattern");
	$("#edit_pseudo_fieldName").val(fieldName);
	$("#edit_pseudo_fieldAlias").val(fieldAlias);
	$("#edit_pseudo_convertType option:selected").attr("selected", false);
	$("#edit_pseudo_convertType option[value='" + convertType + "']").attr(
			"selected", true);
	$("#edit_pseudo_convertExp").html(convertExp);
	$("#edit_pseudo_pattern").val(pattern);
	$("#editpseudofield")
			.dialog(
					{
						autoOpen : true,
						width : 250,
						height : 260,
						resizable : false,
						buttons : [
								{
									text : "����",
									click : function() {
										if ($(
												"#edit_pseudo_convertType option:selected")
												.val() != ""
												&& $("#edit_pseudo_convertExp")
														.val() == "") {
											alert("������ʽ����Ϊ�գ�");
											return false;
										}
										sFields[no]
												.put(
														"fieldName",
														$(
																"#edit_pseudo_fieldName")
																.val());
										sFields[no].put("fieldAlias", $(
												"#edit_pseudo_fieldAlias")
												.val());
										sFields[no]
												.put(
														"convertType",
														$(
																"#edit_pseudo_convertType option:selected")
																.val());
										if ($(
												"#edit_pseudo_convertType option:selected")
												.val() == "") {
											sFields[no].put("convertExp", "");
										} else {
											sFields[no].put("convertExp", $(
													"#edit_pseudo_convertExp")
													.html());
										}

										sFields[no].put("pattern", $(
												"#edit_pseudo_pattern").val());
										alert("�ѱ��棡");
										refreshSFieldsDisplay();
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
function refreshSFieldsDisplay() {
	$("#sFieldsTable").empty();
	if (sFields.length < 1) {
		return;
	}
	$("#sFieldsTable")
			.append(
					"<tr class='list_table_thead_tr_title'><th width='3%'>���</th><th width='8%'>����</th><th width='10%'>�ֶ���</th><th width='10%'>��ʾ��</th><th width='10%'>��������</th><th width='10%'>��������</th><th>������ʽ</th><th width='10%'>��ʾ��ʽ</th><th width='3%'>����</th><th width='6%'>����</th></tr>");
	for ( var i = 0; i < sFields.length; i++) {
		var tableComments = sFields[i].get("tableComments");
		var dateType = sFields[i].get("dateType");
		var fieldName = sFields[i].get("fieldName");
		var fieldComments = sFields[i].get("fieldComments");
		var fieldAlias = sFields[i].get("fieldAlias");
		var hidden = sFields[i].get("hidden");
		var convertType = sFields[i].get("convertType");
		var convertExp = sFields[i].get("convertExp");
		var pattern = sFields[i].get("pattern");
		var checked = "";
		if (hidden == true) {
			checked = "checked='checked'";
		}
		var trStr = "";
		if (dateType == "α�ֶ�") {
			trStr += "<tr class='list_table_tbody_tr_p'><td class='list_table_tbody_td'>"
					+ (i + 1)
					+ "</td><td class='list_table_tbody_td'  colspan='2'>"
					+ fieldName
					+ "</td><td class='list_table_tbody_td'>"
					+ fieldAlias
					+ "</td><td class='list_table_tbody_td'>"
					+ dateType
					+ "</td><td class='list_table_tbody_td'>"
					+ convertType
					+ "</td><td>"
					+ convertExp
					+ "</td><td class='list_table_tbody_td'>"
					+ pattern
					+ "</td><td class='list_table_tbody_td'><input type='checkbox' "
					+ checked
					+ " onclick='hideSField(this,"
					+ i
					+ ");'></td><td class='list_table_tbody_td'><a href='#tab-1' onclick='editPseudoField("
					+ i
					+ ")'><img border='0' title='�޸�' src='./images/edit.gif'></a><a href='#tab-1' onclick='resortSFields(1,"
					+ i
					+ ")'><img border='0' title='����' src='./images/up.gif'></a><a href='#tab-1' onclick='resortSFields(0,"
					+ i
					+ ")'><img border='0' title='����' src='./images/down.gif'></a></td></tr>";
		} else {
			trStr += "<tr class='list_table_tbody_tr'><td class='list_table_tbody_td'>"
					+ (i + 1)
					+ "</td><td class='list_table_tbody_td'>"
					+ tableComments
					+ "</td><td class='list_table_tbody_td'>"
					+ fieldComments
					+ "</td><td class='list_table_tbody_td'>"
					+ fieldAlias
					+ "</td><td class='list_table_tbody_td'>"
					+ dateType
					+ "</td><td class='list_table_tbody_td'>"
					+ convertType
					+ "</td><td>"
					+ convertExp
					+ "</td><td class='list_table_tbody_td'>"
					+ pattern
					+ "</td><td class='list_table_tbody_td'><input type='checkbox' "
					+ checked
					+ " onclick='hideSField(this,"
					+ i
					+ ");'></td><td class='list_table_tbody_td'><a href='#tab-1' onclick='editSFields("
					+ i
					+ ")'><img border='0' title='�޸�' src='./images/edit.gif'></a><a href='#tab-1' onclick='resortSFields(1,"
					+ i
					+ ")'><img border='0' title='����' src='./images/up.gif'></a><a href='#tab-1' onclick='resortSFields(0,"
					+ i
					+ ")'><img border='0' title='����' src='./images/down.gif'></a></td></tr>";
		}
		$("#sFieldsTable").append(trStr);
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
	for ( var i = 0, l = nodes.length; i < l; i++) {
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
	var fieldsel = sTables.get(nodeid).field;
	for ( var i = 0; i < fieldsel.length; i++) {
		var opt = "<option value='" + fieldsel[i].name + "'>"
				+ fieldsel[i].comments + "</option>";
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
	for ( var i = index + 1; i < sFilters.length; i++) {
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
	for ( var i = 0; i < arrlen; i++) {
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
		for ( var i = 1; i < sFilters.length; i++) {
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
	for ( var i = 0; i < sFilters.length; i++) {
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

function editcombination() {
	if (sFilters.length < 2) {
		alert("������������2������Ҫ��ϣ�");
		return false;
	}
	$("#editcombinationtext").val(filtersCombination);
	$("#editcombinationdiv").dialog(
			{
				autoOpen : true,
				width : 250,
				height : 200,
				resizable : false,
				buttons : [
						{
							text : "����",
							click : function() {
								if ($("#editcombinationtext").val() == "") {
									alert("�����������Ϊ�գ�Ĭ��ȫ��ΪAnd");
									return false;
								}
								filtersCombination = $("#editcombinationtext")
										.val();
								$("#editcombinationdesc").empty().append(
										"������ϣ�"
												+ $("#editcombinationtext")
														.val());
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

/** ***************************************��ѯ����***************************** */
/**
 * ����ֶε���ѯ����sCriterias
 * 
 * @param tableId
 *            ��ID
 * 
 * @param fieldName
 *            �ֶ���
 */
function addToSCriterias(tableId, fieldName) {
	var sNode = sTables.get(tableId);
	var sCriteriaMap = new Map();
	sCriteriaMap.put("tableId", tableId);
	sCriteriaMap.put("tableName", sNode.name);
	sCriteriaMap.put("tableComments", sNode.comments);
	sCriteriaMap.put("fieldName", fieldName);
	sCriteriaMap.put("matchMode", "exact");
	sCriteriaMap.put("operation", "=");
	sCriteriaMap.put("inputType", "string");
	sCriteriaMap.put("dicType", "");
	sCriteriaMap.put("dicExp", "");
	sCriteriaMap.put("defaultVal", "");
	sCriteriaMap.put("hidden", false);
	for ( var i = 0; i < sNode.field.length; i++) {
		if (sNode.field[i].name == fieldName) {
			sCriteriaMap.put("fieldComments", sNode.field[i].comments);
			sCriteriaMap.put("fieldAlias", sNode.field[i].comments);
			break;
		}
	}
	sCriterias[sCriterias.length] = sCriteriaMap;
	refreshSCriteriasDisplay();
}

/**
 * ����ѡ��Ĳ�ѯ������ɾ���ֶ�
 * 
 * @param tableId
 *            ��id
 * @param fieldName
 *            �ֶ�����Ϊnull��ɾ���˱����в�ѯ�����ֶ�
 */
function removeFromSCriterias(tableId, fieldName) {
	var arrlen = sCriterias.length;
	if (fieldName == null) {
		for ( var i = 0; i < arrlen; i++) {
			var v = sCriterias.pop();
			if (v.get("tableId") == tableId) {
				continue;
			}
			sCriterias.unshift(v);
		}
	} else {
		for ( var i = 0; i < arrlen; i++) {
			var v = sCriterias.pop();
			if (v.get("tableId") == tableId && v.get("fieldName") == fieldName) {
				continue;
			}
			sCriterias.unshift(v);
		}
	}
	refreshSCriteriasDisplay();
}

/**
 * �༭��ѯ����
 * 
 * @param index
 */
function editSCriterias(index) {
	var tableComments = sCriterias[index].get("tableComments");
	var fieldComments = sCriterias[index].get("fieldComments");
	var fieldAlias = sCriterias[index].get("fieldAlias");
	var matchMode = sCriterias[index].get("matchMode");
	var operation = sCriterias[index].get("operation");
	var inputType = sCriterias[index].get("inputType");
	var dicType = sCriterias[index].get("dicType");
	var dicExp = sCriterias[index].get("dicExp");
	var defaultVal = sCriterias[index].get("defaultVal");
	$("#edit_criterias_tableComments").html(tableComments);
	$("#edit_criterias_fieldComments").html(fieldComments);
	$("#edit_criterias_fieldAlias").val(fieldAlias);
	$("#edit_criterias_matchmode option:selected").attr("selected", false);
	$("#edit_criterias_matchmode option[value='" + matchMode + "']").attr(
			"selected", true);
	$("#edit_criterias_operation option:selected").attr("selected", false);
	$("#edit_criterias_operation option[value='" + operation + "']").attr(
			"selected", true);
	$("#edit_criterias_inputType option:selected").attr("selected", false);
	$("#edit_criterias_inputType option[value='" + inputType + "']").attr(
			"selected", true);
	$("#edit_criterias_dicType option:selected").attr("selected", false);
	$("#edit_criterias_dicType option[value='" + dicType + "']").attr(
			"selected", true);
	$("#edit_criterias_dicExp").val(dicExp);
	$("#edit_criterias_defaultVal").val(defaultVal);

	if (inputType == "select") {
		$("#edit_criterias_dicType").show();
		$("#edit_criterias_dicExp").show();
	} else {
		$("#edit_criterias_dicType").hide();
		$("#edit_criterias_dicExp").hide();
	}

	$("#editscriteriasdiv")
			.dialog(
					{
						autoOpen : true,
						width : 280,
						height : inputType == "select" ? 320 : 280,
						resizable : false,
						buttons : [
								{
									text : "����",
									click : function() {
										sCriterias[index].put("fieldAlias", $(
												"#edit_criterias_fieldAlias")
												.val());
										sCriterias[index].put("matchMode", $(
												"#edit_criterias_matchmode")
												.val());
										sCriterias[index].put("operation", $(
												"#edit_criterias_operation")
												.val());
										sCriterias[index].put("inputType", $(
												"#edit_criterias_inputType")
												.val());
										if ($("#edit_criterias_inputType")
												.val() == "select") {
											if ($("#edit_criterias_dicExp")
													.val() == "") {
												alert("�������ֵ���ʽ��");
												return false;
											}
											sCriterias[index].put("dicType", $(
													"#edit_criterias_dicType")
													.val());
											sCriterias[index].put("dicExp", $(
													"#edit_criterias_dicExp")
													.val());
										} else {
											sCriterias[index]
													.put("dicType", "");
											sCriterias[index].put("dicExp", "");
										}
										sCriterias[index].put("defaultVal", $(
												"#edit_criterias_defaultVal")
												.val());
										alert("�ѱ��棡");
										refreshSCriteriasDisplay();
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
 * �ı���������ʱ������
 * 
 * @param sel
 */
function changinputtype(sel) {
	if (sel[sel.selectedIndex].value == "select") {
		$("#editscriteriasdiv").dialog("option", "height", 320);
		$("#edit_criterias_dicType").show();
		$("#edit_criterias_dicExp").show();
	} else {
		$("#editscriteriasdiv").dialog("option", "height", 280);
		$("#edit_criterias_dicType").hide();
		$("#edit_criterias_dicExp").hide();
	}
}

/**
 * �������Ʋ�ѯ����
 * 
 * @param direction
 *            0�� 1��
 * @param no
 *            sFields�е����
 */
function resortSCriterias(direction, no) {
	var temp = sCriterias[no];
	if (direction == 0) {
		if (no == (sCriterias.length - 1)) {
			alert("�Ѿ����������ˣ�");
		} else {
			sCriterias[no] = sCriterias[no + 1];
			sCriterias[no + 1] = temp;
		}
	} else {
		if (no == 0) {
			alert("�Ѿ����������ˣ�");
		} else {
			sCriterias[no] = sCriterias[no - 1];
			sCriterias[no - 1] = temp;
		}
	}
	refreshSCriteriasDisplay();
}

/**
 * ����ƥ��ģʽ
 * 
 * @param matchMode
 */
function getMatchModeStr(matchMode) {
	if (matchMode == "exact") {
		return "��ȷ";
	} else if (matchMode == "leftmatch") {
		return "��ƥ��";
	} else if (matchMode == "rightmatch") {
		return "��ƥ��";
	} else if (matchMode == "middlematch") {
		return "ģ��ƥ��";
	} else {
		return "ƥ��ģʽ��������";
	}
}

/**
 * ������������
 * 
 * @param inputType
 */
function getInputTypeStr(inputType) {
	if (inputType == "string") {
		return "�ַ���";
	} else if (inputType == "int") {
		return "����";
	} else if (inputType == "double") {
		return "С��";
	} else if (inputType == "dateRange") {
		return "����ʱ���";
	} else if (inputType == "select") {
		return "�ֵ�ѡ��";
	} else {
		return "����������������";
	}
}

/**
 * �����ֵ�����
 * 
 * @param dicType
 */
function getDicTypeStr(dicType) {
	if (dicType == "kind") {
		return "�ֵ�";
	} else if (dicType == "sql") {
		return "sql���";
	} else if (dicType == "") {
		return "";
	} else {
		return "�ֵ�������������";
	}
}

/**
 * ��ѯ����checkbox����¼�
 * 
 * @param o
 * @param index
 */
function hideSCriterias(o, index) {
	var sCriteriaMap = sCriterias[index];
	sCriteriaMap.put("hidden", o.checked);
}

/**
 * ˢ����ѡ���ѯ����
 */
function refreshSCriteriasDisplay() {
	$("#sCriteriasTable").empty();
	if (sCriterias.length < 1) {
		return;
	}
	$("#sCriteriasTable")
			.append(
					"<tr class='list_table_thead_tr_title'><th width='3%'>���</th><th width='8%'>����</th><th width='10%'>�ֶ���</th><th width='10%'>��ʾ��</th></th><th width='6%'>ƥ��ģʽ</th><th width='6%'>��ѯ����</th><th width='6%'>��������</th><th width='6%'>�ֵ�����</th><th>�ֵ���ʽ</th><th width='8%'>Ĭ��ֵ</th><th width='3%'>����</th><th width='6%'>����</th></tr>");
	for ( var i = 0; i < sCriterias.length; i++) {
		var tableComments = sCriterias[i].get("tableComments");
		var fieldComments = sCriterias[i].get("fieldComments");
		var fieldAlias = sCriterias[i].get("fieldAlias");
		var matchModeStr = getMatchModeStr(sCriterias[i].get("matchMode"));
		var operation = sCriterias[i].get("operation");
		var inputTypeStr = getInputTypeStr(sCriterias[i].get("inputType"));
		var dicTypeStr = getDicTypeStr(sCriterias[i].get("dicType"));
		var dicExp = sCriterias[i].get("dicExp");
		var hidden = sCriterias[i].get("hidden");
		var defaultVal = sCriterias[i].get("defaultVal");
		var checked = "";
		if (hidden == true) {
			checked = "checked='checked'";
		}
		var trStr = "<tr class='list_table_tbody_tr'><td class='list_table_tbody_td'>"
				+ (i + 1)
				+ "</td><td class='list_table_tbody_td'>"
				+ tableComments
				+ "</td><td class='list_table_tbody_td'>"
				+ fieldComments
				+ "</td><td class='list_table_tbody_td'>"
				+ fieldAlias
				+ "</td><td class='list_table_tbody_td'>"
				+ matchModeStr
				+ "</td><td class='list_table_tbody_td'>"
				+ operation
				+ "</td><td class='list_table_tbody_td'>"
				+ inputTypeStr
				+ "</td><td class='list_table_tbody_td'>"
				+ dicTypeStr
				+ "</td><td class='list_table_tbody_td'>"
				+ dicExp
				+ "</td><td class='list_table_tbody_td'>"
				+ defaultVal
				+ "</td><td class='list_table_tbody_td'><input type='checkbox' "
				+ checked
				+ " onclick='hideSCriterias(this,"
				+ i
				+ ")'></td><td class='list_table_tbody_td'><a href='#tab-3' onclick='editSCriterias("
				+ i
				+ ")'><img border='0' title='�޸�' src='./images/edit.gif'></a><a href='#tab-3' onclick='resortSCriterias(1,"
				+ i
				+ ")'><img border='0' title='����' src='./images/up.gif'></a><a href='#tab-3' onclick='resortSCriterias(0,"
				+ i
				+ ")'><img border='0' title='����' src='./images/down.gif'></a></td></tr>";
		$("#sCriteriasTable").append(trStr);
	}
}

/** **********************************����************************************* */
/**
 * ����ֶε������ֶ�sSorts
 * 
 * @param tableId
 *            ��ID
 * 
 * @param fieldName
 *            �ֶ���
 */
function addToSSorts(tableId, fieldName) {
	var sNode = sTables.get(tableId);
	var sSortMap = new Map();
	sSortMap.put("tableId", tableId);
	sSortMap.put("tableName", sNode.name);
	sSortMap.put("tableComments", sNode.comments);
	sSortMap.put("fieldName", fieldName);
	sSortMap.put("hidden", false);
	sSortMap.put("order", "Asc");
	for ( var i = 0; i < sNode.field.length; i++) {
		if (sNode.field[i].name == fieldName) {
			sSortMap.put("fieldComments", sNode.field[i].comments);
			sSortMap.put("dateType", sNode.field[i].type);
			break;
		}
	}
	sSorts[sSorts.length] = sSortMap;
	refreshSSortsDisplay();
}

/**
 * ����ѡ��������ֶ���ɾ���ֶ�
 * 
 * @param tableId
 *            ��id
 * @param fieldName
 *            �ֶ�����Ϊnull��ɾ���˱����������ֶ�
 */
function removeFromSSorts(tableId, fieldName) {
	var arrlen = sSorts.length;
	if (fieldName == null) {
		for ( var i = 0; i < arrlen; i++) {
			var v = sSorts.pop();
			if (v.get("tableId") == tableId) {
				continue;
			}
			sSorts.unshift(v);
		}
	} else {
		for ( var i = 0; i < arrlen; i++) {
			var v = sSorts.pop();
			if (v.get("tableId") == tableId && v.get("fieldName") == fieldName) {
				continue;
			}
			sSorts.unshift(v);
		}
	}
	refreshSSortsDisplay();
}

/**
 * ��������checkbox�����¼�
 * 
 * @param o
 * @param index
 */
function hideSSort(o, index) {
	var sSortMap = sSorts[index];
	sSortMap.put("hidden", o.checked);
}

/**
 * ˢ����ѡ�������ֶ�
 */
function refreshSSortsDisplay() {
	$("#sSortsTable").empty();
	if (sSorts.length < 1) {
		return;
	}
	$("#sSortsTable")
			.append(
					"<tr class='list_table_thead_tr_title'><th width='3%'>���</th><th width='12%'>����</th><th width='12%'>�ֶ���</th><th width='12%'>��������</th><th>����</th><th width='3%'>����</th><th width='6%'>����</th></tr>");
	for ( var i = 0; i < sSorts.length; i++) {
		var tableComments = sSorts[i].get("tableComments");
		var dateType = sSorts[i].get("dateType");
		var fieldComments = sSorts[i].get("fieldComments");
		var hidden = sSorts[i].get("hidden");
		var order = "";
		var checked = "";
		if (sSorts[i].get("order") == "Asc") {
			order = "��";
		} else {
			order = "��";
		}
		if (hidden == true) {
			checked = "checked='checked'";
		}
		var trStr = "<tr class='list_table_tbody_tr'><td class='list_table_tbody_td'>"
				+ (i + 1)
				+ "</td><td class='list_table_tbody_td'>"
				+ tableComments
				+ "</td><td class='list_table_tbody_td'>"
				+ fieldComments
				+ "</td><td class='list_table_tbody_td'>"
				+ dateType
				+ "</td><td class='list_table_tbody_td'>"
				+ order
				+ "</td><td class='list_table_tbody_td'><input type='checkbox' "
				+ checked
				+ " onclick='hideSSort(this,"
				+ i
				+ ")'></input></td><td class='list_table_tbody_td'><a href='#tab-4' onclick='editSSorts("
				+ i
				+ ")'><img border='0' title='�޸�' src='./images/edit.gif'></a><a href='#tab-4' onclick='resortSSorts(1,"
				+ i
				+ ")'><img border='0' title='����' src='./images/up.gif'></a><a href='#tab-4' onclick='resortSSorts(0,"
				+ i
				+ ")'><img border='0' title='����' src='./images/down.gif'></a></td></tr>";
		$("#sSortsTable").append(trStr);
	}
}

/**
 * ������������
 * 
 * @param direction
 *            0�� 1��
 * @param no
 *            sSorts�е����
 */
function resortSSorts(direction, no) {
	var temp = sSorts[no];
	if (direction == 0) {
		if (no == (sSorts.length - 1)) {
			alert("�Ѿ����������ˣ�");
		} else {
			sSorts[no] = sSorts[no + 1];
			sSorts[no + 1] = temp;
		}
	} else {
		if (no == 0) {
			alert("�Ѿ����������ˣ�");
		} else {
			sSorts[no] = sSorts[no - 1];
			sSorts[no - 1] = temp;
		}
	}
	refreshSSortsDisplay();
}

/**
 * �޸���������
 */
function editSSorts(no) {
	var tableComments = sSorts[no].get("tableComments");
	var dateType = sSorts[no].get("dateType");
	var fieldComments = sSorts[no].get("fieldComments");
	var order = sSorts[no].get("order");
	$("#edit_sort_tableName").html(tableComments);
	$("#edit_sort_fieldComments").html(fieldComments);
	$("#edit_sort_dataType").html(dateType);
	$("#edit_sort_order option:selected").attr("selected", false);
	$("#edit_sort_order option[value='" + order + "']").attr("selected", true);
	$("#editssort").dialog({
		autoOpen : true,
		width : 250,
		height : 170,
		resizable : false,
		buttons : [ {
			text : "����",
			click : function() {
				sSorts[no].put("order", $("#edit_sort_order").val());
				alert("�ѱ��棡");
				refreshSSortsDisplay();
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

function showSql() {
	var sqlStr = "";
	var sFieldSql = "";
	var sTableSql = "";
	var sFilterSql = " </br><span style='color:#000080'><b>Where</b></span> 1 = 1 ";
	var sCriteriaSql = "";
	var sSortSql = "";

	/** *������ϵ** */
	if (sTables.arr.length == 0) {
		alert("����û��ѡ��һ����");
		return false;
	} else if (sTables.arr.length == 1) {
		sTableSql = " <span style='color:#000080'><b>From</b></span> ";
		sTableSql = sTableSql + sTables.arr[0].value.name;
	} else {
		if (tableConnections.length > 0) {
			sTableSql = " <span style='color:#000080'><b>From</b></span> ";
			for ( var i = 0; i < tableConnections.length; i++) {
				if (i < (tableConnections.length < 1)) {
					sTableSql = sTableSql
							+ " </br><span style='color:#000080'><b>"
							+ tableConnections[i].get("connection")
							+ "</b></span> "
							+ tableConnections[i].get("rightTableName")
							+ " <span style='color:#000080'><b>On</b></span> ";
				} else {
					sTableSql = sTableSql
							+ tableConnections[i].get("leftTableName")
							+ " </br><span style='color:#000080'><b>"
							+ tableConnections[i].get("connection")
							+ "</b></span> "
							+ tableConnections[i].get("rightTableName")
							+ " <span style='color:#000080'><b>On</b></span> ";
				}
				var conFields = tableConnections[i].get("conFields");
				for ( var j = 0; j < conFields.length; j++) {
					sTableSql = sTableSql
							+ tableConnections[i].get("leftTableName") + "."
							+ conFields[j].get("leftFieldName") + " = "
							+ tableConnections[i].get("rightTableName") + "."
							+ conFields[j].get("rightFieldName");
					if (j < (conFields.length - 1)) {
						sTableSql = sTableSql
								+ " <span style='color:#000080'><b>And</b></span> ";
					}
				}
			}
		} else {
			alert("�Ѿ�ѡ����2�������������ǵĹ�����ϵ��");
		}
	}

	/** *��ʾ�ֶ�** */
	if (sFields.length >= 1) {
		sFieldSql = "<span style='color:#000080'><b>Select</b></span> ";
		for ( var i = 0; i < sFields.length; i++) {
			if (sFields[i].get("convertType") == "") {
				if (sFields[i].get("dateType") == "α�ֶ�") {
					sFieldSql = sFieldSql + "'" + sFields[i].get("fieldName")
							+ "'"
							+ " <span style='color:#000080'><b>As</b></span> "
							+ "'" + sFields[i].get("fieldAlias") + "'" + ",";
				} else {
					sFieldSql = sFieldSql + sFields[i].get("tableName") + "."
							+ sFields[i].get("fieldName")
							+ " <span style='color:#000080'><b>As</b></span> "
							+ "'" + sFields[i].get("fieldAlias") + "'" + ",";
				}
			} else if (sFields[i].get("convertType") == "kind") {
				if (sFields[i].get("dateType") == "α�ֶ�") {
					sFieldSql = sFieldSql
							+ " (Select Detail From Dictionary d Where d.code = '"
							+ sFields[i].get("fieldName") + "' And d.kind = '"
							+ sFields[i].get("convertExp") + "') "
							+ " <span style='color:#000080'><b>As</b></span> "
							+ "'" + sFields[i].get("fieldAlias") + "'" + ",";
				} else {
					sFieldSql = sFieldSql
							+ " (Select Detail From Dictionary d Where d.code = "
							+ sFields[i].get("tableName") + "."
							+ sFields[i].get("fieldName") + " And d.kind = '"
							+ sFields[i].get("convertExp") + "') "
							+ " <span style='color:#000080'><b>As</b></span> "
							+ "'" + sFields[i].get("fieldAlias") + "'" + ",";
				}
			} else {
				sFieldSql = sFieldSql + " ( " + sFields[i].get("convertExp")
						+ ") "
						+ " <span style='color:#000080'><b>As</b></span> "
						+ "'" + sFields[i].get("fieldAlias") + "'" + ",";
			}
		}
		sFieldSql = sFieldSql.substring(0, sFieldSql.length - 1) + "</br>";
	} else {
		alert("����û��ѡ����ʾ�ֶΣ�");
	}
	/** *��������** */
	if (sFilters.length >= 1) {
		sFilterSql = sFilterSql
				+ " <span style='color:#000080'><b>And</b></span> ";
		var fieldCombArr = filtersCombination.split(" ");
		for ( var i = 0; i < fieldCombArr.length; i++) {
			var temStr = fieldCombArr[i].toString();
			if (temStr >= 1 && temStr <= 100) {
				sFilterSql = sFilterSql + sFilters[temStr - 1].get("tableName")
						+ "." + sFilters[temStr - 1].get("fieldName") + " "
						+ sFilters[temStr - 1].get("operation") + " '"
						+ sFilters[temStr - 1].get("fieldValue") + "' ";
			} else {
				if (fieldCombArr[i] == "and") {
					fieldCombArr[i] = " And ";
				}
				if (fieldCombArr[i] == "or") {
					fieldCombArr[i] = " Or ";
				}
				sFilterSql = sFilterSql + " <span style='color:#000080'><b>"
						+ fieldCombArr[i] + "</b></span> ";
			}
		}
	}
	/** *��ѯ����** */
	if (sCriterias.length >= 1) {
		for ( var i = 0; i < sCriterias.length; i++) {
			if (sCriterias[i].get("inputType") == "dateRange") {
				sCriteriaSql = sCriteriaSql
						+ " <span style='color:#000080'><b>And</b></span> "
						+ sCriterias[i].get("tableName")
						+ "."
						+ sCriterias[i].get("fieldName")
						+ " >= To_Date(?,'yyyy-MM-dd') <span style='color:#000080'><b>And</b></span> "
						+ sCriterias[i].get("tableName") + "."
						+ sCriterias[i].get("fieldName")
						+ " <= To_Date(?,'yyyy-MM-dd') ";
			} else {
				if (sCriterias[i].get("operation") == "Like") {
					sCriteriaSql = sCriteriaSql
							+ " <span style='color:#000080'><b>And</b></span> "
							+ sCriterias[i].get("tableName") + "."
							+ sCriterias[i].get("fieldName")
							+ " <span style='color:#000080'><b>"
							+ sCriterias[i].get("operation") + "</b></span> "
							+ " ?";
				} else {
					sCriteriaSql = sCriteriaSql
							+ " <span style='color:#000080'><b>And</b></span> "
							+ sCriterias[i].get("tableName") + "."
							+ sCriterias[i].get("fieldName") + " "
							+ sCriterias[i].get("operation") + " ?" + " ";
				}
			}
		}
	}
	/** *����** */
	if (sSorts.length >= 1) {
		sSortSql = " </br><span style='color:#000080'><b>Order By</b></span> ";
		for ( var i = 0; i < sSorts.length; i++) {
			sSortSql = sSortSql + sSorts[i].get("tableName") + "."
					+ sSorts[i].get("fieldName")
					+ " <span style='color:#000080'><b>"
					+ sSorts[i].get("order") + "</b></span> ,";
		}
		sSortSql = sSortSql.substring(0, sSortSql.length - 1);
	}
	sqlStr = sqlStr + sFieldSql + sTableSql + sFilterSql + sCriteriaSql
			+ sSortSql;
	$("#showsqldivtext").empty().append(sqlStr);
	sql = sqlStr.replace(/<[^>]+>/g, "");
}
