/*global getJquerySafeId, showErrorMessage, oldLineSelected, changeAdvanceBreedingMethod, setCorrecMethodValues, noMethodVariatesError, oldMethodSelected, msgSamplePlotError, msgHarvestDateError, noLineVariatesError, setCorrectMethodValues, methodSuggestionsFav_obj, isInt, breedingMethodId, oldMethodSelected*/
function checkMethod() {
	'use strict';
	if ($('input[type=checkbox][name=methodChoice]:checked').val() == 1) {
		$('#methodIdFavorite').select2('enable', true);
		$('#methodIdAll').select2('enable', true);
		$('#showFavoriteMethod').prop('disabled', false);

		$('#method-variates-section').hide();
		$('.method-selection-div').find('input,select').prop('disabled', false);
		$('#methodIdAll').select2('enable', true);
		$('#methodIdFavorite').select2("enable", true);

		setCorrectMethodValues(true);
		changeAdvanceBreedingMethod();
	} else {
		$('#method-variates-section').show();
		$('.method-selection-div').find('input,select').prop('disabled', true);
		$('#methodIdAll').select2('enable', false);
		$('#methodIdFavorite').select2('enable', false);

		if ($('#namingConvention').val() != 3) {
			$('#showFavoriteMethod').prop('disabled', 'disabled');
			$('#methodIdFavorite').select2('enable', false);
			$('#methodIdAll').select2('enable', false);
		}
		oldMethodSelected = $('#' + getJquerySafeId('advanceBreedingMethodId')).val();
		$('#methodSelected').val($('#defaultMethodId').val());
		setCorrectMethodValues(false);
		// we show the bulk and lines section
		$('.bulk-section').css('display', 'none');
		$('.lines-section').css('display', 'none');
		if ($("#methodVariateId").has("option").length === 0) {
			$('input[type=checkbox][name=methodChoice]').prop('checked', true);
			$('input[type=checkbox][name=methodChoice]').change();
			showErrorMessage("page-advance-modal-message", noMethodVariatesError);
		}
		else {
			displaySectionsPerMethodVariateValues();
		}
	}
}

function displaySectionsPerMethodVariateValues() {
	'use strict';
	var id = $("#methodVariateId").val();
	if (id !== '') {
		$.ajax({
			url : '/Fieldbook/NurseryManager/advance/nursery/checkMethodTypeMode/' + id,
			type : "GET",
			cache : false,
			success : function(data) {
				if (data === 'LINE') {
					$('.lines-section').css('display', 'block');
				}
				else if (data === 'BULK') {
					$('.bulk-section').css('display', 'block');
				}
				else if (data === 'MIXED') {
					$('.lines-section').css('display', 'block');
					$('.bulk-section').css('display', 'block');
				}
			}
		});
	}
}

function setCorrectMethodValues(isCheckMethod) {
	'use strict';
	var isFound = false,
		dataVal = null,
		findId = $('#defaultMethodId').val(),
		objKey = null;

	if ($('#showFavoriteMethod').is(':checked')) {
		// we check if the default is in the favorite method list or not
		if (isCheckMethod) {
			findId = oldMethodSelected;
		}
		for (objKey in methodSuggestionsFav_obj) {
			if (methodSuggestionsFav_obj[objKey].id == findId) {
				isFound = true;
				dataVal = methodSuggestionsFav_obj[objKey];
				break;
			}
		}
		if (isFound) {
			$('#methodIdFavorite').select2('data', dataVal).trigger('change');
		} else if (methodSuggestionsFav_obj.length > 0) {
			// we set the first
			$('#methodIdFavorite').select2('data', methodSuggestionsFav_obj[0])
					.trigger('change');
		} else {
			$('#' + getJquerySafeId("advanceBreedingMethodId")).val(0);
		}
	} else {
		if (isCheckMethod) {
			findId = oldMethodSelected;
		}
		for (objKey in methodSuggestions_obj) {
			if (methodSuggestions_obj[objKey].id == findId) {
				isFound = true;
				dataVal = methodSuggestions_obj[objKey];
				break;
			}
		}
		if (isFound) {
			$("#methodIdAll").select2('data', dataVal).trigger('change');
		}
	}
}
function lineMethod() {
	if ($('input[type=checkbox][name=lineChoice]:checked').val() == 1) {
		$('#lineSelected').prop('disabled', false);
		$('#lineSelected').val(oldLineSelected);
		$("#line-variates-section").hide();
		$(".lines-per-plot-section").show();
	} else {
		$('#lineSelected').prop('disabled', 'disabled');
		oldLineSelected = $('#lineSelected').val();
		$('#lineSelected').val(1);
		$("#line-variates-section").show();
		$(".lines-per-plot-section").hide();
		if ($("#lineVariateId").has("option").length === 0) {
			$('input[type=checkbox][name=lineChoice]').prop('checked', true);
			$('input[type=checkbox][name=lineChoice]').change();
			showErrorMessage("page-advance-modal-message", noLineVariatesError);
		}
	}
}

function validateAdvanceNursery() {
	// validate number of sample per plot
	var numberOfSamplePerPlot = $('#lineSelected').val();
	if (numberOfSamplePerPlot === '') {
		showInvalidInputMessage(msgSamplePlotError);
		return false;
	}
	if (!isInt(numberOfSamplePerPlot)) {
		showInvalidInputMessage(msgSamplePlotError);
		return false;
	}
	if (Number(numberOfSamplePerPlot) < 1
			|| Number(numberOfSamplePerPlot) > 1000) {
		showInvalidInputMessage(msgSamplePlotError);
		return false;
	}
	if ($('#harvestDate').val() === '') {
		showInvalidInputMessage(msgHarvestDateError);
		return false;
	}

	if ($('#namingConvention').val() === '3' && $('#breedingMethodId').val() === '0') {
		showInvalidInputMessage(msgMethodError);
		return false;
	}
	return true;
}

function showCorrectLocationCombo() {
	var isChecked = $('#showFavoriteLocation').is(':checked');
	// if show favorite location is checked, hide all field locations, else,
	// show only favorite locations
	if (isChecked) {
		$('#s2id_harvestLocationIdFavorite').show();
		$('#s2id_harvestLocationIdAll').hide();
		if ($('#' + getJquerySafeId("harvestLocationIdFavorite")).select2(
				"data") != null) {
			$('#' + getJquerySafeId("harvestLocationId")).val(
					$('#' + getJquerySafeId("harvestLocationIdFavorite"))
							.select2("data").id);
			$('#' + getJquerySafeId("harvestLocationName")).val(
					$('#' + getJquerySafeId("harvestLocationIdFavorite"))
							.select2("data").text);
			$('#' + getJquerySafeId("harvestLocationAbbreviation")).val(
					$('#' + getJquerySafeId("harvestLocationIdFavorite"))
							.select2("data").abbr);

		} else {
			$('#' + getJquerySafeId("harvestLocationId")).val(0);
			$('#' + getJquerySafeId("harvestLocationName")).val("");
			$('#' + getJquerySafeId("harvestLocationAbbreviation")).val("");
		}
	} else {
		$('#s2id_harvestLocationIdFavorite').hide();
		$('#s2id_harvestLocationIdAll').show();
		if ($('#' + getJquerySafeId("harvestLocationIdAll")).select2("data") != null) {
			$('#' + getJquerySafeId("harvestLocationId")).val($('#' + getJquerySafeId("harvestLocationIdAll")).select2("data").id);
			$('#' + getJquerySafeId("harvestLocationName")).val($('#' + getJquerySafeId("harvestLocationIdAll")).select2("data").text);
			$('#' + getJquerySafeId("harvestLocationAbbreviation")).val($('#' + getJquerySafeId("harvestLocationIdAll")).select2("data").abbr);
		} else {
			$('#' + getJquerySafeId("harvestLocationId")).val(0);
			$('#' + getJquerySafeId("harvestLocationName")).val("");
			$('#' + getJquerySafeId("harvestLocationAbbreviation")).val("");
		}
	}
}

