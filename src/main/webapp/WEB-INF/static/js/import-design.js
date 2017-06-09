var ImportDesign = (function() {
	'use strict';
	return {

		hasCheckListSelected: function() {
			if (isNursery()) {
				return $('.check-germplasm-list-items tbody tr').length !== 0;
			} else {
				return false;
			}
		},

		hasGermplasmListSelected: function() {
			if (isNursery()) {
				return ($('#numberOfEntries').text() !== '');
			} else {
				return angular.element('#mainApp').injector().get(
						'TrialManagerDataService').applicationData.germplasmListSelected;
			}
		},

		getDesignImportNgApp: function() {
			return angular.injector(['ng', 'designImportApp']);
		},

		getMessages: function() {
			return ImportDesign.getDesignImportNgApp().get('Messages');
		},

		showDesignWarningMessage: function() {
			showAlertMessage('',
					ImportDesign.getMessages().OWN_DESIGN_SELECT_WARNING, 5000);
		},

		getTrialManagerDataService: function() {
			return isNursery() ? {
				currentData: {},
				applicationData: {}
			} : angular.element('#mainApp').injector().get(
					'TrialManagerDataService');
		},

		trialManagerCurrentData: function() {
			return ImportDesign.getTrialManagerDataService().currentData;
		},

		reloadMeasurements: function() {
			if (!isNursery()){
				var angularElem = angular.element('#mainApp');

				angularElem
						.scope()
						.$apply(
								function() {
									ImportDesign.getTrialManagerDataService().applicationData.isGeneratedOwnDesign = true;
									ImportDesign.getTrialManagerDataService().applicationData.unsavedGeneratedDesign = true;
								});
			}
		},

		showPopup: function(hasGermplasmListSelected) {

			var studyType = isNursery() ? 'Nursery' : 'Trial';

			if (hasMeasurementData()) {
				showErrorMessage(designImportErrorHeader, 'This ' + studyType + ' has saved observations, the experimental design can no longer be modified.');
			} else if (!hasGermplasmListSelected) {
				showErrorMessage(designImportErrorHeader, 'Please choose a germplasm list before you can import a design.');
			} else {
				$('#importDesignModal').modal({
					backdrop: 'static',
					keyboard: true
				});
			}

		},

		showDesignMapPopup: function() {
			setTimeout(function() {
				$('#designMapModal').one('show.bs.modal', function() {
					ImportDesign.initDesignMapPopup();

					if (!isNursery()) {
						setTimeout(function() {
							ImportDesign.showDesignWarningMessage();
						}, 200);
					}

				}).modal();
			}, 300);

		},

		hideDesignMapPopup: function() {
			var deferred = $.Deferred();

			setTimeout(function() {
				$('#designMapModal').one('hidden.bs.modal', function() {
					deferred.resolve();
				}).modal('hide');

			}, 300);

			return deferred.promise();
		},

		initDesignMapPopup: function() {

			// get your angular element
			var elem = angular
					.element('#designMapModal .modal-content[ng-controller=designImportCtrl]');

			// get the injector.
			var injector = elem.injector();

			// get the service.
			var myService = injector.get('DesignMappingService');

			var scope = elem.scope();
			scope.designType = '';

			// retrieve initial data from the service
			$.getJSON('/Fieldbook/DesignImport/getMappingData').done(
					function(data) {

						myService.data = data;
						scope.data = myService.data;

						// apply the changes to the scope.
						scope.$apply();
					});
		},

		showReviewPopup: function() {
			return ImportDesign.showReviewDesignData().then(function(html) {
				$('#reviewDesignModal').one('shown.bs.modal', function() {
					$('#divDesignMeasurements').html(html);
					$('body').addClass('modal-open');
				}).modal({
					backdrop: 'static',
					keyboard: true
				});
			});
		},

		showReviewDesignData: function() {
			return $.get('/Fieldbook/DesignImport/showDetails');
		},

		nurseryEnvironmentDetails: {
			noOfEnvironments: 1,
			environments: [{
				stockId: 0,
				locationId: 0,
				experimentId: 0,
				managementDetailValues: {},
				trialDetailValues: {},
				phenotypeIDMap: {}
			}]
		},

		showChangeDesignPopup: function(hasGermplasmListSelected) {
			if (hasGermplasmListSelected &&
					!ImportDesign.hasCheckListSelected()) {
				$('#changeDesignModal').modal({
					backdrop: 'static',
					keyboard: true
				});

				if (!isNursery()) {
					$('#change-design-description-nursery').hide();
					$('#change-design-description-trial').show();
				} else {
					$('#change-design-description-nursery').show();
					$('#change-design-description-trial').hide();
				}
			} else {
				if (ImportDesign.hasCheckListSelected()) {
					showErrorMessage(designImportErrorHeader,
							'You cannot import a design if you have Selected Checks specified.');
				} else {
					showErrorMessage(designImportErrorHeader,
							'Please choose a germplasm list before you can import a design.');
				}
			}

		},

		generateDesign: function() {
			var $body = $('body');

			//if the design is generated but not saved, the measurements datatable is for preview only (edit is not allowed)
			$body.addClass('preview-measurements-only');
			//TODO Clear the style on Nursery save

			var environmentData = isNursery() ? ImportDesign.nurseryEnvironmentDetails
					: angular.copy(ImportDesign.trialManagerCurrentData().environments);

			$.each(environmentData.environments, function(key, data) {
				$.each(data.managementDetailValues, function(key, value) {
					if (value && value.id) {
						data.managementDetailValues[key] = value.id;
					}
				});
			});

			var service = ImportDesign.getTrialManagerDataService();
			// custom import design type id
			var designTypeId = 3;
			var data = isNursery()? { 	environmentData: null, 
									  	selectedDesignType: designTypeId,
									  	startingEntryNo: $('#txtStartingEntryNo').val(),
									  	startingPlotNo: $('#txtStartingPlotNo').val(),
									  	hasNewEnvironmentAdded: false
									} : service.retrieveGenerateDesignInput(designTypeId);
			data.environmentData = environmentData;

			$.ajax({
				type: 'POST',
				url: '/Fieldbook/DesignImport/generate',
				data: JSON.stringify(data),
				dataType: 'json',
				contentType: 'application/json; charset=utf-8'
			}).done(function(resp) {
				ImportDesign.updateEnvironmentAndMeasurements(resp);
				//TODO Remove expDesignShowPreview global, broadcast and update
				angular.element('#mainApp').scope().$broadcast('designImportGenerated');
				//TODO if error - remove preview class and show error

			});
		},

		updateEnvironmentAndMeasurements: function(resp) {
			if (!resp.isSuccess) {
				createErrorNotification(designImportErrorHeader, resp.error.join('<br/>'));
				return;
			}

			var $body = $('body');

			$body.removeClass('modal-open');
			$('#chooseGermplasmAndChecks').data('replace', '1');

			ImportDesign.closeReviewModal();
			ImportDesign.reloadMeasurements();

			if (isNursery()) {
				//TODO Localise the message
				showSuccessfulMessage(
						'',
						'The nursery design was imported successfully. Please save your nursery before proceeding to Measurements tab.');
				$('#nursery-experimental-design-li').show();

			} else {
				var environmentData = resp.environmentData, environmentSettings = resp.environmentSettings, trialService = ImportDesign
						.getTrialManagerDataService();

				$.each(environmentSettings, function(key, value) {
					trialService.settings.environments.managementDetails.push(
							value.variable.cvTermId, trialService
									.transformViewSettingsVariable(value));
				});

				// Set the design type to Other Design Type
				trialService.currentData.experimentalDesign.designType = 3;
				trialService.updateCurrentData('environments', environmentData);

				angular.element('#mainApp').scope().$apply();

				ImportDesign.getTrialManagerDataService().clearUnappliedChangesFlag();
				//TODO Localise the message
				showSuccessfulMessage(
						'',
						'The trial design was imported successfully. Please review the Measurements tab.');
			}
		},

		loadReviewDesignData: function() {
			setTimeout(
					function() {

						var environmentData = isNursery() ? ImportDesign.nurseryEnvironmentDetails
								: angular
										.copy(ImportDesign
												.trialManagerCurrentData().environments);
						$.each(environmentData.environments,
							function(key, data) {
								$.each(data.managementDetailValues,
									function(key, value) {
										if (value &&
												value.id) {
											data.managementDetailValues[key] = value.id;
										}
									});
							});

						$.ajax({
								url: '/Fieldbook/DesignImport/showDetails/data/',
								type: 'POST',
								data: JSON.stringify(environmentData),
								dataType: 'json',
								contentType: 'application/json; charset=utf-8',
								cache: false,
								success: function(response) {
									new BMS.Fieldbook.PreviewDesignMeasurementsDataTable(
											'#design-measurement-table',
											response);
								}
							});

					}, 200);

		},

		cancelDesignImport: function() {
			$.get('/Fieldbook/DesignImport/cancelImportDesign');
		},

		doSubmitImport: function() {
			if ($('#fileupload-import-design').val() === '') {
				showErrorMessage('', 'Please choose a file to import');
				return false;
			}

			if (isNursery()) {
				$('#importDesignUploadForm').attr('action',
						'/Fieldbook/DesignImport/import/N');
			} else {
				var TrialManagerDataService = angular.element('#mainApp').injector().get('TrialManagerDataService');
				var hasNewEnvironmentAdded = TrialManagerDataService.applicationData.hasNewEnvironmentAdded;
				var noOfEnvironments = parseInt(TrialManagerDataService.currentData.environments.noOfEnvironments);

				var actionURL = '/Fieldbook/DesignImport/import/T';
				if(hasNewEnvironmentAdded){
					actionURL += '/' + noOfEnvironments;
				}

				$('#importDesignUploadForm').attr('action',actionURL);
			}

			ImportDesign.submitImport($('#importDesignUploadForm')).done(
					function(resp) {

						var resultJson = JSON.parse(resp);

						if (!resultJson.isSuccess) {
							createErrorNotification(designImportErrorHeader,
									resultJson.error.join('<br/>'));
							return;
						} else if (resultJson.warning) {
							showAlertMessage('', resultJson.warning);
						}

						$('#importDesignModal').one('hidden.bs.modal',
								function() {
									ImportDesign.showDesignMapPopup();
								}).modal('hide');

					});

		},
		closeReviewModal: function() {
			$('#reviewDesignModal').modal('hide');
		},

		submitImport: function($importDesignUploadForm) {
			var deferred = $.Deferred();

			$importDesignUploadForm.ajaxForm(
					{
						dataType: 'text',
						success: function(response) {
							deferred.resolve(response);
						},
						error: function(response) {
							createErrorNotification(designImportErrorHeader,
									invalidImportedFile);
							deferred.reject(response);
						}
					}).submit();

			return deferred.promise();
		},

		hideChangeButton: function() {
			if ($('#measurementDataExisting').val() === 'true') {
				$('#change-import-design-url-link').hide();
			} else {
				$('#change-import-design-url-link').show();
			}
		},

		doResetDesign: function() {
			'use strict';
			var studyId = 0;
			if ($('#studyId').val() != undefined && $('#studyId').val() != '') {
				studyId = $('#studyId').val();
			}

			$.ajax({
				url: '/Fieldbook/DesignImport/import/change/' + studyId + '/' +
						isNursery(),
				type: 'POST',
				data: '',
				cache: false,
				success: function(response) {
					showSuccessfulMessage('', response.success);

					if (isNursery()) {
						// hide experimental design
						$('#nursery-experimental-design-li').hide();
						$('#nursery-experimental-design').hide();

						// show measurement row
						$('li#nursery-measurements-li a').tab('show');

						// to enforce overwrite when the nursery is saved
						$('#chooseGermplasmAndChecks').data('replace', '1');
					} else {
						ImportDesign.reloadMeasurements();
					}

					$('#changeDesignModal').modal('hide');
				}
			});
		}
	};
})();

$(document).ready(
		function() {
			'use strict';
			$('.btn-import-design').on('click', ImportDesign.doSubmitImport);
			$('.btn-import-generate').on('click', ImportDesign.generateDesign);
			$('.btn-import-generate-cancel').on('click',
					ImportDesign.cancelDesignImport);
			$('.import-design-section .modal').on(
					'hide.bs.modal',
					function() {
						$('#fileupload-import-design').parent().parent()
								.removeClass('has-error');
					});
			$('.btn-change-imported-design').on('click',
					ImportDesign.doResetDesign);

			// hide the change button if the study has measurement data,
			// otherwise, leave as is.
			ImportDesign.hideChangeButton();
		});
