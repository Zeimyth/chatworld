goog.provide('zeimyth.comm.Notifier');

goog.require('zeimyth.gui.Display');
goog.require('zeimyth.util.listen');


/**
 * @constructor
 */
zeimyth.comm.Notifier = function() {
	this.hasFocus = true;
	this.docTitle = document.title;
	this.missedMessages = 0;

	this.display = new zeimyth.gui.Display();

	this.setupListeners();
};

/**
 * @private
 */
zeimyth.comm.Notifier.prototype.setupListeners = function() {
	zeimyth.util.listen('window.focus', function() {
		this.hasFocus = true;
		this.missedMessages = 0;
		document.title = this.docTitle;
	});

	zeimyth.util.listen('window.unfocus', function() {
		this.hasFocus = false;
	});
};

/**
 * @param {string} message
 * @param {string=} type
 */
zeimyth.comm.Notifier.prototype.notifyMessageReceived = function(message, type) {
	// TODO: Make type an enum
	type = type || 'normal';

	this.display.printMessage(message, type);

	if (!this.hasFocus) {
		this.missedMessages++;
		document.title = "(" + this.missedMessages + ") " + this.docTitle;
	}
};