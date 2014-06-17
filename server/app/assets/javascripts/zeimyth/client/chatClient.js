goog.provide('zeimyth.client.ChatClient');

goog.require('zeimyth.comm.CommandParser');
goog.require('zeimyth.comm.Notifier');
goog.require('zeimyth.util.listen');

/**
 * @constructor
 */
zeimyth.client.ChatClient = function() {
	this.notifier = new zeimyth.comm.Notifier();
	this.commandParser = new zeimyth.comm.CommandParser();

	this.listenForEvents();
};

/**
 * @private
 */
zeimyth.client.ChatClient.prototype.listenForEvents = function() {
	this.setWindowListeners();
	this.listenForInput();
};

/**
 * @private
 */
zeimyth.client.ChatClient.prototype.setWindowListeners = function() {
 	window.onfocus = function() {
		zeimyth.util.listen.trigger('window.onfocus');
	};

	window.onblur = function() {
		zeimyth.util.listen.trigger('window.onblur');
	};

	$(window).bind('beforeunload', function beforeUnload() {
		zeimyth.util.listen.trigger('window.beforeunload');
	});
 };

/**
 * @private
 */
zeimyth.client.ChatClient.prototype.listenForInput = function() {
	var me = this;

	$('#submit').click(function(e) {
		var input = $('#input').val();
		$('#input').val('');

		var lines = input.split('\n');
		lines.forEach(function(message) {
			if (message) {
				me.notifier.notifyMessageReceived(message, 'echo');
				me.commandParser.parseInput(message);
			}
		});

	});

	$('#input').keypress(function(e) {
		if (e.keyCode == 13 || e.keyCode == 10) {
			if (e.ctrlKey) {
				// Add a newline to the input instead of submitting
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
				// Enter from the text input is the same as clicking submit
				$('#submit').click();
			}
			return false;
		}
	});
};

goog.exportSymbol('zeimyth.client.ChatClient', zeimyth.client.ChatClient);
