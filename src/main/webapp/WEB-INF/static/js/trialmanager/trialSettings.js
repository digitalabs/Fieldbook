/**
 * Created by cyrus on 7/2/14.
 */

/*global angular*/

(function(){
    'use strict';

    var manageTrialApp = angular.module('manageTrialApp');

    manageTrialApp.controller('TrialSettingsCtrl',['$scope','TrialSettingsService',function($scope,TrialSettingsService) {
        TrialSettingsService.addDummyData();
        $scope.data = TrialSettingsService.data;


        $scope.addManagementVariables = function(result) {
            $.each(result,function(key,val){
                TrialSettingsService.addManagementDetailVar(key,val);
            });
        };

        $scope.removeVariable = function(cvTermId) {
            //TrialSettingsService.removeUserInputByIndex(cvTermId);
            TrialSettingsService.removeManagementDetailVarByIndex(cvTermId);
        };

    }]);

    // factory and services
    manageTrialApp.factory('TrialSettingsService',function() {
        return {
            data : {
                managementDetails : {},
                userInput : {}
            },

            addManagementDetailVar : function(cvTermId,item) {
                this.data.managementDetails[cvTermId] = item;
                this.data.managementDetails[cvTermId].variableLabelCount = 0;
                this.data.managementDetails[cvTermId].variableLabels = [];
            },

            removeUserInputByIndex : function(cvTermId) {
                if (cvTermId !== undefined && cvTermId !== null && this.userInput[cvTermId] !== undefined) {
                    delete this.userInput[cvTermId];
                }
            },

            removeManagementDetailVarByIndex : function(index) {
                if (index !== undefined && index !== null) {
                    delete this.data.managementDetails[index];
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



