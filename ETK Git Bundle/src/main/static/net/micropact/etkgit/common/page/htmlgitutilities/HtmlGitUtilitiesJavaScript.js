/**
 *
 * HtmlGitUtilitiesJavaScript
 *
 * administrator 05/26/2020
 **/
function init() {
	jQuery('#rebuildButton').click(rebuildLocalRepository);
	jQuery('#syncButton').click(syncSystemRepository);
}

function syncSystemRepository() {
	EntellitrakOAuth.ajaxCurrentUser("SciGitEndpoint/syncSystemRepository", {
			dataType: "json",
			method: 'POST',
			beforeSend: function() { show('loading'); }
		})
		.done(function(data) { addDataToResultAndLog(data); })
		.fail(function(data) { addDataToResultAndLog(data); });
}

function rebuildLocalRepository() {
	EntellitrakOAuth.ajaxCurrentUser("SciGitEndpoint/rebuildLocalRepository", {
		dataType: "json",
		method: 'POST',
		beforeSend: function() { show('loading'); }
	})
	.done(function(data) { addDataToResultAndLog(data); })
	.fail(function(data) { addDataToResultAndLog(data); });
}

function addDataToResultAndLog(data) {
	hide('loading');

	var message;
	if (console) {
		console.log(data);
	}
	
	if (data.error) {
		message = 'ERROR: ' + data.error;
		console.error(message);
	} else if (data.responseJSON && data.responseJSON.error) {
		message = 'ERROR: ' + data.responseJSON.error;
		console.error(message);
	} else {
		message = data.detail;
		console.log(message);
	}

	jQuery('#resultText').val(message);
}

addOnloadEvent(init);