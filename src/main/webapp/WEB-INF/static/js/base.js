$(document).ready(function() {
	$('.help-tooltip').tooltip();
	$(".help-tooltip-nursery").tooltip();			
});
function createErrorNotification( titleDisplay, textDisplay){
	'use strict';
	createNotification('default-error-notification',titleDisplay,textDisplay, false);
}
function createSuccessNotification( titleDisplay, textDisplay){
	//we remove all error
	'use strict';
	$('.error-notify').remove();
	$('.warning-notify').remove();
	createNotification('default-notification',titleDisplay,textDisplay, 3000);
}
function createWarningNotification( titleDisplay, textDisplay){
	'use strict';
	createNotification('default-warning-notification',titleDisplay,textDisplay, 3000);
}
function createNotification( template, titleDisplay, textDisplay, expires){
	'use strict';
	var $container = $('#notification-container').notify();
	if(textDisplay !== null && textDisplay !== '' && textDisplay.length > 0) {
		if(textDisplay[textDisplay.length-1] !== '.') {
			textDisplay += '.';
		}
	}
	return $container.notify('create', template, {title: titleDisplay, text: textDisplay}, {expires: expires, click: function(e,instance){
		instance.close();
	}});
}