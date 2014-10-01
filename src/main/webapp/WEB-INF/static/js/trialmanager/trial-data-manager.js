/*globals angular,displayStudyGermplasmSection,isStudyNameUnique,showSuccessfulMessage,
 showInvalidInputMessage, nurseryFieldsIsRequired,saveSuccessMessage,validateStartEndDateBasic, showAlertMessage, doSaveImportedData,
 invalidTreatmentFactorPair,unpairedTreatmentFactor,createErrorNotification,openStudyTree*/
(function () {
    'use strict';

    angular.module('manageTrialApp').service('TrialManagerDataService', ['GERMPLASM_LIST_SIZE','TRIAL_SETTINGS_INITIAL_DATA',
        'ENVIRONMENTS_INITIAL_DATA', 'GERMPLASM_INITIAL_DATA', 'EXPERIMENTAL_DESIGN_INITIAL_DATA',
        'EXPERIMENTAL_DESIGN_SPECIAL_DATA', 'MEASUREMENTS_INITIAL_DATA', 'TREATMENT_FACTORS_INITIAL_DATA',
        'BASIC_DETAILS_DATA', '$http', '$resource', 'TRIAL_HAS_MEASUREMENT', 'TRIAL_MEASUREMENT_COUNT', 'TRIAL_MANAGEMENT_MODE', '$q',
        'TrialSettingsManager','_','$localStorage',
        function (GERMPLASM_LIST_SIZE,TRIAL_SETTINGS_INITIAL_DATA, ENVIRONMENTS_INITIAL_DATA, GERMPLASM_INITIAL_DATA,
                  EXPERIMENTAL_DESIGN_INITIAL_DATA, EXPERIMENTAL_DESIGN_SPECIAL_DATA,MEASUREMENTS_INITIAL_DATA,
                  TREATMENT_FACTORS_INITIAL_DATA, BASIC_DETAILS_DATA, $http, $resource,
                  TRIAL_HAS_MEASUREMENT, TRIAL_MEASUREMENT_COUNT, TRIAL_MANAGEMENT_MODE, $q,TrialSettingsManager,_,$localStorage) {

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

            var extractBasicDetailsData = function(initialData, initializeProperty) {
                var data = extractData(initialData, initializeProperty);

                // data saved from the database is htmlEscaped so we need to un-escape the retrieved data first.
                _.each(data.basicDetails,function(val,key) {
                    data.basicDetails[key] = _.unescape(val);
                });

                if (data.basicDetails[8050] === null || data.basicDetails[8050] === '') {
                    data.basicDetails[8050] = $.datepicker.formatDate('yy-mm-dd', new Date());
                }

                return data;
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
                        service.updateTrialMeasurementRowCount(data.measurementRowCount);

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
                            item.extracted = true;
                            return item.variable.cvTermId;
                        });

                        return data;
                    } else {
                        var dataMap = {};

                        $.each(initialData.settingMap, function (key, value) {
                            dataMap[key] = new angular.OrderedHash();
                            dataMap[key].addList(value, function (item) {
                                item.extracted = true;
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
                            item.extracted = true;
                            return item.variable.cvTermId;
                        });

                        settingMap.details = data;
                    }

                    if (initialData.settingMap && initialData.settingMap.treatmentLevelPairs) {
                        settingMap.treatmentLevelPairs = initialData.settingMap.treatmentLevelPairs;
                        angular.forEach(settingMap.treatmentLevelPairs, function(value, key) {
                            var data = new angular.OrderedHash();
                            data.addList(value, function (item) {
                                item.extracted = true;
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

            var notifySaveEventListeners = function () {
                $('body').data('expDesignShowPreview', '0');
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
                    basicDetails: extractBasicDetailsData(BASIC_DETAILS_DATA),
                    treatmentFactors : extractData(TREATMENT_FACTORS_INITIAL_DATA, 'currentData'),
                    experimentalDesign : extractData(EXPERIMENTAL_DESIGN_INITIAL_DATA)
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
                    unsavedTraitsAvailable : false,
                    germplasmListCleared: false,
                    germplasmListSelected : GERMPLASM_LIST_SIZE > 0
                },

                // settings that has special data structure
                specialSettings: {
                    experimentalDesign: {
                        factors: (function () {
                            var hardFactors = new angular.OrderedHash();
                            if (EXPERIMENTAL_DESIGN_SPECIAL_DATA && EXPERIMENTAL_DESIGN_SPECIAL_DATA.data) {
                                hardFactors.addList(EXPERIMENTAL_DESIGN_SPECIAL_DATA.data.expDesignDetailList, function (item) {
                                    return item.variable.cvTermId;
                                });
                            }


                            return hardFactors;

                        })(),
                        germplasmTotalListCount: GERMPLASM_LIST_SIZE,

                        showAdvancedOptions : [false,false,false]
                    },
                    treatmentLevelPairs: {},
                    auxSettings : {}
                },

                trialMeasurement: {
                    hasMeasurement: TRIAL_HAS_MEASUREMENT,
                    count: parseInt(TRIAL_MEASUREMENT_COUNT, 10)
                },

                // returns a promise object to be resolved later
                retrieveVariablePairs: function (cvTermId) {
                    return VariablePairService.get({id: cvTermId}).$promise;
                },

                updateSelectedFolder: function (folderID) {
                    service.currentData.basicDetails.folderId = folderID;
                },
                // the data param structures
                generateExpDesign: function (data) {
                    return GenerateExpDesignService.save(data).$promise;
                },


                isOpenTrial: function () {
                    return service.currentData.basicDetails.studyID !== null &&
                        service.currentData.basicDetails.studyID !== 0;
                },

                indicateUnappliedChangesAvailable: function () {
                    if (!service.applicationData.unappliedChangesAvailable && service.trialMeasurement.count != 0) {
                        service.applicationData.unappliedChangesAvailable = true;
                        showAlertMessage('', 'These changes have not yet been applied to the Measurements table. ' +
                            'To update the Measurements table, please review your settings and regenerate ' +
                            'the Experimental Design on the next tab', 10000);
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
                                doSaveImportedData().then(function () {
                                    notifySaveEventListeners();
                                    updateTrialDataAfterCreation(service.currentData.basicDetails.studyID, function (data) {
                                        service.trialMeasurement.hasMeasurement = (data.measurementDataExisting);
                                        service.updateTrialMeasurementRowCount(data.measurementRowCount);
                                        service.updateSettings('measurements', extractSettings(data.measurementsData));
                                        displayStudyGermplasmSection(service.trialMeasurement.hasMeasurement,
                                            service.trialMeasurement.count);
                                        service.applicationData.unsavedGeneratedDesign = false;
                                        service.applicationData.unsavedTraitsAvailable = false;
                                        $('body').data('needToSave', '0');
                                    });
                                });


							}
                            else if (service.trialMeasurement.count > 0 &&
                                (($('#chooseGermplasmAndChecks').length !== 0 &&
                                    $('#chooseGermplasmAndChecks').data('replace') !== undefined &&
                                    parseInt($('#chooseGermplasmAndChecks').data('replace')) !== 1) ||
                                    service.applicationData.unsavedGeneratedDesign === false)
                                ) {
                                $http.post('/Fieldbook/TrialManager/openTrial?replace=0', service.currentData).success(function (data) {
                                    recreateSessionVariablesTrial();
                                    notifySaveEventListeners();
                                    service.trialMeasurement.hasMeasurement = (data.measurementDataExisting);
                                    service.updateTrialMeasurementRowCount(data.measurementRowCount);
                                    displayStudyGermplasmSection(service.trialMeasurement.hasMeasurement,
                                        service.trialMeasurement.count);
                                    service.applicationData.unsavedGeneratedDesign = false;
                                    service.applicationData.unsavedTraitsAvailable = false;
                                    $('body').data('needToSave', '0');
                                });
                            }
                            else {
                                $http.post('/Fieldbook/TrialManager/openTrial?replace=1', service.currentData).
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
                onUpdateData: function (dataKey, updateFunction) {
                    if (!dataRegistry[dataKey]) {
                        dataRegistry[dataKey] = [];
                        dataRegistry[dataKey].push(updateFunction);
                    } else if (dataRegistry[dataKey].indexOf(updateFunction) === -1) {
                        dataRegistry[dataKey].push(updateFunction);
                    }
                },

                updateCurrentData: function (dataKey, newValue) {
                    _.each(_.keys(newValue),function(nvkey) {
                        service.currentData[dataKey][nvkey] = newValue[nvkey];
                    });
                    propagateChange(dataRegistry, dataKey, newValue);
                },

                updateSettings: function (key, newValue) {

                    if (service.settings[key] instanceof angular.OrderedHash) {
                        service.settings[key].removeAll();

                        _.each(newValue.vals(),function(val,nvkey) {
                            service.settings[key].push(nvkey,val);
                        });
                    } else {
                        _.each(_.keys(newValue),function(nvkey) {

                            if (newValue[nvkey] instanceof angular.OrderedHash) {

                                service.settings[key][nvkey].removeAll();

                                _.each(newValue[nvkey].vals(),function(val,ohkey) {
                                    service.settings[key][nvkey].push(ohkey,val);
                                });
                            } else {
                                service.settings[key][nvkey] = newValue[nvkey];
                            }

                        });
                    }

                    propagateChange(settingRegistry, key, newValue);
                    settingsArray = [];
                },

                updateTrialMeasurementRowCount : function (newCountValue) {
                    service.trialMeasurement.count = newCountValue;
                    $('body').data('service.trialMeasurement.count', newCountValue);
                },

                onUpdateSettings: function (key, updateFunction) {
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

                    angular.forEach(service.currentData.treatmentFactors.currentData, function(value) {
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

                constructDataStructureFromDetails: function (details) {
                    var returnVal = {};
                    $.each(details.vals(), function (key, value) {
                        returnVal[value.variable.cvTermId] = null;
                    });

                    return returnVal;
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
                    var creationDate = service.currentData.basicDetails.basicDetails[8050];
                    var completionDate = service.currentData.basicDetails.basicDetails[8060];
                    if (!service.currentData.basicDetails.folderId || service.currentData.basicDetails.folderId === '') {
                        hasError = true;
                        name = $('#folderLabel').text();
                        openStudyTree(2, service.updateSelectedFolder, true);
                        return false;
                    } else if ($.trim(service.currentData.basicDetails.basicDetails[8005]) === '') {
                        hasError = true;
                        name = 'Name';
                    } else if ($.trim(service.currentData.basicDetails.basicDetails[8007]) === '') {
                        hasError = true;
                        name = 'Description';
                    } else if (!isEdit && isStudyNameUnique(service.currentData.basicDetails.basicDetails[8005]) === false) {
                        hasError = true;
                        customMessage = 'Name should be unique';
                    } else if (creationDate === '') {
                        // validate creation date
                        hasError = true;
                        name = 'Creation Date';
                    } else if (service.currentData.environments.noOfEnvironments <= 0) {
                        hasError = true;
                        customMessage = 'Trials should have at least one environment';
                    }
                    
                    var invalidDateMsg = validateAllDates();
                    if(invalidDateMsg !== '') {
                    	hasError = true;
                    	customMessage = invalidDateMsg;
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

                    var valid = validateStartEndDateBasic(creationDate,completionDate);

                    if (valid !== true) {
                        showInvalidInputMessage(valid);
                        return false;
                    }

                    // Validate all variables
                    var isValidVariables = service.validateAllVariablesInput();

                    if (isValidVariables && isValidVariables.hasError) {
                        valid = valid && !isValidVariables.hasError;
                        createErrorNotification(isValidVariables.customHeader,isValidVariables.customMessage);
                    }

                    //valid = false;    // remove later



                    return valid;

                },

                validateAllVariablesInput : function() {
                    var results = {
                        hasError : false,
                        customMessage : '',
                        customHeader : 'Invalid Input '
                    };

                    // perform validation on all settings.currentData, (min / max) if any
                    // Validate all Trial Settings
                    _.each(service.settings.trialSettings.vals(),function(item,key){
                        if (!(!item.variable.maxRange && !item.variable.minRange) ) {
                            _.find(service.currentData.trialSettings,function(val) {
                                if (!!val[key]) {
                                    if (item.variable.maxRange < Number(val[key])) {
                                        results.customMessage = 'Invalid maximum range on variable ' + item.variable.name;
                                        results.hasError = true;
                                        return results.hasError;
                                    } else if (item.variable.minRange > Number(val[key])) {
                                        results.customMessage = 'Invalid minimum range on variable ' + item.variable.name;
                                        results.hasError = true;
                                        return results.hasError;
                                    }
                                }
                            });
                        }
                    });

                    if (results.hasError) {
                        return function(tab) { results.customHeader += tab; return results;  }('on Trial Settings');
                    }

                    // validate environments
                    _.each(service.settings.environments.managementDetails.vals(),function(item,key){
                        if (!(!item.variable.maxRange && !item.variable.minRange) ) {
                            // let's validate each environment
                            _.find(service.currentData.environments.environments,function(val,index) {
                                if (!!val.managementDetailValues[key]) {
                                    if (item.variable.maxRange < Number(val.managementDetailValues[key])) {
                                        results.customMessage = 'Invalid maximum range on management detail variable ' +
                                            item.variable.name + ' at environment ' + (Number(index) + Number(1));
                                        results.hasError = true;
                                        return results.hasError;
                                    } else if (item.variable.minRange > Number(val.managementDetailValues[key])) {
                                        results.customMessage = 'Invalid minimum range on management detail variable ' +
                                            item.variable.name + ' at environment ' + (Number(index) + Number(1));
                                        results.hasError = true;
                                        return results.hasError;
                                    }
                                }
                            });
                        }
                    });

                    if (results.hasError) {
                        return function(tab) { results.customHeader += tab; return results;  }('on Environments');
                    }

                    _.each(service.settings.environments.trialConditionDetails.vals(),function(item,key){
                        if (!(!item.variable.maxRange && !item.variable.minRange) ) {
                            // letz validate each environment
                            _.find(service.currentData.environments.environments,function(val,index) {
                                if (!!val.trialDetailValues[key]) {
                                    if (item.variable.maxRange < Number(val.trialDetailValues[key])) {
                                        results.customMessage = 'Invalid maximum range on trial details variable ' +
                                            item.variable.name + ' at environment ' + (Number(index) + Number(1));
                                        results.hasError = true;
                                        return results.hasError;
                                    } else if (item.variable.minRange > Number(val.trialDetailValues[key])) {
                                        results.customMessage = 'Invalid minimum range on trial details variable ' +
                                            item.variable.name + ' at environment ' + (Number(index) + Number(1));
                                        results.hasError = true;
                                        return results.hasError;
                                    }
                                }
                            });
                        }
                    });


                    if (results.hasError) {
                        return function(tab) { results.customHeader += tab; return results;  }('on Environments');
                    }

                    //  validate all treatments variable inputs
                    return service.validateAllTreatmentFactorLabels(results);
                },

                validateAllTreatmentFactorLabels : function(results) {
                    //  validate all treatments variable inputs
                    _.find(service.currentData.treatmentFactors.currentData, function (item, key) {
                        var settings_var = service.settings.treatmentFactors.treatmentLevelPairs[key].
                            val(service.currentData.treatmentFactors.currentData[key].variableId).variable;
                        if (!(!settings_var.maxRange && !settings_var.minRange)) {
                            _.find(item.labels, function (val, index) {

                                if (!!val) {
                                    if (settings_var.maxRange < Number(val)) {
                                        results.customMessage = 'Invalid maximum range on variable ' +
                                            settings_var.name + ' at level ' + (Number(index) + Number(1));
                                        results.hasError = true;
                                        return results.hasError;
                                    } else if (settings_var.minRange > Number(val)) {
                                        results.customMessage = 'Invalid minimum range on variable ' +
                                            settings_var.name + ' at level ' + (Number(index) + Number(1));
                                        results.hasError = true;
                                        return results.hasError;
                                    }
                                }

                            });
                        }
                    });

                    if (results.hasError) {
                        return function(tab) { results.customHeader += tab; return results;  }('on Treatment Factors');
                    }
                }
            };

            // store the initial values on some service properties so that we can revert to it later
            $localStorage.serviceBackup = {
                settings : angular.copy(service.settings),
                currentData : angular.copy(service.currentData),
                specialSettings : angular.copy(service.specialSettings),
                applicationData : angular.copy(service.applicationData),
                trialMeasurement : angular.copy(service.trialMeasurement)
            };

            // 5 is the group no of treatment factors
            TrialSettingsManager.addDynamicFilterObj(service.currentData.treatmentFactors,5);

            // assign auxilliary settings to the service.settings items to indicate it came from a initialization
            _.each(service.settings,function(val,settingsKey) {
                if (val instanceof angular.OrderedHash) {
                    _.find(val.vals(),function(_val) {
                        service.specialSettings.auxSettings[_val.variable.cvTermId] = { initialData :true  };
                    });

                } else {
                    _.each(val,function(_val,key) {

                        if (_val instanceof angular.OrderedHash) {
                            _.find(_val.vals(),function(__val) {
                                service.specialSettings.auxSettings[__val.variable.cvTermId] = { initialData :true  };
                            });
                        }
                    });
                }

            });


            return service;
        }
    ])

    .service('TrialSettingsManager', ['TRIAL_VARIABLE_SELECTION_LABELS', function(TRIAL_VARIABLE_SELECTION_LABELS) {
        var TrialSettingsManager = window.TrialSettingsManager;
        var settingsManager = new TrialSettingsManager(TRIAL_VARIABLE_SELECTION_LABELS);



        return {
            openVariableSelectionDialog: function (params) {
                settingsManager._openVariableSelectionDialog(params);
            },

            // @param = this map contains variables of the pair that will be filtered
            addDynamicFilterObj: function (_map, group) {
                settingsManager._addDynamicFilter(_map, group);
            }


        };
    }]);
})();
