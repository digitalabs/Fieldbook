/* global checkShowSettingsFormReminder, showErrorMessage */
/* global createDynamicSettingVariables, createTableSettingVariables, checkTraitsAndSelectionVariateTable, hideDummyRow */

window.ChooseSettings = (function() {
	'use strict';

	var MODES = {
			MANAGEMENT_DETAILS: 1805,
			FACTORS: 1804,
			TRAITS: 1808,
			SELECTION_VARIATES: 1807,
			NURSERY_CONDITIONS: 1803
		},

		dialogOpenSelector = '.chs-add-variable',

		variableSelectionGroups = {},
		variableMarkupSelectors = [],

		ChooseSettings;

	/*
	 * Finds a property in a list by id.
	 *
	 * @param {string} propertyId the id of the property to look for
	 * @param {object[]} propertyList the list of properties to search
	 * @returns {number} the index of the found property, or -1 if it was not found
	 */
	function _findPropertyById(propertyId, propertyList) {

		var index = -1;

		// Use each instead of grep to prevent the need to continue to iterate over the array once we've found what we're looking for
		$.each(propertyList, function(i, propertyObj) {

			var found = false;

			if (propertyObj.propertyId === propertyId) {
				found = true;
				index = i;
			}
			return !found;
		});
		return index;
	}

	/*
	 * Removes an item by item from a given list.
	 *
	 * @param {string} id the id of the item to remove
	 * @param {object[]} list the list of objects to search
	 * @param {string} idSelector the key with which the id property should be accessed on each item of the list (e.g. `id` or `propertyId`)
	 * @returns {object} the object that was removed or null if the property was not found
	 */
	function _removeById(id, list, idSelector) {

		var property = null;

		// Use each instead of grep to prevent the need to continue to iterate over the array once we've found what we're looking for
		$.each(list, function(index, obj) {

			var found = false;

			if (obj[idSelector] === id) {
				found = true;
				property = list.splice(index, 1)[0];
			}
			return !found;
		});
		return property;
	}

	/*
	 * For a list of given exclusions, ensure that if any of the variables are in a specified list of selected variables, their exclusions
	 * are removed from the given list of properties.
	 *
	 * @param {object} exclusions an exclusion object. Each key in the object represents a variable id that, if found, should have a matched
	 * variable removed. Each object in the exclusion should have two properties, `variableId` - the variable to remove, and `propertyId` -
	 * the id of the property of the variable to be removed.
	 * @param {object} selectedVariables an object representing all currently selected variables. Each key represents a variable id that is
	 * selected, and it's value the name of that variable.
	 * @param {object[]} properties a list of properties from which to remove variables / properties as appropriate
	 * @returns {object[]} a list of properties that were removed
	 */
	function _performExclusions(exclusions, selectedVariables, properties) {

		var removedProperties = [],
			key,
			exclusion,
			id,
			index;

		for (key in exclusions) {
			if (exclusions.hasOwnProperty(key)) {

				id = parseInt(key, 10);
				exclusion = exclusions[id];

				// If any of the variables that need translating are selected, we must remove it's counterpart
				if (typeof selectedVariables[id] !== 'undefined') {
					index = _findPropertyById(exclusion.propertyId, properties);

					// Remove the variable if we found the property. Weird if we can't find the property, but not an issue since we just
					// wanted to make sure one of it's variables was excluded
					if (index !== -1) {
						_removeById(exclusion.variableId, properties[index].standardVariables, 'id');

						// If the property has no more variables, remove it too
						if (properties[index].standardVariables.length === 0) {
							removedProperties.push(properties.splice(index, 1)[0]);
						}
					}
				}
			}
		}
		return removedProperties;
	}

	/*
	 * Remove the specified variables from the given list.
	 *
	 * @param {object[]} variableList a list of variables, each with a variableId and propertyId property
	 * @param {object[]} properties a list of properties from which to remove variables / properties as appropriate.
	 * @returns {object[]} a list of properties that were removed
	 */
	function _removeVariables(variableList, properties) {

		var removedProperties = [],
			index;

		$.each(variableList, function(i, variable) {

			index = _findPropertyById(variable.propertyId, properties);

			// Remove the variable if we found the property. Weird if we can't find the property, but not an issue since we just
			// wanted to make sure one of it's variables was excluded
			if (index !== -1) {
				_removeById(variable.variableId, properties[index].standardVariables, 'id');

				// If the property has no more variables, remove it too
				if (properties[index].standardVariables.length === 0) {
					removedProperties.push(properties.splice(index, 1)[0]);
				}
			}
		});
		return removedProperties;
	}

	/* FIXME - this logic should be in the back end
	 *
	 * Filters a list of properties according to some hard coded rules (see comments for details).
	 *
	 * @param {object[]} properties the list of properties to filter
	 * @param {number} group properties and variables will be filtered to be specific to the group represented by this number
	 * @returns {object} an object with two properties, excluded and included - the list of properties that were removed and left
	 */
	function _filterProperties(properties, selectedVariables, group) {

		// Don't modify the original list
		var filteredProperties = JSON.parse(JSON.stringify(properties)),

			studyLevelBreedingMethodPropertyId = 2670,

			selectionExclusions = {
				// Don't allow user to select BM_CODE_VTE from the Breeding method property if BM_ID_VTE is present
				8262: {
					variableId: 8252,
					propertyId: 2670
				}
			},

			exclusions = [],
			removedProperty;

		// Currently we only have special handling for management details (1) and selection strategy (6)
		switch (group) {
			case 1805:
				// This property must be excluded as the variables it contains are duplicated by a dropdown on the main page
				removedProperty = _removeById(studyLevelBreedingMethodPropertyId, filteredProperties, 'propertyId');

				// If we found the property to remove. Weird but not disastrous if we can't find the variable to remove it.
				if (removedProperty) {
					exclusions.push(removedProperty);
				}

				break;
			case 1807:
				// Remove variables and properties as necessary
				exclusions = exclusions.concat(_performExclusions(selectionExclusions, selectedVariables, filteredProperties));
				break;
			default:
				break;
		}
		return {
			exclusions: exclusions,
			inclusions: filteredProperties
		};
	}

	function _findVariables(selectors) {

		var variableNameSelector = '.var-names',
			variables = {},
			findElement,
			allMatches,
			i,
			variableElement,
			variableId,
			nameElement,
			parent;

		$.each(selectors, function(index, startingSelector) {
			findElement = function() {
				return $(this).attr('id') === startingSelector + i + '.variable.cvTermId';
			};

			allMatches = $('[id^=' + startingSelector + ']');

			for (i = allMatches.length - 1; i >= 0; i--) {
				variableElement = allMatches.filter(findElement);

				if (variableElement.length > 0) {

					// Find the name of the variable
					parent = variableElement.parent();

					// If we're inside a td, go up to the row level element
					if (parent.is('td')) {
						parent = parent.parent('tr');
					}

					nameElement = parent.find(variableNameSelector)[0];
					variableId = parseInt($(variableElement[0]).attr('value'));

					if (typeof variableId === 'number') {
						variables[variableId] = nameElement ? $(nameElement).text() : null;
					} else {
						if (console) {
							console.error('Could not parse variable id from variable element with id \'' + startingSelector + i +
								'.variable.cvTermId' + '\'. Value that was attempted to be parsed was ' + variableId);
						}
					}
				}

				// We expect there to be only one matching variable. If there are more than one, we have changed the way variables are
				// inserted into the page, and the logic here would have to change as well. There should really be unit tests for this.
				if (variableElement.length > 1) {
					if (console) {
						console.error('More than one variable with id \'' + startingSelector + i + '.variable.cvTermId' + '\' was found.');
					}
				}
			}

		});

		return variables;
	}

	/* FIXME - this logic should be in the back end
	 *
	 * If certain legacy variable IDs are found in the list of selected variables, we need to select their modern alternatives, so that the
	 * user can't select the same variable twice. This method will alter the list of variables passed to it to contain any necessary
	 * additions.
	 *
	 * @param {object} currentlySelectedVariables the currently selected variables
	 * @returns {number[]} a list of added variable IDs
	 */
	function _performVariableSelectionConversion(currentlySelectedVariables) {

		var variableConversions = {

			// Mark PI_NAME as selected if PI_ID is present
			8110: 8100,

			// Mark COOPERATOR as selected if COOPERATOR_ID is present
			8372: 8373,

			// Mark LOCATION_NAME as selected if LOCATION_ID is present
			8190: 8180
		},

		addedVariables = [];

		$.each(variableConversions, function(key, val) {
			if (variableConversions.hasOwnProperty(key)) {

				// If we find one of the legacy variables in the list of currently selected variables, add it's modern equivalent to the
				// list, and set it's alias (if it has one) to be equal to that of the existing variable
				if (currentlySelectedVariables[key]) {
					currentlySelectedVariables[val] = currentlySelectedVariables[key];
					addedVariables.push(val);
				}
			}
		});

		return addedVariables;
	}

	function addSelectedVariables(e) {

		var group = e.group,
			data = e.responseData;

		switch (group) {
			case MODES.MANAGEMENT_DETAILS:
				createDynamicSettingVariables(data,
						'studyLevelVariables', 'nurseryLevelSettings-dev', 'nurseryLevelSettings', group, '');
				break;
			case MODES.FACTORS:
				createTableSettingVariables(data, 'plotLevelVariables', 'plotLevelSettings', group);
				break;
			case MODES.TRAITS:
				hideDummyRow('baselineTraitSettings');
				createTableSettingVariables(data, 'baselineTraitVariables', 'baselineTraitSettings', group);
				checkTraitsAndSelectionVariateTable('', false);
				break;
			case MODES.SELECTION_VARIATES:
				hideDummyRow('selectionVariatesSettings');
				createTableSettingVariables(data,
						'selectionVariatesVariables',
						'selectionVariatesSettings', group);
				checkTraitsAndSelectionVariateTable('', false);
				break;
			case MODES.NURSERY_CONDITIONS:
				createDynamicSettingVariables(data,
						'nurseryConditions', 'nurseryConditionsSettings',
						'nurseryConditionsSettings', group, 'Cons');
				break;
			default:
				createDynamicSettingVariables(data, 'studyLevelVariables', 'nurseryLevelSettings-dev',
				'nurseryLevelSettings', group, '');
		}
		checkShowSettingsFormReminder();
	}

	ChooseSettings = function(translations) {

		var group;

		// Need to think about a better pattern than this
		this._translations = translations;

		variableSelectionGroups[MODES.MANAGEMENT_DETAILS] = {
			selector: '.chs-management-details',
			label: translations.mdLabel,
			placeholder: translations.mdPlaceholder,
			variableMarkupSelector: 'studyLevelVariables'
		};

		variableSelectionGroups[MODES.FACTORS] = {
			selector: '.chs-factors',
			label: translations.fdLabel,
			placeholder: translations.fdPlaceholder,
			variableMarkupSelector: 'plotLevelVariables'
		};

		variableSelectionGroups[MODES.TRAITS] = {
			selector: '.chs-traits',
			label: translations.tdLabel,
			placeholder: translations.tdPlaceholder,
			variableMarkupSelector: 'baselineTraitVariables'
		};

		variableSelectionGroups[MODES.SELECTION_VARIATES] = {
			selector: '.chs-selection-variates',
			label: translations.svLabel,
			placeholder: translations.svPlaceholder,
			variableMarkupSelector: 'selectionVariatesVariables'
		};

		variableSelectionGroups[MODES.NURSERY_CONDITIONS] = {
			selector: '.chs-nursery-conditions',
			label: translations.ncLabel,
			placeholder: translations.ncPlaceholder,
			variableMarkupSelector: 'nurseryConditions'
		};

		// Populate selectors required to find selected variables from each group
		variableMarkupSelectors = [];

		for (group in variableSelectionGroups) {
			if (variableSelectionGroups.hasOwnProperty(group)) {
				variableMarkupSelectors.push(variableSelectionGroups[group].variableMarkupSelector);
			}
		}
	};

	ChooseSettings.prototype._initialiseVariableSelectionDialog = function() {
		this._variableSelection = new window.BMS.NurseryManager.VariableSelection({
			uniqueVariableError: this._translations.uniqueVariableError,
			generalAjaxError: this._translations.generalAjaxError,
			variableSelectedMessage: this._translations.variableSelectedMessage,
			invalidAliasError: this._translations.invalidAliasError
		});
		this._variableSelection.getModal().off('variable-select').on('variable-select', addSelectedVariables);

		return this._variableSelection;
	};

	ChooseSettings.prototype._openVariableSelectionDialog = function(e) {
		e.preventDefault();

		var groupId = e.data.group,
			group = variableSelectionGroups[groupId],
			groupTranslations = {
				label: group.label,
				placeholderLabel: group.placeholder
			},
			modal = this._variableSelection,
			generalAjaxErrorMessage = this._translations.generalAjaxError,
			selectedVariables,
			properties;

		// Initialise a variable selection modal if we haven't done so before
		if (!modal) {
			modal = this._initialiseVariableSelectionDialog();
		}

		selectedVariables = _findVariables(variableMarkupSelectors);
		_performVariableSelectionConversion(selectedVariables);

		// If we haven't loaded data for this group before, then load it
		if (!group.data) {
			$.getJSON('/Fieldbook/manageSettings/settings/properties?type=' + groupId, function (data) {
				variableSelectionGroups[groupId].data = data;

				properties = _filterProperties(variableSelectionGroups[groupId].data, selectedVariables, groupId);

				// Initialise a new Variable Selection instance, passing through the properties, group type and groupTranslations
				// TODO get variable usage
				modal.show(groupId, groupTranslations, {
					propertyData: properties.inclusions,
					excludedProperties: properties.exclusions,
					variableUsageData: [],
					selectedVariables: selectedVariables
				});

			}).fail(function(jqxhr, textStatus, error) {

				var errorMessage;

				showErrorMessage(null, generalAjaxErrorMessage);

				if (console) {
					errorMessage = textStatus + ', ' + error;
					console.error('Request to get properties for group ' + groupId + ' failed with error: ' + errorMessage);
				}
			});

		} else {
			// We've shown this before, and have the data. Just show the dialog. Note - we have to filter the properties again in case
			// they removed a variable that had caused a variable or property to previously be excluded from the list

			properties = _filterProperties(group.data, selectedVariables, groupId);

			modal.show(groupId, groupTranslations, {
				propertyData: properties.inclusions,
				excludedProperties: properties.exclusions,
				variableUsageData: [],
				selectedVariables: selectedVariables
			});
		}
	};

	ChooseSettings.prototype.initialiseVariableSelection = function() {

		var key,
			group,
			openDialogButton;

		// Initialising on click handlers for variable selection buttons
		for (key in variableSelectionGroups) {
			if (variableSelectionGroups.hasOwnProperty(key)) {
				group = variableSelectionGroups[key];

				openDialogButton = $(group.selector  + ' ' + dialogOpenSelector);

				if (openDialogButton) {
					openDialogButton.click({group: parseInt(key, 10)}, $.proxy(this._openVariableSelectionDialog, this));
				} else {
					// This shouldn't happen.
					if (console) {
						console.error('Failed to find button with selector \'' + group.selector  + ' ' + dialogOpenSelector + '\' to ' +
							'attach open dialog handler to.');
					}
				}

			}
		}
	};

	return ChooseSettings;
}());
