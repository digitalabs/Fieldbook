/**
 * Created by cyrus on 7/2/14.
 */

/* global angular */
(function(){
    'use strict';

    angular.module('manageTrialApp').controller('MeasurementsCtrl',
        ['$scope', 'TrialManagerDataService', function($scope, TrialManagerDataService) {

            $scope.settings = TrialManagerDataService.settings.measurements;

            $scope.$watch(function () {
                return TrialManagerDataService.settings.measurements;
            }, function (newValue) {
                angular.copy(newValue, $scope.settings);
            });

            $scope.updateOccurred = false;
            
            $scope.addVariable = true;
            
            $scope.$on('deleteOccurred', function() {
                $scope.updateOccurred = true;
            });

            $scope.$on('variableAdded', function() {
                $scope.updateOccurred = true;
            });
        }]);
})();