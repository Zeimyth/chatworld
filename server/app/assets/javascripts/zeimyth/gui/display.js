goog.provide('zeimyth.gui.Display');

/**
 * @constructor
 */
zeimyth.gui.Display = function() {
	this.dom = $('#display-content');
};

/**
 * @param {string} message
 * @param {string} type
 */
zeimyth.gui.Display.prototype.printMessage = function(message, type) {
	var messageDiv = this.generateMessageDiv(message, type);

	// TODO: Only scroll if they are at the bottom of their display and the window
	// is active. Otherwise, list it as a missed message
	this.dom.append(messageDiv).animate({ scrollTop: this.dom.scrollHeight }, 800);
};

/**
 * @param {string} message
 * @param {string} type
 *
 * @private
 * @return {Object} jQuery
 */
zeimyth.gui.Display.prototype.generateMessageDiv = function(message, type) {
	var messageSpan = $('<span>').text(message).addClass(type);
	var messageDiv = $('<div>').addClass('text');
	messageDiv.append(messageSpan);

	return messageDiv;
};