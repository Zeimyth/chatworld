goog.provide('com.zeimyth.net.send');

/**
 * Helper function for sending input to the server.
 *
 * @param {string|Object?} content The message to send to the server
 * @param {string=} url The url to send the message to
 * @param {function(Object)=} success Function to call on the json content returned by
 *	the server
 * @param {function=} error Function to call when an error occurs
 */
com.zeimyth.net.send = function(content, url, success, error) {
	if (!content && !url) {
		return;
	}

	// content = content || null;
	if (typeof content == 'string') {
		content = {'text': content};
	}

	url = url || '/echo';
	success = success || function(content) {
		display(content['message'], content['status']);
	}
	error = error || function(errorMessage, errorClass) {
		if (typeof errorMessage == "string") {
			try {
				var data = JSON.parse(errorMessage);
			} catch (error) {
				var data = null;
			}
			var message = data && data['content'] && data['content']['message'];
			if (message) {
				display(message, 'error');
			}
			else {
				display(errorMessage, 'error');
			}
		}
		else {
			display(errorClass, 'error');
		}
	}

	$.ajax(url, {
		'contentType': 'application/json; charset=UTF-8',
		'data': JSON.stringify(content),
		'dataType': 'json',
		'success': function(data) {
			if (data['status'] != 'failure' && data['content']) {
				success(data['content']);
			}
		},
		'type': 'POST',
		'error': function(xhr, text, e) {
			if (text == 'error') {
				error(xhr.responseText, e);
			}
			else {
				display('Unrecognized response from the server: ' + e, 'error');
			}
		}
	});
};