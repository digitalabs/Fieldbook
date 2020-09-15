(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.controller('ChangePlotEntryModalCtrl', ['$scope', '$rootScope', 'studyContext', '$uibModalInstance', 'DTOptionsBuilder', 'DTColumnBuilder',
		'$timeout', '$q', '$compile', 'ChangePlotEntryService',
		function ($scope, $rootScope, studyContext, $uibModalInstance, DTOptionsBuilder, DTColumnBuilder, $timeout, $q, $compile, ChangePlotEntryService) {

			var initResolve;
			$scope.initPromise = new Promise(function (resolve) {
				initResolve = resolve;
			});

			$scope.selected = {entryId: ''};
			$scope.numberOfInstances = 0;
			$scope.numberOfPlots = 0;

			var dtColumnsPromise = $q.defer();
			var dtColumnDefsPromise = $q.defer();

			$scope.dtColumns = dtColumnsPromise.promise;
			$scope.dtColumnDefs = dtColumnDefsPromise.promise;
			$scope.dtOptions = null;

			ChangePlotEntryService.getObservationUnitsMetadata($scope.$resolve.searchComposite,$scope.$resolve.datasetId).then(function (response) {
				$scope.numberOfInstances = response.selectedInstances;
				$scope.numberOfPlots = response.selectedObservationUnits;
			}, onError);


			loadTable();

			$scope.confirm = function () {
				proceed().then((doProceed) => {

					if (doProceed) {
						var observationUnitEntryReplaceRequest= {
							searchRequest: $scope.$resolve.searchComposite,
							entryId: $scope.selected.entryId
						}
						ChangePlotEntryService.updateObservationUnitsEntry(observationUnitEntryReplaceRequest, $scope.$resolve.datasetId).then(() => {
							$uibModalInstance.close();
							showSuccessfulMessage('', $.fieldbookMessages.changingPlotEntrySuccess);
						}, onError);
					}
				});
			};

			$scope.cancel = function () {
				$uibModalInstance.dismiss();
			};

			$scope.valid = function () {
				return  $scope.selected.entryId != '' ;
			};

			function proceed() {
				var deferred = $q.defer();

				let confirmationMessages = $.fieldbookMessages.changingPlotEntryWarning;

				const message = '<ul>' + confirmationMessages + '</ul>';
				var confirmModal = $scope.openConfirmModal(message);
				confirmModal.result.then(deferred.resolve);
				return deferred.promise;
			}

			function onError(response) {
				if (response.errors && response.errors.length) {
					showErrorMessage('', response.errors[0].message);
				} else {
					showErrorMessage('', ajaxGenericErrorMsg);
				}
			}

			function loadTable() {
				var dtColumnsPromise = $q.defer();
				var dtColumnDefsPromise = $q.defer();
				$scope.dtColumns = dtColumnsPromise.promise;
				$scope.dtColumnDefs = dtColumnDefsPromise.promise;
				$scope.dtOptions = null;

				return loadColumns().then(function (columnsObj) {
					$scope.dtOptions = getDtOptions();
					dtColumnsPromise.resolve(columnsObj.columns);
					dtColumnDefsPromise.resolve(columnsObj.columnsDef);
					initResolve();
				});
			}

			function loadColumns() {
				return ChangePlotEntryService.getColumns().then(function (columnsData) {
					$scope.columnsData = addRadioButtonColumn(columnsData);
					var columnsObj = $scope.columnsObj = mapColumns($scope.columnsData);
					return columnsObj;
				});
			}

			function addRadioButtonColumn(columnsData) {
				var columns = columnsData.slice();
				columns.unshift({
					alias: "",
					factor: true,
					name: "RADIO",
					termId: -6,
				});
				return columns;
			}

			function getDtOptions() {
				return addCommonOptions(DTOptionsBuilder.newOptions()
						.withOption('ajax',
							function (d, callback) {
								$.ajax({
									type: 'GET',
									url: ChangePlotEntryService.getEntriesTableUrl() + getPageQueryParameters(d),
									dataSrc: '',
									success: function (res, status, xhr) {
										let json = {recordsTotal: 0, recordsFiltered: 0}
										json.recordsTotal = json.recordsFiltered = xhr.getResponseHeader('X-Total-Count');
										json.data = res;
										callback(json);
									},
									contentType: 'application/json',
									beforeSend: function (xhr) {
										xhr.setRequestHeader('X-Auth-Token', JSON.parse(localStorage['bms.xAuthToken']).token);
									},
								});
							})
						.withDataProp('data')
						.withOption('serverSide', true)
				);
			}

			function mapColumns(columnsData) {
				var columns = [],
					columnsDef = [];

				angular.forEach(columnsData, function (columnData, index) {
					if (columnData.possibleValues) {
						columnData.possibleValuesByName = {};
						columnData.possibleValuesById = {};
						angular.forEach(columnData.possibleValues, function (possibleValue) {
							possibleValue.displayValue = possibleValue.name;
							columnData.possibleValuesByName[possibleValue.name] = possibleValue;
							columnData.possibleValuesById[possibleValue.id] = possibleValue;
						});
					}

					columnData.index = index;

					columns.push({
						title: columnData.alias,
						name: columnData.alias,
						data: function (row) {
							return row.variables[columnData.name];
						},
						defaultContent: '',
						className: 'factors dt-head-nowrap',
						columnData: columnData
					});

					// Radio Button Column
					if (columnData.termId === -6) {
						columnsDef.push({
							targets: columns.length - 1,
							orderable: false,
							createdCell: function (td, cellData, rowData, rowIndex, colIndex) {
								$(td).append($compile('<span><input type="radio" name="rowData.entryId" ng-model="selected.entryId" value=' + rowData.entryId +'></span>')($scope));
							}
						});
					} else if (columnData.termId === 8240 || columnData.termId === 8250) {
						// GID or DESIGNATION
						columnsDef.push({
							targets: columns.length - 1,
							orderable: false,
							render: function (data, type, full, meta) {
								return '<a class="gid-link" href="javascript: void(0)" ' +
									'onclick="openGermplasmDetailsPopopWithGidAndDesig(\'' +
									full.gid + '\',\'' + full.designation + '\')">' + EscapeHTML.escape(data.value) + '</a>';
							}
						});
					} else if (columnData.termId === -3) {
						//ACTIVE LOT
						columnsDef.push({
							targets: columns.length - 1,
							orderable: false,
							render: function (data, type, full, meta) {
								return full.lots;
							}
						});

					} else if (columnData.termId === -4) {
						// AVAILABLE
						columnsDef.push({
							targets: columns.length - 1,
							orderable: false,
							render: function (data, type, full, meta) {
								return full.available;
							}
						});

					} else if (columnData.termId === -5) {
						// UNIT
						columnsDef.push({
							targets: columns.length - 1,
							orderable: false,
							render: function (data, type, full, meta) {
								return full.unit;
							}
						});
					}
					columnsDef.push({
						targets: columns.length - 1,
						orderable: false,
						render: function (data, type, full, meta) {

							if (!data) {
								return '';
							}

							if (columnData.dataTypeId === 1130) {
								return renderCategoricalValue(data.value, columnData);
							}

							return EscapeHTML.escape(data.value);
						}
					});
				});

				return {
					columns: columns,
					columnsDef: columnsDef
				};
			}

			function getPageQueryParameters(data) {
				var order = data.order && data.order[0];
				var pageQuery = '?size=' + data.length
					+ '&page=' + ((data.length === 0) ? 0 : data.start / data.length);
				if ($scope.columnsData[order.column]) {
					pageQuery += '&sort=' + $scope.columnsData[order.column].termId + ',' + order.dir;
				}
				return pageQuery;
			}

			function addCommonOptions(options) {
				return options
					.withOption('processing', true)
					.withOption('lengthMenu', [[50, 75, 100], [50, 75, 100]])
					.withOption('scrollY', '500px')
					.withOption('scrollCollapse', true)
					.withOption('scrollX', '100%')
					.withOption('deferRender', true)
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
						'<"clearfix">' + //
						'<"row add-top-padding-small"<"col-sm-12"t>>' + //
						'<"row"<"col-sm-12 paginate-float-center"<"pull-left"i><"pull-right"l>p>>')
					.withPaginationType('full_numbers');
			}

			function renderCategoricalValue(value, columnData) {
				var displayValue = '';

				if (columnData.possibleValues) {
					var possibleValue = columnData.possibleValuesByName[value] || columnData.possibleValuesById[value];
					displayValue = possibleValue.description;
				}
				return displayValue;

			}
		}
	]);
})();
