/**
 * Created by cyrus on 7/2/14.
 */

/* global angular */
(function(){
    'use strict';

    angular.module('manageTrialApp')
        .constant("EXPERIMENTAL_DESIGN_PARTIALS_LOC", "/Fieldbook/static/angular-templates/experimentalDesignPartials/")
        .controller('ExperimentalDesignCtrl',['$scope','$state','EXPERIMENTAL_DESIGN_PARTIALS_LOC',function($scope,$state,EXPERIMENTAL_DESIGN_PARTIALS_LOC){

            //TODO: temporarily hide features that are not implemented in this release
            $scope.hideFeatures = true;

            $scope.designTypes = [
                {name: 'Randomized Complete Block Design',params :'randomizedCompleteBlockParams.html',
                    data: {
                        'replicationsCount' : 5
                    },
                    settings: {
                        trialEnvironments: 4,
                        factors : {},
                        treatmentFactors : {}
                    }
                },
                {name: 'Incomplete Block Design', params: 'incompleteBlockParams.html',
                    withResolvable: true,
                    showAdvancedOptions: false,
                    data: {
                        'isResolvable' : true,
                        'replicationsCount' : 0,
                        'blockSize' : 0,
                        'useLatenized' : true,
                        'contiguousBlocksToLatenize' : 0,
                        'replicationsPerCol' : 0
                    },
                    settings: {
                        trialEnvironments: 6,
                        treatments: 24,
                        blocks: 6,
                        factors : {}
                    }
                },
                {name: 'Row-and-Column',params:'rowAndColumnParams.html',
                    withResolvable: true,
                    showAdvancedOptions: false,
                    data: {
                        'isResolvable' : true,
                        'replicationsCount': 0,
                        'rowsPerReplications' : 0,
                        'colsPerReplications' : 0,
                        'contiguousRowsToLatenize':0,
                        'contiguousColToLatenize': 0
                    },
                    settings: {
                        trialEnvironments: 3,
                        treatments: 24,
                        factors : {}
                    }
                }];

            $scope.currentDesignType = $scope.designTypes[0];

            $scope.currentParams = EXPERIMENTAL_DESIGN_PARTIALS_LOC + $scope.currentDesignType.params;

            $scope.onSwitchDesignTypes = function(selectedDesignType) {
               $scope.currentParams = EXPERIMENTAL_DESIGN_PARTIALS_LOC + selectedDesignType.params;
               $scope.currentDesignType = selectedDesignType;
            };
    }]);

})();


