/* global displayOntologyTree */

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

BMS.NurseryManager.VariableSelection = (function($) {
	'use strict';

	var treeDivId = 'ontologyBrowserTree',

		VariableSelection;

	function clearAttributeFields() {
		$('selectedTraitClass').html('&nbsp;');
		$('selectedProperty').html('&nbsp;');
		$('#selectedMethod').html('&nbsp;');
		$('#selectedScale').html('&nbsp;');
		$('#selectedDataType').html('&nbsp;');
		$('#selectedRole').html('&nbsp;');
		$('#selectedCropOntologyId').html('&nbsp;');
		$('#selectedStdVarId').val('');
		$('#selectedName').val('');
	}

	function submitSelectedVariables(group, successFn) {

		// TODO Prevent already added variables from being able to be selected
		// TODO Can we get by without variable renaming?

		// Test data
		var testVariable;

		switch (group) {
			case 1:
				testVariable = {
					cvTermId: 8008,
					name: 'STUDY_DATE'
				};
				break;
			case 7:
				testVariable = {
					cvTermId: 22531,
					name: 'SOILPH'
				};
				break;
			case 2:
				testVariable = {
					cvTermId: 8255,
					name: 'ENTRY_TYPE'
				};
				break;

			case 3:
				testVariable = {
					cvTermId: 22564,
					name: 'HT'
				};
				break;
			case 6:
				testVariable = {
					cvTermId: 8263,
					name: 'NPSEL'
				};
				break;
		}

		$.ajax({
			url: '/Fieldbook/NurseryManager/createNursery/addSettings/' + group,
			type: 'POST',
			data: JSON.stringify({selectedVariables: [testVariable]}),
			dataType: 'json',
			headers: {
				Accept: 'application/json',
				'Content-Type': 'application/json'
			},
			success: function(data) {
				$.event.trigger({
					type: 'variable-select',
					group: group,
					responseData: data
				});
				successFn();
			}
		});
	}

	VariableSelection = function(modal) {
		this._modal = modal;

		this._modal.on('hide.bs.modal', function() {
			$('#addVariables').off('click');
		});
	};

	VariableSelection.prototype.show = function(group, data, translations) {

		if ($('#' + treeDivId + ' .fbtree-container').length > 0) {
			$('#' + treeDivId).dynatree('destroy');
		}

		displayOntologyTree(treeDivId, data.treeData, data.searchTreeData, 'srch-term');
		$('#' + 'srch-term').val('');

		// clear selected variables table and attribute fields
		$('#newVariablesList > tbody').empty();

		clearAttributeFields();

		$('.nrm-vs-modal .fbk-modal-title').text(translations.label);
		$('.nrm-vs-modal .nrm-vs-hint-placeholder').html(translations.placeholderLabel);

		$('#ontology-detail-tabs').empty().html($('.variable-detail-info').html());
		$('#variable-details').html('');

		$('#addVariables').on('click', null, {group: group}, $.proxy(function(e) {
			e.preventDefault();
			submitSelectedVariables(e.data.group, $.proxy(this.hide, this));
		}, this));

		$('#newVariablesList').addClass('fbk-hide');

		// Show the modal
		this._modal.modal({
			backdrop: 'static',
			keyboard: true
		});
	};

	VariableSelection.prototype.hide = function() {
		this._modal.modal('hide');
	};

	return VariableSelection;

})(jQuery);
