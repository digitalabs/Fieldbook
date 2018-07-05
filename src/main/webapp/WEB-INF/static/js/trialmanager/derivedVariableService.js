(function() {
    'use strict';

    var manageTrialApp = angular.module('manageTrialApp');

    manageTrialApp.factory('derivedVariableService', ['$http','$q', function($http, $q) {

        var derivedVariableService = {};

        derivedVariableService.getDependencies = function () {
            return $http.get('/Fieldbook/DerivedVariableController/derived-variable/dependencies');
        };
        derivedVariableService.hasMeasurementData = function (variableIds) {
            return $http.post('/Fieldbook/DerivedVariableController/derived-variable/dependencyVariableHasMeasurementData/',
                variableIds, {cache: false});
        };

        return derivedVariableService;

    }]);

})();
