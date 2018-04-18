/*global getJquerySafeId, showErrorMessage, oldLineSelected, changeAdvanceBreedingMethod, setCorrecMethodValues, oldMethodSelected, msgSamplePlotError, msgHarvestDateError, methodSuggestionsFav_obj, isInt, breedingMethodId, oldMethodSelected*/
/*global isStudyNameUnique, validateStartDateEndDateBasic*/

//Used globle variable to selected location for study
var selectedLocationForTrial;
var possibleLocationsForTrial;

function checkMethod() {
	'use strict';
	var methodChoiceCheckbox = $('input[type=checkbox][name=methodChoice]');

	if (!methodChoiceCheckbox.length) {
		return;
	}

	if (methodChoiceCheckbox.is(':checked')) {
		$('#methodIdFavorite').select2('enable', true);
		$('#methodIdAll').select2('enable', true);
		$('#methodIdDerivativeAndMaintenance').select2('enable', true);
		$('#methodIdDerivativeAndMaintenanceFavorite').select2('enable', true);

		$('#showFavoriteMethod').prop('disabled', false);

		$('#method-variates-section').hide();
		$('.method-selection-div').find('input,select').prop('disabled', false);

		changeAdvanceBreedingMethod();
	} else {
		$('#method-variates-section').show();
		$('.method-selection-div').find('input,select').prop('disabled', true);

		$('#methodIdAll').select2('enable', false);
		$('#methodIdFavorite').select2('enable', false);
		$('#methodIdDerivativeAndMaintenance').select2('enable', false);
		$('#methodIdDerivativeAndMaintenanceFavorite').select2('enable', false);

		if ($('#namingConvention').val() != 3) {
			$('#showFavoriteMethod').prop('disabled', 'disabled');
		}
		
		// we show the bulk and lines section
		$('.bulk-section').show();
		$('.lines-section').show();
		if ($('#methodVariateId').has('option').length === 0) {
			$('input[type=checkbox][name=methodChoice]').prop('checked', true);
			$('input[type=checkbox][name=methodChoice]').change();
			showErrorMessage('page-advance-modal-message', noMethodVariatesErrorTrial);

		} else {
			displaySectionsPerMethodVariateValues();
		}
	}
}

function displaySectionsPerMethodVariateValues() {
	'use strict';
	var id = $('#methodVariateId').val();
	if (id !== '') {
		$.ajax({
			url: '/Fieldbook/NurseryManager/advance/nursery/checkMethodTypeMode/' + id,
			type: 'GET',
			cache: false,
			success: function(data) {
				if (data === 'LINE') {
					$('.lines-section').css('display', 'block');
				} else if (data === 'BULK') {
					$('.bulk-section').css('display', 'block');
				} else if (data === 'MIXED') {
					$('.lines-section').css('display', 'block');
					$('.bulk-section').css('display', 'block');
				}
			}
		});
	}
}

