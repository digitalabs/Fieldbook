/*globals angular,displayStudyGermplasmSection,isStudyNameUnique,showSuccessfulMessage,
 showInvalidInputMessage, nurseryFieldsIsRequired,saveSuccessMessage,validateStartEndDateBasic, showAlertMessage, doSaveImportedData*/

(function () {
    'use strict';

    angular.module('manageTrialApp').service('TrialManagerDataService', ['TRIAL_SETTINGS_INITIAL_DATA', 'ENVIRONMENTS_INITIAL_DATA',
        'GERMPLASM_INITIAL_DATA', 'EXPERIMENTAL_DESIGN_INITIAL_DATA', 'MEASUREMENTS_INITIAL_DATA', 'TREATMENT_FACTORS_INITIAL_DATA',
        'BASIC_DETAILS_DATA', '$http', '$resource', 'TRIAL_HAS_MEASUREMENT', 'TRIAL_MEASUREMENT_COUNT', 'TRIAL_MANAGEMENT_MODE', '$q','TrialSettingsManager',
        function (TRIAL_SETTINGS_INITIAL_DATA, ENVIRONMENTS_INITIAL_DATA, GERMPLASM_INITIAL_DATA, EXPERIMENTAL_DESIGN_INITIAL_DATA, MEASUREMENTS_INITIAL_DATA, TREATMENT_FACTORS_INITIAL_DATA, BASIC_DETAILS_DATA, $http, $resource, TRIAL_HAS_MEASUREMENT, TRIAL_MEASUREMENT_COUNT, TRIAL_MANAGEMENT_MODE, $q,TrialSettingsManager) {

            // TODO : clean up data service, at the very least arrange the functions in alphabetical order
            var extractData = function (initialData, initializeProperty) {
                if (!initialData) {
                    var data = {};
                    if (initializeProperty) {
                        data[initializeProperty] = {};
                    }

                    return data;
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

            var extractTreatmentFactorSettings = function(initialData) {
                var settingMap = {};
                if (initialData) {
                    if (initialData.settingMap && initialData.settingMap.details) {
                        var data = new angular.OrderedHash();
                        data.addList(initialData.settingMap.details, function (item) {
                            return item.variable.cvTermId;
                        });

                        settingMap.details = data;
                    }

                    if (initialData.settingMap && initialData.settingMap.treatmentLevelPairs) {
                        settingMap.treatmentLevelPairs = initialData.settingMap.treatmentLevelPairs;
                        angular.forEach(settingMap.treatmentLevelPairs, function(value, key) {
                            var data = new angular.OrderedHash();
                            data.addList(value, function (item) {
                                return item.variable.cvTermId;
                            });

                            settingMap.treatmentLevelPairs[key] = data;
                        });
                    }
                } else {
                    settingMap.details = new angular.OrderedHash();
                    settingMap.treatmentLevelPairs = {};
                }

                return settingMap;
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

            var cleanupData = function(values) {
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
                    basicDetails: extractData(BASIC_DETAILS_DATA),
                    treatmentFactors : extractData(TREATMENT_FACTORS_INITIAL_DATA, 'currentData')
                },
                // standard variable [meta-data] information or a particular tab settings information
                // what I get is an instance of OrderedHash containing an array of keys with the map
                settings: {
                    trialSettings: extractSettings(TRIAL_SETTINGS_INITIAL_DATA),
                    environments: extractSettings(ENVIRONMENTS_INITIAL_DATA),
                    germplasm: extractSettings(GERMPLASM_INITIAL_DATA),
                    treatmentFactors: extractTreatmentFactorSettings(TREATMENT_FACTORS_INITIAL_DATA),
                    measurements: extractSettings(MEASUREMENTS_INITIAL_DATA),
                    basicDetails: extractSettings(BASIC_DETAILS_DATA)
                },
                applicationData: {
                    unappliedChangesAvailable: false,
                    unsavedGeneratedDesign : false,
                    unsavedTraitsAvailable : false
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

                        showAdvancedOptions : [false,false,false],

                        data: {
                            'noOfEnvironments': 0,
                            'designType': 0,
                            'replicationsCount' : 0,
                            'isResolvable' : true,
                            'blockSize' : 0,
                            'useLatenized' : false,
                            'nblatin' : 0,
                            'replicationsArrangement' : 0,
                            'rowsPerReplications' : 0,
                            'colsPerReplications' : 0,
                            'nrlatin':0,
                            'nclatin': 0,
                            'replatinGroups': ''
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

                indicateUnappliedChangesAvailable: function (fromGermplasm) {
                    if (!service.applicationData.unappliedChangesAvailable && (fromGermplasm || service.trialMeasurement.count > 0)) {
                        service.applicationData.unappliedChangesAvailable = true;
                        showAlertMessage('', 'These changes have not yet been applied to the Measurements table. To update the Measurements table, ' +
                            'please review your settings and regenerate the Experimental Design on the next tab', 10000);
                        $('body').data('needGenerateExperimentalDesign', '1');
                    }
                },

                clearUnappliedChangesFlag : function() {
                    service.applicationData.unappliedChangesAvailable = false;
                    $('body').data('needGenerateExperimentalDesign', '0');
                },

                extractData: extractData,
                extractSettings: extractSettings,
                extractTreatmentFactorSettings : extractTreatmentFactorSettings,
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
                                        service.applicationData.unsavedGeneratedDesign = false;
                                        service.applicationData.unsavedTraitsAvailable = false;
                                        $('body').data('needToSave', '0');
                                    });
                                });
                        } else {

							if (service.trialMeasurement.count > 0 && $('.import-study-data').data('data-import') === '1') {
                                doSaveImportedData();

                                notifySaveEventListeners();
                                updateTrialDataAfterCreation(service.currentData.basicDetails.studyID, function (data) {
                                    service.trialMeasurement.hasMeasurement = (data.measurementDataExisting);
                                    service.trialMeasurement.count = data.measurementRowCount;
                                    service.updateSettings('measurements', extractSettings(data.measurementsData));
                                    displayStudyGermplasmSection(service.trialMeasurement.hasMeasurement,
                                        service.trialMeasurement.count);
                                    service.applicationData.unsavedGeneratedDesign = false;
                                    service.applicationData.unsavedTraitsAvailable = false;
                                    $('body').data('needToSave', '0');
                                  });
							}
                            else if (service.trialMeasurement.count >  0 && parseInt($('.germplasm-list-items tbody tr').length) === 0) {
                                $http.post('/Fieldbook/TrialManager/openTrial', service.currentData).success(function (data) {
                                    recreateSessionVariablesTrial();
                                    notifySaveEventListeners();
                                    service.trialMeasurement.hasMeasurement = (data.measurementDataExisting);
                                    service.trialMeasurement.count = data.measurementRowCount;
                                    displayStudyGermplasmSection(service.trialMeasurement.hasMeasurement,
                                        service.trialMeasurement.count);
                                    service.applicationData.unsavedGeneratedDesign = false;
                                    service.applicationData.unsavedTraitsAvailable = false;
                                    $('body').data('needToSave', '0');
                                });
                            }
                            else {
                                $http.post('/Fieldbook/TrialManager/openTrial', service.currentData).
                                    success(function () {
                                        submitGermplasmList().then(function (trialID) {
                                            showSuccessfulMessage('', saveSuccessMessage);
                                            notifySaveEventListeners();
                                            window.location = '/Fieldbook/TrialManager/openTrial/' + trialID;

                                            displayStudyGermplasmSection(service.trialMeasurement.hasMeasurement,
                                                                    service.trialMeasurement.count);
                                            service.applicationData.unsavedGeneratedDesign = false;
                                            service.applicationData.unsavedTraitsAvailable = false;
                                            $('body').data('needToSave', '0');
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

                treatmentFactorDataInvalid : function() {
                    var errorCode = 0;

                    angular.forEach(service.currentData.treatmentFactors.currentData, function(value, key) {
                        // check if a factor selected as a pair is also used as
                        if (!value.variableId || value.variableId === 0) {
                            errorCode = 2;
                            return false;
                        }

                        if (service.currentData.treatmentFactors.currentData[value.variableId]) {
                            errorCode = 1;
                            return false;
                        }
                    });

                    return errorCode;
                },

                getSettingsArray: function () {
                    if (settingsArray.length === 0) {
                        angular.forEach(service.settings, function (value, key) {
                            if (key === 'environments') {
                                settingsArray.push(value.managementDetails);
                                settingsArray.push(value.trialConditionDetails);
                            } else if (key === 'experimentalDesign') {
                                return true;
                            } else if (key === 'treatmentFactors') {
                                settingsArray.push(value.details);
                            } else {
                                if (value) {
                                    settingsArray.push(value);
                                }

                            }
                        });
                    }

                    return settingsArray;
                },

                isCurrentTrialDataValid: function (isEdit) {
                    if (isEdit === undefined) {
                        isEdit = false;
                    }

                    var hasError = false, name = '', customMessage = '', errorCode = 0;

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

                    if (!hasError) {
                        errorCode = service.treatmentFactorDataInvalid();
                        if (errorCode === 1) {
                            hasError = true;
                            customMessage = invalidTreatmentFactorPair;
                        } else if (errorCode === 2) {
                            hasError = true;
                            customMessage = unpairedTreatmentFactor;
                        }

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

                    var valid = validateStartEndDateBasic(service.currentData.basicDetails.basicDetails[8050],service.currentData.basicDetails.basicDetails[8060]);

                    if (valid !== true) {
                        showInvalidInputMessage(valid);
                        return false;
                    }

                    return valid;

                }
            };

            // 5 is the group no of treatment factors
            TrialSettingsManager.addDynamicFilterObj(service.currentData.treatmentFactors,5);


            return service;
        }
    ])

    .service('TrialSettingsManager', ['TRIAL_VARIABLE_SELECTION_LABELS', function(TRIAL_VARIABLE_SELECTION_LABELS) {
        var TrialSettingsManager = window.TrialSettingsManager;
        var settingsManager = new TrialSettingsManager(TRIAL_VARIABLE_SELECTION_LABELS);



        var service = {
            openVariableSelectionDialog : function(params) {
                settingsManager._openVariableSelectionDialog(params);
            },

            // @param = this map contains variables of the pair that will be filtered
            addDynamicFilterObj : function(_map,group) {
                settingsManager._addDynamicFilter(_map,group);
            }


        };

        return service;
    }]);
})();
