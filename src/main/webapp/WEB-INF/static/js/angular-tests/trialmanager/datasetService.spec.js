'use strict';

describe('Dataset Service', function () {

	var datasetService, $httpBackend, $q;

	beforeEach(function () {

		module('datasets-api');

		spyOn(JSON, 'parse').and.callFake(function (key) {
			return {token: 734789327};
		});

		module(function ($provide) {
			$provide.value("studyContext", {});
			$provide.value("serviceUtilities", {});
			$provide.value("DATASET_TYPES_SUBOBSERVATION_IDS", {});
		});
	});

	beforeEach(inject(function ($injector) {
		datasetService = $injector.get('datasetService');
		$httpBackend = $injector.get('$httpBackend');
		$q = $injector.get('$q');
	}));

	describe('observationCount', function () {

		it('should call the correct web api', function () {

			var studyId = 1;
			var datasetId = 2;
			var variableIds = [888, 999];

			$httpBackend.whenHEAD('/bmsapi/crops/maize/studies/' + studyId + '/datasets/' +
				datasetId + '/variables/observations?variableIds=' + variableIds.join(','))
				.respond({}, {'X-Total-Count': '100'});

			datasetService.observationCount(studyId, datasetId, variableIds).then(function (response) {
				expect(response.headers('response')).toEqual('100');
			});

		});

		it('should return reject if any of the parameters are undefined', function () {

			datasetService.observationCount(undefined, undefined, undefined).then(function (response) {
			}).catch(function (reason) {
				expect(reason).toEqual('studyId, datasetId and variableIds are not defined.');
			});

		});

	});

});
