$(document).ready(function() {
	$('.help-tooltip').tooltip();
	$(".help-tooltip-nursery").tooltip();
	//this would handle the closing of modal since some modal window wont close on esc
	$('body').keydown(function(event){
		if(event.keyCode == 27){
			var length = $('.modal.in').length - 1;
			$($('.modal.in')[length]).modal('hide');
		}
	});
});
function createErrorNotification( titleDisplay, textDisplay){
	'use strict';
	createNotification('default-notification',titleDisplay,textDisplay, false, 'error-notify');
}
function createSuccessNotification( titleDisplay, textDisplay){
	//we remove all error
	'use strict';
	$('.error-notify').remove();
	$('.warning-notify').remove();
	createNotification('default-notification',titleDisplay,textDisplay, 3000, '');
}
function createWarningNotification( titleDisplay, textDisplay, duration){
	'use strict';
    if (! duration) {
        duration = 3000;
    }
	createNotification('default-notification',titleDisplay,textDisplay, duration, 'warning-notify');
}
function createNotification( template, titleDisplay, textDisplay, expires, className){
	'use strict';
	var $container = $('#notification-container').notify();
	if(textDisplay !== null && textDisplay !== '' && textDisplay.length > 0) {
		if(textDisplay[textDisplay.length-1] !== '.') {
			textDisplay += '.';
		}
	}
	var temp = $container.notify('create', template, {title: titleDisplay, text: textDisplay}, {expires: expires, click: function(e,instance){
		instance.close();
	}});
	$(temp.element).addClass(className);
}