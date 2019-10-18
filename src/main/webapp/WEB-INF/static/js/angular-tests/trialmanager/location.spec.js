'use strict';

describe('Location', function () {
    var controller;
    var trialDataManagerService = {
        settings: {
            environments: [],
            managementDetails: [8170]
        },
        currentData: {
            environments: {
                noOfEnvironments: 2
            }
        }
    };
    var rootScopeMock = {
        openConfirmModal: jasmine.createSpy('openConfirmModal')
    };
    var datasetServiceMock = {observationCountByInstance: jasmine.createSpy('observationCountByInstance')};
    beforeEach(function(){
        module('manageTrialApp');
        module(function ($provider) {
            $provider.value("TrialManagerDataService", trialDataManagerService);
        })
    });

    beforeEach(function () {
        inject(function ($rootScope, $controller) {

            controller = $controller('EnvironmentCtrl',{
                $rootScope: rootScopeMock,
                $scope: $rootScope,
                studyContext: studyContext,
                datasetService: datasetServiceMock,
                TrialManagerDataService: trialDataManagerService,
                environmentService: {},
                LOCATION_ID: 1
            });
        });
    });
    describe('Location Delete', function () {
        it('should delete', function () {
        });
    });
});