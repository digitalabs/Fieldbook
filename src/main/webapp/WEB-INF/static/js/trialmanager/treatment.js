/**
 * Created by cyrus on 7/1/14.
 */

/*global angular*/
/*global showBaselineTraitDetailsModal*/
(function () {
    'use strict';

    var manageTrialApp = angular.module('manageTrialApp');

    manageTrialApp.controller('TreatmentCtrl', ['$scope', 'TrialManagerDataService', '_', '$q', function ($scope, TrialManagerDataService, _, $q) {

        $scope.settings = TrialManagerDataService.settings.treatmentFactors;
        $scope.data = TrialManagerDataService.currentData.treatmentFactors;

        // watch $scope.settings, since we are sure that $scope.settings is an orderedhash even empty, we could just
        // use $watchCollection, for every added change we retrieve the 'AMOUNT ' pairs dynamically. also creat a
        // store to $scope.currentData for the variable levels.
        
        $scope.trialMeasurement = TrialManagerDataService.trialMeasurement;
        
        // initialize currentData if has no values yet
        if (!$scope.data) {
            $scope.data = TrialManagerDataService.currentData.treatmentFactors = {currentData : {}};
        }

        // map containing the treatment factor level pairs
        TrialManagerDataService.specialSettings.treatmentLevelPairs = $scope.settings.treatmentLevelPairs;

        $scope.generateTreatmentLevelPair = function (key) {
            var deferred = $q.defer();
            TrialManagerDataService.retrieveVariablePairs(key).then(function (data) {
                $scope.settings.treatmentLevelPairs[key] = new angular.OrderedHash();
                angular.forEach(data, function (val1) {
                    $scope.settings.treatmentLevelPairs[key].push(val1.variable.cvTermId, val1);
                });

                deferred.resolve();
            });

            return deferred;
        };
        
        $scope.onAddVariable = function(result) {

            angular.forEach(result, function (val, key) {
                // there's no existing treatmentLevelPair
                if (!$scope.settings.treatmentLevelPairs[key]) {
                    $scope.generateTreatmentLevelPair(key).then(function() {
                        $scope.data.currentData[key] = {
                            levels: 0,
                            labels: [],
                            variableId: 0
                        };
                    });
                } else {
                    $scope.data.currentData[key] = {
                        levels: 0,
                        labels: [],
                        variableId: 0
                    };
                }
            });

            TrialManagerDataService.indicateUnappliedChangesAvailable();
        };

        $scope.invalidBlockSizeMsg = '<b class="text-danger">Invalid Block Size</b>';
        $scope.addVariable = function () {
            return !TrialManagerDataService.trialMeasurement.hasMeasurement;
        };

        $scope.generateDropdownList = function(key) {
            if (!$scope.settings.treatmentLevelPairs[key]) {
                return [];
            } else {
                /*var arrayCopy = angular.copy($scope.settings.treatmentLevelPairs[key].vals());*/
                var options = _.filter($scope.settings.treatmentLevelPairs[key].vals(), function (entry) {
                    var found = false;
                    angular.forEach($scope.data.currentData, function (value, key2) {
                        if (key === key2) {
                            return true;
                        }

                        if (!value) {
                            return true;
                        } else if (value.variableId == entry.variable.cvTermId) {
                            found = true;
                            return false;
                        }
                    });
                    return !found;
                });

                return options;
            }
        };

        $scope.generateDropdownOption = function (key) {
            var dropdownList = $scope.generateDropdownList(key);
            var options = {
                data: function () {
                    return {
                        results : dropdownList
                    };
                },

                formatSelection: function (value) {
                    return value.variable.name;
                },

                formatResult: function (value) {
                    return value.variable.name;
                },

                minimumResultsForSearch: 20,
                id: function (value) {
                    return value.variable.cvTermId;
                }
            };

            if ($scope.data.currentData[key] && $scope.data.currentData[key].variableId) {
                options.initSelection = function(element, callback) {
                    angular.forEach(dropdownList, function(value) {
                        if (value.variable.cvTermId === $scope.data.currentData[key].variableId) {
                            callback(value);
                        }
                    });
                };
            }

            return options;
        };

        $scope.isDisableLevel = function(key) {
            return !($scope.data.currentData[key] && $scope.data.currentData[key].variableId);
        };

        $scope.performDelete = function(key) {
            // TODO : make a server side call to also remove the setting detail from the session
            $scope.settings.details.remove(key);
            delete $scope.data.currentData[key];
        };

        $scope.onLabelChange = function () {
            TrialManagerDataService.indicateUnappliedChangesAvailable();
        };

        $scope.onLevelChange = function(key,levels) {
            if (isNaN(levels)) {
                return;
            }

            levels = parseInt(levels);

            var diff = Math.abs($scope.data.currentData[key].labels.length - levels);

            // remove items if no of levels is less thant array
            if ($scope.data.currentData[key].labels.length > levels) {
                while ($scope.data.currentData[key].labels.length > levels) {
                    $scope.data.currentData[key].labels.pop();
                }
            }

            // add items if no of levels is more thant array
            else {
                for (var j = 0; j < diff; j++) {
                    $scope.data.currentData[key].labels.push(null);
                }
            }

            TrialManagerDataService.indicateUnappliedChangesAvailable();

        }; // end $scope.onLevelChange


    }]);

})();