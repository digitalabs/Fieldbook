/*global angular, showAlertMessage, showErrorMessage, */
(function() {
	'use strict';
	var manageTrialApp = angular.module('manageTrialApp');

	manageTrialApp.controller('CrossingSettingsCtrl', ['$scope', function ($scope) {
		$scope.nextSequenceName = '';
		$scope.sampleParentageDesignation = '';
		$scope.harvestYears = [];
		$scope.harvestMonths = [];
		$scope.useManualNaming = 'false';
		$scope.checkExistingCrosses = false;
		$scope.settingObject = {
			name : '',
			breedingMethodSetting : {
				methodId : null,
				basedOnStatusOfParentalLines : false,
				basedOnImportFile : false
			},
			crossNameSetting : {
				prefix : '',
				suffix : '',
				addSpaceBetweenPrefixAndCode : false,
				addSpaceBetweenSuffixAndCode : false,
				numOfDigits : null,
				separator : '/',
				startNumber : 1,
				saveParentageDesignationAsAString : false
			},
			preservePlotDuplicate : false,
			applyNewGroupToPreviousCrosses : false,
			isUseManualSettingsForNaming : false,
			additionalDetailsSetting : {
				harvestLocationId : null,
				harvestYear : null,
				harvestMonth : null,
				harvestDate : null
			}
		};

		$scope.targetkey = 'selectedLocation';
		$scope.valuecontainer = {selectedLocation : 1};

		function init() {
			retrieveLocationIdFromFirstEnviroment().done(function (locationId) {
				$scope.valuecontainer.selectedLocation = locationId;
				$scope.settingObject.additionalDetailsSetting.harvestLocationId = locationId;
				console.log('default location ' + $scope.settingObject.additionalDetailsSetting.harvestLocationId);
			});

		}

		init();

		$scope.isUseManualNaming = function () {
			return $scope.settingObject.isUseManualSettingsForNaming === 'true';
		}

		$scope.locationChanged = function () {
			$scope.settingObject.additionalDetailsSetting.harvestLocationId = $scope.valuecontainer.selectedLocation
			console.log('selected location ' + $scope.settingObject.additionalDetailsSetting.harvestLocationId);
		}

		$scope.continue = function () {
			var valid = true;
			if (!isCrossImportSettingsValid($scope.settingObject)) {
				valid = false;
			}
			if (valid) {
				retrieveNextNameInSequence(function(data){
					if (data.success === '1') {
						console.log('validation succeeds');
						// ImportCrosses.showCrossListPopup(crossSettingsPopupModal);
					} else {
						showErrorMessage('', data.error);
					}
				}, function(){ showErrorMessage('', $.fieldbookMessages.errorNoNextNameInSequence)} );
			}
		}

		$scope.toggleNamingSection = function () {
			$scope.settingObject.isUseManualSettingsForNaming = $scope.useManualNaming === 'true';
		}

		$scope.updateDisplayedSequenceNameValue = function () {
			if(validateStartingSequenceNumber($scope.settingObject.crossNameSetting.startNumber)) {
				retrieveNextNameInSequence(updateNextSequenceName
					, function() { showErrorMessage('', $.fieldbookMessages.errorNoNextNameInSequence); });
			}
		}


		$scope.updateSampleParentageDesignation = function () {
			$scope.sampleParentageDesignation = 'FEMALE-123' + $scope.settingObject.crossNameSetting.separator + 'MALE-456';
		}

		$scope.fetchMonths = function ($select, $event) {
			// no event means first load!
			if (!$event) {
				$scope.harvestMonths = [];
			} else {
				$event.stopPropagation();
				$event.preventDefault();
			}
			retrieveHarvestMonths().done(function(monthData) {
				$scope.harvestMonths = $scope.harvestMonths.concat(monthData);
			});
		};

		$scope.fetchYears = function ($select, $event) {
			// no event means first load!
			if (!$event) {
				$scope.harvestYears = [];
			} else {
				$event.stopPropagation();
				$event.preventDefault();
			}
			retrieveHarvestYears().done(function(yearData) {
				$scope.harvestYears = $scope.harvestYears.concat(yearData);
				//select the current year; the current year is the middle with the options as -10, current, +10 years
				var currentYearIndex = parseInt(yearData.length/2);
				$scope.settingObject.additionalDetailsSetting.harvestYear = yearData[currentYearIndex];
			});
		};

		$scope.updateHarvestDate = function () {
			$scope.settingObject.additionalDetailsSetting.harvestDate =
				$scope.settingObject.additionalDetailsSetting.harvestYear + '-' + $scope.settingObject.additionalDetailsSetting.harvestMonth + '-01';
		}

		function retrieveLocationIdFromFirstEnviroment() {
			return $.ajax({
				url: '/Fieldbook/crosses/getLocationIdFromFirstEnviroment',
				type: 'GET',
				cache: false,
				async: false,
				success: function (data) {
				},
				error: function (jqXHR, textStatus, errorThrown) {
					console.log('The following error occurred: ' + textStatus, errorThrown);
				},
				complete: function () {
				}
			});
		}
		function validateStartingSequenceNumber(value) {
			'use strict';
			if (value !== null && value !== '' && (!isInt(value))) {
				createErrorNotification(invalidInputMsgHeader, invalidStartingNumberErrorMsg);
				return false;
			}
			return true;
		}

		function updateNextSequenceName(data) {
			if (data.success === '1') {
				$scope.nextSequenceName = data.sequenceValue;
			} else {
				showErrorMessage('', data.error);
			}
		}

		function retrieveNextNameInSequence(success, fail) {
			'use strict';
			$.ajax({
				headers: {
					Accept: 'application/json',
					'Content-Type': 'application/json'
				},
				url: '/Fieldbook/crosses/generateSequenceValue',
				type: 'POST',
				data: JSON.stringify($scope.settingObject),
				cache: false
			}).done(function(data) {
				success(data);
			}).fail(function() {
				fail();
			});
		}

		function retrieveHarvestMonths() {
			'use strict';
			//TODO handle errors for ajax request
			return $.ajax({
				url: '/Fieldbook/crosses/getHarvestMonths',
				type: 'GET',
				cache: false
			});
		}

		function retrieveHarvestYears() {
			'use strict';
			//TODO handle errors for ajax request
			return $.ajax({
				url: '/Fieldbook/crosses/getHarvestYears',
				type: 'GET',
				cache: false
			});
		}

		function isCrossImportSettingsValid() {

			var valid = true;
			if($scope.settingObject.additionalDetailsSetting.harvestMonth === '') {
				valid = false;
				showErrorMessage('', $.fieldbookMessages.errorNoHarvestMonth);
			}
			if (!$scope.settingObject.additionalDetailsSetting.harvestLocationId) {
				valid = false;
				showErrorMessage('', $.fieldbookMessages.errorNoHarvestLocation);
			}
			if ($scope.settingObject.isUseManualSettingsForNaming) {
				if (!$scope.settingObject.crossNameSetting.prefix || $scope.settingObject.crossNameSetting.prefix === '') {
					valid = false;
					showErrorMessage('', $.fieldbookMessages.errorNoNamePrefix);
				} else if (!$scope.settingObject.crossNameSetting.separator || $scope.settingObject.crossNameSetting.separator === '') {
					valid = false;
					showErrorMessage('', $.fieldbookMessages.errorNoParentageDesignationSeparator);
				}

				if (!validateStartingSequenceNumber($scope.settingObject.crossNameSetting.startNumber)) {
					return false;
				}
			}

			return valid;
		}
	}]);
})();
