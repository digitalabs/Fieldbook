(function() {
    'use strict';

    var manageTrialApp = angular.module('manageTrialApp');

    manageTrialApp.controller('SelectEnvironmentModalCtrl', ['$scope', 'TrialManagerDataService', function($scope, TrialManagerDataService) {

        $scope.settings = TrialManagerDataService.settings.environments;
        if (Object.keys($scope.settings).length === 0) {
            $scope.settings = {};
            $scope.settings.managementDetails = [];
            $scope.settings.trialConditionDetails = [];
        }

        $scope.TRIAL_LOCATION_NAME_INDEX = 8180;
        $scope.TRIAL_LOCATION_ABBR_INDEX = 8189;
        $scope.TRIAL_INSTANCE_INDEX = 8170;
        $scope.PREFERENCED_LOCATION_VARIABLE=8170;

        $scope.data = TrialManagerDataService.currentData.environments;

        $scope.trialInstances = [];

        $scope.noOfReplications = TrialManagerDataService.currentData.experimentalDesign.replicationsCount;

        if($scope.settings.managementDetails.val($scope.TRIAL_LOCATION_NAME_INDEX) != null){
            $scope.PREFERENCED_LOCATION_VARIABLE = $scope.TRIAL_LOCATION_NAME_INDEX;
        }
        else if($scope.settings.managementDetails.val($scope.TRIAL_LOCATION_ABBR_INDEX) != null){
            $scope.PREFERENCED_LOCATION_VARIABLE = $scope.TRIAL_LOCATION_ABBR_INDEX;
        }

        //NOTE: Continue action for navigate from Locations to Advance Study Modal
        $scope.trialSelectEnvironmentContinue = function(){

            // Do not go ahead for Advancing unless trial has experimental design & number of replications variables
            if(TrialManagerDataService.currentData.experimentalDesign.designType == null || TrialManagerDataService.currentData.experimentalDesign.replicationsCount == null) {
                showAlertMessage('', advanceListUnableToGenerateWarningMessage);
                return;
            }

            var isTrialInstanceSelected = false;
            var selectedTrialInstances=[];
            var selectedLocationDetails = [];
            angular.forEach($scope.trialInstances,function(id){
                if(id != undefined && !isTrialInstanceSelected){
                    isTrialInstanceSelected = true;
                }
            });

            if(!isTrialInstanceSelected){
                var selectOneLocationErrorMessagge = 'Please select at least one location for Advancing Trial';
                showErrorMessage('', selectOneLocationErrorMessagge);
            }
            else{
                selectedLocationDetails.push($scope.settings.managementDetails.val($scope.PREFERENCED_LOCATION_VARIABLE).variable.name);

                angular.forEach($scope.trialInstances,function(trialInstanceNumber,idx){
                    if(trialInstanceNumber != undefined){
                        selectedTrialInstances.push(trialInstanceNumber);

                        angular.forEach($scope.data.environments, function(env,position) {
                            if(position==idx){
                                selectedLocationDetails.push(env.managementDetailValues[$scope.PREFERENCED_LOCATION_VARIABLE]);
                            }
                        });

                    }
                });

                var isTrialInstanceNumberUsed = false;
                if($scope.PREFERENCED_LOCATION_VARIABLE == 8170){
                    isTrialInstanceNumberUsed = true;
                }
                trialSelectEnviornmentContinue(selectedTrialInstances,$scope.noOfReplications,selectedLocationDetails,isTrialInstanceNumberUsed);
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
                if($scope.selectAll){
                    $scope.trialInstances.push(env.managementDetailValues[$scope.TRIAL_INSTANCE_INDEX]);
                }
            });

        };

    }]);

})();
