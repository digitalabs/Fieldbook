/*global angular, showAlertMessage, showErrorMessage, selectedEnvironmentContinueCreatingSample*/
(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('selectSelectionVariableToSampleListModalCtrl', ['$scope', 'TrialManagerDataService', '$http', '$timeout', 'studyContext', function ($scope,
																																				   TrialManagerDataService, $http, $timeout, studyContext) {

		$scope.backToCreateSample = function () {
			$('#selectSelectionVariableToSampleListModal').modal('hide');
			$('#selectEnvironmentToSampleListModal').modal('show');
		};

		$scope.openToCalendar = function ($event) {
			$event.preventDefault();
			$event.stopPropagation();
		};

		$scope.init = function (trialInstances) {
			$scope.instances = trialInstances;

			$scope.selectionVariables = TrialManagerDataService.settings.selectionVariables.m_keys;
			$scope.saveSampleListButton = false;
			$scope.dateSampling = '';

			$scope.variables = [];
			$scope.users = [];

			$scope.selectedUser = undefined;
			$scope.variableSelected = undefined;
			$scope.sampleForm.$setPristine();
			$scope.listOwner = '';

			if ($scope.selectionVariables.length !== 0) {
				angular.forEach($scope.selectionVariables, function (variableId) {
					if (TrialManagerDataService.settings.selectionVariables.m_vals[parseInt(variableId)].variable.dataType === "Numeric") {
						$scope.variables.push(TrialManagerDataService.settings.selectionVariables.m_vals[parseInt(variableId)].variable);
					}
				});
			}

			if ($scope.variables.length === 0) {
				showErrorMessage('', $.fieldbookMessages.errorNoVarietiesSamples);
				$scope.sampleForm.selectVariableManageSample.$setDirty();
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
				if (data.status == 401) {
					bmsAuth.handleReAuthentication();
				}
				showErrorMessage('', data.errors[0].message);
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
				"instanceIds": $scope.instances,
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
	}]);
})();
