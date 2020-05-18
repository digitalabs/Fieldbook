(function () {
	'use strict';

	const module = angular.module('manageTrialApp');

	module.controller('PreparePlantingModalCtrl', ['$scope', 'studyContext', '$uibModalInstance', 'DTOptionsBuilder', 'DTColumnBuilder',
		'PreparePlantingService', 'InventoryService',
		function ($scope, studyContext, $uibModalInstance, DTOptionsBuilder, DTColumnBuilder, service, InventoryService) {

			$scope.unitsDTOptions = DTOptionsBuilder.newOptions().withDOM('<"row"<"col-sm-12"tr>>');
			$scope.entriesDTOptions = DTOptionsBuilder.newOptions().withDOM('<"row"<"col-sm-6"l><"col-sm-6"f>>' +
				'<"row"<"col-sm-12"tr>>' +
				'<"row"<"col-sm-5"i><"col-sm-7">>' +
				'<"row"<"col-sm-12"p>>')
				.withOption('processing', true)
				.withOption('lengthMenu', [[20, 50, 75, 100], [20, 50, 75, 100]])
				.withOption('scrollY', '300px')
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
				.withPaginationType('full_numbers');
			$scope.nested = {}
			$scope.nested.entriesDTInstance = null;

			/** { "unitId": { "entryNo1": entryObj } } */
			$scope.entryMap = {};

			service.getPreparePlantingData($scope.$resolve.searchComposite, $scope.$resolve.datasetId).then(function (data) {
				return $scope.transformData(data);
			}).then(() => {
				// Finish initialization setup
				for (const entry of $scope.entries) {
					if (!$scope.entryMap[entry.unit]) {
						$scope.entryMap[entry.unit] = {};
					}
					$scope.entryMap[entry.unit][entry.entryNo] = entry;
					if (entry.stockByStockId) {
						if ($scope.size(entry.stockByStockId) === 1) {
							entry.stockIdSelected = Object.keys(entry.stockByStockId)[0];
						} else if ($scope.size(entry.stockByStockId) > 1) {
							entry.stockIdSelected = Object.entries(entry.stockByStockId)
								.sort((a, b) => a[1].availableBalance > b[1].availableBalance)[0][0];
						}
					}
				}
			});

			$scope.transformData = function (data) {
				return InventoryService.queryUnits().then((unitTypes) => {
					const unitsById = unitTypes.reduce((unitsById, unitType) => {
						unitsById[unitType.id] = unitType.name
						return unitsById;
					}, {})

					$scope.entries = data.entries;
					$scope.entries.forEach((entry) => {
						// FIXME
						//  more than one unit
						const stock = Object.values(entry.stockByStockId);
						if (stock[0]) {
							entry.unit = unitsById[stock[0].unitId];
						}
						entry.numberOfPackets = entry.observationUnits.length;

					})
					$scope.units = data.entries
						.reduce((unitIds, entry) => {
							return unitIds.concat(Object.values(entry.stockByStockId).map((stock) => stock.unitId));
						}, [])
						.reduce((units, unitId) => {
							units[unitsById[unitId]] = {}
							return units;
						}, {});
				});
			};

			$scope.onGroupTransactionsChecked = function (unitId, unit) {
				unit.withdrawAll = false;
				$scope.revalidateEntries(unitId);
			};

			$scope.onWithdrawAllChecked = function (unitId, unit) {
				if (unit.withdrawAll) {
					unit.amountPerPacket = 0;
				}
				$scope.revalidateEntries(unitId);
			};

			$scope.onAmountPerPacketChanged = function (unitId, unit) {
				$scope.revalidateEntries(unitId);
			};

			$scope.revalidateEntries = function (unitId) {
				const entryMap = $scope.entryMap[unitId];
				if (entryMap) {
					for (const [entryNo, entry] of Object.entries(entryMap)) {
						entryMap[Number(entryNo)].valid = $scope.isValid(entry);
					}
				}
			};

			$scope.stockIdChange = function (entry) {
				const entryMap = $scope.entryMap[entry.unit];
				if (!entryMap) {
					return;
				}
				entryMap[entry.entryNo].valid = $scope.isValid(entry);
			};

			$scope.isValid = function (entry) {
				if (!($scope.units && entry)) {
					return false;
				}
				const unit = $scope.units[entry.unit];
				const stock = entry.stockByStockId[entry.stockIdSelected];
				if (!(unit && stock)) {
					return false;
				}
				if (unit.withdrawAll) {
					return stock.availableBalance;
				}
				return unit.amountPerPacket && //
					unit.amountPerPacket * entry.numberOfPackets <= stock.availableBalance;
			};

			$scope.validLotsCount = function (unitId) {
				const entryMap = $scope.entryMap[unitId];
				if (!entryMap) {
					return;
				}
				return Object.values(Object.values(entryMap))
					.filter((entry) => entry.valid).length
			};

			$scope.size = function (obj) {
				return Object.keys(obj).length;
			};

			$scope.keys = function (obj) {
				return Object.keys(obj);
			};

			$scope.cancel = function () {
				$uibModalInstance.dismiss();
			};

			function adjustColumns() {
				$timeout(function () {
					$scope.nested.entriesDTInstance.DataTable.columns.adjust();
				});
			}
		}
	]);
})();
