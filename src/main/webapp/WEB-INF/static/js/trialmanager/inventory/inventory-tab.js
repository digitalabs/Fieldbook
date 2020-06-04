(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.controller('InventoryTabCtrl', ['$scope', 'DTOptionsBuilder', 'DTColumnBuilder', 'InventoryService', '$compile', '$timeout',
		function (
			$scope, DTOptionsBuilder, DTColumnBuilder, InventoryService, $compile, $timeout
		) {
			$scope.nested = {};
			$scope.nested.dtInstance = null;
			$scope.dtOptions = DTOptionsBuilder.newOptions()
				.withOption('ajax', {
					url: InventoryService.getSearchStudyTransactionsUrl(),
					type: 'POST',
					contentType: 'application/json',
					beforeSend: function (xhr) {
						xhr.setRequestHeader('X-Auth-Token', JSON.parse(localStorage['bms.xAuthToken']).token);
					},
					data: function (d) {
						var order = d.order && d.order[0];

						return JSON.stringify({
							draw: d.draw,
							sortedPageRequest: {
								pageSize: d.length,
								pageNumber: d.length === 0 ? 1 : d.start / d.length + 1,
								// TODO
								// sortBy: ,
								// sortOrder: order.dir
							}
							// TODO filters
						});
					}
				})
				.withDataProp('data')
				.withOption('serverSide', true)
				.withOption('processing', true)
				.withOption('lengthMenu', [[50, 75, 100], [50, 75, 100]])
				.withOption('scrollY', '500px')
				.withOption('scrollCollapse', true)
				.withOption('scrollX', '100%')
				.withOption('language', {
					processing: '<span class="throbber throbber-2x"></span>',
					lengthMenu: 'Records per page: _MENU_',
					paginate: {
						next: '>',
						previous: '<',
						first: '<<',
						last: '>>'
					}
				})
				.withDOM('<"pull-left fbk-left-padding"r>' + //
					'<"pull-right"B>' + //
					'<"clearfix">' + //
					'<"row add-top-padding-small"<"col-sm-12"t>>' + //
					'<"row"<"col-sm-12 paginate-float-center"<"pull-left"i><"pull-right"l>p>>')
				.withButtons([{
					extend: 'colvis',
					className: 'fbk-buttons-no-border fbk-colvis-button',
					text: '<i class="glyphicon glyphicon-th"></i>',
					columns: ':gt(0)'
				}])
				.withPaginationType('full_numbers');

			$scope.dtColumns = [
				{
					// checkbox
					data: function () {
						return "";
					}
				},
				{
					name: "instanceNo",
					data: function (row) {
						if (!row.observationUnits || !row.observationUnits.length) {
							return "";
						}
						return row.observationUnits[0].instanceNo
							+ (row.observationUnits.length > 1 ? " and more" : "");
					}
				},
				{data: "transactionId"},
				{
					name: "entryType",
					data: function (row) {
						if (!row.observationUnits || !row.observationUnits.length) {
							return "";
						}
						return row.observationUnits[0].entryType;
					}
				},
				{
					name: "gid",
					data: function (row) {
						return row.lot.gid;
					}
				},
				{
					name: "designation",
					data: function (row) {
						return row.lot.designation;
					}
				},
				{
					name: "entryNo",
					data: function (row) {
						if (!row.observationUnits || !row.observationUnits.length) {
							return "";
						}
						return row.observationUnits[0].entryNo;
					}
				},
				{
					name: "plotNo",
					data: function (row) {
						if (!row.observationUnits || !row.observationUnits.length) {
							return "";
						}
						return row.observationUnits[0].plotNo
							+ (row.observationUnits.length > 1 ? " and more" : "");
					}
				},
				{data: "lot.locationName"}, // TODO add loc abbr to query
				{data: "lot.stockId"},
				{data: "lot.createdDate"},
				{data: "lot.createdByUsername"},
				{data: "transactionType"},
				{data: "transactionStatus"},
				{data: "lot.unitName"},
				{data: "amount"},
				{data: "notes"}
			];
			$scope.dtColumnDefs = [
				{
					targets: 0,
					orderable: false,
					createdCell: function (td, cellData, rowData, rowIndex, colIndex) {
						$(td).append($compile('<span><input type="checkbox" ng-checked="isSelected(' + rowData.transactionId + ')" ng-click="toggleSelect(' + rowData.transactionId + ')"></span>')($scope));
						$scope.$apply();
					}
				}
			];

			$scope.totalItems = 0;
			$scope.selectedItems = {};
			$scope.isAllPagesSelected = false;

			$scope.isPageSelected = function () {
				var pageItemIds = getPageItemIds();
				return $scope.size($scope.selectedItems) > 0 && pageItemIds.every((item) => $scope.selectedItems[item]);
			};

			$scope.onSelectPage = function () {
				var pageItemIds = getPageItemIds();
				if ($scope.isPageSelected()) {
					// remove all items
					pageItemIds.forEach((item) => delete $scope.selectedItems[item]);
				} else {
					// check remaining items
					pageItemIds.forEach((item) => $scope.selectedItems[item] = true);
				}
			};

			$scope.onSelectAllPages = function () {
				$scope.isAllPagesSelected = !$scope.isAllPagesSelected;
				table().columns(0).visible(!$scope.isAllPagesSelected);
				$scope.selectedItems = {};
			};

			$scope.getRecordsFiltered = function () {
				return table().context[0].json && table().context[0].json['recordsFiltered'];
			};

			$scope.isSelected = function (itemId) {
				return $scope.selectedItems[itemId];
			};

			$scope.toggleSelect = function (itemId) {
				if ($scope.selectedItems[itemId]) {
					delete $scope.selectedItems[itemId];
				} else {
					$scope.selectedItems[itemId] = true;
				}
			};

			$scope.size = function (obj) {
				return Object.keys(obj).length;
			};

			function getPageItemIds() {
				return table().data().toArray().map((data) => {
					return data.transactionId;
				});
			}

			function table() {
				return $scope.nested.dtInstance.DataTable;
			}
		}
	]);
})();
