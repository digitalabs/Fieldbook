(function() {
    'use strict';
    angular.module('manageTrialApp').service('studyStateService', [
        function() {
            var service = {
                state: {
                    // hasUnsavedData becomes true when adding/deleting environments and variables in a study.
                    hasUnsavedData: false
                },

                updateOccurred: function () {
                    service.state.hasUnsavedData = true;
                },

                resetState: function () {
                    service.state.hasUnsavedData = false;
                },

                hasUnsavedData: function() {
                    return service.state.hasUnsavedData;
                }
            };
            return service;
        }
    ]);
})();