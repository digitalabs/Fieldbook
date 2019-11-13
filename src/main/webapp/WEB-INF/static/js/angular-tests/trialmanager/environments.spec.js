'use strict';

describe('Measurement Controller', function () {

	var controller, scope, q, httpBackend;
	var studyContext = {
		studyId: 1,
		measurementDatasetId: 2009
	};
	var rootScopeMock = {
		openConfirmModal: jasmine.createSpy('openConfirmModal'),
	};
	var datasetServiceMock = {observationCountByInstance: jasmine.createSpy('observationCountByInstance')};
	var responseMock = {headers: jasmine.createSpy('headers')};
	var trialManagerServiceMock = {
		settings: {
			environments: [],
			managementDetails: []
		},
		currentData: {
			environments: []
		},
		trialMeasurement: {},
		onUpdateData: function () {
		},
	};

	beforeEach(function () {

		module('datatables');
		module('datatables.buttons');
		module('ngResource');
		module('ngStorage');
		module('leafnode-utils');
		module('fieldbook-utils');
		module('ui.bootstrap');
		module('ui.select');
		module('ui.select2');
		module('ngSanitize');
		module('ui.router');
		module('designImportApp');
		module('ngLodash');
		module('showSettingFormElementNew');
		module('manageTrialApp');

		module(function ($provide) {
			$provide.value("TrialManagerDataService", trialManagerServiceMock);
		});
	});

	beforeEach(function () {
		inject(function (_$rootScope_, $controller, $q, $httpBackend) {

			scope = _$rootScope_.$new();
			q = $q;
			httpBackend = $httpBackend;

			trialManagerServiceMock.trialMeasurement['hasAdvancedOrCrossesList'] = false;

			controller = $controller('EnvironmentCtrl', {
				$rootScope: rootScopeMock,
				$scope: scope,
				studyContext: studyContext,
				datasetService: datasetServiceMock,
				TrialManagerDataService: trialManagerServiceMock,
				studyInstanceService: {},
				LOCATION_ID: 1
			});

			spyOn(controller, 'confirmDeleteEnvironment');
			spyOn(controller, 'updateEnvironmentVariables');
			spyOn(controller, 'showAlertMessage');

		});
	});

	beforeEach(function () {
		httpBackend.whenGET('/Fieldbook/TrialManager/createTrial/trialSettings').respond(200, {data: "ok"});
	})

});
