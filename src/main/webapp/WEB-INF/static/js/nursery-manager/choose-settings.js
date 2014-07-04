// These should moved into this file
/* global checkShowSettingsFormReminder */
/* global createDynamicSettingVariables, createTableSettingVariables, checkTraitsAndSelectionVariateTable, hideDummyRow */

window.ChooseSettings = (function() {
	'use strict';

	var modalSelector = '.nrm-var-selection-modal',
		dialogOpenSelector = '.nrm-var-select-open',
		variableSelectionGroups,
		ChooseSettings;

	function addSelectedVariables(e) {

		var group = e.group,
			data = e.responseData;

		switch (group) {
			case 1:
				createDynamicSettingVariables(data,
						'studyLevelVariables', 'nurseryLevelSettings-dev', 'nurseryLevelSettings', group, '');
				break;
			case 2:
				createTableSettingVariables(data, 'plotLevelVariables', 'plotLevelSettings', group);
				break;
			case 3:
				hideDummyRow('baselineTraitSettings');
				createTableSettingVariables(data, 'baselineTraitVariables', 'baselineTraitSettings', group);
				checkTraitsAndSelectionVariateTable('', false);
				break;
			case 6:
				hideDummyRow('selectionVariatesSettings');
				createTableSettingVariables(data,
						'selectionVariatesVariables',
						'selectionVariatesSettings', group);
				checkTraitsAndSelectionVariateTable('', false);
				break;
			case 7:
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

		variableSelectionGroups = {
			1: {
				selector: '.nrm-management-details',
				label: translations.mdLabel,
				placeholder: translations.mdPlaceholder
			},
			2: {
				selector: '.nrm-factors',
				label: translations.fdLabel,
				placeholder: translations.fdPlaceholder
			},
			6: {
				selector: '.nrm-selection-variates',
				label: translations.svLabel,
				placeholder: translations.svPlaceholder
			},
			3: {
				selector: '.nrm-traits',
				label: translations.tdLabel,
				placeholder: translations.tdPlaceholder
			},
			7: {
				selector: '.nrm-nursery-conditions',
				label: translations.ncLabel,
				placeholder: translations.ncPlaceholder
			}
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
				modal.show(groupId, data, [], groupTranslations);
			});

			// TODO Error handling

		} else {
			// We've shown this before, and have the data. Just show the dialog.
			modal.show(groupId, group.data, group.usageData, groupTranslations);
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
