/**
 * Created by cyrus on 7/2/14.
 */

/* global angular */
(function () {
    'use strict';

    angular.module('manageTrialApp').controller('MeasurementsCtrl',
        ['$scope', 'TrialManagerDataService', '$modal', '$q','debounce','$timeout',
            function ($scope, TrialManagerDataService, $modal, $q,debounce,$timeout) {

                $scope.settings = TrialManagerDataService.settings.measurements;

                $scope.updateSettings = function (newValue) {
                    angular.copy(newValue, $scope.settings);
                };

                TrialManagerDataService.registerSetting('measurementsData', $scope.updateSettings);

                $scope.$watch(function () {
                    return TrialManagerDataService.settings.measurements;
                }, function (newValue) {
                    if ($scope.settings !== newValue) {
                        angular.copy(newValue, $scope.settings);
                    }
                });

                $scope.predeleteFunction = function (variableType, key) {
                    var hasData = false;
                    var deferred = $q.defer();
                    // do AJAX stuff here
                    $.ajax({
                        url: "/Fieldbook/manageSettings/checkMeasurementData/" + variableType + "/" + key,
                        cache: false,
                        type: "GET",
                        async: false,
                        success: function (data) {
                            hasData = data.hasMeasurementData;
                        }
                    });

                    // if still needed to ask for confirmation, return value of modal popup
                    // else return false
                    var modalInstance = null;
                    if (hasData === '1') {
                        modalInstance = $modal.open({
                            templateUrl: '/Fieldbook/static/angular-templates/confirmModal.html',
                            controller: 'ConfirmModalController',
                            resolve: {
                                MODAL_TITLE: function () {
                                    return modalConfirmationTitle;
                                },
                                MODAL_TEXT: function () {
                                    return measurementModalConfirmationText;
                                },
                                CONFIRM_BUTTON_LABEL: function () {
                                    return environmentConfirmLabel;
                                }
                            }
                        });
                        modalInstance.result.then(function (shouldContinue) {
                            deferred.resolve(shouldContinue);
                        });
                    } else {
                        deferred.resolve(true);
                    }

                    return deferred.promise;
                };

                $scope.isHideDelete = false;

                $scope.updateOccurred = false;

                $scope.addVariable = true;

                $scope.$on('deleteOccurred', function () {
                    $scope.updateOccurred = true;

                    $scope.reloadOnDebounce();

                    TrialManagerDataService.applicationData.unsavedTraitsAvailable = true;
                });

                $scope.$on('variableAdded', function () {
                    $scope.updateOccurred = true;

                    $scope.reloadOnDebounce();

                    TrialManagerDataService.applicationData.unsavedTraitsAvailable = true;
                });

                $scope.reloadMeasurementPage = function () {

                    if ($('#measurement-table').length !== 0) {
                        //we reload
                        $.ajax({
                            url: '/Fieldbook/TrialManager/openTrial/load/dynamic/change/measurement',
                            type: 'POST',
                            data: 'traitsList=' + TrialManagerDataService.settings.measurements.m_keys,
                            cache: false,
                            success: function (html) {
                                $('#measurementsDiv').html(html);
                                $('body').data('needToSave', '1');
                            }
                        });
                    }
                };

                var DELAY = 1000; // 1 secs
                $scope.reloadOnDebounce = debounce($scope.reloadMeasurementPage,DELAY,false);

            }]);
})();