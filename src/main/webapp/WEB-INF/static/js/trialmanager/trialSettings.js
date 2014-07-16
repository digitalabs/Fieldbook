/**
 * Created by cyrus on 7/2/14.
 */

/*global angular*/

(function(){
    'use strict';

    var manageTrialApp = angular.module('manageTrialApp');

    manageTrialApp.controller('TrialSettingsCtrl',['$scope','TrialManagerDataService',function($scope,TrialManagerDataService) {

        $scope.settings = TrialManagerDataService.settings.trialSettings;
        $scope.data = TrialManagerDataService.currentData.trialSettings;

        $scope.addVariable = true;

        $scope.$watch(function () {
            return TrialManagerDataService.currentData.trialSettings;
        }, function (newValue) {
        	if ($scope.data !== newValue) {
        		angular.copy(newValue, $scope.data);
            }            
        });

        $scope.$watch(function () {
            return TrialManagerDataService.settings.trialSettings;
        }, function (newValue) {
            if ($scope.settings !== newValue) {
                angular.copy(newValue, $scope.settings);
            }
        });
        
        $scope.removeVariable = function(cvTermId) {
            // remove the equivalent setting
            $scope.settings.remove(cvTermId);
            $.ajax({
                url: '/Fieldbook/manageSettings/deleteVariable/1/' + cvTermId,
                type: 'POST',
                cache: false,
                data: '',
                contentType: 'application/json',
                success: function () {
                }
            });

            // remove the equivalent current data
            delete $scope.data.userInput[cvTermId];
        };



        $scope.managementDetailsSize = function() {
            return $scope.settings.length();
        };

    }]);



})();



