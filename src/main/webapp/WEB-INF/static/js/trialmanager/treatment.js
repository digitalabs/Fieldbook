/**
 * Created by cyrus on 7/1/14.
 */

/*global angular*/
/*global showBaselineTraitDetailsModal*/
(function(){
    'use strict';

    var manageTrialApp = angular.module('manageTrialApp');

    manageTrialApp.controller('TreatmentCtrl',['$scope','TreatmentFactorsService',function($scope,TreatmentFactorsService) {
        $scope.data = TreatmentFactorsService.data;
        $scope.addManagementVariables = function(result) {
            $.each(result, function (key, val) {
                TreatmentFactorsService.addManagementDetailVar(key,val);
            });
        };

        $scope.showDetailsModal = function(cvTermId) {
            // this function is currently defined in the fieldbook-common.js, loaded globally for the page
            // TODO : move away from global function definitions
            showBaselineTraitDetailsModal(cvTermId);
        };

        $scope.onLevelChange = function(variableName,levelCount) {
            levelCount = parseInt(levelCount);
            // search the treatment factors then update it
            $.each($scope.data.managementDetails, function (key, val) {
                if (val.variable.name === variableName) {

                    if (isNaN(levelCount)) { return; }

                    var diff = Math.abs(val.variableLabels.length - levelCount);

                    // remove items if no of levels is less thant array
                    if (val.variableLabels.length  > levelCount) {
                        while(val.variableLabels.length > levelCount) {
                            val.variableLabels.pop();
                        }
                    }

                    // add items if no of levels is more thant array
                    else {
                        for (var j = 0; j < diff; j++) {
                            val.variableLabels.push('');
                        }
                    }

                    return false;   // this breaks the each
                }
            });

        }; // end $scope.onLevelChange

        $scope.removeTreatmentFactorByIndex = TreatmentFactorsService.removeManagementDetailVarByIndex;

    }]);

    /**
     * Data Structure of variable :
     * { CV_ID : { variable : {
     *
     *  cvTermId:
        name: LOCATION_ABBR
        description: Location code - assigned (LOC_ABBR)
        property: Location
        scale: LOC_ABBR
        method: Assigned
        role: Trial environment information
        dataType: Character  variable
        traitClass: Trial environment
        cropOntologyId:
        dataTypeId: 1120
        minRange: null
        maxRange: null
        widgetType: CTEXT
        operation: ADD
     *
     * },  }
     *
     */

    // factory and services
    manageTrialApp.factory('TreatmentFactorsService',function() {
        return {
            data : {
                managementDetails : {}
            },

            addManagementDetailVar : function(cvTermId,item) {
                this.data.managementDetails[cvTermId] = item;
                this.data.managementDetails[cvTermId].variableLabelCount = 0;
                this.data.managementDetails[cvTermId].variableLabels = [];
            },

            removeManagementDetailVarByIndex : function(index) {
                if ( index !== null) {
                    this.data.managementDetails.delete(index);
                }
            },

            addDummyData : function() {
                this.addManagementDetailVar(8195,{
                    variable: {
                        cvTermId: 8195,
                        name: 'SITE_CODE',
                        description: 'Site code - assigned (text)',
                        property: 'Location',
                        scale: 'Code',
                        method: 'Assigned',
                        role: 'Trial environment information',
                        dataType: 'Character variable',
                        traitClass: 'Trial environment',
                        cropOntologyId: '',
                        dataTypeId: 1120,
                        minRange: null,
                        maxRange: null,
                        widgetType: 'CTEXT',
                        operation: 'ADD'
                    },
                    possibleValues: [ ],
                    possibleValuesFavorite: null,
                    possibleValuesJson: null,
                    possibleValuesFavoriteJson: null,
                    value: null,
                    order: 0,
                    group: null,
                    deletable: true,
                    favorite: false
                });
            }
        };
    });

})();



