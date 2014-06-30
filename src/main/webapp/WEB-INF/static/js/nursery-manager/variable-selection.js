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

	var VariableSelection;

	function selectVariable(group) {

		// TODO Prevent already added variables from being able to be selected
		// TODO HH Need the ability to rename variables

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
					type: 'nrm-variable-select',
					group: group,
					responseData: data
				});
			}
			// TODO HH Error handling
		});
	}

	VariableSelection = function(selector) {
		this._modalSelector = selector;
		this._modal = $(selector);

		this._modal.on('hide.bs.modal', function() {
			$('.nrm-var-select-add').off('click');
		});
	};

	VariableSelection.prototype.show = function(group, data, translations) {

		var modalHeader = $(this._modalSelector + ' ' + '.modal-header'),
			title;

		// Clear title
		modalHeader.empty();

		// Append new title
		title = $('<h4 class="modal-title" id="nrm-var-selection-modal-title">' + translations.label + '</h4>');
		modalHeader.append(title);

		//displayOntologyTree(treeDivId, data.treeData, data.searchTreeData, 'srch-term');

		$('.nrm-var-select-add').on('click', null, {group: group}, $.proxy(function(e) {
			e.preventDefault();
			selectVariable(e.data.group);
		}, this));

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