function showCorrectMethodCombo() {
	var isChecked = $('#showFavoriteMethod').is(':checked');
	// if show favorite Method is checked, hide all field locations, else, show
	// only favorite methods
	var methodSelect = false;
	if ($('input[type=checkbox][name=methodChoice]:checked').val() === '1')
		methodSelect = true;

	if (isChecked) {
		$('#s2id_methodIdFavorite').show();
		$('#s2id_methodIdAll').hide();
		setCorrectMethodValues(methodSelect);
		if ($('#' + getJquerySafeId("methodIdFavorite")).select2("data") != null) {
			$('#' + getJquerySafeId("breedingMethodId")).val($('#' + getJquerySafeId("methodIdFavorite")).select2("data").id);
		} else {
			$('#' + getJquerySafeId("breedingMethodId")).val(0);
		}
	} else {
		$('#s2id_methodIdFavorite').hide();
		$('#s2id_methodIdAll').show();
		setCorrectMethodValues(methodSelect);
		if ($('#' + getJquerySafeId("methodIdAll")).select2("data") != null) {
			$('#' + getJquerySafeId("breedingMethodId")).val($('#' + getJquerySafeId("methodIdAll")).select2("data").id);
		} else {
			$('#' + getJquerySafeId("breedingMethodId")).val(0);
		}
	}
}

function getBreedingMethodRowIndex() {
	var rowIndex = -1;
	$.each($('.nurseryLevelSettings'), function(index, row) {
		var cvTermId = $($(row).find('.1st').find('.cvTermIds')).val();
		if (parseInt(cvTermId, 10) == parseInt(breedingMethodId, 10) || parseInt(cvTermId, 10) == parseInt($('#breedingMethodCode').val(), 10)) {
			rowIndex = getIndexFromName($($(row).find('.1st').find('.cvTermIds')).attr('name'));
		}
	});

	$.each($('.breedingMethodDetails'), function(index, row) {
		rowIndex = getIndexFromName($($(row).find('.1st').find(".cvTermIds")).attr('name'));
	});
	return rowIndex;
}

function getLocationRowIndex() {
	var rowIndex = -1;
	$.each($(".nurseryLevelSettings"), function(index, row) {
		var cvTermId = $($(row).find('.1st').find(".cvTermIds")).val();
		if (parseInt(cvTermId, 10) == parseInt(locationId, 10)) {
			rowIndex = getIndexFromName($($(row).find('.1st').find(".cvTermIds")).attr("name"));
		}
	});

	return rowIndex;
}

function getIndexFromName(name) {
	return name.split('[')[1].split(']')[0];
}

function replacePossibleJsonValues(favoriteJson, allJson, index) {
	'use strict';
	$('#possibleValuesJson' + index).text(allJson);
	$('#possibleValuesFavoriteJson' + index).text(favoriteJson);
}

function setComboValues(suggestions_obj, id, name) {
	var dataVal = {
		id : '',
		text : '',
		description : ''
	}; // default value
	if (id != '') {
		var count = 0;
		// find the matching value in the array given
		for (count = 0; count < suggestions_obj.length; count++) {
			if (suggestions_obj[count].id == id) {
				dataVal = suggestions_obj[count];
				break;
			}
		}
	}
	// set the selected value of the combo
	$("#" + name).select2('data', dataVal);
}

function initializeStandardVariableSearch(variables) {
	// set values
	var stdVariableSuggestions_obj = [];
	$.each(variables, function(index, value) {
		stdVariableSuggestions_obj.push({
			'id' : value.id,
			'text' : value.name
		});
	});

	stdVariableSuggestions_obj = sortByKey(stdVariableSuggestions_obj, "text");

	$("#stdVarSearch").select2({
		query : function(query) {
			var data = {
				results : stdVariableSuggestions_obj
			};
			// return the array that matches
			data.results = $.grep(data.results, function(item, index) {
				return ($.fn.select2.defaults.matcher(query.term, item.text));

			});
			if (data.results.length === 0) {
				data.results.unshift({
					id : query.term,
					text : query.term
				});
			}

			query.callback(data);
		}
	}).unbind("change").on("change", function() {
		// set attribute values
		getStandardVariableDetailsModal($("#stdVarSearch").select2("data").id);
	});
	var dataVal = {
		'id' : '',
		'text' : ''
	};
	$("#stdVarSearch").select2('data', dataVal).trigger('change');
}

function getStandardVariableDetailsModal(id) {
	'use strict';
	if (id !== '') {
		$
				.ajax({
					url : '/Fieldbook/NurseryManager/createNursery/showVariableDetails/'
							+ id,
					type : "GET",
					cache : false,
					success : function(data) {
						$('#var-info').slideDown('slow');
						populateAttributeFields($.parseJSON(data));
					}
				});
	}

}

function populateAttributeFields(data) {
	'use strict';
	$('#selectedTraitClass').html(checkIfEmpty(data.traitClass));
	$('#selectedProperty').html(checkIfEmpty(data.property));
	$('#selectedMethod').html(checkIfEmpty(data.method));
	$('#selectedScale').html(checkIfEmpty(data.scale));
	$('#selectedDataType').html(checkIfEmpty(data.dataType));
	$('#selectedRole').html(checkIfEmpty(data.role));
	$('#selectedCropOntologyId').html(checkIfEmpty(data.cropOntologyId));
	$('#selectedStdVarId').val(data.cvTermId);
	$('#selectedName').val(data.name);
}

function checkIfEmpty(value) {
	'use strict';
	if (value === '') {
		return "&nbsp";
	} else {
		return value;
	}
}

function hideDummyRow(tableId) {
	$('#'+tableId).find('.dummy-row').remove();
}

function showDummyRow(tableId) {
	var dummyRow = '<tr class=\'dummy-row\'> ' +
                	'<td class=\'even\'>&nbsp;</td> ' +
                	'<td class=\'even\'>&nbsp;</td> ' +
                	'<td class=\'even\'>&nbsp;</td> </tr>';
	$('#'+tableId+' tbody').append(dummyRow);
}

function getLastRowIndex(name, hasTBody) {
	'use strict';
	if (hasTBody) {
		return $('#' + name + ' tbody tr').length - 1;
	} else {
		return $('#' + name + ' tr').length - 1;
	}
}

