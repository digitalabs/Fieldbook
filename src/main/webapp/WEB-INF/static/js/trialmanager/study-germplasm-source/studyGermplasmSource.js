(function () {
	'use strict';

	var studyGermplasmSourceModule = angular.module('study-germplasmm-source', []);

	studyGermplasmSourceModule.controller('StudyGermplasmSourceCtrl',
		['$scope', '$q', '$compile', 'studyContext', 'DTOptionsBuilder', 'studyGermplasmSourceService',
			function ($scope, $q, $compile, studyContext, DTOptionsBuilder, studyGermplasmSourceService) {

				var dtColumnsPromise = $q.defer();
				var dtColumnDefsPromise = $q.defer();

				$scope.nested = {};
				$scope.nested.dtInstance = null;
				$scope.dtColumns = dtColumnsPromise.promise;
				$scope.dtColumnDefs = dtColumnDefsPromise.promise;
				$scope.dtOptions = null;
				$scope.selectedItems = [];

				$scope.loadTable = function () {

					$scope.dtOptions = getDtOptions();

					var columns = [
						{data: null, className: 'checkboxCol', title: '', defaultContent: ''},
						{data: 'gid', className: 'gidCol', title: 'GID'},
						{data: 'groupId', className: 'groupIdCol', title: 'GROUP ID'},
						{data: 'designation', className: 'designationCol', title: 'DESIGNATION'},
						{data: 'cross', className: 'crossCol', title: 'CROSS'},
						{data: 'lots', className: 'lotsCol', title: 'LOTS'},
						{data: 'breedingMethodAbbrevation', className: 'breedingMethodAbbrevationCol', title: 'BREEDING METHOD ABBR'},
						{data: 'breedingMethodName', className: 'breedingMethodNameCol', title: 'BREEDING METHOD NAME'},
						{data: 'breedingMethodType', className: 'breedingMethodTypeCol', title: 'BREEDING METHOD TYPE'},
						{data: 'location', className: 'locationCol', title: 'LOCATION'},
						{data: 'trialInstance', className: 'trialInstanceCol', title: 'TRIAL INSTANCE'},
						{data: 'plotNumber', className: 'plotNumberCol', title: 'PLOT_NO'},
						{data: 'replicationNumber', className: 'replicationNumberCol', title: 'REP_NO'},
						{data: 'germplasmDate', className: 'germplasmDateCol', title: 'GERMPLASM DATE'}
					];
					var columnsDef = [];
					columnsDef.push({
						targets: 0,
						orderable: false,
						createdCell: function (td, cellData, rowData, rowIndex, colIndex) {
							$(td).append($compile('<span><input type="checkbox" ng-checked="isSelected(' + rowData.gid + ')" ng-click="toggleSelect(' + rowData.gid + ')"></span>')($scope));
						}
					});
					dtColumnsPromise.resolve(columns);
					dtColumnDefsPromise.resolve(columnsDef);

				}

				$scope.isSelected = function (id) {
					return id && $scope.selectedItems.length > 0 && $scope.selectedItems.find((item) => item === id);
				};

				$scope.toggleSelect = function (id) {
					var idx = $scope.selectedItems.indexOf(id);
					if (idx > -1) {
						$scope.selectedItems.splice(idx, 1)
					} else {
						$scope.selectedItems.push(id);
					}
				};

				function getDtOptions() {
					return addCommonOptions(DTOptionsBuilder.newOptions()
						.withOption('ajax', {
							url: studyGermplasmSourceService.getStudyGermplasmSourceTableUrl(),
							type: 'POST',
							contentType: 'application/json',
							beforeSend: function (xhr) {
								xhr.setRequestHeader('X-Auth-Token', JSON.parse(localStorage['bms.xAuthToken']).token);
							},
							data: function (d) {
								var order = d.order && d.order[0];
								return JSON.stringify({
									studyId: studyContext.studyId,
									sortedRequest: {
										pageSize: d.length,
										pageNumber: d.length === 0 ? 1 : d.start / d.length + 1
										// TODO: Implent sort
										//sortBy: null,
										//sortOrder: order.dir
									},
									// TODO: Implement filter
									//filter: getFilter()
								});
							}
						})
						.withDataProp('data')
						.withOption('serverSide', true));
					// TODO: Implement filter
					//.withOption('initComplete', initCompleteCallback);
				}

				function addCommonOptions(options) {
					return options
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
						.withColReorder()
						.withPaginationType('full_numbers');
				}

				$scope.loadTable();

			}]);

})();