/* globals getJquerySafeId */
// TODO: Delete?
var LocationsFunctions = window.LocationsFunctions;

// undefined check is performed to avoid overwriting the state / functionality of the object
if (typeof (LocationsFunctions) === 'undefined') {
	LocationsFunctions = {
		isModalAvailableAndForOpening: function(modalID) {
			return $('#' + getJquerySafeId(modalID)).length !== 0 && $('#' + getJquerySafeId(modalID)).data('open') == '1';
		},

		openModal: function(modalID) {
			setTimeout(function() {$('#' + getJquerySafeId(modalID)).modal('show');}, 200);
			setTimeout(function() {$('#' + getJquerySafeId(modalID)).data('open', '0');}, 500);
		},

		sourceURL: '',

		// function for opening the Manage Locations modal that links to the IBPWorkbench application and ensuring that the proper modal window is re-opened on close of this one
		openLocationsModal: function() {
			$('#manageLocationModal').modal({backdrop: 'static', keyboard: true});

			LocationsFunctions.retrieveCurrentProjectID().done(function(projectID) {
				if (LocationsFunctions.sourceURL === '') {
					LocationsFunctions.retrieveProgramLocationURL().done(function(data) {
						LocationsFunctions.sourceURL = data;
						$('#locationFrame').attr('src', LocationsFunctions.sourceURL + projectID);
					});
				} else {
					$('#locationFrame').attr('src', LocationsFunctions.sourceURL + projectID);
				}
			});

		},

		retrieveProgramLocationURL: function() {
			return $.get('/Fieldbook/locations/programLocationsURL');
		},

		retrieveCurrentProjectID: function() {
			return $.get('/Fieldbook/breedingMethod/programID');
		}

	};
}
