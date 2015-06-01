/*global $:false */
var StockIDFunctions = window.StockIDFunctions;

if (typeof StockIDFunctions === 'undefined') {
    StockIDFunctions = {
        defaultSeparator: '-',

        openGenerateStockIDModal: function (sourceId, hasPedigreeDupe, hasPlotReciprocal, hasPedigreeReciprocal) {
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
                $('.withPedigreeDupeOnly').hide();
            } else {
                $('.withPedigreeDupeOnly').show();
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

        saveStockList: function () {
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
                    success: function (result) {
                        if (result.isSuccess === '1') {
                            StockIDFunctions.generateStockListTabIfNecessary(listId).done(function() {
                                $('#generateStockIDModal').modal('hide');
                                // logic for displaying the stock list immediately after successful saving
                                $('#create-nursery-tabs .tab-pane.info').removeClass('active');
                                $('.advance-germplasm-items').removeClass('active');
                                $(this).data('has-loaded', '1');
                                StockIDFunctions.displayStockList(listId);
                                StockIDFunctions.disableGenerateStockListButton(listId);
                                $('#stock-tab-pane' + listId).addClass('active');
                                $('#stock-list' + listId + '-li').addClass('active');
                                $('.nav-tabs').tabdrop('layout');
                            });


                        } else {
                            showErrorMessage('', result.errorMessage);
                        }
                    }
                }
            );

        },

        disableGenerateStockListButton : function(listId) {
            var sectionContainerDiv = 'advance-list'+listId;
            if ($('#' + sectionContainerDiv).data('has-stock') === 'true') {
                $('#generateStockListMenuItem' + listId).parent('ul').remove();
                $('#generateStockListMenuItem' + listId).remove();
                $('#listActionButton' + listId).addClass('disabled');

            }
        },

        generateStockListTabIfNecessary: function (listId) {
            'use strict';

            var url = '/Fieldbook/stock/generateStockTabIfNecessary/' + listId;

            return $.ajax({
                url: url,
                type: 'GET',
                cache: false,
                success: function (html) {
                    if (html && html.length > 0) {
                        $('#advance-list' + listId + '-li').after(html);
                        $('#advance-list' + listId).data('has-stock', 'true');
                        $('#stock-list-anchor' + listId).on('shown.bs.tab', function () {
                            if ($(this).data('has-loaded') !== '1') {
                                $(this).data('has-loaded', '1');
                                StockIDFunctions.displayStockList($(this).data('list-id'));
                            }
                        });
                    }
                }
            });
        },

        displayStockList: function (listId) {
            'use strict';

            var url = '/Fieldbook/germplasm/list/stock/' + listId;

            return $.ajax({
                url: url,
                type: 'GET',
                cache: false,
                success: function(html) {
                    $('#stock-content-pane' + listId).html(html);
                    //we just show the button
                    $('.export-advance-list-action-button').removeClass('fbk-hide');
                    $('#stock-list' + listId+'-li').addClass('advance-germplasm-items');
                    $('#stock-list' + listId+'-li').data('advance-germplasm-list-id', listId);
                }
            });
        },

        displayStockListInventoryView : function(listId) {
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
                    $('#stock-list' + listId+'-li').addClass('advance-germplasm-items');
                    $('#stock-list' + listId+'-li').data('advance-germplasm-list-id', listId);
                }
            });
        },

        closeStockList: function (listId) {
            'use strict';
            $('li#stock-list' + listId + '-li').remove();

            if ($('#stock-list-anchor' + listId).length === 1) {
                $('#stock-list-anchor' + listId).remove();
            }

            setTimeout(function() {
                $('#create-nursery-tab-headers li:eq(0) a').tab('show');
                $('.nav-tabs').tabdrop('layout');
            }, 100);
        },

        openSaveListModal: function () {
            'use strict';

            $('#generateStockIDModal').modal('hide');

            var germplasmTreeNode = $('#germplasmFolderTree').dynatree('getTree');
            $.ajax(
                {
                    url: '/Fieldbook/ListTreeManager/saveStockList/',
                    type: 'GET',
                    cache: false,
                    success: function (html) {
                        $('#saveListTreeModal').data('is-save-stock', '1');
                        $('#saveListTreeModal').data('sourceListId', $('#stockIDModalSourceListID').val());
                        $('#saveGermplasmRightSection').html(html);
                        setTimeout(function () {
                            $('#saveListTreeModal').modal({backdrop: 'static', keyboard: true});
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

        updateLabels: function () {
            'use strict';
            StockIDFunctions.retrieveNextStockIDPrefix().done(function (nextPrefix) {
                $('#samplePrefixLabel').html(nextPrefix);
                $('#nextStockIDLabel').html(nextPrefix + StockIDFunctions.defaultSeparator + '1');
            });
        },

        retrieveNextStockIDPrefix: function () {
            'use strict';
            var stockGenerationSettings = {
                // use the default identifier of SID when user provides no value
                breederIdentifier : $('#breederIdentifierField').val().trim() === '' ? 'SID': $('#breederIdentifierField').val().trim(),
                // default separator for now is -
                separator : StockIDFunctions.defaultSeparator
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
                    contentType : 'application/json',
                    data : JSON.stringify(stockGenerationSettings),
                    success: function (result) {
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
        	var url =  $("#stock-tab-pane"+stockId+" #label-printing-url").attr('href') + "/" + stockId;
    		location.href = url;
        },
        
        exportList: function(stockId) {		
    		'use strict';		
    		var formName = 'exportStockForm';
    		$('#'+formName+' #exportStockListId').val(stockId);
    		$('#'+formName).ajaxForm({dataType: 'text', success: StockIDFunctions.showExportResponse}).submit();
    	},
    	
    	showExportResponse: function(responseText, statusText, xhr, $form) {
    		'use strict';
    		var resp = $.parseJSON(responseText);
    		var formName = 'exportAdvanceStudyDownloadForm';
    		$('#'+formName+' #outputFilename').val(resp.outputFilename);
    		$('#'+formName+' #filename').val(resp.filename);
    		$('#'+formName+' #contentType').val(resp.contentType);
    		$('#'+formName).submit();
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
    			error: StockIDFunctions.importListErrorCallBack,
    		}).submit();
    	},
    	
    	importListErrorCallBack : function() {
    		showErrorMessage('','Import Failed');
    	},
    	
    	importListSuccessCallBack : function(responseText) {
    		var resp = $.parseJSON(responseText);
    		if (resp.hasError) {
				showErrorMessage('',resp.errorMessage);
			} else {
				showSuccessfulMessage('','Import Success');
				$('.import-stock-section .modal').modal('hide');
				StockIDFunctions.displayStockList(resp.stockListId);
			}
    	},
    
    	getSelectedInventoryEntryIds : function(){
    		'use strict';
    		var ids = [],
    			listDivIdentifier  = getCurrentAdvanceTabTempIdentifier(),
    			sectionContainerDiv = 'stock-tab-pane'+listDivIdentifier;
    		$('#'+sectionContainerDiv + ' .stockListEntryId:checked').each(function(){
    			ids.push($(this).data('entryid'));
    		});
    		return ids;
    	},
    	
    	
    	
    	showUpdateInventoryModal : function(listId){
    		'use strict';
    		var entryIds = StockIDFunctions.getSelectedInventoryEntryIds();
    		if(entryIds.length === 0){
    			showErrorMessage('page-message', germplasmSelectError);
    			moveToTopScreen();
    			return;		
    		}
    		
    		var entryIdList = entryIds.join();
    		
    		$.ajax({
    			url: '/Fieldbook/stock/ajax/'+getCurrentAdvanceTabTempIdentifier() + '/' + entryIdList,
    			type: 'GET',
    			cache: false,
    		    success: function(data) {
    		    	$('#addLotsModalDiv').html(data);	    	
    		    	$('#comments').val('');
    		    	$('#amount').val('');
    		    	$('#page-message-lots').html('');
    		    	$('#addLotsModal').modal({ backdrop: 'static', keyboard: true });
    		    	initializePossibleValuesComboInventory(inventoryLocationSuggestions, '#inventoryLocationIdAll', true, null);
    		    	initializePossibleValuesComboInventory(inventoryFavoriteLocationSuggestions, '#inventoryLocationIdFavorite', false, null);
    		  	  	initializePossibleValuesComboScale(scaleSuggestions, '#inventoryScaleId', false, null);
    		  	    showCorrectLocationInventoryCombo();
    		    }
    		});
    	},
    	
    	updateInventory : function(){
    		'use strict';
    		var entryIds = StockIDFunctions.getSelectedInventoryEntryIds();
    		$('#entryIdList').val(entryIds);
    		if($('#showFavoriteLocationInventory').is(':checked')){
    			if($('#'+getJquerySafeId('inventoryLocationIdFavorite')).select2('data') !== null){
    				$('#inventoryLocationId').val($('#'+getJquerySafeId('inventoryLocationIdFavorite')).select2('data').id);
    			}
    		}else{
    			if($('#'+getJquerySafeId('inventoryLocationIdAll')).select2('data') !== null){
    				$('#inventoryLocationId').val($('#'+getJquerySafeId('inventoryLocationIdAll')).select2('data').id);
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
    		} else if(isFloatNumber($('#amount').val()) === false || parseFloat($('#amount').val()) < 0) {
    			showInvalidInputMessage(inventoryAmountPositiveRequired);
    			moveToTopScreen();
    		}  else if($('#inventoryComments').val().length > 250) {
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
                url: '/Fieldbook/stock/executeBulkingInstructions/'+listId,
                type: 'POST',
                cache: false,
                success: function (resp) {
                	if (resp.hasError) {
        				showErrorMessage('',resp.errorMessage);
        			} else {
        				showSuccessfulMessage('','Bulking duplicates and reciprocals completed');
        				StockIDFunctions.displayStockList(resp.stockListId);
        			}
                }
            });
    	}
    };
};

$(document).ready(function() {
	$('.btn-import-stock').off('click');
	$('.btn-import-stock').on('click', StockIDFunctions.importList);
	$('.import-stock-section .modal').on('hide.bs.modal', function() {
		$('div.import-stock-file-upload').parent().parent().removeClass('has-error');
	});
});