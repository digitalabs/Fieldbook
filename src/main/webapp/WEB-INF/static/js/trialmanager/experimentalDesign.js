/**
 * Created by cyrus on 7/2/14.
 */

/* global angular, showErrorMessage, showSuccessfulMessage, showMeasurementsPreview, expDesignMsgs */
(function () {
    'use strict';

    angular.module('manageTrialApp')
        .constant('EXP_DESIGN_MSGS', expDesignMsgs)
        .constant('EXPERIMENTAL_DESIGN_PARTIALS_LOC', '/Fieldbook/static/angular-templates/experimentalDesignPartials/')
        .controller('ExperimentalDesignCtrl', ['$scope', '$state', 'EXPERIMENTAL_DESIGN_PARTIALS_LOC', 'TrialManagerDataService',
            'EXP_DESIGN_MSGS', '_',function ($scope, $state, EXPERIMENTAL_DESIGN_PARTIALS_LOC, TrialManagerDataService, EXP_DESIGN_MSGS) {

                //$scope.hideFeatures = true;
                $scope.Math = Math;
                $scope.designTypes = [
                    {
                        id: 0,
                        name: 'Randomized Complete Block Design', params: 'randomizedCompleteBlockParams.html'
                    },
                    {
                        id: 1,
                        name: 'Incomplete Block Design', params: 'incompleteBlockParams.html',
                        withResolvable: true
                    },
                    {
                        id: 2,
                        name: 'Row-and-Column', params: 'rowAndColumnParams.html',
                        withResolvable: true
                    }
                ];

                // TODO : re run computeLocalData after loading of previous trial as template
                $scope.computeLocalData = function () {
                    $scope.settings = TrialManagerDataService.specialSettings.experimentalDesign;
                    $scope.settings.treatmentFactors = TrialManagerDataService.settings.treatmentFactors.details;

                    // user has a treatment factor, if previous exp design is not RCBD, then set selection to RCBD
                    // may need to clear non RCBD input
                    if (TrialManagerDataService.settings.treatmentFactors.details.keys().length > 0) {
                        $scope.data.designType = $scope.designTypes[0].id;
                    }

                    $scope.currentDesignType = $scope.designTypes[$scope.data.designType];
                    $scope.currentDesignTypeId = $scope.currentDesignType.id;
                    $scope.germplasmDescriptorSettings = TrialManagerDataService.settings.germplasm;
                    $scope.disableGenerateDesign = TrialManagerDataService.trialMeasurement.hasMeasurement;
                    $scope.data.noOfEnvironments = TrialManagerDataService.currentData.environments.noOfEnvironments ?
                                                TrialManagerDataService.currentData.environments.noOfEnvironments : 0;
                    $scope.data.treatmentFactors = TrialManagerDataService.settings.treatmentFactors.details;
                    $scope.data.treatmentFactorsData = TrialManagerDataService.currentData.treatmentFactors.currentData;

                    $scope.currentParams = EXPERIMENTAL_DESIGN_PARTIALS_LOC + $scope.currentDesignType.params;
                    if (!$scope.settings.showAdvancedOptions[$scope.currentDesignType.id]) {
                        $scope.settings.showAdvancedOptions[$scope.currentDesignType.id] = $scope.data.useLatenized;
                    }
                };

                //FIXME: cheating a bit for the meantime.
                if (!TrialManagerDataService.applicationData.germplasmListCleared) {
                    $scope.totalGermplasmEntryListCount = TrialManagerDataService.specialSettings.experimentalDesign.
                        germplasmTotalListCount = parseInt($('#totalGermplasms').val() ? $('#totalGermplasms').val() :
                        TrialManagerDataService.specialSettings.experimentalDesign.germplasmTotalListCount);
                }
                else {
                    $scope.totalGermplasmEntryListCount = TrialManagerDataService.specialSettings.experimentalDesign.
                        germplasmTotalListCount = parseInt($('#totalGermplasms').val() ? $('#totalGermplasms').val() : 0);
                }

                if (isNaN($scope.totalGermplasmEntryListCount)) {
                    $scope.totalGermplasmEntryListCount = TrialManagerDataService.specialSettings.
                        experimentalDesign.germplasmTotalListCount = 0;
                }

                $scope.data = TrialManagerDataService.currentData.experimentalDesign;

                if (!$scope.data || Object.keys($scope.data).length === 0) {
                    angular.copy({
                        totalGermplasmListCount: $scope.totalGermplasmEntryListCount,
                        designType: 0,
                        'replicationsCount': null,
                        isResolvable : true,
                        'blockSize': null,
                        'useLatenized': false,
                        'nblatin': null,
                        'replicationsArrangement': null,
                        'rowsPerReplications': null,
                        'colsPerReplications': null,
                        'nrlatin': null,
                        'nclatin': null,
                        'replatinGroups': ''
                    },$scope.data);
                }

                TrialManagerDataService.specialSettings.experimentalDesign.data = $scope.data;

                $scope.computeLocalData();

                $scope.replicationsArrangementGroupsOpts = {};
                $scope.replicationsArrangementGroupsOpts[1] = 'In a single column';
                $scope.replicationsArrangementGroupsOpts[2] = 'In a single row';
                $scope.replicationsArrangementGroupsOpts[3] = 'In adjacent columns';

                $scope.onSwitchDesignTypes = function (newId) {
                    $scope.currentDesignType = $scope.designTypes[newId];
                    $scope.currentParams = EXPERIMENTAL_DESIGN_PARTIALS_LOC + $scope.currentDesignType.params;
                    $scope.data.designType = $scope.currentDesignType.id;
                };

                // on click generate design button
                $scope.generateDesign = function () {
                    if (!$scope.doValidate()) {
                        return;
                    }

                    var data = angular.copy($scope.data);
                    // transform ordered has of treatment factors if existing to just the map
                    if (data && data.treatmentFactors) {
                        data.treatmentFactors = $scope.data.treatmentFactors.vals();
                    }

                    TrialManagerDataService.generateExpDesign(data).then(
                        function (response) {
                            if (response.valid === true) {
                                //we show the preview
                                showSuccessfulMessage('', 'Experimental Design generated successfully, please check the measurements tab');
                                TrialManagerDataService.clearUnappliedChangesFlag();
                                TrialManagerDataService.applicationData.unsavedGeneratedDesign = true;
                                $('#chooseGermplasmAndChecks').data('replace', '1');                                
                                $('body').data('expDesignShowPreview', '1');
                            } else {
                                showErrorMessage('', response.message);
                            }
                        }
                    );
                };

                $scope.designTypeOptions = function() {
                    var options = {
                        data : function() {
                            var data = [];
                            if (TrialManagerDataService.settings.treatmentFactors.details.keys().length > 0) {
                                data.push($scope.designTypes[0]);
                            } else {
                                data = $scope.designTypes;
                            }
                            return {
                                results : data
                            };
                        },
                        formatResult : function(value) {
                            return value.name;
                        },
                        formatSelection : function(value) {
                            return value.name;
                        },
                        idAsValue : true
                    };

                    return options;
                };

                $scope.determineDesignTypeFilter = function() {
                    if (TrialManagerDataService.settings.treatmentFactors.details) {
                        return function(entry) {
                            return entry.name === 'Randomized Complete Block Design';
                        };
                    }
                    return function() {
                        return true;
                    };
                };

                $scope.doValidate = function () {

                    switch ($scope.currentDesignType.id) {
                        case 0:
                        {
                            if (!$scope.data.replicationsCount || $scope.expDesignForm.replicationsCount.$invalid) {
                                showErrorMessage('page-message', EXP_DESIGN_MSGS[4]);
                                return false;
                            }

                            if (!$scope.settings.treatmentFactors || !TrialManagerDataService.currentData.treatmentFactors.currentData) {
                                showErrorMessage('page-message', EXP_DESIGN_MSGS[18]);
                                return false;
                            }

                            var isValidTreatmentVars = TrialManagerDataService.validateAllTreatmentFactorLabels({});
                            if (!!isValidTreatmentVars && isValidTreatmentVars.hasError) {
                                showErrorMessage(isValidTreatmentVars.customHeader,isValidTreatmentVars.customMessage);
                                return false;
                            }

                            var errorCode = TrialManagerDataService.treatmentFactorDataInvalid();

                            if (errorCode === 1) {
                                showErrorMessage('page-message', EXP_DESIGN_MSGS[24]);
                                return false;
                            } else if (errorCode === 2) {
                                showErrorMessage('page-message', EXP_DESIGN_MSGS[25]);
                                return false;
                            }


                            break;
                        }
                        case 1:
                        {

                            if (!$scope.data.replicationsCount || $scope.expDesignForm.replicationsCount.$invalid) {
                                showErrorMessage('page-message', EXP_DESIGN_MSGS[5]);
                                return false;
                            }

                            if (!$scope.data.blockSize || $scope.expDesignForm.blockSize.$invalid) {
                                showErrorMessage('page-message', EXP_DESIGN_MSGS[8]);
                                return false;
                            }

                            if ($scope.totalGermplasmEntryListCount % $scope.data.blockSize > 0) {
                                showErrorMessage('page-message', EXP_DESIGN_MSGS[13]);
                                return false;
                            }

                            // latinized
                            if ($scope.data.useLatenized) {
                                if ($scope.data.nblatin === null) {
                                    showErrorMessage('page-message', EXP_DESIGN_MSGS[27]);
                                    return false;
                                }
                                if ($scope.data.nblatin >= ($scope.totalGermplasmEntryListCount / $scope.data.blockSize)) {
                                    showErrorMessage('page-message', EXP_DESIGN_MSGS[11]);
                                    return false;
                                }

                                if ($scope.data.nblatin >= Number($scope.data.replicationsCount)) {
                                    showErrorMessage('page-message', EXP_DESIGN_MSGS[23]);
                                    return false;
                                }

                                if (Number($scope.data.replicationsArrangement) <= 0) {
                                    showErrorMessage('page-message', EXP_DESIGN_MSGS[21]);
                                    return false;

                                }
                                if (Number($scope.data.replicationsArrangement) === 3) {
                                    if (!$scope.data.replatinGroups || $scope.expDesignForm.replatinGroups.$invalid) {
                                        showErrorMessage('page-message', EXP_DESIGN_MSGS[22]);
                                        return false;
                                    }

                                    // validate sum of replatinGroups
                                    var sum = 0;
                                    var arrGroups = $scope.data.replatinGroups.split(',');

                                    for (var i = 0; i < arrGroups.length; i++) {
                                        sum += Number(arrGroups[i]);
                                    }

                                    if (sum !== Number($scope.data.replicationsCount)) {
                                        showErrorMessage('page-message', EXP_DESIGN_MSGS[12]);
                                        return false;
                                    }
                                }
                            }

                            break;
                        }
                        case 2:
                        {
                            if (!$scope.data.replicationsCount && $scope.expDesignForm.replicationsCount.$invalid) {
                                showErrorMessage('page-message', EXP_DESIGN_MSGS[5]);
                                return false;
                            }

                            if ($scope.data.rowsPerReplications * $scope.data.colsPerReplications !== $scope.totalGermplasmEntryListCount) {
                                showErrorMessage('page-message', EXP_DESIGN_MSGS[6]);
                                return false;
                            }

                            if ($scope.data.useLatenized) {

                                if ($scope.data.nrlatin >= $scope.data.replicationsCount) {
                                    showErrorMessage('page-message', EXP_DESIGN_MSGS[15]);
                                    return false;
                                }

                                if ($scope.data.nclatin >= $scope.data.replicationsCount) {
                                    showErrorMessage('page-message', EXP_DESIGN_MSGS[16]);
                                    return false;
                                }

                                if ($scope.data.nrlatin <= 0 || $scope.data.nrlatin >= $scope.data.rowsPerReplications) {
                                    showErrorMessage('page-message', EXP_DESIGN_MSGS[14]);
                                    return false;

                                }

                                if ($scope.data.nclatin <= 0 || $scope.data.nclatin >= $scope.data.colsPerReplications) {
                                    showErrorMessage('page-message', EXP_DESIGN_MSGS[17]);
                                    return false;

                                }

                                if ($scope.data.replicationsArrangement <= 0) {
                                    showErrorMessage('page-message', EXP_DESIGN_MSGS[21]);
                                    return false;
                                }

                                if (Number($scope.data.replicationsArrangement) === 3) {
                                    if (!$scope.data.replatinGroups || $scope.expDesignForm.replatinGroups.$invalid) {
                                        showErrorMessage('page-message', EXP_DESIGN_MSGS[22]);
                                        return false;
                                    }

                                    // validate sum of replatinGroups
                                    var _sum = 0;
                                    var _arrGroups = $scope.data.replatinGroups.split(',');

                                    for (var j = 0; j < _arrGroups.length; j++) {
                                        _sum += Number(_arrGroups[j]);
                                    }

                                    if (_sum !== Number($scope.data.replicationsCount)) {
                                        showErrorMessage('page-message', EXP_DESIGN_MSGS[12]);
                                        return false;
                                    }
                                }


                            }


                            break;
                        }
                    }

                    if ($scope.totalGermplasmEntryListCount <= 0) {
                        showErrorMessage('page-message', EXP_DESIGN_MSGS[26]);
                        return false;
                    }

                    return true;
                };


            }])

        // FILTERS USED FOR EXP DESIGN

        .filter('filterFactors', function () {
            return function (factorList, designTypeIndex) {

                var excludes = [
                    [8230, 8210, 8581, 8582],
                    [8581, 8582],
                    [8220, 8200]
                ];

                var copyList = angular.copy(factorList);

                angular.forEach(excludes[designTypeIndex], function (val) {
                    for (var i = 0; i < copyList.length; i++) {
                        if (copyList[i] === val) {
                            copyList.splice(i, 1);
                        }
                    }
                });

                return copyList;
            };
        })

        .filter('filterExperimentalDesignType', function(TrialManagerDataService) {
            return function(designTypes) {
                var result = [];
                if (TrialManagerDataService.settings.treatmentFactors.details.keys().length > 0) {
                    result.push(designTypes[0]);
                } else {
                    result = designTypes;
                }

                return result;
            };
        });


})();
