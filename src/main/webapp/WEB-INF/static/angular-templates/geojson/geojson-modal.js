(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.controller('GeoJSONModalCtrl', ['$scope', 'studyContext', '$uibModalInstance', '$http',
		function ($scope, studyContext, $uibModalInstance, $http) {
			$scope.geoJSONEditorUrl = '/ibpworkbench/controller/brapi-fieldmap?'
				+ 'instanceId=' + $scope.$resolve.instanceId
				+ '&cropName=' + studyContext.cropName;

			$http({
				method: 'GET',
				url: '/ibpworkbench/controller/help/getUrl/' + $scope.$resolve.helpModule,
				responseType: 'text',
				transformResponse: undefined
			}).then(function(response){
				$scope.helpToolUrl = response.data;
			}, function (error){
			});


			if ($scope.$resolve.isViewGeoJSON) {
				$scope.geoJSONEditorUrl += '&hasLayout=true';
			}

			$scope.cancel = function () {
				$uibModalInstance.dismiss();
			};
		}
	]);
})();
