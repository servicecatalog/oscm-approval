<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>


<c:set var="language" value="${pageContext.request.locale}"
	scope="session" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="i18n.messages" />
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge" />
<title><fmt:message key='title.application' /></title>

<link rel="stylesheet" type="text/css" href="approval.css">

<script type="text/javascript" src="jquery-1.10.2.js"></script>
<script type="text/javascript" src="jquery-ui-1.10.4.custom.min.js"></script>
<script type="text/javascript">
	// switches for filtering the task list
	var show_notifications = true;
	var show_finished_tasks = false;
	var show_open_tasks = true;
	var show_granted_clearances = false;
	var show_open_clearances = true;

	// switches for deleting tasks
	var delete_notifications = false;
	var delete_finished_tasks = false;
	var delete_granted_clearances = false;

	var selectedTask;

	$(document).ready(function() {
		loadTaskList();
	});

	function enableButton(id) {

		if (id == 'approve_button') {
			$('span#approve_button').remove();
			$('a#approve_button').remove();
			$('td#approve_button')
					.append(
							'<a id="approve_button" href="" onclick="approveTask();return false;" class="button"><img src="img/transparentPixel.png" class="buttonImg approve"><span><fmt:message key='btn.label.approve'/></span></a>');
		}

		if (id == 'reject_button') {
			$('span#reject_button').remove();
			$('a#reject_button').remove();
			$('td#reject_button')
					.append(
							'<a id="reject_button" href="" onclick="rejectTask();return false;" class="button"><img src="img/transparentPixel.png" class="buttonImg reject"><span><fmt:message key='btn.label.reject'/></span></a>');
		}

		if (id == 'grant_clearance_button') {
			$('span#grant_clearance_button').remove();
			$('a#grant_clearance_button').remove();
			$('td#grant_clearance_button')
					.append(
							'<a id="grant_clearance_button" href="" onclick="grantClearance();return false;" class="button"><img src="img/transparentPixel.png" class="buttonImg grant_clearance"><span><fmt:message key='btn.label.grant_clearance'/></span></a>');
		}

		if (id == 'details_button') {
			$('span#details_button').remove();
			$('a#details_button').remove();
			$('td#details_button')
					.append(
							'<a id="details_button" href="" onclick="showTaskDetails();return false;" class="button"><img src="img/transparentPixel.png" class="buttonImg details"><span><fmt:message key='btn.label.details'/></span></a>');
		}
	}

	function disableButtons() {
		$('span#approve_button').remove();
		$('a#approve_button').remove();
		// $('td#approve_button').append('<span id="approve_button" class="buttonDisabled"><span><img src="img/transparentPixel.png" class="buttonImg approve"><span><fmt:message key='btn.label.approve'/></span></span>');

		$('span#reject_button').remove();
		$('a#reject_button').remove();
		// $('td#reject_button').append('<span id="reject_button" class="buttonDisabled"><span><img src="img/transparentPixel.png" class="buttonImg reject"><span><fmt:message key='btn.label.reject'/></span></span>');

		$('span#grant_clearance_button').remove();
		$('a#grant_clearance_button').remove();
		// $('td#grant_clearance_button').append('<span id="grant_clearance_button" class="buttonDisabled"><span><img src="img/transparentPixel.png" class="buttonImg grant_clearance"><span><fmt:message key='btn.label.grant_clearance'/></span></span>');

		$('span#details_button').remove();
		$('a#details_button').remove();
		// $('td#details_button').append('<span id="details_button" class="buttonDisabled"><span><img src="img/transparentPixel.png" class="buttonImg details"><span><fmt:message key='btn.label.details'/></span></span>');
	}

	function showTaskDetailsImpl(par, description, table) {
		$
				.each(
						par,
						function(i, obj) {

							var selectedParam = par[i].trim();
							try {
								var value = description.ctmg_service.params[selectedParam].value;
								var label = description.ctmg_service.params[selectedParam].label;

								createRow(table, label, value);

							} catch (e) {
								console.log(e);
							}
						});
	}

	function createRow(table, label, value) {
		console.log("value:" + value + " label:" + label);

		if (value != '' && value != undefined) {
			var col1 = $('<td></td>').text(label);
			var input = $('<input type="text" size="30" readonly/>').val(value);
			if (value == 'true' || value == 'false') {
				input = $('<input type="checkbox"/>').prop('checked',
						(value == 'true')).prop('disabled', 'disabled');
			}
			var col2 = $('<td></td>').append(input);
			var row = $('<tr></tr>');
			row.append(col1);
			row.append(col2);
			table.append(row);
		}

	}

	function showTaskImpl(par, description, table) {
		$
				.each(
						par,
						function(i, obj) {

							var selectedParam = par[i].trim();
							try {
								var value = description.ctmg_service.params[selectedParam].value;
								var label = description.ctmg_service.params[selectedParam].label;
								console.log("value:" + value + " label:"
										+ label);
								if (value != '' && value != undefined) {
									var col1 = $('<td></td>').text(label);
									var input = $(
											'<input type="text" size="30" readonly/>')
											.val(value);
									input.prop('id', '${label}');
									if (value == 'true' || value == 'false') {
										input = $('<input type="checkbox"/>')
												.prop('checked',
														(value == 'true'))
												.prop('disabled', 'disabled');
									}
									var col2 = $('<td></td>').append(input);
									var row = $('<tr></tr>');
									row.append(col1);
									row.append(col2);
									table.append(row);
								}
							} catch (e) {
								console.log(e);
							}
						});
	}

	function clearTaskDetailsImpl(par, description) {

		$
				.each(
						par,
						function(i, obj) {

							var selectedParam = par[i].trim();
							try {

								if (description.ctmg_service != undefined) {
									var label = description.ctmg_service.params[selectedParam].label;
									$("input").find(`[id='${label}']`).val('');
								}
							} catch (e) {
								console.log(e);
							}
						});
	}

	function clearTaskDetails(description) {
		try {

			if (description.ctmg_service != undefined) {
				var displayParams = description.ctmg_service.params['DISPLAY_PARAMS'].value;
				if (displayParams != '' && displayParams != undefined) {
					params = displayParams.split(",");
				}
				clearTaskDetailsImpl(params, description);
			}

		} catch (e) {
			console.log(e);
		}
		clearTaskView();
	}

	function showTaskDetails() {
		console.log('showTaskDetails()  tkey: ' + selectedTask);
		var json = httpGet('task?cmd=details&tkey=' + selectedTask);
		var task = jQuery.parseJSON(json);
		var description = jQuery.parseJSON(task.description);
		var params = [ 'OPERATING_SYSTEM', 'TEMPLATENAME',
				'TARGET_VCENTER_SERVER', 'TARGET_DATACENTER', 'TARGET_CLUSTER',
				'TARGET_HOST', 'TARGET_STORAGE', 'TARGET_FOLDER' ];

		var table = $('<table cellspacing="4" cellpadding="4" border="0" width="100%"></table>');

		try {

			if (description.ctmg_service != undefined) {
				var displayParams = description.ctmg_service.params['DISPLAY_PARAMS'].value;
				if (displayParams != '' && displayParams != undefined) {
					params = displayParams.split(",");
				}
				showTaskDetailsImpl(params, description, table)
			}
			createRow(table, "<fmt:message key='label.requestingUser'/>",
					getUserDisplay(description.ctmg_user));
		} catch (e) {
			console.log(e);
		}

		$('#details_dialog').html(table);
		$('#details_dialog').dialog('open');
	}

	function filterTaskList() {
		var table = $('<table cellspacing="4" cellpadding="4" border="0" width="100%"></table>');
		var cbx1 = $('<input id="show_notifications" type="checkbox">');
		var cbx2 = $('<input id="show_finished_tasks" type="checkbox">');
		var cbx3 = $('<input id="show_open_tasks" type="checkbox">');
		var cbx4 = $('<input id="show_granted_clearances" type="checkbox">');
		var cbx5 = $('<input id="show_open_clearances" type="checkbox">');

		var row = $('<tr></tr>');
		var col1 = $('<td></td>').append(cbx1);
		var col2 = $('<td></td>').text(
				"<fmt:message key='label.notifications'/>");
		row.append(col1);
		row.append(col2);
		table.append(row);

		row = $('<tr></tr>');
		col1 = $('<td></td>').append(cbx2);
		col2 = $('<td></td>').text("<fmt:message key='label.finished_tasks'/>");
		row.append(col1);
		row.append(col2);
		table.append(row);

		row = $('<tr></tr>');
		col1 = $('<td></td>').append(cbx3);
		col2 = $('<td></td>').text("<fmt:message key='label.open_tasks'/>");
		row.append(col1);
		row.append(col2);
		table.append(row);

		row = $('<tr></tr>');
		col1 = $('<td></td>').append(cbx4);
		col2 = $('<td></td>').text(
				"<fmt:message key='label.granted_clearances'/>");
		row.append(col1);
		row.append(col2);
		table.append(row);

		row = $('<tr></tr>');
		col1 = $('<td></td>').append(cbx5);
		col2 = $('<td></td>')
				.text("<fmt:message key='label.open_clearances'/>");
		row.append(col1);
		row.append(col2);
		table.append(row);

		$('#filter_dialog').html(table);
		$('#filter_dialog').dialog(
				{
					open : function(event, ui) {
						$('#show_notifications').prop('checked',
								show_notifications);
						$('#show_finished_tasks').prop('checked',
								show_finished_tasks);
						$('#show_open_tasks').prop('checked', show_open_tasks);
						$('#show_granted_clearances').prop('checked',
								show_granted_clearances);
						$('#show_open_clearances').prop("checked",
								show_open_clearances);
					}
				});
		$('#filter_dialog').dialog('open');
	}

	function showTask(tkey) {
		selectedTask = tkey;
		console.log('showTask()  tkey: ' + tkey);

		var json = httpGet('task?cmd=open&tkey=' + tkey);
		var task = jQuery.parseJSON(json);
		var description = jQuery.parseJSON(task.description);
		var table = $("#details_table");
		clearTaskDetails(description);
		disableButtons();
		enableButton('details_button');
		$('input#activity').val(description.ctmg_trigger_name);

		if (task.status == 'WAITING_FOR_APPROVAL') {
			$('input#status')
					.val(
							"<fmt:message key='APPROVAL_STATUS_WAITING_FOR_APPROVAL'/>");
			enableButton('reject_button');
			enableButton('approve_button');
		}
		if (task.status == 'WAITING_FOR_CLEARANCE') {
			$('input#status')
					.val(
							"<fmt:message key='APPROVAL_STATUS_WAITING_FOR_CLEARANCE'/>");
			enableButton('grant_clearance_button');
		}
		if (task.status == 'TIMEOUT') {
			$('input#status').val(
					"<fmt:message key='APPROVAL_STATUS_TIMEOUT'/>");
		}
		if (task.status == 'FAILED') {
			$('input#status')
					.val("<fmt:message key='APPROVAL_STATUS_FAILED'/>");
		}
		if (task.status == 'APPROVED') {
			$('input#status').val(
					"<fmt:message key='APPROVAL_STATUS_APPROVED'/>");
		}
		if (task.status == 'CLEARANCE_GRANTED') {
			$('input#status').val(
					"<fmt:message key='APPROVAL_STATUS_CLEARANCE_GRANTED'/>");
		}
		if (task.status == 'REJECTED') {
			$('input#status').val(
					"<fmt:message key='APPROVAL_STATUS_REJECTED'/>");
		}
		if (task.status == 'NOTIFICATION') {
			$('input#status').val(
					"<fmt:message key='APPROVAL_STATUS_NOTIFICATION'/>");
		}

		$('input#created').val(task.created);
		$('input#organization').val(description.ctmg_organization.name);
		$('input#subscription').val(description.ctmg_subscription.id);
		$('textarea#comment').val(task.comment);

		if (description.ctmg_trigger_id == 'onModifySubscription'
				|| description.ctmg_trigger_id == 'onSubscribeToProduct') {

			if (description.ctmg_user.lastname != undefined) {
				$('td#requesting_person').text(
						"<fmt:message key='label.requestingUser'/>");
				$('input#requesting_person').val(
						getUserDisplay(description.ctmg_user));
			} else {
				$('td#requesting_person').text('');
				$('input#requesting_person').val('');
			}
			if (description.ctmg_service.params.RESPONSIBLE_PERSON != undefined) {
				$('td#responsible_person')
						.text(
								description.ctmg_service.params.RESPONSIBLE_PERSON.label);
				$('input#responsible_person')
						.val(
								description.ctmg_service.params.RESPONSIBLE_PERSON.value);
				$('div#old_responsible_person').text('');
			} else {
				$('td#responsible_person').text('');
				$('input#responsible_person').val('');
				$('div#old_responsible_person').text('');
			}

		}
	}

	function getUserDisplay(user) {
		var salutation = "<fmt:message key='label.msormr' /> ";
		if (user.salutation != undefined) {
			if (user.salutation == 'MR') {
				salutation = "<fmt:message key='label.mr' /> ";
			} else if (user.salutation == 'MS') {
				salutation = "<fmt:message key='label.ms'/> ";
			}
		}

		var lastname = "";
		if (user.lastname != undefined) {
			lastname = user.lastname;
		}
		var fullname = salutation.concat(lastname);
		if (user.firstname != undefined) {
			var firstname = user.firstname;
			fullname = salutation.concat(firstname, " ", lastname);
		}

		return fullname;
	}

	function saveTask() {
		if (selectedTask == '') {
			var message = "<fmt:message key='info.no_task'/><br>";
			$('#info_dialog').html(message);
			$('#info_dialog').dialog('open');
			return;
		}
		var comment = $('textarea#comment').val();
		var json = '{"tkey":"' + selectedTask + '","comment":"' + comment
				+ '"}';
		var response = httpPost('task?cmd=save', json);
		if (response == '') {
			var message = "<fmt:message key='success.save_task'/><br>";
			$('#info_dialog').html(message);
			$('#info_dialog').dialog('open');
		} else {
			var resp = jQuery.parseJSON(response);
			var message = "<fmt:message key='error.save_task'/><br>";
			$('#error_dialog').html(message + resp.status);
			$('#error_dialog').dialog('open');
		}
	}

	function approveTask() {
		if (selectedTask == '') {
			var message = "<fmt:message key='info.no_task'/><br>";
			$('#info_dialog').html(message);
			$('#info_dialog').dialog('open');
			return;
		}
		var comment = $('textarea#comment').val();
		var json = '{"tkey":"' + selectedTask + '","comment":"' + comment
				+ '"}';
		var response = httpPost('task?cmd=approve', json);
		if (response == '') {
			var message = "<fmt:message key='success.approve_task'/><br>";
			$('#info_dialog').html(message);
			$('#info_dialog').dialog('open');
			loadTaskList();
		} else {
			var resp = jQuery.parseJSON(response);
			var message = "<fmt:message key='error.approve_task'/><br>";
			$('#error_dialog').html(message + resp.status);
			$('#error_dialog').dialog('open');
		}
	}

	function rejectTask() {
		if (selectedTask == '') {
			var message = "<fmt:message key='info.no_task'/><br>";
			$('#info_dialog').html(message);
			$('#info_dialog').dialog('open');
			return;
		}
		var comment = $('textarea#comment').val();
		var json = '{"tkey":"' + selectedTask + '","comment":"' + comment
				+ '"}';
		var response = httpPost('task?cmd=reject', json);
		if (response == '') {
			var message = "<fmt:message key='success.reject_task'/><br>";
			$('#info_dialog').html(message);
			$('#info_dialog').dialog('open');
			loadTaskList();
		} else {
			var resp = jQuery.parseJSON(response);
			var message = "<fmt:message key='error.reject_task'/><br>";
			$('#error_dialog').html(message + resp.status);
			$('#error_dialog').dialog('open');
		}
	}

	function grantClearance() {
		if (selectedTask == '') {
			var message = "<fmt:message key='info.no_task'/><br>";
			$('#info_dialog').html(message);
			$('#info_dialog').dialog('open');
			return;
		}

		var comment = $('textarea#comment').val();
		var json = '{"tkey":"' + selectedTask + '","comment":"' + comment
				+ '"}';
		var response = httpPost('task?cmd=grant_clearance', json);
		if (response == '') {
			var message = "<fmt:message key='success.approve_task'/><br>";
			$('#info_dialog').html(message);
			$('#info_dialog').dialog('open');
			loadTaskList();
		} else {
			var resp = jQuery.parseJSON(response);
			var message = "<fmt:message key='error.approve_task'/><br>";
			$('#error_dialog').html(message + resp.status);
			$('#error_dialog').dialog('open');
		}
	}

	function getStatusIcon(status_tkey) {
		var WAITING_FOR_APPROVAL = '1';
		var TIMEOUT = '2';
		var FAILED = '3';
		var APPROVED = '4';
		var REJECTED = '5';
		var NOTIFICATION = '6';
		var WAITING_FOR_CLEARANCE = '7';
		var CLEARANCE_GRANTED = '8';
		var icon = '';
		switch (status_tkey) {
		case WAITING_FOR_APPROVAL:
			icon = '<img src="img/transparentPixel.png" class="buttonImg waiting">';
			break;
		case TIMEOUT:
			icon = '<img src="img/transparentPixel.png" class="buttonImg timout">';
			break;
		case FAILED:
			icon = '<img src="img/transparentPixel.png" class="buttonImg failed">';
			break;
		case APPROVED:
			icon = '<img src="img/transparentPixel.png" class="buttonImg approve">';
			break;
		case REJECTED:
			icon = '<img src="img/transparentPixel.png" class="buttonImg reject">';
			break;
		case NOTIFICATION:
			icon = '<img src="img/transparentPixel.png" class="buttonImg details">';
			break;
		case WAITING_FOR_CLEARANCE:
			icon = '<img src="img/transparentPixel.png" class="buttonImg waiting">';
			break;
		case CLEARANCE_GRANTED:
			icon = '<img src="img/transparentPixel.png" class="buttonImg approve">';
			break;
		default:
			console.log('unknown status key ' + status_tkey);
		}
		return icon;
	}

	function clearTaskView() {
		$('input#activity').val('');
		$('input#status').val('');
		$('input#created').val('');
		$('input#organization').val('');
		$('input#subscription').val('');
		$('input#responsible_person').val('')
		$('input#requesting_person').val('');
		$('textarea#comment').val('');
	}

	function loadTaskList() {
		disableButtons();
		clearTaskView();
		selectedTask = '';

		$("table#task_table").find('tbody').children().remove();
		var json = httpGet('task?cmd=tasklist&show_notifications='
				+ show_notifications + '&show_finished_tasks='
				+ show_finished_tasks + '&show_open_tasks=' + show_open_tasks
				+ '&show_granted_clearances=' + show_granted_clearances
				+ '&show_open_clearances=' + show_open_clearances);
		var taskArray = jQuery.parseJSON(json);

		if (taskArray.length == 0) {
			var message = "<fmt:message key='info.no_tasks_assigned'/><br>";
			var row = $("<tr>");
			var col = $("<td>");
			col.append().html(message);
			row.append(col);
			$('table#task_table').find('tbody').append(row);
			return;
		}

		for ( var i in taskArray) {
			var task = taskArray[i];
			var row = $("<tr>");
			row.data('tkey', task.tkey);
			var col1 = $("<td>");
			var icon = getStatusIcon(task.status_tkey);
			col1.append().html(icon);
			var col2 = $("<td>");
			col2.append().html(task.triggername);
			var col3 = $("<td>");
			col3.append().html(task.requestinguser);
			row.append(col1);
			row.append(col2);
			row.append(col3);
			$('table#task_table').find('tbody').append(row);

			row.hover(function() {
				$(this).addClass('hover');
			}, function() {
				$(this).removeClass('hover');
			});

			row.click(function() {
				$("table#task_table").find('tbody').children().removeClass(
						'selected');
				$(this).addClass('selected');
				var tkey = $(this).data('tkey');
				showTask(tkey);
			});
		}
	}

	function deleteTasks() {
		var table = $('<table cellspacing="4" cellpadding="4" border="0" width="100%"></table>');
		var cbx1 = $('<input id="delete_notifications" type="checkbox">');
		var cbx2 = $('<input id="delete_finished_tasks" type="checkbox">');
		var cbx3 = $('<input id="delete_granted_clearances" type="checkbox">');

		var row = $('<tr></tr>');
		var col1 = $('<td></td>').append(cbx1);
		var col2 = $('<td></td>').text(
				"<fmt:message key='label.notifications'/>");
		row.append(col1);
		row.append(col2);
		table.append(row);

		row = $('<tr></tr>');
		col1 = $('<td></td>').append(cbx2);
		col2 = $('<td></td>').text("<fmt:message key='label.finished_tasks'/>");
		row.append(col1);
		row.append(col2);
		table.append(row);

		row = $('<tr></tr>');
		col1 = $('<td></td>').append(cbx3);
		col2 = $('<td></td>').text(
				"<fmt:message key='label.granted_clearances'/>");
		row.append(col1);
		row.append(col2);
		table.append(row);

		$('#delete_dialog').html(table);
		$('#delete_dialog').dialog(
				{
					open : function(event, ui) {
						$('#delete_notifications').prop('checked',
								delete_notifications);
						$('#delete_finished_tasks').prop('checked',
								delete_finished_tasks);
						$('#delete_granted_clearances').prop('checked',
								delete_granted_clearances);
					}
				});

		$('#delete_dialog').dialog('open');
	}

	function createUID() {
		var s = [];
		var digits = "0123456789abcdefghijklmnopqrstuvwxyz";
		s[0] = 'a';
		for (var i = 1; i < 20; i++) {
			s[i] = digits.substr(Math.floor(Math.random() * 0x10), 1);
		}

		var uid = s.join("");
		return uid;
	}

	function httpPost(theUrl, content) {
		var xmlHttp = null;
		xmlHttp = new XMLHttpRequest();
		xmlHttp.open("POST", theUrl, false);
		xmlHttp.send(content);
		return xmlHttp.responseText;
	}

	function httpGet(theUrl) {
		var xmlHttp = null;
		xmlHttp = new XMLHttpRequest();
		xmlHttp.open("GET", theUrl, false);
		xmlHttp.send(null);
		return xmlHttp.responseText;
	}

	$(function() {
		$('#error_dialog').dialog({
			autoOpen : false,
			modal : true,
			dialogClass : "no-close",
			show : {
				effect : "fadeIn",
				duration : 100
			},
			hide : {
				effect : "fadeOut",
				duration : 0
			},
			buttons : {
				"<fmt:message key='btn.label.close'/>" : function() {
					$(this).dialog("close");
				}
			}
		});
	});

	$(function() {
		$('#info_dialog').dialog({
			autoOpen : false,
			modal : true,
			dialogClass : "no-close",
			show : {
				effect : "fadeIn",
				duration : 100
			},
			hide : {
				effect : "fadeOut",
				duration : 0
			},
			buttons : {
				"<fmt:message key='btn.label.ok'/>" : function() {
					$(this).dialog("close");
				}
			}
		});
	});

	$(function() {
		$('#details_dialog').dialog({
			autoOpen : false,
			modal : true,
			width : "auto",
			dialogClass : "no-close",
			show : {
				effect : "fadeIn",
				duration : 100
			},
			hide : {
				effect : "fadeOut",
				duration : 0
			},
			buttons : {
				"<fmt:message key='btn.label.close'/>" : function() {
					$(this).dialog("close");
				}
			}
		});
	});

	$(function() {
		$('#delete_dialog')
				.dialog(
						{
							autoOpen : false,
							modal : true,
							width : "auto",
							dialogClass : "no-close",
							show : {
								effect : "fadeIn",
								duration : 100
							},
							hide : {
								effect : "fadeOut",
								duration : 0
							},
							buttons : {
								"<fmt:message key='btn.label.delete'/>" : function() {
									delete_notifications = $(
											'#delete_notifications').prop(
											'checked');
									delete_finished_tasks = $(
											'#delete_finished_tasks').prop(
											'checked');
									delete_granted_clearances = $(
											'#delete_granted_clearances').prop(
											'checked');
									var response = httpGet('task?cmd=delete&delete_notifications='
											+ delete_notifications
											+ '&delete_finished_tasks='
											+ delete_finished_tasks
											+ '&delete_granted_clearances='
											+ delete_granted_clearances);
									$(this).dialog("close");
									if (response == '') {
										loadTaskList();
									} else {
										var resp = jQuery.parseJSON(response);
										var message = "<fmt:message key='error.delete_tasks'/><br>";
										$('#error_dialog').html(
												message + resp.status);
										$('#error_dialog').dialog('open');
									}
								},
								"<fmt:message key='btn.label.cancel'/>" : function() {
									$(this).dialog("close");
								}
							}
						});
	});

	$(function() {
		$('#filter_dialog')
				.dialog(
						{
							autoOpen : false,
							modal : true,
							width : "auto",
							dialogClass : "no-close",
							show : {
								effect : "fadeIn",
								duration : 100
							},
							hide : {
								effect : "fadeOut",
								duration : 0
							},
							buttons : {
								"<fmt:message key='btn.label.filter'/>" : function() {
									show_notifications = $(
											'#show_notifications').prop(
											'checked');
									show_finished_tasks = $(
											'#show_finished_tasks').prop(
											'checked');
									show_open_tasks = $('#show_open_tasks')
											.prop('checked');
									show_granted_clearances = $(
											'#show_granted_clearances').prop(
											'checked');
									show_open_clearances = $(
											'#show_open_clearances').prop(
											"checked");
									$(this).dialog("close");
									loadTaskList();
								},
								"<fmt:message key='btn.label.cancel'/>" : function() {
									$(this).dialog("close");
								}
							}
						});
	});
