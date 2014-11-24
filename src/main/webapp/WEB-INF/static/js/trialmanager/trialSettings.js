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

        $scope.options = {
            selectAll : false
        };

        $scope.doSelectAll = function() {
            var filteredVariables = $filter('removeHiddenAndDeletablesVariableFilter')($scope.settings.keys(),$scope.settings.vals());

            _.each(filteredVariables,function(cvTermID){
                $scope.settings.val(cvTermID).isChecked = $scope.options.selectAll;
            });

        };

        $scope.removeSettings = function() {
            TrialManagerDataService.removeSettings(1,$scope.settings).then(function(data) {
                _(data).each(function(ids) {
                    delete $scope.data.userInput[ids];
                });

                $scope.options.selectAll = false;
            });

        };


        $scope.managementDetailsSize = function () {
            return $scope.settings.length();
        };

    }]);


})();



