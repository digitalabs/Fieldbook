$(document).ready(function() {
	$('.help-tooltip').tooltip();
	$(".help-tooltip-nursery").tooltip();			
});
function createErrorNotification( titleDisplay, textDisplay){
	createNotification('default-error-notification',titleDisplay,textDisplay, false);
}
function createSuccessNotification( titleDisplay, textDisplay){
	//we remove all error
	$('.error-notify').remove();
	$('.warning-notify').remove();
	createNotification('default-notification',titleDisplay,textDisplay, 3000);
}
function createWarningNotification( titleDisplay, textDisplay){
	createNotification('default-warning-notification',titleDisplay,textDisplay, 3000);
}
function createNotification( template, titleDisplay, textDisplay, expires){
	var $container = $('#notification-container').notify();
	return $container.notify('create', template, {title: titleDisplay, text: textDisplay}, {expires: expires, click: function(e,instance){
		instance.close();
	}});
}