// FIXME Currently this module depends on globally available select2 code. Should try and fix this.
/* global Handlebars */

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

// TODO Prevent already added variables from being able to be selected
// TODO Need the ability to rename variables

BMS.NurseryManager.VariableSelection = (function($) {
	'use strict';

	var VARIABLE_SELECT_EVENT = 'nrm-variable-select',

		modalHeaderSelector = '.modal-header',
		propertyDropdownSelector = '.nrm-var-select-dropdown',
		propertyDropdownContainerSelector = '.nrm-var-select-dropdown-container',
		addVariableButtonSelector = '.nrm-var-select-add',
		variableListSelector = '.nrm-var-select-vars',

		generateVariable = Handlebars.compile($('#variable-template').html()),
		generateDropdownMarkup = Handlebars.compile($('#nrm-var-select-dropdown-template').html()),

		selectedProperty,
		selectedVariables,

		VariableSelection;

	Handlebars.registerHelper('ifSelectedVariable', function(variableId, options) {
		if (selectedVariables.indexOf(variableId) !== -1)  {
			return options.fn(this);
		} else {
			return options.inverse(this);
		}
	});

	function formatResult(item) {
		var formattedItem,
			variables,
			i;

		formattedItem  = '<p><strong>' + item.name + '</strong> (' + item.traitClass.traitClassName + ')<br/>';

		variables = item.standardVariables.sort(function(a, b) {
			return a.name.localeCompare(b.name);
		});

		for (i = 0; i < variables.length; i++) {
			if (i !== 0) {
				formattedItem += ', ';
			}
			formattedItem += variables[i].name;
		}
		return formattedItem + '</p>';
	}

	function formatSelection(item) {
		return '<p><strong>' + item.name + '</strong> (' + item.traitClass.traitClassName + ')</p>';
	}

	function initialisePropertyDropdown(placeholder, properties) {

		$(propertyDropdownContainerSelector).append(generateDropdownMarkup());

		return $(propertyDropdownSelector).select2({
			placeholder: placeholder,
			minimumResultsForSearch: 20,
			data: {
				results: properties,
				text: 'name'
			},
			formatSelection: formatSelection,
			formatResult: formatResult,
			id: function(item) {
				return {
					id: item.propertyId
				};
			}
		});
	}

	function destroyPropertyDropdown() {
		$(propertyDropdownSelector).select2('destroy');
		$(propertyDropdownContainerSelector).empty();
	}

	function selectVariableButton(e) {
		e.preventDefault();

		// TODO Error handling for not finding element
		var selectButton = $(e.target),
			selectedVariableElement = selectButton.parent('.nrm-var-select-var'),
			variableName = $(selectedVariableElement.find('.nrm-var-name')[0]).text(),
			variables = selectedProperty.standardVariables,
			varLength = variables.length,
			group = e.data.group,
			i,
			selectedVariable;

		// Find the variable from the name
		for (i = 0; i < varLength; i++) {
			if (variables[i].name === variableName) {
				selectedVariable = variables[i];
			}
		}

		// TODO Error handling if selected var is undefined

		selectButton.attr('disabled', 'disabled');

		$.ajax({
			url: '/Fieldbook/NurseryManager/createNursery/addSettings/' + group,
			type: 'POST',
			data: JSON.stringify({selectedVariables: [{
				cvTermId: selectedVariable.id,
				name: selectedVariable.name
			}]}),
			dataType: 'json',
			headers: {
				Accept: 'application/json',
				'Content-Type': 'application/json'
			},
			success: function(data) {

				// Prevent this variable from being selected again
				selectedVariables.push(selectedVariable.id);

				// FIXME Should pass this selector through
				$('.nrm-var-selection-modal-container').trigger({
					type: VARIABLE_SELECT_EVENT,
					group: group,
					responseData: data
				});
			},
			failure: function() {
				// TODO HH Error handling
				selectButton.removeAttr('disabled');
			}
		});
	}

	/**
	 * Creates a new Variable Selection dialog.
	 *
	 * @constructor
	 * @param {string} selector the selector referencing the modal
	 */
	VariableSelection = function(selector) {
		this._modalSelector = selector;
		this._$modal = $(selector);

		this._$modal.on('hide.bs.modal', $.proxy(this._destroy, this));
	};

	VariableSelection.prototype._destroy = function() {

		var modalHeader = $(this._modalSelector + ' ' + modalHeaderSelector),
			variableList = $(variableListSelector);

		// Clear title
		modalHeader.empty();

		// Clear variable selection events
		variableList.off('click');

		// Clear variables
		variableList.empty();

		// Destroy the select dropdown
		destroyPropertyDropdown();
	};

	VariableSelection.prototype.show = function(group, translations, groupData) {

		var properties = groupData.propertyData,
			modalHeader = $(this._modalSelector + ' ' + modalHeaderSelector),
			title,
			propertyDropdown;

		selectedVariables = groupData.selectedVariables;

		// Append title
		title = $('<h4 class="modal-title" id="nrm-var-selection-modal-title">' + translations.label + '</h4>');
		modalHeader.append(title);

		// Instantiate select2 widget for property dropdown
		propertyDropdown = initialisePropertyDropdown(translations.placeholderLabel, properties);

		propertyDropdown.on('select2-selecting', function(e) {

			var variableList = $(variableListSelector);

			// Hold on this for later use
			selectedProperty = e.choice;

			// Clear currently selected property and select the new one
			variableList.empty();
			variableList.append(generateVariable({variables: selectedProperty.standardVariables}));
		});

		// TODO Awaiting Rebecca's JSONified variable usage service
		// $('.nrm-var-select-popular-vars').append(generateVariable({variables: groupData.variables}));

		// Listen for variable selection
		$(variableListSelector).on('click', addVariableButtonSelector, {group: group}, $.proxy(selectVariableButton, this));

		// Show the modal
		this._$modal.modal({
			backdrop: 'static',
			keyboard: true
		});
	};

	VariableSelection.prototype.hide = function() {
		this._$modal.modal('hide');
	};

	return VariableSelection;

})(jQuery);
