/**
 * Created by cyrus on 7/1/14.
 */

/*global angular, changeBuildOption, getJquerySafeId*/

(function() {
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
    manageTrialApp.controller('manageTrialCtrl',['$scope','$rootScope','TrialManagerDataService', function($scope,$rootScope, TrialManagerDataService){
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

            {   'name' : 'Measurements',
                'state' : 'measurements'
            }
        ];

        $scope.data = TrialManagerDataService.currentData.basicDetails;

        $scope.saveCurrentTrialData = TrialManagerDataService.saveCurrentData;
    }]);

    manageTrialApp.service('TrialManagerDataService', ['TRIAL_SETTINGS_INITIAL_DATA', 'ENVIRONMENTS_INITIAL_DATA',
        'GERMPLASM_INITIAL_DATA', 'EXPERIMENTAL_DESIGN_INITIAL_DATA', 'MEASUREMENTS_INITIAL_DATA', 'TREATMENT_FACTORS_INITIAL_DATA',
        'BASIC_DETAILS_DATA', '$http',
        function (TRIAL_SETTINGS_INITIAL_DATA, ENVIRONMENTS_INITIAL_DATA, GERMPLASM_INITIAL_DATA, EXPERIMENTAL_DESIGN_INITIAL_DATA,
                  MEASUREMENTS_INITIAL_DATA, TREATMENT_FACTORS_INITIAL_DATA, BASIC_DETAILS_DATA, $http) {

            var extractData = function (initialData) {
                if (!initialData) {
                    return null;
                } else {
                    return initialData.data;
                }
            };

            var extractSettings = function (initialData) {

                if (initialData) {
                    if (initialData.settings.length > 0) {
                        var data = new angular.OrderedHash();
                        data.addList(initialData.settings,function(item) {
                            return item.variable.cvTermId;
                        });

                        return data;
                    } else {
                        var dataMap = {};

                        if (Object.keys(initialData).length > 0 ) {

                            angular.each(initialData.settingMap,function(val,key) {
                                dataMap[key] = new angular.OrderedHash();
                                dataMap[key].addList(val,function(item) {
                                    return item.variable.cvTermId;
                                });

                            });
                        }

                    }

                }

                return new angular.OrderedHash();
            };

            var service = {
                // user input data and default values of standard variables
                currentData: {
                    trialSettings: extractData(TRIAL_SETTINGS_INITIAL_DATA),
                    environments: extractData(ENVIRONMENTS_INITIAL_DATA),
                    germplasm: extractData(GERMPLASM_INITIAL_DATA),
                    treatmentFactors: extractData(TREATMENT_FACTORS_INITIAL_DATA),
                    experimentalDesign: extractData(ENVIRONMENTS_INITIAL_DATA),
                    measurements: extractData(MEASUREMENTS_INITIAL_DATA),
                    basicDetails: extractData(BASIC_DETAILS_DATA)
                },
                // standard variable [meta-data] information or a particular tab settings information
                // what I get is an instance of OrderedHash containing an array of keys with the map
                settings: {
                    trialSettings: extractSettings(TRIAL_SETTINGS_INITIAL_DATA),
                    environments: extractSettings(ENVIRONMENTS_INITIAL_DATA),
                    germplasm: extractSettings(GERMPLASM_INITIAL_DATA),
                    treatmentFactors: extractSettings(TREATMENT_FACTORS_INITIAL_DATA),
                    experimentalDesign: extractSettings(ENVIRONMENTS_INITIAL_DATA),
                    measurements: extractSettings(MEASUREMENTS_INITIAL_DATA),
                    basicDetails: extractSettings(BASIC_DETAILS_DATA)
                },

                saveCurrentData: function () {
                    $http.post('/Fieldbook/TrialManager/createTrial', service.currentData);
                }
            };

            return service;
        }]);

    // README IMPORTANT: Code unmanaged by angular should go here
    document.onInitManageTrial = function() {
        $('#studyBuildOption').on('change', changeBuildOption);
        $('#choosePreviousStudy').hide();

        setTimeout(function() {
            $('#' + getJquerySafeId('basicDetails.value2')).datepicker('setDate', new Date());
        }, 600);
    };

})();

