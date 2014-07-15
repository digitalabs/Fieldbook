/**
 * Created by cyrus on 7/1/14.
 */

/*global angular*/
/*global showBaselineTraitDetailsModal*/
(function(){
    'use strict';

    var manageTrialApp = angular.module('manageTrialApp');

    manageTrialApp.controller('TreatmentCtrl',['$scope','TrialManagerDataService','_','$http',function($scope,TrialManagerDataService,_,$http) {

        $scope.settings = TrialManagerDataService.settings.treatmentFactors;
        $scope.currentData = TrialManagerDataService.currentData.treatmentFactors;

        // watch $scope.settings, since we are sure that $scope.settings is an orderedhash even empty, we could just
        // use $watchCollection, for every added change we retrieve the 'AMOUNT ' pairs dynamically. also creat a
        // store to $scope.currentData for the variable levels.

        // initialize currentData if has no values yet
        if (typeof $scope.currentData === 'undefined') {
            $scope.currentData = {};
        }

        $scope.onAddVariable = function(result) {

            angular.forEach(result,function(val,key) {
                $scope.currentData[key] = {
                    levels: 0,
                    labels: [],
                    pairCvTermId: 0,
                    possiblePairs: new angular.OrderedHash()
                };

                TrialManagerDataService.retrieveVariablePairs(key).then(function(data) {

                    angular.forEach(data,function(val1,key1) {
                        $scope.currentData[key].possiblePairs.push(val1.variable.cvTermId,val1);


                    });
                });

            });

        };

        
        $scope.addVariable = !TrialManagerDataService.trialMeasurement.hasMeasurement;
        
        // note for some reasons this gets called twice :( , might be the diff function causing the watchCollection t
        $scope.$watchCollection(function(){return $scope.settings.m_keys; },function(newArr,oldArr){
            // add
            if (newArr.length > oldArr.length) {

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

