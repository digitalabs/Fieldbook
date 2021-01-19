(function () {
	'use strict';

	angular.module('manageTrialApp').factory('locationService', ['$http', '$q', 'studyContext', 'serviceUtilities',
		function ($http, $q, studyContext, serviceUtilities) {

			var BASE_URL = '/bmsapi/crops/' + studyContext.cropName;

			var failureHandler = serviceUtilities.restFailureHandler;

			var locationService = {};

			locationService.getLocations = function (locationTypes, favoriteLocation, name, page, size) {

				var locationSearchRequest = {
					"favourites": favoriteLocation,
					"locationName": name,
					"locationTypes": locationTypes,
					"programUUID": studyContext.programId
				};

				var request = $http.post(BASE_URL + '/locations?page=' + page + '&size=' + size, locationSearchRequest);
				return request.then(((response) => {
					return response;
				}), failureHandler);
			};

			return locationService;

		}]);

	angular.module('manageTrialApp').directive('locationsSelect', ['locationService', function (locationService) {
		return {
			restrict: 'EA',
			scope: {
				targetkey: '=',
				valuecontainer: '=',
				onLocationSelect: ' &'
			},
			template: '<ui-select ng-model="valuecontainer[targetkey]"\n' +
				'   on-select="onLocationSelect()"\n' +
				'   tagging-label="false"\n' +
				'   append-to-body="true"\n' +
				'   theme="select2">' +
				'<ui-select-match\n' +
				'class="ui-select-match">\n' +
				'<span ng-bind="$select.selected.name"></span>\n' +
				'</ui-select-match>\n' +
				'     <ui-select-choices refresh="fetch($select)" refresh-delay="300" repeat="locationItem.id as locationItem in locationItems | filter: $select.search track by $index">\n' +
				'          <div title="{{::locationItem.name}} - ({{::locationItem.abbreviation}})">{{::locationItem.name}} - ({{::locationItem.abbreviation}})</div>\n' +
				'          <button class="btn btn-xs btn-default" style="width: 100%; margin-top:5px;" ng-if="$index == $select.items.length-1 && loadMore" title="{{locationItem.name}}" ng-click="fetch($select, $event);">Load more...\n' +
				'          </button>\n' +
				'     </ui-select-choices>\n' +
				'</ui-select>\n' +
				'<div class="possibleValuesDiv">\n' +
				'     <input type="radio" name="location-lookup" ng-model="localData.locationLookup" value="1" ng-click="fetch()">\n' +
				'     <span th:text="#{show.breeding.location}">Breeding locations</span> &nbsp;\n' +
				'     <input type="radio" name="location-lookup" ng-model="localData.locationLookup" value="2" ng-click="fetch()">\n' +
				'     <span th:text="#{show.all.location}">All locations types</span> &nbsp;\n' +
				'     </div>\n' +
				'<div class="possibleValuesDiv">\n' +
				'     <input type="checkbox" name="location-favorite" ng-model="localData.useFavorites" ng-click="fetch()">\n' +
				'     <span th:text="#{show.favorite.location}">Show only favorite locations </span>\n' +
				'</div>',
			link: function (scope, element, attrs, paginationCtrl) {
			},
			controller: ['$scope', 'locationService', function ($scope, locationService) {
				var BREEDING_LOCATION = [410, 411];

				$scope.locationItems = [];
				$scope.locationPage = 0;
				$scope.loadMore = true;
				$scope.localData = {locationLookup: 1, useFavorites: false};

				$scope.fetch = function ($select, $event) {
					// no event means first load!
					if (!$event) {
						$scope.locationPage = 0;
						$scope.locationItems = [];
					} else {
						$event.stopPropagation();
						$event.preventDefault();
						$scope.locationPage++;
					}

					locationService.getLocations($scope.localData.locationLookup == 1 ? BREEDING_LOCATION : [], $scope.localData.useFavorites, $select ? $select.search : '', $scope.locationPage, 500).then(function (response) {
						$scope.locationItems = $scope.locationItems.concat(response.data);
						$scope.loadMore = ($scope.locationPage + 1) * 500 < response.headers()['x-filtered-count'];
					});
				}
			}]
		};
	}]);
})();