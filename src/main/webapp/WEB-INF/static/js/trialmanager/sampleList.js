/*global angular, showAlertMessage, showErrorMessage, trialSelectedEnvironmentContinueCreatingSample*/
(function() {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('SampleListController', ['$scope', 'TrialManagerDataService', 'environmentService', function($scope,
	TrialManagerDataService, environmentService) {

		$scope.settings = TrialManagerDataService.settings.environments;
		if (Object.keys($scope.settings).length === 0) {
			$scope.settings = {};
			$scope.settings.managementDetails = [];
			$scope.settings.trialConditionDetails = [];
		}

		$scope.userInput = TrialManagerDataService.currentData.trialSettings.userInput;
		$scope.trialSettings = TrialManagerDataService.settings.trialSettings;

		$scope.TRIAL_LOCATION_NAME_INDEX = 8180;
		$scope.TRIAL_INSTANCE_INDEX = 8170;
		$scope.PREFERRED_LOCATION_VARIABLE = 8170;
		$scope.LOCATION_NAME_ID = 8190;

		$scope.data = TrialManagerDataService.currentData.environments;

		$scope.trialInstances = [];

		$scope.noOfReplications = TrialManagerDataService.currentData.experimentalDesign.replicationsCount;

		//NOTE: Continue action for navigate from Locations to Sample List Modal
		$scope.continueCreatingSample = function() {

			var isTrialInstanceSelected = false;
			var selectedTrialInstances = [];
			var selectedLocationDetails = [];
			angular.forEach($scope.trialInstances, function(id) {
				if (id && !isTrialInstanceSelected) {
					isTrialInstanceSelected = true;
				}
			});

			if (!isTrialInstanceSelected) {
				showErrorMessage('', selectOneLocationErrorMessage);
			} else {
				if ($scope.locationFromTrialSettings) {
					selectedLocationDetails
						.push($scope.trialSettings.val($scope.PREFERRED_LOCATION_VARIABLE).variable.name);
				} else {
					selectedLocationDetails
						.push($scope.settings.managementDetails.val($scope.PREFERRED_LOCATION_VARIABLE).variable.name);
				}

				angular.forEach($scope.trialInstances, function(trialInstanceNumber, idx) {
					if (trialInstanceNumber) {
						selectedTrialInstances.push(trialInstanceNumber);

						if ($scope.locationFromTrialSettings) {
							selectedLocationDetails.push($scope.userInput[$scope.PREFERRED_LOCATION_VARIABLE]);
						} else {
							angular.forEach($scope.data.environments, function(env, position) {
								if (position === idx) {
									selectedLocationDetails.push(env.managementDetailValues[$scope.PREFERRED_LOCATION_VARIABLE]);
								}
							});
						}

					}
				});

				var isTrialInstanceNumberUsed = false;
				if ($scope.PREFERRED_LOCATION_VARIABLE === 8170) {
					isTrialInstanceNumberUsed = true;
				}
				trialSelectedEnvironmentContinueCreatingSample(selectedTrialInstances, $scope.noOfReplications, selectedLocationDetails,
					isTrialInstanceNumberUsed);
			}

		};

		$scope.doSelectAll = function() {
			$scope.trialInstances = [];
			$scope.trialInstancesName = [];
			if ($scope.selectAll) {
				$scope.selectAll = true;
			} else {
				$scope.selectAll = false;
				$scope.trialInstances = [];
			}
			angular.forEach($scope.data.environments, function(env) {
				env.Selected = $scope.selectAll;
				if ($scope.selectAll) {
					$scope.trialInstances.push(env.managementDetailValues[$scope.TRIAL_INSTANCE_INDEX]);
				}
			});

		};

		$scope.init = function() {
			$scope.locationFromTrialSettings = false;
			$scope.locationFromTrial = false;
          //  $scope.changeEnvironments();
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
        $scope.variableRequired = false;
		$scope.data = {
            dateSampling: {},
			variables: {},
            variableSelected: null

        };

		$scope.backToCreateSample = function() {
			$('#managerSampleModal').modal('hide');
			$('#selectEnvironmentToSampleListModal').modal('show');
        }

		$scope.openToCalendar = function($event) {
			$event.preventDefault();
			$event.stopPropagation();

			//$scope.data.toCalendarOpened = true;
		};

		// TODO see Workbench/src/main/web/src/apps/ontology/app-services/bmsAuth.js
        $scope.init = function () {
            var xAuthToken = JSON.parse(localStorage["bms.xAuthToken"]).token;

            var config = {
                headers: {
                    'X-Auth-Token': xAuthToken
                }
            };
            $http.get('/bmsapi/ontology/' + cropName + '/filtervariables?programId=' + currentProgramId + '&dataTypeIds=1110&variableTypeIds=1801',config).success(function (data) {
                $scope.data.variables = data;
            }).error(function () {
                showErrorMessage('', $.fieldbookMessages.errorNoVarietiesSamples);
                $scope.variableSelected = [];
                $scope.variableRequired = true;
            });


            $http.get('/bmsapi/user/list?projectUUID=' + currentProgramId ,config).success(function (data) {
                $scope.users = data;

                angular.forEach($scope.users, function(user) {
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
            var message = 'Sample list created successfully!';
                showSuccessfulMessage('', message);
		};

	}]);
})();
