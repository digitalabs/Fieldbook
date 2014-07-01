/*global displayOntologyTree, showErrorMessage, getJquerySafeId */

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

    // TODO : fix use of global message variables
    function submitSelectedVariables(variableType) {
        if ($('#newVariablesList tbody tr').length === 0) {
            showErrorMessage('', noVariableAddedMessage);
        } else if ($('#newVariablesList tbody tr').length > 0 && hasNoVariableName()) {
            showErrorMessage('', noVariableNameError);
        } else if ($('#newVariablesList tbody tr').length > 0) {
            var varName = validateUniqueVariableName();
            if (varName !== '') {
                showErrorMessage('', errorTheVariable + ' &quot;' + varName + '&quot; ' + errorTheVariableNurseryUnique);
                return;
            }

            replaceNameVariables();
            var serializedData = $('input.addVariables').serialize();
            $('#page-message-modal').html('');

            return $.ajax({
                url: '/Fieldbook/manageSettings/addSettings/' + variableType,
                type: 'POST',
                data: serializedData,
                success: function () {
                    $('#addVariablesSettingModal').modal('hide');
                }
            });
        } else {
            showErrorMessage('', varInListMessage);
        }
    }

    function replaceNameVariables() {
        $.each($('#newVariablesList tbody tr'), function (index, row) {
            var value = $(
                $(row).children('td:nth-child(1)').children(
                        '#' + getJquerySafeId('selectedVariables' + index + '.cvTermId'))).val();
            // use the id counterpart of the name variable
            $(
                $(row).children('td:nth-child(1)').children(
                        '#' + getJquerySafeId('selectedVariables' + index + '.cvTermId')))
                .val(getIdCounterpart(value, idNameVariables.split(',')));
        });

    }

    function getIdCounterpart(selectedVariable, idNameCombinationVariables) {
        var inList = selectedVariable;
        // return the id counterpart of the variable selected if it is in the list
        $.each(idNameCombinationVariables, function (index, item) {
            if (parseInt(item.split('|')[1], 10) === parseInt(selectedVariable, 10)) {
                inList = parseInt(item.split('|')[0], 10);
                return false;
            }
        });
        return inList;
    }

    function hasNoVariableName() {
        var result = false;
        $.each($('#newVariablesList tbody tr'), function (index, row) {
            if ($($(row).children('td:nth-child(1)').children('#' + getJquerySafeId('selectedVariables' + index + '.name'))).val() === '') {
                result = true;
            }
        });
        return result;
    }

    function validateUniqueVariableName() {

        var existingNameMap = [],
            isFound = false,
            existingVarName = '',
            newName = '';
        $('.var-names').each(function () {
            var varName = $.trim($(this).html()).toUpperCase();
            existingNameMap[varName] = $(this).html();
        });
        $('input[type=text].addVariables').each(function () {
            newName = $.trim($(this).val()).toUpperCase();
            if (isFound === false && existingNameMap[newName] !== undefined) {
                existingVarName = $.trim($(this).val());
                isFound = true;
                return;
            } else {
                existingNameMap[newName] = newName;
            }
        });
        return existingVarName;
    }

	return {
		getStandardVariables: function(labels, variableType, treeDivId, chainFunction) {
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

                    // clear the selected variable details display
                    $('#ontology-detail-tabs').empty().html($('.variable-detail-info').html());
                    $('#variable-details').html('');

                    // set the display labels for the modal dialog
                    $('.nrm-vs-modal .fbk-modal-title').text(labels.label);
                    $('.nrm-vs-modal .nrm-vs-hint-placeholder').html(labels.placeholderLabel);


					clearAttributeFields();

                    if (!chainFunction) {
                        $('#addVariables').removeAttr('onclick');
                        $('#addVariables').on('click', function() {
                            submitSelectedVariables(variableType);
                            $('#addVariables').off('click');
                        });
                    } else {
                        $('#addVariables').removeAttr('onclick');
                        $('#addVariables').on('click', function () {
                            var promise = submitSelectedVariables(variableType);
                            promise.done(function (data) {
                                chainFunction.apply(this, [data, variableType]);
                                $('#addVariables').off('click');
                            });
                        });
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

function addVariableToList() {
    'use strict';
    var newRow;
    var rowCount = $('#newVariablesList tbody tr').length;
    var ctr;

    // get the last counter for the selected variables and add 1
    if (rowCount === 0) {
        ctr = 0;
    } else {
        var lastVarId = $(
            "#newVariablesList tbody tr:last-child td input[type='hidden']")
            .attr('name');
        ctr = parseInt(lastVarId.substring(lastVarId.indexOf('[') + 1,
            lastVarId.indexOf(']')), 10) + 1;
    }

    var length = $("#newVariablesList tbody tr").length + 1;
    var className = length % 2 == 1 ? 'even' : 'odd';

    if (idNameCounterpartSelected($("#selectedStdVarId").val())) {
        // if selected variable is an id/name counterpart of a variable already
        // selected/added
        showErrorMessage('', idNameCounterpartAddedError);
    } else if (notInList($("#selectedStdVarId").val()) && $("#selectedStdVarId").val() != "") {
        // if selected variable is not yet in the list and is not blank or new,
        // add it
        newRow = "<tr>";
        newRow = newRow
            + "<td class='"
            + className
            + "'><input type='hidden' class='addVariables cvTermIds' id='selectedVariables"
            + ctr + ".cvTermId' " + "name='selectedVariables[" + ctr
            + "].cvTermId' value='" + $("#selectedStdVarId").val() + "' />";
        newRow = newRow
            + "<input type='text' class='addVariables' id='selectedVariables"
            + ctr + ".name' " + "name='selectedVariables[" + ctr
            + "].name' maxLength='75' value='" + $("#selectedName").val()
            + "' /></td>";
        newRow = newRow + "<td class='" + className + "'>"
            + $("#selectedProperty").text() + "</td>";
        newRow = newRow + "<td class='" + className + "'>"
            + $("#selectedScale").text() + "</td>";
        newRow = newRow + "<td class='" + className + "'>"
            + $("#selectedMethod").text() + "</td>";
        newRow = newRow + "<td class='" + className + "'>"
            + $("#selectedRole").text() + "</td>";
        newRow = newRow + "</tr>";

        $("#newVariablesList").append(newRow);
        $("#page-message-modal").html("");

        $('#newVariablesList').removeClass('fbk-hide');
    } else {
        showErrorMessage('', varInListMessage);
    }
}

function idNameCounterpartSelected(selectedVariable) {
    'use strict';
    var itemToCompare = getIdNameCounterpart(selectedVariable, idNameVariables.split(","));

    if (itemToCompare != -1) {
        // if it is selected/added already
        if (!notInList(itemToCompare)) {
            return true;
        }
    }
    return false;
}

function getIdNameCounterpart(selectedVariable, idNameCombinationVariables) {
    'use strict';
    var inList = -1;
    // return the id or name counterpart of the selected variable if it is in
    // the combination list
    $.each(idNameCombinationVariables, function (index, item) {
        if (parseInt(item.split("|")[0], 10) === parseInt(selectedVariable, 10)) {
            inList = parseInt(item.split("|")[1], 10);
            return false;
        }
        if (parseInt(item.split("|")[1], 10) === parseInt(selectedVariable, 10)) {
            inList = parseInt(item.split("|")[0], 10);
            return false;
        }
    });
    return inList;
}

function notInList(id) {
    'use strict';
    var isNotInList = true;
    $.each($('.cvTermIds'), function () {
        if ($(this).val() == id) {
            isNotInList = false;
        }
    });
    return isNotInList;
}