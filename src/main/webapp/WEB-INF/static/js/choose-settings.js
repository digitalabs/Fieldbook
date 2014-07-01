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
		getStandardVariables: function(variableType, treeDivId, evalFunction) {
			var treeData,
				searchTreeData;

			$.ajax({
				url: '/Fieldbook/manageSettings/displayAddSetting/' + variableType,
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

                    if (!evalFunction) {
                        $('#addVariables').attr('onclick', 'javascript: submitSelectedVariables(' + variableType + ');');
                    } else {
                        $('#addVariables').removeAttribute('onclick');
                        $('#addVariables').on('click', evalFunction);
                    }

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