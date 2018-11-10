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
	var trialManageServiceMock = {
		settings: {
			environments: [],
			managementDetails: []
		},
		currentData: {
			environments: []
		},
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
		module('ct.ui.router.extras');
		module('designImportApp');
		module('ngLodash');
		module('showSettingFormElementNew');
		module('manageTrialApp');

	});

	beforeEach(function () {
		inject(function (_$rootScope_, $controller, $q, $httpBackend) {

			scope = _$rootScope_.$new();
			q = $q;
			httpBackend = $httpBackend;

			rootScopeMock.stateSuccessfullyLoaded['createMeasurements'] = true;
			rootScopeMock.stateSuccessfullyLoaded['editMeasurements'] = true;

			controller = $controller('EnvironmentCtrl', {
				$rootScope: rootScopeMock,
				$scope: scope,
				studyContext: studyContext,
				datasetService: datasetServiceMock,
				TrialManagerDataService: trialManageServiceMock,
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

		it('it should open the confirm delete modal', function () {

			var environmentNo = 1;
			var environmentList = [];
			environmentList[environmentNo] = undefined;

			httpBackend.whenGET('/Fieldbook/trial/measurements/instanceMetadata/' + studyContext.studyId).respond(environmentList);

			controller.hasMeasurementDataOnEnvironment(environmentNo).then(function () {
				expect(controller.confirmDeleteEnvironment).toHaveBeenCalled();
			});

			httpBackend.flush();

		});

	});

	describe('When study environment to be deleted is already saved,', function () {


		describe('and observation count is 0', function () {

			it('it should open the confirm delete modal', function () {

				var environmentNo = 1;
				var environmentList = [];
				environmentList[environmentNo] = {};
				httpBackend.whenGET('/Fieldbook/trial/measurements/instanceMetadata/' + studyContext.studyId).respond(environmentList);

				responseMock.headers.withArgs('X-Total-Count').and.returnValue(0);
				datasetServiceMock.observationCountByInstance.and.returnValue(q.resolve(responseMock));

				controller.hasMeasurementDataOnEnvironment(environmentNo).then(function () {
					expect(controller.confirmDeleteEnvironment).toHaveBeenCalled();
				});

				httpBackend.flush();

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

				httpBackend.flush();

			});

		});

	});

});