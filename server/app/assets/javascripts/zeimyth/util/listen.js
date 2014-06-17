goog.provide('zeimyth.util.listen');

/** @type {Object.<string, Array.<Function>>} */
var listeners = {};

/**
 * @param {string} event
 * @param {Function} callback
 */
zeimyth.util.listen = function(event, callback) {
	listeners[event] = listeners[event] || [];

	listeners[event].push(callback);
};

/**
 * @param {string} event
 * @param {*=} value
 */
zeimyth.util.listen.trigger = function(event, value) {
	if (listeners[event]) {
		listeners[event].forEach(function eachListener(callback) {
			try {
				callback(value);
			}
			catch (e) {
				window.console.log('Error caught during zeimyth.util.listen:', e);
			}
		});
	}
};
