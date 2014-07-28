/**
 * Created by cyrus on 7/2/14.
 */

/* global angular */
(function(){
    'use strict';

    angular.module('manageTrialApp').controller('MeasurementsCtrl',
        ['$scope', 'TrialManagerDataService', function($scope, TrialManagerDataService) {

            $scope.settings = TrialManagerDataService.settings.measurements;

            $scope.updateSettings = function(newValue) {
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

            $scope.isHideDelete = false;
            
            $scope.updateOccurred = false;
            
            $scope.addVariable = true;
            
            $scope.$on('deleteOccurred', function() {
                $scope.updateOccurred = true;
                $scope.reloadMeasurementPage();
            });

            $scope.$on('variableAdded', function() {
                $scope.updateOccurred = true;
                $scope.reloadMeasurementPage();
            });
            
            $scope.reloadMeasurementPage = function(){
            	
            	if($('#measurement-table').length !== 0){
            		//we reload            	
            		$.ajax({
            	        url: '/Fieldbook/TrialManager/openTrial/load/dynamic/change/measurement',
            	        type: 'POST',
            	        data: 'traitsList='+TrialManagerDataService.settings.measurements.m_keys,
            	        cache: false,
            	        success: function (html) {
            	            $('#measurementsDiv').html(html);
            	            $('body').data('needToSave', '1');
            	        }
            	    });	
            	}
            };
        }]);
})();