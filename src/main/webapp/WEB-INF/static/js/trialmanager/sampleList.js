/*global angular, showAlertMessage, showErrorMessage, trialSelectedEnvironmentContinueCreatingSample*/
(function() {
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

		$scope.TRIAL_LOCATION_NAME_INDEX = 8180;
		$scope.TRIAL_INSTANCE_INDEX = 8170;
		$scope.PREFERRED_LOCATION_VARIABLE = 8170;
		$scope.LOCATION_NAME_ID = 8190;

		$scope.data = TrialManagerDataService.currentData.environments;

		//NOTE: Continue action for navigate from Locations to Sample List Modal
        $scope.continueCreatingSampleList = function () {

            if ($scope.selectedTrialInstancesBySampleList.length === 0) {
                showErrorMessage('', selectOneLocationErrorMessage);
            }
            trialSelectedEnvironmentContinueCreatingSample($scope.selectedTrialInstancesBySampleList);
        };

		$scope.doSelectAll = function() {
			if ($scope.selectAll) {
				$scope.selectAll = true;
			} else {
                $scope.selectAll = false;
			}
			angular.forEach($scope.data.environments, function(env) {
				env.Selected = $scope.selectAll;
				if ($scope.selectAll) {
				    if(!$scope.selectedTrialInstancesBySampleList.includes(String(env.managementDetailValues[$scope.TRIAL_INSTANCE_INDEX]))){
                        $scope.doSelectInstance(env.managementDetailValues[$scope.TRIAL_INSTANCE_INDEX]);
                    }
				}else{
                    $scope.doSelectInstance(env.managementDetailValues[$scope.TRIAL_INSTANCE_INDEX]);
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

            if ($scope.settings.managementDetails.val($scope.TRIAL_LOCATION_NAME_INDEX) != null) {
                // LOCATION_NAME from environments
                $scope.PREFERRED_LOCATION_VARIABLE = $scope.TRIAL_LOCATION_NAME_INDEX;
                $scope.locationFromTrial = true;
            } else if ($scope.trialSettings.val($scope.TRIAL_LOCATION_NAME_INDEX) != null) {
                // LOCATION_NAME from trial settings
                $scope.PREFERRED_LOCATION_VARIABLE = $scope.TRIAL_LOCATION_NAME_INDEX;

                $scope.locationFromTrialSettings = true;
            } else {
                $scope.PREFERRED_LOCATION_VARIABLE = $scope.TRIAL_INSTANCE_INDEX;
            }
        };
        $scope.init();
	}]);

    manageTrialApp.controller('ManagerSampleListController', ['$scope', 'TrialManagerDataService', '$http', function ($scope,
                                                                                                                      TrialManagerDataService, $http) {
        var xAuthToken = JSON.parse(localStorage["bms.xAuthToken"]).token;

        var config = {
            headers: {
                'X-Auth-Token': xAuthToken
            }
        };

		$scope.backToCreateSample = function() {
			$('#managerSampleListModal').modal('hide');
			$('#selectEnvironmentToSampleListModal').modal('show');
        }

		$scope.openToCalendar = function($event) {
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

            $scope.variables = {};
            $scope.variableSelected = undefined;
            $scope.sampleList = {
                "description": "",
                "notes": "",
                "createdBy": "",
                "selectionVariableId": 0,
                "instanceIds": [
                    0
                ],
                "takenBy": "",
                "samplingDate": "",
                "studyId": 0,
                "cropName": ""
            };

            $scope.sampleForm.$setPristine();
            if ($scope.selectionVariables.length !== 0) {
                $http.get('/bmsapi/ontology/' + cropName + '/filtervariables?programId=' + currentProgramId + '&dataTypeIds=1110&variableTypeIds=1807', config).success(function (data) {
                    $scope.variables = data;

                    for (var i = $scope.variables.length - 1; i >= 0; i--) {
                        if (!$scope.selectionVariables.includes(parseInt($scope.variables[i].id))) {
                            $scope.variables.splice(i, 1);
                        }
                    }

                }).error(function () {
                    showErrorMessage('', $.fieldbookMessages.errorNoVarietiesSamples);
                    $scope.variables = {};
                    $scope.variableSelected = undefined;
                });
            } else {
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

		$scope.saveSample = function() {
            $scope.saveSampleListButton = true;
            $scope.sampleList.studyId = $scope.studyId;
            $scope.sampleList.selectionVariableId = $scope.variableSelected.id;
            $scope.sampleList.instanceIds = $scope.instances;
            $scope.sampleList.samplingDate = $scope.dateSampling;
            $scope.sampleList.cropName = cropName;

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
            }).error(function () {
                showErrorMessage('', $.fieldbookMessages.errorSaveSamplesList);
                $scope.saveSampleListButton = false;
            });
            $('#managerSampleListModal').modal('hide');
        };
	}]);

})();
