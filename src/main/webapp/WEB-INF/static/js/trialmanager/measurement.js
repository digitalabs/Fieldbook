/**
 * Created by cyrus on 7/2/14.
 */

/* global angular */
(function(){
    'use strict';

    angular.module('manageTrialApp').controller('MeasurementsCtrl',
        ['$scope', 'TrialManagerDataService', function($scope, TrialManagerDataService) {

            $scope.data = TrialManagerDataService.currentData.measurements;

            $scope.updateOccurred = false;

            $scope.$on('deleteOccurred', function() {
                $scope.updateOccurred = true;
            });

            $scope.$on('variableAdded', function() {
                $scope.updateOccurred = true;
            });
        }]);
})();