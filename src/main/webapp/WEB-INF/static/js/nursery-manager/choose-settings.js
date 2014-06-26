// These should moved into this file
/* global checkShowSettingsFormReminder */
/* global createDynamicSettingVariables, createTableSettingVariables, checkTraitsAndSelectionVariateTable, hideDummyRow */

window.ChooseSettings = (function() {
	'use strict';

	var variableSelectionGroups,
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

	function openVariableSelectionDialog(e) {
		e.preventDefault();

		var group = e.data.group,
			groupData = variableSelectionGroups[group],
			VariableSelection = window.BMS.NurseryManager.VariableSelection,
			properties;

		if (!groupData.modal) {
			// Get properties for this group
			properties = getStandardVariables(group, function(data) {

				// Initialise a new Variable Selection instance, passing through the properties, group type and translations
				variableSelectionGroups[group].modal = new VariableSelection(group, {
					treeData: data.treeData,
					searchTreeData: data.searchTreeData
				}, {
					label: groupData.label,
					placeholderLabel: groupData.placeholder
				});

				// Call Variable Selection.show
				variableSelectionGroups[group].modal.show();
			});

		} else {
			// We've opened this dialog before, so just call Variable Selection.show
			groupData.modal.show();
		}
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
				placeholder: translations.mdPlaceholder,
				modal: null
			},
			2: {
				selector: '.nrm-fct-variable-select',
				label: translations.fdLabel,
				placeholder: translations.fdPlaceholder,
				modal: null
			},
			6: {
				selector: '.nrm-sv-variable-select',
				label: translations.svLabel,
				placeholder: translations.svPlaceholder,
				modal: null
			},
			3: {
				selector: '.nrm-trait-variable-select',
				label: translations.tdLabel,
				placeholder: translations.tdPlaceholder,
				modal: null
			},
			7: {
				selector: '.nrm-nc-variable-select',
				label: translations.ncLabel,
				placeholder: translations.ncPlaceholder,
				modal: null
			}
		};

		// TODO HH Scope this a little better
		$(document).on('variable-select', addSelectedVariables);
	};

	ChooseSettings.prototype.initialiseVariableSelection = function() {

		var key,
			group;

		// Initialising on click handlers for variable selection buttons
		for (key in variableSelectionGroups) {
			if (variableSelectionGroups.hasOwnProperty(key)) {
				group = variableSelectionGroups[key];

				$(group.selector).click({group: parseInt(key, 10)}, openVariableSelectionDialog);
			}
		}
	};

	return ChooseSettings;
}());
