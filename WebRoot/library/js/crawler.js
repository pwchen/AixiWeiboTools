/**
 * 已经载入的字段
 * 
 * @param fieldName
 *            字段名
 * @param fieldComments
 *            字段描述
 * @param fieldAlias
 *            字段别名
 */
var sFields = new Array();

/**
 * 爬取入口用户ID列表
 * 
 * @param userId
 *            用户ID
 * @param depth
 *            用户ID
 */
var sUsers = new Array();

/**
 * 已选择的过滤规则列表
 * 
 */
var sFilters = new Array();

/**
 * Map对象
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
 * 初始化
 */
$(function() {

	/*
	 * $.ajax({ url : "test.html",// "ZtreeJsonDate.jsp", type : "post", data : {
	 * tableName : "TableJsonDate.jsp" }, datatype : "Json", success :
	 * function(data) { var fieldsdata = eval("(" + data + ")");
	 * initsFields(fieldsdata); }, error : function(xhr, data, ts) {
	 * alert("未连接到服务器！无法读取表信息！"); } });
	 */

	var fieldsdata = eval("("
			+ "[{columnName:'screenName',inputType:'String',comments:'屏幕名'},"
			+ "{columnName:'sex',inputType:'String',comments:'性别'},"
			+ "{columnName:'description',inputType:'String',comments:'用户描述信息'},"
			+ "{columnName:'userName',inputType:'String',comments:'用户名'},"
			+ "{columnName:'followNum',inputType:'int',comments:'关注数量'},"
			+ "{columnName:'fansNum',inputType:'int',comments:'粉丝数量 '},"
			+ "{columnName:'messageNum',inputType:'int',comments:'消息数量'},"
			+ "{columnName:'profileImageUrl',inputType:'String',comments:'头像URL'},"
			+ "{columnName:'isVerified',inputType:'String',comments:'是否认证0/1'},"
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
			+ "{columnName:'verifyInfo',inputType:'String',comments:'认证信息'}]"
			+ ")");
	initsFields(fieldsdata);
	/**
	 * 初始化所以UI button
	 */
	$(".mybutton").button();

	$("#sTabs").tabs({
		collapsible : true
	});

});

/**
 * 读入用户表字段数据
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
 * 读取表格信息
 */
