var ImportInventoryFunctions  = window.ImportInventoryFunctions;

if (typeof ImportInventoryFunctions === 'undefined') {
	ImportInventoryFunctions = {

		openImportInventoryModal: function(source) {
			$('#importInventoryModal').modal({backdrop: 'static', keyboard: true});

			$('#import-list-id').val(getCurrentAdvanceTabListIdentifier());
			$('#importUploadClearButton').click();
			$('#importInventorySubmitButton').off('click');
			$('#importInventorySubmitButton').on('click', ImportInventoryFunctions.processImport);

			$('#import-source-string').val(source);
		},

		processImport: function() {
			'use strict';

			if ($('#fileupload-import-inventory').val() === '') {
				showErrorMessage('', 'Please choose a file to import');
				return false;
			}

			ImportInventoryFunctions.submitImport().done(function(response) {

				if (!response.isSuccess) {
					showErrorMessage('', response.error);
					return;
				}

				if (response.isOverwrite) {
					showAlertMessage('', 'Data will be overwritten');
				}

				$('#importInventoryModal').modal('hide');

				if ($('#import-source-string').val() === 'advance') {
					ImportInventoryFunctions.displayUpdatedAdvanceGermplasmDetails($('#import-list-id').val());
				} else {
					ImportInventoryFunctions.displayUpdatedCrossGermplasmDetails($('#import-list-id').val());
				}

				// this is to prevent the user from performing other activities even tab navigation prior to saving the imported data
				$('.import-inventory').data('data-import', '1');
			});

		},

		submitImport: function() {
			'use strict';
			var deferred = $.Deferred();
			$('#importInventoryForm').ajaxForm({
				dataType: 'json',
				success: function(response) {
					deferred.resolve(response);
				},
				error: function(response) {

					showErrorMessage('', 'Error occurred while importing file');

					deferred.reject(response);
				}
			}).submit();

			return deferred.promise();
		},

		saveImport: function(source) {
			'use strict';
			$.post('/Fieldbook/importInventory/save', function(result) {
				if (result.isSuccess) {
					ImportInventoryFunctions.displayFinalizedInventoryDetails(getCurrentAdvanceTabListIdentifier(), source);
				}
			});
		},

		discardImport: function(source) {
			'use strict';
			ImportInventoryFunctions.displayFinalizedInventoryDetails(getCurrentAdvanceTabListIdentifier(), source);
		},

		displayUpdatedAdvanceGermplasmDetails: function(listId) {
			'use strict';
			$.ajax({
				url: '/Fieldbook/importInventory/displayTemporaryAdvanceGermplasmDetails/' + listId,
				type: 'GET',
				cache: false,
				success: function(html) {
					$('#advance-list' + getCurrentAdvanceTabTempIdentifier()).html(html);
				}
			});
		},

		displayUpdatedCrossGermplasmDetails: function(listId) {
			'use strict';
			$.ajax({
				url: '/Fieldbook/importInventory/displayTemporaryCrossGermplasmDetails/' + listId,
				type: 'GET',
				cache: false,
				success: function(html) {
					$('#advance-list' + getCurrentAdvanceTabTempIdentifier()).html(html);
				}
			});
		}
	};
}
