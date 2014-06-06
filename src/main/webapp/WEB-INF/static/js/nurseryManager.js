/*global getJquerySafeId, showErrorMessage, oldLineSelected, changeAdvanceBreedingMethod, setCorrecMethodValues, noMethodVariatesError, oldMethodSelected, msgSamplePlotError, msgHarvestDateError, noLineVariatesError, setCorrectMethodValues, methodSuggestionsFav_obj, isInt, breedingMethodId, oldMethodSelected*/
function checkMethod() {
	'use strict';
	if ($('input[type=checkbox][name=methodChoice]:checked').val() == 1) {
		$('#methodIdFavorite').select2('enable', true);
		$('#methodIdAll').select2('enable', true);
		$('#showFavoriteMethod').prop('disabled', false);

		$("#method-variates-section").hide();
		$(".method-selection-div").find("input,select").prop("disabled", false);
		$("#methodIdAll").select2("enable", true);
		$("#methodIdFavorite").select2("enable", true);

		setCorrectMethodValues(true);
		changeAdvanceBreedingMethod();
	} else {
		$("#method-variates-section").show();
		$(".method-selection-div").find("input,select").prop("disabled", true);
		$("#methodIdAll").select2("enable", false);
		$("#methodIdFavorite").select2("enable", false);

		if ($('#namingConvention').val() != 3) {
			$('#showFavoriteMethod').prop('disabled', 'disabled');
			$('#methodIdFavorite').select2('enable', false);
			$('#methodIdAll').select2('enable', false);
		}
		oldMethodSelected = $('#' + getJquerySafeId("advanceBreedingMethodId")).val();
		$('#methodSelected').val($('#defaultMethodId').val());
		setCorrectMethodValues(false);
		// we show the bulk and lines section
		$('.bulk-section').css('display', 'block');
		$('.lines-section').css('display', 'block');
		if ($("#methodVariateId").has("option").length === 0) {
			$('input[type=checkbox][name=methodChoice]').prop('checked', true);
			$('input[type=checkbox][name=methodChoice]').change();
			showErrorMessage("page-message", noMethodVariatesError);
		}
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
			$("#methodIdFavorite").select2('data', dataVal).trigger('change');
		} else if (methodSuggestionsFav_obj.length > 0) {
			// we set the first
			$("#methodIdFavorite").select2('data', methodSuggestionsFav_obj[0])
					.trigger('change');
		} else {
			$('#' + getJquerySafeId("breedingMethodId")).val(0);
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
			showErrorMessage("page-message", noLineVariatesError);
		}
	}
}

