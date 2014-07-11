// These should moved into this file
/* global checkShowSettingsFormReminder */
/* global createDynamicSettingVariables, createTableSettingVariables, checkTraitsAndSelectionVariateTable, hideDummyRow */

window.ChooseSettings = (function() {
	'use strict';

	var MODES = {
			MANAGEMENT_DETAILS: 1,
			FACTORS: 2,
			TRAITS: 3,
			SELECTION_VARIATES: 6,
			NURSERY_CONDITIONS: 7
		},

		modalSelector = '.nrm-var-selection-modal',
		dialogOpenSelector = '.nrm-var-select-open',

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
	 * @returns {object} the object that was removed
	 */
	function _removeById(id, list, idSelector) {

		var property;

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

					// Remove the excluded variable from the property
					_removeById(exclusion.variableId, properties[index].standardVariables, 'id');

					// If the property has no more variables, remove it too
					if (properties[index].standardVariables.length === 0) {
						removedProperties.push(properties.splice(index, 1)[0]);
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

			// Remove the variable from the property
			_removeById(variable.variableId, properties[index].standardVariables, 'id');

			// If the property has no more variables, remove it too
			if (properties[index].standardVariables.length === 0) {
				removedProperties.push(properties.splice(index, 1)[0]);
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

			managementDetailExclusions = {

				// Don't allow user to select PI_NAME from the Person property if PI_ID is present
				8110: {
					variableId: 8100,
					propertyId: 2080
				},

				// Don't allow user to select COOPERATOR from the PERSON property if COOPERATOR_ID is present
				8372: {
					variableId: 8373,
					propertyId: 2080
				},

				// Don't allow user to select LOCATION_NAME from the Location property if LOCATION_ID is present
				8190: {
					variableId: 8180,
					propertyId: 2110
				}
			},

			// There are a basic set of details hard coded into the page that should not be presented as variables
			basicDetails = [
				{
					variableId: 8005, // STUDY_NAME
					propertyId: 2010  // Study
				},
				{
					variableId: 8007, // STUDY_TITLE
					propertyId: 2012  // Study title
				},

				{
					variableId: 8009, // STUDY_UPDATE
					propertyId: 2045  // Update date
				},

				{
					variableId: 8020, // Study_UID
					propertyId: 2002  // User
				},

				{
					variableId: 8030, // STUDY_OBJECTIVE
					propertyId: 2014  // Study objective
				},

				{
					variableId: 8050, // START_DATE
					propertyId: 2050  // Start date
				},

				{
					variableId: 8060, // END_DATE
					propertyId: 2052  // End date
				}
			],

			selectionExclusions = {
				// Don't allow user to select BM_CODE_VTE from the Breeding method property if BM_ID_VTE is present
				8262: {
					variableId: 8252,
					propertyId: 2670
				}
			},

			exclusions = [];

		// Currently we only have special handling for management details (1) and selection strategy (6)
		switch (group) {
			case 1:
				// This property must be excluded as the variables it contains are duplicated by a dropdown on the main page
				exclusions.push(_removeById(studyLevelBreedingMethodPropertyId, filteredProperties, 'propertyId'));

				// Remove variables and properties as necessary
				exclusions = exclusions.concat(_performExclusions(managementDetailExclusions, selectedVariables, filteredProperties));
				exclusions = exclusions.concat(_removeVariables(basicDetails, filteredProperties));
				break;
			case 6:
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

					variables[variableId] = nameElement ? $(nameElement).text() : null;
				}
				// TODO Error handling
			}
		});

		return variables;
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

	ChooseSettings = function(modalContainerSelector, translations) {

		var group;

		// Look for any existing variables and instaniate our list of them

		variableSelectionGroups[MODES.MANAGEMENT_DETAILS] = {
			selector: '.nrm-management-details',
			label: translations.mdLabel,
			placeholder: translations.mdPlaceholder,
			variableMarkupSelector: 'studyLevelVariables'
		};

		variableSelectionGroups[MODES.FACTORS] = {
			selector: '.nrm-factors',
			label: translations.fdLabel,
			placeholder: translations.fdPlaceholder,
			variableMarkupSelector: 'plotLevelVariables'
		};

		variableSelectionGroups[MODES.TRAITS] = {
			selector: '.nrm-traits',
			label: translations.tdLabel,
			placeholder: translations.tdPlaceholder,
			variableMarkupSelector: 'baselineTraitVariables'
		};

		variableSelectionGroups[MODES.SELECTION_VARIATES] = {
			selector: '.nrm-selection-variates',
			label: translations.svLabel,
			placeholder: translations.svPlaceholder,
			variableMarkupSelector: 'selectionVariatesVariables'
		};

		variableSelectionGroups[MODES.NURSERY_CONDITIONS] = {
			selector: '.nrm-nursery-conditions',
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

		$(modalContainerSelector).on('nrm-variable-select', addSelectedVariables);
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
			selectedVariables,
			properties;

		// Initialise a variable selection modal if we haven't done so before
		if (!modal) {
			modal = this._variableSelection = new window.BMS.NurseryManager.VariableSelection(modalSelector);
		}

		selectedVariables = _findVariables(variableMarkupSelectors);

		// If we haven't loaded data for this group before, then load it
		if (!group.data) {

			$.getJSON('/Fieldbook/OntologyBrowser/settings/properties?groupId=' + groupId, function(data) {
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

			});

			// TODO Error handling

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
			group;

		// Initialising on click handlers for variable selection buttons
		for (key in variableSelectionGroups) {
			if (variableSelectionGroups.hasOwnProperty(key)) {
				group = variableSelectionGroups[key];

				$(group.selector  + ' ' + dialogOpenSelector).click({group: parseInt(key, 10)},
					$.proxy(this._openVariableSelectionDialog, this));
			}
		}
	};

	return ChooseSettings;
}());
