/**
 * Created by cyrus on 7/1/14.
 */

/*global angular*/

(function(){
    'use strict';

    var manageTrialApp = angular.module('manageTrialApp', ['leafnode-utils','fieldbook-utils','ngRoute','ui.bootstrap']);

    // routing configuration
    // TODO: if possible, retrieve the template urls from the list of constants
    manageTrialApp.config(['$routeProvider',
        function($routeProvider) {
            $routeProvider.
                when('/', {
                    templateUrl: '/Fieldbook/TrialManager/createTrial/trialSettings',
                    controller: 'TrialSettingsCtrl'
                }).

                when('/environment', {
                    templateUrl: '/Fieldbook/TrialManager/createTrial/environment',
                    controller: 'EnvironmentCtrl'
                }).
                when('/germplasm', {
                    templateUrl: '/Fieldbook/TrialManager/createTrial/germplasm',
                    controller: 'GermplasmCtrl'
                }).
                when('/treatment', {
                    templateUrl: '/Fieldbook/TrialManager/createTrial/treatment',
                    controller: 'TreatmentCtrl'
                }).
                when('/experimentalDesign', {
                    templateUrl: '/Fieldbook/TrialManager/createTrial/experimentalDesign'
                }).
                when('/measurements', {
                    templateUrl: '/Fieldbook/TrialManager/createTrial/measurements'
                }).
                otherwise({
                    redirectTo: '/Fieldbook/TrialManager/createTrial/trialSettings'
                });
        }]);

    // common filters
    manageTrialApp.filter('range', function() {
        return function(input, total) {
            total = parseInt(total);
            for (var i=0; i<total; i++) { input.push(i); }

            return input;
        };
    });

    // THE parent controller for the manageTrial (create/edit) page
    manageTrialApp.controller('manageTrialCtrl',['$scope',function($scope){
        $scope.trialTabs = [
            {   'name' : 'Trial Settings',
                'link' : '',
                'active' : 'active' },
            {   'name' : 'Environments',
                'link' : 'environment',
                'active' : '' },
            {   'name' : 'Germplasm',
                'link' : 'germplasm',
                'active' : '' },
            {   'name' : 'Treatment Factors',
                'link' : 'treatment',
                'active' : '' },
            {   'name' : 'Experimental Design',
                'link' : 'experimentalDesign',
                'active' : '' },
            {   'name' : 'Measurements',
                'link' : 'measurements',
                'active' : '' }
        ];

        $scope.activeTabIndex = 0;

        $scope.switchTab = function(index) {
            $scope.trialTabs[$scope.activeTabIndex].active = '';
            $scope.trialTabs[index].active = 'active';
            $scope.activeTabIndex = index;
        };

        $scope.$on('$locationChangeStart', function(event, next, current) {
            var nextPage = next.split('#/')[1];

            if (nextPage !== undefined) {
                for (var i = 0; i < $scope.trialTabs.length; i++) {
                    if ($scope.trialTabs[i].link === nextPage) {

                        $scope.switchTab(i);

                        break;
                    }
                }
            }
        });

    }]);

    manageTrialApp.service('TrialManagerDataService', ['TRIAL_SETTINGS_INITIAL_DATA', 'ENVIRONMENTS_INITIAL_DATA',
        'GERMPLASM_INITIAL_DATA', 'EXPERIMENTAL_DESIGN_INITIAL_DATA', 'MEASUREMENTS_INITIAL_DATA', 'TREATMENT_FACTORS_INITIAL_DATA',
        function (TRIAL_SETTINGS_INITIAL_DATA, ENVIRONMENTS_INITIAL_DATA,GERMPLASM_INITIAL_DATA,EXPERIMENTAL_DESIGN_INITIAL_DATA,
                  MEASUREMENTS_INITIAL_DATA, TREATMENT_FACTORS_INITIAL_DATA) {
        var service = {
            currentData: {
                trialSettings: TRIAL_SETTINGS_INITIAL_DATA,
                environments: ENVIRONMENTS_INITIAL_DATA,
                germplasm: GERMPLASM_INITIAL_DATA,
                treatmentFactors: TREATMENT_FACTORS_INITIAL_DATA,
                experimentalDesign: ENVIRONMENTS_INITIAL_DATA,
                measurements: MEASUREMENTS_INITIAL_DATA
            }
        };

        return service;
    }]);
})();

