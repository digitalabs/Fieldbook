/**
 * Created by cyrus on 7/2/14.
 */

/* global angular, showErrorMessage, showSuccessfulMessage, showMeasurementsPreview */
(function(){
    'use strict';

    angular.module('manageTrialApp')
        .constant('EXPERIMENTAL_DESIGN_PARTIALS_LOC', '/Fieldbook/static/angular-templates/experimentalDesignPartials/')
        .controller('ExperimentalDesignCtrl',['$scope','$state','EXPERIMENTAL_DESIGN_PARTIALS_LOC','TrialManagerDataService',function($scope,$state,EXPERIMENTAL_DESIGN_PARTIALS_LOC,TrialManagerDataService){

            //TODO: temporarily hide features that are not implemented in this release
            //$scope.hideFeatures = true;

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

            $scope.replatinGroupsOpts = ['Please Choose','single col','single row','adjacent'];

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
                if (data.treatmentFactors) {
                    data.treatmentFactors = $scope.currentDesignType.data.treatmentFactors.vals();
                }

                TrialManagerDataService.generateExpDesign(data).then(
                    function (response) {
                        if(response.valid === true){
                            //we show the preview
                            showSuccessfulMessage('', 'Experimental Design generated successfully, please check the measurements tab');
                            showMeasurementsPreview();
                        }else{
                            showErrorMessage('', response.message);
                        }
                    }
                );
            };


            $scope.doValidate = function() {
                switch($scope.currentDesignType.id) {
                    case 0: {
                        // validate replication count, must be 1 to 11
                        if (!($scope.currentDesignType.data.replicationsCount > 0 && $scope.currentDesignType.data.replicationsCount <= 10)) {
                            showErrorMessage('page-message','Number of Replications must be between 1 to 10');
                            return false;
                        }

                        if (!$scope.settings.treatmentFactors || !TrialManagerDataService.currentData.treatmentFactors) {
                            showErrorMessage('page-message','Please setup the treatment factors to be used');
                            return false;
                        }

                        break;
                    }
                    case 1: {

                        if (!($scope.currentDesignType.data.replicationsCount > 1 && $scope.currentDesignType.data.replicationsCount <= 10)) {
                            showErrorMessage('page-message','Number of Replications must be between 2 to 10');
                            return false;
                        }

                        break;
                    }
                    case 2: {
                        if (!($scope.currentDesignType.data.replicationsCount > 1 && $scope.currentDesignType.data.replicationsCount <= 10)) {
                            showErrorMessage('page-message','Number of Replications must be between 2 to 10');
                            return false;
                        }

                        if ($scope.currentDesignType.data.rowsPerReplications * $scope.currentDesignType.data.colsPerReplications !== $scope.totalGermplasmEntryListCount) {
                            showErrorMessage('page-message','Product of rows and cols (rows x cols) should be equal to the number of treatments');
                            return false;
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
        });


})();