(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.controller('CalculateCOPModalCtrl', ['$scope', '$rootScope', 'studyContext', '$uibModalInstance', 'DTOptionsBuilder', 'DTColumnBuilder',
		'$timeout', '$q', '$compile', 'ChangePlotEntryService', 'CalculateCOPService', '$filter', '$location',
		function ($scope, $rootScope, studyContext, $uibModalInstance, DTOptionsBuilder, DTColumnBuilder, $timeout, $q, $compile, ChangePlotEntryService, CalculateCOPService, $filter, $location) {

			var initResolve;
			$scope.initPromise = new Promise(function (resolve) {
				initResolve = resolve;
			});

			$scope.selectedItems = [];
			var dtColumnsPromise = $q.defer();
			var dtColumnDefsPromise = $q.defer();

			$scope.dtColumns = dtColumnsPromise.promise;
			$scope.dtColumnDefs = dtColumnDefsPromise.promise;
			$scope.dtOptions = null;

			const url = $location.$$absUrl.split("/");
			$scope.model = {};
			$scope.model.platform_url = url[0] + "//" + url[2];
			$scope.model.generations = 1;
			$scope.model.breedermail = "";
			$scope.model.pollination_type = "self";

			loadTable();

			$scope.confirm = function (dtForm) {

				if(!dtForm.email.$valid){
					showErrorMessage('', 'Please verify the Breeder email');
					return;
				}
				if(!dtForm.generations.$valid){
					showErrorMessage('', 'Please verify the number of generations. The minimum value allowed is zero');
					return;
				}

				const studyRequest = {
					"source": "IBP",
					"breederid": studyContext.loggedInUserId,
					"breedermail": $scope.model.breedermail,
					"breedername": "Diego",
					"studydata": {
						"sid": studyContext.studyId,
						"listid": 8354, // Mandatory
						"germplasmids": $scope.selectedItems,
						"crop": studyContext.cropName
					},
					"tenantid": "I7Z4",
					"pollination_type": $scope.model.pollination_type,
					"generations": $scope.model.generations,
					"skip_cache": true,
					"last_modified_date": $filter('date')(new Date(), 'yyyy-MM-dd')
				};
				CalculateCOPService.exportStudy(studyRequest).then((responsed) => {
					const smartModuleToken = responsed.success.token;
					const apiToken = JSON.parse(localStorage['bms.xAuthToken']).token;
					const apiRequest = {
						apis: [],
						token: smartModuleToken,
						platform_url: $scope.model.platform_url
					};

					$scope.selectedItems.forEach(gid => apiRequest.apis.push({
						api_name: "getpedgree",
						url: "/bmsapi/maize/brapi/v1/germplasm/" + gid + "/pedigree?includeSiblings=true",
						api_token: apiToken
					}));

					CalculateCOPService.permissions(apiRequest).then((responsed) => {
						$uibModalInstance.close();
						showSuccessfulMessage('', 'calculation COP process has been started successfully. You will receive an email on ' + $scope.model.breedermail + ' with the process status.');
					}, onError)
				}, onError);
			};

			$scope.isSelected = function (gid) {
				return gid && $scope.selectedItems.length > 0 && $scope.selectedItems.find((item) => item === gid);
			};

			$scope.toggleSelect = function (gid) {
				var idx = $scope.selectedItems.indexOf(gid);
				if (idx > -1) {
					$scope.selectedItems.splice(idx, 1)
				} else {
					$scope.selectedItems.push(gid);
				}
			};

			$scope.cancel = function () {
				$uibModalInstance.dismiss();
			};

			$scope.valid = function () {
				return $scope.selectedItems.length > 0;
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
				if (response) {
					showErrorMessage('', Object.keys(response.data) + ": " + Object.values(response.data));
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
							return row.properties[columnData.termId];
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
								//$(td).append($compile('<span><input type="radio" name="rowData.entryId" ng-model="selected.entryId" value=' + rowData.entryId + '></span>')($scope));
								$(td).append($compile('<span><input type="checkbox" ng-checked="isSelected(' + rowData.gid + ')" ng-click="toggleSelect(' + rowData.gid + ')"></span>')($scope));

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
								return full.lotCount;
							}
						});

					} else if (columnData.termId === -4) {
						// AVAILABLE
						columnsDef.push({
							targets: columns.length - 1,
							orderable: false,
							render: function (data, type, full, meta) {
								return full.availableBalance;
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
				// FIXME: Until now the sort works with entryNumber when will implements by specific column we need replace the code by the commented.
				/*if ($scope.columnsData[order.column]) {
					pageQuery += '&sort=' + $scope.columnsData[order.column].termId + ',' + order.dir;
				}*/
				pageQuery += '&sort=entryNumber' + ',' + order.dir;

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
					}).withDOM('<"row"<"col-sm-6"l>>' +
						'<"row"<"col-sm-12"tr>>' +
						'<"row"<"col-sm-5"i><"col-sm-7">>' +
						'<"row"<"col-sm-12"p>>')
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
