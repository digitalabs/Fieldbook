/**
 * Created by cyrus on 7/2/14.
 */

/* global angular */
(function(){
    'use strict';

    angular.module('manageTrialApp')
        .constant("EXPERIMENTAL_DESIGN_PARTIALS_LOC", "/Fieldbook/static/angular-templates/experimentalDesignPartials/")
        .controller('ExperimentalDesignCtrl',['$scope','$state','EXPERIMENTAL_DESIGN_PARTIALS_LOC','TrialManagerDataService',function($scope,$state,EXPERIMENTAL_DESIGN_PARTIALS_LOC,TrialManagerDataService){

            //TODO: temporarily hide features that are not implemented in this release
            $scope.hideFeatures = true;

            $scope.settings = {
                trialEnvironments : TrialManagerDataService.settings.trialSettings.keys().length ? TrialManagerDataService.settings.trialSettings.keys().length : 0,
                factors: TrialManagerDataService.specialSettings.experimentalDesign.factors,
                treatmentFactors : TrialManagerDataService.settings.treatmentFactors,
                treatments: TrialManagerDataService.settings.treatmentFactors.keys().length ? TrialManagerDataService.settings.treatmentFactors.keys().length : 0,

            };

            $scope.designTypes = [
                {
                    id: 0,
                    name: 'Randomized Complete Block Design',params :'randomizedCompleteBlockParams.html',
                    data: {
                        'designType': 0,
                        'replicationsCount' : 5,
                        'treatmentFactors' : $scope.settings.treatmentFactors,
                        'treatmentFactorsData': TrialManagerDataService.currentData.treatmentFactors
                    },
                    settings: {
                        trialEnvironments: $scope.settings.trialEnvironments,
                        factors : $scope.settings.factors
                    }
                },
                {
                    id: 1,
                    name: 'Incomplete Block Design', params: 'incompleteBlockParams.html',
                    withResolvable: true,
                    showAdvancedOptions: false,
                    data: {
                        'designType': 1,
                        'isResolvable' : true,
                        'replicationsCount' : 0,
                        'blockSize' : 0,
                        'useLatenized' : true,
                        'contiguousBlocksToLatenize' : 0,
                        'replicationsPerCol' : 0
                    },
                    settings: {
                        trialEnvironments: $scope.settings.trialEnvironments,
                        treatments: $scope.settings.treatments,
                        factors : $scope.settings.factors
                    }
                },
                {
                    id: 2,
                    name: 'Row-and-Column',params:'rowAndColumnParams.html',
                    withResolvable: true,
                    showAdvancedOptions: false,
                    data: {
                        'designType': 2,
                        'isResolvable' : true,
                        'replicationsCount': 0,
                        'rowsPerReplications' : 0,
                        'colsPerReplications' : 0,
                        'contiguousRowsToLatenize':0,
                        'contiguousColToLatenize': 0
                    },
                    settings: {
                        trialEnvironments: $scope.settings.trialEnvironments,
                        treatments: $scope.settings.treatments,
                        factors : $scope.settings.factors
                    }
                }];

            $scope.currentDesignType = $scope.designTypes[0];

            $scope.currentParams = EXPERIMENTAL_DESIGN_PARTIALS_LOC + $scope.currentDesignType.params;

            $scope.onSwitchDesignTypes = function(selectedDesignType) {
               $scope.currentParams = EXPERIMENTAL_DESIGN_PARTIALS_LOC + selectedDesignType.params;
               $scope.currentDesignType = selectedDesignType;
            };

            // on click generate design button
            $scope.generateDesign = function() {
                var data = angular.copy($scope.currentDesignType.data);
                // transform ordered has of treatment factors if existing to just the map
                if (data.treatmentFactors) {
                    data.treatmentFactors = $scope.currentDesignType.data.treatmentFactors.vals();
                }

                TrialManagerDataService.generateExpDesign(data).then(
                    function (response) {
                    }
                );
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