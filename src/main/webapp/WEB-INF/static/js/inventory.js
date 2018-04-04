/*globals selectedGidsForAdvance, getJquerySafeId, Spinner, showErrorMessage, triggerInventoryTableSelection*/
/*globals initializePossibleValuesComboInventory, initializePossibleValuesComboScale, germplasmSelectError, inventoryLocationSuggestions*/
/*globals initializePossibleValuesComboScale, scaleSuggestions, initializePossibleValuesComboInventory*/
/*globals caleRequired, inventoryFavoriteLocationSuggestions, locationRequired*/
/*exported initializePossibleValuesComboInventory, triggerInventoryTableSelection, getCurrentAdvanceTabListIdentifier, initializePossibleValuesComboScale*/
function triggerInventoryTableSelection(tableName, sectionContainerDiv, listIdentifier) {
	'use strict';
	$('#' + sectionContainerDiv + ' #' + tableName + ' tr.primaryRow').on('click', function() {
		$(this).toggleClass('field-map-highlight');
		var gid = $(this).data('gid') + '';
		if (selectedGidsForAdvance[listIdentifier] == null) {
			selectedGidsForAdvance[listIdentifier] = [];
		}

		if ($(this).hasClass('field-map-highlight')) {
			selectedGidsForAdvance[listIdentifier][gid] = gid;
		} else {
			selectedGidsForAdvance[listIdentifier][gid] = null;
		}

	});
}
function isCurrentTabIdentifierAdvanced() {
	'use strict';
	if ($('#create-nursery-tab-headers .tabdrop').hasClass('active')) {
		//means the active is in the tab drop
		return $('#create-nursery-tab-headers .tabdrop li.active').hasClass('crosses-list');
	} else {
		return $('#create-nursery-tab-headers li.active').hasClass('crosses-list');
	}
}
function getCurrentAdvanceTabTempIdentifier() {
	return $('#manage-trial-tab-headers .active').children('a').attr('tab-data');
}
function getCurrentAdvanceTabListIdentifier() {
	'use strict';
	return $('#manage-trial-tab-headers .active').children('a').attr('tab-data');
}

function initializePossibleValuesComboScale(possibleValues, name, isLocation, defaultValue) {
	'use strict';
	var possibleValuesObj = [],
		defaultJsonVal = null;

	$.each(possibleValues, function(index, value) {
		var jsonVal = null;
		if (value.id !== undefined) {
			jsonVal = {
				'id': value.id,
				'text': value.displayName
			};
		}

		possibleValuesObj.push(jsonVal);
		if (defaultValue != null && defaultValue != '' &&
				((defaultValue == value.key || defaultValue == value.locid || defaultValue == value.mid) ||
				(defaultValue == value.name || defaultValue == value.lname || defaultValue == value.mname))) {
			defaultJsonVal = jsonVal;
		}

	});

	possibleValuesObj = sortByKey(possibleValuesObj, 'text');

	$(name).select2({
		minimumResultsForSearch: possibleValuesObj.length == 0 ? -1 : 20,
		query: function(query) {
			var data = {results: possibleValuesObj};
			// return the array that matches
			data.results = $.grep(data.results, function(item, index) {
				return ($.fn.select2.defaults.matcher(query.term, item.text));

			});
			query.callback(data);
		}
	});

	if (defaultJsonVal != null) {
		$(name).select2('data', defaultJsonVal).trigger('change');
	}
}
function initializePossibleValuesComboInventory(possibleValues, name, showAllLocation, defaultValue) {
	'use strict';
	var possibleValuesObj = [];
	var defaultJsonVal = null;

	$.each(possibleValues, function(index, value) {
		var jsonVal = null;
		if (value.id != undefined) {
			jsonVal = {
				'id': value.id,
				'text': value.name
			};
		} else if (value.locid != undefined) {
			var locNameDisplay = value.lname;
			if (value.labbr != null && value.labbr != '') {
				locNameDisplay  += ' - (' + value.labbr + ')';
			}
			jsonVal = {
				'id': value.locid,
				'text': locNameDisplay
			};
		}

		possibleValuesObj.push(jsonVal);
		if (defaultValue != null && defaultValue != '' &&
				((defaultValue == value.key || defaultValue == value.locid) ||
				(defaultValue == value.name || defaultValue == value.lname))) {
			defaultJsonVal = jsonVal;
		}

	});

	possibleValuesObj = sortByKey(possibleValuesObj, 'text');

	if (showAllLocation) {
		$(name).select2({
			minimumResultsForSearch: possibleValuesObj.length == 0 ? -1 : 20,
			query: function(query) {
				var data = {results: possibleValuesObj};
				// return the array that matches
				data.results = $.grep(data.results, function(item, index) {
					return ($.fn.select2.defaults.matcher(query.term, item.text));

				});

				query.callback(data);
			}
		}).on('change', function() {
			if ($('#' + getJquerySafeId('inventoryLocationId')).length !== 0) {
				$('#' + getJquerySafeId('inventoryLocationId')).val($('#' + getJquerySafeId('inventoryLocationIdAll')).select2(
					'data').id);
			}
		});
	} else {
		$(name).select2({
			minimumResultsForSearch: possibleValuesObj.length == 0 ? -1 : 20,
			query: function(query) {
				var data = {results: possibleValuesObj};
				// return the array that matches
				data.results = $.grep(data.results, function(item, index) {
					return ($.fn.select2.defaults.matcher(query.term, item.text));

				});

				query.callback(data);
			}
		}).on(
			'change',
			function() {
				if ($('#' + getJquerySafeId('inventoryLocationId')).length !== 0) {
					$('#' + getJquerySafeId('inventoryLocationId')).val($(name).select2('data').id);
				}
			});
	}

	if (defaultJsonVal != null) {
		$(name).select2('data', defaultJsonVal).trigger('change');
	}
}

