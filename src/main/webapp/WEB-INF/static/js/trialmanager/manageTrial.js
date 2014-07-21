/**
 * Created by cyrus on 7/1/14.
 */

/*global angular,openStudyTree*/

(function () {
    'use strict';

    var manageTrialApp = angular.module('manageTrialApp', ['leafnode-utils', 'fieldbook-utils',
        'ct.ui.router.extras', 'ui.bootstrap', 'ngLodash', 'ngResource']);

    // routing configuration
    // TODO: if possible, retrieve the template urls from the list of constants
    manageTrialApp.config(function ($stateProvider, $urlRouterProvider, $stickyStateProvider) {

        $stickyStateProvider.enableDebug(false);

        $urlRouterProvider.otherwise('/trialSettings');
        $stateProvider

            .state('trialSettings', {
                url: '/trialSettings',
                templateUrl: '/Fieldbook/TrialManager/createTrial/trialSettings',
                controller: 'TrialSettingsCtrl'
            })

            .state('treatment', {
                url: '/treatment',
                templateUrl: '/Fieldbook/TrialManager/createTrial/treatment',
                controller: 'TreatmentCtrl'
            })

            .state('environment', {
                url: '/environment',
                templateUrl: '/Fieldbook/TrialManager/createTrial/environment',
                controller: 'EnvironmentCtrl'
            })

            .state('experimentalDesign', {
                url: '/experimentalDesign',
                templateUrl: '/Fieldbook/TrialManager/createTrial/experimentalDesign'
            })

            .state('germplasm', {
                url: '/germplasm',
                views: {
                    'germplasm': {
                        controller: 'GermplasmCtrl',
                        templateUrl: '/Fieldbook/TrialManager/createTrial/germplasm'
                    }
                },
                deepStateRedirect: true, sticky: true
            })

            .state('createMeasurements', {
                url: '/createMeasurements',
                templateUrl: '/Fieldbook/TrialManager/createTrial/measurements',
                controller: 'MeasurementsCtrl'
            })

            .state('editMeasurements', {
                url: '/editMeasurements',
                views: {
                    'editMeasurements': {
                        controller: 'MeasurementsCtrl',
                        templateUrl: '/Fieldbook/TrialManager/openTrial/measurements'
                    }
                },
                deepStateRedirect: true, sticky: true
            });

    });

    // common filters
    manageTrialApp.filter('range', function () {
        return function (input, total) {
            total = parseInt(total);
            for (var i = 0; i < total; i++) {
                input.push(i);
            }

            return input;
        };
    });

    manageTrialApp.run(
        [          '$rootScope', '$state', '$stateParams',
            function ($rootScope, $state, $stateParams) {

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
    manageTrialApp.controller('manageTrialCtrl', ['$scope', '$rootScope', 'TrialManagerDataService', '$http',
        function ($scope, $rootScope, TrialManagerDataService, $http) {
            $scope.trialTabs = [
                {   'name': 'Trial Settings',
                    'state': 'trialSettings'
                },
                {   'name': 'Germplasm',
                    'state': 'germplasm'
                },
                {   'name': 'Treatment Factors',
                    'state': 'treatment'
                },
                {   'name': 'Environments',
                    'state': 'environment'
                },
                {   'name': 'Experimental Design',
                    'state': 'experimentalDesign'
                },
                {   'name': 'Measurements',
                    'state': 'createMeasurements'
                },
                {
                    'name': 'Measurements',
                    'state': 'editMeasurements'
                }

            ];

            $scope.isOpenTrial = TrialManagerDataService.isOpenTrial;

            $scope.isChoosePreviousTrial = false;

            $scope.toggleChoosePreviousTrial = function () {
                $scope.isChoosePreviousTrial = !$scope.isChoosePreviousTrial;
            };

            $scope.data = TrialManagerDataService.currentData.basicDetails;

            $scope.saveCurrentTrialData = TrialManagerDataService.saveCurrentData;

            $scope.selectPreviousTrial = function () {
                openStudyTree(3, $scope.useExistingTrial);
            };

            $scope.changeFolderLocation = function () {
                openStudyTree(2, $scope.updateSelectedFolder);
            };

            $scope.updateSelectedFolder = function (folderID) {
                TrialManagerDataService.currentData.basicDetails.folderId = folderID;
            };

            $scope.useExistingTrial = function (existingTrialID) {
                $http.get('/Fieldbook/TrialManager/createTrial/useExistingTrial?trialID=' + existingTrialID).success(function (data) {
                    // update data and settings
                    TrialManagerDataService.updateCurrentData('trialSettings', TrialManagerDataService.extractData(data.trialSettingsData));
                    TrialManagerDataService.updateCurrentData('environments', TrialManagerDataService.extractData(data.environmentData));
                    // TODO : treatment factor here

                    TrialManagerDataService.updateSettings('trialSettings', TrialManagerDataService.extractSettings(data.trialSettingsData));
                    TrialManagerDataService.updateSettings('environments', TrialManagerDataService.extractSettings(data.environmentData));
                    TrialManagerDataService.updateSettings('germplasm', TrialManagerDataService.extractSettings(data.germplasmData));
                    // TODO : treatment factor here
                });
            };

            $scope.displayMeasurementOnlyActions = function () {
                return TrialManagerDataService.trialMeasurement.count &&
                    TrialManagerDataService.trialMeasurement.count > 0;
            };

            $scope.resizeMeasurementsIfNecessary = function(targetState) {
                if (targetState === 'editMeasurements') {
                    if ($('#measurement-table').length !== 0 && $('#measurement-table').dataTable() !== null) {
                        $('#measurement-table').dataTable().fnAdjustColumnSizing();
                    }
                }
            };
        }]);

    manageTrialApp.filter('filterMeasurementState', function () {
        return function (tabs, isOpenTrial) {
            var filtered = angular.copy(tabs);

            for (var i = 0; i < filtered.length; i++) {
                if (filtered[i].state === 'editMeasurements' && isOpenTrial) {
                    filtered.splice(i, 1);

                    break;
                }

                else if (filtered[i].state === 'openMeasurements' && !isOpenTrial) {
                    filtered.splice(i, 1);

                    break;
                }
            }

            return filtered;
        };
    });

// README IMPORTANT: Code unmanaged by angular should go here
    document.onInitManageTrial = function () {
        // do nothing for now
    };

})
();
