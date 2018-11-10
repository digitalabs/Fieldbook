'use strict';

describe('Measurement Controller', function () {

	var createController, scope, q;
	var studyContext = {
		studyId: 1,
		measurementDatasetId: 2009
	};

	var modalInstanceMock = {
		result: {
			then: jasmine.createSpy('then')
		}
	};
	var rootScopeMock = jasmine.createSpyObj('$rootScope', {openConfirmModal: modalInstanceMock})
	var trialManagerServiceMock = {settings: {measurements: {}}};
	var derivedVariableServiceMock = {hasMeasurementData: jasmine.createSpy('hasMeasurementData')};
	var datasetServiceMock = {observationCount: jasmine.createSpy('observationCount')};
	var responseMock = {headers: jasmine.createSpy('headers')}

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
		inject(function (_$rootScope_, $controller, $q) {

			scope = _$rootScope_.$new();
			q = $q;
			createController = function () {
				return $controller('MeasurementsCtrl', {
					$rootScope: rootScopeMock,
					$scope: scope,
					TrialManagerDataService: trialManagerServiceMock,
					studyContext: studyContext,
					derivedVariableService: derivedVariableServiceMock,
					datasetService: datasetServiceMock
				});
			};

			createController();
		});
	});

	describe('beforeDelete', function () {

		describe('When dependency variable(s) has measurement data', function () {

			it('it should open the confirmation modal', function () {

				derivedVariableServiceMock.hasMeasurementData.and.returnValue(q.resolve({data: true}));
				datasetServiceMock.observationCount.and.returnValue()

				var variableIds = [1, 2, 3];

				scope.beforeDelete('', variableIds);

				modalInstanceMock.result.then.and.callFake(function () {
					expect(rootScopeMock.openConfirmModal).toHaveBeenCalled();
					expect(modalInstanceMock.result.then).toHaveBeenCalled();
				});

			});

		});

		describe('When trait(s) has measurement data', function () {

			it('it should open the confirmation modal', function () {

				derivedVariableServiceMock.hasMeasurementData.and.returnValue(q.resolve({data: false}));
				datasetServiceMock.observationCount.and.returnValue(q.resolve(responseMock));
				responseMock.headers.withArgs('X-Total-Count').and.returnValue(100);

				var variableIds = [1, 2, 3];

				scope.beforeDelete('', variableIds);

				modalInstanceMock.result.then.and.callFake(function () {
					expect(rootScopeMock.openConfirmModal).toHaveBeenCalled();
					expect(modalInstanceMock.result.then).toHaveBeenCalled();
				});

			});

		});

		describe('When trait(s) don\'t have measurement data', function () {

			it('it should open the confirmation modal', function () {

				derivedVariableServiceMock.hasMeasurementData.and.returnValue(q.resolve({data: false}));
				datasetServiceMock.observationCount.and.returnValue(q.resolve(responseMock));
				responseMock.headers.withArgs('X-Total-Count').and.returnValue(0);

				var variableIds = [1, 2, 3];

				scope.beforeDelete('', variableIds);

				modalInstanceMock.result.then.and.callFake(function () {
					expect(rootScopeMock.openConfirmModal).calls().count().toEqual(0);
					expect(modalInstanceMock.result.then).calls().count().toEqual(0);
				});

			});

		});

		describe('When Sudy is new and hasnt been saved', function () {

			it('it should not check for measurement data', function () {

				var variableIds = [1, 2, 3];

				studyContext.studyId = undefined;

				scope.beforeDelete('', variableIds).then(function () {
					expect(derivedVariableServiceMock.hasMeasurementData).calls().count().toEqual(0);
					expect(datasetServiceMock.observationCount).calls().count().toEqual(0);
					expect(rootScopeMock.openConfirmModal).calls().count().toEqual(0);
					expect(modalInstanceMock.result.then).calls().count().toEqual(0);
				})

			});

		});

	});

});