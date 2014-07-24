/**
 * Created by cyrus on 7/2/14.
 */

/* global angular, showErrorMessage, showSuccessfulMessage, showMeasurementsPreview, expDesignMsgs */
(function(){
    'use strict';

    angular.module('manageTrialApp')
        .constant('EXP_DESIGN_MSGS',expDesignMsgs)
        .constant('EXPERIMENTAL_DESIGN_PARTIALS_LOC', '/Fieldbook/static/angular-templates/experimentalDesignPartials/')
        .controller('ExperimentalDesignCtrl',['$scope','$state','EXPERIMENTAL_DESIGN_PARTIALS_LOC','TrialManagerDataService','EXP_DESIGN_MSGS',function($scope,$state,EXPERIMENTAL_DESIGN_PARTIALS_LOC,TrialManagerDataService,EXP_DESIGN_MSGS){

            //TODO: temporarily hide features that are not implemented in this release
            //$scope.hideFeatures = true;
            $scope.Math = Math;
            $scope.germplasmDescriptorSettings = TrialManagerDataService.settings.germplasm;
            //FIXME: cheating a bit for the meantime.
            $scope.totalGermplasmEntryListCount = TrialManagerDataService.specialSettings.experimentalDesign.germplasmTotalListCount = parseInt($('#totalGermplasms').val() ? $('#totalGermplasms').val() : 0);

            $scope.settings = {
                factors: TrialManagerDataService.specialSettings.experimentalDesign.factors,
                treatmentFactors : TrialManagerDataService.settings.treatmentFactors
            };

            // initialize some data not in currentData
            TrialManagerDataService.specialSettings.experimentalDesign.data.noOfEnvironments = TrialManagerDataService.currentData.environments.noOfEnvironments ? TrialManagerDataService.currentData.environments.noOfEnvironments : 0;
            TrialManagerDataService.specialSettings.experimentalDesign.data.treatmentFactors = $scope.settings.treatmentFactors;
            TrialManagerDataService.specialSettings.experimentalDesign.data.treatmentFactorsData = TrialManagerDataService.currentData.treatmentFactors;
            TrialManagerDataService.specialSettings.experimentalDesign.data.totalGermplasmListCount = $scope.totalGermplasmEntryListCount;

            $scope.replicationsArrangementGroupsOpts = {};
            $scope.replicationsArrangementGroupsOpts[1] = 'single col';
            $scope.replicationsArrangementGroupsOpts[2] = 'single row';
            $scope.replicationsArrangementGroupsOpts[3] = 'adjacent';

            $scope.designTypes = [
            {
                id: 0,
                name: 'Randomized Complete Block Design',params :'randomizedCompleteBlockParams.html',
                data : TrialManagerDataService.specialSettings.experimentalDesign.data
            },
            {
                id: 1,
                name: 'Incomplete Block Design', params: 'incompleteBlockParams.html',
                withResolvable: true,
                showAdvancedOptions: false,
                data : TrialManagerDataService.specialSettings.experimentalDesign.data
            },
            {
                id: 2,
                name: 'Row-and-Column',params:'rowAndColumnParams.html',
                withResolvable: true,
                showAdvancedOptions: false,
                data : TrialManagerDataService.specialSettings.experimentalDesign.data
            }];

            $scope.currentDesignType = $scope.designTypes[TrialManagerDataService.specialSettings.experimentalDesign.data.designType];

            //$scope.noOfBlocks = ($scope.currentDesignType.data.blockSize > 0) ? $scope.totalGermplasmEntryListCount / $scope.currentDesignType.data.blockSize : 0;


            $scope.currentParams = EXPERIMENTAL_DESIGN_PARTIALS_LOC + $scope.currentDesignType.params;

            $scope.onSwitchDesignTypes = function(selectedDesignType) {
               $scope.currentParams = EXPERIMENTAL_DESIGN_PARTIALS_LOC + selectedDesignType.params;
               $scope.currentDesignType = selectedDesignType;
               $scope.currentDesignType.data.designType = selectedDesignType.id;
            };

            // on click generate design button
            $scope.generateDesign = function() {
                if (!$scope.doValidate()) {
                    return;
                }

                var data = angular.copy(TrialManagerDataService.specialSettings.experimentalDesign.data);
                // transform ordered has of treatment factors if existing to just the map
                if (data && data.treatmentFactors) {
                    data.treatmentFactors = $scope.currentDesignType.data.treatmentFactors.vals();
                }

                TrialManagerDataService.applicationData.unappliedChangesAvailable = false;

                TrialManagerDataService.generateExpDesign(data).then(
                    function (response) {
                        if(response.valid === true){
                            //we show the preview
                            showSuccessfulMessage('', 'Experimental Design generated successfully, please check the measurements tab');

                            if (TrialManagerDataService.isOpenTrial()) {
                                $state.go("editMeasurements");
                            } else {
                                $state.go("createMeasurements");
                            }

                            showMeasurementsPreview();
                        }else{
                            showErrorMessage('', response.message);
                        }
                    }
                );
            };

            // TODO FIXME Please put the messages in a global list
            $scope.doValidate = function() {

                switch($scope.currentDesignType.id) {
                    case 0: {
                        if (!$scope.currentDesignType.data.replicationsCount || $scope.expDesignForm.replicationsCount.$invalid) {
                            showErrorMessage('page-message',EXP_DESIGN_MSGS[4]);
                            return false;
                        }

                        if (!$scope.settings.treatmentFactors || !TrialManagerDataService.currentData.treatmentFactors) {
                            showErrorMessage('page-message',EXP_DESIGN_MSGS[18]);
                            return false;
                        }

                        break;
                    }
                    case 1: {

                        if (!$scope.currentDesignType.data.replicationsCount || $scope.expDesignForm.replicationsCount.$invalid) {
                            showErrorMessage('page-message',EXP_DESIGN_MSGS[5]);
                            return false;
                        }

                        if (!$scope.currentDesignType.data.blockSize || $scope.expDesignForm.blockSize.$invalid) {
                            showErrorMessage('page-message',EXP_DESIGN_MSGS[8]);
                            return false;
                        }

                        if ($scope.totalGermplasmEntryListCount % $scope.currentDesignType.data.blockSize > 0) {
                            showErrorMessage('page-message',EXP_DESIGN_MSGS[19]);
                            return false;
                        }

                        // latinized
                        if ($scope.currentDesignType.data.useLatenized) {
                            if ($scope.currentDesignType.data.nblatin >= ($scope.totalGermplasmEntryListCount / $scope.currentDesignType.data.blockSize)) {
                                showErrorMessage('page-message',EXP_DESIGN_MSGS[11]);
                                return false;
                            }
                            
                            if ($scope.currentDesignType.data.nblatin >= $scope.currentDesignType.data.replicationsCount) {
                                showErrorMessage('page-message',EXP_DESIGN_MSGS[23]);
                                return false;
                            }

                            if ($scope.currentDesignType.data.replicationsArrangement <= 0) {
                                showErrorMessage('page-message',EXP_DESIGN_MSGS[21]);
                                return false;

                            }
                            if (Number($scope.currentDesignType.data.replicationsArrangement) === 3) {
                                if (!$scope.currentDesignType.data.replatinGroups || $scope.expDesignForm.replatinGroups.$invalid) {
                                    showErrorMessage('page-message',EXP_DESIGN_MSGS[22]);
                                    return false;
                                }

                                // validate sum of replatinGroups
                                var sum=0;
                                var arrGroups =  $scope.currentDesignType.data.replatinGroups.split(",");

                                for (var i = 0; i < arrGroups.length;i++) {
                                    sum += Number(arrGroups[i]);
                                }

                                if (sum !== $scope.currentDesignType.data.replicationsCount) {
                                    showErrorMessage('page-message',EXP_DESIGN_MSGS[12]);
                                    return false;
                                }
                            }
                        }

                        break;
                    }
                    case 2: {
                        if (!$scope.currentDesignType.data.replicationsCount && $scope.expDesignForm.replicationsCount.$invalid) {
                            showErrorMessage('page-message',EXP_DESIGN_MSGS[5]);
                            return false;
                        }

                        if ($scope.currentDesignType.data.rowsPerReplications * $scope.currentDesignType.data.colsPerReplications !== $scope.totalGermplasmEntryListCount) {
                            showErrorMessage('page-message',EXP_DESIGN_MSGS[6]);
                            return false;
                        }

                        if ($scope.currentDesignType.data.useLatenized) {

                            if ($scope.currentDesignType.data.nrlatin >= $scope.currentDesignType.data.replicationsCount) {
                                showErrorMessage('page-message',EXP_DESIGN_MSGS[15]);
                                return false;
                            }

                            if ($scope.currentDesignType.data.nclatin >= $scope.currentDesignType.data.replicationsCount) {
                                showErrorMessage('page-message',EXP_DESIGN_MSGS[16]);
                                return false;
                            }

                            if($scope.currentDesignType.data.nrlatin <= 0 || $scope.currentDesignType.data.nrlatin >= $scope.currentDesignType.data.rowsPerReplications) {
                                showErrorMessage('page-message',EXP_DESIGN_MSGS[14]);
                                return false;

                            }

                            if($scope.currentDesignType.data.nclatin <= 0 || $scope.currentDesignType.data.nclatin >= $scope.currentDesignType.data.colsPerReplications) {
                                showErrorMessage('page-message',EXP_DESIGN_MSGS[17]);
                                return false;

                            }

                            if ($scope.currentDesignType.data.replicationsArrangement <= 0) {
                                showErrorMessage('page-message',EXP_DESIGN_MSGS[21]);
                                return false;
                            }

                            if (Number($scope.currentDesignType.data.replicationsArrangement) === 3) {
                                if (!$scope.currentDesignType.data.replatinGroups || $scope.expDesignForm.replatinGroups.$invalid) {
                                    showErrorMessage('page-message',EXP_DESIGN_MSGS[22]);
                                    return false;
                                }

                                // validate sum of replatinGroups
                                var _sum=0;
                                var _arrGroups =  $scope.currentDesignType.data.replatinGroups.split(",");

                                for (var j = 0; j < _arrGroups.length;j++) {
                                    _sum += Number(_arrGroups[j]);
                                }

                                if (_sum !== $scope.currentDesignType.data.replicationsCount) {
                                    showErrorMessage('page-message',EXP_DESIGN_MSGS[12]);
                                    return false;
                                }
                            }



                        }


                        break;
                    }
                }

                if ($scope.totalGermplasmEntryListCount <= 0) {
                    showErrorMessage('page-message','No germplasm list found');
                    return false;
                }

                return true;
            };


    }]).

        filter('filterFactors',function(){
            return function(factorList,designTypeIndex) {

                var excludes = [[8230,8210],[],[8210]];

                var copyList = angular.copy(factorList);

                angular.forEach(excludes[designTypeIndex],function(val,key) {
                    for (var i = 0; i < copyList.length; i++) {
                        if (copyList[i] === val)
                            {copyList.splice(i,1);}
                    }
                });

                return copyList;
            };
        })

        .directive('inputType', function () {
            return {
                require: 'ngModel',
                link: function (scope, elem, attrs, ctrl) {
                    // Custom number validation logic.
                    if (attrs.inputType === 'number') {
                        elem.attr('type', 'text');

                        return ctrl.$parsers.push(function (value) {
                            var valid = value === null || isFinite(value);

                            ctrl.$setValidity('number', valid);

                            return valid && value !== null ? Number(value) : undefined;
                        });
                    }

                    // Fallback to setting the default `type` attribute.
                    return elem.attr('type', attrs.inputType);
                }
            };
        })

        .directive('minVal', function () {
            return {
                require: 'ngModel',
                link: function (scope, elem, attrs, ctrl) {
                    return ctrl.$parsers.push(function (value) {
                        var valid = value === null || Number(value) >= Number(attrs.minVal);

                        ctrl.$setValidity('min', valid);

                        return valid ? value : undefined;
                    });
                }
            };
        })

        .directive('maxVal', function () {
            return {
                require: 'ngModel',
                link: function (scope, elem, attrs, ctrl) {
                    return ctrl.$parsers.push(function (value) {
                        var valid = value === null || Number(value) <= Number(attrs.maxVal);

                        ctrl.$setValidity('max', valid);

                        return valid ? value : undefined;
                    });
                }
            };
        });


})();
