/**
 * Created by cyrus on 7/1/14.
 */

/*global angular*/

(function(){
    'use strict';

    var manageTrialApp = angular.module('manageTrialApp');

    manageTrialApp.controller('TreatmentCtrl',['$scope','TreatmentFactorsService',function($scope,TreatmentFactorsService) {
        $scope.treatmentFactors = TreatmentFactorsService.treatmentFactors;

        $scope.onLevelChange = function(variableName,levelCount) {
            // search the treatment factors then update it
            for (var i = 0; i < $scope.treatmentFactors.length; i++) {
                if ($scope.treatmentFactors[i].variableName === variableName) {

                    if (isNaN(levelCount)) { return; }

                    var diff = Math.abs($scope.treatmentFactors[i].variableLabelsForEachCount.length - parseInt(levelCount));

                    // remove items if no of levels is less thant array
                    if ($scope.treatmentFactors[i].variableLabelsForEachCount.length  > parseInt(levelCount)) {
                        while($scope.treatmentFactors[i].variableLabelsForEachCount.length > parseInt(levelCount)) {
                            $scope.treatmentFactors[i].variableLabelsForEachCount.pop();
                        }
                    }

                    // add items if no of levels is more thant array
                    else {
                        for (var j = 0; j < diff; j++) {
                            $scope.treatmentFactors[i].variableLabelsForEachCount.push('');
                        }
                    }

                    break;
                }
            }
        }; // end $scope.onLevelChange

        $scope.addTreatmentFactor = function() {
            TreatmentFactorsService.addDummyTreatmentFactor();
        };

        $scope.removeTreatmentFactorByIndex = TreatmentFactorsService.removeTreatmentFactorByIndex;

    }]);



})();