function loadFields(name) {
	var field = null;
	$.ajax({
		url : "loadFields",// 获取表格信息后台servlet
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

/** **************************************添加入口用户***************************** */

/**
 * 通过文件加载用户爬取入口
 */
function addUserFromFile() {
	var fileName = $("#fileName").val();
	if (fileName == "") {
		alert("请先选择文件！");
		return;
	}
	$("#fileName").val("");
	if (fileName.indexOf(".csv", 0) == -1 && fileName.indexOf(".CSV", 0) == -1) {
		alert("文件格式有误~！仅支持csv格式数据！");
		return;
	}
	var text = readFile(fileName);
	var strs = text.split(/\n/g);
	for (var i = 0; i < strs.length; i++) {
		var us = strs[i].split(",");
		if (us == "") {
		} else if (us.length != 2) {
			alert("第" + (i + 1) + "数据格式出错");
		} else {
			addUserTosUsers(us[0], us[1]);
		}
	}
}
/**
 * 添加user到user列表中
 * 
 * @param userId
 * @param depth
 */
function addUserTosUsers(userId, depth) {
	if (checkUserInSUsers(userId, depth)) {
		return false;
	}
	if (userId == "") {
		alert(userId + ":" + depth + "用户ID不能为空！");
		return false;
	}
	if (depth == "") {
		alert(userId + ":" + depth + "爬取深度不能为空！");
		return false;
	} else if (isNaN(depth)) {
		alert(userId + ":" + depth + "爬取深度必须为数字！");
		return false;
	}
	var userMap = new Map();
	userMap.put("userId", userId);// depth
	userMap.put("depth", depth);// depth
	sUsers[sUsers.length] = userMap;
	refreshsUsersDisplay();
}
/**
 * 读文件
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
 * 打开添加用户dialog
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
							text : "保存",
							click : function() {
								var userId = $("#add_userId").val().replace(
										/\s+/g, "");
								$("#add_userId").val(userId);
								var depth = $("#add_depth").val().replace(
										/\s+/g, "");
								$("#add_depth").val(depth);
								if (userId == "") {
									alert("用户ID不能为空！");
									return false;
								}
								if (depth == "") {
									alert("爬取深度不能为空！");
									return false;
								} else if (isNaN(depth)) {
									alert("爬取深度必须为数字！");
									return false;
								}
								if (checkUserInSUsers(userId, depth)) {
									return false;
								}
								var userMap = new Map();
								userMap.put("userId", userId);
								userMap.put("depth", depth);
								sUsers[sUsers.length] = userMap;
								alert("用户"
										+ sUsers[sUsers.length - 1]
												.get("userId")
										+ "："
										+ sUsers[sUsers.length - 1]
												.get("depth") + " 已保存！用户总量:"
										+ sUsers.length);
								refreshsUsersDisplay();
								$(this).dialog("close");
							}
						}, {
							text : "关闭",
							click : function() {
								$(this).dialog("close");
							}
						} ],
			});
}

/**
 * 根据数组编号
 * 
 * @param no
 */
function deleteUser(no) {
	if (window.confirm('你确定要从队列中删除该用户吗？')) {
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
 * 根据用户Id和深度删除用户
 * 
 * @param userId
 *            用戶id
 * @param depth
 *            爬取深度
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
 * 清空用户表
 * 
 * @param fileName
 */
function clearUser() {
	if (window.confirm('你确定要删除所有用户吗？')) {
		sUsers = new Array();
		refreshsUsersDisplay();
	}
}

/**
 * 检查改用户是否在用户任务列表中,存在返回true
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
				alert("用户" + userId + "：" + depth + " 已存在用户列表中！");
				return true;
			}
		}
	}
	return false;
}

/**
 * 上移下移显示字段
 * 
 * @param direction
 *            0下 1上
 * @param no
 *            sFields中的序号
 */
function resortSUsers(direction, no) {
	var temp = sUsers[no];
	if (direction == 0) {
		if (no == (sUsers.length - 1)) {
			alert("已经到最下面了！");
		} else {
			sUsers[no] = sUsers[no + 1];
			sUsers[no + 1] = temp;
		}
	} else {
		if (no == 0) {
			alert("已经到最上面了！");
		} else {
			sUsers[no] = sUsers[no - 1];
			sUsers[no - 1] = temp;
		}
	}
	refreshsUsersDisplay();
}

/**
 * 编辑显示字段
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
							text : "保存",
							click : function() {
								var userId = $("#edit_userId").val().replace(
										/\s+/g, "");
								$("#userId").val(userId);
								var depth = $("#edit_depth").val().replace(
										/\s+/g, "");
								$("#depth").val(depth);
								if (userId == "") {
									alert("用户ID不能为空！");
									return false;
								}
								if (depth == "") {
									alert("爬取深度不能为空！");
									return false;
								} else if (isNaN(depth)) {
									alert("爬取深度必须为数字！");
									return false;
								}
								if (checkUserInSUsers(userId, depth)) {
									return false;
								}
								sUsers[no].put("userId", userId);
								sUsers[no].put("depth", depth);
								alert("用户" + userId + "：" + depth
										+ " 已保存！用户总量:" + sUsers.length);
								refreshsUsersDisplay();
								$(this).dialog("close");
							}
						}, {
							text : "关闭",
							click : function() {
								$(this).dialog("close");
							}
						} ],
			});
}

/**
 * 刷新已选择显示字段
 */
function refreshsUsersDisplay() {
	$("#sUsersTable").empty();
	if (sUsers.length < 1) {
		return;
	}
	$("#sUsersTable")
			.append(
					"<tr class='list_table_thead_tr_title'><th width='3%'>序号</th><th width='8%'>用户ID</th><th width='10%'>爬取深度</th><th width='6%'>操作</th></tr>");
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
				+ ")'><img border='0' title='修改' src='./images/edit.gif'></a><a href='#tab-1' onclick='resortSUsers(1,"
				+ i
				+ ")'><img border='0' title='上移' src='./images/up.gif'></a><a href='#tab-1' onclick='resortSUsers(0,"
				+ i
				+ ")'><img border='0' title='下移' src='./images/down.gif'></a><a href='#tab-2' onclick='deleteUser("
				+ i
				+ ")'><img border='0' title='删除' src='./images/delete.gif'></td></tr>";
		$("#sUsersTable").append(trStr);
	}

}

/**
 * 提交过滤规则
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
		}, // 操作成功后的操作！msg是后台传过来的值
		error : function(msg) {
			alert("error! " + msg);
		} // 操作成功
	});
}
/** **************************************过滤条件***************************** */

/**
 * 初始化添加过滤条件dialog
 */
function initaddfiltersdialog() {
	$("#addfieldselect").empty();
	$("#addoperationselect").hide();
	$("#addmatchselect").hide();
	$("#addcomments").text("");
	$("#addinputtype").text("");
	$("<option value='-1'>请选择........</option>").appendTo("#addfieldselect");
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
			text : "保存",
			click : function() {
				var selectedId = $("#addfieldselect option:selected").val();
				if (selectedId == -1) {
					alert("请选择一个字段!");
					return false;
				}
				var columnName = sFields[selectedId].get("columnName");
				var comments = sFields[selectedId].get("comments");
				var inputType = sFields[selectedId].get("inputType");
				var matchMode = $("#addmatchselect option:selected").val();
				var operation = $("#addoperationselect option:selected").val();
				var inputValue = $("#addinputvalue").val();
				if (inputValue == "") {
					alert("请输入值!");
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
				alert("添加成功!" + sFilters.length);
				refreshSFiltersDisplay();
			}
		}, {
			text : "关闭",
			click : function() {
				$(this).dialog("close");
			}
		} ]
	});
}

/**
 * 添加过滤条件表选择字段联动
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
				"<option  title='模糊匹配' value='fuzzy' selected='selected'>fuzzy</option><option value='left'>left</option><option value='right'>right</option><option value='middle'>middle</option><option value='full'>full</option>")
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
 * 编辑过滤条件
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
				"<option  title='模糊匹配' value='fuzzy'>fuzzy</option><option value='left'>left</option><option value='right'>right</option><option value='middle'>middle</option><option value='full'>full</option>")
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
							text : "保存",
							click : function() {
								var matchMode = $(
										"#editmatchselect option:selected")
										.val();
								var operation = $(
										"#editoperationselect option:selected")
										.val();
								var inputValue = $("#editinputvalue").val();
								if (inputValue == "") {
									alert("请输入值!");
									return false;
								}
								sFilters[index].put("matchMode", matchMode);
								sFilters[index].put("operation", operation);
								sFilters[index].put("inputValue", inputValue);
								refreshSFiltersDisplay();
								$(this).dialog("close");
								alert("已保存！");
							}
						}, {
							text : "关闭",
							click : function() {
								$(this).dialog("close");
							}
						} ],
			});
}

/**
 * 按sFilters索引删除过滤条件
 * 
 * @param index
 */
function deleteSFilters(index) {
	if (!confirm("确定删除此过滤规则？")) {
		return;
	}
	for (var i = index; i < sFilters.length; i++) {
		sFilters[i] = sFilters[i + 1];
	}
	sFilters.pop();
	refreshSFiltersDisplay();
}

/**
 * 按表ID删除过滤条件
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
 * 刷新过滤条件
 */
function refreshSFiltersDisplay() {
	$("#sFiltersTable").empty();
	if (sFilters.length < 1) {
		return;
	}
	$("#sFiltersTable")
			.append(
					"<tr class='list_table_thead_tr_title'><th width='3%'>序号</th><th width='10%'>标签</th><th width='10%'>字段名</th><th width='10%'>输入类型</th><th>操作符</th><th >匹配模式</th><th>输入值</th><th width='6%'>操作</th></tr>");
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
				+ ")'><img border='0' title='修改' src='./images/edit.gif'></a><a href='#tab-2' onclick='deleteSFilters("
				+ i
				+ ")'><img border='0' title='删除' src='./images/delete.gif'></a></td></td></tr>";
		$("#sFiltersTable").append(trStr);
	}
}

