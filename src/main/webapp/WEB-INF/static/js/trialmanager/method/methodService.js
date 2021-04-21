(function () {
	'use strict';

	angular.module('manageTrialApp').factory('methodService', ['$http', '$q', 'studyContext', 'serviceUtilities',
		function ($http, $q, studyContext, serviceUtilities) {

			var BASE_URL = '/bmsapi/crops/' + studyContext.cropName;

			var failureHandler = serviceUtilities.restFailureHandler;

			var methodService = {};

			methodService.getMethods = function (methodTypes, favoritesOnly, methodCodes) {
				var methodCodesURL = methodCodes ? '&methodCodes=' + methodCodes : '';
				var methodTypesURL = methodTypes ? '&methodTypes=' + methodTypes : '';

				var request = $http.get(BASE_URL + '/breedingmethods?programUUID=' + studyContext.programId + '&favoritesOnly=' + favoritesOnly +
					methodCodesURL + methodTypesURL);
				return request.then(((response) => {
					// Concatenate name and code to form displayDescription
					angular.forEach(response.data, function (method) {
						method.displayDescription = method.name + ' - ' + method.code;
					});
					// Add a "Please Choose" option for resetting value to empty
					response.data.unshift({"code": "", "name": "", "displayDescription": "Please Choose"});
					return response;
				}), failureHandler);
			};

			return methodService;

		}]);

	angular.module('manageTrialApp').directive('methodsSelect', ['methodService', function (methodService) {
		return {
			restrict: 'EA',
			scope: {
				targetkey: '=',
				valuecontainer: '=',
				onMethodSelect: ' &',
				hideTypes: '=',
				hideFavorites: '=',
				showDerManMethodsRadio: '=',
				showGenMethodsRadio: '=',
				disableMethodTypesRadio: '=',
				enableDropdown: '=',
				methodType: '='
			},
			templateUrl: '/Fieldbook/static/angular-templates/method/methodSelect.html',
			link: function (scope, element, attrs, paginationCtrl) {
			},
			controller: ['$scope', 'methodService', '$rootScope', function ($scope, methodService, $rootScope) {
				var DERIVATIVE_MAINTENANCE = ['DER', 'MAN'];
				var GENERATIVE = ['GEN'];

				const DER_MAN_ONLY = 1;
				const GENERATIVE_ONLY = 2;
				const ALL_METHODS = 3;

				$scope.methodItems = [];
				$scope.localData = {methodType: ALL_METHODS, useFavorites: false};
				if ($scope.methodType) {
					$scope.localData.methodType = $scope.methodType;
				}
				$scope.fetch = function ($select, $event) {
					// no event means first load!
					if (!$event) {
						$scope.methodItems = [];
					} else {
						$event.stopPropagation();
						$event.preventDefault();
					}

					var methodTypes = [];
					if ($scope.localData.methodType === DER_MAN_ONLY) {
						methodTypes = DERIVATIVE_MAINTENANCE;
					} else if ($scope.localData.methodType === GENERATIVE_ONLY) {
						methodTypes = GENERATIVE;
					}

					methodService.getMethods(methodTypes, $scope.localData.useFavorites, null).then(function (response) {
						$scope.methodItems = $scope.methodItems.concat(response.data);
					});
				};

				$scope.onSelect = function (item, model) {
					$scope.onMethodSelect()(item, model);
				}

				$rootScope.$on('enableDisableMethodsSelect', function (event, enableDropdown) {
					$scope.enableDropdown = enableDropdown;
				});
			}]
		};
	}]);
})();
