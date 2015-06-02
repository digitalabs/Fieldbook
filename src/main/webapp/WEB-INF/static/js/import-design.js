var ImportDesign = {
		
		hasCheckListSelected : function() {
			if (isNursery()){
				return $('.check-germplasm-list-items tbody tr').length != 0;
			}else{
				return false;
			}
		},

		hasGermplasmListSelected : function(){
			if (isNursery()){
				return ($('#numberOfEntries').text() !== '');
			}else{
				return angular.element('#mainApp').injector().get('TrialManagerDataService').applicationData.germplasmListSelected;
			}
		},

		getDesignImportNgApp : function() {
			return angular.injector(['ng','designImportApp']);
		},

		getMessages : function() {
			return ImportDesign.getDesignImportNgApp().get('Messages');
		},

		showDesignWarningMessage : function() {
			showAlertMessage('',ImportDesign.getMessages().OWN_DESIGN_SELECT_WARNING, 5000);
		},

		getTrialManagerDataService : function() {
			return isNursery() ? {currentData : {},applicationData :{}} : angular.element('#mainApp').injector().get('TrialManagerDataService');
		},

		trialManagerCurrentData: function() {
			return ImportDesign.getTrialManagerDataService().currentData;
		},
		
		reloadMeasurements: function(){
			if (isNursery()) {
				// reload nursery measurments here
			} else {
				var angularElem = angular.element('#mainApp');

				angularElem.scope().$apply(function(){
					ImportDesign.getTrialManagerDataService().applicationData.isGeneratedOwnDesign = true;
				});
			}
		},
		
		showPopup : function(hasGermplasmListSelected){
			if (hasGermplasmListSelected && !ImportDesign.hasCheckListSelected()){
				$('#importDesignModal').modal({ backdrop: 'static', keyboard: true });
			}else{
				if (ImportDesign.hasCheckListSelected()){
					showErrorMessage(designImportErrorHeader, 'You cannot import a design if you have Selected Checks specified.');
				}else{
					showErrorMessage(designImportErrorHeader, 'Please choose a germplasm list before you can import a design.');
				}
			}

		},

		showDesignMapPopup : function() {
			setTimeout(function(){
				$('#designMapModal').one('show.bs.modal',function() {
					ImportDesign.initDesignMapPopup();

					if (!isNursery()) {
						setTimeout(function() { ImportDesign.showDesignWarningMessage();  },200);
					}

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

			var scope = elem.scope();
			scope.designType = '';

			// retrieve initial data from the service
			$.getJSON('/Fieldbook/DesignImport/getMappingData').done(function(data) {

				myService.data = data;
				scope.data = myService.data;

				//apply the changes to the scope.
				scope.$apply();
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

		nurseryEnvironmentDetails : {
			noOfEnvironments : 1,
			environments : [{
				stockId : 0,
				locationId : 0,
				experimentId : 0,
				managementDetailValues : {},
				trialDetailValues : {},
				phenotypeIDMap : {}
			}]
		},
		
		generateDesign : function() {

			var environmentData = isNursery() ? ImportDesign.nurseryEnvironmentDetails : angular.copy(ImportDesign.trialManagerCurrentData().environments);
			
			$.each(environmentData.environments, function(key, data){
				$.each(data.managementDetailValues, function(key, value){
					if (value && value.id){
						data.managementDetailValues[key] = value.id;
					}
				});
			});
			
			$.ajax(
					{ 
						url: '/Fieldbook/DesignImport/generate',
						type: 'POST',
						data: JSON.stringify(environmentData),
						dataType: 'json',
						contentType: 'application/json; charset=utf-8',
						cache: false,
						success: function(resp) {
							if (resp.isSuccess) {
								
								
								var $body = $('body');
								var environmentData = resp.environmentData;
								console.log(resp);


								
								$('#chooseGermplasmAndChecks').data('replace', '1');
								$body.data('expDesignShowPreview', '1');
								$body.data('needGenerateExperimentalDesign', '0');
								
								ImportDesign.closeReviewModal();
								
								ImportDesign.reloadMeasurements();
								
								if (isNursery()){
									showSuccessfulMessage('', 'The nursery design was imported successfully. Please save your nursery before proceeding to Measurements tab.');
									$('#nursery-experimental-design-li').show();
								}else{
									// gonna get get that settings.managementDetails if trial
									$.each(environmentData,function(key,value) {
										ImportDesign.getTrialManagerDataService().settings.environments.managementDetails.push(value.variable.cvTermId, ImportDesign.getTrialManagerDataService().transformViewSettingsVariable(value));
									});


									ImportDesign.getTrialManagerDataService().clearUnappliedChangesFlag();
									showSuccessfulMessage('', 'The trial design was imported successfully. Please review the Measurements tab.');
								}
								
								
							}else{
								createErrorNotification(designImportErrorHeader,resp.error.join('<br/>'));
							}
						}
				});
		},
		
		loadReviewDesignData : function() {
			setTimeout(function(){
			
				var environmentData = isNursery() ? ImportDesign.nurseryEnvironmentDetails : angular.copy(ImportDesign.trialManagerCurrentData().environments);				
				$.each(environmentData.environments, function(key, data){
					$.each(data.managementDetailValues, function(key, value){
						if (value && value.id){
							data.managementDetailValues[key] = value.id;
						}
					});
				});
				
				$.ajax(
						{ 
						url: '/Fieldbook/DesignImport/showDetails/data',
						type: 'POST',
						data: JSON.stringify(environmentData),
						dataType: 'json',
						contentType: 'application/json; charset=utf-8',
						cache: false,
						success: function(response) {
							new  BMS.Fieldbook.PreviewDesignMeasurementsDataTable('#design-measurement-table', response);					
						}
					});
				
			}, 200);
			
		},
		
		doSubmitImport : function() {
			'use strict';

			if ($('#fileupload-import-design').val() === '') {
				showErrorMessage('', 'Please choose a file to import');
				return false;
			}

			if (isNursery()){
				$('#importDesignUploadForm').attr('action', '/Fieldbook/DesignImport/import/N');
			}else{
				$('#importDesignUploadForm').attr('action', '/Fieldbook/DesignImport/import/T');
			}
			
			ImportDesign.submitImport($('#importDesignUploadForm')).done(function(resp) {
				
				var resultJson = JSON.parse(resp);
				
				if (!resultJson.isSuccess) {
					createErrorNotification(designImportErrorHeader,resultJson.error.join('<br/>'));
					return;
				} else if (resultJson.warning){
					showAlertMessage('', resultJson.warning);
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
				dataType: 'text',
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