</script>

</head>

<body>
	<div id="info_dialog" title="<fmt:message key='title.info'/>"></div>
	<div id="error_dialog" title="<fmt:message key='title.error'/>"></div>
	<div id="details_dialog" title="<fmt:message key='title.taskdetails'/>"></div>
	<div id="delete_dialog" title="<fmt:message key='title.delete'/>"></div>
	<div id="filter_dialog" title="<fmt:message key='title.filter'/>"></div>


	<div class="wrapper" style="display: inline;">
		<div id="inner-left" class="block" style="width: 35%; height: 650px;">
			<h4>
				<fmt:message key='title.tasklist' />
			</h4>
			<div style="position: relative; width: 100%; height: 535px;"
				class="scrollingtable">
				<!-- style="position: relative; width: 95%; height: 85%;"  -->
				<table id="task_table" class="pointer" width="100%">
					<tbody>
					</tbody>
				</table>
			</div>
			<div class="buttonPanel">
				<table cellspacing="0" cellpadding="2" border="0" width="70%">
					<tbody>
						<tr>
							<td><a href="" onclick="loadTaskList();return false;"
								class="button"> <img src="img/transparentPixel.png"
									class="buttonImg refresh"><span><fmt:message
											key='btn.label.refresh' /></span>
							</a></td>
							<td><a href="" onclick="deleteTasks();return false;"
								class="button"> <img src="img/transparentPixel.png"
									class="buttonImg delete"><span><fmt:message
											key='btn.label.delete_tasks' /></span>
							</a></td>
							<td><a href="" onclick="filterTaskList();return false;"
								class="button"> <img src="img/transparentPixel.png"
									class="buttonImg filter"><span><fmt:message
											key='btn.label.filter_tasklist' /></span>
							</a></td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
		<div id="inner-right" class="block2"
			style="height: 650px; width: 60%;">
			<div class="block" style="width: 95%;">

				<h4>
					<fmt:message key='title.task' />
				</h4>

				<div id="task_details"
					style="position: relative; width: 95%; height: 545px;">
					<table id="details_table" cellspacing="4" cellpadding="4"
						border="0" width="100%">
						<tbody>
							<tr>
								<td id="requesting_person"><fmt:message
										key='label.requestingUser' /></td>
								<td><input id="requesting_person" type="text" size="30"
									readonly /></td>
							</tr>
							<tr>
								<td><fmt:message key='label.activity' /></td>
								<td><input id="activity" type="text" size="30" readonly /></td>
							</tr>
							<tr>
								<td><fmt:message key='label.status' /></td>
								<td><input id="status" type="text" size="30" readonly /></td>
							</tr>
							<tr>
								<td><fmt:message key='label.created' /></td>
								<td><input id="created" type="text" size="30" readonly /></td>
							</tr>
							<tr>
								<td><fmt:message key='label.organization' /></td>
								<td><input id="organization" type="text" size="30" readonly /></td>
							</tr>
							<tr>
								<td><fmt:message key='label.subscription' /></td>
								<td><input id="subscription" type="text" size="30" readonly /></td>
							</tr>

							<tr>
								<td style="Vertical-align: text-top;"><fmt:message
										key='label.comment' /></td>
								<td><textarea id="comment" cols="45" rows="3"
										style="resize: none;"></textarea></td>
							</tr>
						</tbody>
					</table>
				</div>

				<div class="buttonPanel">
					<table cellspacing="0" cellpadding="2" border="0" width="50%">
						<tbody>
							<tr>
								<td id="approve_button"><a id="approve_button" href=""
									onclick="approveTask();return false;" class="button"> <img
										src="img/transparentPixel.png" class="buttonImg approve"><span><fmt:message
												key='btn.label.approve' /></span>
								</a></td>
								<td id="reject_button"><a id="reject_button" href=""
									onclick="rejectTask();return false;" class="button"> <img
										src="img/transparentPixel.png" class="buttonImg reject"><span><fmt:message
												key='btn.label.reject' /></span>
								</a></td>
								<td id="grant_clearance_button"><a
									id="grant_clearance_button" href=""
									onclick="grantClearance();return false;" class="button"> <img
										src="img/transparentPixel.png"
										class="buttonImg grant_clearance"><span><fmt:message
												key='btn.label.grant_clearance' /></span>
								</a></td>
								<td id="details_button"><a id="details_button" href=""
									onclick="showTaskDetails();return false;" class="button"> <img
										src="img/transparentPixel.png" class="buttonImg details"><span><fmt:message
												key='btn.label.details' /></span>
								</a></td>
							</tr>
						</tbody>
					</table>
				</div>

			</div>
		</div>
	</div>

</body>
</html>