function createDynamicSettingVariables(data, name, tableId, rowClass, varType,
		posValSuffix) {
	var ctr = $('.' + rowClass).length; // getLastRowIndex("nurseryLevelSettings",
										// false) + 1;
	if (name === 'studyLevelVariables') {
		ctr++;
	}

	$
			.each(
					data,
					function(index, settingDetail) {
						var newRow = '<div class="row form-group ' + rowClass
								+ ' newVariable">';
						var isDelete = '';

						// include delete button if variable is deletable
						if (settingDetail.deletable) {
							isDelete = '<span style="font-size: 16px;" class="delete-icon" onclick="deleteVariable('
									+ varType
									+ ','
									+ settingDetail.variable.cvTermId
									+ ',$(this))"></span>';
						}

						// create html elements dynamically
						newRow = newRow
								+ '<div class="col-xs-5 col-md-5 1st">'
								+ isDelete
								+ '<input class="cvTermIds nurseryLevelVariableIdClass" type="hidden" id="'
								+ name + ctr + '.variable.cvTermId" name="'
								+ name + '[' + ctr
								+ '].variable.cvTermId" value="'
								+ settingDetail.variable.cvTermId
								+ '" />&nbsp;&nbsp;&nbsp;&nbsp;';

						newRow = newRow
								+ '<span style="word-wrap: break-word"  class="var-names control-label label-bold">'
								+ settingDetail.variable.name
								+ '</span>: &nbsp;</div>';

						newRow = newRow
									+ '<div class="col-xs-7 col-md-7 2nd">';

						var inputHtml = '';

						if (settingDetail.variable.widgetType === 'DROPDOWN') {
							inputHtml = createDropdownInput(ctr, name);
						} else if (settingDetail.variable.widgetType === 'DATE') {
							inputHtml = createDateInput(ctr, name);
						} else if (settingDetail.variable.widgetType === 'CTEXT') {
							inputHtml = createCharacterTextInput(ctr, name);
						} else if (settingDetail.variable.widgetType === 'NTEXT') {
							inputHtml = createNumericalTextInput(ctr, name);
						} else if (settingDetail.variable.widgetType === 'SLIDER') {
							inputHtml = createSliderInput(ctr,
									settingDetail.variable.minRange,
									settingDetail.variable.maxRange, name);
						}
						newRow = newRow + inputHtml;

						if (parseInt(settingDetail.variable.cvTermId, 10) == parseInt(breedingMethodId, 10) ||
								parseInt(settingDetail.variable.cvTermId, 10) === parseInt($('#breedingMethodCode').val(), 10)) {
							// show favorite method
							newRow = newRow
									+ '<div class="possibleValuesDiv"><input type="checkbox" id="'
									+ name
									+ ctr
									+ '.favorite1"'
									+ ' name="'
									+ name
									+ '['
									+ ctr
									+ '].favorite"'
									+ ' onclick="javascript: toggleMethodDropdown('
									+ ctr
									+ ');" />'
									+ '<input type="hidden" name="_'
									+ name
									+ '['
									+ ctr
									+ '].favorite" value="on" /> '
									+ '<span>&nbsp;&nbsp;'
									+ showFavoriteMethodLabel
									+ '</span></div>'
									+ '<div id="possibleValuesJson'
									+ ctr
									+ '" class="possibleValuesJson" style="display:none">'
									+ JSON.stringify(settingDetail.possibleValues)
									+ '</div><div id="possibleValuesFavoriteJson'
									+ ctr
									+ '" class="possibleValuesFavoriteJson" style="display:none">'
									+ JSON.stringify(settingDetail.possibleValuesFavorite)
									+ '</div>';

							newRow = newRow
									+ "<span><a href='javascript: openManageMethods();'>"
									+ manageMethodLabel + "</a></span>";
							newRow = newRow + "</div>";

						} else if (settingDetail.variable.cvTermId == locationId) {
							// show favorite location
							newRow = newRow
									+ "<div class='possibleValuesDiv'><input type='checkbox' id='"
									+ name
									+ ctr
									+ ".favorite1'"
									+ " name='"
									+ name
									+ "["
									+ ctr
									+ "].favorite'"
									+ " onclick='javascript: toggleLocationDropdown("
									+ ctr
									+ ");' />"
									+ "<input type='hidden' name='_"
									+ name
									+ "["
									+ ctr
									+ "].favorite' value='on' /> "
									+ "<span>&nbsp;&nbsp;"
									+ showFavoriteLocationLabel
									+ "</span></div>"
									+ "<div id='possibleValuesJson"
									+ ctr
									+ "' class='possibleValuesJson' style='display:none'>"
									+ JSON.stringify(settingDetail.possibleValues)
									+ "</div><div id='possibleValuesFavoriteJson"
									+ ctr
									+ "' class='possibleValuesFavoriteJson' style='display:none'>"
									+ JSON.stringify(settingDetail.possibleValuesFavorite)
									+ "</div>";

							newRow = newRow
									+ '<span><a href="javascript: openManageLocations();">'
									+ manageLocationLabel + '</a></span>';
							newRow = newRow + '</div>';

						} else {
							newRow = newRow + '<div id="possibleValuesJson'
									+ posValSuffix + ctr
									+ '" class="possibleValuesJson'
									+ posValSuffix + '" style="display:none">'
									+ JSON.stringify(settingDetail.possibleValues)
									+ '</div></div>';
						}

						$("#" + tableId).append(newRow);

						if (settingDetail.variable.widgetType === 'DROPDOWN') {
							// initialize select 2 combo
							if (settingDetail.variable.cvTermId == locationId) {
								initializePossibleValuesCombo(
										settingDetail.possibleValues, '#'
												+ getJquerySafeId(name + ctr
														+ '.value'), true, null);
							} else {
								initializePossibleValuesCombo(
										settingDetail.possibleValues, '#'
												+ getJquerySafeId(name + ctr
														+ '.value'), false,
										null);
							}
						}
						ctr++;
					});

	initializeDateAndSliderInputs();
}

function toggleDropdownGen(comboId, favoriteCheckId, suffix, isLocation) {
	var possibleValues;
	var showFavorite = $('#'+favoriteCheckId).is(':checked');
	var selectedVal = '';

	// get previously selected value
	if ($('#'+comboId).select2('data')) {
		selectedVal = $('#'+comboId).select2('data').id;
	}

	// reset select2 combo
	initializePossibleValuesCombo([], '#' + comboId, isLocation, null);

	// get possible values based on checkbox
	if (showFavorite) {
		possibleValues = $('#possibleValuesFavoriteJson' + suffix).text();
		$($('#' + comboId).parent().find('.selectedValue')).val(selectedVal);
		selectedVal = $($('#' + comboId).parent().find('.selectedValueFave')).val();
	} else {
		possibleValues = $('#possibleValuesJson' + suffix).text();
		$($('#' + comboId).parent().find('.selectedValueFave')).val(selectedVal);
		selectedVal = $($('#' + comboId).parent().find('.selectedValue')).val();
	}

	// recreate select2 combo
	initializePossibleValuesCombo($.parseJSON(possibleValues), '#' + comboId,
			showFavorite ? false : isLocation, selectedVal);

}

function toggleMethodDropdown(rowIndex) {
	var possibleValues;
	var showFavorite = $(
			"#"
					+ getJquerySafeId("studyLevelVariables" + rowIndex
							+ ".favorite1")).is(":checked");
	var selectedVal = "";

	// get previously selected value
	if ($("#" + getJquerySafeId("studyLevelVariables" + rowIndex + ".value"))
			.select2("data")) {
		selectedVal = $(
				"#"
						+ getJquerySafeId("studyLevelVariables" + rowIndex
								+ ".value")).select2("data").id;
	}

	// reset select2 combo
	initializePossibleValuesCombo([], "#"
			+ getJquerySafeId("studyLevelVariables" + rowIndex + ".value"),
			false, null);

	// get possible values based on checkbox
	if (showFavorite) {
		possibleValues = $("#possibleValuesFavoriteJson" + rowIndex).text();

		$(
				$(
						"#"
								+ getJquerySafeId("studyLevelVariables"
										+ rowIndex + ".value")).parent().find(
						".selectedValue")).val(selectedVal);
		selectedVal = $(
				$(
						"#"
								+ getJquerySafeId("studyLevelVariables"
										+ rowIndex + ".value")).parent().find(
						".selectedValueFave")).val();
	} else {
		possibleValues = $("#possibleValuesJson" + rowIndex).text();
		$(
				$(
						"#"
								+ getJquerySafeId("studyLevelVariables"
										+ rowIndex + ".value")).parent().find(
						".selectedValueFave")).val(selectedVal);
		selectedVal = $(
				$(
						"#"
								+ getJquerySafeId("studyLevelVariables"
										+ rowIndex + ".value")).parent().find(
						".selectedValue")).val();
	}
	// recreate select2 combo
	initializePossibleValuesCombo($.parseJSON(possibleValues), "#"
			+ getJquerySafeId("studyLevelVariables" + rowIndex + ".value"),
			false, selectedVal);
}

