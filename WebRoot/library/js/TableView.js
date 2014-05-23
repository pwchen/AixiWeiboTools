/**
 * 打开表dialog
 * 
 * @param treeNode
 *            表元数据
 */
var closeflag;
function openTableDialog(treeNode) {
	var id = treeNode.id;
	var name = treeNode.name;
	$("#tabledialog" + id).remove();
	if ($("#tabledialog" + id).id == undefined) {
		cloneTableDialog(id, name, treeNode);
	}
	$("#tabledialog" + id).dialog("open");
	if ($.browser.msie) {
		$("#tabledialog" + id).css("width", 200);
		$("#tabledialog" + id).css("height", 200);
	} else if ($.browser.mozilla) {
		alert("have not been test in mozilla em!");
	} else if ($.browser.safari) {
		alert("have not been test in safari em!");
	}
}

/**
 * 传入id关闭表格Dialog
 * 
 * @param 表ID
 */
function closeTableDialog(id) {
	$("#tabledialog" + id).dialog("close");
	return flag;
}

/**
 * 隐/显未选中字段
 * 
 * @param o
 *            checkBox对象
 */
function hidefields(o) {
	var $o = $(o);
	var $trs = $o.parent().next().children();
	if ($o.attr("checked") == "checked") {
		var $tr = $trs.find("tr").next();// 第一个字段的tr
		while ($tr.html() != null) {
			var flag = true;
			if ($tr.children().next().children().attr("checked") == "checked") {
				flag = false;
			}
			if ($tr.children().next().next().children().attr("checked") == "checked") {
				flag = false;
			}
			if ($tr.children().next().next().next().children().attr("checked") == "checked") {
				flag = false;
			}
			if (flag) {
				$tr.hide();
				flag = true;
			} else {
				$tr.show();
			}
			$tr = $tr.next();
		}
	} else {
		var $tr = $trs.find("tr").next();// 第一个字段的tr
		while ($tr.html() != null) {
			$tr.show();
			$tr = $tr.next();
		}
	}
}

/**
 * 根据名称、ID克隆一个表格dialog
 * 
 * @param id
 *            表ID
 * @param name
 *            表名
 * @param treeNode
 *            表的treeNode(表的元数据信息)
 */
function cloneTableDialog(id, name, treeNode) {
	$("#tabledialogdemo").append(
			$("#tabledialogdemo").clone().attr("id", "tabledialog" + id));
	var comments = treeNode.comments;
	var hidediv = "<div align='left'><input id='hidefields"
			+ id
			+ "' class='hidefields' onclick='hidefields(this);' type='checkbox'><span style='font-size: 10px; font-family: 黑体;'>隐未选中</span></div>";
	$("#tabledialog" + id).attr("title",
			"<u>" + comments + "</u>(" + name + ")");

	var fields = treeNode.field;
	var fieldtr = "";
	for ( var i = 0; i < fields.length; i++) {
		fieldtr = fieldtr
				+ "<tr class='list_table_tbody_tr'><td class='list_table_tbody_td'>"
				+ fields[i].comments
				+ "</td><td class='list_table_tbody_td'><input type='checkbox' onclick='fcheck(this);' class='fcheckbox' id='d_"
				+ id
				+ "_"
				+ fields[i].name
				+ "'></td><td class='list_table_tbody_td'><input type='checkbox' onclick='fcheck(this);' class='fcheckbox' id='c_"
				+ id
				+ "_"
				+ fields[i].name
				+ "'></td><td class='list_table_tbody_td'><input type='checkbox' onclick='fcheck(this);' class='fcheckbox' id='s_"
				+ id + "_" + fields[i].name + "'></td></tr>";
	}
	var tablelab = "<table border='0' cellpadding='0' cellspacing='1' width='100%' class='list_tableNoHtc' width='155'><tr class='list_table_thead_tr_title'><th class='list_table_thead_td_title' width='40%'>字段名</th><th class='list_table_thead_td_title' width='20%'>显示</th><th class='list_table_thead_td_title' width='20%'>条件</th><th class='list_table_thead_td_title' width='20%'>排序</th></tr>"
			+ fieldtr + "</table>";
	$("#tabledialog" + id).append(hidediv + tablelab);
	x = Math.random() * 500;
	$("#tabledialog" + id).dialog({
		autoOpen : false,
		width : 205,
		height : 300,
		resizable : false,
		position : [ x, 10 ],
		buttons : [ {
			text : "关闭",
			click : function() {
				$(this).dialog("close");
			}
		} ],
		beforeclose : function(event, ui) {
			if (confirm("关闭表格将会删除所有关联，确定删除吗?")) {
				flag = true;
				return true;
			} else {
				flag = false;
				return false;
			}
		}
	});
	$("#tabledialog" + id).bind("dialogclose", function(event, ui) {
		parent.setunchecked(id);
	});
}

/**
 * 显示、条件、排序checkBox事件
 * 
 * @param o
 *            checkBox 对象
 */
function fcheck(o) {
	parent.fcheck(o.checked, o.id);
}

/**
 * 初始化
 */
$(function() {
});