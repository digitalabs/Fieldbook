/**
 * Created by cyrus on 7/2/14.
 */

/*global angular, modalConfirmationTitle,
environmentModalConfirmationText,environmentConfirmLabel*/

(function () {
    'use strict';

    angular.module('manageTrialApp').controller('EnvironmentCtrl', ['$scope', 'TrialManagerDataService', '$modal',
        function ($scope, TrialManagerDataService, $modal) {

            $scope.data = {};

            $scope.data = TrialManagerDataService.currentData.environments;
            $scope.isHideDelete = false;
            $scope.settings = TrialManagerDataService.settings.environments;
            if (Object.keys($scope.settings).length === 0) {
                $scope.settings = {};
                $scope.settings.managementDetails = [];
                $scope.settings.trialConditionDetails = [];
            }

            if ($scope.data.noOfEnvironments > 0 && $scope.data.environments.length === 0) {
                while ($scope.data.environments.length !== $scope.data.noOfEnvironments) {
                    $scope.data.environments.push({
                        managementDetailValues: $scope.constructDataStructureFromDetails($scope.settings.managementDetails),
                        trialDetailValues: $scope.constructDataStructureFromDetails($scope.settings.trialConditionDetails)
                    });
                }
            }

            TrialManagerDataService.registerData('environments', function(newValue) {
                angular.copy(newValue, $scope.data);
                $scope.temp.noOfEnvironments = $scope.data.noOfEnvironments;
            });


            TrialManagerDataService.registerSetting('environments', function (newValue) {
                angular.copy(newValue, $scope.settings);
            });

            $scope.temp = {
                settingMap : {},
                noOfEnvironments : $scope.data.noOfEnvironments
            };

            $scope.shouldDisableEnvironmentCountUpdate = function() {
                return TrialManagerDataService.trialMeasurement.hasMeasurement;
            };

            $scope.updateEnvironmentCount = function() {
                if ($scope.temp.noOfEnvironments > $scope.data.environments.length) {
                    $scope.data.noOfEnvironments = $scope.temp.noOfEnvironments;
                } else if ($scope.temp.noOfEnvironments < $scope.data.environments.length) {
                    var modalInstance = $modal.open({
                        templateUrl: '/Fieldbook/static/angular-templates/confirmModal.html',
                        controller: 'ConfirmModalController',
                        resolve: {
                            MODAL_TITLE : function() {
                                return modalConfirmationTitle;
                            },
                            MODAL_TEXT : function() {
                                return environmentModalConfirmationText;
                            },
                            CONFIRM_BUTTON_LABEL : function() {
                                return environmentConfirmLabel;
                            }
                        }
                    });

                    modalInstance.result.then(function(shouldContinue) {
                        if (shouldContinue) {
                            $scope.data.noOfEnvironments = $scope.temp.noOfEnvironments;
                        }
                    });
                }

            };

            $scope.addVariable = true;
            
            $scope.findSetting = function(targetKey, type) {
                if (! $scope.temp.settingMap[targetKey]) {
                    var targetSettingList = null;

                    if (type === 'managementDetails') {
                        targetSettingList = $scope.settings.managementDetails;
                    } else if (type === 'trialConditionDetails') {
                        targetSettingList = $scope.settings.trialConditionDetails;
                    }

                    $.each(targetSettingList, function(key, value) {
                        if (value.variable.cvTermId == targetKey) {
                            $scope.temp.settingMap[targetKey] = value;
                            return false;
                        }
                    });
                }

                return $scope.temp.settingMap[targetKey];
            };

            $scope.$watch('data.noOfEnvironments', function (newVal, oldVal) {

                if (newVal < oldVal) {
                    // if new environment count is less than previous value, splice array
                    while ($scope.data.environments.length > newVal) {
                        $scope.data.environments.pop();
                    }
                    TrialManagerDataService.indicateUnappliedChangesAvailable();
                } else if (oldVal < newVal) {
                    // if new environment count is greater than old value, add new element to environments array
                    while ($scope.data.environments.length < newVal) {
                        $scope.data.environments.push({
                            managementDetailValues: $scope.constructDataStructureFromDetails($scope.settings.managementDetails),
                            trialDetailValues: $scope.constructDataStructureFromDetails($scope.settings.trialConditionDetails)
                        });
                    }
                    TrialManagerDataService.indicateUnappliedChangesAvailable();
                }   // else do nothing equal
            });

            $scope.$watch('settings.managementDetails', function (newVal, oldVal) {
                $scope.updateEnvironmentVariables('managementDetails', newVal.length > oldVal.length);
            }, true);

            $scope.$watch('settings.trialConditionDetails', function (newVal, oldVal) {
                $scope.updateEnvironmentVariables('trialConditionDetails', newVal.length > oldVal.length);
            }, true);

            $scope.updateEnvironmentVariables = function (type, entriesIncreased) {

                var settingDetailSource = null;
                var targetKey = null;

                if (type === 'managementDetails') {
                    settingDetailSource = $scope.settings.managementDetails;
                    targetKey = 'managementDetailValues';
                } else if (type === 'trialConditionDetails') {
                    settingDetailSource = $scope.settings.trialConditionDetails;
                    targetKey = 'trialDetailValues';
                }

                $.each($scope.data.environments, function (key, value) {
                    var subList = value[targetKey];

                    if (entriesIncreased) {
                        $.each(settingDetailSource.keys(), function (key, value) {
                            if (subList[value] === undefined) {
                                subList[value] = null;
                            }
                        });
                    } else {
                        $.each(subList, function (idKey) {
                            if (!settingDetailSource.vals().hasOwnProperty(idKey)) {
                                delete subList[idKey];
                            }
                        });
                    }
                });
            };

            $scope.constructDataStructureFromDetails = function (details) {
                var returnVal = {};
                $.each(details.vals(), function (key, value) {
                    returnVal[value.variable.cvTermId] = null;
                });

                return returnVal;
            };
        }]);
})();
