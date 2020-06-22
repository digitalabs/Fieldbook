(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.controller('GeoJSONModalCtrl', ['$scope', 'studyContext', '$uibModalInstance',
		function ($scope, studyContext, $uibModalInstance) {
			$scope.geoJSONEditorUrl = '/ibpworkbench/controller/brapi-fieldmap?'
				+ 'instanceId=' + $scope.$resolve.instanceId
				+ '&cropName=' + studyContext.cropName;

			if ($scope.$resolve.isViewGeoJSON) {
				$scope.geoJSONEditorUrl += '&hasLayout=true';
			}

			$scope.cancel = function () {
				$uibModalInstance.dismiss();
			};
		}
	]);
})();
