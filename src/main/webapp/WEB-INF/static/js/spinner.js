'use strict';
/*
Loading throbber.
*/
window.Spinner = {

	active: false,

	play: function() {
		$.blockUI({ message: '<div id="spinner-wrap"><img src="/Fieldbook/static/img/loading-animation.gif"/></div>' });
		$('.blockPage').css('border', '0px');
		this.active = true;
	},

	stop: function() {
		$.unblockUI();
		this.active = false;
	},

	toggle: function() {
		if (this.active) {
			this.stop();
		} else {
			this.play();
		}
	}
};
