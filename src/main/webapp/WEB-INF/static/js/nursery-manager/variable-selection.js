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

		// TODO Reconsider this - maybe scope to the class. Keep track of the selected property
		selectedProperty,

		VariableSelection;

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

		$(propertyDropdownSelector).select2({
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
		}).on('select2-selecting', function(e) {

			var variableList = $(variableListSelector);

			// Hold on this for later use
			selectedProperty = e.choice;

			// Clear currently selected property and select the new one
			variableList.empty();
			variableList.append(generateVariable({variables: selectedProperty.standardVariables}));
		});
	}

	function destroyPropertyDropdown() {
		$(propertyDropdownSelector).select2('destroy');
		$(propertyDropdownContainerSelector).empty();
	}

	function selectVariableButton(e) {
		e.preventDefault();

		// TODO Error handling for not finding element
		var selectedVariableElement = $(e.target).parent('.nrm-var-select-var'),
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
				// FIXME Should pass this selector through
				$('.nrm-var-selection-modal-container').trigger({
					type: VARIABLE_SELECT_EVENT,
					group: group,
					responseData: data
				});
			}
			// TODO HH Error handling
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

	VariableSelection.prototype.show = function(group, properties, variables, translations) {

		var modalHeader = $(this._modalSelector + ' ' + modalHeaderSelector),
			title;

		// Append title
		title = $('<h4 class="modal-title" id="nrm-var-selection-modal-title">' + translations.label + '</h4>');
		modalHeader.append(title);

		// Instantiate select2 widget for property dropdown
		initialisePropertyDropdown(translations.placeholderLabel, properties);

		// TODO Awaiting Rebecca's JSONified variable usage service
		// $('.nrm-var-select-popular-vars').append(generateVariable({variables: variables}));

		// Listen for variable selection
		$(variableListSelector).on('click', addVariableButtonSelector, {group: group}, selectVariableButton);

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
