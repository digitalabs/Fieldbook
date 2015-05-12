var ImportDesign = {

		hasGermplasmListSelected : function(){
			return angular.element('#mainApp').injector().get('TrialManagerDataService').applicationData.germplasmListSelected;
		},
		
		showPopup : function(hasGermplasmListSelected){
			
			
			if (hasGermplasmListSelected){
				$('#importDesignModal').modal({ backdrop: 'static', keyboard: true });
			}else{
				showErrorMessage(designImportErrorHeader, 'Please choose a germplasm list before you can import a design.');
			}
			
			
		},
		doSubmitImport : function() {
			'use strict';

			if ($('#fileupload-import-design').val() === '') {
				showErrorMessage('', 'Please choose a file to import');
				return false;
			}

			ImportDesign.submitImport($('#importDesignUploadForm')).done(function(resp) {

				if (!resp.isSuccess) {
					createErrorNotification(designImportErrorHeader,resp.error.join('<br/>'));
					return;
				}

				$('#importDesignModal').modal('hide');

			});

		},
		goBackToPage: function(hiddenModalSelector,shownModalSelector) {
			$(hiddenModalSelector).modal('hide');
			$(shownModalSelector).modal({ backdrop: 'static', keyboard: true });
		},
		submitImport : function($importDesignUploadForm) {
			'use strict';
			var deferred = $.Deferred();
			$importDesignUploadForm.ajaxForm({
				dataType: 'json',
				success: function(response) {
					deferred.resolve(response);
				},
				error: function(response) {

                    createErrorNotification(designImportErrorHeader, invalidImportedFile);

                    deferred.reject(response);
				}
			}).submit();

			return deferred.promise();
		}
};

$(document).ready(function() {
    $('.btn-import-design').on('click', ImportDesign.doSubmitImport);
    $('.import-design-section .modal').on('hide.bs.modal', function() {
		$('#fileupload-import-design').parent().parent().removeClass('has-error');
	});
});