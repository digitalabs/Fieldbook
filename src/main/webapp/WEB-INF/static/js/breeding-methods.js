/* globals getJquerySafeId */

var BreedingMethodsFunctions = window.BreedingMethodsFunctions;

// undefined check is performed to avoid overwriting the state / functionality of the object
if (typeof (BreedingMethodsFunctions) === 'undefined') {
	BreedingMethodsFunctions = {
		isModalAvailableAndForOpening: function (modalID) {
			return $('#' + getJquerySafeId(modalID)).length !== 0 && $('#' + getJquerySafeId(modalID)).data('open') == '1';
		},

		openModal: function (modalID) {
			setTimeout(function() {$('#' + getJquerySafeId(modalID)).modal('show');}, 200);
			setTimeout(function() {$('#' + getJquerySafeId(modalID)).data('open', '0');}, 200);
		},

		sourceURL: '',

		// function for opening the Manage Methods modal that links to the IBPWorkbench application and ensuring that the proper modal window is re-opened on close of this one
		openMethodsModal: function() {
			$('#manageMethodModal').modal({backdrop: 'static', keyboard: true});

			BreedingMethodsFunctions.retrieveCurrentProjectID().done(function(projectID) {
				if (BreedingMethodsFunctions.sourceURL === '') {
					BreedingMethodsFunctions.retrieveProgramMethodURL().done(function(data) {
						BreedingMethodsFunctions.sourceURL = data;
						$('#methodFrame').attr('src', BreedingMethodsFunctions.sourceURL + projectID);
					});
				} else {
					$('#methodFrame').attr('src', BreedingMethodsFunctions.sourceURL + projectID);
				}
			});

		},

		retrieveProgramMethodURL: function() {
			return $.get('/Fieldbook/breedingMethod/programMethodURL');
		},

		retrieveCurrentProjectID: function() {
			return $.get('/Fieldbook/breedingMethod/programID');
		}
	};
}
