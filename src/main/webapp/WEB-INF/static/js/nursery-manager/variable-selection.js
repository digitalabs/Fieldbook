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
		variableNameContainerSelector = '.nrm-var-select-var-name-container',
		addVariableButtonSelector = '.nrm-var-select-add',
		aliasVariableButtonSelector = '.nrm-var-select-name-alias',
		aliasVariableInputSelector = '.nrm-var-select-alias-input',
		relatedPropertyLinkSelector = '.nrm-var-property-name',
		variableListSelector = '.nrm-var-select-vars',
		relatedPropertyListSelector = '.nrm-var-select-related-props',
		propertySelectSelector = '.nrm-var-select-dropdown-container',

		// Only compile our templates once, rather than every time we need them
		generateVariable = Handlebars.compile($('#variable-template').html()),
		generateVariableName = Handlebars.compile($('#nrm-var-select-name-template').html()),
		generateVariableAlias = Handlebars.compile($('#nrm-var-select-name-alias-template').html()),
		generateRelatedProperty = Handlebars.compile($('#related-prop-template').html()),

		VariableSelection;

	Handlebars.registerPartial('variable-name', $('#nrm-var-select-name-partial').html());

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

	/*
	 * Finds a variable with a specified name from a list of variables.
	 *
	 * @param {string} variableName the name fo the variable to find
	 * @param {object[]} variableList the array of variables to search through
	 */
	function _findVariableByName(variableName, variableList) {

		var index = -1,
			selectedVariable;

		$.each(variableList, function(i, variableObj) {

			if (variableObj.name === variableName) {
				selectedVariable = variableObj;
			}
			index = i;
			return !selectedVariable;
		});

		return {
			index: index,
			variable: selectedVariable
		};
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

		// Listen for variable selection and name aliasing
		$(variableListSelector).on('click', addVariableButtonSelector, {}, $.proxy(this._selectVariableButton, this));

		$(variableListSelector).on('click', aliasVariableButtonSelector, {}, $.proxy(this._aliasVariableButton, this));

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
			variables = this._selectedProperty.standardVariables,
			variableListElement = $(variableListSelector),
			relatedPropertyList = $(relatedPropertyListSelector),
			classId = selectedProperty.traitClass.traitClassId,
			selectedVariables = this._currentlySelectedVariables,

			i,
			selectedVariableName,
			variableId,
			relatedPropertiesKey;

		// If we know of aliases for any of the variables we're loading, set them now
		for (i = 0; i < variables.length; i++) {
			variableId = variables[i].id;
			selectedVariableName = selectedVariables[variableId];

			if (typeof(selectedVariableName) !== 'undefined') {

				// Only set the alias if it is different from the name we know for the variable
				if (selectedVariableName && selectedVariableName !== variables[i].name) {
					variables[i].alias = selectedVariableName;
				}
				// Whether or not the selected variable name has been provided, if the key is present the variable has
				// been selected
				variables[i].selected = true;
			}
		}

		// Update our saved property list to reflect our new knowledge of aliases and which variables are selected
		this._selectedProperty.standardVariables = variables;

		// Clear out any existing variables and append the variables of the selectedProperty
		variableListElement.empty();
		variableListElement.append(generateVariable({
			variables: variables
		}));

		// Clear out any existing related properties, and update the related property class name
		relatedPropertyList.empty();

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

		var selectButton = $(e.currentTarget),
			iconContainer = selectButton.children('.glyphicon'),
			variableName = $(selectButton.parent('p').find('.nrm-var-name')[0]).text(),
			selectedVariable;

		selectedVariable = _findVariableByName(variableName, this._selectedProperty.standardVariables).variable;

		// Disable the select button to prevent clicking twice
		selectButton.attr('disabled', 'disabled');
		iconContainer.removeClass('glyphicon-plus').addClass('glyphicon-ok');

		$.ajax({
			url: '/Fieldbook/manageSettings/addSettings/' + this._group,
			type: 'POST',
			data: JSON.stringify({
				selectedVariables: [{cvTermId: selectedVariable.id, name: selectedVariable.alias || selectedVariable.name}]
			}),
			dataType: 'json',
			headers: {
				Accept: 'application/json',
				'Content-Type': 'application/json'
			},
			success: $.proxy(function(data) {
				// Prevent this variable from being selected again
				this._currentlySelectedVariables[selectedVariable.id] = selectedVariable.alias;

				// Remove the edit button
				selectButton.parent('p').find(aliasVariableButtonSelector).remove();

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
				iconContainer.removeClass('glyphicon-ok').addClass('glyphicon-plus');
			}
		});
	};

	/*
	 * Handles a variable alias event. Allows the user to provide an alias for a variable name.
	 *
	 * @param {object} event the JQuery click event
	 */
	VariableSelection.prototype._aliasVariableButton = function(e) {
		e.preventDefault();

		var aliasButton = $(e.currentTarget),
			container = aliasButton.parent(variableNameContainerSelector),
			variableName = $(container.children('.nrm-var-name')[0]).text(),
			variableInfo;

		variableInfo = _findVariableByName(variableName, this._selectedProperty.standardVariables);

		// Remove the display of the name and edit button, and render the input and save/ cancel buttons
		container.empty();
		container.append(generateVariableAlias({
			index: variableInfo.index,
			// FIXME I18n placeholder
			placeholder: 'Enter an alias',
			alias: variableInfo.variable.alias || ''
		}));

		container.on('click', '.nrm-var-select-name-save', {}, $.proxy(this._saveAlias, this));
		container.on('click', '.nrm-var-select-name-cancel', {}, $.proxy(this._cancelAlias, this));
		container.on('keyup ', aliasVariableInputSelector, {}, $.proxy(function(e) {

			switch (e.keyCode) {
				case 13:
					// Save on enter
					this._saveAlias(e);
					break;
				case 27:
					// Cancel on escape - this is actually being trapped by the escape to escape from
					// the modal, so won't work at the moment :(
					this._cancelAlias(e);
					break;
				default:
					// Don't do anything for any other keys
					break;
			}
		}, this));

		container.find(aliasVariableInputSelector).focus();
	};

	function _renderVariableName(variable, variableContainer) {

		variableContainer.off('click');
		variableContainer.empty();

		variableContainer.append(generateVariableName(variable));
	}

	/*
	 * Handles a variable alias save event.
	 *
	 * @param {object} event the JQuery click event
	 */
	VariableSelection.prototype._saveAlias = function(e) {
		e.preventDefault();

		var container = $(e.currentTarget).parent(variableNameContainerSelector),
			input = container.find(aliasVariableInputSelector),
			alias = input.val(),
			index = input.data('index');

		if (alias) {
			// Store the alias
			this._selectedProperty.standardVariables[index].alias = alias;
		}

		_renderVariableName(this._selectedProperty.standardVariables[index], container);
	};

	/*
	 * Handles a variable alias cancel event.
	 *
	 * @param {object} event the JQuery click event
	 */
	VariableSelection.prototype._cancelAlias = function(e) {
		e.preventDefault();

		var container = $(e.currentTarget).parent(variableNameContainerSelector),
			input = container.find(aliasVariableInputSelector),
			index = input.data('index');

		_renderVariableName(this._selectedProperty.standardVariables[index], container);
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
