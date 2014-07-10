// FIXME Currently this module depends on globally available select2 code. Should try and fix this.
/* global Handlebars */

/**
 * @module property-select
 */
 var BMS = window.BMS;

if (typeof (BMS) === 'undefined') {
	BMS = {};
}

if (typeof (BMS.NurseryManager) === 'undefined') {
	BMS.NurseryManager = {};
}

BMS.NurseryManager.PropertySelect = (function($) {
	'use strict';

	var generateDropdownMarkup = Handlebars.compile($('#nrm-var-select-dropdown-template').html()),
		propertyDropdownSelector = '.nrm-var-select-dropdown',

		PropertySelect;

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

	/**
	 * Creates a new Property Select dropdown
	 *
	 * @constructor
	 * @param {string} container the selector referencing the container to which the dropdown should be attached
	 * @param {string} placeholder the label to use for a placeholder in the select
	 * @param {object[]} properties an array of properties to populate the dropdown
	 * @param {function} onSelect a function to call on dropdown select
	 */
	PropertySelect = function(container, placeholder, properties, onSelectFn) {

		this._$container = $(container);
		this._$container.append(generateDropdownMarkup());

		this._$select = this._initialiseSelect(placeholder, properties);

		this._$select.on('select2-selecting', onSelectFn);

		return this;
	};

	/**
	 * Sets the value of the dropdown to a specified property.
	 *
	 * @param {object} property the property object to set as the new selected value
	 */
	PropertySelect.prototype.setValue = function(property) {
		this._$select.select2('data', property);
	};

	/**
	 * Destroy the dropdown.
	 */
	PropertySelect.prototype.destroy = function() {
		this._$select.select2('destroy');
		this._$container.empty();
	};

	/*
	 * Initialise the select2 widget.
	 *
	 * @param {string} placeholder the placeholder to set inside the select
	 * @param {object[]} properties the properties to set as the contents of the dropdown
	 */
	PropertySelect.prototype._initialiseSelect = function(placeholder, properties) {

		return $(propertyDropdownSelector).select2({
			placeholder: placeholder,
			minimumResultsForSearch: 20,
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
	};

	return PropertySelect;

})(jQuery);