function toggleLocationDropdown(rowIndex) {
	var possibleValues;
	var showFavorite = $("#" + getJquerySafeId("studyLevelVariables" + rowIndex
							+ ".favorite1")).is(":checked");
	var selectedVal = "";
	var showAll = true;

	// get previously selected value
	if ($("#" + getJquerySafeId("studyLevelVariables" + rowIndex + ".value"))
			.select2("data")) {
		selectedVal = $(
				"#"
						+ getJquerySafeId("studyLevelVariables" + rowIndex
								+ ".value")).select2("data").id;
	}

	// reset select2 combo
	initializePossibleValuesCombo([], "#"
			+ getJquerySafeId("studyLevelVariables" + rowIndex + ".value"),
			false, null);

	// get possible values based on checkbox
    if (showFavorite) {
        possibleValues = $("#possibleValuesFavoriteJson" + rowIndex).text();
        showAll = false;
        $(
            $(
                    "#"
                    + getJquerySafeId("studyLevelVariables"
                    + rowIndex + ".value")).parent().find(
                ".selectedValue")).val(selectedVal);
        selectedVal = $(
            $(
                    "#"
                    + getJquerySafeId("studyLevelVariables"
                    + rowIndex + ".value")).parent().find(
                ".selectedValueFave")).val();
    } else {
        possibleValues = $("#possibleValuesJson" + rowIndex).text();
        $(
            $(
                    "#"
                    + getJquerySafeId("studyLevelVariables"
                    + rowIndex + ".value")).parent().find(
                ".selectedValueFave")).val(selectedVal);
        selectedVal = $(
            $(
                    "#"
                    + getJquerySafeId("studyLevelVariables"
                    + rowIndex + ".value")).parent().find(
                ".selectedValue")).val();
    }

    // recreate select2 combo
    initializePossibleValuesCombo($.parseJSON(possibleValues), "#"
            + getJquerySafeId("studyLevelVariables" + rowIndex + ".value"),
        showAll, selectedVal);
}

function createTableSettingVariables(data, name, tableId, varType) {
	$
			.each(
					data,
					function(index, settingDetail) {
						var length = $("#" + tableId + " tbody tr").length+1;
						var className = length % 2 == 1 ? 'even' : 'odd';
						var rowClass = "";
						if (varType == 3) {
							rowClass = "baseline-traits";
						}
						var newRow = "<tr class='newVariable " + rowClass
								+ "'>";
						var isDelete = "";

						if (settingDetail.deletable) {
							isDelete = "<span style='font-size: 16px;' class='delete-icon' onclick='deleteVariable("
									+ varType
									+ ","
									+ settingDetail.variable.cvTermId
									+ ",$(this))'></span>";
						}
						newRow = newRow
								+ "<td style='text-align: center' class='fbk-delete-link "
								+ className + "'>" + isDelete
								+ "<input class='cvTermIds' type='hidden' id='"
								+ name + (length - 1)
								+ ".variable.cvTermId' name='" + name + "["
								+ (length - 1) + "].variable.cvTermId' value='"
								+ settingDetail.variable.cvTermId + "' />"
								+ "</td>";
						newRow = newRow
								+ "<td width='45%' class='"
								+ className
								+ "'><a href='javascript: void(0);' onclick='javascript:showBaselineTraitDetailsModal("
								+ settingDetail.variable.cvTermId
								+ ");' ><span class='var-names'>" + settingDetail.variable.name
								+ "</span></a></td>";
						newRow = newRow + "<td width='50%' class='" + className
								+ "'>" + settingDetail.variable.description
								+ "</td></tr>";
						$("#" + tableId).append(newRow);
					});
}

function sortByKey(array, key) {
	return array.sort(function(a, b) {
		var x = a[key].toLowerCase();
		var y = b[key].toLowerCase();
		return ((x < y) ? -1 : ((x > y) ? 1 : 0));
	});
}

function initializePossibleValuesCombo(possibleValues, name, isLocation,
		defaultValue) {
	var possibleValues_obj = [],
		counter = 0,
		newPossibleValues = [];
	var defaultJsonVal = null;
	var isBreedingMethodSetting = $(name).parent().next().children(".breeding-method-tooltip").length > 0;

	$.each(
					possibleValues,
					function(index, value) {
						var jsonVal;
						if (isBreedingMethodSetting && value.id !== undefined) {
							jsonVal = {
									'id' : value.key,
									'text' : value.description,
									'description' : value.name
								};
						}
						else if (value.id !== undefined) {
							jsonVal = {
								'id' : value.key,
								'text' : value.description
							};
						} else if (value.locid !== undefined) {
							jsonVal = {
								'id' : value.locid,
								'text' : value.lname
							};
						} else {
							jsonVal = {
								'id' : value.mid,
								'text' : value.mname + (value.mcode !== undefined ? ' - ' + value.mcode : ''),
								'description': value.mdesc
							};
						}

						possibleValues_obj.push(jsonVal);
						if (defaultValue !== null
								&& defaultValue !== ''
								&& ((defaultValue === value.key
										|| defaultValue == value.locid || defaultValue == value.mid) || (defaultValue == value.name
										|| defaultValue == value.lname || defaultValue == value.mname))) {
							defaultJsonVal = jsonVal;
						}

					});

	// possibleValues_obj = sortByKey(possibleValues_obj, "text");

	if (isLocation) {
		$(name).select2(
				{ 	minimumResultsForSearch: (possibleValues_obj != null && possibleValues_obj.length != 0) ? 20 : -1,
					query : function(query) {
						var data = {
							results : possibleValues_obj
						};
						// return the array that matches
						data.results = $.grep(data.results, function(item,
								index) {
							return ($.fn.select2.defaults.matcher(query.term,
									item.text));

						});
						query.callback(data);
					}
				});
	} else if ($(name).parent().next().children(".breeding-method-tooltip").length > 0) {
		$(name).select2(
				{
					minimumResultsForSearch: (possibleValues_obj != null && possibleValues_obj.length != 0) ? 20 : -1,
					query : function(query) {
						var data = {
							results : possibleValues_obj
						};
						// return the array that matches
						data.results = $.grep(data.results, function(item,
								index) {
							return ($.fn.select2.defaults.matcher(query.term,
									item.text));

						});
						/*
						 * if (data.results.length === 0){
						 * data.results.unshift({id:query.term,text:query.term}); }
						 */
						query.callback(data);
					}
				}).on('change', function() {
					if ($('.breeding-method-tooltip')) {
						$('.breeding-method-tooltip').attr('title', $(name).select2('data').description);
						$('.help-tooltip-nursery').tooltip('destroy');
						$('.help-tooltip-nursery').tooltip();
					}
				});
	} else {
		var minResults = (possibleValues_obj.length > 0) ? 20 : -1;

		$(name).select2(
				{
					minimumResultsForSearch: minResults,
					query : function(query) {
						var data = {
							results : possibleValues_obj
						};
						// return the array that matches
						data.results = $.grep(data.results, function(item,
								index) {
							return ($.fn.select2.defaults.matcher(query.term,
									item.text));

						});
						/*
						 * if (data.results.length === 0){
						 * data.results.unshift({id:query.term,text:query.term}); }
						 */
						query.callback(data);
					}
				});
	}
	if (defaultJsonVal != null) {
		$(name).select2('data', defaultJsonVal).trigger('change');
	}
}

function checkMeasurementData(variableType, variableId) {
	var hasData = "0";
	$.ajax({
		url : "/Fieldbook/NurseryManager/editNursery/checkMeasurementData/"
				+ variableType + "/" + variableId,
		cache : false,
		type : "GET",
		async : false,
		success : function(data) {
			hasData = data.hasMeasurementData;
		}
	});
	return hasData;
}

