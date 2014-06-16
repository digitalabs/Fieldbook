$(document).ready(function() {
	$('.help-tooltip').tooltip();
	$(".help-tooltip-nursery").tooltip();			
});
function createErrorNotification( titleDisplay, textDisplay){
	createNotification('default-error-notification',titleDisplay,textDisplay);
}
function createSuccessNotification( titleDisplay, textDisplay){
	createNotification('default-notification',titleDisplay,textDisplay);
}
function createNotification( template, titleDisplay, textDisplay){
	var $container = $('#notification-container').notify();
	return $container.notify('create', template, {title: titleDisplay, text: textDisplay}, {expires: 3000, click: function(e,instance){
		instance.close();
	}});
}