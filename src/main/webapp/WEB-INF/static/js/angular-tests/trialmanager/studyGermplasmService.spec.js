'use strict';

describe('Study Germplasm Service', function () {

	var studyGermplasmService, $httpBackend, $q;
	var studyContext = {
		studyId: 1,
		cropName: 'maize',
		measurementDatasetId: 2009
	};
	var entryId = 55;
	var newGid = 23;
	var $uibModal = jasmine.createSpyObj('$uibModal', ['open']);

	beforeEach(function () {

		module('manageTrialApp');

		module(function ($provide) {
			$provide.value("studyContext", studyContext);
			$provide.value("serviceUtilities", {});
			$provide.value("$uibModal", $uibModal);
		});
	});

	beforeEach(inject(function ($injector) {
		studyGermplasmService = $injector.get('studyGermplasmService');
		$httpBackend = $injector.get('$httpBackend');
		$q = $injector.get('$q');
		$uibModal = $injector.get('$uibModal');
	}));

	describe('Replace Germplasm API call', function () {

		it('should call the correct web api', function () {
			$httpBackend.whenPUT('/bmsapi/crops/maize/programs/' + studyContext.programId + '/studies/' + studyContext.studyId + '/germplasm/' +
				entryId)
				.respond(200, {data: getMockStudyGermplasm()});

			studyGermplasmService.replaceStudyGermplasm(entryId, newGid).then(function (response) {
				expect(response.data).toEqual(mockData);
			});

		});

		it('should return reject if any of the parameters are undefined', function () {

			studyGermplasmService.replaceStudyGermplasm(undefined, undefined).then(function (response) {
			}).catch(function (reason) {
				expect(reason).toEqual('studyId, datasetId and variableIds are not defined.');
			});

		});

	});

	describe('Open Replace Germplasm Modal Service', function () {

		it('should open the Replace Germplasm modal window', function () {
			studyGermplasmService.openReplaceGermplasmModal();
			expect($uibModal.open).toHaveBeenCalled();

			var capturedArgument = $uibModal.open.calls.mostRecent().args[0];

			expect(capturedArgument.templateUrl).toEqual('/Fieldbook/static/angular-templates/germplasm/replaceGermplasm.html');
			expect(capturedArgument.controller).toEqual('replaceGermplasmCtrl');
			expect(capturedArgument.size).toEqual('md');

		});


	});

	describe('Manage Selected Germplasm Entries', function () {

		it('should toggle selected germplasm entries', function () {
			studyGermplasmService.toggleSelect(11);
			studyGermplasmService.toggleSelect(13);
			studyGermplasmService.toggleSelect(15);
			expect(studyGermplasmService.getSelectedEntries()).toEqual([11,13,15]);
		});

		it('should reset selected germplasm entries', function () {
			studyGermplasmService.toggleSelect(11);
			studyGermplasmService.toggleSelect(13);
			studyGermplasmService.toggleSelect(15);
			expect(studyGermplasmService.getSelectedEntries()).toEqual([11,13,15]);

			studyGermplasmService.resetSelectedEntries();
			expect(studyGermplasmService.getSelectedEntries()).toEqual([]);
		});

	});

	function getMockStudyGermplasm() {
		return {
			entryId : 56,
			germplasmId : 23
		};
	}


});
