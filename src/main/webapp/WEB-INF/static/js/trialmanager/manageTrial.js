/**
 * Created by cyrus on 7/1/14.
 */

/*global angular, changeBuildOption, isStudyNameUnique, showSuccessfulMessage,
 showInvalidInputMessage, nurseryFieldsIsRequired, validateStartEndDateBasic, openStudyTree,alert, displayStudyGermplasmSection*/

(function () {
    'use strict';

    var manageTrialApp = angular.module('manageTrialApp', ['leafnode-utils', 'fieldbook-utils', 'ct.ui.router.extras', 'ui.bootstrap', 'ngLodash', 'ngResource']);

    // routing configuration
    // TODO: if possible, retrieve the template urls from the list of constants
    manageTrialApp.config(function ($stateProvider, $urlRouterProvider, $stickyStateProvider, TRIAL_MANAGEMENT_MODE) {

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
                        controller: 'GermplasmCtrl',
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
                    'state':'createMeasurements'
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

            $scope.displayMeasurementOnlyActions = TrialManagerDataService.trialMeasurement.count &&
                TrialManagerDataService.trialMeasurement.count > 0;
        }]);

    manageTrialApp.service('TrialManagerDataService', ['TRIAL_SETTINGS_INITIAL_DATA', 'ENVIRONMENTS_INITIAL_DATA',
        'GERMPLASM_INITIAL_DATA', 'EXPERIMENTAL_DESIGN_INITIAL_DATA', 'MEASUREMENTS_INITIAL_DATA', 'TREATMENT_FACTORS_INITIAL_DATA',
        'BASIC_DETAILS_DATA', '$http', '$resource', 'TRIAL_HAS_MEASUREMENT', 'TRIAL_MEASUREMENT_COUNT', 'TRIAL_MANAGEMENT_MODE', '$q', '$location',
        function (TRIAL_SETTINGS_INITIAL_DATA, ENVIRONMENTS_INITIAL_DATA, GERMPLASM_INITIAL_DATA,
                  EXPERIMENTAL_DESIGN_INITIAL_DATA, MEASUREMENTS_INITIAL_DATA, TREATMENT_FACTORS_INITIAL_DATA,
                  BASIC_DETAILS_DATA, $http, $resource, TRIAL_HAS_MEASUREMENT, TRIAL_MEASUREMENT_COUNT, TRIAL_MANAGEMENT_MODE, $q,$location) {

            var extractData = function (initialData) {
                if (!initialData) {
                    return null;
                } else {
                    return initialData.data;
                }
            };

            var dataRegistry = {};
            var settingRegistry = {};
            var settingsArray = [];

            var propagateChange = function (targetRegistry, dataKey, newValue) {
                if (targetRegistry[dataKey]) {
                    angular.forEach(targetRegistry[dataKey], function (updateFunction) {
                        updateFunction(newValue);
                    });
                }
            };

            var updateTrialDataAfterCreation = function (trialID) {
                $http.get('/Fieldbook/TrialManager/openTrial/updateSavedTrial?trialID=' + trialID).success(function (data) {
                    // update necessary data and settings
                    // currently identified is the stockid, locationid, and experimentid found in the environment tab
                    service.updateCurrentData('environments', extractData(data.environmentData));
                    service.updateSettings('environments', extractSettings(data.environmentData));

                    service.currentData.basicDetails.studyID = trialID;
                    service.trialMeasurement.hasMeasurement = data.measurementDataExisting;
                    service.trialMeasurement.count = data.measurementRowCount;

                    displayStudyGermplasmSection(service.trialMeasurement.hasMeasurement,
                        service.trialMeasurement.count);
                });
            };

            var extractSettings = function (initialData) {

                if (initialData) {
                    if (!initialData.settingMap) {
                        var data = new angular.OrderedHash();
                        data.addList(initialData.settings, function (item) {
                            return item.variable.cvTermId;
                        });

                        return data;
                    } else {
                        var dataMap = {};

                        $.each(initialData.settingMap, function (key, value) {
                            dataMap[key] = new angular.OrderedHash();
                            dataMap[key].addList(value, function (item) {
                                return item.variable.cvTermId;
                            });
                        });

                        return dataMap;
                    }

                }

                return new angular.OrderedHash();
            };

            // TODO : change function such that it does not require jQuery style element / id based access for value retrieval
            var submitGermplasmList = function () {
                var $form = $('#germplasm-list-form');
                $('#startIndex').val($('#startIndex2').val());
                $('#interval').val($('#interval2').val());
                $('#mannerOfInsertion').val($('#mannerOfInsertion2').val());

                var serializedData = $form.serialize();
                var d = $q.defer();
                $http.post('/Fieldbook/NurseryManager/importGermplasmList/next', serializedData).success(function (data) {
                    d.resolve(data);
                });
                return d.promise;
            };
            
            var recreateSessionVariablesTrial = function () {
                $.ajax({
                    url: '/Fieldbook/TrialManager/openTrial/recreate/session/variables',
                    type: 'GET',
                    data: '',
                    cache: false,
                    success: function (html) {
                        $('#measurementsDiv').html(html);
                        showSuccessfulMessage('', 'Success');
                    }
                });
            };

            var VariablePairService = $resource("/Fieldbook/TrialManager/createTrial/retrieveVariablePairs/:id", {id: '@id'}, { 'get': {method: 'get', isArray: true} });
            var GenerateExpDesignService = $resource("/Fieldbook/TrialManager/experimental/design/generate", {}, { });

            var service = {
                // user input data and default values of standard variables
                currentData: {
                    trialSettings: extractData(TRIAL_SETTINGS_INITIAL_DATA),
                    environments: extractData(ENVIRONMENTS_INITIAL_DATA),
                    basicDetails: extractData(BASIC_DETAILS_DATA)
                },
                // standard variable [meta-data] information or a particular tab settings information
                // what I get is an instance of OrderedHash containing an array of keys with the map
                settings: {
                    trialSettings: extractSettings(TRIAL_SETTINGS_INITIAL_DATA),
                    environments: extractSettings(ENVIRONMENTS_INITIAL_DATA),
                    germplasm: extractSettings(GERMPLASM_INITIAL_DATA),
                    treatmentFactors: extractSettings(TREATMENT_FACTORS_INITIAL_DATA),
                    measurements: extractSettings(MEASUREMENTS_INITIAL_DATA),
                    basicDetails: extractSettings(BASIC_DETAILS_DATA)
                },

                // settings that has special data structure
                specialSettings: {
                    experimentalDesign: {
                        factors: (function () {
                            var hardFactors = new angular.OrderedHash();
                            if (EXPERIMENTAL_DESIGN_INITIAL_DATA && EXPERIMENTAL_DESIGN_INITIAL_DATA.data) {
                                hardFactors.addList(EXPERIMENTAL_DESIGN_INITIAL_DATA.data.expDesignDetailList, function (item) {
                                    return item.variable.cvTermId;
                                });
                            }


                            return hardFactors;

                        })()
                    },
                    treatmentLevelPairs: {}
                },

                // returns a promise object to be resolved later
                retrieveVariablePairs: function (cvTermId) {
                    return VariablePairService.get({id: cvTermId}).$promise;
                },

                // the data param structures
                generateExpDesign: function (data) {
                    return GenerateExpDesignService.save(data).$promise;
                },

                trialMeasurement: {
                    hasMeasurement: TRIAL_HAS_MEASUREMENT == 'true',
                    count: parseInt(TRIAL_MEASUREMENT_COUNT, 10)
                },

                isOpenTrial: function () {
                    return service.currentData.basicDetails.studyID !== null &&
                        service.currentData.basicDetails.studyID !== 0;
                },

                extractData: extractData,
                extractSettings: extractSettings,
                saveCurrentData: function () {
                    if (service.isCurrentTrialDataValid(service.isOpenTrial())) {
                        // TODO : double check
                        if (!service.isOpenTrial()) {
                            $http.post('/Fieldbook/TrialManager/createTrial', service.currentData).
                                success(function () {
                                    submitGermplasmList().then(function (generatedID) {
                                        showSuccessfulMessage('', 'Success');
                                        /*updateTrialDataAfterCreation(generatedID);*/
                                        window.location = '/Fieldbook/TrialManager/openTrial/' + generatedID;
                                    });
                                });
                        } else {
                            if (service.trialMeasurement.count > 0) {
                                    $http.post('/Fieldbook/TrialManager/openTrial', service.currentData).success(recreateSessionVariablesTrial);
                                } else {
                                    $http.post('/Fieldbook/TrialManager/openTrial', service.currentData).success(submitGermplasmList);
                                }

                            }

                    }
                },
                registerData: function (dataKey, updateFunction) {
                    if (!dataRegistry[dataKey]) {
                        dataRegistry[dataKey] = [];
                        dataRegistry[dataKey].push(updateFunction);
                    } else if (dataRegistry[dataKey].indexOf(updateFunction) === -1) {
                        dataRegistry[dataKey].push(updateFunction);
                    }
                },

                updateCurrentData: function (dataKey, newValue) {
                    service.currentData[dataKey] = newValue;
                    propagateChange(dataRegistry, dataKey, newValue);
                },

                updateSettings: function (key, newValue) {
                    service.settings[key] = newValue;
                    propagateChange(settingRegistry, key, newValue);
                    settingsArray = [];
                },

                registerSetting: function (key, updateFunction) {
                    if (!settingRegistry[key]) {
                        settingRegistry[key] = [];
                        settingRegistry[key].push(updateFunction);
                    } else if (settingRegistry[key].indexOf(updateFunction) === -1) {
                        settingRegistry[key].push(updateFunction);
                    }
                },

                getSettingsArray: function () {
                    if (settingsArray.length === 0) {
                        angular.forEach(service.settings, function (value, key) {
                            if (key !== 'environments') {
                                if (value) {
                                    settingsArray.push(value);
                                }
                            } else if (key === 'experimentalDesign') {
                                return true;
                            } else {
                                settingsArray.push(value.managementDetails);
                                settingsArray.push(value.trialConditionDetails);

                            }
                        });
                    }

                    return settingsArray;
                },

                isCurrentTrialDataValid: function (isEdit) {
                    if (isEdit === undefined) {
                        isEdit = false;
                    }

                    var hasError = false, name = '', customMessage = '';

                    if ($.trim(service.currentData.basicDetails.basicDetails[8005]) === '') {
                        hasError = true;
                        name = 'Name';
                    } else if ($.trim(service.currentData.basicDetails.basicDetails[8007]) === '') {
                        hasError = true;
                        name = 'Description';
                    } else if (!isEdit && isStudyNameUnique(service.currentData.basicDetails.basicDetails[8005]) === false) {
                        hasError = true;
                        customMessage = 'Name should be unique';
                    } else if (!service.currentData.basicDetails.folderId || service.currentData.basicDetails.folderId === '') {
                        hasError = true;
                        name = $('#folderLabel').text();
                    } else if (service.currentData.basicDetails.basicDetails[8050] === '') {
                        // validate creation date
                        hasError = true;
                        name = 'Creation Date';
                    } else if (service.currentData.environments.noOfEnvironments <= 0) {
                        hasError = true;
                        customMessage = 'Trials should have at least one environment';
                    }

                    if (hasError) {
                        var errMsg = '';
                        if (name !== '') {
                            errMsg = name.replace('*', '').replace(':', '') + ' ' + nurseryFieldsIsRequired;
                        }

                        if (customMessage !== '') {
                            errMsg = customMessage;
                        }

                        showInvalidInputMessage(errMsg);
                        return false;
                    }

                    var valid = validateStartEndDateBasic();

                    return valid;

                }
            };

            return service;
        }])

        .filter('filterMeasurementState',function() {
            return function (tabs,isOpenTrial) {
                var filtered = angular.copy(tabs);

                for (var i = 0; i < filtered.length; i++) {
                    if (filtered[i].state === 'editMeasurements' && isOpenTrial) {
                        filtered.splice(i,1);

                        break;
                    }

                    else if (filtered[i].state === 'openMeasurements' && !isOpenTrial) {
                        filtered.splice(i,1);

                        break;
                    }
                }

                return filtered;
            };
        });

    // README IMPORTANT: Code unmanaged by angular should go here
    document.onInitManageTrial = function () {

    };

})();
