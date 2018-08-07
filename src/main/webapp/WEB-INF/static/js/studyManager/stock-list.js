/*global $:false */
var StockIDFunctions = window.StockIDFunctions;

if (typeof StockIDFunctions === 'undefined') {
	StockIDFunctions = {
		defaultSeparator: '-',

		openGenerateStockIDModal: function(sourceId, hasPedigreeDupe, hasPlotReciprocal, hasPedigreeReciprocal) {
			'use strict';
			$('#generateStockIDModal').modal({backdrop: 'static', keyboard: true});
			$('#stockIDModalSourceListID').val(sourceId);

			// the following lines re-initializes the popup modal to clear their values
			$('#breederIdentifierField').val('');
			$('#samplePrefixLabel').html('');
			$('#nextStockIDLabel').html('');
			$('#generateStockIDForm input[name=addPedigreeDuplicate][value=false]').prop('checked', 'checked');
			$('#generateStockIDForm input[name=addPlotReciprocal][value=false]').prop('checked', 'checked');
			$('#generateStockIDForm input[name=addPedigreeReciprocal][value=false]').prop('checked', 'checked');

			// the following lines enable / disable bulk instruction related items depending on the cross list
			if (!(hasPedigreeDupe || hasPlotReciprocal || hasPedigreeReciprocal)) {
				$('.withDupeReciprocalOnly').hide();
			} else {
				$('.withDupeReciprocalOnly').show();
			}

			if (!hasPedigreeDupe) {
				$('.withPedigreeDuplicateOnly').hide();
			} else {
				$('.withPedigreeDuplicateOnly').show();
			}

			if (!hasPlotReciprocal) {
				$('.withPlotReciprocalOnly').hide();
			} else {
				$('.withPlotReciprocalOnly').show();
			}

			if (!hasPedigreeReciprocal) {
				$('.withPedigreeReciprocalOnly').hide();
			} else {
				$('.withPedigreeReciprocalOnly').show();
			}
		},

		saveStockList: function() {
			'use strict';
			var breederIdentifier = $('#breederIdentifierField').val().trim();
			
			if(!StockIDFunctions.validatePrefix(breederIdentifier)){
				showErrorMessage('', stockIdGenerationPrefixError);
				return;
			}
			
			var stockGenerationSettings = {
				// use the default identifier of SID when user provides no value
				breederIdentifier: $('#breederIdentifierField').val().trim() === '' ? 'SID' : $('#breederIdentifierField').val().trim(),
				// default separator for now is -
				separator: StockIDFunctions.defaultSeparator,
				addPedigreeDuplicate: $('#generateStockIDForm input[name=addPedigreeDuplicate]:checked').val(),
				addPlotReciprocal: $('#generateStockIDForm input[name=addPlotReciprocal]:checked').val(),
				addPedigreeReciprocal: $('#generateStockIDForm input[name=addPedigreeReciprocal]:checked').val()
			};

			var listId = $('#stockIDModalSourceListID').val();

			$.ajax(
				{
					headers: {
						'Accept': 'application/json',
						'Content-Type': 'application/json'
					},
					url: '/Fieldbook/stock/generateStockList/' + $('#stockIDModalSourceListID').val(),
					type: 'POST',
					cache: false,
					contentType: 'application/json',
					data: JSON.stringify(stockGenerationSettings),
					success: function(result) {
						if (result.isSuccess === '1') {
							StockIDFunctions.generateStockListTabIfNecessary(listId).done(function() {
								$('#generateStockIDModal').modal('hide');
								// logic for displaying the stock list immediately after successful saving
								$('.advance-germplasm-items').removeClass('active');
								$(this).data('has-loaded', '1');
								StockIDFunctions.displayStockList(listId);
								StockIDFunctions.disableGenerateStockListButton(listId);
								$('#stock-tab-pane' + listId).addClass('active');
								$('#stock-list' + listId + '-li').addClass('active');
                                // FIXME: To be done to bifurcate context of Nursery and Trial
								//$('.nav-tabs').tabdrop('layout');
							});

						} else {
							showErrorMessage('', result.errorMessage);
						}
					}
				}
			);

		},

		disableGenerateStockListButton: function(listId) {
			var sectionContainerDiv = 'advance-list' + listId;
			if ($('#' + sectionContainerDiv).data('has-stock') === 'true') {
				$('#generateStockListMenuItem' + listId).parent('ul').remove();
				$('#generateStockListMenuItem' + listId).remove();
				$('#listActionButton' + listId).addClass('disabled');

			}
		},

		generateStockListTabIfNecessary: function (listId, isPageLoading) {
			'use strict';

			var url = '/Fieldbook/stock/generateStockTabIfNecessary/' + listId;

			return $.ajax({
				url: url,
				type: 'GET',
				cache: false,
				success: function (html) {
					if (html && html.length > 0) {
						// Display already generated Stock List
						StockIDFunctions.displayStockList(listId, isPageLoading);

					}
				}
			});
		},

		displayStockList: function (listId, isPageLoading) {
			'use strict';
			var url = '/Fieldbook/germplasm/list/stock/' + listId;

			return $.ajax({
				url: url,
				type: 'GET',
				cache: false,
				success: function (html) {
					$('#stock-content-pane' + listId).html(html);
		            //we just show the button
		            $('.export-advance-list-action-button').removeClass('fbk-hide');
		            $('#stock-list' + listId + '-li').addClass('advance-germplasm-items');
		            $('#stock-list' + listId + '-li').data('advance-germplasm-list-id', listId);
					var element = angular.element(document.getElementById("mainApp")).scope();
					// To apply scope safely
					element.safeApply(function () {
						element.addStockTabData(listId, html, 'stock-list', isPageLoading);
					});

				}
			});
		},

		displayStockListInventoryView: function(listId) {
			'use strict';

			var url = '/Fieldbook/germplasm/list/stockinventory/' + listId;

			return $.ajax({
				url: url,
				type: 'GET',
				cache: false,
				success: function(html) {
					$('#stock-content-pane' + listId).html(html);
					//we just show the button
					$('.export-advance-list-action-button').removeClass('fbk-hide');
					$('#stock-list' + listId + '-li').addClass('advance-germplasm-items');
					$('#stock-list' + listId + '-li').data('advance-germplasm-list-id', listId);
				}
			});
		},

		closeStockList: function(listId) {
			'use strict';
			$('li#stock-list' + listId + '-li').remove();

			if ($('#stock-tab-pane' + listId).length === 1) {
				$('#stock-tab-pane' + listId).remove();
			}

			setTimeout(function() {
				$('#create-nursery-tab-headers li:eq(0) a').tab('show');
				$('.nav-tabs').tabdrop('layout');
			}, 100);
		},

		openSaveListModal: function() {
			'use strict';

			$('#generateStockIDModal').modal('hide');

			var germplasmTreeNode = $('#germplasmFolderTree').dynatree('getTree');
			$.ajax(
				{
					url: '/Fieldbook/ListTreeManager/saveStockList/',
					type: 'GET',
					cache: false,
					success: function(html) {
						$('#saveListTreeModal').data('is-save-stock', '1');
						$('#saveListTreeModal').data('sourceListId', $('#stockIDModalSourceListID').val());
						$('#saveGermplasmRightSection').html(html);
						setTimeout(function() {
							$('#saveListTreeModal').modal({backdrop: 'static', keyboard: true});
							TreePersist.preLoadGermplasmTreeState(false, '#germplasmFolderTree', true);
						}, 300);
						//we preselect the program lists
						if (germplasmTreeNode !== null && germplasmTreeNode.getNodeByKey('LISTS') !== null) {
							germplasmTreeNode.getNodeByKey('LISTS').activate();
							germplasmTreeNode.getNodeByKey('LISTS').expand();
						}
					}
				}
			);
		},

		updateLabels: function() {
			'use strict';
			var breederIdentifier = $('#breederIdentifierField').val().trim();
			
			if(!StockIDFunctions.validatePrefix(breederIdentifier)){
				showErrorMessage('', stockIdGenerationPrefixError);
			} else {
				StockIDFunctions.retrieveNextStockIDPrefix().done(function(nextPrefix) {
					$('#samplePrefixLabel').html(nextPrefix);
					$('#nextStockIDLabel').html(nextPrefix + StockIDFunctions.defaultSeparator + '1');
				});
			}
		},
		
		validatePrefix : function(prefix){
			var patt = new RegExp("^[A-Za-z]*$");
			return patt.test(prefix); 
		},

		retrieveNextStockIDPrefix: function() {
			'use strict';
			var stockGenerationSettings = {
				// use the default identifier of SID when user provides no value
				breederIdentifier: $('#breederIdentifierField').val().trim() === '' ? 'SID' : $('#breederIdentifierField').val().trim(),
				// default separator for now is -
				separator: StockIDFunctions.defaultSeparator
			};

			var deferred = $.Deferred();

			$.ajax(
				{
					headers: {
						'Accept': 'application/json',
						'Content-Type': 'application/json'
					},
					url: '/Fieldbook/stock/retrieveNextStockPrefix',
					type: 'POST',
					cache: false,
					contentType: 'application/json',
					data: JSON.stringify(stockGenerationSettings),
					success: function(result) {
						if (result.isSuccess === '1') {
							deferred.resolve(result.prefix);
						} else {
							showErrorMessage('', result.errorMessage);
							deferred.reject(result);
						}
					}
				}
			);

			return deferred;
		},

		createLabels: function(stockId) {
			'use strict';
			var url =  $("#stock-content-pane" + stockId + " #label-printing-url").attr('href') + "/" + stockId;
			location.href = url;
		},

		exportList: function(stockId) {
			'use strict';
			var formName = 'exportStockForm';
			$('#' + formName + ' #exportStockListId').val(stockId);
			$('#' + formName).ajaxForm({dataType: 'text', success: StockIDFunctions.showExportResponse}).submit();
		},

		showExportResponse: function(responseText, statusText, xhr, $form) {
			'use strict';
			var resp = $.parseJSON(responseText);
			var formName = 'exportAdvanceStudyDownloadForm';
			$('#' + formName + ' #outputFilename').val(resp.outputFilename);
			$('#' + formName + ' #filename').val(resp.filename);
			$('#' + formName + ' #contentType').val(resp.contentType);
			$('#' + formName).submit();
		},

		showImportPopup: function(listId) {
			$('#import-stock-list-id').val(listId);
			$('#fileupload-import-stock').val('');
			$('.import-stock-section .modal').modal({ backdrop: 'static', keyboard: true });
			$('.import-stock-section .modal .fileupload-exists').click();
		},

		importList: function() {
			'use strict';

			if ($('#fileupload-import-stock').val() === '') {
				showErrorMessage('', 'Please choose a file to import');
				return false;
			}
			$('#importStockListUploadForm').ajaxForm({
				dataType: 'text',
				success: StockIDFunctions.importListSuccessCallBack,
				error: StockIDFunctions.importListErrorCallBack
			}).submit();
		},

		importListErrorCallBack: function() {
			showErrorMessage('','Import Failed');
		},

		importListSuccessCallBack: function(responseText) {
			var resp = $.parseJSON(responseText);
			if (resp.hasError) {
				showErrorMessage('', resp.errorMessage);
			} else {
                stockListImportNotSaved = true;
				if(resp.hasConflict){
					$('.fbk-save-nursery').addClass('fbk-hide');
					$('.fbk-save-stocklist').removeClass('fbk-hide');
					$('.fbk-discard-imported-stocklist-data').removeClass('fbk-hide');
					showAlertMessage('', importStocklistSuccessOverwriteDataWarningToSaveMessage);
				}
				else{
					showSuccessfulMessage('', 'Import Success');
				}
				$('.import-stock-section .modal').modal('hide');
                // Display Discard Imported Data button after successful import of stock list
                $('.fbk-discard-imported-stocklist-data').removeClass('fbk-hide');
				StockIDFunctions.displayStockList(resp.stockListId);
			}
		},

		getSelectedInventoryEntryIds: function() {
            'use strict';
            var ids = [],
                listDivIdentifier  = getCurrentAdvanceTabTempIdentifier(),
                inventoryTableId = '#inventory-table' + listDivIdentifier;
                var oTable = $(inventoryTableId).dataTable({retrieve: true, searching: false});
                var nodes = oTable.api().rows(':has(input.stockListEntryId:checked)').nodes();
                $(nodes).each(function (i, node) {
                    ids.push($('input.stockListEntryId:checked', node).data('entryid'));
                });
            return ids;
		},

		showUpdateInventoryModal: function(listId) {
			'use strict';
			var entryIds = StockIDFunctions.getSelectedInventoryEntryIds();
			if (entryIds.length === 0) {
				showErrorMessage('page-message', germplasmSelectError);
				moveToTopScreen();
				return;
			}

			$.ajax({
				url: '/Fieldbook/stock/ajax/' + getCurrentAdvanceTabTempIdentifier(),
				type: 'POST',
				data: JSON.stringify(entryIds),
				cache: false,
				contentType: "application/json; charset=utf-8",
				success: function(data) {
					$('#addLotsModalDiv').html(data);
					$('#comments').val('');
					$('#amount').val('');
					$('#page-message-lots').html('');
					$('#addLotsModal').modal({ backdrop: 'static', keyboard: true });
					initializePossibleValuesComboInventory(inventoryLocationSuggestions, '#inventoryLocationIdAll', true, null);
					initializePossibleValuesComboInventory(inventorySeedStorageLocationSuggestions, '#inventoryLocationIdSeedStorage', false, null);
					initializePossibleValuesComboInventory(inventoryFavoriteLocationSuggestions, '#inventoryLocationIdFavorite', false, null);
					initializePossibleValuesComboInventory(inventoryFavoriteSeedStorageLocationSuggestions, '#inventoryLocationIdFavoriteSeedStorage', false, null);
					initializePossibleValuesComboScale(scaleSuggestions, '#inventoryScaleId', false, null);
					showCorrectLocationInventoryCombo();
				}
			});
		},

		updateInventory: function() {
			'use strict';
			var entryIds = StockIDFunctions.getSelectedInventoryEntryIds();
			$('#entryIdList').val(entryIds);
			if ($('#showFavoriteLocationInventory').is(':checked')) {
				if ($('#showAllLocationInventory').is(':checked')) {
					if ($('#inventoryLocationIdFavorite').select2('data')) {
						$('#inventoryLocationId').val($('#inventoryLocationIdFavorite').select2('data').id);
					}
				} else if ($('#inventoryLocationIdFavoriteSeedStorage').select2('data')) {
					$('#inventoryLocationId').val($('#inventoryLocationIdFavoriteSeedStorage').select2('data').id);
				}
			} else {
				if ($('#showAllLocationInventory').is(':checked')) {
					if ($('#inventoryLocationIdAll').select2('data')) {
						$('#inventoryLocationId').val($('#inventoryLocationIdAll').select2('data').id);
					}
				} else if ($('#inventoryLocationIdSeedStorage').select2('data')) {
					$('#inventoryLocationId').val($('#inventoryLocationIdSeedStorage').select2('data').id);
				}
			}
			if ($('#inventoryLocationId').val() === '0' || $('#inventoryLocationId').val() === '') {
				showInvalidInputMessage(locationRequired);
				moveToTopScreen();
			} else if (!$('#inventoryScaleId').select2('data')) {
				showInvalidInputMessage(scaleRequired);
				moveToTopScreen();
			} else if ($('#amount').val() === '') {
				showInvalidInputMessage(inventoryAmountRequired);
				moveToTopScreen();
			} else if (isFloatNumber($('#amount').val()) === false || parseFloat($('#amount').val()) < 0) {
				showInvalidInputMessage(inventoryAmountPositiveRequired);
				moveToTopScreen();
			}  else if ($('#inventoryComments').val().length > 250) {
				showInvalidInputMessage(commentLimitError);
				moveToTopScreen();
			} else {
				// update lots
				var serializedData = $('#add-plot-form').serialize();

				$.ajax({
					url: '/Fieldbook/stock/update/lots',
					type: 'POST',
					data: serializedData,
					success: function(data) {
						if (data.success === 1) {
							showSuccessfulMessage('page-message', data.message);
							$('#addLotsModal').modal('hide');
							StockIDFunctions.displayStockList(data.listId);
						} else {
							showErrorMessage('page-message-lots', data.message);
						}
					}
				});
			}
		},

		executeBulkingInstructions: function(listId) {
			$.ajax({
				url: '/Fieldbook/stock/executeBulkingInstructions/' + listId,
				type: 'POST',
				cache: false,
				success: function(resp) {
					if (resp.hasError) {
						showErrorMessage('', resp.errorMessage);
					} else {
						showSuccessfulMessage('', 'Bulking duplicates and reciprocals completed');
						StockIDFunctions.displayStockList(resp.stockListId);
					}
				}
			});
		}
	};
}

$(document).ready(function() {
	$('.btn-import-stock').off('click');
	$('.btn-import-stock').on('click', StockIDFunctions.importList);
	$('.import-stock-section .modal').on('hide.bs.modal', function() {
		$('div.import-stock-file-upload').parent().parent().removeClass('has-error');
	});
});