function deleteVariable(variableType, variableId, deleteButton) {
	var hasMeasurementData = false;
	if (variableType == selectionVariatesSegment
			|| variableType == baselineTraitsSegment) {
		hasMeasurementData = checkMeasurementData(variableType, variableId);
	}

	// if no data for measurement rows is saved yet, proceed with delete
	if (hasMeasurementData == "0") {
		// remove row from UI
		deleteButton.parent().parent().remove();
		checkShowSettingsFormReminder();
		// remove row from session
		$.ajax({
			url : "/Fieldbook/NurseryManager/createNursery/deleteVariable/"
					+ variableType + "/" + variableId,
			cache : false,
			type : "POST",
			success : function() {
			}
		});

		// add dummy row to selection variates/traits if no record is left
		if (variableType === 3 && $('#baselineTraitSettings tbody tr').length === 0) {
			showDummyRow('baselineTraitSettings');
		} else if (variableType === 6 && $('#selectionVariatesSettings tbody tr').length === 0) {
			showDummyRow('selectionVariatesSettings');
		}

		// reinstantiate counters of ids and names
		sortVariableIdsAndNames(variableType);
		inputChange = true;

		return true;
	} else {
		// show confirmation popup
		$("#variateDeleteConfirmationModal").modal({
			backdrop : 'static',
			keyboard : false
		});
		$("#varToDelete").val(variableId);
		$("#variableType").val(variableType);
		buttonToDelete = deleteButton;

		return false;
	}
}

function proceedWithDelete() {
	var variableId = $("#varToDelete").val();
	var variableType = $("#variableType").val();
	var deleteButton = buttonToDelete;

	// remove row from UI
	deleteButton.parent().parent().remove();

	// remove row from session
	$.ajax({
		url : "/Fieldbook/NurseryManager/createNursery/deleteVariable/"
				+ variableType + "/" + variableId,
		cache : false,
		type : "POST",
		success : function() {
		}
	});
	// add dummy row to selection variates/traits if no record is left
	if (variableType === 3 && $('#baselineTraitSettings tbody tr').length === 0) {
		showDummyRow('baselineTraitSettings');
	} else if (variableType === 6 && $('#selectionVariatesSettings tbody tr').length === 0) {
		showDummyRow('selectionVariatesSettings');
	}

	// reinstantiate counters of ids and names
	sortVariableIdsAndNames(variableType);
	inputChange = true;
}

function recreateDynamicFieldsAfterDelete(name, tableId, rowClass, posValSuffix) {
	var reg = new RegExp(name + "[0-9]+", "g");
	var reg2 = new RegExp(name + "\[[0-9]+\]", "g");
	var breedingMethodIndex = getBreedingMethodRowIndex();
	$
			.each(
					$("." + rowClass),
					function(index, row) {
						if (index >= breedingMethodIndex && name === 'studyLevelVariables') {
							index++;
						}
						// get currently selected value of select2 dropdown
						var selectedVal = null;
						var oldSelect2 = row.innerHTML.match(reg)[0];

						if ($("#" + getJquerySafeId(oldSelect2 + ".value"))
								.select2("data")
								&& row.innerHTML.indexOf("select2") > -1) {
							selectedVal = $(
									"#"
											+ getJquerySafeId(oldSelect2
													+ ".value"))
									.select2("data").id;
						} else {
							selectedVal = $(
									"#"
											+ getJquerySafeId(oldSelect2
													+ ".value")).val();
						}
						// if dropdown is for location or method, check if show
						// favorite is checked
						var isFavoriteChecked = "";
						if ($("#" + getJquerySafeId(oldSelect2 + ".favorite1")).length != 0) {
							isFavoriteChecked = $(
									"#"
											+ getJquerySafeId(oldSelect2
													+ ".favorite1")).is(
									":checked");
						}

						// change the ids and names of the objects
						row.innerHTML = row.innerHTML
								.replace(reg, name + index);
						row.innerHTML = row.innerHTML.replace(reg2, name + "["
								+ index + "]");

						// delete the existing select2 object and recreate the
						// select2 combo and checkbox/links for location/method
						if (row.innerHTML.indexOf("select2") > -1) {
							recreateSelect2Combo(index, row, selectedVal,
									isFavoriteChecked, name, posValSuffix);
						} else if (row.innerHTML.indexOf("spinner-input") > -1) {
							recreateSpinnerInput(index, row, selectedVal, name);
						} else if (row.innerHTML.indexOf("date-input") > -1) {
							recreateDateInput(index, row, selectedVal, name);
						} else {
							$('#'+getJquerySafeId(name+index+'.value')).val(selectedVal);
						}
					});
	initializeDateAndSliderInputs();
}

function resetIdsOfTables(name, tableId) {
	var reg = new RegExp(name + "[0-9]+", "g");
	var reg2 = new RegExp(name + "\[[0-9]+\]", "g");
	$.each($("#" + tableId + " tbody tr"), function(index, row) {
		row.innerHTML = row.innerHTML.replace(reg, name + index);
		row.innerHTML = row.innerHTML.replace(reg2, name + "[" + index + "]");
	});
}

function sortVariableIdsAndNames(variableType) {
	switch (variableType) {
	case 1:
		recreateDynamicFieldsAfterDelete("studyLevelVariables",
				"nurseryLevelSettings-dev", "nurseryLevelSettings", "");
		break;
	case 2:
		resetIdsOfTables("plotLevelVariables", "plotLevelSettings");
		break;
	case 3:
		resetIdsOfTables("baselineTraitVariables", "baselineTraitSettings");
		checkTraitsAndSelectionVariateTable('', false);
		break;
	case 6:
		resetIdsOfTables("selectionVariatesVariables",
				"selectionVariatesSettings");
		checkTraitsAndSelectionVariateTable('', false);
		break;
	case 7:
		recreateDynamicFieldsAfterDelete("nurseryConditions",
				"nurseryConditionsSettings", "nurseryConditionsSettings",
				"Cons");
		break;
	default:

	}
}

function recreateDateInput(index, row, selectedVal, name) {
	'use strict';
	var newCell = "<input placeholder='yyyy-mm-dd' type='text' id='" + name + index + ".value' name='"
			+ name + "[" + index + "].value' " + "value='" + selectedVal
			+ "' class='form-control date-input' />";
	newCell += '<label for="'
			+ name
			+ index
			+ '.value" class="btn datepicker"><img  src="/Fieldbook/static/img/calendar.png" style="padding-bottom:3px;" /></label>';

	$($(row).find(".2nd")).html(newCell);
}

function recreateSpinnerInput(index, row, selectedVal, name) {
	var newCell = "<input  maxlength='250'  type='text' id='"
			+ name
			+ index
			+ ".value' name='"
			+ name
			+ "["
			+ index
			+ "].value' "
			+ "data-min='"
			+ $($(row).find(".2nd").children("input.spinner-input"))
					.data("min")
			+ "' data-max='"
			+ $($(row).find(".2nd").children("input.spinner-input"))
					.data("max")
			+ "' data-step='"
			+ $($(row).find(".2nd").children("input.spinner-input")).data(
					"step") + "' value='" + selectedVal
			+ "' class='form-control spinner-input spinnerElement' />";

	$($(row).find(".2nd")).html(newCell);
}

