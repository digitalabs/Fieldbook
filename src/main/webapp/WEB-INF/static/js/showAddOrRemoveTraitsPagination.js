
function onMeasurementsObservationLoad(isCategoricalDisplay) {
	'use strict';
	var $categoricalDisplayToggleBtn = $('.fbk-toggle-categorical-display');

	window.isCategoricalDescriptionView = isCategoricalDisplay;

	// update the toggle button text depending on what current session value is
	$categoricalDisplayToggleBtn.text(isCategoricalDisplay ? window.measurementObservationMessages.hideCategoricalDescription :
		window.measurementObservationMessages.showCategoricalDescription);

	// add event handlers
	$('.inline-edit-confirmation').off('click').on('click', onMeasurementsInlineEditConfirmationEvent());
	$categoricalDisplayToggleBtn.off('click').on('click', function() {
		// process any unedited saves before updating measurement table's categorical view
		processInlineEditInput();

		switchCategoricalView();
	});

	// display the measurements table
	return new BMS.Fieldbook.MeasurementsDataTable('#measurement-table');

}

function initializeTraitsPagination(isCategoricalDisplay) {
    'use strict';
    onMeasurementsObservationLoad(isCategoricalDisplay);

    $('#inlineEditConfirmationModalClose').on('click', function (e) {
        // When the confirmation popup is clicked, stop the bubbling of event to parent elements so that body.click is not executed.
        e.stopPropagation();
        // then manually close the confirmation popup.
        $('#inlineEditConfirmationModal').modal('hide');
    });
}
