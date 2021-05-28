(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	angular.module('manageTrialApp').factory('fileService', ['$http', '$q', 'studyContext', 'serviceUtilities', '$uibModal',
		'fileDownloadHelper',
		function ($http, $q, studyContext, serviceUtilities, $uibModal, fileDownloadHelper) {

			var BASE_URL = '/bmsapi/files/';

			var successHandler = serviceUtilities.restSuccessHandler,
				failureHandler = serviceUtilities.restFailureHandler;

			var fileService = {};

			fileService.upload = function (file, key) {
				var request = $http({
					method: 'POST',
					url: BASE_URL,
					headers: {
						'Content-Type': undefined
					},
					data: {
						file: file,
						key: key
					},
					transformRequest: function (data, headersGetter) {
						var formData = new FormData();
						angular.forEach(data, function (value, key) {
							formData.append(key, value);
						});

						return formData;
					}
				});
				return request.then(successHandler, failureHandler);
			};

			fileService.showFile = function (fileKey, fileName) {
				if (!fileName.match(/\.(gif|jpe?g|tiff?|png|webp|bmp)$/i)) {
					$http.get('/bmsapi/files/' + fileKey, {responseType: 'blob'}).then((response) => {
						fileDownloadHelper.save(response.data, fileName);
					}, (response) => {
						showErrorMessage('', "Something went wrong (possibly file storage configuration not available)");
					});
					return;
				}
				$uibModal.open({
					template: '<iframe ng-src="{{url}}"' +
						' style="width:100%; height: 560px; border: 0" />',
					size: 'lg',
					controller: function ($scope, $uibModalInstance) {
						$scope.url = '/ibpworkbench/controller/jhipster#file-manager/' + encodeURIComponent(fileKey) + '?fileName=' + fileName;

						window.closeModal = function() {
							$uibModalInstance.close();
						}
					},
				});
			};

			return fileService;
		}]);

}());
