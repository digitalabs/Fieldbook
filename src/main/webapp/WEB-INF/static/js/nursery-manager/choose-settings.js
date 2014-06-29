// These should moved into this file
/* global checkShowSettingsFormReminder */
/* global createDynamicSettingVariables, createTableSettingVariables, checkTraitsAndSelectionVariateTable, hideDummyRow */

window.ChooseSettings = (function() {
	'use strict';

	var modalSelector = '.nrm-vs-modal',
		variableSelectionGroups,
		ChooseSettings;

	function getStandardVariables(variableType, successFn) {
		$.ajax({
			url: '/Fieldbook/NurseryManager/createNursery/displayAddSetting/' + variableType,
			type: 'GET',
			cache: false,
			success: successFn
			// TODO Error handling
		});
	}

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

	ChooseSettings = function(translations) {

		variableSelectionGroups = {
			1: {
				selector: '.nrm-md-variable-select',
				label: translations.mdLabel,
				placeholder: translations.mdPlaceholder
			},
			2: {
				selector: '.nrm-fct-variable-select',
				label: translations.fdLabel,
				placeholder: translations.fdPlaceholder
			},
			6: {
				selector: '.nrm-sv-variable-select',
				label: translations.svLabel,
				placeholder: translations.svPlaceholder
			},
			3: {
				selector: '.nrm-trait-variable-select',
				label: translations.tdLabel,
				placeholder: translations.tdPlaceholder
			},
			7: {
				selector: '.nrm-nc-variable-select',
				label: translations.ncLabel,
				placeholder: translations.ncPlaceholder
			}
		};

		// TODO HH Scope this a little better
		$(document).on('variable-select', addSelectedVariables);
	};

	ChooseSettings.prototype._openVariableSelectionDialog = function(e) {
		e.preventDefault();

		var groupId = e.data.group,
			group = variableSelectionGroups[groupId],
			groupTranslations = {
				label: group.label,
				placeholderLabel: group.placeholder
			},
			properties;

		if (!this._variableSelection) {
			this._variableSelection = new window.BMS.NurseryManager.VariableSelection($(modalSelector));
		}

		if (!group.data) {
			// Get properties for this group
			properties = getStandardVariables(groupId, $.proxy(function(data) {
				// Initialise a new Variable Selection instance, passing through the properties, group type and groupTranslations
				variableSelectionGroups[groupId].data = {
					treeData: data.treeData,
					searchTreeData: data.searchTreeData
				};

				this._variableSelection.show(groupId, variableSelectionGroups[groupId].data, groupTranslations);
			}, this));
		} else {
			// We've shown this before, and have the data. Just show the dialog.
			this._variableSelection.show(groupId, group.data, groupTranslations);
		}
	};

	ChooseSettings.prototype.initialiseVariableSelection = function() {

		var key,
			group;

		// Initialising on click handlers for variable selection buttons
		for (key in variableSelectionGroups) {
			if (variableSelectionGroups.hasOwnProperty(key)) {
				group = variableSelectionGroups[key];

				$(group.selector).click({group: parseInt(key, 10)}, this._openVariableSelectionDialog);
			}
		}
	};

	return ChooseSettings;
}());
