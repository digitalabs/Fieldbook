/**
 * Created by cyrus on 7/1/14.
 */

/*global angular*/

(function(){
    'use strict';

    var treatmentApp = angular.module('treatmentApp', ['fieldbook-settings']);

    treatmentApp.controller('TreatmentCtrl',['$scope','TreatmentFactorsService',function($scope,TreatmentFactorsService) {
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

    treatmentApp.filter('range', function() {
        return function(input, total) {
            total = parseInt(total);
            for (var i=0; i<total; i++) { input.push(i); }

            return input;
        };
    });

    treatmentApp.factory('TreatmentFactorsService',function() {
        return {
            treatmentFactors : [
                {
                    variableName : "FERTILE",
                    variableLink : "/somewhere/in/the/galaxy",
                    variableDescription : "Fertilizer level",
                    variableScale : "kg/hectars",
                    variableLevelCount : 0,
                    variableLabelsForEachCount : []
                },

                {
                    variableName : "PDATE",
                    variableLink : "/somewhere/in/the/galaxy",
                    variableDescription : "Planting date",
                    variableScale : "Date",
                    variableLevelCount : 0,
                    variableLabelsForEachCount : []
                }
            ],

            // TODO: if factors var has no value, we should retrieve it from spring service
            addDummyTreatmentFactor : function() {
                this.addTreatmentFactor({
                    variableName : "DUMMY_BAKA",
                    variableLink : "/somewhere/in/the/galaxy",
                    variableDescription : "You dummy!!! BAKA!",
                    variableScale : "Hmmph",
                    variableLevelCount : 3,
                    variableLabelsForEachCount : ['a','b','c']

                });
            },

            addTreatmentFactor : function(treatmentFactor) {
                this.treatmentFactors.push(treatmentFactor);
            },

            removeTreatmentFactorByIndex : function(index) {
                if ( index !== null) {
                    this.treatmentFactors.splice(index, 1);
                }
            },

            removeTreatmentFactor : function(treatmentFactorVariableName) {
                if ( treatmentFactorVariableName !== null) {
                    for(var i = this.treatmentFactors.length; i--;) {
                        if(this.treatmentFactors[i] === treatmentFactorVariableName) {
                            this.treatmentFactors.splice(i, 1);
                        }
                    }
                }
            }
        };
    });

// common directives, might be better to add this section of code to a common module
    treatmentApp.directive('validNumber', function() {
        return {
            require: '?ngModel',
            link: function(scope, element, attrs, ngModelCtrl) {
                if(!ngModelCtrl) {
                    return;
                }

                ngModelCtrl.$parsers.push(function(val) {
                    var clean = val.replace( /[^0-9]+/g, '');
                    if (val !== clean) {
                        ngModelCtrl.$setViewValue(clean);
                        ngModelCtrl.$render();
                    }
                    return clean;
                });

                element.bind('keypress', function(event) {
                    if(event.keyCode === 32) {
                        event.preventDefault();
                    }
                });
            }
        };
    });

})();



