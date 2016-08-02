/* global $,bootbox */
(function() {
	'use strict';
	$('.page-header')
		.on('click', '.di-help', function() {
			var helpModule = $(this).data().helpLink;
			$.get('/ibpworkbench/controller/help/getUrl/' + helpModule).success(function(helpUrl) {
				if (!helpUrl || !helpUrl.length) {
					$.when(
						$.get('/ibpworkbench/controller/help/headerText'),
						$.get('/ibpworkbench/VAADIN/themes/gcp-default/layouts/help_not_installed.html')
					).done(function(headerText, helpHtml) {
							bootbox.dialog({
								title: headerText[0],
								message: helpHtml[0],
								className: 'help-box',
								onEscape: true
							});
						});
				} else {
					window.open(helpUrl);
				}
			});
		});

})();