function recreateSelect2Combo(index, row, selectedVal, isFavoriteChecked, name,
		posValSuffix) {
	// get the possible values of the variable
	var possibleValuesJson = $(
			$(row).find(".possibleValuesJson" + posValSuffix)).text();
	var possibleValuesFavoriteJson = $(
			$(row).find(".possibleValuesFavoriteJson" + posValSuffix)).text();
	var cvTermId = $(
			$(row).find('.1st').find(
					"#" + getJquerySafeId(name + index + ".variable.cvTermId")))
			.val();

	// hidden field for select2
	var newCell = "<input type='hidden' id='" + name + index + ".value' name='"
			+ name + "[" + index + "].value' class='form-control select2' />";

	// div containing the possible values
	newCell = newCell + "<div id='possibleValuesJson" + posValSuffix + index
			+ "' class='possibleValuesJson" + posValSuffix
			+ "' style='display:none'>" + possibleValuesJson + "</div>";

	// div containing the favorite possible values
	if (possibleValuesFavoriteJson !== "") {
		newCell = newCell + "<div id='possibleValuesFavoriteJson"
				+ posValSuffix + index + "' class='possibleValuesFavoriteJson"
				+ posValSuffix + "' style='display:none'>"
				+ possibleValuesFavoriteJson + "</div>";
	}

	// div containing checkbox and label for location and method
	var methodName = "toggleMethodDropdown";
	var favoriteLabel = showFavoriteMethodLabel;
	var managePopupLabel = manageMethodLabel;
	var manageMethodName = "openManageMethods";
	var isChecked = "";
	var showAll = true;

	// set possibleValues to favorite possible values
	if (isFavoriteChecked) {
		possibleValuesJson = possibleValuesFavoriteJson;
		isChecked = "checked='checked'";
		showAll = false;
	}

	// set values for location
	if (parseInt(cvTermId, 10) === parseInt(locationId, 10)) {
		methodName = "toggleLocationDropdown";
		favoriteLabel = showFavoriteLocationLabel;
		managePopupLabel = manageLocationLabel;
		manageMethodName = "openManageLocations";
	}

	// add checkbox and manage location/method links
	if (parseInt(cvTermId, 10) === parseInt(breedingMethodId, 10)
			|| parseInt(cvTermId, 10) === parseInt($('#breedingMethodCode').val(), 10)
			|| parseInt(cvTermId, 10) === parseInt(locationId, 10)) {
		newCell = newCell
				+ "<div class='possibleValuesDiv'><input type='checkbox' id='"
				+ name + index + ".favorite1'" + " name='" + name + "[" + index
				+ "].favorite'" + " onclick='javascript: " + methodName + "("
				+ index + ");' " + isChecked + " />"
				+ "<input type='hidden' name='_" + name + "[" + index
				+ "].favorite' value='on' /> " + "<span>&nbsp;&nbsp;"
				+ favoriteLabel + "</span></div>";

		newCell = newCell + "<span><a href='javascript: " + manageMethodName
				+ "();'>" + managePopupLabel + "</a></span>";
	}

	$($(row).find(".2nd")).html(newCell);

	// recreate the select2 object
	if (parseInt(cvTermId, 10) === parseInt(locationId, 10)) {
		initializePossibleValuesCombo($.parseJSON(possibleValuesJson), "#"
				+ getJquerySafeId(name + index + ".value"), showAll,
				selectedVal);
	} else {
		initializePossibleValuesCombo($.parseJSON(possibleValuesJson), "#"
				+ getJquerySafeId(name + index + ".value"), false, selectedVal);
	}
}

function hideDeleteConfirmation() {
	$('#delete-settings-confirmation').modal('hide');
}

function clearSettings() {

	$.ajax({
		url : "/Fieldbook/NurseryManager/createNursery/clearSettings",
		type : "GET",
		cache : false,
		success : function(html) {
			$("#chooseSettingsDiv").html(html);
			moveToTopScreen();

		}
	});

}

function hasEmptyNurseryValue() {
	var hasError = false;
	var name = '';
	$('.numeric-input').each(function() {
		$(this).val($.trim($(this).val()));
		if (hasError === false && $(this).val() !== '' && isNaN($(this).val())) {
			hasError = true;
			name = $(this).parent().parent().find('.control-label').html();

		}
	});
	if (hasError) {
		showInvalidInputMessage(name + " " + nurseryNumericError);
	}

	return hasError;

}

function doSaveSettings() {
	$('#settingName').val($('#settingName').val().trim());
	if ($('#settingName').val() === '') {
		showErrorMessage('page-message', templateSettingNameError);
		moveToTopScreen();
		return false;
	} else if (hasDuplicateSettingName()) {
		showErrorMessage('page-message', templateSettingNameErrorUnique);
		moveToTopScreen();
		return false;
	} else if (hasEmptyNurseryValue()) {
		// showErrorMessage('page-message', nurseryLevelValueEmpty);
		moveToTopScreen();
		return false;
	} else if (!validateStartEndDate('nurseryLevelSettings')) {
		moveToTopScreen();
		return false;
	} else {
		doAjaxMainSubmit('page-message', saveTemplateSettingSuccess, null);
		moveToTopScreen();
	}
}


function createSliderInput(ctr, minVal, maxVal, name) {
	return "<input data-slider-orientation='horizontal' data-slider-selection='after' type='text' data-min='"
			+ minVal
			+ "' data-max='"
			+ maxVal
			+ "' id='"
			+ name
			+ ctr
			+ ".value' name='"
			+ name
			+ "["
			+ ctr
			+ "].value' class='form-control numeric-input' />";
}
function createDropdownInput(ctr, name) {
	return "<input type='hidden' id='" + name + ctr + ".value' name='" + name
			+ "[" + ctr + "].value' class='form-control select2' />"
			+ "<input class='selectedValue' type='hidden' />"
			+ "<input class='selectedValueFave' type='hidden' />";
}
function createDateInput(ctr, name) {
	return "<input placeholder='yyyy-mm-dd' type='text' id='"
			+ name
			+ ctr
			+ ".value' name='"
			+ name
			+ "["
			+ ctr
			+ "].value' class='form-control date-input' />"
			+ '<label for="'
			+ name
			+ ctr
			+ '.value" class="btn datepicker"><img src="/Fieldbook/static/img/calendar.png" style="padding-bottom:3px;" /></label>';

}
function createNumericalTextInput(ctr, name) {
	return "<input  maxlength='250' type='text' id='" + name + ctr + ".value' name='" + name
			+ "[" + ctr + "].value' class='form-control numeric-input' />";
}
function createCharacterTextInput(ctr, name) {
	return "<input  maxlength='250' type='text' id='" + name + ctr + ".value' name='" + name
			+ "[" + ctr + "].value' class='form-control character-input' />";

}

function initializeDateAndSliderInputs() {
	if ($('.date-input').length > 0) {
		$('.date-input').each(function() {
			$(this).datepicker({
				'format' : 'yyyy-mm-dd'
			}).on('changeDate', function(ev) {
				$(this).datepicker('hide');
			}).on("change", function (e) {
				var curDate = $(this).val();
				try {
					var r = $.datepicker.parseDate("yy-mm-dd", curDate);
					$(this).datepicker('setDate', r);
				} catch(e) {
					$(this).datepicker('setDate', new Date());
				}
			});
		});
	}
	if ($('.datepicker img').length > 0) {
		$('.datepicker img').on('click', function() {
			$(this).parent().parent().find('.date-input').datepicker('show');
		});
	}
	if ($('.spinner-input').length > 0) {

		$('.spinner-input').each(
				function() {
					var currentVal = $(this).val() == '' ? parseFloat($(this)
							.data('min')) : parseFloat($(this).val());
					$(this).spinedit({
						minimum : parseFloat($(this).data('min')),
						maximum : parseFloat($(this).data('max')),
						step : parseFloat($(this).data('step')),
						value : currentVal,
						numberOfDecimals : 4
					});
				});
	}
}

function loadNurserySettingsForCreate(templateSettingsId) {
	var $form = $("#createNurseryForm");

	var serializedData = $form.serialize();

	$.ajax({
		url : "/Fieldbook/NurseryManager/createNursery/view/"
				+ templateSettingsId,
		type : "POST",
		data : serializedData,
		cache : false,
		timeout : 70000,
		success : function(html) {
			$("#chooseSettingsDiv").html(html);
		},
		error : function(jqXHR, textStatus, errorThrown) {
			console.log("The following error occured: " + textStatus,
					errorThrown);
		},
		complete : function() {
		}
	});
}

function openUsePreviousNurseryModal() {
	$("#selectedNursery").select2("destroy");
	$("#selectedNursery").val("");
	$("#selectedNursery").select2({minimumResultsForSearch: 20});
	$("#usePreviousNurseryModal").modal("show");
}