function validateAdvanceNursery() {
	// validate number of sample per plot
	var numberOfSamplePerPlot = $('#lineSelected').val();
	if (numberOfSamplePerPlot === '') {
		showErrorMessage('page-message', msgSamplePlotError);
		return false;
	}
	if (!isInt(numberOfSamplePerPlot)) {
		showErrorMessage('page-message', msgSamplePlotError);
		return false;
	}
	if (Number(numberOfSamplePerPlot) < 1
			|| Number(numberOfSamplePerPlot) > 1000) {
		showErrorMessage('page-message', msgSamplePlotError);
		return false;
	}
	if ($('#harvestDate').val() === '') {
		showErrorMessage('page-message', msgHarvestDateError);
		return false;
	}

	if ($('#namingConvention').val() === '3' && $('#breedingMethodId').val() === '0') {
		showErrorMessage('page-message', msgMethodError);
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
	var rowIndex = 0;
	$.each($(".nurseryLevelSettings"), function(index, row) {
		var cvTermId = $(
				$(row).find('.1st').find("#"+ getJquerySafeId("studyLevelVariables" + index + ".variable.cvTermId"))).val();
		if (parseInt(cvTermId, 10) == parseInt(breedingMethodId, 10)) {
			rowIndex = index;
		}
	});
	return rowIndex;
}

function getLocationRowIndex() {
	var rowIndex = 0;
	$.each($(".nurseryLevelSettings"), function(index, row) {
		var cvTermId = $($(row).find('.1st').find("#" + getJquerySafeId("studyLevelVariables" + index + ".variable.cvTermId"))).val();
		if (parseInt(cvTermId, 10) == parseInt(locationId, 10)) {
			rowIndex = index;
		}
	});
	return rowIndex;
}

function recreateLocationCombo() {
	var selectedLocationAll = $("#harvestLocationIdAll").val();
	var selectedLocationFavorite = $("#harvestLocationIdFavorite").val();

	var inventoryPopup = false;
	var advancePopup = false;
	if ($('#addLotsModal').length !== 0 && $('#addLotsModal').hasClass('in')){
		inventoryPopup = true;
	}
	else if ($('#advanceNurseryModal').length !== 0 && $('#advanceNurseryModal').hasClass('in')) {
		advancePopup = true;
	}

	Spinner.toggle();
	$
			.ajax({
				url : "/Fieldbook/NurseryManager/advance/nursery/getLocations",
				type : "GET",
				cache : false,
				data : "",
				async : false,
				success : function(data) {
					if (data.success == "1") {
					if (inventoryPopup) {
							// console.log('here');
							recreateLocationComboAfterClose("inventoryMethodIdAll", $.parseJSON(data.allLocations));
							recreateLocationComboAfterClose("inventoryMethodIdFavorite", $.parseJSON(data.favoriteLocations));
							showCorrectLocationInventoryCombo();
							// set previously selected value of location
							if ($("#showFavoriteLocationInventory").prop("checked")) {
								setComboValues(locationSuggestionsFav_obj, $("#inventoryMethodIdFavorite").val(),"inventoryMethodIdFavorite");
							} else {
								setComboValues(locationSuggestions_obj, $("#inventoryMethodIdAll").val(),"inventoryMethodIdAll");
							}
						} else if (advancePopup === true
								|| selectedLocationAll != null) {
							// recreate the select2 combos to get updated list
							// of locations
							recreateLocationComboAfterClose("harvestLocationIdAll", $.parseJSON(data.allLocations));
							recreateLocationComboAfterClose("harvestLocationIdFavorite", $.parseJSON(data.favoriteLocations));
							showCorrectLocationCombo();
							// set previously selected value of location
							if ($("#showFavoriteLocation").prop("checked")) {setComboValues(locationSuggestionsFav_obj,selectedLocationFavorite,"harvestLocationIdFavorite");
							} else {
								setComboValues(locationSuggestions_obj,selectedLocationAll,"harvestLocationIdAll");
							}
						} else {
							var selectedVal = null;
							var index = getLocationRowIndex();

							if ($("#"+ getJquerySafeId("studyLevelVariables"+ index + ".value")).select2("data")) {
								selectedVal = $("#"+ getJquerySafeId("studyLevelVariables" + index + ".value")).select2("data").id;
							}
							initializePossibleValuesCombo([], "#" + getJquerySafeId("studyLevelVariables" + index + ".value"), true, selectedVal);

							// update values in combo
							if ($("#"+ getJquerySafeId("studyLevelVariables" + index + ".favorite1")).is(":checked")) {
								initializePossibleValuesCombo($.parseJSON(data.favoriteLocations), "#" + getJquerySafeId("studyLevelVariables" + index + ".value"), false, selectedVal);
							} else {
								initializePossibleValuesCombo($.parseJSON(data.allLocations), "#" + getJquerySafeId("studyLevelVariables" + index + ".value"), true, selectedVal);
							}

							replacePossibleJsonValues(data.favoriteLocations, data.allLocations, index);
						}
					} else {
						showErrorMessage("page-message", data.errorMessage);
					}
				},
				error : function(jqXHR, textStatus, errorThrown) {
					console.log("The following error occured: " + textStatus,
							errorThrown);
				},
				complete : function() {
					Spinner.toggle();
				}
			});
}

function replacePossibleJsonValues(favoriteJson, allJson, index) {
	$("#possibleValuesJson" + index).text(allJson);
	$("#possibleValuesFavoriteJson" + index).text(favoriteJson);
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

function openAddVariablesSetting(variableType) {
	// change heading of popup based on clicked link
	$('#ontology-detail-tabs').empty().html($('.variable-detail-info').html());
	// this would reset the tree view

	$("#variable-details").html('');
	switch (parseInt(variableType)) {
	case 1:
		$("#heading-modal").text(addNurseryLevelSettings);
		$('#reminder-placeholder').html(reminderNursery);
		break;
	case 2:
		$("#heading-modal").text(addPlotLevelSettings);
		$('#reminder-placeholder').html(reminderPlot);
		break;
	case 3:
		$("#heading-modal").text(addBaselineTraits);
		$('#reminder-placeholder').html(reminderTraits);
		break;
	case 6:
		$("#heading-modal").text(addSelectionVariates);
		$('#reminder-placeholder').html(reminderSelectionVariates);
		break;
	case 7:
		$("#heading-modal").text(addNurseryConditions);
		$('#reminder-placeholder').html(reminderNurseryCondtions);
		break;
	default:
		$("#heading-modal").text(addNurseryLevelSettings);
		$('#reminder-placeholder').html(reminderNursery);
	}
	getStandardVariables(variableType);

}

function getStandardVariables(variableType) {
	Spinner.toggle();
	$.ajax({
		url : "/Fieldbook/NurseryManager/createNursery/displayAddSetting/"
				+ variableType,
		type : "GET",
		cache : false,
		success : function(data) {
			if (treeData != null) {
				$("#" + treeDivId).dynatree("destroy");
			}
			treeData = data.treeData;
			searchTreeData = data.searchTreeData;
			displayOntologyTree(treeDivId, treeData, searchTreeData,
					'srch-term');
			$('#' + 'srch-term').val('');
			
			// clear selected variables table and attribute fields
			$("#newVariablesList > tbody").empty();
			$("#page-message-modal").html("");
			clearAttributeFields();
			$("#addVariables").attr(
					"onclick",
					"javascript: submitSelectedVariables(" + variableType
							+ ");");
			$("#addVariablesSettingModal").modal({
				backdrop : 'static',
				keyboard : false
			});
			Spinner.toggle();
		}
	});
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
	if (id != '') {
		Spinner.toggle();
		$
				.ajax({
					url : "/Fieldbook/NurseryManager/createNursery/showVariableDetails/"
							+ id,
					type : "GET",
					cache : false,
					success : function(data) {
						$('#var-info').slideDown("slow");
						populateAttributeFields($.parseJSON(data));
					},
					error : function(jqXHR, textStatus, errorThrown) {
						console.log("The following error occured: "
								+ textStatus, errorThrown);
					},
					complete : function() {
						Spinner.toggle();
					}
				});
	}

}

function populateAttributeFields(data) {
	$("#selectedTraitClass").html(checkIfEmpty(data.traitClass));
	$("#selectedProperty").html(checkIfEmpty(data.property));
	$("#selectedMethod").html(checkIfEmpty(data.method));
	$("#selectedScale").html(checkIfEmpty(data.scale));
	$("#selectedDataType").html(checkIfEmpty(data.dataType));
	$("#selectedRole").html(checkIfEmpty(data.role));
	$("#selectedCropOntologyId").html(checkIfEmpty(data.cropOntologyId));
	$("#selectedStdVarId").val(data.cvTermId);
	$("#selectedName").val(data.name);
}

function checkIfEmpty(value) {
	if (value == "") {
		return "&nbsp";
	} else {
		return value;
	}
}

function clearAttributeFields() {
	$("#selectedTraitClass").html("&nbsp;");
	$("#selectedProperty").html("&nbsp;");
	$("#selectedMethod").html("&nbsp;");
	$("#selectedScale").html("&nbsp;");
	$("#selectedDataType").html("&nbsp;");
	$("#selectedRole").html("&nbsp;");
	$("#selectedCropOntologyId").html("&nbsp;");
	$("#selectedStdVarId").val("");
	$("#selectedName").val("");
}

function getIdNameCounterpart(selectedVariable, idNameCombinationVariables) {
	var inList = -1;
	// return the id or name counterpart of the selected variable if it is in
	// the combination list
	$.each(idNameCombinationVariables, function(index, item) {
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

function getIdCounterpart(selectedVariable, idNameCombinationVariables) {
	var inList = selectedVariable;
	// return the id counterpart of the variable selected if it is in the list
	$.each(idNameCombinationVariables, function(index, item) {
		if (parseInt(item.split("|")[1], 10) === parseInt(selectedVariable, 10)) {
			inList = parseInt(item.split("|")[0], 10);
			return false;
		}
	});
	return inList;
}

function idNameCounterpartSelected(selectedVariable) {
	var itemToCompare = getIdNameCounterpart(selectedVariable, $(
			"#idNameVariables").val().split(","));

	if (itemToCompare != -1) {
		// if it is selected/added already
		if (!notInList(itemToCompare)) {
			return true;
		}
	}
	return false;
}

function addVariableToList() {
	var newRow;
	var rowCount = $("#newVariablesList tbody tr").length;
	var ctr;

	// get the last counter for the selected variables and add 1
	if (rowCount === 0) {
		ctr = 0;
	} else {
		var lastVarId = $(
				"#newVariablesList tbody tr:last-child td input[type='hidden']")
				.attr("name");
		ctr = parseInt(lastVarId.substring(lastVarId.indexOf("[") + 1,
				lastVarId.indexOf("]")), 10) + 1;
	}

	var length = $("#newVariablesList tbody tr").length + 1;
	var className = length % 2 == 1 ? 'even' : 'odd';

	if (idNameCounterpartSelected($("#selectedStdVarId").val())) {
		// if selected variable is an id/name counterpart of a variable already
		// selected/added
		$("#page-message-modal").html("<div class='alert alert-danger'>" + idNameCounterpartAddedError + "</div>");
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

	} else {

		$("#page-message-modal").html(
				"<div class='alert alert-danger'>" + varInListMessage
						+ "</div>");
	}
}

function notInList(id) {
	var isNotInList = true;
	$.each($('.cvTermIds'), function() {
		if ($(this).val() == id) {
			isNotInList = false;
		}
	});
	return isNotInList;
}

function hasNoVariableName() {
	var result = false;
	$.each($("#newVariablesList tbody tr"), function(index, row) {
		if ($(
				$(row).children("td:nth-child(1)").children(
						"#"
								+ getJquerySafeId("selectedVariables" + index
										+ ".name"))).val() == "") {
			result = true;
		}
	});
	return result;
}

function submitSelectedVariables(variableType) {
	if ($("#newVariablesList tbody tr").length == 0) {
		$("#page-message-modal").html(
				"<div class='alert alert-danger'>" + noVariableAddedMessage
						+ "</div>");
	} else if ($("#newVariablesList tbody tr").length > 0
			&& hasNoVariableName()) {
		$("#page-message-modal").html(
				"<div class='alert alert-danger'>" + noVariableNameError
						+ "</div>");
	} else if ($("#newVariablesList tbody tr").length > 0) {
		replaceNameVariables();
		var serializedData = $("input.addVariables").serialize();
		$("#page-message-modal").html("");
		Spinner.toggle();

		$.ajax({
			url : "/Fieldbook/NurseryManager/createNursery/addSettings/"
					+ variableType,
			type : "POST",
			data : serializedData,
			success : function(data) {
				switch (variableType) {
				case 1:
					createDynamicSettingVariables($.parseJSON(data),
							"studyLevelVariables", "nurseryLevelSettings-dev",
							"nurseryLevelSettings", variableType, "");
					break;
				case 2:
					createTableSettingVariables($.parseJSON(data),
							"plotLevelVariables", "plotLevelSettings",
							variableType);
					break;
				case 3:
					createTableSettingVariables($.parseJSON(data),
							"baselineTraitVariables", "baselineTraitSettings",
							variableType);
					break;
				case 6:
					createTableSettingVariables($.parseJSON(data),
							"selectionVariatesVariables",
							"selectionVariatesSettings", variableType);
					break;
				case 7:
					createDynamicSettingVariables($.parseJSON(data),
							"nurseryConditions", "nurseryConditionsSettings",
							"nurseryConditionsSettings", variableType, "Cons");
					break;
				default:
					createDynamicSettingVariables($.parseJSON(data),
							"studyLevelVariables", "nurseryLevelSettings-dev",
							"nurseryLevelSettings", variableType, "");
				}

			},
			error : function(jqXHR, textStatus, errorThrown) {
				console.log("The following error occured: " + textStatus,
						errorThrown);
			},
			complete : function() {
				Spinner.toggle();
				$("#addVariablesSettingModal").modal("hide");
			}
		});
	} else {
		$("#page-message-modal").html(
				"<div class='alert alert-danger'>" + varInListMessage
						+ "</div>");
	}
}

function replaceNameVariables() {
	$.each($("#newVariablesList tbody tr"), function(index, row) {
		value = $(
				$(row).children("td:nth-child(1)").children(
						"#"
								+ getJquerySafeId("selectedVariables" + index
										+ ".cvTermId"))).val();
		// use the id counterpart of the name variable
		$(
				$(row).children("td:nth-child(1)").children(
						"#"
								+ getJquerySafeId("selectedVariables" + index
										+ ".cvTermId")))
				.val(
						getIdCounterpart(value, $("#idNameVariables").val()
								.split(",")));
	});

}

function getLastRowIndex(name, hasTBody) {
	if (hasTBody) {
		return $("#" + name + " tbody tr").length - 1;
	} else {
		return $("#" + name + " tr").length - 1;
	}
}

function createDynamicSettingVariables(data, name, tableId, rowClass, varType,
		posValSuffix) {
	var ctr = $('.' + rowClass).length; // getLastRowIndex("nurseryLevelSettings",
										// false) + 1;
	$
			.each(
					data,
					function(index, settingDetail) {
						var newRow = "<div class='row form-group " + rowClass
								+ " newVariable'>";
						var isDelete = "";

						// include delete button if variable is deletable
						if (settingDetail.deletable) {
							isDelete = "<span style='cursor: default; font-size: 16px;' class='delete-icon' onclick='deleteVariable("
									+ varType
									+ ","
									+ settingDetail.variable.cvTermId
									+ ",$(this))'></span>";
						}

						// create html elements dynamically
						newRow = newRow
								+ "<div class='col-xs-5 col-md-5 1st'>"
								+ isDelete
								+ "<input class='cvTermIds nurseryLevelVariableIdClass' type='hidden' id='"
								+ name + ctr + ".variable.cvTermId' name='"
								+ name + "[" + ctr
								+ "].variable.cvTermId' value='"
								+ settingDetail.variable.cvTermId
								+ "' />&nbsp;&nbsp;&nbsp;&nbsp;";

						newRow = newRow
								+ "<span style='word-wrap: break-word'  class='control-label label-bold study-variable-name'>"
								+ settingDetail.variable.name
								+ "</span>: &nbsp;<span class='required'>*</span></div>";

						if (settingDetail.variable.widgetType == 'DATE') {
							newRow = newRow
									+ "<div class='col-xs-4 col-md-4 2nd'>";
						} else
							newRow = newRow
									+ "<div class='col-xs-7 col-md-7 2nd'>";

						var inputHtml = '';

						if (settingDetail.variable.widgetType == 'DROPDOWN') {
							inputHtml = createDropdownInput(ctr, name);
						} else if (settingDetail.variable.widgetType == 'DATE') {
							inputHtml = createDateInput(ctr, name);
						} else if (settingDetail.variable.widgetType == 'CTEXT') {
							inputHtml = createCharacterTextInput(ctr, name);
						} else if (settingDetail.variable.widgetType == 'NTEXT') {
							inputHtml = createNumericalTextInput(ctr, name);
						} else if (settingDetail.variable.widgetType == 'SLIDER') {
							inputHtml = createSliderInput(ctr,
									settingDetail.variable.minRange,
									settingDetail.variable.maxRange, name);
						}
						newRow = newRow + inputHtml;

						if (settingDetail.variable.cvTermId == breedingMethodId) {
							// show favorite method
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
									+ " onclick='javascript: toggleMethodDropdown("
									+ ctr
									+ ");' />"
									+ "<input type='hidden' name='_"
									+ name
									+ "["
									+ ctr
									+ "].favorite' value='on' /> "
									+ "<span>&nbsp;&nbsp;"
									+ showFavoriteMethodLabel
									+ "</span></div>"
									+ "<div id='possibleValuesJson"
									+ ctr
									+ "' class='possibleValuesJson' style='display:none'>"
									+ settingDetail.possibleValuesJson
									+ "</div><div id='possibleValuesFavoriteJson"
									+ ctr
									+ "' class='possibleValuesFavoriteJson' style='display:none'>"
									+ settingDetail.possibleValuesFavoriteJson
									+ "</div>";

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
									+ settingDetail.possibleValuesJson
									+ "</div><div id='possibleValuesFavoriteJson"
									+ ctr
									+ "' class='possibleValuesFavoriteJson' style='display:none'>"
									+ settingDetail.possibleValuesFavoriteJson
									+ "</div>";

							newRow = newRow
									+ "<span><a href='javascript: openManageLocations();'>"
									+ manageLocationLabel + "</a></span>";
							newRow = newRow + "</div>";

						} else {
							newRow = newRow + "<div id='possibleValuesJson"
									+ posValSuffix + ctr
									+ "' class='possibleValuesJson"
									+ posValSuffix + "' style='display:none'>"
									+ settingDetail.possibleValuesJson
									+ "</div></div>";
						}

						$("#" + tableId).append(newRow);

						if (settingDetail.variable.widgetType == 'DROPDOWN') {
							// initialize select 2 combo
							if (settingDetail.variable.cvTermId == locationId) {
								initializePossibleValuesCombo(
										settingDetail.possibleValues, "#"
												+ getJquerySafeId(name + ctr
														+ ".value"), true, null);
							} else {
								initializePossibleValuesCombo(
										settingDetail.possibleValues, "#"
												+ getJquerySafeId(name + ctr
														+ ".value"), false,
										null);
							}
						}
						ctr++;
					});

	initializeDateAndSliderInputs();
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
	var showFavorite = $(
			"#"
					+ getJquerySafeId("studyLevelVariables" + rowIndex
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
						var length = $("#" + tableId + " tbody tr").length + 1;
						var className = length % 2 == 1 ? 'even' : 'odd';
						var rowClass = "";
						if (varType == 3) {
							rowClass = "baseline-traits";
						}
						var newRow = "<tr class='newVariable " + rowClass
								+ "'>";
						var isDelete = "";

						if (settingDetail.deletable) {
							isDelete = "<span style='cursor: default; font-size: 16px;' class='delete-icon' onclick='deleteVariable("
									+ varType
									+ ","
									+ settingDetail.variable.cvTermId
									+ ",$(this))'></span>";
						}
						newRow = newRow
								+ "<td width='5%' style='text-align: center' class='"
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
								+ ");' ><span>" + settingDetail.variable.name
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
	var possibleValues_obj = [];
	var defaultJsonVal = null;

	$.each(
					possibleValues,
					function(index, value) {
						var jsonVal;
						if (value.id !== undefined) {
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
								'text' : value.mname
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

	//possibleValues_obj = sortByKey(possibleValues_obj, "text");

	if (isLocation) {
		$(name).select2(
				{
					minimumInputLength : 2,
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
	} else {
		$(name).select2(
				{
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

		// remove row from session
		Spinner.toggle();
		$.ajax({
			url : "/Fieldbook/NurseryManager/createNursery/deleteVariable/"
					+ variableType + "/" + variableId,
			cache : false,
			type : "POST",
			success : function() {
				Spinner.toggle();
			}
		});

		// reinstantiate counters of ids and names
		sortVariableIdsAndNames(variableType);
		inputChange = true;
	} else {
		// show confirmation popup
		$("#variateDeleteConfirmationModal").modal({
			backdrop : 'static',
			keyboard : false
		});
		$("#varToDelete").val(variableId);
		$("#variableType").val(variableType);
		buttonToDelete = deleteButton;
	}
}

function proceedWithDelete() {
	var variableId = $("#varToDelete").val();
	var variableType = $("#variableType").val();
	var deleteButton = buttonToDelete;

	// remove row from UI
	deleteButton.parent().parent().remove();

	// remove row from session
	Spinner.toggle();
	$.ajax({
		url : "/Fieldbook/NurseryManager/createNursery/deleteVariable/"
				+ variableType + "/" + variableId,
		cache : false,
		type : "POST",
		success : function() {
			Spinner.toggle();
		}
	});

	// reinstantiate counters of ids and names
	sortVariableIdsAndNames(variableType);
	inputChange = true;
}

function recreateDynamicFieldsAfterDelete(name, tableId, rowClass, posValSuffix) {
	var reg = new RegExp(name + "[0-9]+", "g");
	var reg2 = new RegExp(name + "\[[0-9]+\]", "g");
	$
			.each(
					$("." + rowClass),
					function(index, row) {
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
		break;
	case 6:
		resetIdsOfTables("selectionVariatesVariables",
				"selectionVariatesSettings");
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
	var newCell = "<input type='text' id='" + name + index + ".value' name='"
			+ name + "[" + index + "].value' " + "value='" + selectedVal
			+ "' class='form-control date-input' />";
	newCell += '<label for="'
			+ name
			+ index
			+ '.value" class="btn datepicker"><img  src="/Fieldbook/static/img/calendar.png" style="padding-bottom:3px;" /></label>';

	$($(row).find(".2nd")).html(newCell);
}

function recreateSpinnerInput(index, row, selectedVal, name) {
	var newCell = "<input type='text' id='"
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
	} else if (parseInt(cvTermId, 10) === parseInt(breedingMethodId, 10)) {
		initializePossibleValuesCombo($.parseJSON(possibleValuesJson), "#"
				+ getJquerySafeId(name + index + ".value"), false, selectedVal);
	} else {
		initializePossibleValuesCombo($.parseJSON(possibleValuesJson), "#"
				+ getJquerySafeId(name + index + ".value"), false, selectedVal);
	}
}

function hideDeleteConfirmation() {
	$('#delete-settings-confirmation').modal('hide');
}

function clearSettings() {
	Spinner.toggle();
	$.ajax({
		url : "/Fieldbook/NurseryManager/createNursery/clearSettings",
		type : "GET",
		cache : false,
		success : function(html) {
			$("#chooseSettingsDiv").html(html);
			moveToTopScreen();
			Spinner.toggle();
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
		showErrorMessage('page-message', name + " " + nurseryNumericError);
	}

	return hasError;

}

function doSaveSettings() {
	$('#settingName').val($('#settingName').val().trim());
	if ($('#settingName').val() == '') {
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

function checkIfNull(object) {
	if (object != null) {
		return object;
	} else {
		return "";
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
	return "<input type='text' id='"
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
	return "<input type='text' id='" + name + ctr + ".value' name='" + name
			+ "[" + ctr + "].value' class='form-control numeric-input' />";
}
function createCharacterTextInput(ctr, name) {
	return "<input type='text' id='" + name + ctr + ".value' name='" + name
			+ "[" + ctr + "].value' class='form-control character-input' />";

}

function initializeDateAndSliderInputs() {
	if ($('.date-input').length > 0) {
		$('.date-input').each(function() {
			$(this).datepicker({
				'format' : 'yyyymmdd'
			}).on('changeDate', function(ev) {
				$(this).datepicker('hide');
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
	truncateStudyVariableNames('.study-variable-name', 19);
}

function checkPlantsSelected() {
	if (parseInt($("#plotsWithPlantsSelected").val()) === 0) {
		showErrorMessage('page-message', msgEmptyListError);
		$("#lineChoice1").prop("checked", true);
	}
}

function loadNurserySettingsForCreate(templateSettingsId) {
	Spinner.toggle();
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
			Spinner.toggle();
		}
	});
}

function openUsePreviousNurseryModal() {
	$("#selectedNursery").select2("destroy");
	$("#selectedNursery").val("");
	$("#selectedNursery").select2();
	$("#usePreviousNurseryModal").modal("show");
}

function choosePreviousNursery(studyId) {

	if ($("#chooseSettingsDiv").length !== 0) {
		url = "/Fieldbook/NurseryManager/createNursery/nursery/";
	}

	Spinner.toggle();
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
			Spinner.toggle();
		}
	});
}
function isStudyNameUnique() {
	var studyId = '0';
	if ($('#createNurseryMainForm #studyId').length !== 0)
		studyId = $('#createNurseryMainForm #studyId').val();
	var studyName = $.trim($('#' + getJquerySafeId('basicDetails0.value'))
			.val());

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
	var hasError = false;
	var name = '';
	var customMessage = '';
	var studyBookName;
	var studyNameId = $("#studyNameTermId").val();

	$('.nurseryLevelVariableIdClass').each(function() {
		if (studyNameId == $(this).val()) {
			studyBookName = $(this).parent().find(".form-control").val();
		}
	});

	var startDate = $("#" + getJquerySafeId("basicDetails.value2")).val();

	if (isStudyNameUnique() === false) {
		hasError = true;
		customMessage = "Name should be unique";
	} else if ($("#folderId").val() === '') {
		hasError = true;
		name = $("#folderLabel").text();
	} else if (startDate == '') {
		// validate creation date
		hasError = true;
		name = "Creation Date";
	} else if ($('#checkId').val() === '') {
		hasError = true;
		customMessage = checkTypeIsRequired;
	} else if ($('.check-germplasm-list-items tbody tr').length > 0 && $('.germplasm-list-items tbody tr').length === 0){
		hasError = true;
		customMessage = nurseryGermplasmListIsRequired;
	} else {
		$('.nurseryLevelVariableIdClass')
				.each(
						function() {
							if (!hasError) {

								if ($(this).parent().next().find(
										".form-control").hasClass("select2")
										&& $(this).parent().next().find(
												".form-control")
												.select2("data")) {
									idname = $(this).parent().next().find(
											".form-control").attr("id");
									// console.log(idname);
									value = $("#" + getJquerySafeId(idname))
											.select2("data").text;
								} else {
									value = $(this).parent().next().find(
											".form-control").val();
								}
								value = $.trim(value);
								if (!value) {
									name = $(this).parent().find(
											".control-label").html();
									hasError = true;
								}
							}

						});
	}

	if (hasError) {
		var errMsg = '';
		if (name !== '')
			errMsg = name.replace('*', '').replace(":", "") + " "
					+ nurseryFieldsIsRequired;
		if (customMessage !== '')
			errMsg = customMessage;
		showErrorMessage('page-message', errMsg);
		return false;
	}

	var valid = validateStartEndDateBasic();

	if (!valid) {
		return false;
	}

	$.each($(".numeric-input"), function(index, textField) {
		if (isNaN($(textField).val())) {
			hasError = true;
			name = $(this).parent().prev().find(".control-label").html();
			customMessage = name + " " + valueNotNumeric;
		}
	});

	$.each($(".numeric-input"), function(index, textField) {
		if (parseFloat($(textField).val()) > $(textField).data("max")
				|| parseFloat($(textField).val()) < $(textField).data("min")) {
			hasError = true;
			name = $(this).parent().prev().find(".control-label").html();
			customMessage = name + " " + valueNotWIMinMax + " "
					+ $(textField).data("min") + " to "
					+ $(textField).data("max");
		}
	});

	if (hasError) {
		showErrorMessage('page-message', customMessage.replace('*', '')
				.replace(":", ""));
		return false;
	}

	var found = false;
	$("#selectedNursery option").each(function(i) {
		if (studyBookName == $(this).text()) {
			found = true;
		}
	});
	if (found) {
		hasError = true;
		showErrorMessage('page-message', uniqueBookNameErrorMessage);
		return false;
	}
	/*
	 * Validate Position is less than the total germplasm Validate the Interval
	 * should be less than the total germplasm
	 */
	if ($('.checkRow').length > 0) {
		// we validate only if there is a check
		// we try to validate if all the check row has check
		var checkIndex = 0;
		for (checkIndex = 0; checkIndex < $('.checkRow').length; checkIndex++) {
			if ($('select.checklist-select:eq(' + checkIndex + ')').val() == '') {
				showErrorMessage('page-message',
						"Please make sure all selected checks have their check type");
				return false;
			}
		}

		if (isInt($("#startIndex2").val()) === false) {
			showErrorMessage('page-message',
					"Position should be a whole number");
			return false;
		}
		if (isInt($("#interval2").val()) === false) {
			showErrorMessage('page-message',
					"Interval should be a whole number");
			return false;
		}
		var totalGermplasms = $('#totalGermplasms').val();
		if (parseInt($("#startIndex2").val(), 10) < 0
				|| parseInt($("#startIndex2").val(), 10) > totalGermplasms) {
			showErrorMessage('page-message', "Position should be between 1 to "
					+ totalGermplasms);
			return false;
		}
		if (parseInt($("#interval2").val(), 10) < 0) {
			showErrorMessage('page-message', "Interval should be between 1 to "
					+ totalGermplasms);
			return false;
		}
		var totalNumberOfChecks = $('#totalNumberOfChecks').val();
		if (parseInt($("#interval2").val(), 10) <= totalNumberOfChecks) {
			showErrorMessage('page-message',
					"Interval should be greater than the number of checks ("
							+ totalNumberOfChecks + ")");
			return false;
		}
	}

	return true;
}

function reloadCheckTypeDropDown(addOnChange, select2ClassName) {
	Spinner.toggle();
	var currentCheckId = $('#checkId').val();
	$.ajax({
		url : "/Fieldbook/NurseryManager/importGermplasmList/getAllCheckTypes",
		type : "GET",
		cache : false,
		data : "",
		success : function(data) {
			initializeCheckTypeSelect2($.parseJSON(data.allCheckTypes), [],
					addOnChange, currentCheckId, select2ClassName);
			Spinner.toggle();
		}
	});
}

function initializeCheckTypeSelect2(suggestions, suggestions_obj, addOnChange,
		currentFieldId, comboName) {
	var defaultData = null;

	if (suggestions_obj.length === 0) {
		$
				.ajax({
					url : "/Fieldbook/NurseryManager/importGermplasmList/getAllCheckTypes",
					type : "GET",
					cache : false,
					data : "",
					async : false,
					success : function(data) {
						checkTypes = $.parseJSON(data.allCheckTypes);
						suggestions = checkTypes;
						// alert('here');
					}
				});
	}

	if (suggestions != null) {
		$.each(suggestions, function(index, value) {
			if (comboName == "comboCheckCode") {
				dataObj = {
					'id' : value.id,
					'text' : value.name,
					'description' : value.description
				};
			} else {
				dataObj = {
					'id' : value.id,
					'text' : value.description,
					'description' : value.description
				};
			}
			suggestions_obj.push(dataObj);
		});
	} else {
		$.each(suggestions_obj, function(index, value) {
			if (currentFieldId != '' && currentFieldId == value.id) {
				defaultData = value;
			}
		});
	}
	// if combo to create is one of the ontology combos, add an onchange event
	// to populate the description based on the selected value
	if (comboName == "comboCheckCode") {
		$('#' + comboName)
				.select2(
						{
							query : function(query) {
								var data = {
									results : sortByKey(suggestions_obj, "text")
								};
								// return the array that matches
								data.results = $.grep(data.results, function(
										item, index) {
									return ($.fn.select2.defaults.matcher(
											query.term, item.text));
								});
								if (data.results.length === 0) {
									data.results.unshift({
										id : query.term,
										text : query.term
									});
								}
								query.callback(data);
							}
						})
				.on(
						"change",
						function() {
							if ($("#comboCheckCode").select2("data")) {
								if ($("#comboCheckCode").select2("data").id == $("#comboCheckCode").select2("data").text) {
									$("#manageCheckValue").val("");
									$("#updateCheckTypes").hide();
									$("#deleteCheckTypes").hide();
									$("#addCheckTypes").show();
								} else {
									$("#manageCheckValue").val($("#comboCheckCode").select2("data").description);
									$("#updateCheckTypes").show();
									$("#deleteCheckTypes").show();
									$("#addCheckTypes").hide();
								}
							}
						});
	} else {
		$('.' + comboName).each(
				function() {
					var currentVal = $(this).val();
					$(this).empty();
					$(this).select2('destroy');
					var i = 0;
					for (i = 0; i < suggestions_obj.length; i++) {
						var val = suggestions_obj[i].text;
						var id = suggestions_obj[i].id;
						var selected = '';
						if (currentVal == id)
							selected = 'selected';
						$(this).append(
								$('<option ' + selected + ' ></option>').attr(
										'value', id).text(val));
					}

				});
		$('.' + comboName).select2();
	}
}

function showManageCheckTypePopup() {
	$('#page-check-message-modal').html("");
	resetButtonsAndFields();
	$('#manageCheckTypesModal').modal({
		backdrop : 'static',
		keyboard : false
	});
}

function resetButtonsAndFields() {
	$("#manageCheckValue").val("");
	$("#comboCheckCode").select2("val", "");
	$("#updateCheckTypes").hide();
	$("#deleteCheckTypes").hide();
	$("#addCheckTypes").show();
}

function addUpdateCheckType(operation) {
	if (validateCheckFields()) {
		var $form = $("#manageCheckValue,#comboCheckCode");
		var serializedData = $form.serialize();
		Spinner.toggle();
		$
				.ajax({
					url : "/Fieldbook/NurseryManager/importGermplasmList/addUpdateCheckType/"
							+ operation,
					type : "POST",
					data : serializedData,
					cache : false,
					success : function(data) {
						if (data.success == "1") {
							// reload dropdown
							reloadCheckTypeList(data.checkTypes, operation);
							showCheckTypeMessage(data.successMessage);
						} else {
							showCheckTypeErrorMessage(data.error);
						}
						Spinner.toggle();
					}
				});
	}
}

function validateCheckFields() {
	if (checkTypes_obj.length === 0 && checkTypes != null) {
		$.each(checkTypes, function(index, item) {
			checkTypes_obj.push({
				'id' : item.id,
				'text' : item.name,
				'description' : item.description
			});
		});
	}

	if (!$("#comboCheckCode").select2("data")) {
		showCheckTypeErrorMessage(codeRequiredError);
		return false;
	} else if ($("#manageCheckValue").val() === "") {
		showCheckTypeErrorMessage(valueRequiredError);
		return false;
	} else if (!isValueUnique()) {
		showCheckTypeErrorMessage(valueNotUniqueError);
		return false;
	}

	return true;
}

function isValueUnique() {
	var isUnique = true;
	$.each(checkTypes_obj, function(index, item) {
		if (item.description == $("#manageCheckValue").val()
				&& item.id != $("#comboCheckCode").select2("data").id) {
			isUnique = false;
			return false;
		}
	});
	return isUnique;
}

function showCheckTypeErrorMessage(message) {
	$('#page-check-message-modal').html(
			"<div class='alert alert-danger'>" + message + "</div>");
}

function showCheckTypeMessage(message) {
	$('#page-check-message-modal').html(
			"<div class='alert alert-success'>" + message + "</div>");
}

function deleteCheckType() {
	if ($("manageCheckCode").select2("data")) {

		var $form = $("#manageCheckValue,#comboCheckCode");
		var serializedData = $form.serialize();
		Spinner.toggle();
		$
				.ajax({
					url : "/Fieldbook/NurseryManager/importGermplasmList/deleteCheckType",
					type : "POST",
					data : serializedData,
					cache : false,
					success : function(data) {
						if (data.success == "1") {
							reloadCheckTypeList(data.checkTypes, 3);
							showCheckTypeMessage(data.successMessage);
							resetButtonsAndFields();
						} else {
							showCheckTypeErrorMessage(data.error);
						}
						Spinner.toggle();
					}
				});
	} else {
		showCheckTypeErrorMessage(noCheckSelected);
	}
}

function reloadCheckTypeList(data, operation) {
	var selectedValue = 0;

	checkTypes_obj = [];

	if (data != null) {
		$.each($.parseJSON(data), function(index, value) {
			checkTypes_obj.push({
				'id' : value.id,
				'text' : value.name,
				'description' : value.description
			});
		});
	}

	if (operation == 2) {
		// update
		selectedValue = getIdOfValue($("#manageCheckValue").val());
	}

	$("#manageCheckValue").val("");
	initializeCheckTypeSelect2(null, [], false, 0, "comboCheckCode");
	initializeCheckTypeSelect2(null, checkTypes_obj, false, selectedValue,
			"comboCheckCode");
}

function getIdOfValue(value) {
	var id = 0;
	$.each(checkTypes_obj, function(index, item) {
		if (item.description == value) {
			id = item.id;
			return false;
		}
	});
	return id;
}

function changeBrowseNurseryButtonBehavior(isEnable) {
	if (isEnable)
		$('.browse-nursery-action').removeClass('disable-image');
	else
		$('.browse-nursery-action').addClass('disable-image');

}

function changeBrowseGermplasmButtonBehavior(isEnable) {
	if (isEnable)
		$('.browse-germplasm-action').removeClass('disable-image');
	else
		$('.browse-germplasm-action').addClass('disable-image');

}

function addStudyTreeHighlight(node) {
	$(node.span).addClass('fbtree-focused');
}

function initializeStudyTabs() {
	$('#study-tab-headers li').on('click', function() {
		$('#study-tab-headers li').removeClass('active');
		$(this).addClass('active');
		$('#study-tabs .info').hide();
		$('.info#' + $(this).attr('id')).show();
	});
	$('#study-tab-headers .fbk-close-tab').on('click', function() {
		var studyId = $(this).attr('id');
		var showFirst = false;
		if ($(this).parent().parent().hasClass('active')) {
			// console.log('get the first item');
			showFirst = true;
		}
		$('li#study' + studyId).remove();
		$('.info#study' + studyId).remove();
		if (showFirst && $('#study-tab-headers li').length > 0) {
			var studyIdString = $('#study-tab-headers li:eq(0)').attr('id');
			$('li#' + studyIdString).addClass('active');
			$('.info#' + studyIdString).show();
		}
		determineIfShowCloseAllStudyTabs();
	});
	determineIfShowCloseAllStudyTabs();
}
function addDetailsTab(studyId, title) {
	// if the study is already existing, we show that tab
	$('#study-tab-headers li').removeClass('active');
	$('#study-tabs .info').hide();
	if ($('li#study' + studyId).length !== 0) {
		$('li#study' + studyId).addClass('active');
		$('.info#study' + studyId).show();
	} else {

		Spinner.toggle();
		$.ajax({
			url : "/Fieldbook/NurseryManager/reviewNurseryDetails/show/"
					+ studyId,
			type : "GET",
			cache : false,
			success : function(data) {
				var close = '<i class="glyphicon glyphicon-remove fbk-close-tab" id="'+studyId+'"></i>';
				$('#study-tab-headers').append(
						"<li id='study" + studyId + "' class='active'><a>"
								+ title + close + "</a></li>");
				$('#study-tabs').append(
						'<div class="info" id="study' + studyId + '">' + data
								+ '</div>');
				$('.info#study' + studyId).show();
				initializeStudyTabs();
				$('.info#study' + studyId + ' select').each(function() {
					$(this).select2();
				});
				Spinner.toggle();
			}
		});
	}
	determineIfShowCloseAllStudyTabs();
	// if not we get the info
}

function determineIfShowCloseAllStudyTabs() {
	if ($('#study-tab-headers li').length > 0) {
		$('#closeAllStudytabs').css('display', 'block');
	} else {
		$('#closeAllStudytabs').css('display', 'none');
	}
}

function openStudyTree(type) {
	$('#page-study-tree-message-modal').html('');
	if( $('#create-nursery #studyTree').length !== 0){
		 	$('#create-nursery #studyTree').dynatree("getTree").reloadStudyTree();
		 	changeBrowseNurseryButtonBehavior(false);
	}	
	
	$('#studyTreeModal').modal({
		backdrop : 'static',
		keyboard : true
	});
	choosingType = type;	
}
function closeAllStudyTabs() {

	$('#study-tab-headers').html('');
	$('#study-tabs').html('');
	determineIfShowCloseAllStudyTabs();
}

function loadDatasetDropdown(optionTag) {
	if ($('#study' + getCurrentStudyIdInTab() + " #dataset-selection option").length > 1)
		return;
	Spinner.toggle();
	$.ajax({
		url : "/Fieldbook/NurseryManager/reviewNurseryDetails/datasets/"
				+ getCurrentStudyIdInTab(),
		type : "GET",
		cache : false,
		success : function(data) {
			var i = 0;
			for (i = 0; i < data.length; i++) {
				optionTag.append(new Option(data[i].name, data[i].id));
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			console.log("The following error occured: " + textStatus,
					errorThrown);
		},
		complete : function() {
			Spinner.toggle();
		}
	});
}

function getCurrentStudyIdInTab() {
	return $('#study-tab-headers li.active .fbk-close-tab').attr('id');
}

function loadDatasetMeasurementRowsViewOnly(datasetId, datasetName) {

	var currentStudyId = getCurrentStudyIdInTab();
	if (datasetId == 'Please Choose'
			|| $("#" + getJquerySafeId('dset-tab-') + datasetId).length !== 0)
		return;
	Spinner.toggle();
	$.ajax({
		url : "/Fieldbook/NurseryManager/addOrRemoveTraits/viewNurseryAjax/"
				+ currentStudyId,
		type : "GET",
		cache : false,
		success : function(html) {
			$("#study" + currentStudyId + " #measurement-tab-headers").append(
					"<li class='active' id='dataset-li'" + datasetId + "><a>"
							+ datasetName + "</a> " + "</li>");
			$("#study" + currentStudyId + " #measurement-tabs").append(
					"<div id='dset-tab-" + datasetId + "'>" + html + "</div>");
			$("#study" + currentStudyId + " .measurement-section").show();
		},
		error : function(jqXHR, textStatus, errorThrown) {
			console.log("The following error occured: " + textStatus,
					errorThrown);
		},
		complete : function() {
			Spinner.toggle();
		}
	});
}
function showSelectedTab(selectedTabName) {
	$("#create-nursery-tab-headers").show();
	var tabs = $("#create-nursery-tabs").children();
	for (var i = 0; i < tabs.length; i++) {
		if (tabs[i].id == selectedTabName) {
			$("#" + tabs[i].id + "-li").addClass("active");
			$("#" + tabs[i].id).show();
		} else {
			$("#" + tabs[i].id + "-li").removeClass("active");
			$("#" + tabs[i].id).hide();
		}
	}
	
	if(selectedTabName === 'nursery-measurements') {
		var dataTable = $('#measurement-table').dataTable();
		if(dataTable.length != 0)
			dataTable.fnAdjustColumnSizing();
	}

}

function showStudyInfo() {
	$("#folderBrowserModal").modal("show");
}

function validateStartEndDateBasic() {
	var startDate = $("#" + getJquerySafeId("basicDetails.value2")).val();
	var endDate = $("#" + getJquerySafeId("basicDetails.value4")).val();

	startDate = startDate == null ? '' : startDate;
	endDate = endDate == null ? '' : endDate;

	if (startDate === '' && endDate === '')
		return true;
	else if (startDate !== '' && endDate === '') {
		return true;
	} else if (startDate === '' && endDate !== '') {
		showErrorMessage("page-message", startDateRequiredError);
		return false;
	} else if (parseInt(startDate) > parseInt(endDate)) {
		showErrorMessage("page-message", startDateRequiredEarlierError);
		return false;
	}
	return true;

}

function recreateModalMethodCombo(comboName, comboFaveCBoxName) {
	var selectedMethodAll = $("#methodIdAll").val();
	var selectedMethodFavorite = $("#methodIdFavorite").val();

	Spinner.toggle();
	$.ajax({
		url : "/Fieldbook/NurseryManager/advance/nursery/getBreedingMethods",
		type : "GET",
		cache : false,
		data : "",
		async : false,
		success : function(data) {
			if (data.success == "1") {
				if (selectedMethodAll != null) {
					//recreate the select2 combos to get updated list of methods    			   
					recreateMethodComboAfterClose("methodIdAll", $
							.parseJSON(data.allMethods));
					recreateMethodComboAfterClose("methodIdFavorite", $
							.parseJSON(data.favoriteMethods));
					showCorrectMethodCombo();
					//set previously selected value of method
					if ($("#showFavoriteMethod").prop("checked")) {
						setComboValues(methodSuggestionsFav_obj,
								selectedMethodFavorite, "methodIdFavorite");
					} else {
						setComboValues(methodSuggestions_obj,
								selectedMethodAll, "methodIdAll");
					}
				} else {
					var selectedVal = null;
					//get index of breeding method row
					var index = getBreedingMethodRowIndex();

					if ($("#" + getJquerySafeId(comboName)).select2("data")) {
						selectedVal = $("#" + getJquerySafeId(comboName))
								.select2("data").id;
					}
					//recreate select2 of breeding method
					initializePossibleValuesCombo([], "#"
							+ getJquerySafeId(comboName), false, selectedVal);

					//update values of combo
					if ($("#" + getJquerySafeId(comboFaveCBoxName)).is(
							":checked")) {
						initializePossibleValuesCombo($
								.parseJSON(data.favoriteMethods), "#"
								+ getJquerySafeId(comboName), false,
								selectedVal);
					} else {
						initializePossibleValuesCombo($
								.parseJSON(data.allMethods), "#"
								+ getJquerySafeId(comboName), false,
								selectedVal);
					}

					replacePossibleJsonValues(data.favoriteMethods,
							data.allMethods, index);
				}
			} else {
				showErrorMessage("page-message", data.errorMessage);
			}
		},
		error : function(jqXHR, textStatus, errorThrown) {
			console.log("The following error occured: " + textStatus,
					errorThrown);
		},
		complete : function() {
			Spinner.toggle();
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
			showErrorMessage("page-message", noPlotVariatesError);
		}
	}
}

function refreshEditNursery() {
	$('#successStudyMessageModal').modal('hide');
	$('#page-message').html('');
}

function recreateSessionVariables() {
	$.ajax({
		url: "/Fieldbook/NurseryManager/editNursery/recreate/session/variables",
		type: "GET",
		data: "",
		success: function (html) {
			$("#measurementsDiv").html(html);
			if ($("#measurementDataExisting")) {
				displayCorrespondingGermplasmSections();
			}
			$('#successStudyMessageModal').modal({ backdrop: 'static', keyboard: true });
		},
		error: function (jqXHR, textStatus, errorThrown) {
			console.log(errorThrown);
		},
		complete: function () {
			Spinner.toggle();
		}	
	});
}

function displayCorrespondingGermplasmSections() {
	if ($("#measurementDataExisting").val() === "true") {
		$("#chooseGermplasmAndChecks").hide();
		$(".overwrite-germplasm-list").hide();
		$(".observation-exists-notif").show();
	} else if (measurementRowCount > 0) {
		$("#chooseGermplasmAndChecks").hide();
		$(".observation-exists-notif").hide();
		$(".overwrite-germplasm-list").show();
	} else {
		$("#chooseGermplasmAndChecks").show();
		$(".observation-exists-notif").hide();
		$(".overwrite-germplasm-list").hide();
	}
}

function showGermplasmDetailsSection() {
	$("#chooseGermplasmAndChecks").show();
	$(".observation-exists-notif").hide();
	$(".overwrite-germplasm-list").hide();
}

//FIXME Should not be using global variables or functions
/*global lastDraggedChecksList, Spinner, validateCreateNursery, validateStartEndDate, moveToTopScreen*/
/*global loadNurserySettingsForCreate, getJquerySafeId, changeBuildOption*/
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
			$('#successStudyMessageModal').modal({ backdrop: 'static', keyboard: true });
			Spinner.toggle(); 
		} 
	});
}
