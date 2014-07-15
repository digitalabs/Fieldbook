/**
 * Created by cyrus on 7/1/14.
 */

/*global angular*/
/*global showBaselineTraitDetailsModal*/
(function(){
    'use strict';

    var manageTrialApp = angular.module('manageTrialApp');

    manageTrialApp.controller('TreatmentCtrl',['$scope','TrialManagerDataService','_',function($scope,TrialManagerDataService,_) {

        $scope.settings = TrialManagerDataService.settings.treatmentFactors;
        $scope.currentData = TrialManagerDataService.currentData.treatmentFactors;

        // watch $scope.settings, since we are sure that $scope.settings is an orderedhash even empty, we could just
        // use $watchCollection, for every added change we retrieve the 'AMOUNT ' pairs dynamically. also creat a
        // store to $scope.currentData for the variable levels.

        // initialize currentData if has no values yet
        if (typeof $scope.currentData === 'undefined') {
            $scope.currentData = {};
        }
        
        $scope.addVariable = !TrialManagerDataService.trialMeasurement.hasMeasurement;
        
        // note for some reasons this gets called twice :( , might be the diff function causing the watchCollection t
        $scope.$watchCollection(function(){return $scope.settings.m_keys; },function(newArr,oldArr){
            // add
            if (newArr.length > oldArr.length) {
                angular.forEach(_(newArr).difference(oldArr),function(val,key) {
                    $scope.currentData[val] = {
                        levels: 0,
                        labels: [],
                        pairCvTermId: 0
                    };
                });
            }
            // delete
            else {
                angular.forEach(_(oldArr).difference(newArr),function(val,key) {
                    delete $scope.currentData[val];
                });
            }

        });

        $scope.onLevelChange = function(key,levels) {


            if (isNaN(levels)) {
                return;
            }

            levels = parseInt(levels);

            var diff = Math.abs($scope.currentData[key].labels.length - levels);

            // remove items if no of levels is less thant array
            if ($scope.currentData[key].labels.length > levels) {
                while ($scope.currentData[key].labels.length > levels) {
                    $scope.currentData[key].labels.pop();
                }
            }

            // add items if no of levels is more thant array
            else {
                for (var j = 0; j < diff; j++) {
                    $scope.currentData[key].labels.push('');
                }
            }

        }; // end $scope.onLevelChange

        $scope.removeTreatmentFactorByIndex = function(cvTermId) {
            // remove an item from the service
        };

    }]);

})();

