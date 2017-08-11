/*global angular, showAlertMessage, showErrorMessage, trialSelectedEnvironmentContinueCreatingSample*/
(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('SampleListController', ['$scope', 'TrialManagerDataService', function ($scope,
																									  TrialManagerDataService) {

		$scope.selectedTrialInstancesBySampleList = [];
		$scope.settings = TrialManagerDataService.settings.environments;

		if (Object.keys($scope.settings).length === 0) {
			$scope.settings = {};
			$scope.settings.managementDetails = [];
			$scope.settings.trialConditionDetails = [];
		}

		$scope.trialSettings = TrialManagerDataService.settings.trialSettings;

		$scope.TRIAL_LOCATION_NAME_ID = 8180;
		$scope.TRIAL_INSTANCE_ID = 8170;
		$scope.PREFERRED_LOCATION_VARIABLE = 8170;
		$scope.LOCATION_NAME_ID = 8190;

		$scope.data = TrialManagerDataService.currentData.environments;

		$scope.continueCreatingSampleList = function () {

			if ($scope.selectedTrialInstancesBySampleList.length === 0) {
				showErrorMessage('', selectOneLocationErrorMessage);
			}
			trialSelectedEnvironmentContinueCreatingSample($scope.selectedTrialInstancesBySampleList);
		};

		$scope.doSelectAll = function () {
			if ($scope.selectAll) {
				$scope.selectAll = true;
			} else {
				$scope.selectAll = false;
			}
			angular.forEach($scope.data.environments, function (env) {
				env.Selected = $scope.selectAll;
				if ($scope.selectAll) {
					if (!$scope.selectedTrialInstancesBySampleList.includes(String(env.managementDetailValues[$scope.TRIAL_INSTANCE_ID]))) {
						$scope.doSelectInstance(env.managementDetailValues[$scope.TRIAL_INSTANCE_ID]);
					}
				} else {
					$scope.doSelectInstance(env.managementDetailValues[$scope.TRIAL_INSTANCE_ID]);
				}
			});

		};

		$scope.doSelectInstance = function (trialInstance) {
			if ($scope.selectedTrialInstancesBySampleList.length != 0) {
				if ($scope.selectedTrialInstancesBySampleList.includes(String(trialInstance))) {
					for (var i = $scope.selectedTrialInstancesBySampleList.length - 1; i >= 0; i--) {
						if ($scope.selectedTrialInstancesBySampleList[i].includes(String(trialInstance))) {
							$scope.selectedTrialInstancesBySampleList.splice(i, 1);
							$scope.selectAll = false;
						}
					}
				} else {
					$scope.selectedTrialInstancesBySampleList.push(trialInstance);
				}

			} else {
				$scope.selectedTrialInstancesBySampleList.push(trialInstance);
			}
		};

		$scope.init = function () {
			$scope.locationFromTrialSettings = false;
			$scope.locationFromTrial = false;

			if ($scope.settings.managementDetails.val($scope.TRIAL_LOCATION_NAME_ID) != null) {
				// LOCATION_NAME from environments
				$scope.PREFERRED_LOCATION_VARIABLE = $scope.TRIAL_LOCATION_NAME_ID;
				$scope.locationFromTrial = true;
			} else if ($scope.trialSettings.val($scope.TRIAL_LOCATION_NAME_ID) != null) {
				// LOCATION_NAME from trial settings
				$scope.PREFERRED_LOCATION_VARIABLE = $scope.TRIAL_LOCATION_NAME_ID;

				$scope.locationFromTrialSettings = true;
			} else {
				$scope.PREFERRED_LOCATION_VARIABLE = $scope.TRIAL_INSTANCE_ID;
			}
		};
		$scope.init();
	}]);

	manageTrialApp.controller('ManagerSampleListController', ['$scope', 'TrialManagerDataService', '$http', function ($scope,
																													  TrialManagerDataService, $http) {

		// TODO see Workbench/src/main/web/src/apps/ontology/app-services/bmsAuth.js
		var xAuthToken = JSON.parse(localStorage["bms.xAuthToken"]).token;

		var config = {
			headers: {
				'X-Auth-Token': xAuthToken
			}
		};

		$scope.backToCreateSample = function () {
			$('#managerSampleListModal').modal('hide');
			$('#selectEnvironmentToSampleListModal').modal('show');
		}

		$scope.openToCalendar = function ($event) {
			$event.preventDefault();
			$event.stopPropagation();
		};

		// TODO see Workbench/src/main/web/src/apps/ontology/app-services/bmsAuth.js
		$scope.init = function (trialStudyId, trialInstances) {
			$scope.studyId = trialStudyId;
			$scope.instances = trialInstances;

			$scope.selectionVariables = TrialManagerDataService.settings.selectionVariables.m_keys;
			$scope.saveSampleListButton = false;
			$scope.dateSampling = '';

			$scope.variables = [];
			$scope.variableSelected = undefined;
			$scope.sampleForm.$setPristine();

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

			$http.get('/bmsapi/user/list?projectUUID=' + currentProgramId, config).success(function (data) {
				$scope.users = data;

				angular.forEach($scope.users, function (user) {
					if (user.id === loggedInUserId) {
						$scope.selectedUser = user.id;
					}
				});
			}).error(function () {
				showErrorMessage('', $.fieldbookMessages.errorNoVarietiesSamples);
				$scope.selectedUser = [];
			});
		};

		$scope.saveSample = function () {
			$scope.saveSampleListButton = true;
			$scope.sampleList = {
				"description": "",
				"notes": "",
				"createdBy": "",
				"selectionVariableId": $scope.variableSelected.cvTermId,
				"instanceIds": $scope.instances,
				"takenBy": "",
				"samplingDate": $scope.dateSampling,
				"studyId": $scope.studyId,
				"cropName": cropName
			};

			if ($scope.selectedUser !== null) {
				angular.forEach($scope.users, function (user) {
					if (user.id === $scope.selectedUser) {
						$scope.sampleList.takenBy = user.username;
					}
				});
			}

			$http.post('/bmsapi/sample/' + cropName + '/sampleList', JSON.stringify($scope.sampleList), config).success(function (data) {
				var message = 'Sample list created successfully!';
				showSuccessfulMessage('', message);
			}).error(function (data) {
				showErrorMessage('', data.errors[0].message);
				$scope.saveSampleListButton = false;
			});
			$('#managerSampleListModal').modal('hide');
		};
	}]);
})();
