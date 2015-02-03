var ImportCrosses = {
		showPopup : function(){
			$('#fileupload-import-crosses').val('');
			$('.import-crosses-section .modal').modal({ backdrop: 'static', keyboard: true });
					
		},

		doSubmitImport : function() {
			'use strict';

			if ($('#fileupload-import-crosses').val() === '') {
				showErrorMessage('', 'Please choose a file to import');
				return false;
			}

			ImportCrosses.submitImport($('.import-crosses-section')).done(function(resp) {

				if (!resp.isSuccess) {
					createErrorNotification(crossingImportErrorHeader,resp.error.join('<br/>'));
					return;
				}

				$('.import-crosses-section .modal').modal('hide');

				// show review crosses page
				ImportCrosses.openCrossesList();

			});

		},

		openCrossesList : function() {
			'use strict';
			$('#openCrossesListModal').modal({ backdrop: 'static', keyboard: true });

			ImportCrosses.getImportedCrossesTable().done(function(response) {
				setTimeout(function() {
					new  BMS.Fieldbook.PreviewCrossesDataTable('#preview-crosses-table', response);
				},240);
			});

			$('#openCrossListNextButton').off('click');
			$('#openCrossListNextButton').on('click', function() {
				$('#openCrossesListModal').modal('hide');
				ImportCrosses.showImportSettingsPopup();
			});

		},

		getImportedCrossesTable : function(){
			'use strict';
			return $.ajax(
			{
				url: '/Fieldbook/import/crosses/getImportedCrossesList',
				type: 'GET',
				cache: false
			});
		},

		submitImport : function($importCrossesForm) {
			'use strict';

			var deferred = $.Deferred();

			$importCrossesForm.ajaxForm({
				dataType: 'json',
				success: function(response) {
					deferred.resolve(response);
				},
				error: function(response) {

					showErrorMessage(null,generalAjaxErrorMessage);

					deferred.reject(response);
				}
			}).submit();

			return deferred.promise();
		},	
		displayCrossesGermplasmDetails: function (listId) {
			'use strict';
			$.ajax({
				url: '/Fieldbook/SeedStoreManager/crosses/displayGermplasmDetails/' + listId,
				type: 'GET',
				cache: false,
				success: function(html) {
					$('.crosses-list' + getCurrentAdvanceTabTempIdentifier()).html(html);
				}
			});
		},

		showImportSettingsPopup : function() {
			var crossSettingsPopupModal = $('#crossSettingsModal');
			crossSettingsPopupModal.modal({ backdrop: 'static', keyboard: true });
			BreedingMethodsFunctions.processMethodDropdownAndFavoritesCheckbox('breedingMethodDropdown', 'showFavoritesOnlyCheckbox');
			ImportCrosses.processImportSettingsDropdown('presetSettingsDropdown', 'loadSettingsCheckbox');

			$('.cross-import-name-setting').off('change');
			$('.cross-import-name-setting').on('change', ImportCrosses.updateDisplayedSequenceNameValue);

			$('#parentageDesignationSeparator').off('change');
			$('#parentageDesignationSeparator').on('change', ImportCrosses.updateSampleParentageDesignation);
		},

		updateSampleParentageDesignation : function() {
			var value = $('#parentageDesignationSeparator').val();
			$('#sampleParentageDesignation').text('ABC-123' + value + 'DEF-456');
		},

		processImportSettingsDropdown : function(dropdownID, useSettingsCheckboxID) {

			ImportCrosses.retrieveAvailableImportSettings().done(function(settingList) {
				ImportCrosses.createAvailableImportSettingsDropdown(dropdownID, settingList);

				$('#' + getJquerySafeId(dropdownID)).on('change', function() {
					ImportCrosses.triggerImportSettingUpdate(settingList, dropdownID, useSettingsCheckboxID);
					// update the displayed sequence name value so that it makes use of the possibly new settings
					ImportCrosses.updateDisplayedSequenceNameValue();
					ImportCrosses.updateSampleParentageDesignation();
				});

				$('#' + getJquerySafeId(useSettingsCheckboxID)).on('change', function() {
					ImportCrosses.triggerImportSettingUpdate(settingList, dropdownID, useSettingsCheckboxID);

					// update the displayed sequence name value so that it makes use of the possibly new settings
					ImportCrosses.updateDisplayedSequenceNameValue();
					ImportCrosses.updateSampleParentageDesignation();
				});
			}).fail(function(data) {
			});
		},

		triggerImportSettingUpdate : function(settingList, dropdownID, useSettingsCheckboxID) {
			if ($('#' + getJquerySafeId(useSettingsCheckboxID)).is(':checked')) {
				var currentSelectedItem = $('#' + dropdownID).select2('val');

				$.each(settingList, function (index, setting) {
					if (setting.name === currentSelectedItem) {
						ImportCrosses.updateImportSettingsFromSavedSetting(setting);
					}
				})
			}
		},

		updateImportSettingsFromSavedSetting : function(setting) {

			$('#presetName').val(setting.name);
			$('#breedingMethodDropdown').select2('val', setting.breedingMethodID);
			$('#useSelectedMethodCheckbox').prop('checked', !setting.basedOnStatusOfParentalLines);

			$('#crossPrefix').val(setting.crossPrefix);
			$('#crossSuffix').val(setting.crossSuffix);
			$('input:radio[name=hasPrefixSpace][value=' + setting.hasPrefixSpace + ']').prop('checked', true);
			$('input:radio[name=hasSuffixSpace][value=' + setting.hasSuffixSpace + ']').prop('checked', true);
			$('#sequenceNumberDigits').val(setting.sequenceNumberDigits);
			$('#parentageDesignationSeparator').val(setting.parentageDesignationSeparator);
			$('#startingSequenceNumber').val(setting.startingSequenceNumber);
		},

		createAvailableImportSettingsDropdown : function(dropdownID, settingList) {
			var possibleValues = [];
			$.each(settingList, function(index, setting) {
				possibleValues.push(ImportCrosses.convertSettingToSelect2Item(setting));
			});

			$('#' + getJquerySafeId(dropdownID)).select2(
				{
					initSelection: function (element, callback) {
						$.each(possibleValues, function (index, value) {
							if (value.id == element.val()) {
								callback(value);
							}
						});
					},
					query: function (query) {
						var data = {
							results: possibleValues
						};
						// return the array that matches
						data.results = $.grep(data.results, function (item) {
							return ($.fn.select2.defaults.matcher(query.term,
								item.text));

						});
						query.callback(data);
					}
				});
		},

		convertSettingToSelect2Item : function(setting) {
			return {
				'id': setting.name,
				'text': setting.name,
				'description': setting.name
			};
		},

		retrieveAvailableImportSettings : function() {
			return $.ajax({
				url: '/Fieldbook/import/crosses/retrieveSettings',
				type: 'GET',
				cache: false
			});
		},

		submitCrossImportSettings : function() {
			var settingData = ImportCrosses.constructSettingsObjectFromForm();

			if (ImportCrosses.isCrossImportSettingsValid(settingData)) {
				var targetURL;
				if ($('#saveSettingsCheckbox').is(':checked')) {
					targetURL = '/Fieldbook/import/crosses/submitAndSaveSetting';
				} else {
					targetURL = '/Fieldbook/import/crosses/submit';
				}

				$.ajax({
					headers: {
						'Accept': 'application/json',
						'Content-Type': 'application/json'
					},
					url: targetURL,
					type: 'POST',
					cache: false,
					data: JSON.stringify(settingData),
					success: function (data) {
						if (data.success == '0') {
							alert('error');
						} else {
							$('#crossSettingsModal').modal('hide');
							ImportCrosses.openSaveListModal();
						}
					}
				});
			}

		},

		isCrossImportSettingsValid : function(importSettings) {
			var valid = true;
			if (!importSettings.crossNameSetting.prefix || importSettings.crossNameSetting.prefix === '') {
				valid = false;
				showErrorMessage('', 'Cross name prefix is required');
			}

			if (!importSettings.crossNameSetting.separator || importSettings.crossNameSetting.separator === '') {
				valid = false;
				showErrorMessage('', 'Separator for parentage designation is required');
			}

			return valid;
		},

		updateDisplayedSequenceNameValue : function() {
			ImportCrosses.retrieveNextNameInSequence().done(function(data){
				if (data.success === '1') {
					$('#importNextSequenceName').text(data.sequenceValue);
				} else {
					showErrorMessage('', ajaxGenericErrorMsg);
				}
			}).fail(function() {
				showErrorMessage('', ajaxGenericErrorMsg);
			});
		},

		retrieveNextNameInSequence : function() {
			var settingData = ImportCrosses.constructSettingsObjectFromForm();

			return $.ajax({
				headers: {
										'Accept': 'application/json',
										'Content-Type': 'application/json'
									},
				'url' : '/Fieldbook/import/crosses/generateSequenceValue',
				'type' : 'POST',
				'data' : JSON.stringify(settingData),
				'cache' : false
			});
		},

		constructSettingsObjectFromForm : function() {
			var settingObject = {};
			settingObject.name = $('#presetName').val();

			settingObject.breedingMethodSetting = {};
			settingObject.breedingMethodSetting.methodId = $('#breedingMethodDropdown').select2('val');

			if ( !settingObject.breedingMethodSetting.methodId || settingObject.breedingMethodSetting.methodId == '') {
				settingObject.breedingMethodSetting.methodId = null;
			}

			settingObject.breedingMethodSetting.basedOnStatusOfParentalLines = ! $('#useSelectedMethodCheckbox').is(':checked');

			settingObject.crossNameSetting = {};
			settingObject.crossNameSetting.prefix = $('#crossPrefix').val();
			settingObject.crossNameSetting.suffix = $('#crossSuffix').val();
			settingObject.crossNameSetting.addSpaceBetweenPrefixAndCode = $('input:radio[name=hasPrefixSpace]:checked').val() == 'true';
			settingObject.crossNameSetting.addSpaceBetweenSuffixAndCode= $('input:radio[name=hasSuffixSpace]:checked').val() == 'true';
			settingObject.crossNameSetting.numOfDigits = $('#sequenceNumberDigits').val();
			settingObject.crossNameSetting.separator = $('#parentageDesignationSeparator').val();
			settingObject.crossNameSetting.startNumber = $('#startingSequenceNumber').val();

			settingObject.additionalDetailsSetting = {};
			settingObject.additionalDetailsSetting.harvestLocationId = 0;
			settingObject.additionalDetailsSetting.harvestDate = '';

			return settingObject;

		},

		displayCrossesList: function (uniqueId, germplasmListId, listName, isDefault, crossesListId) {
			'use strict';
			var url = '/Fieldbook/SeedStoreManager/crosses/displayGermplasmDetails/' + germplasmListId;
			if(!isDefault){
				$('#advanceHref' + uniqueId + ' .fbk-close-tab').before(': [' + listName + ']');
				url += '?isSnapshot=0';
			}else{
				url += '?isSnapshot=1';
			}
			$.ajax({
				url: url,
				type: 'GET',
				cache: false,
				success: function(html) {
					$('.crosses-list' + uniqueId).html(html);
					$('.crosses-list' + uniqueId+'-li').addClass('crosses-germplasm-items');
					$('.crosses-list' + uniqueId+'-li').data('crosses-germplasm-list-id', crossesListId);
				}
			});
		},
		displayTabCrossesList: function (germplasmListId, crossesListId, listName) {
			'use strict';
			var url = '/Fieldbook/SeedStoreManager/crosses/displayGermplasmDetails/' + germplasmListId;
			url += '?isSnapshot=0';
			
			
			$.ajax({
				url: url,
				type: 'GET',
				cache: false,
				success: function(html) {
					//ImportCrosses.displayCrossesList($(this).data('list-id'), $(this).data('list-id'), '', true);
					
					$('#saveListTreeModal').modal('hide');
					$('#saveListTreeModal').data('is-save-crosses', '0');
					$('#create-nursery-tabs .tab-pane.info').removeClass('active');
					
					var uniqueId,
					close,
					aHtml;
					uniqueId = germplasmListId;
					close = '<i class="glyphicon glyphicon-remove fbk-close-tab" id="'+uniqueId+'" onclick="javascript: closeAdvanceListTab(' + uniqueId +')"></i>';
					aHtml = '<a id="advance-list'+uniqueId+'" role="tab" class="advanceList crossesList crossesList'+uniqueId+'" data-toggle="tab" href="#advance-list' + uniqueId + '" data-list-id="' + uniqueId + '">Crosses: [' + listName + ']' + close + '</a>';
					$('#create-nursery-tab-headers').append('<li id="advance-list' + uniqueId + '-li" class="advance-germplasm-items crosses-list">' + aHtml + '</li>');
					$('#create-nursery-tabs').append('<div class="tab-pane info crosses-list'+uniqueId+'" id="advance-list' + uniqueId + '">' + html + '</div>');
					$('a#advance-list'+uniqueId).tab('show');
					$('#advance-list'+uniqueId+'.tab-pane.info').addClass('active');
					$('.nav-tabs').tabdrop('layout');
					
					$('a#advance-list'+uniqueId).on('click', function(){
						$('#create-nursery-tabs .tab-pane.info').removeClass('active');
						$('#advance-list'+uniqueId+'.tab-pane.info').addClass('active');
					});
				}
			});
		},
		openSaveListModal: function(){
			'use strict';
			var  germplasmTreeNode = $('#germplasmFolderTree').dynatree('getTree');
			$.ajax(
				{ 
					url: '/Fieldbook/ListTreeManager/saveCrossesList/',
					type: 'GET',
					cache: false,
					success: function(html) {
						$('#saveGermplasmRightSection').html(html);
						$('#saveListTreeModal').modal({
							show: true,
							keyboard: true,
							backdrop: 'static'
						});
						$('#saveListTreeModal').data('is-save-crosses', '1');
						//we preselect the program lists
						if(germplasmTreeNode !== null && germplasmTreeNode.getNodeByKey('LOCAL') !== null) {
							germplasmTreeNode.getNodeByKey('LOCAL').activate();
							germplasmTreeNode.getNodeByKey('LOCAL').expand();
						}
					}
				}
			);
		}
};

$(document).ready(function() {
	$('.import-crosses').on('click', ImportCrosses.showPopup);
	$('.btn-import-crosses').on('click', ImportCrosses.doSubmitImport);
	$('.import-crosses-section .modal').on('hide.bs.modal', function() {
		$('div.import-crosses-file-upload').parent().parent().removeClass('has-error');
		$('.import-crosses-section .modal .fileupload-exists').click();
	});
});