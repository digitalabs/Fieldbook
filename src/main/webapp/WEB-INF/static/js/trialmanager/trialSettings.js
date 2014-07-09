/**
 * Created by cyrus on 7/2/14.
 */

/*global angular*/

(function(){
    'use strict';

    var manageTrialApp = angular.module('manageTrialApp');

    manageTrialApp.controller('TrialSettingsCtrl',['$scope','TrialManagerDataService',function($scope,TrialManagerDataService) {

        $scope.settings = TrialManagerDataService.settings.trialSettings;
        $scope.currentData = TrialManagerDataService.currentData.trialSettings;

        $scope.removeVariable = function(cvTermId) {
            // remove the equivalent setting
            $scope.settings.remove(cvTermId);

            // remove the equivalent current data


        };

        $scope.managementDetailsSize = function() {
            var size = 0, key;
            for (key in $scope.data.managementDetails) {
                if ($scope.data.managementDetails.hasOwnProperty(key)) { size++; }
            }
            return size;
        };

    }]);



})();



