'use strict';

describe('Location', function () {
    var controller;
    var trialDataManagerService = {
        settings: {
            environments: [],
            managementDetails: [23057]
        },
        currentData: {
            environments: {
                noOfEnvironments: 2
            }
        }, 
        onUpdateData: function () {

        },
        checkVariableIsUsedInCalculatedVariable: function () {
            
        }
    };

    var scope =  jasmine.createSpyObj('scope', ['checkVariableIsUsedInCalculatedVariable']);
    var rootScope = jasmine.createSpyObj('$rootScope', ['openConfirmModal']);

    var studyContext = {
        studyId: 1,
        cropName: 'maize',
        measurementDatasetId: null
    };

    var datasetService = jasmine.createSpyObj('datasetService', ['getDatasetInstances', 'exportDataset']);
    var derivedVariableService=  jasmine.createSpyObj('derivedVariableService', ['getFormulaVariables']);
    var formulaVariables;

    beforeEach(function(){

        module(function ($provide) {

            $provide.value("studyContext", studyContext);
            $provide.value("serviceUtilities", {});
            $provide.value("DATASET_TYPES_OBSERVATION_IDS", [4, 5, 6, 7, 8]);
            $provide.value("DATASET_TYPES", {});
            $provide.value("derivedVariableService", derivedVariableService);
            $provide.value("TrialManagerDataService", trialDataManagerService);

        });


        angular.module('subObservation');
        angular.module('datasets-api');
        angular.module('fieldbook-utils');
        angular.module('ui.bootstrap');
        angular.module('datasetOptionModal');
        angular.module('leafnode-utils');
        angular.module('derived-variable');
        module('manageTrialApp');

    });


    beforeEach(function () {
        inject(function ($rootScope, $controller, $injector, $q){

            scope = $rootScope.$new();
            datasetService = $injector.get('datasetService');
            derivedVariableService.getFormulaVariables.and.returnValue($q.resolve(null));
            controller = $controller('EnvironmentCtrl',{
                $rootScope: rootScope,
                $scope: scope,
                studyContext: studyContext,
                derivedVariableService: derivedVariableService,
                TrialManagerDataService: trialDataManagerService,
                environmentService: {},
                LOCATION_ID: 1
            });
        });
    });


    describe('Location Detail Delete', function () {
        it('unsaved study remove should not throw an error', function () {
            formulaVariables = null;
            expect(function(){
                scope.checkVariableIsUsedInCalculatedVariable()
            }).not.toThrow();
        });

        it('saved study remove should not throw an error', function () {
            formulaVariables = [];
            expect(scope.checkVariableIsUsedInCalculatedVariable()).not.toBeNull();
        });
    });


});