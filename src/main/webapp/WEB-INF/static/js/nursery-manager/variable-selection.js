/* global Handlebars */

/**
 * @module variable-selection
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

	var VARIABLE_SELECT_EVENT = 'nrm-variable-select',

		modalHeaderSelector = '.modal-header',
		addVariableButtonSelector = '.nrm-var-select-add',
		relatedPropertyLinkSelector = '.nrm-var-property-name',
		variableListSelector = '.nrm-var-select-vars',
		relatedPropertyListSelector = '.nrm-var-select-related-props',
		propertySelectSelector = '.nrm-var-select-dropdown-container',

		// Only compile our templates once, rather than every time we need them
		generateVariable = Handlebars.compile($('#variable-template').html()),
		generateRelatedProperty = Handlebars.compile($('#related-prop-template').html()),

		VariableSelection;

	/* Provides a conditional check as to whether a provided variable is one of a list of already selected variables. Used to ensure already
	 * selected variables cannot be selected again.
	 */
	Handlebars.registerHelper('ifSelectedVariable', function(variableId, selectedVariables, options) {
		if (selectedVariables.indexOf(variableId) !== -1)  {
			return options.fn(this);
		} else {
			return options.inverse(this);
		}
	});

	/* Attaches the specified list of related properties to the provided container, after ensuring the currently selected property is
	 * removed from the list.
	 *
	 * @param {JQuery} container the container to which the properties should be appended
	 * @param {object} selectedProperty the currently selected property, which should be removed from the list of related properties
	 * @param {object[]} the list of properties related to the selected property
	 */
	function _renderRelatedProperties(container, selectedProperty, properties) {

		// Filter out the currently selected property
		var filteredProperties = $.grep(properties, function(element) {
			return element.propertyId !== selectedProperty.propertyId;
		});

		container.append(generateRelatedProperty({properties: filteredProperties}));
	}

	/* Constructs a new property dropdown.
	 *
	 * @param {string} placeholder the placeholder to use in the select
	 * @param {object[]} the list of properties to render in the dropdown
	 * @param {function} onSelectingFn a function to perform when the user selects a new property
	 */
	function _instantiatePropertyDropdown(placeholder, properties, onSelectingFn) {

		var propertyDropdown = new window.BMS.NurseryManager.PropertySelect(propertySelectSelector,
			placeholder, properties, onSelectingFn);

		return propertyDropdown;
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

		this._relatedProperties = [];

		this._$modal.on('hide.bs.modal', $.proxy(this._clear, this));
	};

	/**
	 * Display the variable selection dialog for the specified group.
	 *
	 * @param {number} group properties and variables will be filtered to be specific to the group represented by this number
	 * @param {object} translations internationalised labels to be used in the dialog
	 * @param {string} translations.label the title of the dialog
	 * @param {string} translations.placeholderLabel the placeholder in the property dropdown
	 * @param {object} groupData data about the group, including selected variables and properties
	 */
	VariableSelection.prototype.show = function(group, translations, groupData) {

		var properties = groupData.propertyData,
			modalHeader = $(this._modalSelector + ' ' + modalHeaderSelector),
			title;

		// Store these properties for later use
		this._currentlySelectedVariables = groupData.selectedVariables;
		this._group = group;
		this._properties = properties;

		// Append title
		title = $('<h4 class="modal-title" id="nrm-var-selection-modal-title">' + translations.label + '</h4>');
		modalHeader.append(title);

		// Instantiate property dropdown, passing in a function that will record the new property and load it's details on select
		this._propertyDropdown = _instantiatePropertyDropdown(translations.placeholderLabel, properties, $.proxy(function(e) {
			this._selectedProperty = e.choice;
			this._loadVariablesAndRelatedProperties();
		}, this));

		// TODO Awaiting Rebecca's JSONified variable usage service
		// $('.nrm-var-select-popular-vars').append(generateVariable({variables: groupData.variables}));

		// Listen for variable selection
		$(variableListSelector).on('click', addVariableButtonSelector, {}, $.proxy(this._selectVariableButton, this));

		// Listen for the user clicking on a related property
		$(relatedPropertyListSelector).on('click', relatedPropertyLinkSelector, {}, $.proxy(this._loadRelatedProperty, this));

		// Show the modal
		this._$modal.modal({
			backdrop: 'static',
			keyboard: true
		});
	};

	/**
	 * Hide the dialog. This programmatic method is available for use, but because we are using a bootstrap modal, pressing the escape key
	 * will also hide the modal.
	 */
	VariableSelection.prototype.hide = function() {
		this._$modal.modal('hide');
	};

	/*
	 * Loads variables and related properties for the currently selected property.
	 */
	VariableSelection.prototype._loadVariablesAndRelatedProperties = function() {

		var selectedProperty = this._selectedProperty,
			variableList = $(variableListSelector),
			relatedPropertyList = $(relatedPropertyListSelector),
			classId = selectedProperty.traitClass.traitClassId,

			relatedPropertiesKey;

		// Clear out any existing variables and append the variables of the selectedProperty
		variableList.empty();
		variableList.append(generateVariable({
			variables: selectedProperty.standardVariables,
			selectedVariables: this._currentlySelectedVariables
		}));

		// Clear out any existing related properties, and update the related property class name
		relatedPropertyList.empty();
		$('.nrm-var-select-related-prop-class').text(selectedProperty.traitClass.traitClassName);

		// Key identifies whether we have retrieved the related properties for this group / class before (so we don't retrieve them again)
		relatedPropertiesKey = this._group + ':' + classId;

		if (!this._relatedProperties[relatedPropertiesKey]) {

			var url = '/Fieldbook/OntologyBrowser/settings/properties?groupId=' + this._group + '&classId=' + classId;

			// TODO Deal with the situation where they have moved on before it returns
			$.getJSON(url, $.proxy(function(data) {

				// Store for later to prevent multiple calls to the same service with the same data
				this._relatedProperties[relatedPropertiesKey] = data;
				_renderRelatedProperties(relatedPropertyList, selectedProperty, data);
			}, this));
		} else {
			_renderRelatedProperties(relatedPropertyList, selectedProperty, this._relatedProperties[relatedPropertiesKey]);
		}
	};

	/*
	 * Handles a variable select event. Selects the clicked variable.
	 *
	 * @param {object} event the JQuery click event
	 */
	VariableSelection.prototype._selectVariableButton = function(e) {
		e.preventDefault();

		var selectButton = $(e.target),
			variableName = $(selectButton.parent('.nrm-var-select-var').find('.nrm-var-name')[0]).text(),
			selectedVariable;

		// Find the variable from the name of the variable that was clicked on
		$.each(this._selectedProperty.standardVariables, function(index, variableObj) {

			if (variableObj.name === variableName) {
				selectedVariable = variableObj;
			}
			return !selectedVariable;
		});

		// Disable the select button to prevent clicking twice
		selectButton.attr('disabled', 'disabled');

		$.ajax({
			url: '/Fieldbook/manageSettings/addSettings/' + this._group,
			type: 'POST',
			data: JSON.stringify({selectedVariables: [{cvTermId: selectedVariable.id, name: selectedVariable.name}]}),
			dataType: 'json',
			headers: {
				Accept: 'application/json',
				'Content-Type': 'application/json'
			},
			success: $.proxy(function(data) {
				// Prevent this variable from being selected again
				this._currentlySelectedVariables.push(selectedVariable.id);

				// Throw a variable select event, so interested parties can do something with the user's intention to add this variable.
				// FIXME Should pass this selector through
				$('.nrm-var-selection-modal-container').trigger({
					type: VARIABLE_SELECT_EVENT,
					group: this._group,
					responseData: data
				});
			}, this),
			failure: function() {
				selectButton.removeAttr('disabled');
			}
		});
	};

	/*
	 * Handles a click on a related property. Loads the selected property.
	 *
	 * @param {object} event the JQuery click event
	 */
	VariableSelection.prototype._loadRelatedProperty = function(e) {
		e.preventDefault();

		// The id of the property that the user clicked on
		var propertyId = $(e.target).data('id'),
			property;

		// Find the property that was selected from our list of properties
		$.each(this._properties, function(index, propertyObj) {

			if (propertyObj.propertyId === propertyId) {
				property = propertyObj;
			}

			return !property;
		});

		// Set the selected property in the dropdown, store for later use and load the variables and related properties for that property
		this._propertyDropdown.setValue(property);
		this._selectedProperty = property;
		this._loadVariablesAndRelatedProperties();
	};

	/*
	 * Clears out data from the dialog.
	 */
	VariableSelection.prototype._clear = function() {

		var modalHeader = $(this._modalSelector + ' ' + modalHeaderSelector),
			variableList = $(variableListSelector),
			relatedPropertyList = $(relatedPropertyListSelector);

		// Clear title
		modalHeader.empty();

		// Clear variable and related property selection events
		variableList.off('click');
		relatedPropertyList.off('click');

		// Clear variables and related properties
		variableList.empty();
		relatedPropertyList.empty();

		// Destroy the select dropdown
		this._propertyDropdown.destroy();
	};

	return VariableSelection;

})(jQuery);
