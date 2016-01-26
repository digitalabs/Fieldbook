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

        $scope.locationIds = [];

        //NOTE: Continue action for navigate from Locations to Advance Study Modal
        $scope.trialSelectEnviornmentContinue = function(){

            var selectedLocationIds=[];
            angular.forEach($scope.locationIds,function(id){
                if(id != undefined){
                    selectedLocationIds.push(id);
                }
            });

            trialSelectEnviornmentContinue(selectedLocationIds);
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
