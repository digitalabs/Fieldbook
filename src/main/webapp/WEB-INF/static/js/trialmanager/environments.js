/**
 * Created by cyrus on 7/2/14.
 */

/*global angular*/
/*global mdLabel*/
/*global mdPlaceholder*/

(function(){
    'use strict';

    angular.module('manageTrialApp').controller('EnvironmentCtrl', ['$scope', function ($scope) {
        $scope.data = {};

        // storage of number of environments uses a temp variable to account for user temporarily deleting contents of field to enter new value
        $scope.temp = {
            noOfEnvironments : 0
        };

        $scope.data.noOfEnvironments = 0;
        $scope.data.environments = [];
        $scope.data.managementDetails = {};
        $scope.data.trialConditionDetails = {};

        $scope.labels = {};

        $scope.labels.managementDetails = {
            label : mdLabel,
            placeholderLabel : mdPlaceholder
        };

        $scope.labels.trialConditionDetails = {
            label: mdLabel,
            placeholderLabel: mdPlaceholder
        };

        $scope.$watch('temp.noOfEnvironments', function(newVal, oldVal) {
            if (newVal) {
                $scope.data.noOfEnvironments = newVal;
            }
        });

        $scope.$watch('data.noOfEnvironments', function(newVal, oldVal) {

            if (newVal < oldVal) {
                // if new environment count is less than previous value, splice array
                while ($scope.data.environments.length > newVal) {
                    $scope.data.environments.pop();
                }
            } else if (oldVal < newVal) {
                // if new environment count is greater than old value, add new element to environments array
                while ($scope.data.environments.length < newVal) {
                    $scope.data.environments.push({
                        managementDetailValues: $scope.constructDataStructureFromDetails($scope.data.managementDetails),
                        trialDetailValues: $scope.constructDataStructureFromDetails($scope.data.trialConditionDetails)
                    });
                }
            }   // else do nothing equal
        });

        $scope.$watch('data.managementDetails', function(newVal, oldVal) {
            $scope.updateEnvironmentVariables('managementDetails', Object.keys(newVal).length > Object.keys(oldVal).length);
        }, true);

        $scope.$watch('data.trialConditionDetails', function (newVal, oldVal) {
            $scope.updateEnvironmentVariables('trialConditionDetails', Object.keys(newVal).length > Object.keys(oldVal).length);
        }, true);

        $scope.updateEnvironmentVariables = function(type, entriesIncreased) {

            var settingDetailSource = null;
            var targetKey = null;

            if (type === 'managementDetails') {
                settingDetailSource = $scope.data.managementDetails;
                targetKey = 'managementDetailValues';
            } else if (type === 'trialConditionDetails'){
                settingDetailSource = $scope.data.trialConditionDetails;
                targetKey = 'trialDetailValues';
            }

            $.each($scope.data.environments, function(key, value) {
                var subList = value[targetKey];

                if (entriesIncreased) {
                    $.each(settingDetailSource, function (key, value) {
                        if (subList[key] === undefined) {
                            subList[key] = null;
                        }
                    });
                } else {
                    $.each(subList, function (key, value) {
                        if (settingDetailSource[key] === undefined) {
                            delete subList[key];
                        }
                    });
                }
            });
        };

        $scope.constructDataStructureFromDetails = function(details) {
            var returnVal = {};
            $.each(details, function(key, value) {
                returnVal[key] = null;
            });

            return returnVal;
        };
    }]);
})();