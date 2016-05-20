/*global onMeasurementsObservationLoad */

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
