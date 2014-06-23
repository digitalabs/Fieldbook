/*global displayOntologyTree */

window.ChooseSettings = (function() {
	'use strict';

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

	return {
		getStandardVariables: function(variableType, treeDivId) {
			var treeData,
				searchTreeData;

			$.ajax({
				url: '/Fieldbook/NurseryManager/createNursery/displayAddSetting/' + variableType,
				type: 'GET',
				cache: false,
				success: function(data) {
					if ($('#' + treeDivId + ' .fbtree-container').length > 0) {
						$('#' + treeDivId).dynatree('destroy');
					}
					treeData = data.treeData;
					searchTreeData = data.searchTreeData;
					displayOntologyTree(treeDivId, treeData, searchTreeData, 'srch-term');
					$('#' + 'srch-term').val('');

					// clear selected variables table and attribute fields
					$('#newVariablesList > tbody').empty();
					$('#page-message-modal').html('');

					clearAttributeFields();

					$('#addVariables').attr('onclick', 'javascript: submitSelectedVariables(' + variableType + ');');
					$('#newVariablesList').addClass('fbk-hide');
					$('#addVariablesSettingModal').modal({
						backdrop: 'static',
						keyboard: true
					});
				}
			});
		}
	};
}());
