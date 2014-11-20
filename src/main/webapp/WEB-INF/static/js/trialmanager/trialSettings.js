/**
 * Created by cyrus on 7/2/14.
 */

/*global angular*/

(function () {
    'use strict';

    var manageTrialApp = angular.module('manageTrialApp');

    manageTrialApp.controller('TrialSettingsCtrl', ['$scope', 'TrialManagerDataService','_','$filter', function ($scope, TrialManagerDataService,_,$filter) {

        $scope.settings = TrialManagerDataService.settings.trialSettings;
        $scope.data = TrialManagerDataService.currentData.trialSettings;
        $scope.addVariable = true;
        $scope.isSelectAllChecked = false;

        $scope.doSelectAll = function() {
          $scope.isSelectAllChecked = !$scope.isSelectAllChecked;

            var filteredVariables = $filter('removeHiddenAndDeletablesVariableFilter')($scope.settings.keys(),$scope.settings.vals());

            _.each(filteredVariables,function(cvTermID){
                $scope.settings.val(cvTermID).isChecked = $scope.isSelectAllChecked;
            });

        };

        $scope.removeSettings = function() {
            TrialManagerDataService.removeSettings(1,$scope.settings,function(cvTermId) {
                $scope.settings.remove(cvTermId);
                delete $scope.data.userInput[cvTermId];
            });
            $scope.isSelectAllChecked = false;
        };

        $scope.managementDetailsSize = function () {
            return $scope.settings.length();
        };

    }]);


})();



