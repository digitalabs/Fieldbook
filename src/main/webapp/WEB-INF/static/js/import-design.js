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

		showDesignMapPopup : function() {
			setTimeout(function(){
				$('#designMapModal').one('show.bs.modal',function() {
					ImportDesign.initDesignMapPopup();
				}).modal();
			},300);

		},

		initDesignMapPopup : function() {
			//get your angular element
			var elem = angular.element('#designMapModal .modal-content[ng-controller=designImportCtrl]');

			//get the injector.
			var injector = elem.injector();

			//get the service.
			var myService = injector.get('DesignMappingService');

			// retrieve initial data from the service
			$.getJSON('/Fieldbook/DesignImport/getMappingData').done(function(data) {
				myService.data = data;
				elem.scope().data = myService.data;
				//apply the changes to the scope.
				elem.scope().$apply();
			});

		},
		
		showReviewPopup : function() {
			$('#designMapModal').one('hidden.bs.modal',function() {
				setTimeout(function() {
					$('#reviewDesignModal').modal({ backdrop: 'static', keyboard: true });
					ImportDesign.showReviewDesignData();
				},200);
			}).modal('hide');
		},
		
		showReviewDesignData : function() {
					
			$.ajax(
				{ 
					url: '/Fieldbook/DesignImport/showDetails',
					type: 'GET',
					success: function(html) {
						$('#divDesignMeasurements').html(html);
					}
			});

		},
		
		generateDesign : function() {
			$.ajax(
					{ 
						url: '/Fieldbook/DesignImport/generate',
						type: 'GET',
						success: function(resp) {
							if (resp.isSuccess) {
								
								$('#chooseGermplasmAndChecks').data('replace', '1');
								$('body').data('expDesignShowPreview', '1');
								$('body').data('needGenerateExperimentalDesign', '0');
								
								ImportDesign.closeReviewModal();
							}else{
								createErrorNotification(designImportErrorHeader,resp.error.join('<br/>'));
								return;
							}
						}
				});
		},
		
		loadReviewDesignData : function() {
			setTimeout(function(){
				
				$.ajax(
						{ 
						url: '/Fieldbook/DesignImport/showDetails/data',
						type: 'GET',
						data: '',
						cache: false,
						success: function(response) {
							new  BMS.Fieldbook.PreviewDesignMeasurementsDataTable('#design-measurement-table', response);					
						}
					});
				
			}, 50);
			
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
				} else if (resp.warning){
					showAlertMessage('', resp.warning);
				}

				$('#importDesignModal').one('hidden.bs.modal',function() {
					ImportDesign.showDesignMapPopup();
				}).modal('hide');

			});

		},
		closeReviewModal: function() {
			$('#reviewDesignModal').modal('hide');
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
    $('.btn-import-generate').on('click', ImportDesign.generateDesign);
    $('.import-design-section .modal').on('hide.bs.modal', function() {
		$('#fileupload-import-design').parent().parent().removeClass('has-error');
	});
});