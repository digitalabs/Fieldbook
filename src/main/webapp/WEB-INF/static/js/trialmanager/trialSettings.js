/**
 * Created by cyrus on 7/2/14.
 */

/*global angular*/

(function(){
    'use strict';

    var manageTrialApp = angular.module('manageTrialApp');

    manageTrialApp.controller('TrialSettingsCtrl',['$scope','TrialSettingsService',function($scope,TrialSettingsService) {

    }]);

    // factory and services
    manageTrialApp.factory('TrialSettingsService',function() {
        return {
            //TODO: map this to whatever DanV is doing to variables
            managementDetailsVars : [
                {
                    variableName : "FERTILE",
                    variableLink : "/somewhere/in/the/galaxy",
                    variableDescription : "Fertilizer level",
                    variableScale : "kg/hectars",
                    variableLevelCount : 0,
                    variableLabelsForEachCount : []
                },

                {
                    variableName : "PDATE",
                    variableLink : "/somewhere/in/the/galaxy",
                    variableDescription : "Planting date",
                    variableScale : "Date",
                    variableLevelCount : 0,
                    variableLabelsForEachCount : []
                }
            ],

            // TODO: if factors var has no value, we should retrieve it from spring service
            addDummyData : function() {
                this.addVariable({
                    variableName : "DUMMY_BAKA",
                    variableLink : "/somewhere/in/the/galaxy",
                    variableDescription : "You dummy!!! BAKA!",
                    variableScale : "Hmmph",
                    variableLevelCount : 3,
                    variableLabelsForEachCount : ['a','b','c']

                });
            },

            addVariable : function(treatmentFactor) {
                this.managementDetailsVars.push(treatmentFactor);
            },

            removeVariableByIndex : function(index) {
                if ( index !== null) {
                    this.managementDetailsVars.splice(index, 1);
                }
            },

            removeVariable : function(variableName) {
                if ( variableName !== null) {
                    for(var i = this.managementDetailsVars.length; i--;) {
                        if(this.managementDetailsVars[i] === variableName) {
                            this.managementDetailsVars.splice(i, 1);
                        }
                    }
                }
            }
        };
    });


})();



