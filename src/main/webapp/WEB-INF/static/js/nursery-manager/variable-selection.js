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
		relatedPropertyLinkSelector = '.nrm-var-property-name',
		variableListSelector = '.nrm-var-select-vars',
		relatedPropertyListSelector = '.nrm-var-select-related-props',

		generateVariable = Handlebars.compile($('#variable-template').html()),
		generateDropdownMarkup = Handlebars.compile($('#nrm-var-select-dropdown-template').html()),
		generateRelatedProperty = Handlebars.compile($('#related-prop-template').html()),

		relatedProperties = [],
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

	function formatResult(item, container, query) {
		var searchTerm = query.term,
			regex,
			propertyName,
			className,
			formattedItem,
			variables,
			i;

		regex = new RegExp(searchTerm, 'gi');

		propertyName = item.name.replace(regex, '<strong>$&</strong>');
		className = item.traitClass.traitClassName.replace(regex, '<strong>$&</strong>');

		formattedItem  = '<p><span class="var-select-result-prop">' + propertyName + '</span> (<span class="var-select-result-class">' +
			className + '</span>)<br/>';

		variables = item.standardVariables.sort(function(a, b) {
			return a.name.localeCompare(b.name);
		});

		formattedItem += '<span class="var-select-result-vars">';

		for (i = 0; i < variables.length; i++) {
			if (i !== 0) {
				formattedItem += ', ';
			}

			formattedItem += variables[i].name.replace(regex, '<strong>$&</strong>');
		}
		return formattedItem + '</span></p>';
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
			},
			query: function(options) {

				var searchTerm = options.term.toLocaleLowerCase(),
					result = {};

				result.results = $.grep(properties, function(prop) {

					var propContainsTerm,
						varNameContainsTerm;

					// Include the object in the results if either the property name or class name contains the search term
					propContainsTerm = prop.name.toLocaleLowerCase().indexOf(searchTerm) > -1 ||
						prop.traitClass.traitClassName.toLocaleLowerCase().indexOf(searchTerm) > -1;

					if (propContainsTerm) {
						return true;
					}

					// Also include the object if any of the property's variables have the search term in their name
					$.each(prop.standardVariables, function(index, variable) {
						varNameContainsTerm = variable.name.toLocaleLowerCase().indexOf(searchTerm) > -1;

						// Returning false will stop the loop - we want to stop when we've found the first variable
						// in this property that contains the search term
						return !varNameContainsTerm;
					});

					return varNameContainsTerm;
				});

				options.callback(result);
			},
			dropdownCssClass: 'var-select-results'
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
			url: '/Fieldbook/manageSettings/addSettings/' + group,
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

	VariableSelection.prototype._loadPropertyDetails = function(property, group) {

		var variableList = $(variableListSelector),
			relatedPropertyList = $(relatedPropertyListSelector),
			classId = property.traitClass.traitClassId,
			relatedPropertiesKey = group + ':' + classId,

			filterOutCurrentProperty = function(element) {
				return element.propertyId !== property.propertyId;
			};

		// Clear currently selected property and select the new one
		variableList.empty();
		relatedPropertyList.empty();

		$('.nrm-var-select-related-prop-class').text(property.traitClass.traitClassName);

		variableList.append(generateVariable({variables: property.standardVariables}));

		// Check to see if we have loaded this set before
		if (!relatedProperties[relatedPropertiesKey]) {

			// TODO Deal with the situation where they have moved on before it returns
			$.getJSON('/Fieldbook/OntologyBrowser/settings/properties?groupId=' + group + '&classId=' + classId, function(data) {

				var filteredProperties;

				// Store for later to prevent multiple calls to the same service with the same data
				relatedProperties[relatedPropertiesKey] = data;

				// Filter out the currently selected property
				filteredProperties = $.grep(data, filterOutCurrentProperty);

				relatedPropertyList.append(generateRelatedProperty({properties: filteredProperties}));
			});
		} else {
			relatedPropertyList.append(generateRelatedProperty({
				properties: $.grep(relatedProperties[relatedPropertiesKey], filterOutCurrentProperty)
			}));
		}
	};

	VariableSelection.prototype._loadProperty = function(property, group) {

		this._propertyDropdown.select2('data', property);
		this._loadPropertyDetails(property, group);
	};

	VariableSelection.prototype._destroy = function() {

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
		destroyPropertyDropdown();
	};

	VariableSelection.prototype.show = function(group, translations, groupData) {

		var properties = groupData.propertyData,
			modalHeader = $(this._modalSelector + ' ' + modalHeaderSelector),
			title;

		selectedVariables = groupData.selectedVariables;

		// Append title
		title = $('<h4 class="modal-title" id="nrm-var-selection-modal-title">' + translations.label + '</h4>');
		modalHeader.append(title);

		// Instantiate select2 widget for property dropdown
		this._propertyDropdown = initialisePropertyDropdown(translations.placeholderLabel, properties);

		// When the user selects a new property, load the property
		this._propertyDropdown.on('select2-selecting', $.proxy(function(e) {
			// Hold on this for later use
			selectedProperty = e.choice;

			this._loadPropertyDetails(selectedProperty, group);
		}, this));

		// When the user selects a related property, load the property
		$(relatedPropertyListSelector).on('click', relatedPropertyLinkSelector, {}, $.proxy(function(e) {
			e.preventDefault();

			var propertyId = $(e.target).data('id'),
				property;

			$.each(properties, function(index, propertyObj) {

				if (propertyObj.propertyId === propertyId) {
					property = propertyObj;
				}

				return !property;
			});

			selectedProperty = property;

			this._loadProperty(property, group);
		}, this));

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
