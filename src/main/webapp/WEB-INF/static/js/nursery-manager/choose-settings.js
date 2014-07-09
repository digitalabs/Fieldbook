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
		ChooseSettings;

	function findVariables(startingSelector) {

		var allMatches = $('[id^=' + startingSelector + ']'),
			variableNameSelector = '.var-names',
			variables = {},
			findElement,
			variableElement,
			variableId,
			nameElement,
			parent;

		findElement = function() {
			return $(this).attr('id') === startingSelector + i + '.variable.cvTermId';
		};

		for (var i = allMatches.length - 1; i >= 0; i--) {
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
			modal = this._variableSelection;

		// Initialise a variable selection modal if we haven't done so before
		if (!modal) {
			modal = this._variableSelection = new window.BMS.NurseryManager.VariableSelection(modalSelector);
		}

		// If we haven't loaded data for this group before, then load it
		if (!group.data || !group.usageData) {

			$.getJSON('/Fieldbook/OntologyBrowser/settings/properties?groupId=' + groupId, function(data) {
				variableSelectionGroups[groupId].data = data;

				// Initialise a new Variable Selection instance, passing through the properties, group type and groupTranslations
				// TODO get variable usage
				modal.show(groupId, groupTranslations, {
					propertyData: data,
					variableUsageData: [],
					selectedVariables: findVariables(group.variableMarkupSelector)
				});
			});

			// TODO Error handling

		} else {
			// We've shown this before, and have the data. Just show the dialog.
			modal.show(groupId, groupTranslations, {
				propertyData: group.data,
				variableUsageData: group.usageData,
				selectedVariables: findVariables(group.variableMarkupSelector)
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
