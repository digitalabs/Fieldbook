window.TrialSettingsManager = (function() {
	'use strict';

	var MODES = {
			TRIAL_SETTINGS: 1,
			GERMPLASM_FACTORS: 8,
			MEASUREMENT_TRAITS: 3,
			TRIAL_ENVIRONMENT: 4,
			TREATMENT_FACTORS: 5,
			TRIAL_CONDITIONS: 7
		},
        modalSelector = '.vs-modal',
		variableSelectionGroups = {},
		TrialSettingsManager;

	TrialSettingsManager = function(translations) {

		// Look for any existing variables and instaniate our list of them

		$.each(MODES, function(key, value) {
			variableSelectionGroups[value] = {
				label: translations[value] ? translations[value].label : '',
				placeholder: translations[value] ? translations[value].placeholder : ''
			};
		});

	};

    TrialSettingsManager.prototype._initialiseVariableSelectionDialog = function() {
        this._variableSelection = new window.BMS.NurseryManager.VariableSelection({
            // FIXME pass in translated value with key variable.selection.unique.variable.error from html
            uniqueVariableError: 'This name has been used before, please enter a different name.'
        });
        //this._variableSelection.getModal().on('variable-select', addSelectedVariables);

        return this._variableSelection;
    };

	TrialSettingsManager.prototype._openVariableSelectionDialog = function(params) {
		var groupId = params.variableType,
			group = variableSelectionGroups[groupId],
			groupTranslations = {
				label: group.label,
				placeholderLabel: group.placeholder
			},
			modal = this._variableSelection;

		// Initialise a variable selection modal if we haven't done so before
		if (!modal) {
			modal = this._variableSelection = this._initialiseVariableSelectionDialog();
		}

		// If we haven't loaded data for this group before, then load it
		if (!group.data) {

			$.getJSON('/Fieldbook/OntologyBrowser/settings/properties?groupId=' + groupId + '&useTrialFiltering=true', function(data) {
				group.data = data;

				// Initialise a new Variable Selection instance, passing through the properties, group type and groupTranslations
				// TODO get variable usage
				modal.show(groupId, groupTranslations, {
					propertyData: group.data,
					variableUsageData: [],
					selectedVariables: params.retrieveSelectedVariableFunction()
				});
			});

			// TODO Error handling

		} else {
			// We've shown this before, and have the data. Just show the dialog.
			modal.show(groupId, groupTranslations, {
				propertyData: group.data,
				variableUsageData: [],
				selectedVariables: params.retrieveSelectedVariableFunction()
			});
		}
	};

	return TrialSettingsManager;
}());
