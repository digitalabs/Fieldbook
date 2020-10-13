'use strict';

describe('Replace Germplasm Controller', function () {
	var replaceGermplasmCtrl, scope, $q;
	var studyEntryService = jasmine.createSpyObj('studyEntryService', ['replaceStudyGermplasm','getSelectedEntries']);
	var studyContext = {
		studyId: 1,
		cropName: 'maize',
		measurementDatasetId: 2009
	};

	var uibModalInstance = {
		close: jasmine.createSpy('close'),
		dismiss: jasmine.createSpy('dismiss'),
		result: {
			then: jasmine.createSpy('then')
		}
	};

	beforeEach(function () {
		module('manageTrialApp');
		module(function ($provide) {
			$provide.value("studyEntryService", studyEntryService);
			$provide.value("$uibModalInstance", uibModalInstance);
		});
	});

	beforeEach(function () {
		inject(function ($injector) {
			scope = $injector.get('$rootScope').$new();
			var $controller = $injector.get('$controller');
			$q = $injector.get('$q');
			uibModalInstance = $injector.get('$uibModalInstance');
			studyEntryService = $injector.get('studyEntryService');
			replaceGermplasmCtrl = $controller('replaceGermplasmCtrl', {
				$scope: scope,
				$uibModalInstance: uibModalInstance,
				studyContext: studyContext,
				studyEntryService: studyEntryService,
			});

			spyOn(replaceGermplasmCtrl, 'showAlertMessage');
		})
	});

	describe('performGermplasmReplacement', function () {
		it('should replace germplasm for valid GID', function () {
			studyEntryService.getSelectedEntries.and.returnValue([56]);
			var response = {data: {}};
			studyEntryService.replaceStudyGermplasm.and.returnValue($q.resolve(response));
			spyOn($.fn, 'val').and.callFake(function() {
				return '135';
			});

			scope.performGermplasmReplacement();
			expect(replaceGermplasmCtrl.showAlertMessage).not.toHaveBeenCalled();
			expect(studyEntryService.replaceStudyGermplasm).toHaveBeenCalledWith(56,'135');
		});

		it('should not replace germplasm if non-numeric GID', function () {
			spyOn($.fn, 'val').and.callFake(function() {
				return 'test';
			});
			scope.performGermplasmReplacement();
			expect(replaceGermplasmCtrl.showAlertMessage).toHaveBeenCalledWith('','Please enter valid GID.');
		});
	});

	describe('cancel', function () {

		it('it should close the modal instance', function () {
			scope.cancel();
			expect(uibModalInstance.dismiss).toHaveBeenCalled();
		});

	});

});
