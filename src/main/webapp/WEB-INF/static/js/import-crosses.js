var ImportCrosses = {
		
		importOptions : {
				dataType: 'text',
				success: this.showImportCrossesResponse // post-submit callback
			},
		
		showPopup : function(){
			$('#fileupload-import-crosses').val('');
			$('.import-crosses-section .modal').modal({ backdrop: 'static', keyboard: true });
					
		},
		showImportCrossesResponse : function(responseText) {
			//reload the screen
			'use strict';
			var resp = $.parseJSON(responseText),
				importError = '',
				errorIndex = 0;
								
			
			if(resp.isSuccess === 1){
				if(resp.conditionConstantsImportErrorMessage !== null && resp.conditionConstantsImportErrorMessage !== ''){
					showAlertMessage('', resp.conditionConstantsImportErrorMessage);	
				}				
			}else{
				showErrorMessage('', resp.error);
				$('.import-crosses-section .modal').modal('hide');
			}
		},
		submitImport : function() {
			'use strict';
			
			if ($('#fileupload-import-crosses').val() === '') {
				showErrorMessage('', 'Please choose a file to import');
				return false;
			}

			$('.import-crosses-section').ajaxForm(this.importOptions).submit();			
		},	
		displayCrossesGermplasmDetails: function (listId) {
			'use strict';
			$.ajax({
				url: "/Fieldbook/SeedStoreManager/crosses/displayGermplasmDetails/" + listId,
				type: "GET",
				cache: false,
				success: function(html) {
					$('.crosses-list' + getCurrentAdvanceTabTempIdentifier()).html(html);
				}
			});
		},
		displayCrossesList: function (uniqueId, germplasmListId, listName, isDefault, crossesListId) {
			'use script';
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
		}
}

$(document).ready(function() {
	$('.import-crosses').on('click', ImportCrosses.showPopup);
	$('.btn-import-crosses').on('click', ImportCrosses.submitImport);
	$('.import-crosses-section .modal').on('hide.bs.modal', function() {
		$('div.import-crosses-file-upload').parent().parent().removeClass('has-error');
		$('.import-crosses-section .modal .fileupload-exists').click();
	});
});