function choosePreviousNursery(studyId) {

	if ($("#chooseSettingsDiv").length !== 0) {
		url = "/Fieldbook/NurseryManager/createNursery/nursery/";
	}


	$.ajax({
		url : url + studyId,
		type : "GET",
		cache : false,
		data : "",
		success : function(html) {
			if ($("#chooseSettingsDiv").length !== 0) {
				$("#chooseSettingsDiv").html(html);
			} else {
				$('.container .row').first().html(html);
			}

		}
	});
}
function isStudyNameUnique() {
	'use strict';
	var studyId = '0';
	if ($('#createNurseryMainForm #studyId').length !== 0){
		studyId = $('#createNurseryMainForm #studyId').val();
		// we dont need to call the is name unique again since its not editable
		// anymore in edit
		return true;
	}

	var studyName = $.trim($('#' + getJquerySafeId('basicDetails0.value')).val());

	$('#' + getJquerySafeId('basicDetails0.value')).val(studyName);

	var isUnique = true;
	$.ajax({
		url : "/Fieldbook/StudyTreeManager/isNameUnique",
		type : "POST",
		data : "studyId=" + studyId + "&name=" + studyName,
		cache : false,
		async : false,
		success : function(data) {
			if (data.isSuccess == 1) {
				isUnique = true;
			} else {
				isUnique = false;
			}
		}
	});
	return isUnique;
}
function validateCreateNursery() {
	var hasError = false
		,name = ''
		,customMessage = ''
		,studyNameId = $('#studyNameTermId').val();

	$('.nurseryLevelVariableIdClass').each(function() {
		if (studyNameId == $(this).val()) {
			studyBookName = $(this).parent().find('.form-control').val();
		}
	});

	var startDate = $('#' + getJquerySafeId('basicDetails.value2')).val();
	if($.trim($('#' + getJquerySafeId('basicDetails0.value')).val()) === ''){
		hasError = true;
		name = 'Name';
	}else if($.trim($('#' + getJquerySafeId('basicDetails1.value')).val()) === ''){
		hasError = true;
		name = 'Description';
	}else if (isStudyNameUnique() === false) {
		hasError = true;
		customMessage = "Name should be unique";
	} else if ($('#folderId').val() === '') {
		hasError = true;
		name = $('#folderLabel').text();
	} else if (startDate === '') {
		// validate creation date
		hasError = true;
		name = 'Creation Date';
	} else if ($('#checkId').val() === '') {
		hasError = true;
		customMessage = checkTypeIsRequired;
	} else if ($('.check-germplasm-list-items tbody tr').length > 0 && $('.germplasm-list-items tbody tr').length === 0){
		hasError = true;
		customMessage = nurseryGermplasmListIsRequired;
	}

	if (hasError) {
		var errMsg = '';
		if (name !== '')
			errMsg = name.replace('*', '').replace(':', '') + ' ' + nurseryFieldsIsRequired;
		if (customMessage !== '') {
			errMsg = customMessage;
		}

		showInvalidInputMessage(errMsg);
		return false;
	}

	var valid = validateStartEndDateBasic();

	if (!valid) {
		return false;
	}

	$.each($('.numeric-input'), function(index, textField) {
		if (isNaN($(textField).val())) {
			hasError = true;
			if($(this).parent().prev().hasClass('variable-tooltip')) {
				name = $(this).parent().prev().data('original-title');
			}else {
				name = $(this).parent().prev().find('.control-label').html();
			}
			customMessage = name + " " + valueNotNumeric;
		}
	});

	$.each($('.numeric-input'), function(index, textField) {
		if (parseFloat($(textField).val()) > $(textField).data('max')
				|| parseFloat($(textField).val()) < $(textField).data('min')) {
			hasError = true;
			if($(this).parent().prev().hasClass('variable-tooltip')) {
				name = $(this).parent().prev().data('original-title');
			}else {
				name = $(this).parent().prev().find('.control-label').html();
			}
			customMessage = name + ' ' + valueNotWIMinMax + ' '
					+ $(textField).data('min') + ' to '
					+ $(textField).data('max');
		}
	});

	if (hasError) {
		showInvalidInputMessage(customMessage.replace('*', '')
				.replace(':', ''));
		return false;
	}
	/*
	 * Validate Position is less than the total germplasm Validate the Interval
	 * should be less than the total germplasm
	 */
	if($('.check-germplasm-list-items tbody tr').length != 0 && selectedCheckListDataTable !== null && selectedCheckListDataTable.getDataTable() !== null){

		selectedCheckListDataTable.getDataTable().$('.check-hidden').serialize();

		if (selectedCheckListDataTable.getDataTable().$('.check-hidden').length > 0) {

			// we validate only if there is a check
			// we try to validate if all the check row has check
			var hasCheckError = false;
			selectedCheckListDataTable.getDataTable().$('.check-hidden').each(function(){
				  if($(this).val() === ''){
					  hasCheckError = true;
				  }
			});

			if(hasCheckError == true){
				showInvalidInputMessage(selectedCheckError);
				return false;
			}
			if (isInt($('#startIndex2').val()) === false) {
				showInvalidInputMessage(startIndexWholeNumberError);
				return false;
			}
			if (isInt($('#interval2').val()) === false) {
				showInvalidInputMessage(intervalWholeNumberError);
				return false;
			}
			var totalGermplasms = $('#totalGermplasms').val();
			if (parseInt($('#startIndex2').val(), 10) < 0
					|| parseInt($('#startIndex2').val(), 10) > totalGermplasms) {
				showInvalidInputMessage(startIndexLessGermplasmError);
				return false;
			}

			if (parseInt($('#interval2').val(), 10) < 0) {
				showInvalidInputMessage(checkIntervalGreaterThanZeroError);
				return false;
			}
			var totalNumberOfChecks = selectedCheckListDataTable.getDataTable().$('.check-hidden').length;
			if (parseInt($('#interval2').val(), 10) <= totalNumberOfChecks) {
				showInvalidInputMessage(checkIntervalError);
				return false;
			}
		}
	}

	return true;
}


function validateStartEndDateBasic() {
	var startDate = $("#" + getJquerySafeId("basicDetails.value2")).val();
	var endDate = $("#" + getJquerySafeId("basicDetails.value4")).val();

	startDate = startDate == null ? '' : startDate.replace(/-/g, "");
	endDate = endDate == null ? '' : endDate.replace(/-/g, "");

	if (startDate === '' && endDate === '')
		return true;
	else if (startDate !== '' && endDate === '') {
		return true;
	} else if (startDate === '' && endDate !== '') {
		showInvalidInputMessage(startDateRequiredError);
		return false;
	} else if (parseInt(startDate) > parseInt(endDate)) {
		showInvalidInputMessage(startDateRequiredEarlierError);
		return false;
	}
	return true;

}

