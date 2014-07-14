/**
 * Created by cyrus on 7/1/14.
 */

/*global angular*/
/*global showBaselineTraitDetailsModal*/
(function(){
    'use strict';

    var manageTrialApp = angular.module('manageTrialApp');

    manageTrialApp.controller('TreatmentCtrl',['$scope','TrialManagerDataService',function($scope,TrialManagerDataService) {

        $scope.settings = TrialManagerDataService.settings.treatmentFactors;
        $scope.currentData = TrialManagerDataService.currentData.treatmentFactors;

        // watch $scope.settings, since we are sure that $scope.settings is an orderedhash even empty, we could just
        // use $watchCollection, for every added change we retrieve the 'AMOUNT ' pairs dynamically. also creat a
        // store to $scope.currentData for the variable levels.

        // initialize currentData if has no values yet
        if (typeof $scope.currentData === 'undefined') {
            $scope.currentData = {};
        }

        $scope.$watchCollection('settings.m_keys',function(newArr,oldArr) {
            if (newArr.length > oldArr.length) {
                $scope.settings.val(newArr[newArr.length-1]).labels = [];
                $scope.settings.val(newArr[newArr.length-1]).levels = 0;

            }
        });

        $scope.onLevelChange = function(key,levels) {


            if (isNaN(levels)) {
                return;
            }

            levels = parseInt(levels);

            var diff = Math.abs($scope.settings.val(key).labels.length - levels);

            // remove items if no of levels is less thant array
            if ($scope.settings.val(key).labels.length > levels) {
                while ($scope.settings.val(key).labels.length > levels) {
                    $scope.settings.val(key).labels.pop();
                }
            }

            // add items if no of levels is more thant array
            else {
                for (var j = 0; j < diff; j++) {
                    $scope.settings.val(key).labels.push('');
                }
            }

        }; // end $scope.onLevelChange

        $scope.removeTreatmentFactorByIndex = function(cvTermId) {
            // remove an item from the service
        };

    }]);

})();



