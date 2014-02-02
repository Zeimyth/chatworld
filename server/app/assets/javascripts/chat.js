(function() {

	var inherits = function(child, parent) {
		child.prototype = Object.create(parent.prototype);
		child.prototype.constructor = parent;
	};

	/**
	 * Initializes a new chat client with the given message of the day and display function.
	 *
	 * @param {string} motd The Message of the Day received from the server
	 * @param {function(string)} display The function to display a message to the user.
	 */
	var init = function(motd, display) {
		display(motd);
		display("It works!");
	};

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
			send('', '/disconnect', function() {});
		});
	};

	/**
	 * Parses the user's input for commands and takes the appropriate action.
	 */
	var parseInput = function(input) {

		var parseLineOfInput = function(line) {
			if (commands.say.test(line)) {
				commands.say.parse(line);
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
	 * @param {string=} content The message to send to the server
	 * @param {string=} url The url to send the message to
	 * @param {function(Object)} success Function to call on the json content returned by
	 *	the server
	 * @param {function} error Function to call when an error occurs
	 */
	var send = function(content, url, success, error) {
		if (!content && !url) {
			return;
		}

		content = content || null;
		url = url || '/echo';
		success = success || function(content) {
			display(content['message']);
		}
		error = error || function() {
			display('An error occurred during connection!', 'error');
		}

		$.ajax(url, {
			'contentType': 'application/json; charset=UTF-8',
			'data': JSON.stringify({'text': content}),
			'dataType': 'json',
			'success': function(data) {
				if (data.status == 'success') {
					success(data.content);
				}
			},
			'type': 'POST',
			'error': error
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
			// NOTE: Needs to check logged in status first?
			send(text, '/say');
		}

		Command.call(this, regex, action, regex);
		// this.__proto__ = Command.prototype;
	};
	inherits(Say, Command);

	var commands = {
		'say': new Say()
	};

	$(function() {
		setEventListeners();

		send('', '/connect', function(data) {
			display(data.motd);
		});
	});
})();