function displayAdvanceGermplasmDetails(listId) {
	'use strict';
	$.ajax({
		url: '/Fieldbook/germplasm/list/advance/' + listId,
		type: 'GET',
		cache: false,
		success: function(html) {
			$('#advance-list' + getCurrentAdvanceTabTempIdentifier()).html(html);
		}
	});
}

function showCorrectLocationInventoryCombo() {
	'use strict';
	var isChecked = $('#showFavoriteLocationInventory').is(':checked');
	var seedStorageLocations = $('#showSeedStorageLocationInventory').is(':checked');
	var allLocations = $('#showAllLocationInventory').is(':checked');

	// if show favorite location is checked, hide all field locations, else, show only favorite locations
	$('#s2id_inventoryLocationIdFavorite').hide();
	$('#s2id_inventoryLocationIdAll').hide();
	$('#s2id_inventoryLocationIdSeedStorage').hide();
	$('#s2id_inventoryLocationIdFavoriteSeedStorage').hide();
	if (isChecked){
		if(seedStorageLocations) {
			$('#s2id_inventoryLocationIdFavoriteSeedStorage').show();
			if ($('#' + getJquerySafeId('inventoryLocationIdFavoriteSeedStorage')).select2('data') != null) {
				$('#' + getJquerySafeId('inventoryLocationId')).val($('#' + getJquerySafeId('inventoryLocationIdFavoriteSeedStorage')).select2('data').id);

			} else {
				$('#' + getJquerySafeId('inventoryLocationId')).val(0);
			}
		}else{
			$('#s2id_inventoryLocationIdFavorite').show();
			if ($('#' + getJquerySafeId('inventoryLocationIdFavorite')).select2('data') != null) {
				$('#' + getJquerySafeId('inventoryLocationId')).val($('#' + getJquerySafeId('inventoryLocationIdFavorite')).select2('data').id);

			} else {
				$('#' + getJquerySafeId('inventoryLocationId')).val(0);
			}
		}

	} else {
		if (allLocations) {
			$('#s2id_inventoryLocationIdAll').show();
			if ($('#' + getJquerySafeId('inventoryLocationIdAll')).select2('data') != null) {
				$('#' + getJquerySafeId('inventoryLocationId')).val($('#' + getJquerySafeId('inventoryLocationIdAll')).select2('data').id);

			} else {
				$('#' + getJquerySafeId('inventoryLocationId')).val(0);

			}

		} else {
			$('#s2id_inventoryLocationIdSeedStorage').show();
			if ($('#' + getJquerySafeId('inventoryLocationIdSeedStorage')).select2('data') != null) {
				$('#' + getJquerySafeId('inventoryLocationId')).val($('#' + getJquerySafeId('inventoryLocationIdSeedStorage')).select2('data').id);

			} else {
				$('#' + getJquerySafeId('inventoryLocationId')).val(0);

			}
		}
	}
}
