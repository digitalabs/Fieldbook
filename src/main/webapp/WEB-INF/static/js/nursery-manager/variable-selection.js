/* global displayOntologyTree, getJquerySafeId, getIdCounterpart, showErrorMessage, noVariableNameError */

// TODO HH Move these to be passed in
/* global noVariableAddedMessage, errorTheVariable, errorTheVariableNurseryUnique, varInListMessage*/

/**
 * @module measurements-datatable
 */
 var BMS = window.BMS;

if (typeof (BMS) === 'undefined') {
	BMS = {};
}

if (typeof (BMS.NurseryManager) === 'undefined') {
	BMS.NurseryManager = {};
}

BMS.NurseryManager.VariableSelection = (function($) {
	'use strict';

	var treeDivId = 'ontologyBrowserTree',

		VariableSelection;

	function clearAttributeFields() {
		$('selectedTraitClass').html('&nbsp;');
		$('selectedProperty').html('&nbsp;');
		$('#selectedMethod').html('&nbsp;');
		$('#selectedScale').html('&nbsp;');
		$('#selectedDataType').html('&nbsp;');
		$('#selectedRole').html('&nbsp;');
		$('#selectedCropOntologyId').html('&nbsp;');
		$('#selectedStdVarId').val('');
		$('#selectedName').val('');
	}

	function replaceNameVariables($variablesList) {

		$.each($variablesList, function(index, row) {
			var value = $(
					$(row).children('td:nth-child(1)').children('#' + getJquerySafeId('selectedVariables' + index + '.cvTermId'))).val();

			// use the id counterpart of the name variable
			$($(row).children('td:nth-child(1)').children('#' +
				getJquerySafeId('selectedVariables' + index + '.cvTermId'))).val(getIdCounterpart(value,
				$('#idNameVariables').val().split(',')));
		});

	}

	function hasNoVariableName($variablesList) {

		var result = false;

		$.each($variablesList, function(index, row) {
			if ($($(row).children('td:nth-child(1)').children('#' + getJquerySafeId('selectedVariables' + index + '.name'))).val() === '') {
				result = true;
			}
		});
		return result;
	}

	function validateUniqueVariableName() {
		var existingNameMap = [],
			isFound = false,
			existingVarName = '',
			newName = '';

		$('.var-names').each(function() {
			var varName = $.trim($(this).html()).toUpperCase();
			existingNameMap[varName] = $(this).html();
		});

		$('input[type=text].addVariables').each(function() {
			newName = $.trim($(this).val()).toUpperCase();
			if (isFound === false && existingNameMap[newName] !== undefined) {
				existingVarName = $.trim($(this).val());
				isFound = true;
				return;
			} else {
				existingNameMap[newName] = newName;
			}
		});
		return existingVarName;
	}

	function submitSelectedVariables(group, successFn) {

		// TODO Prevent already added variables from being able to be selected
		// TODO Can we get by without variable renaming?

		// Test data
		var testVariable;

		switch (group) {
			case 1:
				testVariable = {
					cvTermId: 8008,
					name: 'STUDY_DATE'
				};
				break;
			case 7:
				testVariable = {
					cvTermId: 22531,
					name: 'SOILPH'
				};
				break;
			case 2:
				testVariable = {
					cvTermId: 8255,
					name: 'ENTRY_TYPE'
				};
				break;

			case 3:
				testVariable = {
					cvTermId: 22564,
					name: 'HT'
				};
				break;
			case 6:
				testVariable = {
					cvTermId: 8263,
					name: 'NPSEL'
				};
				break;
		}

		$.ajax({
			url: '/Fieldbook/NurseryManager/createNursery/addSettings/' + group,
			type: 'POST',
			data: JSON.stringify({selectedVariables: [testVariable]}),
			dataType: 'json',
			headers: {
				Accept: 'application/json',
				'Content-Type': 'application/json'
			},
			success: function(data) {
				$.event.trigger({
					type: 'variable-select',
					group: group,
					responseData: data
				});
				successFn();
			}
		});
	}

	VariableSelection = function(group, data, translations) {
		this._data = data;
		this._group = group;
		this._translations = translations;
		this._modal = $('#addVariablesSettingModal');

		this._modal.on('hide.bs.modal', function() {
			$('#addVariables').off('click');
		});
	};

	VariableSelection.prototype.show = function() {

		if ($('#' + treeDivId + ' .fbtree-container').length > 0) {
			$('#' + treeDivId).dynatree('destroy');
		}

		displayOntologyTree(treeDivId, this._data.treeData, this._data.searchTreeData, 'srch-term');
		$('#' + 'srch-term').val('');

		// clear selected variables table and attribute fields
		$('#newVariablesList > tbody').empty();

		clearAttributeFields();

		$('.nrm-vs-modal .fbk-modal-title').text(this._translations.label);
		$('.nrm-vs-modal .nrm-vs-hint-placeholder').html(this._translations.placeholderLabel);

		$('#ontology-detail-tabs').empty().html($('.variable-detail-info').html());
		$('#variable-details').html('');

		$('#addVariables').on('click', null, {group: this._group}, $.proxy(function(e) {
			e.preventDefault();
			submitSelectedVariables(e.data.group, $.proxy(this.hide, this));
		}, this));

		$('#newVariablesList').addClass('fbk-hide');

		// Show the modal
		this._modal.modal({
			backdrop: 'static',
			keyboard: true
		});
	};

	VariableSelection.prototype.hide = function() {
		this._modal.modal('hide');
	};

	return VariableSelection;

})(jQuery);
