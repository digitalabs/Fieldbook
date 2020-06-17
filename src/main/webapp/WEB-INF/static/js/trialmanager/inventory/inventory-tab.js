(function () {
	'use strict';

	const AND_MORE_LABEL = " and more";

	const module = angular.module('manageTrialApp');

	module.controller('InventoryTabCtrl', ['$scope', '$q', 'DTOptionsBuilder', 'DTColumnBuilder', 'InventoryService', '$compile', '$timeout',
		'$uibModal', 'studyInstanceService',
		function (
			$scope, $q, DTOptionsBuilder, DTColumnBuilder, InventoryService, $compile, $timeout, $uibModal, studyInstanceService,
		) {
			$scope.nested = {};
			$scope.nested.dtInstance = {};

			const dtOptionsDeferred = $q.defer();
			$scope.dtOptions = dtOptionsDeferred.promise;
			const dtOptions = DTOptionsBuilder.newOptions()
				.withOption('ajax', {
					url: InventoryService.getSearchStudyTransactionsUrl(),
					type: 'POST',
					contentType: 'application/json',
					beforeSend: function (xhr) {
						xhr.setRequestHeader('X-Auth-Token', JSON.parse(localStorage['bms.xAuthToken']).token);
					},
					data: function (d) {
						var order = d.order && d.order[0];

						return JSON.stringify(addFilters({
							draw: d.draw,
							sortedPageRequest: {
								pageSize: d.length,
								pageNumber: d.length === 0 ? 1 : d.start / d.length + 1,
								sortBy: $scope.dtColumns[order.column].name,
								sortOrder: order.dir
							}
						}));
					}
				})
				.withDataProp('data')
				.withOption('serverSide', true)
				.withOption('processing', true)
				.withOption('lengthMenu', [[50, 75, 100], [50, 75, 100]])
				.withOption('scrollY', '500px')
				.withOption('scrollCollapse', true)
				.withOption('scrollX', '100%')
				.withOption('order', [[2, 'asc']]) // transactionId
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

			function addFilters(request) {
				request.transactionsSearch = {};
				Object.entries($scope.columns).forEach(([name, column]) => {
					if (column.filter && column.filter.transform) {
						column.filter.isFiltered = false;
						column.filter.transform(request);
					}
				});
				const instanceNumber = $scope.nested.selectedEnvironment && $scope.nested.selectedEnvironment.instanceNumber;
				if (instanceNumber) {
					request.instanceNoList = [instanceNumber];
					$scope.columns.instanceNo.filter.isFiltered = true;
				}

				return request;
			}

			$scope.columns = {
				checkbox: {
					data: function () {
						return "";
					}
				},
				instanceNo: {
					data: function (row) {
						return row.observationUnits[0].instanceNo
							+ (row.observationUnits
								.map(ou => ou.instanceNo)
								.filter((item, pos, self) => {
									// remove dups
									return self.indexOf(item) === pos;
								})
								.length > 1 ? AND_MORE_LABEL : "");
					},
					visible: false,
					filter: {
						transform(request) {
							if (this.value) {
								request.instanceNoList = this.value.split(',');
								this.isFiltered = true;
							}
						}
					}
				},
				transactionId: {
					data: "transactionId",
					filter: {
						transform(request) {
							if (this.value) {
								request.transactionsSearch.transactionIds = this.value.split(',');
								this.isFiltered = true;
							}
						}
					}
				},
				entryType: {
					data: function (row) {
						return row.observationUnits[0].entryType;
					},
					filter: {
						transform(request) {
							if (this.value) {
								request.entryType = this.value;
								this.isFiltered = true;
							}
						}
					}
				},
				lotGid: {
					data: function (row) {
						return row.lot.gid;
					},
					filter: {
						transform(request) {
							if (this.value) {
								request.transactionsSearch.gids = this.value.split(',');
								this.isFiltered = true;
							}
						}
					}
				},
				lotDesignation: {
					data: function (row) {
						return row.lot.designation;
					},
					filter: {
						transform(request) {
							if (this.value) {
								request.transactionsSearch.designation = this.value;
								this.isFiltered = true;
							}
						}
					}
				},
				entryNo: {
					data: function (row) {
						return row.observationUnits[0].entryNo;
					},
					filter: {
						transform(request) {
							if (this.value) {
								request.entryNoList = this.value.split(',');
								this.isFiltered = true;
							}
						}
					}
				},
				plotNo: {
					data: function (row) {
						return row.observationUnits[0].plotNo
							+ (row.observationUnits.length > 1 ? AND_MORE_LABEL : "");
					},
					filter: {
						transform(request) {
							if (this.value) {
								request.plotNoList = this.value.split(',');
								this.isFiltered = true;
							}
						}
					}
				},
				lotLocationAbbr: {
					data: "lot.locationAbbr",
					filter: {
						transform(request) {
							if (this.value) {
								request.transactionsSearch.lotLocationAbbr = this.value;
								this.isFiltered = true;
							}
						}
					}
				},
				lotStockId: {
					data: "lot.stockId",
					filter: {
						placeholder: 'starts with',
						transform(request) {
							if (this.value) {
								request.transactionsSearch.stockId = this.value;
								this.isFiltered = true;
							}
						}
					}
				},
				createdDate: {
					data: "createdDate",
					filter: {
						transform(request) {
							if (this.from) {
								request.transactionsSearch.createdDateFrom = $.datepicker.formatDate("yy-mm-dd", this.from);
								this.isFiltered = true;
							}
							if (this.to) {
								request.transactionsSearch.createdDateTo = $.datepicker.formatDate("yy-mm-dd", this.to);
								this.isFiltered = true;
							}
						},
						reset() {
							this.from = null;
							this.to = null;
						}
					}
				},
				createdByUsername: {
					data: "createdByUsername",
					filter: {
						transform(request) {
							if (this.value) {
								request.transactionsSearch.createdByUsername = this.value;
								this.isFiltered = true;
							}
						}
					}
				},
				transactionType: {
					data: "transactionType",
					filter: {
						options: [],
						transform(request) {
							const selectedOptions = this.options.filter((option) => option.checked);
							if (selectedOptions.length) {
								this.value = selectedOptions.map(option => option.id);
								request.transactionsSearch.transactionTypes = this.value;
								this.isFiltered = true;
							}
						},
						reset() {
							this.options.forEach(option => option.checked = false);
						}
					}
				},
				transactionStatus: {
					data: "transactionStatus",
					filter: {
						options: [],
						transform(request) {
							const selectedOptions = this.options.filter((option) => option.checked);
							if (selectedOptions.length) {
								this.value = selectedOptions.map(option => option.id);
								request.transactionsSearch.transactionStatus = this.value;
								this.isFiltered = true;
							}
						},
						reset() {
							this.options.forEach(option => option.checked = false);
						}
					}
				},
				lotUnitName: {
					data: "lot.unitName",
					filter: {
						options: [],
						transform(request) {
							const selectedOptions = this.options.filter((option) => option.checked);
							if (selectedOptions.length) {
								this.value = selectedOptions.map(option => option.id);
								request.transactionsSearch.unitIds = this.value;
								this.isFiltered = true;
							}
						},
						reset() {
							this.options.forEach(option => option.checked = false);
						}
					}
				},
				amount: {
					data: "amount",
					filter: {
						transform(request) {
							if (this.min) {
								request.transactionsSearch.minAmount = this.min;
								this.isFiltered = true;
							}
							if (this.max) {
								request.transactionsSearch.maxAmount = this.max;
								this.isFiltered = true;
							}
						},
						reset() {
							this.min = null;
							this.max = null;
						}
					}
				},
				notes: {
					data: "notes",
					filter: {
						transform(request) {
							if (this.value) {
								request.transactionsSearch.notes = this.value;
								this.isFiltered = true;
							}
						}
					}
				}
			};

			$scope.filterHelper = {
				filterByColumn(filter) {
					table().ajax.reload();
				},
				resetFilterByColumn(filter) {
					filter.value = null;
					if (filter.reset) {
						filter.reset();
					}
					table().ajax.reload();
				},
				sortColumn(columnName, asc) {
					table().order([$scope.dtColumns.findIndex((column) => column.name === columnName), asc ? 'asc' : 'desc']).draw();
				},
				isSortingAsc(columnName) {
					const index = $scope.dtColumns.findIndex((column) => column.name === columnName);
					const order = table().order().find((order) => order[0] === index);
					if (order) {
						return order[1] === 'asc';
					}
					return null;
				},
				getFilteringByClass(filter) {
					if (filter.isFiltered) {
						return 'filtering-by';
					}
				}
			};

			InventoryService.queryUnits().then((unitTypes) => {
				$scope.columns.lotUnitName.filter.options = unitTypes.map((unitType) => {
					return {
						name: unitType.name,
						id: unitType.id
					}
				});
			});

			InventoryService.queryTransactionTypes().then((transactionTypes) => {
				$scope.columns.transactionType.filter.options = transactionTypes.map((transactionType) => {
					return {
						name: transactionType.value,
						id: transactionType.id
					}
				});
			});

			InventoryService.queryTransactionStatusTypes().then((statusTypes) => {
				$scope.columns.transactionStatus.filter.options = statusTypes.map((statusType) => {
					return {
						name: statusType.value,
						id: statusType.intValue
					}
				});
			});

			/**
			 * - column.name used for sorting
			 */
			$scope.dtColumns = Object.entries($scope.columns).map(([name, column]) => {
				return {
					name: name,
					data: column.data,
					visible: column.visible
				}
			});

			$scope.dtColumnDefs =  [
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
					targets: "germplasm-link-column",
					render: function (data, type, rowData, meta) {
						return '<a class="gid-link" href="javascript: void(0)"'
							+ ` onclick="openGermplasmDetailsPopopWithGidAndDesig('${rowData.lot.gid}','${rowData.lot.designation}')">`
							+ EscapeHTML.escape(data) + '</a>';
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
				const dataTable = table();
				if (!dataTable) {
					return [];
				}
				return dataTable.data().toArray().map((data) => {
					return data.transactionId;
				});
			}

			function table() {
				return $scope.nested.dtInstance.DataTable;
			}

			$scope.changeEnvironment = function () {
				table().columns("instanceNo:name").visible($scope.nested.selectedEnvironment === $scope.environments[0])
				table().ajax.reload();
			};

			studyInstanceService.getStudyInstances().then((instances) => {
				$scope.environments = [{
					instanceNumber: null,
					locationName: 'All environments'
				}].concat(instances.filter((instance) => instance.hasInventory));
				$scope.nested.selectedEnvironment = $scope.environments[1];

				dtOptionsDeferred.resolve(dtOptions);
			});
		}
	]);
})();
