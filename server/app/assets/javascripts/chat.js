(function() {

	//-------------------------------------------------------------------------
	// Utility Functions
	//-------------------------------------------------------------------------

	var inherits = function(child, parent) {
		child.prototype = Object.create(parent.prototype);
		child.prototype.constructor = parent;
	};

	//-------------------------------------------------------------------------
	// Communication
	//-------------------------------------------------------------------------

	/**
	 * Displays the given message to the user.
	 * 
	 * @param {string} message The message to display
	 * @param {string} type The type of the message. This determines the class of the
	 * 	span that will contain the message, and thus the message color
	 */
	var display = function(message, type) {
		type = type || 'normal';

		var displayParsed = function(line) {
			var messageSpan = $('<span>').text(line).addClass(type);
			var messageDiv = $('<div>').addClass('text');
			messageDiv.append(messageSpan);
			$('#display-content').append(messageDiv);
		};

		message.split('\n').forEach(displayParsed);
		$('#display-content').animate({scrollTop:$('#display-content')[0].scrollHeight}, 800);
		// $('#display-content').scrollTop($('#display-content')[0].scrollHeight);
	};

	/**
	 * Sets up the event listeners for the page
	 */
	var setEventListeners = function() {
		$('#submit').click(function(e) {
			var input = $('#input').val();
			$('#input').val('');
			display(input, 'echo');
			parseInput(input);
		});

		$('#input').keypress(function(e) {
			if (e.keyCode == 13 || e.keyCode == 10) {
				if (e.ctrlKey) {
					var val = this.value;
					if (typeof this.selectionStart == "number" && typeof this.selectionEnd == "number") {
						var start = this.selectionStart;
						this.value = val.slice(0, start) + "\n" + val.slice(this.selectionEnd);
						this.selectionStart = this.selectionEnd = start + 1;
					}
					else if (document.selection && document.selection.createRange) {
						this.focus();
						var range = document.selection.createRange();
						range.text = "\r\n";
						range.collapse(false);
						range.select();
					}
				}
				else {
					$('#submit').click();
				}
				return false;
			}
		});

		$(window).bind('beforeunload', function() {
			$.ajax('/disconnect', {
				async: false,
				'type': 'POST'
			});
		});
	};

	var listenLoop = function() {
		// We want this loop to continue indefinitely, but we need to prevent a stack
		// overflow error
		setTimeout(function(){
			send(null, '/listen', function(data) {
					if (data['messages']) {
						data['messages'].forEach(function(message) {
							display(message.text, message.type);
						});
					}
					listenLoop();
				},
				function(errorMessage, errorType) {
					if (errorMessage) {
						display('An error occurred during listen loop! ' + errorMessage, 'error');
						listenLoop();
					}
					else if (errorType) {
						display('An error occurred during listen loop! ' + errorType, 'error');
						listenLoop();
					}
					else {
						display('The connection with the server has been lost.', 'error');
						// No response from server; end loop
					}
				}
			)
		}, 1000);
	};

	/**
	 * Parses the user's input for commands and takes the appropriate action.
	 */
	var parseInput = function(input) {

		var parseLineOfInput = function(line) {
			if (commands['say'].test(line)) {
				commands['say'].parse(line);
			}
			if (commands['emote'].test(line)) {
				commands['emote'].parse(line);
			}
			else if (commands['create'].test(line)) {
				commands['create'].parse(line);
			}
			else if (commands['login'].test(line)) {
				commands['login'].parse(line);
			}
			else {
				send(line);
			}
		};

		input.split('\n').forEach(parseLineOfInput);
	};

	/**
	 * Helper function for sending input to the server.
	 *
	 * @param {string|Object?} content The message to send to the server
	 * @param {string=} url The url to send the message to
	 * @param {function(Object)=} success Function to call on the json content returned by
	 *	the server
	 * @param {function=} error Function to call when an error occurs
	 */
	var send = function(content, url, success, error) {
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
			if (errorMessage) {
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
				if (data.status != 'failure') {
					success(data['content']);
				}
			},
			'type': 'POST',
			'error': function(xhr, text, e) {
				error(xhr.responseText, e);
			}
		});
	};

	//-------------------------------------------------------------------------
	// User Commands
	//-------------------------------------------------------------------------

	/**
	 * @constructor
	 */
	var Command = function(testRegex, action, parseRegex) {
		this.testRegex = testRegex || /.*/;
		this.parseRegex = parseRegex || /(.*)/;
		this.action = action || function(line) {send(line);};
	};

	Command.prototype.test = function(text) {
		return this.testRegex.test(text);
	};

	Command.prototype.parse = function(text) {
		var args = this.parseRegex.exec(text);
		args.splice(0, 1);
		this.action.apply(window, args);
	};

	var Say = function() {
		/*
		 * (?:            non-capturing group
		 *   "|(?:say\s)  quotation mark or "say" followed by whitespace
		 * )              end non-capturing group
		 * \s*            one or more whitespace characters
		 * (              group 1
		 *   .*           one or more of any character
		 * )              end group 1 - message the user is saying
		 */
		var regex = /^(?:"|(?:say\s))\s*(.*)/i
		var action = function(text) {
			send(text, '/say');
		}

		Command.call(this, regex, action, regex);
	};
	inherits(Say, Command);

	var Emote = function() {
		/*
		 * (?:              non-capturing group
		 *   :|(?:emote\s)  colon or "emote" followed by whitespace
		 * )                end non-capturing group
		 * \s*              one or more whitespace characters
		 * (                group 1
		 *   .*             one or more of any character
		 * )                end group 1 - message the user is saying
		 */
		var regex = /^(?::|(?:emote\s))\s*(.*)/i
		var action = function(text) {
			send(text, '/emote');
		}

		Command.call(this, regex, action, regex);
	}
	inherits(Emote, Command);

	var Create = function() {
		/*
		 * create\s+      "create" followed by whitespace
		 * (\w+)\s+       username followed by whitespace
		 * (\w+)          password
		 */
		var regex = /^create\s+(\w+)\s+(\w+)$/i
		var action = function(username, password) {
			// NOTE: Encode password
			send({'name': username, 'password': password}, '/create');
		}

		Command.call(this, regex, action, regex);
	}
	inherits(Create, Command);

	var Login = function() {
		/*
		 * login\s+       "login" followed by whitespace
		 * (\w+)\s+       username followed by whitespace
		 * (\w+)          password
		 */
		var regex = /^login\s+(\w+)\s+(\w+)$/i
		var action = function(username, password) {
			// NOTE: Encode password
			send({'name': username, 'password': password}, '/login');
		}

		Command.call(this, regex, action, regex);
	}
	inherits(Login, Command);

	var commands = {
		'say': new Say(),
		'emote': new Emote(),
		'create': new Create(),
		'login': new Login()
	};

	$(function() {
		setEventListeners();

		send('', '/connect', function(data) {
			display(data.motd);
			listenLoop();
		});
	});
})();