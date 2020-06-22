'use strict';

describe('Replace Germplasm Controller', function () {
	var replaceGermplasmCtrl, scope, $q;
	var studyGermplasmService = jasmine.createSpyObj('studyGermplasmService', ['replaceStudyGermplasm','getSelectedEntries']);
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
			$provide.value("studyGermplasmService", studyGermplasmService);
		});
	});

	beforeEach(function () {
		inject(function ($injector) {
			scope = $injector.get('$rootScope').$new();
			var $controller = $injector.get('$controller');
			$q = $injector.get('$q');
			studyGermplasmService = $injector.get('studyGermplasmService');
			replaceGermplasmCtrl = $controller('replaceGermplasmCtrl', {
				$scope: scope,
				$uibModalInstance: uibModalInstance,
				studyContext: studyContext,
				studyGermplasmService: studyGermplasmService,
			});

			spyOn(replaceGermplasmCtrl, 'showAlertMessage');
		})
	});

	describe('performGermplasmReplacement', function () {
		it('should replace germplasm for valid GID', function () {
			studyGermplasmService.getSelectedEntries.and.returnValue([56]);
			var response = {data: {}};
			studyGermplasmService.replaceStudyGermplasm.and.returnValue($q.resolve(response));
			spyOn($.fn, 'val').and.callFake(function() {
				return '135';
			});

			scope.performGermplasmReplacement();
			expect(replaceGermplasmCtrl.showAlertMessage).not.toHaveBeenCalled();
			expect(studyGermplasmService.replaceStudyGermplasm).toHaveBeenCalledWith(56,'135');
		});

		it('should not replace germplasm if non-numeric GID', function () {
			spyOn($.fn, 'val').and.callFake(function() {
				return 'test';
			});
			scope.performGermplasmReplacement();
			expect(replaceGermplasmCtrl.showAlertMessage).toHaveBeenCalledWith('','Please enter valid GID.');
		});
	});

});
