/*global angular, showAlertMessage, showErrorMessage, trialSelectedEnvironmentContinueCreatingSample*/
(function () {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('SampleListController', ['$scope', 'TrialManagerDataService', 'environmentService', function ($scope,
																															TrialManagerDataService, environmentService) {

		$scope.settings = TrialManagerDataService.settings.environments;

		if (Object.keys($scope.settings).length === 0) {
			$scope.settings = {};
			$scope.settings.managementDetails = [];
			$scope.settings.trialConditionDetails = [];
		}

		$scope.selectedTrialInstancesBySampleList = [];

		$scope.trialSettings = TrialManagerDataService.settings.trialSettings;

		$scope.TRIAL_LOCATION_NAME_ID = 8180;
		$scope.TRIAL_INSTANCE_ID = 8170;
		$scope.PREFERRED_LOCATION_VARIABLE = 8170;
		$scope.LOCATION_NAME_ID = 8190;

		$scope.instances = angular.copy(environmentService.environments);

		$scope.continueCreatingSampleList = function () {
			if ($scope.selectedTrialInstancesBySampleList.length === 0) {
				showErrorMessage('', selectOneLocationErrorMessage);
			} else {
				trialSelectedEnvironmentContinueCreatingSample($scope.selectedTrialInstancesBySampleList);
			}
		};

		$scope.doSelectAll = function () {
			$scope.selectedTrialInstancesBySampleList = [];
			var i = 1;
			angular.forEach($scope.instances.environments, function (environment) {
				if ($scope.selectAll) {
					environment.Selected = i;
					i = i + 1;
					$scope.selectedTrialInstancesBySampleList.push(environment.managementDetailValues[$scope.TRIAL_INSTANCE_ID]);
				} else {
					environment.Selected = undefined;
				}
			});
		};

		$scope.doSelectInstance = function(index){
			var environment = $scope.instances.environments[index];
			if(environment.Selected != undefined){
				$scope.selectedTrialInstancesBySampleList.push(environment.managementDetailValues[$scope.TRIAL_INSTANCE_ID]);
			}else{
				$scope.selectAll = false;
				var idx = $scope.selectedTrialInstancesBySampleList.indexOf(String(index + 1));
				$scope.selectedTrialInstancesBySampleList.splice(idx,1);
			}
		};



		$scope.init = function () {
			$scope.locationFromTrialSettings = false;
			$scope.locationFromTrial = false;
			$scope.selectAll = true;

			if ($scope.settings.managementDetails.val($scope.TRIAL_LOCATION_NAME_ID) != null) {
				// LOCATION_NAME from environments
				$scope.PREFERRED_LOCATION_VARIABLE = $scope.TRIAL_LOCATION_NAME_ID;
				$scope.locationFromTrial = true;
			} else {
				$scope.PREFERRED_LOCATION_VARIABLE = $scope.TRIAL_INSTANCE_ID;
			}
			$scope.doSelectAll();
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

		$scope.init = function (trialStudyId, trialInstances) {
			$scope.studyId = trialStudyId;
			$scope.instances = trialInstances;

			$scope.selectionVariables = TrialManagerDataService.settings.selectionVariables.m_keys;
			$scope.saveSampleListButton = false;
			$scope.dateSampling = '';

			$scope.variables = [];
			$scope.users = [];

			$scope.selectedUser = undefined;
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
			}).error(function (data) {
				if (data.status == 401) {
					bmsAuth.handleReAuthentication();
				}
				showErrorMessage('', data.errors[0].message);
				$scope.selectedUser = [];
			});
		};

		$scope.saveSample = function () {
			Spinner.play();
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

			$http.post('/bmsapi/sampleLists/' + cropName + '/sampleList', JSON.stringify($scope.sampleList), config).success(function (data) {
				if (data.id != 0) {
					var message = 'Sample list created successfully!';
					showSuccessfulMessage('', message);
					if ($('#fbk-measurements-controller-div').scope() != undefined) {
						BMS.Fieldbook.MeasurementsDataTable('#measurement-table');
					}
					$('#managerSampleListModal').modal('hide');
					$scope.saveSampleListButton = false;
					Spinner.stop();
				}
			}).error(function (data) {
				if (data.status == 401) {
					bmsAuth.handleReAuthentication();
				}
				showErrorMessage('', data.errors[0].message);
				$scope.saveSampleListButton = false;
				Spinner.stop();
			});
		};
	}]);
})();
