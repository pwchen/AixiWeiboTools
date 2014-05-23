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
			+ "{columnName:'description',inputType:'String',comments:'用户自我描述信息'},"
			+ "{columnName:'userName',inputType:'String',comments:'用户名'},"
			+ "{columnName:'followNum',inputType:'int',comments:'关注数量 默认0'},"
			+ "{columnName:'fansNum',inputType:'int',comments:'粉丝数量 默认0'},"
			+ "{columnName:'messageNum',inputType:'int',comments:'消息数量 默认0'},"
			+ "{columnName:'profileImageUrl',inputType:'String',comments:'头像URL 最短标识字符串'},"
			+ "{columnName:'isVerified',inputType:'String',comments:'0 普通用户 1认证用户'},"
			+ "{columnName:'careerInfo',inputType:'String',comments:'职业信息'},"
			+ "{columnName:'educationInfo',inputType:'String',comments:'教育信息'},"
			+ "{columnName:'tag',inputType:'String',comments:'用户标签，用\",\"号分隔.'},"
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
		trStr += "<tr class='list_table_tbody_tr_p'><td class='list_table_tbody_td'>"
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
/** **************************************过滤条件***************************** */

/**
 * 初始化添加过滤条件dialog
 */
function initaddfiltersdialog() {
	$("#filtertableselect").empty();
	$("<option value='0'>请选择........</option>").appendTo("#filtertableselect");
	var nodes = zTree.transformToArray(zTree.getNodes());
	if (sTables.size() < 1) {
		alert("你还未选择一个表呢!");
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
	$("<option value='0'>请选择........</option>").appendTo("#filterfieldselect");
	$("#addfiltersdiv").dialog(
			{
				autoOpen : true,
				width : 600,
				height : 130,
				resizable : false,
				buttons : [
						{
							text : "保存",
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
									alert("请选择！");
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
function changfiltertableselect(sel) {
	var nodeid = sel.options[sel.selectedIndex].value;
	$("#filterfieldselect").empty();
	if (nodeid == "0") {// 请选择按钮
		$("<option>请选择........</option>").appendTo("#filterfieldselect");
		return;
	}
	for (var i = 0; i < sFields.length; i++) {
		var opt = "<option value='" + sFields[i].get("comments") + "'>"
				+ sFields[i].get("columnName") + "</option>";
		$(opt).appendTo("#filterfieldselect");
	}
}

/**
 * 编辑过滤条件
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
									text : "保存",
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
										alert("已保存！");
										refreshSFiltersDisplay();
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
 * 按sFilters索引删除过滤条件
 * 
 * @param index
 */
function deleteSFilters(index) {
	if (!confirm("确定删除此过滤条件？")) {
		return;
	}
	for (var i = index + 1; i < sFilters.length; i++) {
		sFilters[i] = sFilters[i - 1];
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
 * 过滤条件隐藏
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
 * 刷新过滤条件
 */
function refreshSFiltersDisplay() {
	refreshFilterCombination();
	$("#sFiltersTable").empty();
	if (sFilters.length < 1) {
		return;
	}
	$("#sFiltersTable")
			.append(
					"<tr class='list_table_thead_tr_title'><th width='3%'>序号</th><th width='10%'>表名</th><th width='10%'>字段名</th><th width='10%'>运算符</th><th>字段值</th><th width='3%'>隐藏</th><th width='6%'>操作</th></tr>");
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
				+ ")'><img border='0' title='修改' src='./images/edit.gif'></a><a href='#tab-2' onclick='deleteSFilters("
				+ i
				+ ")'><img border='0' title='删除' src='./images/delete.gif'></a></td></td></tr>";
		$("#sFiltersTable").append(trStr);
	}
}

/**
 * 翻译匹配模式
 * 
 * @param matchMode
 */
function getMatchModeStr(matchMode) {
	if (matchMode == "fuzzy") {
		return "精确";
	} else if (matchMode == "left") {
		return "左匹配";
	} else if (matchMode == "right") {
		return "右匹配";
	} else if (matchMode == "middle") {
		return "模糊匹配";
	} else {
		return "匹配模式数据有误！";
	}
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
