/* global displayOntologyTree, getJquerySafeId, getIdCounterpart, showErrorMessage, noVariableNameError */

// TODO HH Move these to be passed in
/* global noVariableAddedMessage, errorTheVariable, errorTheVariableNurseryUnique, varInListMessage*/

// These should move to choose-settings
/* global createDynamicSettingVariables, createTableSettingVariables, checkTraitsAndSelectionVariateTable, hideDummyRow */
/* global checkShowSettingsFormReminder */

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

	VariableSelection = function(group, data, translations) {
		this._data = data;
		this._group = group;
		this._translations = translations;
		this._modal = $('#addVariablesSettingModal');
	};

	VariableSelection.prototype.show = function() {

		if ($('#' + treeDivId + ' .fbtree-container').length > 0) {
			$('#' + treeDivId).dynatree('destroy');
		}

		displayOntologyTree(treeDivId, this._data.treeData, this._data.searchTreeData, 'srch-term');
		$('#' + 'srch-term').val('');

		// clear selected variables table and attribute fields
		$('#newVariablesList > tbody').empty();
		$('#page-message-modal').html('');

		clearAttributeFields();

		$('.nrm-vs-modal .fbk-modal-title').text(this._translations.label);
		$('.nrm-vs-modal .nrm-vs-hint-placeholder').html(this._translations.placeholderLabel);

		$('#ontology-detail-tabs').empty().html($('.variable-detail-info').html());
		$('#variable-details').html('');

		$('#addVariables').on('click', null, {group: this._group}, $.proxy(function(e) {
			e.preventDefault();
			this.submitSelectedVariables(e.data.group);
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
		$('#addVariables').off('click');
	};

	VariableSelection.prototype.submitSelectedVariables = function(variableType) {

		var that = this,
			$newVariablesList = $('#newVariablesList tbody tr'),

			numberOfVariablesToAdd = $newVariablesList.length;

		if (numberOfVariablesToAdd === 0) {
			showErrorMessage('', noVariableAddedMessage);

		} else if (numberOfVariablesToAdd > 0 && hasNoVariableName($newVariablesList)) {
			showErrorMessage('', noVariableNameError);

		} else if (numberOfVariablesToAdd > 0) {
			var varName = validateUniqueVariableName();

			if (varName !== '') {
				showErrorMessage('', errorTheVariable + ' &quot;' + varName + '&quot; ' + errorTheVariableNurseryUnique);
				return;
			}

			replaceNameVariables($newVariablesList);
			var serializedData = $('input.addVariables').serialize();
			$('#page-message-modal').html('');

			var promise = $.ajax({
				url: '/Fieldbook/NurseryManager/createNursery/addSettings/' + variableType,
				type: 'POST',
				data: serializedData,
				success: function(data) {
					switch (variableType) {
						case 1:
							createDynamicSettingVariables($.parseJSON(data),
									'studyLevelVariables', 'nurseryLevelSettings-dev', 'nurseryLevelSettings', variableType, '');
							break;
						case 2:
							createTableSettingVariables($.parseJSON(data), 'plotLevelVariables', 'plotLevelSettings', variableType);
							break;
						case 3:
							hideDummyRow('baselineTraitSettings');
							createTableSettingVariables($.parseJSON(data), 'baselineTraitVariables', 'baselineTraitSettings', variableType);
							checkTraitsAndSelectionVariateTable('', false);
							break;
						case 6:
							hideDummyRow('selectionVariatesSettings');
							createTableSettingVariables($.parseJSON(data),
									'selectionVariatesVariables',
									'selectionVariatesSettings', variableType);
							checkTraitsAndSelectionVariateTable('', false);
							break;
						case 7:
							createDynamicSettingVariables($.parseJSON(data),
									'nurseryConditions', 'nurseryConditionsSettings',
									'nurseryConditionsSettings', variableType, 'Cons');
							break;
						default:
							createDynamicSettingVariables($.parseJSON(data), 'studyLevelVariables', 'nurseryLevelSettings-dev',
							'nurseryLevelSettings', variableType, '');
					}
					that.hide();
					checkShowSettingsFormReminder();
				}
			});

			return promise;
		} else {
			showErrorMessage('', varInListMessage);
		}
	};

	return VariableSelection;

})(jQuery);
