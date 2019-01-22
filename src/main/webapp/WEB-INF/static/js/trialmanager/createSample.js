(function () {
	'use strict';


	var createSampleModule = angular.module('create-sample', ['ui.bootstrap', 'datasets-api', 'datasetOptionModal', 'fieldbook-utils']);

	createSampleModule.factory('createSampleModalService', ['$uibModal',
		function ($uibModal) {

			var createSampleModalService = {};

			createSampleModalService.openDatasetOptionModal = function () {
				$uibModal.open({
					template: '<dataset-option-modal modal-title="modalTitle" message="message"' +
					'selected="selected" on-continue="next()"></dataset-option-modal>',
					controller: 'createSampleDatasetOptionCtrl',
					size: 'md'
				});
			};

			createSampleModalService.openSelectEnvironmentToSampleListModal = function (datasetId) {
				$uibModal.open({
					templateUrl: '/Fieldbook/static/angular-templates/createSample/selectEnvironmentToSampleListModal.html',
					controller: "selectEnvironmentToSampleListModalCtrl",
					size: 'md',
					resolve: {
						datasetId: function () {
							return datasetId;
						}
					},
					controllerAs: 'ctrl'
				});
			};

			createSampleModalService.openSelectSelectionVariableToSampleListModal = function (instanceNumbers) {
				$uibModal.open({
					templateUrl: '/Fieldbook/static/angular-templates/createSample/selectSelectionVariableToSampleListModal.html',
					controller: "selectSelectionVariableToSampleListModalCtrl",
					size: 'md',
					resolve: {
						instanceNumbers: function () {
							return instanceNumbers;
						}
					},
					controllerAs: 'ctrl'
				});
			};

			createSampleModalService.showAlertMessage = function (title, message) {
				// Call the global function to show alert message
				showAlertMessage(title, message);
			};

			createSampleModalService.showErrorMessage = function (title, message) {
				// Call the global function to show alert message
				showErrorMessage(title, message);
			};

			return createSampleModalService;

		}]);

	createSampleModule.controller('createSampleDatasetOptionCtrl', ['$scope', '$uibModal', '$uibModalInstance', 'studyContext', 'createSampleModalService',
		function ($scope, $uibModal, $uibModalInstance, studyContext, createSampleModalService) {

			$scope.modalTitle = 'Create sample list';
			$scope.message = 'Select the dataset from where you would like to generate genotyping samples';
			$scope.measurementDatasetId = studyContext.measurementDatasetId;
			$scope.selected = {datasetId: $scope.measurementDatasetId};

			$scope.next = function () {
				createSampleModalService.openSelectEnvironmentToSampleListModal($scope.selected.datasetId);
				$uibModalInstance.close();
			};

		}]);

	createSampleModule.controller('selectEnvironmentToSampleListModalCtrl', ['$scope', 'environmentService', 'createSampleModalService', '$uibModalInstance',
		function ($scope, environmentService, createSampleModalService, $uibModalInstance) {

		$scope.instances = [];
		$scope.selectedInstances = {};
		$scope.isEmptySelection = false;

		$scope.continueCreatingSampleList = function () {

			var instanceNumbers = [];
			Object.keys($scope.selectedInstances).forEach(function (instanceNumber) {
				var isSelected = $scope.selectedInstances[instanceNumber];
				if (isSelected) {
					instanceNumbers.push(instanceNumber);
				}
			});

			if ($scope.isEmptySelection) {
				createSampleModalService.showErrorMessage('', $.fieldbookMessages.errorNotSelectedInstance);
			} else {
				createSampleModalService.openSelectSelectionVariableToSampleListModal(instanceNumbers);
				$uibModalInstance.close();
			}
		};

		$scope.init = function () {
			environmentService.getEnvironments().then(function (environmentDetails) {
				$scope.instances = environmentDetails;
			});
		};

		$scope.init();

	}]);

	createSampleModule.controller('selectSelectionVariableToSampleListModalCtrl', ['$scope', 'TrialManagerDataService', '$http', '$timeout', 'studyContext', 'createSampleModalService', 'instanceNumbers',
		function ($scope, TrialManagerDataService, $http, $timeout, studyContext, createSampleModalService, instanceNumbers) {

		$scope.backToCreateSample = function () {
			createSampleModalService.openSelectEnvironmentToSampleListModal(studyContext.measurementDatasetId);
		};

		$scope.init = function () {

			$scope.selectionVariables = TrialManagerDataService.settings.selectionVariables.m_keys;
			$scope.saveSampleListButton = false;
			$scope.dateSampling = '';

			$scope.variables = [];
			$scope.users = [];

			$scope.selectedUser = undefined;
			$scope.variableSelected = undefined;
			$scope.listOwner = '';

			if ($scope.selectionVariables.length !== 0) {
				angular.forEach($scope.selectionVariables, function (variableId) {
					if (TrialManagerDataService.settings.selectionVariables.m_vals[parseInt(variableId)].variable.dataType === "Numeric") {
						$scope.variables.push(TrialManagerDataService.settings.selectionVariables.m_vals[parseInt(variableId)].variable);
					}
				});
			}

			if ($scope.variables.length === 0) {
				createSampleModalService.showErrorMessage('', $.fieldbookMessages.errorNoVarietiesSamples);
			}

			$http.get('/bmsapi/projects/' + currentProgramId + '/users').success(function (data) {
				$scope.users = data;

				angular.forEach($scope.users, function (user) {
					if (user.id === loggedInUserId) {
						$scope.selectedUser = user.id;
						$scope.listOwner = user.firstName + " " + user.lastName;
					}
					$timeout(function () {
						angular.element('#sampleSelectUser').select2();

					}, 1);
				});
			}).error(function (data) {
				if (data.status === 401) {
					bmsAuth.handleReAuthentication();
				}
				createSampleModalService.showErrorMessage('', data.errors[0].message);
				$scope.selectedUser = [];
			});

			$timeout(function () {
				angular.element('#sampleSelectVariable').focus();
				angular.element('#sampleSelectVariable').select2();
				angular.element('#sampleSelectSamplingDate').datepicker({dateFormat: "yyyy-mm-dd"}).val('');
			}, 1);
		};

		$scope.continue = function () {
			$scope.saveSampleListButton = true;
			$scope.sampleList = {
				"description": "",
				"notes": "",
				"createdBy": $scope.listOwner,
				"selectionVariableId": $scope.variableSelected.cvTermId,
				"instanceIds": instanceNumbers,
				"takenBy": "",
				"samplingDate": $scope.dateSampling,
				"datasetId": studyContext.measurementDatasetId,
				"cropName": cropName,
				"programUUID": currentProgramId,
				"parentId": 0,
				"listName": "",
				"createdDate": ""
			};

			if ($scope.selectedUser !== null) {
				angular.forEach($scope.users, function (user) {
					if (user.id === $scope.selectedUser) {
						$scope.sampleList.takenBy = user.username;
					}
				});
			}
			$scope.saveSampleListButton = false;
			SaveSampleList.openSaveSampleListModal($scope.sampleList);
		};

		$scope.init();
	}]);

})();