function recreateModalMethodCombo(comboName, comboFaveCBoxName) {
	var selectedMethodAll = $("#methodIdAll").val();
	var selectedMethodFavorite = $("#methodIdFavorite").val();


	$.ajax({
		url : "/Fieldbook/NurseryManager/advance/nursery/getBreedingMethods",
		type : "GET",
		cache : false,
		data : "",
		async : false,
		success : function(data) {
			if (data.success == "1") {
				if (selectedMethodAll != null) {
					// recreate the select2 combos to get updated list of
					// methods
					recreateMethodComboAfterClose("methodIdAll", $
							.parseJSON(data.allNonGenerativeMethods));
					recreateMethodComboAfterClose("methodIdFavorite", $
							.parseJSON(data.favoriteNonGenerativeMethods));
					showCorrectMethodCombo();
					// set previously selected value of method
					if ($("#showFavoriteMethod").prop("checked")) {
						setComboValues(methodSuggestionsFav_obj,
								selectedMethodFavorite, "methodIdFavorite");
					} else {
						setComboValues(methodSuggestions_obj,
								selectedMethodAll, "methodIdAll");
					}
				} else {
					var selectedVal = null;
					// get index of breeding method row
					var index = getBreedingMethodRowIndex();


					if ($("#" + getJquerySafeId(comboName)).select2("data")) {
						selectedVal = $("#" + getJquerySafeId(comboName))
								.select2("data").id;
					}
					// recreate select2 of breeding method
					initializePossibleValuesCombo([], "#"
							+ getJquerySafeId(comboName), false, selectedVal);

					// update values of combo
					if ($("#" + getJquerySafeId(comboFaveCBoxName)).is(
							":checked")) {
						initializePossibleValuesCombo($
								.parseJSON(data.favoriteNonGenerativeMethods), "#"
								+ getJquerySafeId(comboName), false,
								selectedVal);
					} else {
						initializePossibleValuesCombo($
								.parseJSON(data.allNonGenerativeMethods), "#"
								+ getJquerySafeId(comboName), false,
								selectedVal);
					}

					if (index > -1) {
						replacePossibleJsonValues(data.favoriteNonGenerativeMethods,
								data.allNonGenerativeMethods, index);
					}
				}
			} else {
				showErrorMessage("page-message", data.errorMessage);
			}

		}
	});
}
function plotMethod() {
	if ($('input[type=checkbox][name=allPlotsChoice]:checked').val() == 1) {
		$("#plot-variates-section").hide();
	} else {
		$("#plot-variates-section").show();
		if ($("#plotVariateId").has("option").length === 0) {
			$('input[type=checkbox][name=allPlotsChoice]')
					.prop('checked', true);
			$('input[type=checkbox][name=allPlotsChoice]').change();
			showErrorMessage("page-advance-modal-message", noPlotVariatesError);
		}
	}
}

function refreshEditNursery() {
	$('#page-message').html('');
}

function displaySaveSuccessMessage(idDomSelector, messageToDisplay){
	'use strict';

	createSuccessNotification(successMsgHeader, messageToDisplay);

}

function recreateSessionVariables() {
	'use strict';

	$.ajax({
		url: '/Fieldbook/NurseryManager/editNursery/recreate/session/variables',
		type: 'GET',
		data: '',
		cache: false,
		success: function (html) {
			$('#measurementsDiv').html(html);
			displayEditFactorsAndGermplasmSection();			
			displaySaveSuccessMessage('page-message', saveSuccessMessage);

		}
	});
}

function displayEditFactorsAndGermplasmSection() {
	'use strict';
	if ($('#measurementDataExisting').length !== 0) {
		displayCorrespondingGermplasmSections();
		
		//enable/disable adding of factors if nursery has measurement data
		if ($('#measurementDataExisting').val() === 'true') {
			$('.nrm-var-select-open').hide();
			$.each($('#plotLevelSettings tbody tr'), function (index, row) {
				$(row).find('.delete-icon').hide();
			});
		} else {
			$('.nrm-var-select-open').show();
			$.each($('#plotLevelSettings tbody tr'), function (index, row) {
				$(row).find('.delete-icon').show();
			});
		}
	} else {
		displayCorrespondingGermplasmSections();
		if ($('#measurementDataExisting').val() === 'true') {
			$('.nrm-var-select-open').hide();		
		} else {
			$('.nrm-var-select-open').show();			
		}
	}
}

function displayCorrespondingGermplasmSections() {
	'use strict';
	if ($('#measurementDataExisting').val() === 'true') {
		$('#chooseGermplasmAndChecks').hide();
		$('.overwrite-germplasm-list').hide();
		$('.observation-exists-notif').show();
	} else if (measurementRowCount > 0) {
		$('#chooseGermplasmAndChecks').hide();
		$('.observation-exists-notif').hide();
		$('.overwrite-germplasm-list').show();
	} else {
		$('#chooseGermplasmAndChecks').show();
		$('.observation-exists-notif').hide();
		$('.overwrite-germplasm-list').hide();
	}
}

function showGermplasmDetailsSection() {
	'use strict';
	$('#chooseGermplasmAndChecks').show();
	$('.observation-exists-notif').hide();
	$('.overwrite-germplasm-list').hide();
}

// FIXME Should not be using global variables or functions
/*
 * global lastDraggedChecksList, Spinner, validateCreateNursery,
 * validateStartEndDate, moveToTopScreen
 */
/* global loadNurserySettingsForCreate, getJquerySafeId, changeBuildOption */
function refreshStudyAfterSave(studyId){
	'use strict';
	$.ajax({
		url: '/Fieldbook/NurseryManager/editNursery/'+studyId,
		type: 'GET',
		data: 'isAjax=1',
		dataType: 'html',
		cache: false,
		async: false,
		success: function(html) {
			$('.container .row:eq(0)').html(html);
			displaySaveSuccessMessage('page-message', saveSuccessMessage);

		}
	});
}

function resetDesigConfirmationFields() {
	'use strict';
	// reset dropdowns and fields
	$('#importLocationId').select2('data', null);
	$('#importMethodId').select2('data', null);
	$('#nameType').select2('data', {'id': $("#nameType option:first").val(), 'text':$("#nameType option:first").html()})
	$('#importDate').val('');
	$('#confirmation-page-message').html('');
}

function validateGermplasmInput(importDate, importLocationId, importMethodId) {
	'use strict';
	if ($('#import-action-type').val() === '2' || $('#import-action-type').val() === '1'){
		if(importDate === ''){
			showInvalidInputMessage(importDateRequired);
			return false;
		}else if(importLocationId === '' || importLocationId === null){
			showInvalidInputMessage(importLocationRequired);
			return false;
		}else if((importMethodId === '' || importMethodId === null) && $('#import-action-type').val() === '2'){
			showInvalidInputMessage(importMethodRequired);
			return false;
		}
	}
	return true;
}

function submitGermplasmAndCheck() {
	'use strict';

	$('#startIndex').val($('#startIndex2').val());
	$('#interval').val($('#interval2').val());
	$('#mannerOfInsertion').val($('#mannerOfInsertion2').val());
	$('#lastDraggedChecksList').val(lastDraggedChecksList);

	var $form = $('#germplasm-list-form'),
		serializedData = $form.serialize();
	if($('.check-germplasm-list-items tbody tr').length != 0 && selectedCheckListDataTable !== null && selectedCheckListDataTable.getDataTable() !== null){
		serializedData += "&" + selectedCheckListDataTable.getDataTable().$('.check-hidden').serialize();
	}

	$.ajax({
		url: '/Fieldbook/NurseryManager/GermplasmList/submitAll',
		type: 'POST',
		data: serializedData,
		cache: false,
		success: function(dataResponse) {
			refreshStudyAfterSave(dataResponse);

		}
	});
}
function addFakeCheckTable(){
	'use strict';
	if($('.germplasm-list-items tbody tr').length > 0 && $('.check-germplasm-list-items tbody tr').length == 0 && $('#check-germplasm-list .fake-check-germplasm-list-items tbody tr').length == 0){
		// we add the fake table
		$('.fake-check-germplasm-list-items').clone().removeClass('fbk-hide').appendTo('#check-germplasm-list');
	}else if($('.germplasm-list-items tbody tr').length === 0 && $('#check-germplasm-list .fake-check-germplasm-list-items tbody tr').length == 1){
		// we remove if there are no nursery check and the selected check is
		// fake
		$('#check-germplasm-list .fake-check-germplasm-list-items').remove();
	}
}
function checkShowSettingsFormReminder(){
	'use strict';
	// we check management details if there are entries
	if($('.nurseryLevelSettings .1st').length === 0){
		$('.management-details-section-reminder').removeClass('fbk-hide');
	}else{
		$('.management-details-section-reminder').addClass('fbk-hide');
	}

	if($('.nurseryConditionsSettings .1st').length === 0){
		$('.constants-section-reminder').removeClass('fbk-hide');
	}else{
		$('.constants-section-reminder').addClass('fbk-hide');
	}
}
function discardImportedData(){
	$('#discardImportDataConfirmation').modal({
		backdrop : 'static',
		keyboard : true
	});
}
