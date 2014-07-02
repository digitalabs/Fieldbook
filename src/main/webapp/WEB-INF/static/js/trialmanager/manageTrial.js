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
                when('/', {
                    templateUrl: '/Fieldbook/TrialManager/createTrial/trialSettings',
                    controller: 'TrialSettingsCtrl'
                }).

                when('/environment', {
                    templateUrl: '/Fieldbook/TrialManager/createTrial/environment'
                }).
                when('/germplasm', {
                    templateUrl: '/Fieldbook/TrialManager/createTrial/germplasm'
                }).
                when('/treatment', {
                    templateUrl: '/Fieldbook/TrialManager/createTrial/treatment',
                    controller: 'TreatmentCtrl'
                }).
                when('/experimentalDesign', {
                    templateUrl: '/Fieldbook/TrialManager/createTrial/experimentalDesign'
                }).
                when('/measurements', {
                    templateUrl: '/Fieldbook/TrialManager/createTrial/measurements',
                    controller: 'MeasurementCtrl'
                }).
                otherwise({
                    redirectTo: '/Fieldbook/TrialManager/createTrial/trialSettings'
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

})();



