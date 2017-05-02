jQuery().ready(function() {
	'use strict';
	setTimeout(callAjax, 500);
});

function callAjax() {
	'use strict';
	$.ajax({
		url: '/Fieldbook/Common/ReviewDetailsOutOfBounds/data/table/ajax',
		type: 'POST',
		data: $('#reviewDetailsOutOfBoundsForm').serialize(),
		cache: false,
		success: function(response) {
			new  BMS.Fieldbook.ReviewDetailsOutOfBoundsDataTable('#review-details-table', response);
			restoreFormDataFromSessionStorage($('#traitTermId').val());
		}
	});
}

function generateDataForProcessing() {
	'use strict';

	if (!validateReviewDetailsOutOfBoundsDataForm()) return;

	saveFormDataToSessionStorage($('#traitTermId').val());

	var dataChanges = { data: [] };

	var oTable = $('#import-preview-measurement-table').dataTable();
	
							
	if (sessionStorage) {
		for (var i in sessionStorage) {
			if (i.indexOf('reviewDetailsFormDataAction') === 0) {
				var actionData = JSON.parse(sessionStorage[i]);
				var data = JSON.parse(sessionStorage['reviewDetailsFormData'+actionData.termId]);

				var newData = { termId: actionData.termId, values: []};

				$.each(data, function (key, val) {
					newData.values.push(val);
				});

				dataChanges.data.push(newData);
			}
		}

		$.ajax({
			headers: {
				'Accept': 'application/json',
				'Content-Type': 'application/json'
			},
			url: '/Fieldbook/Common/ReviewDetailsOutOfBounds/submitDetails',
			type: 'POST',
			data: JSON.stringify(dataChanges),
			cache: false,
			contentType: "application/json",
			success: function(data) {
				if (data.success === '1') {
					$('body').removeClass('modal-open');
					$('#reviewDetailsOutOfBoundsDataModal').modal('hide');

					$.each(dataChanges.data, function(index, value) {
						for (i = 0; i < value.values.length; i++) {
							var traitColumnIndex = value.values[i].colIndex;
							if (value.values[i].action === '2' || value.values[i].action === '') {
								oTable.fnUpdate([value.values[i].newValue,''], value.values[i].rowIndex,
									traitColumnIndex, false); // Cell
                    		} else if (value.values[i].action === '3') {
                    			oTable.fnUpdate(['missing',''], value.values[i].rowIndex,
									traitColumnIndex, false); // Cell
                            }
                    	}
                    });

                    $(".dataTable td[class*='invalid-value']").each(function() {
                    	$(this).removeClass('invalid-value');
                    });
				} else {
					showErrorMessage('', data.errorMessage);
				}
			}
		});

	}

}

function validateReviewDetailsOutOfBoundsDataForm() {
	'use strict';
	if ($('#selectAction').val() === '2' && $('#selectActionValue').val().trim() === '') {
		$('#selectActionValue').focus();
		showErrorMessage('', 'Please input the new value.');
		return false;
	}

	if (checkIfAtLeastOneCheckBoxIsTicked() && $('#selectAction').val() === '') {
		$('#selectAction').select2('open');
		showErrorMessage('', 'Please choose from the options.');
		return false;
	}

	if (!checkIfAtLeastOneCheckBoxIsTicked() && $('#selectAction').val() !== '') {
		showErrorMessage('', 'Please select one or more entries in the table.');
		return false;
	}

	return true;
}

function checkIfAtLeastOneCheckBoxIsTicked() {
	'use strict';
	var cells = $('#review-details-table').DataTable().cells().nodes();
	var isChecked = false;
	$(cells).find(':checkbox').each(function() {
		if ($(this).prop('checked')) {
			isChecked = true;
		};
	});

	return isChecked;
}

function saveFormDataToSessionStorage(dataKey) {
	'use strict';
	var data = {};
	var actionData = {};
	var cells = $('#review-details-table').DataTable().cells().nodes();
	var selectedActionType = $('#selectAction').val();
	var selectedActionValue = $('#selectActionValue').val().trim();

	// get the column index of trait from Measurements data table
	var oTable = $('#import-preview-measurement-table').dataTable();
    var traitTermName = $("#traitTermName").val();
	var traitColumnIndex = $('#import-preview-measurement-table').DataTable().column(':contains(' + traitTermName + ')').index();
	
	$(cells).find('[data-binding]').each(function() {

		data[$(this).data('row-index')] = data[$(this).data('row-index')] || { rowIndex: null, colIndex: null, isSelected: false, newValue: '', action: ''};

		data[$(this).data('row-index')].rowIndex = $(this).data('row-index');
		data[$(this).data('row-index')].colIndex = traitColumnIndex;

		if ($(this).is(':checkbox')) {
			data[$(this).data('row-index')].isSelected = $(this).prop('checked');
		} else {
			data[$(this).data('row-index')].newValue = $(this).val().trim();
		}

		data[$(this).data('row-index')].action = selectedActionType || '';

		if (selectedActionType === '2' && data[$(this).data('row-index')].isSelected) {
			data[$(this).data('row-index')].newValue = selectedActionValue;
		}

	});

	actionData['selectAction'] = $('#selectAction').val();
	actionData['selectActionValue'] = $('#selectActionValue').val();
	actionData['checkReviewDetailsSelectAll'] = $('#checkReviewDetailsSelectAll').prop('checked');
	actionData['termId'] = dataKey;

	if (sessionStorage) {
		sessionStorage['reviewDetailsFormData'+ dataKey] = JSON.stringify(data);
		sessionStorage['reviewDetailsFormDataAction'+ dataKey] = JSON.stringify(actionData);
	}
}

function restoreFormDataFromSessionStorage(dataKey) {
	'use strict';
	var data;
	var actionData;

	if (sessionStorage) {
		if (sessionStorage['reviewDetailsFormData' + dataKey]) {
			data = JSON.parse(sessionStorage['reviewDetailsFormData' + dataKey]);
			actionData = JSON.parse(sessionStorage['reviewDetailsFormDataAction' + dataKey]);
		}
		if (data) {
			var cells = $('#review-details-table').DataTable().cells().nodes();

			$(cells).find('[data-binding]').each(function() {
				if ($(this).is(':checkbox')) {
					$(this).prop('checked', data[$(this).data('row-index')].isSelected);
				} else {
					$(this).val(data[$(this).data('row-index')].newValue);
				}

			});
		}
		if (actionData) {
			$('#selectAction').select2('val', actionData.selectAction);
			$('#selectActionValue').val(actionData.selectActionValue);
			$('#checkReviewDetailsSelectAll').prop('checked', actionData.checkReviewDetailsSelectAll);
		}
	}

}
