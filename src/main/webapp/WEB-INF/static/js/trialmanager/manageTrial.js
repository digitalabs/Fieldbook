/**
 * Created by cyrus on 7/1/14.
 */

/*global angular*/

(function(){
    'use strict';

    var manageTrialApp = angular.module('manageTrialApp', ['leafnode-utils','fieldbook-utils','ct.ui.router.extras','ui.bootstrap']);

    // routing configuration
    // TODO: if possible, retrieve the template urls from the list of constants
    manageTrialApp.config(function($stateProvider, $urlRouterProvider,$stickyStateProvider) {

        $stickyStateProvider.enableDebug(false);

        $urlRouterProvider.otherwise("/trialSettings");
        $stateProvider

            .state('trialSettings', {
                url: "/trialSettings",
                templateUrl: '/Fieldbook/TrialManager/createTrial/trialSettings',
                controller: 'TrialSettingsCtrl'
            })

            .state('treatment', {
                url: "/treatment",
                templateUrl: '/Fieldbook/TrialManager/createTrial/treatment',
                controller: 'TreatmentCtrl'
            })

            .state('environment', {
                url: "/environment",
                templateUrl: '/Fieldbook/TrialManager/createTrial/environment',
                controller: 'EnvironmentCtrl'
            })

            .state('experimentalDesign', {
                url: "/experimentalDesign",
                templateUrl: '/Fieldbook/TrialManager/createTrial/experimentalDesign'
            })

            .state('measurements', {
                url: "/measurements",
                templateUrl: '/Fieldbook/TrialManager/createTrial/measurements'
            })

            .state('germplasm', {
                url: "/germplasm",
                views: {
                    "germplasm" : {
                        controller: 'GermplasmCtrl',
                        templateUrl: "/Fieldbook/TrialManager/createTrial/germplasm"
                    }
                },
                deepStateRedirect: true, sticky: true
            });
    });

    // common filters
    manageTrialApp.filter('range', function() {
        return function(input, total) {
            total = parseInt(total);
            for (var i=0; i<total; i++) { input.push(i); }

            return input;
        };
    });

    manageTrialApp.run(
        [          '$rootScope', '$state', '$stateParams',
            function ($rootScope,   $state,   $stateParams) {

                // It's very handy to add references to $state and $stateParams to the $rootScope
                // so that you can access them from any scope within your applications.For example,
                // <li ui-sref-active="active }"> will set the <li> // to active whenever
                // 'contacts.list' or one of its decendents is active.
                $rootScope.$state = $state;
                $rootScope.$stateParams = $stateParams;
            }
        ]
    );


    // THE parent controller for the manageTrial (create/edit) page
    manageTrialApp.controller('manageTrialCtrl',['$scope','$rootScope',function($scope,$rootScope){
        $scope.trialTabs = [
            {   'name' : 'Trial Settings',
                'state' : 'trialSettings'
            },
            {   'name' : 'Environments',
                'state' : 'environment'
            },
            {   'name' : 'Germplasm',
                'state' : 'germplasm'
            },
            {   'name' : 'Treatment Factors',
                'state' : 'treatment'
            },
            {   'name' : 'Experimental Design',
                'state' : 'experimentalDesign'
            },
            {   'name' : 'Measurements',
                'state' : 'measurements'
            }
        ];

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

