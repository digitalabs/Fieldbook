'use strict';
/*
Loading throbber.
*/
window.Spinner = (function() {

	// Delay in miliseconds
	var DELAY = 1000,
		timeout;

	function startThrobber() {
		$.blockUI({ message: '<div id="spinner-wrap"><img src="/Fieldbook/static/img/loading-animation.gif"/></div>' });
		$('.blockPage').css('border', '0px');
	}

	return {

		play: function() {
			// If there is already a waiting throbber, don't bother adding another one.
			if (!timeout) {
				timeout = setTimeout(startThrobber, DELAY);
			}
		},

		// This method is intended to be called when your asynchronous method has returned (e.g. you have hit the server and come back)
		stop: function() {
			// If there is an existing timeout, we want to clear it. This will cancel any waiting timeouts (for example, if you come back
			// within the delay) - ensuring the throbber does not appear unnecessarily.
			if (timeout) {
				clearTimeout(timeout);
				timeout = null;
			}

			// Stop the throbber if it has already started playing.
			$.unblockUI();
		},

		toggle: function() {
			if (timeout) {
				this.stop();
			} else {
				this.play();
			}
		}
	};

}());
