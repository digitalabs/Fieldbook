'use strict';

describe('Measurement Controller', function () {

	var controller, scope, q, httpBackend;
	var studyContext = {
		studyId: 1,
		measurementDatasetId: 2009
	};
	var rootScopeMock = {
		openConfirmModal: jasmine.createSpy('openConfirmModal'),
		stateSuccessfullyLoaded: {}
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

			rootScopeMock.stateSuccessfullyLoaded['createMeasurements'] = true;
			rootScopeMock.stateSuccessfullyLoaded['editMeasurements'] = true;
			trialManagerServiceMock.trialMeasurement['hasAdvancedOrCrossesList'] = false;

			controller = $controller('EnvironmentCtrl', {
				$rootScope: rootScopeMock,
				$scope: scope,
				studyContext: studyContext,
				datasetService: datasetServiceMock,
				TrialManagerDataService: trialManagerServiceMock,
				environmentService: {},
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

	describe('When study environment to be deleted is not yet saved,', function () {

		it('it should check if there is an existing cross/advance list', function () {
			spyOn(controller, 'hasAdvancedOrCrossesListOnStudy');
			var environmentNo = 1;
			var environmentList = [];
			environmentList[environmentNo] = undefined;

			httpBackend.whenGET('/Fieldbook/trial/measurements/instanceMetadata/' + studyContext.studyId).respond(environmentList);

			controller.hasMeasurementDataOnEnvironment(environmentNo).then(function () {
				expect(controller.hasAdvancedOrCrossesListOnStudy).toHaveBeenCalled();
			});

		});
	});

	describe('When study environment to be deleted is already saved,', function () {

		describe('and observation count is 0', function () {

			it('it should check if there is an existing cross/advance list', function () {
				spyOn(controller, 'hasAdvancedOrCrossesListOnStudy');
				var environmentNo = 1;
				var environmentList = [];
				environmentList[environmentNo] = {};
				httpBackend.whenGET('/Fieldbook/trial/measurements/instanceMetadata/' + studyContext.studyId).respond(environmentList);

				responseMock.headers.withArgs('X-Total-Count').and.returnValue(0);
				datasetServiceMock.observationCountByInstance.and.returnValue(q.resolve(responseMock));

				controller.hasMeasurementDataOnEnvironment(environmentNo).then(function () {
					expect(controller.hasAdvancedOrCrossesListOnStudy).toHaveBeenCalled();
				});

			});

		});

		describe('and observation count is more than 0', function () {

			it('it should show a warning that environment cannot be removed', function () {

				var environmentNo = 1;
				var environmentList = [];
				environmentList[environmentNo] = {};

				httpBackend.whenGET('/Fieldbook/trial/measurements/instanceMetadata/' + studyContext.studyId).respond(environmentList);

				responseMock.headers.withArgs('X-Total-Count').and.returnValue(100);
				datasetServiceMock.observationCountByInstance.and.returnValue(q.resolve(responseMock));

				controller.hasMeasurementDataOnEnvironment(environmentNo).then(function () {
					expect(controller.showAlertMessage)
						.toHaveBeenCalledWith('', 'This environment cannot be removed because it contains measurement data.');
				});

			});

		});

	});

	describe('When study of the environment to be deleted is has no advance/cross list', function () {

		it('it should open the confirm delete modal', function () {
			var environmentNo = 1;

			controller.hasAdvancedOrCrossesListOnStudy(environmentNo).then(function () {
				expect(controller.confirmDeleteEnvironment).toHaveBeenCalled();
			});
		});
	});

	describe('When study of the environment to be deleted has advance/cross list', function () {

		it('it should open the confirm delete modal', function () {
			var environmentNo = 1;
			trialManagerServiceMock.trialMeasurement['hasAdvancedOrCrossesList'] = true;

			controller.hasAdvancedOrCrossesListOnStudy(environmentNo).then(function () {
				expect(controller.showAlertMessage)
					.toHaveBeenCalledWith('', 'This environment cannot be removed because the study has Advance/Cross List.');
			});
		});
	});
});
