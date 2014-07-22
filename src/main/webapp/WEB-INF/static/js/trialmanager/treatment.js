/**
 * Created by cyrus on 7/1/14.
 */

/*global angular*/
/*global showBaselineTraitDetailsModal*/
(function () {
    'use strict';

    var manageTrialApp = angular.module('manageTrialApp');

    manageTrialApp.controller('TreatmentCtrl',['$scope','TrialManagerDataService','_',function($scope,TrialManagerDataService,_) {

        $scope.settings = TrialManagerDataService.settings.treatmentFactors;

        if ($scope.settings && $scope.settings.keys() > 0) {
            angular.forEach($scope.settings.keys(), function(value) {
                $scope.generateTreatmentLevelPair(value);
            });
        }

        $scope.data = TrialManagerDataService.currentData.treatmentFactors;

        // watch $scope.settings, since we are sure that $scope.settings is an orderedhash even empty, we could just
        // use $watchCollection, for every added change we retrieve the 'AMOUNT ' pairs dynamically. also creat a
        // store to $scope.currentData for the variable levels.

        $scope.trialMeasurement = TrialManagerDataService.trialMeasurement;

        // map containing the treatment factor level pairs
        $scope.treatmentLevelPairs = TrialManagerDataService.specialSettings.treatmentLevelPairs;

        $scope.onAddVariable = function (result) {

            angular.forEach(result, function (val, key) {
                // there's no existing treatmentLevelPair
                if (!$scope.treatmentLevelPairs[key]) {
                    TrialManagerDataService.retrieveVariablePairs(key).then(function (data) {
                        $scope.treatmentLevelPairs[key] = new angular.OrderedHash();

                        angular.forEach(data, function (val1) {
                            $scope.treatmentLevelPairs[key].push(val1.variable.cvTermId, val1);
                        });

                        $scope.currentData[key] = {
                            levels: 0,
                            labels: [],
                            variableId: 0
                        };
                    });
                } else {
                    $scope.currentData[key] = {
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

        $scope.generateDropdownOption = function (key) {
            var options = {
                data: function () {
                    if (!$scope.treatmentLevelPairs[key]) {
                        return {
                            results: []
                        };
                    } else {
                        var options = _.filter($scope.treatmentLevelPairs[key].vals(), function (entry) {
                            var found = false;
                            angular.forEach($scope.currentData, function (value, key2) {
                                if (key === key2) {
                                    return true;
                                }

                                if (! value) {
                                    return true;
                                } else if (value.variableId == entry.variable.cvTermId) {
                                    found = true;
                                    return false;
                                }
                            });
                            return !found;
                        });

                        return {
                            results: options
                        };
                    }


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

            return options;
        };

        $scope.isDisableLevel = function(key) {
            return !($scope.currentData[key] && $scope.currentData[key].variableId);
        }



        $scope.deleteTreatmentFactor = function(key) {
            delete $scope.currentData[key];
            $scope.settings.remove(Number(key));
        };

        $scope.onLabelChange = function () {
            TrialManagerDataService.indicateUnappliedChangesAvailable();
        };



        $scope.onLevelChange = function (key, levels) {


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