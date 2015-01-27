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
				//reload the screen
				var	importError = '',
					errorIndex = 0;


				if(resp.isSuccess === 1){

					$('.import-crosses-section .modal').modal('hide');

					// show  aldrins page
					ImportCrosses.openCrossesList();

				}else{
					showErrorMessage('', resp.error);
				}


			});

		},

		openCrossesList : function() {
			'use strict';
			$('#openCrossesListModal').modal({ backdrop: 'static', keyboard: true });

			ImportCrosses.getImportedCrossesTable().done(function(response) {
				setTimeout(function() {
					new  BMS.Fieldbook.PreviewCrossesDataTable('#preview-crosses-table', response);
				},500);
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
				dataType: 'text',
				success: function(responseText) {
					deferred.resolve($.parseJSON(responseText));
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
		displayTabCrossesList: function (germplasmListId, crossesListId) {
			'use script';			
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
					close = '<i class="glyphicon glyphicon-remove fbk-close-tab fbk-hide" id="'+uniqueId+'" onclick="javascript: closeAdvanceListTab(' + uniqueId +')"></i>';
					aHtml = '<a id="advance-list'+uniqueId+'" role="tab" class="advanceList crossesList crossesList'+uniqueId+'" data-toggle="tab" href="#advance-list' + uniqueId + '" data-list-id="' + uniqueId + '">Crosses' + close + '</a>';
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
			additionalLazyLoadUrl = '/1';
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
						if(germplasmTreeNode !== null && germplasmTreeNode.getNodeByKey('LOCAL') !== null){
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