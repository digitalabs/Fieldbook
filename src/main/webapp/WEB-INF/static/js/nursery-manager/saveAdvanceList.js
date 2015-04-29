/*globals displayGermplasmListTree, changeBrowseGermplasmButtonBehavior, additionalLazyLoadUrl, displayAdvanceList,saveGermplasmReviewError*/
/*globals $,showErrorMessage, showInvalidInputMessage, getDisplayedTreeName,ImportCrosses,listShouldNotBeEmptyError,getJquerySafeId,validateAllDates */
/*globals listParentFolderRequired, listNameRequired, listDescriptionRequired, listDateRequired, listTypeRequired, listNameDuplicateError, moveToTopScreen */
/*exported saveGermplasmList, openSaveListModal*/

var SaveAdvanceList = {};
 
(function() {
	'use strict';
	SaveAdvanceList.initializeGermplasmListTree = function() {
		displayGermplasmListTree('germplasmFolderTree', true, 1);
		changeBrowseGermplasmButtonBehavior(false);
		$('#saveListTreeModal').on('hidden.bs.modal', function() {
			$('#germplasmFolderTree').dynatree('getTree').reload();
			changeBrowseGermplasmButtonBehavior(false);
		});
	};
	SaveAdvanceList.openSaveListModal = function(object) {
		if(parseInt($('#reviewAdvanceNurseryModal .total-review-items').html(),10) < 1){
			showErrorMessage('', saveGermplasmReviewError);
			return false;
		}
		$('#reviewAdvanceNurseryModal').modal('hide');
		
		
		
		var listIdentifier = $(object).attr('id'),
		germplasmTreeNode = $('#germplasmFolderTree').dynatree('getTree');
		additionalLazyLoadUrl = '/1';
		$.ajax(
			{ 
				url: '/Fieldbook/ListTreeManager/saveList/'+listIdentifier,
				type: 'GET',
				cache: false,
				success: function(html) {
					$('#saveListTreeModal').data('is-save-crosses', '0');
					$('#saveGermplasmRightSection').html(html);
					setTimeout(function(){$('#saveListTreeModal').modal({ backdrop: 'static', keyboard: true });}, 300);
					//we preselect the program lists
					if(germplasmTreeNode !== null && germplasmTreeNode.getNodeByKey('LISTS') !== null){
						germplasmTreeNode.getNodeByKey('LISTS').activate();
						germplasmTreeNode.getNodeByKey('LISTS').expand();
					}
				}
			}
		);
	};
	SaveAdvanceList.saveGermplasmList = function() {
		var chosenNodeFolder = $('#'+getDisplayedTreeName()).dynatree('getTree').getActiveNode();
		var errorMessageDiv = 'page-save-list-message-modal';
		if(chosenNodeFolder === null){
			showErrorMessage(errorMessageDiv, listParentFolderRequired);
			return false;
		}
		if($('#listName').val() === ''){
			showInvalidInputMessage(listNameRequired);
			return false;
		}
		if($('#listDescription').val() === ''){
			showInvalidInputMessage(listDescriptionRequired);
			return false;
		}
		if($('#listType').val() === ''){
			showInvalidInputMessage(listTypeRequired);
			return false;
		}
		if($('#listDate').val() === ''){
			showInvalidInputMessage(listDateRequired);
			return false;
		}
		var invalidDateMsg = validateAllDates();
	    if(invalidDateMsg !== '') {
	    	showInvalidInputMessage(invalidDateMsg);
	    	return false;
	    }
	    
		var parentId = chosenNodeFolder.data.key;
		$('#saveListForm #parentId').val(parentId);

		
		var saveList  = '/Fieldbook/ListTreeManager/saveList/';
	    var isCrosses = false;
		var isStock = false;

		if($('#saveListTreeModal').data('is-save-crosses') === '1'){
	    	isCrosses = true;
	    }

		if ($('#saveListTreeModal').data('is-save-stock') === '1') {
			isStock = true;
			$('#sourceListId').val($('#saveListTreeModal').data('sourceListId'));
		}

	    if(isCrosses){
			$('#germplasmListType').val('cross');
	    } else if (isStock) {
			$('#germplasmListType').val('stock');
		} else {
			$('#germplasmListType').val('advance');
		}

		var dataForm = $('#saveListForm').serialize();
	    
		$.ajax({
			url: saveList,
			type: 'POST',
			data: dataForm,
			cache: false,
			success: function(data) {
				if(data.isSuccess === 1){
					$('#saveListTreeModal').modal('hide');
					if(isCrosses){
						ImportCrosses.displayTabCrossesList(data.germplasmListId, data.crossesListId,  data.listName);        
						$('#saveListTreeModal').data('is-save-crosses', '0');
					} else if (isStock) {
						$('#saveListTreeModal').data('is-save-stock', '0');
					} else{
						var uniqueId,
							close,
							aHtml;
							uniqueId = data.uniqueId;
							close = '<i class="glyphicon glyphicon-remove fbk-close-tab" id="'+uniqueId+'" onclick="javascript: closeAdvanceListTab(' + uniqueId +')"></i>';
							aHtml = '<a role="tab" data-toggle="tab" id="advanceHref' + uniqueId + '" href="#advance-list' + uniqueId + '">Advance List' + close + '</a>';
							$('#create-nursery-tab-headers').append('<li id="advance-list' + uniqueId + '-li">' + aHtml + '</li>');
							$('#create-nursery-tabs').append('<div class="tab-pane info" id="advance-list' + uniqueId + '"></div>');
							$('a#advanceHref'+uniqueId).tab('show');
														
							setTimeout(function() {
								$('.nav-tabs').tabdrop('layout');
							}, 100);
						displayAdvanceList(data.uniqueId, data.germplasmListId, data.listName, false, data.advancedGermplasmListId);
					}
					showSuccessfulMessage('',saveListSuccessfullyMessage);
				} else {
					showErrorMessage('page-save-list-message-modal', data.message);
				}
			}
		});
	};
	SaveAdvanceList.doAdvanceNursery = function () {

		var serializedData;

		$('input[type=checkbox][name=methodChoice]').prop('disabled', false);
		serializedData = $('#advanceNurseryModalForm').serialize();

		$.ajax({
			url: '/Fieldbook/NurseryManager/advance/nursery',
			type: 'POST',
			data: serializedData,
			cache: false,
			success: function(data) {
				var advanceGermplasmChangeDetail = [];
				if(data.isSuccess === '0'){
					showErrorMessage('page-advance-modal-message', data.message);
				}else{
					if(data.listSize === 0){
						showErrorMessage('page-advance-modal-message', listShouldNotBeEmptyError);
					}else{
						advanceGermplasmChangeDetail = (data.advanceGermplasmChangeDetails);
						$('#advanceNurseryModal').modal('hide');
						if(advanceGermplasmChangeDetail.length === 0){
							SaveAdvanceList.reviewAdvanceList(data.uniqueId);
						}else{
							showAdvanceGermplasmChangeConfirmationPopup(advanceGermplasmChangeDetail, data.uniqueId);
						}
					}
				}

			},
			error: function(jqXHR, textStatus, errorThrown) {
				console.log('The following error occured: ' + textStatus, errorThrown);
			}
		});
	};
	SaveAdvanceList.reviewAdvanceList = function(uniqueId) {
		$.ajax({
			url: '/Fieldbook/NurseryManager/advance/nursery/info?uniqueId='+uniqueId,
			type: 'GET',
			cache: false,
			success: function(html) {				
					$('#advanceNurseryModal').modal('hide');
					
					$('#review-advance-nursery-modal-div').html(html);
					$('.btn-cancel-review').off('click');
					$('.btn-cancel-review').on('click', function(){
						$('#reviewAdvanceNurseryModal').modal('hide');
						setTimeout(function(){$('#advanceNurseryModal').modal({ backdrop: 'static', keyboard: true });}, 300);
					});
					setTimeout(function(){
						$('#reviewAdvanceNurseryModal').off('shown.bs.modal');
						$('#reviewAdvanceNurseryModal').on('shown.bs.modal', function (e) {
							SaveAdvanceList.setupAdvanceListForReview();
						})
						$('#reviewAdvanceNurseryModal').modal({ backdrop: 'static', keyboard: true });
						}, 300);
					
			},
			error: function(jqXHR, textStatus, errorThrown) {
				console.log('The following error occured: ' + textStatus, errorThrown);
			}
		});
	};
	SaveAdvanceList.setupAdvanceListForReview = function(){
		var sectionContainerDiv = 'reviewAdvanceNurseryModal';
  		
		$('#'+getJquerySafeId(sectionContainerDiv) + ' .selectAllAdvance').on('change', function(event){
			//select all the checkbox in the section container div										
				//needed set time out since chrme is not able to rnder properly the checkbox if its checked or not
				setTimeout(function(){
					var isChecked = $('#'+getJquerySafeId(sectionContainerDiv) + ' .selectAllAdvance').prop('checked');
					$('#'+getJquerySafeId(sectionContainerDiv) + ' .advance-nursery-list-table tr').removeClass('selected');
  				$('#'+getJquerySafeId(sectionContainerDiv) + ' .advance-nursery-list-table tr').removeClass('manual-selected');
  				$('#'+getJquerySafeId(sectionContainerDiv) + ' input.advancingListGid').prop('checked', isChecked);
  				
  				if(isChecked){
  					$('#'+getJquerySafeId(sectionContainerDiv) + ' .advance-nursery-list-table tr').addClass('selected');
      				$('#'+getJquerySafeId(sectionContainerDiv) + ' .advance-nursery-list-table tr').addClass('manual-selected');
  				}
  				$('#'+getJquerySafeId(sectionContainerDiv) + ' .numberOfAdvanceSelected').html($('#'+getJquerySafeId(sectionContainerDiv) + ' tr.primaryRow.selected').length);
				}, 10);
				
			});
		
		$('#' + sectionContainerDiv + ' .advance-nursery-list-table').tableSelect({
			onClick: function(row) {
				//we clear all check
				if($('#' + sectionContainerDiv + ' .advance-nursery-list-table').data('check-click') === '1'){
					$('#' + sectionContainerDiv + ' .advance-nursery-list-table').data('check-click', '0');
					$('#' + sectionContainerDiv + ' .advance-nursery-list-table tr.manual-selected').addClass('selected');
					$('#' + sectionContainerDiv + ' .advance-nursery-list-table tr:not(.manual-selected) input.reviewAdvancingListGid:checked').parent().parent().addClass('selected');
					$('#' + sectionContainerDiv + ' .advance-nursery-list-table tr.selected input.reviewAdvancingListGid:not(:checked)').parent().parent().removeClass('selected');
				}else{
					$('#' + sectionContainerDiv + ' .advance-nursery-list-table input.reviewAdvancingListGid').prop('checked', false);
					$('#' + sectionContainerDiv + ' .advance-nursery-list-table tr.manual-selected').removeClass('manual-selected');
					if($(row).hasClass('selected')){
						$(row).find('input.reviewAdvancingListGid').prop('checked', true);
					}else{
						$(row).find('input.reviewAdvancingListGid').prop('checked', false);
					}
				}
				$('#'+getJquerySafeId(sectionContainerDiv) + ' .numberOfAdvanceSelected').html($('#'+getJquerySafeId(sectionContainerDiv) + ' tr.primaryRow.selected').length);
			},
			onCtrl: function(row){
				
				$('#' + sectionContainerDiv + ' .advance-nursery-list-table tr.selected input.reviewAdvancingListGid').prop('checked', true);
				$('#' + sectionContainerDiv + ' .advance-nursery-list-table tr:not(.selected) input.reviewAdvancingListGid').prop('checked', false);
				if($(row).hasClass('manual-selected') || $(row).hasClass('selected')){
					$(row).find('input.reviewAdvancingListGid').prop('checked', true);
					$(row).addClass('selected');
				}else{
					$(row).find('input.reviewAdvancingListGid').prop('checked', false);
					$(row).removeClass('selected');
				}
				if($('#' + sectionContainerDiv + ' .advance-nursery-list-table').data('check-click') === '1'){
					$('#' + sectionContainerDiv + ' .advance-nursery-list-table').data('check-click', '0');
					if($(row).hasClass('selected') && $(row).hasClass('manual-selected') === false){
						$(row).find('input.reviewAdvancingListGid').prop('checked', false);
						$(row).removeClass('selected');	
					}
				}
				$('#'+getJquerySafeId(sectionContainerDiv) + ' .numberOfAdvanceSelected').html($('#'+getJquerySafeId(sectionContainerDiv) + ' tr.primaryRow.selected').length);
			},
			onShift: function(){
				$('#' + sectionContainerDiv + ' .advance-nursery-list-table tr.manual-selected-dummy').addClass('selected');					
				$('#' + sectionContainerDiv + ' .advance-nursery-list-table tr.selected input.reviewAdvancingListGid').prop('checked', true);
				$('#' + sectionContainerDiv + ' .advance-nursery-list-table tr:not(.selected) input.reviewAdvancingListGid').prop('checked', false);
				$('#'+getJquerySafeId(sectionContainerDiv) + ' .numberOfAdvanceSelected').html($('tr.primaryRow.selected').length);
			}
		
		});
		$('#' + sectionContainerDiv + ' .advance-nursery-list-table input.reviewAdvancingListGid').on('click', function(){
			$('#' + sectionContainerDiv + ' .advance-nursery-list-table').data('check-click', '1');
			if($(this).is(':checked')){
				//we highlight
				$(this).parent().parent().addClass('selected');
				$(this).parent().parent().addClass('manual-selected');
			}else{
				$(this).parent().parent().removeClass('selected');
				$(this).parent().parent().removeClass('manual-selected');
			}
			$('#' + sectionContainerDiv + ' .advance-nursery-list-table tr.manual-selected input.reviewAdvancingListGid').prop('checked', true);
			$('#' + sectionContainerDiv + ' .advance-nursery-list-table tr.manual-selected').addClass('selected');
			$('#' + sectionContainerDiv + ' .advance-nursery-list-table tr:not(.manual-selected)').remove('selected');
			$('#'+getJquerySafeId(sectionContainerDiv) + ' .numberOfAdvanceSelected').html($('#'+getJquerySafeId(sectionContainerDiv) + ' tr.primaryRow.selected').length);
		});
		      
		$('#reviewAdvanceNurseryModal').off('shown.bs.modal');
		$('#reviewAdvanceNurseryModal .delete-entries').off('click');
		$('#reviewAdvanceNurseryModal .select-all-entries').off('click');
		$('#reviewAdvanceNurseryModal .delete-entries').on('click', SaveAdvanceList.deleteSelectedEntries);
		$('#reviewAdvanceNurseryModal .select-all-entries').on('click', SaveAdvanceList.selectAllReviewEntries);
		
		$('#' + sectionContainerDiv +' .advance-germplasm-items').contextmenu({
		    delegate: "tr",
		    menu: [
		      {title: 'Delete Selected Entries', cmd: 'deleteSelected'},
		      {title: 'Select All', cmd: 'selectAll'}
			],
			select: function(event, ui) {				
				switch(ui.cmd){
					case 'deleteSelected':
						SaveAdvanceList.deleteSelectedEntries();
						break;
					case 'selectAll':
						SaveAdvanceList.selectAllReviewEntries();
						break;
				}
			},
			beforeOpen: function(event, ui) {				
				ui.menu.zIndex(9999);
		    }			
		  });
		new BMS.Fieldbook.AdvancedGermplasmListDataTable('#'+sectionContainerDiv + ' .advance-germplasm-items', '#'+sectionContainerDiv);
		$('#advance-nursery-germplasm-list').css('opacity','1');
		
	
	};
	SaveAdvanceList.selectAllReviewEntries = function(){
		var sectionContainerDiv = 'reviewAdvanceNurseryModal';
		var isChecked = true;
		$('#'+getJquerySafeId(sectionContainerDiv) + ' .advance-nursery-list-table tr').removeClass('selected');
		$('#'+getJquerySafeId(sectionContainerDiv) + ' .advance-nursery-list-table tr').removeClass('manual-selected');
		$('#'+getJquerySafeId(sectionContainerDiv) + ' input.reviewAdvancingListGid').prop('checked', isChecked);
		
		if(isChecked){
			$('#'+getJquerySafeId(sectionContainerDiv) + ' .advance-nursery-list-table tr').addClass('selected');
			$('#'+getJquerySafeId(sectionContainerDiv) + ' .advance-nursery-list-table tr').addClass('manual-selected');
		}
		$('#'+getJquerySafeId(sectionContainerDiv) + ' .numberOfAdvanceSelected').html($('#'+getJquerySafeId(sectionContainerDiv) + ' tr.primaryRow.selected').length);
	};
	SaveAdvanceList.deleteSelectedEntries = function(){
		var entryNums = '',
			sectionContainerDiv = 'reviewAdvanceNurseryModal',
			uniqueId = $('.btn-save-advance-list').attr('id');
		$('#'+sectionContainerDiv + ' .reviewAdvancingListGid:checked').each(function(){
			if(entryNums !== ''){
				entryNums += ',';
			}
			entryNums += $(this).data('entry');
		});
		if(entryNums.length === 0){
			showErrorMessage('page-message', germplasmEntrySelectError);
			moveToTopScreen();
			return;		
		}	
		
		$.ajax({
			url: '/Fieldbook/NurseryManager/advance/nursery/delete/entries',
			type: 'POST',
			data: { 'entryNums': entryNums, 'uniqueId': uniqueId },
			cache: false,
			success: function(html) {									
					$('#review-advance-nursery-modal-div .review-advance-records').html($(html).find('.review-advance-records').html());
					$('.btn-cancel-review').off('click');
					$('.btn-cancel-review').on('click', function(){
						$('#reviewAdvanceNurseryModal').modal('hide');
						setTimeout(function(){$('#advanceNurseryModal').modal({ backdrop: 'static', keyboard: true });}, 300);
					});
						
					setTimeout(function(){SaveAdvanceList.setupAdvanceListForReview();}, 300);
					
			},
			error: function(jqXHR, textStatus, errorThrown) {
				console.log('The following error occured: ' + textStatus, errorThrown);
			}
		});
	};
})(); 

