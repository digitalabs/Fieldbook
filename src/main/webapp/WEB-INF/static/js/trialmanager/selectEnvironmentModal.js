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

        $scope.TRIAL_LOCATION_NO_INDEX = 8180;

        $scope.data = TrialManagerDataService.currentData.environments;

        $scope.trialInstances = [];

        //NOTE: Continue action for navigate from Locations to Advance Study Modal
        $scope.trialSelectEnviornmentContinue = function(){
            var isTrialInstanceSelected = false;
            var selectedTrialInstances=[];

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
                angular.forEach($scope.trialInstances,function(id){
                    if(id != undefined){
                        selectedTrialInstances.push(id);
                    }
                });

                trialSelectEnviornmentContinue(selectedTrialInstances);
            }





        };

        $scope.doSelectAll = function() {
            $scope.locationIds = [];
            if ($scope.selectAll) {
                $scope.selectAll = true;

            } else {
                $scope.selectAll = false;
                $scope.locationIds = [];
            }
            angular.forEach($scope.data.environments, function(env) {
                env.Selected = $scope.selectAll;
                if($scope.selectAll){
                    $scope.locationIds.push(env.locationId);
                }
            });

        };

    }]);

})();
