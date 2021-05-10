(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	angular.module('manageTrialApp').factory('fileService', ['$http', '$q', 'studyContext', 'serviceUtilities', '$uibModal',
		function ($http, $q, studyContext, serviceUtilities, $uibModal) {

			var BASE_URL = '/bmsapi/files/';

			var successHandler = serviceUtilities.restSuccessHandler,
				failureHandler = serviceUtilities.restFailureHandler;

			var fileService = {};

			fileService.upload = function (file) {
				var request = $http({
					method: 'POST',
					url: BASE_URL,
					headers: {
						'Content-Type': undefined
					},
					data: {
						file: file
					},
					transformRequest: function (data, headersGetter) {
						var formData = new FormData();
						angular.forEach(data, function (value, key) {
							formData.append(key, value);
						});

						var headers = headersGetter();
						return formData;
					}
				});
				return request.then(successHandler, failureHandler);
			};

			fileService.showFile = function (fileName) {
				$uibModal.open({
					template: '<iframe ng-src="{{url}}"' +
						' style="width:100%; height: 560px; border: 0" />',
					size: 'lg',
					controller: function ($scope, $uibModalInstance) {
						$scope.url = '/ibpworkbench/controller/jhipster#file-manager/' + fileName;
					},
				});
			};

			return fileService;
		}]);

}());
