/**
 * Created by cyrus on 7/1/14.
 */

/*global angular*/

(function(){
    'use strict';

    var manageTrialApp = angular.module('manageTrialApp', ['fieldbook-settings','ngRoute']);

    // routing configuration
    manageTrialApp.config(['$routeProvider',
        function($routeProvider) {
            $routeProvider.
                when('/chooseSettings', {
                    templateUrl: '/Fieldbook/TrialManager/chooseSettings'

                }).
                when('/treatment', {
                    templateUrl: '/Fieldbook/TrialManager/treatment',
                    controller: 'TreatmentCtrl'
                }).
                when('/measurement', {
                    templateUrl: '/Fieldbook/TrialManager/measurement',
                    controller: 'MeasurementCtrl'
                }).
                otherwise({
                    redirectTo: '/'
                });
        }]);


    // common directives, might be better to add this section of code to a common module
    manageTrialApp.directive('validNumber', function() {
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

    // common filters
    manageTrialApp.filter('range', function() {
        return function(input, total) {
            total = parseInt(total);
            for (var i=0; i<total; i++) { input.push(i); }

            return input;
        };
    });

    // factory and services
    manageTrialApp.factory('TreatmentFactorsService',function() {
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

})();



