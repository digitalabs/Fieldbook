(function () {
	'use strict';

	var studyGermplasmSourceModule = angular.module('study-germplasmm-source', []);

	studyGermplasmSourceModule.controller('StudyGermplasmSourceCtrl',
		['$scope', '$q', '$compile', 'studyContext', 'DTOptionsBuilder', 'studyGermplasmSourceService',
			function ($scope, $q, $compile, studyContext, DTOptionsBuilder, studyGermplasmSourceService) {

				$scope.nested = {};
				$scope.nested.dtInstance = null;

				const dtOptionsDeferred = $q.defer();
				$scope.dtOptions = dtOptionsDeferred.promise;

				const dtOptions = DTOptionsBuilder.newOptions()
					.withOption('ajax', {
						url: studyGermplasmSourceService.getStudyGermplasmSourceTableUrl(),
						type: 'POST',
						contentType: 'application/json',
						beforeSend: function (xhr) {
							xhr.setRequestHeader('X-Auth-Token', JSON.parse(localStorage['bms.xAuthToken']).token);
						},
						data: function (d) {
							var order = d.order && d.order[0];

							return JSON.stringify(addFilters({
								studyId: studyContext.studyId,
								sortedRequest: {
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
					.withOption('order', [[1, 'asc']]) //sourceId
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
					request.studyGermplasmSourceSearchDto = {};
					Object.entries($scope.columns).forEach(([name, column]) => {
						if (column.filter && column.filter.transform) {
							column.filter.isFiltered = false;
							column.filter.transform(request);
						}
					});
					return request;
				}

				$scope.columns = {
					checkbox: {
						data: function () {
							return "";
						},
						orderable: false
					},
					sourceId: {
						data: function () {
							return "";
						},
						orderable: true
					},
					gid: {
						data: 'gid',
						filter: {
							transform(request) {
								if (this.value) {
									request.studyGermplasmSourceSearchDto.gid = this.value;
									this.isFiltered = true;
								}
							}
						},
						orderable: true
					},
					groupId: {
						data: 'groupId',
						filter: {
							transform(request) {
								if (this.value) {
									request.studyGermplasmSourceSearchDto.groupId = this.value;
									this.isFiltered = true;
								}
							}
						},
						orderable: true
					},
					designation: {
						data: 'designation',
						filter: {
							transform(request) {
								if (this.value) {
									request.studyGermplasmSourceSearchDto.designation = this.value;
									this.isFiltered = true;
								}
							}
						},
						orderable: true
					},
					cross: {
						data: 'cross',
						orderable: false
					},
					lots: {
						data: 'lots',
						filter: {
							transform(request) {
								if (this.value) {
									request.studyGermplasmSourceSearchDto.lots = this.value;
									this.isFiltered = true;
								}
							}
						},
						orderable: true
					},
					breedingMethodAbbrevation: {
						data: 'breedingMethodAbbrevation',
						filter: {
							transform(request) {
								if (this.value) {
									request.breedingMethodAbbrevation = this.value;
									this.isFiltered = true;
								}
							}
						},
						orderable: true
					},
					breedingMethodName: {
						data: 'breedingMethodName',
						filter: {
							transform(request) {
								if (this.value) {
									request.studyGermplasmSourceSearchDto.breedingMethodName = this.value;
									this.isFiltered = true;
								}
							}
						},
						orderable: true
					},
					breedingMethodType: {
						data: 'breedingMethodType',
						filter: {
							transform(request) {
								if (this.value) {
									request.studyGermplasmSourceSearchDto.breedingMethodType = this.value;
									this.isFiltered = true;
								}
							}
						},
						orderable: true
					},
					location: {
						data: 'location',
						filter: {
							transform(request) {
								if (this.value) {
									request.studyGermplasmSourceSearchDto.location = this.value;
									this.isFiltered = true;
								}
							}
						},
						orderable: true
					},
					trialInstance: {
						data: 'trialInstance',
						filter: {
							transform(request) {
								if (this.value) {
									request.studyGermplasmSourceSearchDto.trialInstance = this.value;
									this.isFiltered = true;
								}
							}
						},
						orderable: true
					},
					plotNumber: {
						data: 'plotNumber',
						filter: {
							transform(request) {
								if (this.value) {
									request.studyGermplasmSourceSearchDto.plotNumber = this.value;
									this.isFiltered = true;
								}
							}
						},
						orderable: true
					},
					replicationNumber: {
						data: 'replicationNumber',
						filter: {
							transform(request) {
								if (this.value) {
									request.studyGermplasmSourceSearchDto.replicationNumber = this.value;
									this.isFiltered = true;
								}
							}
						}
					},
					germplasmDate: {
						data: 'germplasmDate',
						filter: {
							transform(request) {
								if (this.value) {
									request.studyGermplasmSourceSearchDto.germplasmDate = this.value;
									this.isFiltered = true;
								}
							}
						},
						orderable: true
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

				/**
				 * - column.name used for sorting
				 */
				$scope.dtColumns = Object.entries($scope.columns).map(([name, column]) => {
					return {
						name: name,
						data: column.data,
						visible: column.visible,
						orderable: column.orderable
					}
				});

				$scope.dtColumnDefs = [
					{
						targets: 0,
						createdCell: function (td, cellData, rowData, rowIndex, colIndex) {
							$(td).append($compile('<span><input type="checkbox" ng-checked="isSelected(' + rowData.sourceId + ')" ng-click="toggleSelect(' + rowData.sourceId + ')"></span>')($scope));
							$scope.$apply();
						}
					},
					{
						targets: 1,
						createdCell: function (td, cellData, rowData, rowIndex, colIndex) {
							$(td).append('<span>' + rowData.sourceId + '</span>');
						}
					},
					{
						targets: "germplasm-link-column",
						render: function (data, type, rowData, meta) {
							return '<a class="gid-link" href="javascript: void(0)"'
								+ ` onclick="openGermplasmDetailsPopopWithGidAndDesig('${rowData.gid}','${rowData.designation}')">`
								+ EscapeHTML.escape(data) + '</a>';
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
					const dataTable = table();
					if (!dataTable) {
						return [];
					}
					return dataTable.data().toArray().map((data) => {
						return data.sourceId;
					});
				}

				function table() {
					return $scope.nested.dtInstance.DataTable;
				}

				dtOptionsDeferred.resolve(dtOptions);


			}]);

})();