function lineMethod() {
	var $lineCheckBox = $('input[type=checkbox][name=lineChoice]');

	if (!$lineCheckBox.length) {
		return;
	}

	if ($lineCheckBox.is(':checked')) {
		$('#lineSelected').prop('disabled', false);
		$('#lineSelected').val(oldLineSelected);
		$('#line-variates-section').hide();
		$('.lines-per-plot-section').show();
	} else {
		$('#lineSelected').prop('disabled', 'disabled');
		oldLineSelected = $('#lineSelected').val();
		$('#lineSelected').val(1);
		$('#line-variates-section').show();
		$('.lines-per-plot-section').hide();
		if ($('#lineVariateId').has('option').length === 0) {
			$('input[type=checkbox][name=lineChoice]').prop('checked', true);
			$('input[type=checkbox][name=lineChoice]').change();
			showErrorMessage('page-advance-modal-message', noLineVariatesErrorTrial);
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

		if ($("#showBreedingLocationOnlyRadio").is(':checked')) {
			$('#s2id_harvestLocationIdBreedingFavorites').show();
			$('#s2id_harvestLocationIdFavorite').hide();
		} else {
			$('#s2id_harvestLocationIdBreedingFavorites').hide();
			$('#s2id_harvestLocationIdFavorite').show();
		}

		$('#s2id_harvestLocationIdAll').hide();
		$('#s2id_harvestLocationIdBreeding').hide();
		if ($('#' + getJquerySafeId('harvestLocationIdFavorite')).select2(
				'data') != null) {
			$('#' + getJquerySafeId('harvestLocationId')).val(
					$('#' + getJquerySafeId('harvestLocationIdFavorite'))
							.select2('data').id);
			$('#' + getJquerySafeId('harvestLocationName')).val(
					$('#' + getJquerySafeId('harvestLocationIdFavorite'))
							.select2('data').text);
			$('#' + getJquerySafeId('harvestLocationAbbreviation')).val(
					$('#' + getJquerySafeId('harvestLocationIdFavorite'))
							.select2('data').abbr);

		} else {
			$('#' + getJquerySafeId('harvestLocationId')).val(0);
			$('#' + getJquerySafeId('harvestLocationName')).val('');
			$('#' + getJquerySafeId('harvestLocationAbbreviation')).val('');
		}
	} else {
		$('#s2id_harvestLocationIdFavorite').hide();
		$('#s2id_harvestLocationIdBreedingFavorites').hide();

		if ($('#showAllLocationOnlyRadio').is(':checked')) {
			$('#s2id_harvestLocationIdAll').show();
			$('#s2id_harvestLocationIdBreeding').hide();
		} else {
			$('#s2id_harvestLocationIdBreeding').show();
			$('#s2id_harvestLocationIdAll').hide();
		}

        // harvestLocationIdAll is not null but it contains blank value so put AND condition
		if ($('#' + getJquerySafeId('harvestLocationIdAll')).select2('data') != null && $('#' + getJquerySafeId('harvestLocationIdAll')).select2('data').id != "" ) {
			$('#' + getJquerySafeId('harvestLocationId')).val($('#' + getJquerySafeId('harvestLocationIdAll')).select2('data').id);
			$('#' + getJquerySafeId('harvestLocationName')).val($('#' + getJquerySafeId('harvestLocationIdAll')).select2('data').text);
			$('#' + getJquerySafeId('harvestLocationAbbreviation')).val($('#' + getJquerySafeId('harvestLocationIdAll')).select2('data').abbr);
		}

	}

	//In case of study we have to set selected location
	setSelectedLocation();
}

function setFavoriteMethodCheckbox(){
	if (methodSuggestionsDerivativeAndMaintenanceFavorite.length > 0) {
		$('#showFavoriteMethod').prop('checked', true);
	}
	else{
		$('#showFavoriteMethod').prop('checked', false);
	}
}

function showCorrectMethodCombo() {
	var isChecked = $('#showFavoriteMethod').is(':checked');
	// if show favorite Method is checked, hide all field locations, else, show
	// only favorite methods
	var methodSelect = false;
	if ($('input[type=checkbox][name=methodChoice]:checked').val() === '1') {
		methodSelect = true;
	}

	// Hide everything then choose what to show
	$('#s2id_methodIdDerivativeAndMaintenanceFavorite').hide();
	$('#s2id_methodIdFavorite').hide();
	$('#s2id_methodIdAll').hide();
	$('#s2id_methodIdDerivativeAndMaintenance').hide();

	if (isChecked) {
		if ($('#showDerivativeAndMaintenanceRadio').is(':checked')) {
			$('#s2id_methodIdDerivativeAndMaintenanceFavorite').show();
		} else {
			$('#s2id_methodIdFavorite').show();
		}

	} else {
		if ($('#showDerivativeAndMaintenanceRadio').is(':checked')) {
			$('#s2id_methodIdDerivativeAndMaintenance').show();
		} else {
			$('#s2id_methodIdAll').show();
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
		rowIndex = getIndexFromName($($(row).find('.1st').find('.cvTermIds')).attr('name'));
	});
	return rowIndex;
}

function getLocationRowIndex() {
	var rowIndex = -1;
	$.each($('.nurseryLevelSettings'), function(index, row) {
		var cvTermId = $($(row).find('.1st').find('.cvTermIds')).val();
		if (parseInt(cvTermId, 10) == parseInt(locationId, 10)) {
			rowIndex = getIndexFromName($($(row).find('.1st').find('.cvTermIds')).attr('name'));
		}
	});

	return rowIndex;
}

function getIndexFromName(name) {
	return name.split('[')[1].split(']')[0];
}

function replacePossibleJsonValues(possibleValues, possibleValuesFavorite, allValues, allFavoriteValues, index) {
	'use strict';
	$('#possibleValuesJson' + index).text(JSON.stringify(possibleValues));
	$('#possibleValuesFavoriteJson' + index).text(JSON.stringify(possibleValuesFavorite));
	$('#allValuesJson' + index).text(JSON.stringify(allValues));
	$('#allFavoriteValuesJson' + index).text(JSON.stringify(allFavoriteValues));
}

function replacePossibleJsonValuesImportGermPlasm(possibleValues, possibleValuesFavorite, allValues, allFavoriteValues, index) {
	'use strict';
	$('#possibleValuesJson' + index).text(JSON.stringify(possibleValues));
	$('#possibleValuesFavoriteJson' + index).text(JSON.stringify(possibleValuesFavorite));
	$('#allPossibleValuesJson' + index).text(JSON.stringify(allValues));
	$('#allPossibleValuesFavoriteJson' + index).text(JSON.stringify(allFavoriteValues));
}

function setComboValues(suggestions_obj, id, name) {
	var dataVal = {
		id: '',
		text: '',
		description: ''
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
	$('#' + name).select2('data', dataVal);
}

function initializeStandardVariableSearch(variables) {
	// set values
	var stdVariableSuggestions_obj = [];
	$.each(variables, function(index, value) {
		stdVariableSuggestions_obj.push({
			'id': value.id,
			'text': value.name
		});
	});

	stdVariableSuggestions_obj = sortByKey(stdVariableSuggestions_obj, 'text');

	$('#stdVarSearch').select2({
		query: function(query) {
			var data = {
				results: stdVariableSuggestions_obj
			};
			// return the array that matches
			data.results = $.grep(data.results, function(item, index) {
				return ($.fn.select2.defaults.matcher(query.term, item.text));

			});
			if (data.results.length === 0) {
				data.results.unshift({
					id: query.term,
					text: query.term
				});
			}

			query.callback(data);
		}
	}).unbind('change').on('change', function() {
		// set attribute values
		getStandardVariableDetailsModal($('#stdVarSearch').select2('data').id);
	});
	var dataVal = {
		'id': '',
		'text': ''
	};
	$('#stdVarSearch').select2('data', dataVal).trigger('change');
}

function getStandardVariableDetailsModal(id) {
	'use strict';
	if (id !== '') {
		$
				.ajax({
					url: '/Fieldbook/NurseryManager/createNursery/showVariableDetails/'
							+ id,
					type: 'GET',
					cache: false,
					success: function(data) {
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
		return '&nbsp';
	} else {
		return value;
	}
}

function hideDummyRow(tableId) {
	'use strict';
	if ($('#' + tableId).find('.dummy-col').parent().length !== 0) {
		$('#' + tableId).find('.dummy-col').parent().remove();
	}
}

function showDummyRow(tableId) {
	var dummyRow = '<tr class="dummy-row"> ' +
					'<td class="even dummy-col">&nbsp;</td> ' +
					'<td class="even">&nbsp;</td> ' +
					'<td class="even">&nbsp;</td> </tr>';
	$('#' + tableId + ' tbody').append(dummyRow);
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
	var ctr = $('.' + rowClass).length;

	$.each(
	data,
	function(index, settingDetail) {
		var newRow = '<div class="row form-group ' + rowClass
				+ ' newVariable">';
		var isDelete = '';

		// include delete button if variable is deletable
		if (settingDetail.deletable) {
			isDelete = '<input class="remove-indv-btn"'
				+ ' type="checkbox" data-variable-type="' + varType + '"'
				+ ' data-cv-term-id="' + settingDetail.variable.cvTermId + '"/>';
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
				+ '" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;';

		newRow = newRow
				+ '<a href="javascript: void(0);" onclick="javascript:showBaselineTraitDetailsModal('
			+ settingDetail.variable.cvTermId + ',' + varType
				+ ');" ><span style="word-wrap: break-word"  class="var-names control-label label-bold">'
				+ settingDetail.variable.name
				+ '</span></a>: &nbsp;</div>';

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

		// we want to keep track of location/method checkbox id
		var locMethodCbxId = null;

		if (parseInt(settingDetail.variable.cvTermId, 10) == parseInt(breedingMethodId, 10) ||
				parseInt(settingDetail.variable.cvTermId, 10) === parseInt($('#breedingMethodCode').val(), 10)) {

			// all values div
			newRow = newRow
					+ '<div class="possibleValuesDiv"> <label class="radio-inline"> <input id="filterMethods" name="methodFilter" type="radio" checked="checked"'
					+ ' onclick="javascript: toggleMethodDropdown('
					+ ctr
					+ ');" /> <span >Derivative and Maintenance methods</span> &nbsp;&nbsp; '
					+ '<input id="allMethods" name="methodFilter" type="radio"'
					+ ' onclick="javascript: toggleMethodDropdown('
					+ ctr
					+ ');" /> <span >All methods</span></label>'
					+ '</span></div>'
					+ '<div id="allValuesJson" class="allValuesJson" style="display:none">'
					+ JSON.stringify(settingDetail.allValues)
					+ '</div>'
					+ '<div id="allFavoriteValuesJson" class="allFavoriteValuesJson" style="display:none">'
					+ JSON.stringify(settingDetail.allFavoriteValues)
					+ '</div>';

			// show favorite method
			locMethodCbxId = name + ctr;
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
					+ '<span><a href="javascript: openManageMethods();">'
					+ manageMethodLabel + '</a></span>';
			newRow = newRow + '</div>';

		} else if (settingDetail.variable.cvTermId == locationId) {

			locMethodCbxId = name + ctr;
			// all values div
			newRow = newRow
					+ '<div class="possibleValuesDiv"> <label class="radio-inline"> <input id="filterLocations" name="locationFilter" type="radio" checked="checked"'
					+ ' onclick="javascript: toggleLocationDropdown('
					+ ctr
					+ ');" /> <span >All Breeding locations</span> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  '
					+ '<input id="allLocations" name="locationFilter" type="radio" '
					+ ' onclick="javascript: toggleLocationDropdown('
					+ ctr
					+ ');" /> <span >All location types</span></label>'
					+ '</span></div>'
					+ '<div id="allValuesJson'
					+ ctr
					+ '" class="allValuesJson"'
					+ 'style="display:none">'
					+ JSON.stringify(settingDetail.allValues)
					+ '</div>';

			// all favorite values div
			newRow = newRow
					+ '<div id="allFavoriteValuesJson'
					+ ctr
					+ '" class="allFavoriteValuesJson"'
					+ 'style="display:none">'
					+ JSON.stringify(settingDetail.allFavoriteValues)
					+ '</div>';

			// show favorite location
			locMethodCbxId = name + ctr;
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
					+ ' onclick="javascript: toggleLocationDropdown('
					+ ctr
					+ ');" />'
					+ '<input type="hidden" name="_'
					+ name
					+ '['
					+ ctr
					+ '].favorite" value="on" /> '
					+ '<span>&nbsp;&nbsp;'
					+ showFavoriteLocationLabel
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
		// the element will be appended to the dom
		if (tableId === 'nurseryLevelSettings-dev' || tableId === 'nurseryConditionsSettings') {
			$('#' + tableId + ' .remove-all-section').before(newRow);
		} else {
			$('#' + tableId).append(newRow);
		}

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

		// after the combo box have been added to the DOM
		// if locMethodCbxId is not null lets toggle it
		// lets make sure that we have a favorite items before we toggle the checkbox
		if (!!locMethodCbxId && settingDetail.possibleValuesFavorite.length > 0) {
			$('input[type=checkbox][id*=' + locMethodCbxId + ']').click();
		}

		ctr++;
	});

	initializeDateAndSliderInputs();
	checkNurseryIfShowRemoveVariableLinks();
}

function toggleDropdownImportStudy(allCheckId, comboId, favoriteCheckId, suffix, isLocation) {
	var possibleValues;
	var showFavorite = $('#' + favoriteCheckId).is(':checked');
	var showAll = $('#' + allCheckId).is(':checked');
	var selectedVal = '';

	// get previously selected value
	if ($('#' + comboId).select2('data')) {
		selectedVal = $('#' + comboId).select2('data').id;
	}

	// reset select2 combo
	initializePossibleValuesCombo([], '#' + comboId, isLocation, null);

	// get possible values based on checkbox
	if (showFavorite) {
		if (showAll) {
			possibleValues = $('#allPossibleValuesFavoriteJson' + suffix).text();
		} else {
			possibleValues = $('#possibleValuesFavoriteJson' + suffix).text();
		}
		$('#' + comboId).parent().find('.selectedValue').val(selectedVal);
		selectedVal = $('#' + comboId).parent().find('.selectedValueFave').val();
	} else if (showAll) {
		possibleValues = $('#allPossibleValuesJson' + suffix).text();
		$('#' + comboId).parent().find('.selectedValueFave').val(selectedVal);
		selectedVal = $('#' + comboId).parent().find('.selectedValue').val();
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
	var showFavorite = $('#' + getJquerySafeId('study-level-method-checkbox')).is(':checked');
	var selectedVal = '';
	var filterMethod = $('#' + getJquerySafeId('filterMethods')).is(':checked');
	var allMethod = $('#' + getJquerySafeId('allMethods')).is(':checked');

	// get previously selected value
	if ($('#' + getJquerySafeId('studyLevelVariables' + rowIndex + '.value')).select2('data')) {
		selectedVal = $('#' + getJquerySafeId('studyLevelVariables' + rowIndex
			+ '.value')).select2('data').id;
	}

	// reset select2 combo
	initializePossibleValuesCombo([], '#'
			+ getJquerySafeId('studyLevelVariables' + rowIndex + '.value'),
			false, null);


	// get possible values based on checkbox

	if (showFavorite && !allMethod) {
		possibleValues = $('#possibleValuesFavoriteJson' + rowIndex).text();
		$($('#' + getJquerySafeId('studyLevelVariables' + rowIndex + '.value')).parent().find('.selectedValue')).val(selectedVal);
		selectedVal = $($('#' + getJquerySafeId('studyLevelVariables' + rowIndex + '.value')).parent().find('.selectedValueFave')).val();
	} else if (allMethod && !showFavorite) {
		possibleValues = $('#allValuesJson' + rowIndex).text();
		$($('#' + getJquerySafeId('studyLevelVariables' + rowIndex + '.value')).parent().find('.selectedValue')).val(selectedVal);
		selectedVal = $($('#' + getJquerySafeId('studyLevelVariables' + rowIndex + '.value')).parent().find('.selectedValueFave')).val();
	} else if (allMethod && showFavorite) {
		possibleValues = $('#allFavoriteValuesJson' + rowIndex).text();
		$($('#' + getJquerySafeId('studyLevelVariables' + rowIndex + '.value')).parent().find('.selectedValue')).val(selectedVal);
		selectedVal = $($('#' + getJquerySafeId('studyLevelVariables' + rowIndex + '.value')).parent().find('.selectedValueFave')).val();
	} else {
		possibleValues = $('#possibleValuesJson' + rowIndex).text();
		$($('#' + getJquerySafeId('studyLevelVariables' + rowIndex + '.value')).parent().find('.selectedValue')).val(selectedVal);
		selectedVal = $($('#' + getJquerySafeId('studyLevelVariables' + rowIndex + '.value')).parent().find('.selectedValueFave')).val();
	}


	// recreate select2 combo
	initializePossibleValuesCombo($.parseJSON(possibleValues), '#'
			+ getJquerySafeId('studyLevelVariables' + rowIndex + '.value'),
			false, selectedVal);
}

function toggleLocationDropdown(rowIndex) {

	var possibleValuesAsText;
	var previousSelectedValue;
	var selectedValueId = '';

	var showFavorite = $('#' + getJquerySafeId('studyLevelVariables'+ rowIndex
		+ '.favorite1')).is(':checked');
	var showAll = true;
	var filterLocations = $('#' + getJquerySafeId('filterLocations')).is(':checked');
	var allLocations = $('#' + getJquerySafeId('allLocations')).is(':checked');

	// If Select2 combobox is initialized, then get the selected value.
	if ($('#' + getJquerySafeId('studyLevelVariables' + rowIndex + '.value')).select2('data')) {
		previousSelectedValue = $('#' + getJquerySafeId('studyLevelVariables' + rowIndex + '.value')).select2('data');
		selectedValueId = (previousSelectedValue !== undefined) ? previousSelectedValue.id : '';
	} else {
		// If Select2 is not yet initialized, that means the page has just loaded, in this case, get the selected value
		// from the saved value in the database
		selectedValueId = $($('#' + getJquerySafeId('studyLevelVariables' + rowIndex + '.value')).parent().find('.selectedValueFave')).val();
	}

	// Reset select2 combo
	initializePossibleValuesCombo([], '#'
			+ getJquerySafeId('studyLevelVariables' + rowIndex + '.value'),
				false, null);

	// Get possible values based on checkbox
	if (showFavorite && !allLocations) {
		possibleValuesAsText = $('#possibleValuesFavoriteJson' + rowIndex).text();
	} else if (allLocations && !showFavorite) {
		possibleValuesAsText = $('#allValuesJson' + rowIndex).text();
	} else if (allLocations && showFavorite) {
		possibleValuesAsText = $('#allFavoriteValuesJson' + rowIndex).text();
	} else {
		possibleValuesAsText = $('#possibleValuesJson' + rowIndex).text();
	}

	// Convert the possible values text as JSON
	var possibleValuesAsObject = $.parseJSON(possibleValuesAsText);

	if (selectedValueId !== '') {

		// Get all possible values of the location
		var allPossibleValuesOfLocation = $.parseJSON($('#allValuesJson' + rowIndex).text());

		// Find the item of the previously selected value from the list of all possibleValues of location
		var selectedValueFoundInAllPossibleValuesOfLocation =
			allPossibleValuesOfLocation.filter(function(item) { return item.id == selectedValueId; });

		// So that we can add it to the possible values list that will be displayed in the re-populated Select2 combobox.
		// This is to make sure that the selected item in dropdown remain selected even when the favorites checkbox or
		// location type radio button is ticked
		if (selectedValueFoundInAllPossibleValuesOfLocation.length !== 0) {
			addToListIfNotExisting(selectedValueFoundInAllPossibleValuesOfLocation[0], possibleValuesAsObject);
		}

	}

	// Recreate select2 combo
	initializePossibleValuesCombo(possibleValuesAsObject, '#'
			+ getJquerySafeId('studyLevelVariables' + rowIndex + '.value'), showAll, selectedValueId);
}

function addToListIfNotExisting(possibleValueToAdd, possibleValues) {

	var found = possibleValues.filter(function(item) { return item.id == possibleValueToAdd.id; });

	if (found.length === 0) {
		possibleValues.push(possibleValueToAdd);
	}


}

function createTableSettingVariables(data, name, tableId, varType) {
	$.each(
		data,
		function(index, settingDetail) {
		var length = $('#' + tableId + ' tbody tr').length+1;
		var className = length % 2 == 1 ? 'even': 'odd';
		var rowClass = '';
		if (varType == 3) {
			rowClass = 'baseline-traits';
		}
		var newRow = '<tr class="newVariable ' + rowClass + '">';
		var isDelete = '';

		if (settingDetail.deletable) {
			isDelete = '<input class="remove-indv-btn"'
				+ ' type="checkbox" data-variable-type="'+varType+'"'
				+ ' data-cv-term-id="'+settingDetail.variable.cvTermId + '"/>';
		}
		newRow = newRow
				+ '<td style="text-align: center" class="fbk-delete-link '
				+ className + '">' + isDelete
				+ '<input class="cvTermIds" type="hidden" id="'
				+ name + (length - 1)
				+ '.variable.cvTermId" name="' + name + '['
				+ (length - 1) + '].variable.cvTermId" value="'
				+ settingDetail.variable.cvTermId + '" />'
				+ '</td>';
		newRow = newRow
				+ '<td width="45%" class="'
				+ className
				+ '"><a href="javascript: void(0);" onclick="javascript:showBaselineTraitDetailsModal('
			+ settingDetail.variable.cvTermId + ',' + varType
				+ ');" ><span class="var-names">' + settingDetail.variable.name
				+ '</span></a></td>';
		newRow = newRow + '<td width="50%" class="' + className
				+ '">' + settingDetail.variable.description
				+ '</td></tr>';
		$('#' + tableId).append(newRow);
	});
	checkNurseryIfShowRemoveVariableLinks();
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
	var isBreedingMethodSetting = $(name).parent().next().children('.breeding-method-tooltip').length > 0;

	$.each(
		possibleValues,
		function(index, value) {
			var jsonVal;
			if (isBreedingMethodSetting && value.id !== undefined) {
				jsonVal = {
					'id': value.key,
					'text': value.description,
					'description': value.name
				};
			} else if (value.id !== undefined) {
				jsonVal = {
					'id': value.key,
					'text': value.description
				};
			} else if (value.locid !== undefined) {
				var locNameDisplay = value.lname;
				if (value.labbr != null && value.labbr != '') {
					locNameDisplay  += ' - (' + value.labbr + ')';
				}
				jsonVal = {
					'id': value.locid,
					'text': locNameDisplay
				};
			} else {
				jsonVal = {
					'id': value.mid,
					'text': value.mname + (value.mcode !== undefined ? ' - ' + value.mcode : ''),
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

	if (isLocation) {
		$(name).select2(
				{
					minimumResultsForSearch: (possibleValues_obj != null && possibleValues_obj.length != 0) ? 20 : -1,
					query: function(query) {
						var data = {
							results: possibleValues_obj
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

        // Set default harvest location id and name
        if (defaultJsonVal != null) {
            $('#' + getJquerySafeId('harvestLocationId')).val(defaultJsonVal.id);
            $('#' + getJquerySafeId('harvestLocationName')).val(defaultJsonVal.text);
        }
	} else if ($(name).parent().next().children('.breeding-method-tooltip').length > 0) {
		$(name).select2(
				{
					minimumResultsForSearch: (possibleValues_obj != null && possibleValues_obj.length != 0) ? 20: -1,
					query: function(query) {
						var data = {
							results: possibleValues_obj
						};
						// return the array that matches
						data.results = $.grep(data.results, function(item,
								index) {
							return ($.fn.select2.defaults.matcher(query.term,
									item.text));

						});
						query.callback(data);
					}
				}).on('change', function() {
					if ($('.breeding-method-tooltip')) {
						$('.breeding-method-tooltip').attr('title', $(name).select2('data').description);
						$('.help-tooltip-nursery').tooltip('destroy');
						$('.help-tooltip-nursery').tooltip();
						if ($(name).select2('data') != null && $(name).select2('data').id === '0') {
							$('.breeding-method-tooltip').addClass('fbk-hide');
						} else {
							$('.breeding-method-tooltip').removeClass('fbk-hide');
						}
					}
				});
	} else {
		var minResults = (possibleValues_obj.length > 0) ? 20: -1;

		$(name).select2(
				{
					minimumResultsForSearch: minResults,
					query: function(query) {
						var data = {
							results: possibleValues_obj
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
	}
	if (defaultJsonVal != null) {
		$(name).select2('data', defaultJsonVal).trigger('change');
	}


	// override the select2 open event to hide the selected item in the dropdown list
	$(name).off("select2-open");
	$(name).on("select2-open", function () {

			// get values of selected option
			var values = $(this).val();
			// get the pop up selection
			var popupSelection = $('.select2-result.select2-highlighted');

			if (values !== null && values !== '') {
				// hide the selected value
				popupSelection.hide();

			} else {
				// show all the selection value
				popupSelection.show();
			}
		}
	);
}

function checkMeasurementData(variableType, variableId) {
	var hasData = '0';
	$.ajax({
		url: '/Fieldbook/NurseryManager/editNursery/checkMeasurementData/'
				+ variableType + '/' + variableId,
		cache: false,
		type: 'GET',
		async: false,
		success: function(data) {
			hasData = data.hasMeasurementData;
		}
	});
	return hasData;
}
function deleteMultiVariable(variableType, variableIds, sectionDiv) {
	'use strict';
	var hasMeasurementData = '0';
	if (variableType == selectionVariatesSegment
			|| variableType == baselineTraitsSegment) {
		hasMeasurementData = checkMeasurementData(variableType, variableIds);
	}
	// if no data for measurement rows is saved yet, proceed with delete
	if (hasMeasurementData === '0') {
		// remove row from UI
		$('.' + sectionDiv + ' .remove-indv-btn:checked').each(function() {
			$(this).parent().parent().remove();
		});
		checkShowSettingsFormReminder();
		// remove row from session
		$.ajax({
			url: '/Fieldbook/NurseryManager/createNursery/deleteVariable/'
					+ variableType + '/' + variableIds,
			cache: false,
			type: 'POST',
			success: function() {
			}
		});

		// add dummy row to selection variates/traits if no record is left
		if (variableType === baselineTraitsSegment && $('#baselineTraitSettings tbody tr').length === 0) {
			showDummyRow('baselineTraitSettings');
		} else if (variableType === selectionVariatesSegment && $('#selectionVariatesSettings tbody tr').length === 0) {
			showDummyRow('selectionVariatesSettings');
		}

		// reinstantiate counters of ids and names
		sortVariableIdsAndNames(variableType);
		inputChange = true;

		// disable actions if applicable
		if (!$('.fbk-float-right-with-top-pos .dropdown-toggle').hasClass('disabled')) {
			document.initialEnabledStat = true;
		} else {
			document.initialEnabledStat = false;
		}

		if (document.disableActions !== undefined) {
			document.disableActions();
			eval($(this).attr('onclick'));
		}
		checkNurseryIfShowRemoveVariableLinks();
		return true;
	} else {
		// show confirmation popup
		$('#variateDeleteConfirmationModal').modal({
			backdrop: 'static',
			keyboard: false
		});
		$('#varToDelete').val(variableIds);
		$('#variableType').val(variableType);
		$('#variateDeleteConfirmationModal').data('section-delete', sectionDiv);
		buttonToDelete = null;

		return false;
	}
}

function proceedWithDelete() {
	var variableId = $('#varToDelete').val();
	var variableType = parseInt($('#variableType').val(), 10);
	var deleteButton = buttonToDelete;

	// remove row from UI
	if ($('#variateDeleteConfirmationModal').data('section-delete') !== '') {
		var sectionDiv = $('#variateDeleteConfirmationModal').data('section-delete');
		$('.' + sectionDiv + ' .remove-indv-btn:checked').each(function() {
			$(this).parent().parent().remove();
		});
	} else if (deleteButton !== null) {
		deleteButton.parent().parent().remove();
	}

	// remove row from session
	$.ajax({
		url: '/Fieldbook/NurseryManager/createNursery/deleteVariable/'
				+ variableType + '/' + variableId,
		cache: false,
		type: 'POST',
		success: function() {
		}
	});
	// add dummy row to selection variates/traits if no record is left
	if (variableType === baselineTraitsSegment && $('#baselineTraitSettings tbody tr').length === 0) {
		showDummyRow('baselineTraitSettings');
	} else if (variableType === selectionVariatesSegment && $('#selectionVariatesSettings tbody tr').length === 0) {
		showDummyRow('selectionVariatesSettings');
	}

	// reinstantiate counters of ids and names
	sortVariableIdsAndNames(variableType);
	inputChange = true;
	checkNurseryIfShowRemoveVariableLinks();
}

function recreateDynamicFieldsAfterDelete(name, tableId, rowClass, posValSuffix) {
	var reg = new RegExp(name + '[0-9]+', 'g');
	var reg2 = new RegExp(name + '\[[0-9]+\]', 'g');
	var breedingMethodIndex = getBreedingMethodRowIndex();
	$.each(
		$('.' + rowClass),
		function(index, row) {
			if (index >= breedingMethodIndex && name === 'studyLevelVariables') {
				index++;
			}
			// get currently selected value of select2 dropdown
			var selectedVal = null;
			var oldSelect2 = row.innerHTML.match(reg)[0];
			if ($('#' + getJquerySafeId(oldSelect2 + '.value'))
				.select2('data') && row.innerHTML.indexOf('select2') > -1) {
				selectedVal = $('#' + getJquerySafeId(oldSelect2
					+ '.value')).select2('data').id;
			} else {
				selectedVal = $('#' + getJquerySafeId(oldSelect2 + '.value')).val();
			}
			// if dropdown is for location or method, check if show
			// favorite is checked
			var isFavoriteChecked = '';
			if ($('#' + getJquerySafeId(oldSelect2 + '.favorite1')).length != 0) {
				isFavoriteChecked = $('#' + getJquerySafeId(oldSelect2 + '.favorite1'))
				.is(':checked');
			}
			// change the ids and names of the objects
			row.innerHTML = row.innerHTML
					.replace(reg, name + index);
			row.innerHTML = row.innerHTML.replace(reg2, name + '['
					+ index + ']');
			// delete the existing select2 object and recreate the
			// select2 combo and checkbox/links for location/method
			if (row.innerHTML.indexOf('select2') > -1) {
				recreateSelect2Combo(index, row, selectedVal,
						isFavoriteChecked, name, posValSuffix);
			} else if (row.innerHTML.indexOf('spinner-input') > -1) {
				recreateSpinnerInput(index, row, selectedVal, name);
			} else if (row.innerHTML.indexOf('date-input') > -1) {
				recreateDateInput(index, row, selectedVal, name);
			} else {
				$('#' + getJquerySafeId(name + index + '.value')).val(selectedVal);
			}
		});
	initializeDateAndSliderInputs();
}

function resetIdsOfTables(name, tableId) {
	var reg = new RegExp(name + '[0-9]+', 'g');
	var reg2 = new RegExp(name + '\[[0-9]+\]', 'g');
	var bodyContent = '', rowContent = '';
	$.each($('#' + tableId + ' tbody tr'), function(index, row) {
		rowContent = row.innerHTML.replace(reg, name + index);
		rowContent = rowContent.replace(reg2, name + '[' + index + ']');
		bodyContent = bodyContent + ' <tr> ' + rowContent + '</tr>';
	});
	$('#' + tableId + ' tbody').html(bodyContent);
}

function sortVariableIdsAndNames(variableType) {
	switch (variableType) {
		case studyLevelDetailsType:
		recreateDynamicFieldsAfterDelete('studyLevelVariables',
				'nurseryLevelSettings-dev', 'nurseryLevelSettings', '');
		break;
		case plotLevelDetailsType:
		resetIdsOfTables('plotLevelVariables', 'plotLevelSettings');
		break;
		case baselineTraitsSegment:
		resetIdsOfTables('baselineTraitVariables', 'baselineTraitSettings');
		checkTraitsAndSelectionVariateTable('', false);
		break;
		case selectionVariatesSegment:
		resetIdsOfTables('selectionVariatesVariables',
				'selectionVariatesSettings');
		checkTraitsAndSelectionVariateTable('', false);
		break;
		case nurseryConditionsType:
		recreateDynamicFieldsAfterDelete('nurseryConditions',
				'nurseryConditionsSettings', 'nurseryConditionsSettings',
				'Cons');
		break;
	default:

	}
}

function recreateDateInput(index, row, selectedVal, name) {
	'use strict';
	var newCell = '<input placeholder="yyyy-mm-dd" type="text" id="' + name + index + '.value" name="'
			+ name + '[' + index + '].value" ' + 'value="' + selectedVal
			+ '" class="form-control date-input" />';
	newCell += '<label for="'
			+ name
			+ index
			+ '.value" class="btn datepicker"><img  src="/Fieldbook/static/img/calendar.png" style="padding-bottom:3px;" /></label>';

	$($(row).find('.2nd')).html(newCell);
}

function recreateSpinnerInput(index, row, selectedVal, name) {
	var newCell = '<input  maxlength="250"  type="text" id="'
			+ name
			+ index
			+ '.value" name="'
			+ name
			+ '['
			+ index
			+ '].value" '
			+ 'data-min="'
			+ $($(row).find('.2nd').children('input.spinner-input'))
					.data('min')
			+ '" data-max="'
			+ $($(row).find('.2nd').children('input.spinner-input'))
					.data('max')
			+ '" data-step="'
			+ $($(row).find('.2nd').children('input.spinner-input')).data(
					'step') + '" value="' + selectedVal
			+ '" class="form-control spinner-input spinnerElement" />';

	$($(row).find('.2nd')).html(newCell);
}

function recreateSelect2Combo(index, row, selectedVal, isFavoriteChecked, name,
		posValSuffix) {
	// get the possible values of the variable
	var possibleValuesJson = $(
			$(row).find('.possibleValuesJson' + posValSuffix)).text();
	var possibleValuesFavoriteJson = $(
			$(row).find('.possibleValuesFavoriteJson' + posValSuffix)).text();
	var cvTermId = $(
			$(row).find('.1st')
				.find('#' + getJquerySafeId(name + index + '.variable.cvTermId')))
				.val();

	// hidden field for select2
	var newCell = '<input type="hidden" id="' + name + index + '.value" name="'
			+ name + '[' + index + '].value" class="form-control select2" />';

	// div containing the possible values
	newCell = newCell + '<div id="possibleValuesJson' + posValSuffix + index
			+ '" class="possibleValuesJson' + posValSuffix
			+ '" style="display:none">' + possibleValuesJson + '</div>';

	// div containing the favorite possible values
	if (possibleValuesFavoriteJson !== '') {
		newCell = newCell + '<div id="possibleValuesFavoriteJson'
				+ posValSuffix + index + '" class="possibleValuesFavoriteJson'
				+ posValSuffix + '" style="display:none">'
				+ possibleValuesFavoriteJson + '</div>';
	}

	// div containing checkbox and label for location and method
	var methodName = 'toggleMethodDropdown';
	var favoriteLabel = showFavoriteMethodLabel;
	var managePopupLabel = manageMethodLabel;
	var manageMethodName = 'openManageMethods';
	var isChecked = '';
	var showAll = true;

	// set possibleValues to favorite possible values
	if (isFavoriteChecked) {
		possibleValuesJson = possibleValuesFavoriteJson;
		isChecked = 'checked="checked"';
		showAll = false;
	}

	// set values for location
	if (parseInt(cvTermId, 10) === parseInt(locationId, 10)) {
		methodName = 'toggleLocationDropdown';
		favoriteLabel = showFavoriteLocationLabel;
		managePopupLabel = manageLocationLabel;
		manageMethodName = 'openManageLocations';
	}

	// add checkbox and manage location/method links
	if (parseInt(cvTermId, 10) === parseInt(breedingMethodId, 10)
			|| parseInt(cvTermId, 10) === parseInt($('#breedingMethodCode').val(), 10)
			|| parseInt(cvTermId, 10) === parseInt(locationId, 10)) {
		newCell = newCell
				+ '<div class="possibleValuesDiv"><input type="checkbox" id="'
				+ name + index + '.favorite1"' + ' name="' + name + '[' + index
				+ '].favorite"' + ' onclick="javascript: ' + methodName + '('
				+ index + ');" ' + isChecked + ' />'
				+ '<input type="hidden" name="_' + name + '[' + index
				+ '].favorite" value="on" /> ' + '<span>&nbsp;&nbsp;'
				+ favoriteLabel + '</span></div>';

		newCell = newCell + '<span><a href="javascript: ' + manageMethodName
				+ '();">' + managePopupLabel + '</a></span>';
	}

	$($(row).find('.2nd')).html(newCell);

	// recreate the select2 object
	if (parseInt(cvTermId, 10) === parseInt(locationId, 10)) {
		initializePossibleValuesCombo($.parseJSON(possibleValuesJson), '#'
				+ getJquerySafeId(name + index + '.value'), showAll,
				selectedVal);
	} else {
		initializePossibleValuesCombo($.parseJSON(possibleValuesJson), '#'
				+ getJquerySafeId(name + index + '.value'), false, selectedVal);
	}
}

function hideDeleteConfirmation() {
	$('#delete-settings-confirmation').modal('hide');
}

function clearSettings() {

	$.ajax({
		url: '/Fieldbook/NurseryManager/createNursery/clearSettings',
		type: 'GET',
		cache: false,
		success: function(html) {
			$('#chooseSettingsDiv').html(html);
			$('.chs-add-variable-factor').show();
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
		showInvalidInputMessage(name + ' ' + nurseryNumericError);
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
	return '<input data-slider-orientation="horizontal" data-slider-selection="after" type="text" data-min="'
			+ minVal
			+ '" data-max="'
			+ maxVal
			+ '" id="'
			+ name
			+ ctr
			+ '.value" name="'
			+ name
			+ '['
			+ ctr
			+ '].value" class="form-control numeric-input" />';
}
function createDropdownInput(ctr, name) {
	return '<input type="hidden" id="' + name + ctr + '.value" name="' + name
			+ '[' + ctr + '].value" class="form-control select2" />'
			+ '<input class="selectedValue" type="hidden" />'
			+ '<input class="selectedValueFave" type="hidden" />';
}
function createDateInput(ctr, name) {
	return '<input placeholder="yyyy-mm-dd" type="text" id="'
			+ name
			+ ctr
			+ '.value" name="'
			+ name
			+ '['
			+ ctr
			+ '].value" class="form-control date-input" />'
			+ '<label for="'
			+ name
			+ ctr
			+ '.value" class="btn datepicker"><img src="/Fieldbook/static/img/calendar.png" style="padding-bottom:3px;" /></label>';

}
function createNumericalTextInput(ctr, name) {
	return '<input  maxlength="250" type="text" id="' + name + ctr + '.value" name="' + name
			+ '[' + ctr + '].value" class="form-control numeric-input" />';
}
function createCharacterTextInput(ctr, name) {
	return '<input  maxlength="250" type="text" id="' + name + ctr + '.value" name="' + name
			+ '[' + ctr + '].value" class="form-control character-input" />';
}

function initializeDateAndSliderInputs() {
	if ($('.date-input').length > 0) {
		$('.date-input').placeholder().each(function() {
			$(this).datepicker({
				'format': 'yyyy-mm-dd'
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
						minimum: parseFloat($(this).data('min')),
						maximum: parseFloat($(this).data('max')),
						step: parseFloat($(this).data('step')),
						value: currentVal,
						numberOfDecimals: 4
					});
				});
	}
}

function loadNurserySettingsForCreate(templateSettingsId) {
	var $form = $('#createNurseryForm');

	var serializedData = $form.serialize();

	$.ajax({
		url: '/Fieldbook/NurseryManager/createNursery/view/'
				+ templateSettingsId,
		type: 'POST',
		data: serializedData,
		cache: false,
		timeout: 70000,
		success: function(html) {
			$('#chooseSettingsDiv').html(html);
		},
		error: function(jqXHR, textStatus, errorThrown) {
			console.log('The following error occured: ' + textStatus,
					errorThrown);
		}
	});
}

function openUsePreviousNurseryModal() {
	'use strict';
	$('#selectedNursery').select2('destroy');
	$('#selectedNursery').val('');
	$('#selectedNursery').select2({minimumResultsForSearch: 20});
	$('#usePreviousNurseryModal').modal('show');
}

function choosePreviousNursery(studyId) {
	'use strict';
	if ($('#chooseSettingsDiv').length !== 0) {
		url = '/Fieldbook/NurseryManager/createNursery/nursery/';
	}

	$.ajax({
		url: url + studyId,
		type: 'GET',
		cache: false,
		data: '',
		success: function(html) {
			if ($('#chooseSettingsDiv').length !== 0) {
				$('#chooseSettingsDiv').html(html);
			} else {
				$('.container .row').first().html(html);
			}
			if ($('#hasError').val() === '1' && $('#errorMessage').val() !== '') {
				createErrorNotification(errorMsgHeader, $('#errorMessage').val());
			}
			$('.chs-add-variable-factor').show();
		},
		complete: function() {
			url = '/Fieldbook/NurseryManager/createNursery/nursery/getChecks/';
			$.ajax({
				url: url + studyId,
				type: 'GET',
				cache: false,
				data: '',
				success: function(html) {
					if ($('#specifyCheckSection').length !== 0) {
						$('#specifyCheckSection').html(html);
					}
				}
			});
		}
	});
}
function isStudyName_Unique() {
	'use strict';
	var studyId = '0';
	if ($('#createNurseryMainForm #studyId').length !== 0) {
		studyId = $('#createNurseryMainForm #studyId').val();
		// we dont need to call the is name unique again since its not editable
		// anymore in edit
		return true;
	}

	var studyName = $.trim($('#' + getJquerySafeId('studyName')).val());

	return isStudyNameUnique(studyName, studyId);
}
function isCheckParametersEditable() {
	'use strict';
	return !$('#'+getJquerySafeId('checkVariables1.value')).prop('disabled');
}
function validateCreateNursery() {
	var hasError = false
		,name = ''
		,customMessage = ''
		,studyNameId = $('#studyNameTermId').val();

	var startDate = $('#' + getJquerySafeId('startDate')).val();
	if ($.trim($('#' + getJquerySafeId('studyName')).val()) === '') {
		hasError = true;
		name = 'Name';
	}else if ($.trim($('#' + getJquerySafeId('description')).val()) === '') {
		hasError = true;
		name = 'Description';
	}else if (isStudyName_Unique() === false) {
		hasError = true;
		customMessage = 'Name should be unique';
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
	} else if ($('.check-germplasm-list-items tbody tr').length > 0 && $('.germplasm-list-items tbody tr').length === 0) {
		hasError = true;
		customMessage = nurseryGermplasmListIsRequired;
    }

	var invalidDateMsg = validateAllDates();
	if (invalidDateMsg !== '') {
		hasError = true;
		customMessage = invalidDateMsg;
	}
	if (hasError) {
		var errMsg = '';
		if (name !== '') {
			errMsg = name.replace('*', '').replace(':', '') + ' ' + studyFieldsIsRequired;
		}
		if (customMessage !== '') {
			errMsg = customMessage;
		}

		showInvalidInputMessage(errMsg);
		return false;
	}

	var valid = nurseryValidateStartEndDateBasic();

	if (!valid) {
		return false;
	}

	$.each($('.numeric-input'), function(index, textField) {
		if (isNaN($(textField).val())) {
			hasError = true;
			if ($(this).parent().prev().hasClass('variable-tooltip')) {
				name = $(this).parent().prev().data('original-title');
			}else {
				name = $(this).parent().prev().find('.control-label').html();
			}
			customMessage = name + ' ' + valueNotNumeric;
		}
	});

	$.each($('.numeric-input'), function(index, textField) {
		if (parseFloat($(textField).val()) > $(textField).data('max')
				|| parseFloat($(textField).val()) < $(textField).data('min')) {
			hasError = true;
			if ($(this).parent().prev().hasClass('variable-tooltip')) {
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
	if ($('.check-germplasm-list-items tbody tr').length != 0 && selectedCheckListDataTable !== null && selectedCheckListDataTable.getDataTable() !== null
			&& (($('#chooseGermplasmAndChecks').data('replace') !== undefined && parseInt($('#chooseGermplasmAndChecks').data('replace')) === 1)
					|| ($('#studyId').length === 0 ) || isCheckParametersEditable())) {

		selectedCheckListDataTable.getDataTable().$('.check-hidden').serialize();

		if (selectedCheckListDataTable.getDataTable().$('.check-hidden').length > 0) {
			// we validate only if there is a check
			// we try to validate if all the check row has check
			var hasCheckError = false;
			var checkStartIndex = $('#' + getJquerySafeId('checkVariables0.value')).val();
			var checkInterval = $('#' + getJquerySafeId('checkVariables1.value')).val();
			selectedCheckListDataTable.getDataTable().$('.check-hidden').each(function() {
				if ($(this).val() === '') {
					hasCheckError = true;
				}
			});

			if (hasCheckError == true) {
				showInvalidInputMessage(selectedCheckError);
				return false;
			}
			if (isInt(checkStartIndex) === false) {
				showInvalidInputMessage(startIndexWholeNumberError);
				return false;
			}
			if (isInt(checkInterval) === false) {
				showInvalidInputMessage(intervalWholeNumberError);
				return false;
			}
			var totalGermplasms = $('#totalGermplasms').val();
			if (parseInt(checkStartIndex, 10) < 0
					|| parseInt(checkStartIndex, 10) > totalGermplasms) {
				showInvalidInputMessage(startIndexLessGermplasmError);
				return false;
			}

			if (parseInt(checkInterval, 10) < 0) {
				showInvalidInputMessage(checkIntervalGreaterThanZeroError);
				return false;
			}
		}
	}

	return true;
}


function nurseryValidateStartEndDateBasic() {
	var startDate = $('#' + getJquerySafeId('startDate')).val();
	var endDate = $('#' + getJquerySafeId('endDate')).val();

	var returnVal =  (validateStartEndDateBasic(startDate, endDate));
	if (returnVal === true) {
		return true;
	} else {
		showInvalidInputMessage(returnVal);
		return false;
	}

}

function recreateModalMethodCombo(comboName, comboFaveCBoxName, url) {
	var selectedMethodAll = $('#methodIdAll').val();
	var selectedMethodDerivativeAndMaintenance = $('#methodIdDerivativeAndMaintenance').val();
	var selectedMethodDerivativeAndMaintenanceFavorite = $('#methodIdDerivativeAndMaintenanceFavorite').val();
	var selectedMethodFavorite = $('#methodIdFavorite').val();

	$.ajax({
		url: url || '/Fieldbook/breedingMethod/getBreedingMethods',
		type: 'GET',
		cache: false,
		data: '',
		async: false,
		success: function(data) {
			if (data.success == '1') {
				if (selectedMethodAll != null) {
					// recreate the select2 combos to get updated list of
					// methods
				
					if (data.favoriteNonGenerativeMethods && data.favoriteNonGenerativeMethods.length > 0) {
						$('#showFavoriteMethod').prop('checked', true);
					} else {
						$('#showFavoriteMethod').prop('checked', false);
					}

					recreateMethodComboAfterClose('methodIdAll', data.allMethods);
					recreateMethodComboAfterClose('methodIdFavorite', data.favoriteMethods);
					recreateMethodComboAfterClose('methodIdDerivativeAndMaintenance', data.allNonGenerativeMethods);
					recreateMethodComboAfterClose('methodIdDerivativeAndMaintenanceFavorite', data.favoriteNonGenerativeMethods);
					showCorrectMethodCombo();
					// set previously selected value of method
					if ($('#showFavoriteMethod').prop('checked')) {
						if ($('#showDerivativeAndMaintenanceRadio').prop('checked')) {
							setComboValues(methodSuggestionsDerivativeAndMaintenanceFavorites_obj, selectedMethodDerivativeAndMaintenanceFavorite, 'methodIdDerivativeAndMaintenanceFavorite');
						} else {
							setComboValues(methodSuggestionsFav_obj, selectedMethodFavorite, 'methodIdFavorite');
						}
					} else if ($('#showDerivativeAndMaintenanceRadio').prop('checked')) {
						setComboValues(methodSuggestionsDerivativeAndMaintenance_obj, selectedMethodDerivativeAndMaintenance, 'methodIdAll');
					} else {
						setComboValues(methodSuggestions_obj, selectedMethodAll, 'methodIdAll');
					}
				} else {
					var selectedVal = null;
					// get index of breeding method row
					var index = getBreedingMethodRowIndex();

					if ($('#' + getJquerySafeId(comboName)).select2('data')) {
						selectedVal = $('#' + getJquerySafeId(comboName))
								.select2('data').id;
					}
					// recreate select2 of breeding method
					initializePossibleValuesCombo([], '#'
							+ getJquerySafeId(comboName), false, selectedVal);

					// update values of combo
					if ($('#' + getJquerySafeId(comboFaveCBoxName)).is(
							':checked')) {
						initializePossibleValuesCombo($
								.parseJSON(data.favoriteNonGenerativeMethods), '#'
								+ getJquerySafeId(comboName), false,
								selectedVal);
					} else {
						initializePossibleValuesCombo(data.allNonGenerativeMethods, '#'
								+ getJquerySafeId(comboName), false,
								selectedVal);
					}

					if (index > -1) {
						replacePossibleJsonValues(data.allNonGenerativeMethods, data.favoriteNonGenerativeMethods, data.allMethods,
							data.favoriteMethods, index);
					}
				}
			} else {
				showErrorMessage('page-message', data.errorMessage);
			}

		}
	});
}
function plotMethod() {
	var $plotCheckBox = $('input[type=checkbox][name=allPlotsChoice]');

	if (!$plotCheckBox.length) {
		return;
	}

	if ($plotCheckBox.is(':checked')) {
		$('#plot-variates-section').hide();
	} else {
		$('#plot-variates-section').show();
		if ($('#plotVariateId').has('option').length === 0) {
			$('input[type=checkbox][name=allPlotsChoice]')
					.prop('checked', true);
			$('input[type=checkbox][name=allPlotsChoice]').change();
			showErrorMessage('page-advance-modal-message', noLineVariatesErrorTrial);
		}
	}
}

function refreshEditNursery() {
	$('#page-message').html('');
}

function displaySaveSuccessMessage(idDomSelector, messageToDisplay) {
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
		success: function(html) {
			$('#measurementsDiv').html(html);
			displayEditFactorsAndGermplasmSection();
			displaySaveSuccessMessage('page-message', saveSuccessMessage);
			refreshListDetails();
		}
	});
}

function recreateSessionVariablesForEntryNo() {
    'use strict';

    $.ajax({
        url: '/Fieldbook/NurseryManager/editNursery/recreate/session/variables',
        type: 'GET',
        data: '',
        cache: false,
        success: function(html) {
            $('#measurementsDiv').html(html);
            displayEditFactorsAndGermplasmSection();
        }
    });
}

// FIXME Should not be using global variables or functions
/*
 * global lastDraggedChecksList, Spinner, validateCreateNursery,
 * validateStartEndDate, moveToTopScreen
 */
/* global loadNurserySettingsForCreate, getJquerySafeId, changeBuildOption */
function refreshStudyAfterSave(studyId) {
	'use strict';

	displaySaveSuccessMessage('page-message', saveSuccessMessage);

	setTimeout(function() {
		window.location = '/Fieldbook/NurseryManager/editNursery/' + studyId;
	}, 600);
}

function resetDesigConfirmationFields() {
	'use strict';
	// reset dropdowns and fields
	$('#importLocationId').select2('data', null);
	$('#importMethodId').select2('data', null);
	$('#nameType').select2('data', {'id': $('#nameType option:first').val(), 'text':$('#nameType option:first').html()});
	$('#importDate').val('');
	$('#confirmation-page-message').html('');
}

function validateGermplasmInput(importDate, importLocationId, importMethodId) {
	'use strict';
	if ($('#import-action-type').val() === '2' || $('#import-action-type').val() === '1') {
		if (importDate === '') {
			showInvalidInputMessage(importDateRequired);
			return false;
		}else if (importLocationId === '' || importLocationId === null) {
			showInvalidInputMessage(importLocationRequired);
			return false;
		}else if ((importMethodId === '' || importMethodId === null) && $('#import-action-type').val() === '2') {
			showInvalidInputMessage(importMethodRequired);
			return false;
		}
	}
	return true;
}

function submitGermplasmAndCheck() {
	'use strict';

	$('#lastDraggedChecksList').val(lastDraggedChecksList);

	var $form = $('#germplasm-list-form,#specify-checks-form'),
		serializedData = $form.serialize() + '&lastDraggedChecksList=' + lastDraggedChecksList;
	if ($('.check-germplasm-list-items tbody tr').length != 0 && selectedCheckListDataTable !== null && selectedCheckListDataTable.getDataTable() !== null) {
		//we need to move to 1st page so the serialize would work properly
		selectedCheckListDataTable.getDataTable().fnDraw();
		serializedData += '&' + selectedCheckListDataTable.getDataTable().$('.check-hidden').serialize();
	}

    serializedData += '&columnOrders=' + (BMS.Fieldbook.MeasurementsTable.getColumnOrdering('measurement-table'));
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
function addFakeCheckTable() {
	'use strict';
	if ($('.germplasm-list-items tbody tr').length > 0 && $('.check-germplasm-list-items tbody tr').length == 0 && $('#check-germplasm-list .fake-check-germplasm-list-items tbody tr').length == 0) {
		// we add the fake table
		$('.fake-check-germplasm-list-items').clone().removeClass('fbk-hide').appendTo('#check-germplasm-list');
	}else if ($('.germplasm-list-items tbody tr').length === 0 && $('#check-germplasm-list .fake-check-germplasm-list-items tbody tr').length == 1) {
		// we remove if there are no nursery check and the selected check is
		// fake
		$('#check-germplasm-list .fake-check-germplasm-list-items').remove();
	}
}
function checkShowSettingsFormReminder() {
	'use strict';
	// we check management details if there are entries
	if ($('.nurseryLevelSettings .1st').length === 0) {
		$('.management-details-section-reminder').removeClass('fbk-hide');
	} else {
		$('.management-details-section-reminder').addClass('fbk-hide');
	}

	if ($('.nurseryConditionsSettings .1st').length === 0) {
		$('.constants-section-reminder').removeClass('fbk-hide');
	} else {
		$('.constants-section-reminder').addClass('fbk-hide');
	}
}
function discardImportedData() {
	$('#discardImportDataConfirmation').modal({
		backdrop: 'static',
		keyboard: true
	});
}

function discardImportedStockList(){
	$('#discardImportStockListDataConfirmation').modal({
		backdrop: 'static',
		keyboard: true
	});
}

function checkFavoritesIfValIsAFavorite(rowIndex) {
	'use strict';
	
	var valueOfDropdown = parseInt($('#' + getJquerySafeId('studyLevelVariables' + rowIndex + '.value')).val(), 10);
	var possibleValuesElm = $('#possibleValuesFavoriteJson' + rowIndex);
	var possibleValuesFavoriteArr = $.parseJSON(possibleValuesElm.text());
	
	if (valueOfDropdown) {
		$('.filter_selectors_' + rowIndex).prop("checked", false);
		$('.filter_selectors_all_' + rowIndex).prop("checked", true);
		return;
	}

	if ('' === possibleValuesElm.text().trim()) {
		return false;
	}
	if (possibleValuesFavoriteArr) {
		$.each(possibleValuesFavoriteArr, function(index, val) {
			if (val.id > 0) {
				$('[name="' + getJquerySafeId('studyLevelVariables[' + rowIndex + '].favorite') + '"]').click();
				return false;
			}
		});
	}
}
function checkNurseryIfShowRemoveVariableLinks() {
	'use strict';
	$('.remove-all-section .remove-all-vars').each(function() {
		var sectionDiv = $(this).data('section');
		var availableCheckboxes = $('.' + sectionDiv + ' .remove-indv-btn');
		if (availableCheckboxes.length !== 0) {
			//we show the remove all
			$(this).parents('.remove-all-section').removeClass('fbk-hide');
			$(this).siblings('.remove-btn').prop('checked', false);
		} else {
			//we hide it
			$(this).parents('.remove-all-section').addClass('fbk-hide');
		}
	});
}

function selectedLocation(location, possibleValues) {
	selectedLocationForTrial = location;
	possibleLocationsForTrial = possibleValues;
}


function setSelectedLocation() {
    //Trial passes preferred values in which location abbreviation available in bracket.
    //We need to split value to get actual abbreviation for selected location
	if (possibleLocationsForTrial != null && selectedLocationForTrial != null && selectedLocationForTrial != '' &&
			selectedLocationForTrial.id != undefined) {
		$('#' + getJquerySafeId('harvestLocationId')).val(selectedLocationForTrial.id);
		var locationName = $.grep(possibleLocationsForTrial, function(e) {
			return e.key == selectedLocationForTrial.id;
		});
		$('#' + getJquerySafeId('harvestLocationName')).val(locationName[0].name);
		var locAbbreviation = locationName[0].name.split("(");
		locAbbreviation[1] = locAbbreviation[1].replace(")", '');
		$('#' + getJquerySafeId('harvestLocationAbbreviation')).val(locAbbreviation[1]);
	}
}
