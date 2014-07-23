/*globals angular,displayStudyGermplasmSection,isStudyNameUnique,showSuccessfulMessage,
 showInvalidInputMessage, nurseryFieldsIsRequired,saveSuccessMessage,validateStartEndDateBasic, showAlertMessage*/

(function () {
    'use strict';

    angular.module('manageTrialApp').service('TrialManagerDataService', ['TRIAL_SETTINGS_INITIAL_DATA', 'ENVIRONMENTS_INITIAL_DATA',
        'GERMPLASM_INITIAL_DATA', 'EXPERIMENTAL_DESIGN_INITIAL_DATA', 'MEASUREMENTS_INITIAL_DATA', 'TREATMENT_FACTORS_INITIAL_DATA',
        'BASIC_DETAILS_DATA', '$http', '$resource', 'TRIAL_HAS_MEASUREMENT', 'TRIAL_MEASUREMENT_COUNT', 'TRIAL_MANAGEMENT_MODE', '$q',
        function (TRIAL_SETTINGS_INITIAL_DATA, ENVIRONMENTS_INITIAL_DATA, GERMPLASM_INITIAL_DATA, EXPERIMENTAL_DESIGN_INITIAL_DATA, MEASUREMENTS_INITIAL_DATA, TREATMENT_FACTORS_INITIAL_DATA, BASIC_DETAILS_DATA, $http, $resource, TRIAL_HAS_MEASUREMENT, TRIAL_MEASUREMENT_COUNT, TRIAL_MANAGEMENT_MODE, $q) {

            // TODO : clean up data service, at the very least arrange the functions in alphabetical order
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
            var saveEventListeners = {};

            var propagateChange = function (targetRegistry, dataKey, newValue) {
                if (targetRegistry[dataKey]) {
                    angular.forEach(targetRegistry[dataKey], function (updateFunction) {
                        updateFunction(newValue);
                    });
                }
            };

            var updateTrialDataAfterCreation = function (trialID, updateFunction) {
                $http.get('/Fieldbook/TrialManager/openTrial/updateSavedTrial?trialID=' + trialID).success(function (data) {
                    if (updateFunction) {
                        updateFunction(data);
                    } else {
                        // update necessary data and settings
                        // currently identified is the stockid, locationid, and experimentid found in the environment tab
                        service.updateCurrentData('environments', extractData(data.environmentData));
                        service.updateSettings('environments', extractSettings(data.environmentData));

                        service.currentData.basicDetails.studyID = trialID;
                        service.trialMeasurement.hasMeasurement = data.measurementDataExisting;
                        service.trialMeasurement.count = data.measurementRowCount;

                        // TODO : change from global function call
                        displayStudyGermplasmSection(service.trialMeasurement.hasMeasurement,
                            service.trialMeasurement.count);
                    }

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
                $http.post('/Fieldbook/TrialManager/GermplasmList/next', serializedData).success(function (data) {
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
                        showSuccessfulMessage('', saveSuccessMessage);
                    }
                });
            };

            var loadMeasurementScreen = function () {
                if ($('.germplasm-list-data-table tr.primaryRow').length !== 0) {
                    $.ajax({
                        url: '/Fieldbook/TrialManager/openTrial/load/measurement',
                        type: 'GET',
                        data: '',
                        cache: false,
                        success: function (html) {
                            $('#measurementsDiv').html(html);
                        }
                    });
                }
            };

            var notifySaveEventListeners = function () {
                angular.forEach(saveEventListeners, function (saveListenerFunction) {
                    saveListenerFunction();
                });
            };

            var performDataCleanup = function () {
                // TODO : delegate the task of cleaning up data to each tab that produces it, probably via listener

                // perform cleanup of data for trial settings
                // right now, just make sure that no objects are sent as user input for user-defined settings
                cleanupData(service.currentData.trialSettings.userInput);
                angular.forEach(service.currentData.environments.environments, function (environment) {
                    cleanupData(environment.managementDetailValues);
                    cleanupData(environment.trialDetailValues);
                });
            };

            var cleanupData = function (values) {
                if (values) {
                    angular.forEach(values, function (value, key) {
                        if (value && value.id) {
                            values[key] = value.id;
                        }
                    });
                }
            };

            var VariablePairService = $resource('/Fieldbook/TrialManager/createTrial/retrieveVariablePairs/:id',
                {id: '@id'}, { 'get': {method: 'get', isArray: true} });
            var GenerateExpDesignService = $resource('/Fieldbook/TrialManager/experimental/design/generate', {}, { });

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
                applicationData: {
                    unappliedChangesAvailable: false
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

                        })(),
                        germplasmTotalListCount: 0,

                        data: {
                            'noOfEnvironments': 0,
                            'designType': 0,
                            'replicationsCount': 0,
                            'isResolvable': true,
                            'blockSize': 0,
                            'useLatenized': true,
                            'contiguousBlocksToLatenize': 0,
                            'replicationsPerCol': 0,
                            'rowsPerReplications': 0,
                            'colsPerReplications': 0,
                            'contiguousRowsToLatenize': 0,
                            'contiguousColToLatenize': 0
                        }
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
                    hasMeasurement: TRIAL_HAS_MEASUREMENT,
                    count: parseInt(TRIAL_MEASUREMENT_COUNT, 10)
                },

                isOpenTrial: function () {
                    return service.currentData.basicDetails.studyID !== null &&
                        service.currentData.basicDetails.studyID !== 0;
                },

                indicateUnappliedChangesAvailable: function () {
                    if (!service.applicationData.unappliedChangesAvailable) {
                        service.applicationData.unappliedChangesAvailable = true;
                        showAlertMessage('', 'These changes have not yet been applied to the Measurements table. To update the Measurements table, ' +
                            'please review your settings and regenerate the Experimental Design on the next tab', 10000);
                    }
                },

                extractData: extractData,
                extractSettings: extractSettings,
                saveCurrentData: function () {
                    if (service.applicationData.unappliedChangesAvailable) {
                        showAlertMessage('', 'Changes have been made that may affect the experimental design of this trial. Please ' +
                            'regenerate the design on the Experimental Design tab', 10000);
                    } else if (service.isCurrentTrialDataValid(service.isOpenTrial())) {
                        performDataCleanup();
                        if (!service.isOpenTrial()) {
                            $http.post('/Fieldbook/TrialManager/createTrial', service.currentData).
                                success(function () {
                                    submitGermplasmList().then(function (generatedID) {
                                        showSuccessfulMessage('', saveSuccessMessage);
                                        notifySaveEventListeners();
                                        window.location = '/Fieldbook/TrialManager/openTrial/' + generatedID;

                                        displayStudyGermplasmSection(service.trialMeasurement.hasMeasurement,
                                            service.trialMeasurement.count);
                                    });
                                });
                        } else {
                            if (service.trialMeasurement.count > 0) {
                                if ($('.import-study-data').data('data-import') === '1') {
                                    doSaveImportedData();

                                    notifySaveEventListeners();
                                    updateTrialDataAfterCreation(service.currentData.basicDetails.studyID, function (data) {
                                        service.trialMeasurement.hasMeasurement = (data.measurementDataExisting);
                                        service.trialMeasurement.count = data.measurementRowCount;
                                        service.updateSettings('measurements', extractSettings(data.measurementsData));
                                        displayStudyGermplasmSection(service.trialMeasurement.hasMeasurement,
                                            service.trialMeasurement.count);

                                    });
                                    //need to refresh trait screen
                                } else {
                                    $http.post('/Fieldbook/TrialManager/openTrial', service.currentData).success(function (data) {
                                        recreateSessionVariablesTrial();
                                        notifySaveEventListeners();
                                        service.trialMeasurement.hasMeasurement = (data.measurementDataExisting);
                                        service.trialMeasurement.count = data.measurementRowCount;
                                        displayStudyGermplasmSection(service.trialMeasurement.hasMeasurement,
                                            service.trialMeasurement.count);
                                    });
                                }
                            } else {
                                $http.post('/Fieldbook/TrialManager/openTrial', service.currentData).
                                    success(function () {
                                        submitGermplasmList().then(function (trialID) {
                                            loadMeasurementScreen();
                                            showSuccessfulMessage('', saveSuccessMessage);
                                            notifySaveEventListeners();
                                            updateTrialDataAfterCreation(trialID, function (data) {
                                                service.trialMeasurement.hasMeasurement = (data.measurementDataExisting == 'true');
                                                service.trialMeasurement.count = data.measurementRowCount;
                                                displayStudyGermplasmSection(service.trialMeasurement.hasMeasurement,
                                                    service.trialMeasurement.count);
                                            });
                                            //we also hide the update button
                                        });
                                    });
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

                registerSaveListener: function (name, saveListenerFunction) {
                    saveEventListeners[name] = saveListenerFunction;
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
        }
    ]);
})();