/**
 * 清空过滤表
 * 
 * @param fileName
 */
function clearFilter() {
	if (window.confirm('你确定要删除所有过滤规则吗？')) {
		sFilters = new Array();
		refreshSFiltersDisplay();
	}
}

/**
 * 提交过滤规则
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
		}, // 操作成功后的操作！msg是后台传过来的值
		error : function(msg) {
			alert("error! " + msg);
		} // 操作成功
	});
}

/**
 * 开始爬数据
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
		}, // 操作成功后的操作！msg是后台传过来的值
		error : function(msg) {
			alert("error! " + msg);
		} // 操作成功
	});
}

/**
 * 暂停爬虫
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
		}, // 操作成功后的操作！msg是后台传过来的值
		error : function(msg) {
			alert("error! " + msg);
		} // 操作成功
	});
}

/**
 * 暂停爬虫
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
		}, // 操作成功后的操作！msg是后台传过来的值
		error : function(msg) {
			alert("error! " + msg);
		} // 操作成功
	});
}

/**
 * 自动刷新log
 */
function autoLog() {
	setInterval(showLog, 1000);
}

/**
 * 刷新日志信息
 */
function showLog() {
	var logtext = "[log]" + new Date().toLocaleTimeString() + "<br>";
	if (logtext.length > 10000) {

	}
	setTimeout($("#showlogdivtext").append(logtext), 1000);
}
