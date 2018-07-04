(function() {
    'use strict';

    var manageTrialApp = angular.module('manageTrialApp');

    manageTrialApp.factory('derivedVariableService', ['$http','$q', function($http, $q) {

        var derivedVariableService = {};

        derivedVariableService.getDependencies = function () {
            var deferred = $q.defer();
            $http.get('/Fieldbook/DerivedVariableController/derived-variable/dependencies').success(function(data) {
                deferred.resolve(data);
            });
            return deferred.promise;
        };

        return derivedVariableService;

    }]);

})();