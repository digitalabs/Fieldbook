(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.controller('InventoryTabCtrl', ['$scope', 'DTOptionsBuilder', 'DTColumnBuilder', 'InventoryService', '$compile', '$timeout',
		'$uibModal',
		function (
			$scope, DTOptionsBuilder, DTColumnBuilder, InventoryService, $compile, $timeout, $uibModal,
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
								sortBy: $scope.dtColumns[order.column].name,
								sortOrder: order.dir
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
				.withOption('order', [2, 'asc']) // transactionId
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
						return row.observationUnits[0].instanceNo
							+ (row.observationUnits.length > 1 ? " and more" : "");
					}
				},
				{name: "transactionId", data: "transactionId"},
				{
					name: "entryType",
					data: function (row) {
						return row.observationUnits[0].entryType;
					}
				},
				{
					name: "lotGid",
					data: function (row) {
						return row.lot.gid;
					}
				},
				{
					name: "lotDesignation",
					data: function (row) {
						return row.lot.designation;
					}
				},
				{
					name: "entryNo",
					data: function (row) {
						return row.observationUnits[0].entryNo;
					}
				},
				{
					name: "plotNo",
					data: function (row) {
						return row.observationUnits[0].plotNo
							+ (row.observationUnits.length > 1 ? " and more" : "");
					}
				},
				{name: "lotLocationAbbr", data: "lot.locationAbbr"},
				{name: "lotStockId", data: "lot.stockId"},
				{name: "createdDate", data: "createdDate"},
				{name: "createdByUsername", data: "createdByUsername"},
				{name: "transactionType", data: "transactionType"},
				{name: "transactionStatus", data: "transactionStatus"},
				{name: "lotUnitName", data: "lot.unitName"},
				{name: "amount", data: "amount"},
				{name: "notes", data: "notes"}
			];

			$scope.dtColumnDefs = [
				{
					targets: 0,
					createdCell: function (td, cellData, rowData, rowIndex, colIndex) {
						$(td).append($compile('<span><input type="checkbox" ng-checked="isSelected(' + rowData.transactionId + ')" ng-click="toggleSelect(' + rowData.transactionId + ')"></span>')($scope));
						$scope.$apply();
					}
				},
				{
					targets: "info-modal-column",
					createdCell: function (td, cellData, rowData, rowIndex, colIndex) {
						$(td).html($compile('<a href ng-click="openInfoModal(' + rowIndex + ')">' + cellData + '</a>')($scope));
						$scope.$apply();
					}
				},
				{
					targets: "_all",
					orderable: false
				}
			];

			$scope.openInfoModal = function (rowIndex) {
				$uibModal.open({
					templateUrl: 'inventory-tab-info-modal.html',
					size: 'lg',
					controller: function ($scope, $uibModalInstance) {
						const rowData = table().row(rowIndex).data();
						$scope.transactionId = rowData.transactionId;
						$scope.stockId = rowData.lot.stockId;

						$scope.observationUnits = rowData.observationUnits;
						$scope.dtOptions = DTOptionsBuilder.newOptions()
							.withOption("paging", $scope.observationUnits.length > 10)
							.withOption('scrollX', '100%')
							.withOption('language', {
								lengthMenu: 'Records per page: _MENU_',
								paginate: {
									next: '>',
									previous: '<',
									first: '<<',
									last: '>>'
								}
							})
							.withPaginationType('full_numbers')
							.withDOM('<"row"<"col-sm-12"t>>' + //
								'<"row"<"col-sm-12 paginate-float-center"<"pull-left"i><"pull-right"l>p>>');

						$scope.cancel = function () {
							$uibModalInstance.dismiss();
						};
					}
				});
			};

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
