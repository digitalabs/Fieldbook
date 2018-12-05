'use strict';

describe('SubObservationSetCtrl', function () {

	var $controller,
		$rootScope,
		$q,
		$timeout,
		controller,
		scope;

	// Mock objects copied from actual objects using Chrome console
	var rootScopeMock = {},
		extractedSettings = {
			"m_keys": [
				8206,
				100022,
				8630,
				51547,
				20369
			],
			"m_vals": {
				"8206": {
					"variable": {
						"cvTermId": 8206,
						"name": "PLANT_NO",
						"description": "Enumerator for the observed plant",
						"property": "Plants Observed",
						"scale": "Number",
						"method": "Enumerated",
						"dataType": "Numeric",
						"dataTypeId": 1110,
						"minRange": null,
						"maxRange": null,
						"operation": null,
						"formula": null
					},
					"hidden": true,
					"deletable": true,
					"extracted": true
				},
				"8630": {
					"variable": {
						"cvTermId": 8630,
						"name": "GermiTest_date",
						"description": "Date of germination test",
						"property": "Seed germination",
						"scale": "Date (yyyymmdd)",
						"method": "Assigned",
						"dataType": "Date",
						"dataTypeId": 1117,
						"minRange": null,
						"maxRange": null,
						"operation": null,
						"formula": null
					},
					"hidden": false,
					"deletable": true,
					"extracted": true
				},
				"20369": {
					"variable": {
						"cvTermId": 20369,
						"name": "Aflatox_M_ppb",
						"description": "Aflatoxin content BY Aflatox - Measurement IN Ppb",
						"property": "Aflatoxin content",
						"scale": "PPB",
						"method": "Aflatox - Measurement",
						"dataType": "Numeric",
						"dataTypeId": 1110,
						"minRange": null,
						"maxRange": null,
						"operation": null,
						"formula": {
							"formulaId": 10,
							"target": {
								"id": 20369,
								"vocabularyId": 0,
								"name": "Aflatox_M_ppb",
								"definition": null,
								"obsolete": false,
								"dateCreated": null,
								"dateLastModified": null,
								"targetTermId": null
							},
							"inputs": [
								{
									"id": 20369,
									"vocabularyId": 0,
									"name": "Aflatox_M_ppb",
									"definition": null,
									"obsolete": false,
									"dateCreated": null,
									"dateLastModified": null,
									"targetTermId": 20369
								}
							],
							"definition": "{{20369}}+1",
							"active": true,
							"name": "",
							"description": ""
						}
					},
					"hidden": false,
					"deletable": true,
					"extracted": true
				},
				"51547": {
					"variable": {
						"cvTermId": 51547,
						"name": "AleuCol_E_1to5",
						"description": "Aleurone color BY AleuCol - Estimation IN 1-5 Aleurone color scale",
						"property": "Aleurone color",
						"scale": "1-5 Aleurone color scale",
						"method": "AleuCol - Estimation",
						"dataType": "Categorical",
						"dataTypeId": 1130,
						"minRange": null,
						"maxRange": null,
						"operation": null,
						"formula": null
					},
					"hidden": false,
					"deletable": true,
					"extracted": true
				},
				"100022": {
					"variable": {
						"cvTermId": 100022,
						"name": "nah_expected_range",
						"description": "",
						"property": "Aleurone color",
						"scale": "nah_expected_scale",
						"method": "200GW measurement",
						"dataType": "Numeric",
						"dataTypeId": 1110,
						"minRange": null,
						"maxRange": null,
						"operation": null,
						"formula": {
							"formulaId": 9,
							"target": {
								"id": 100022,
								"vocabularyId": 0,
								"name": "nah_expected_range",
								"definition": null,
								"obsolete": false,
								"dateCreated": null,
								"dateLastModified": null,
								"targetTermId": null
							},
							"inputs": [
								{
									"id": 20369,
									"vocabularyId": 0,
									"name": "Aflatox_M_ppb",
									"definition": null,
									"obsolete": false,
									"dateCreated": null,
									"dateLastModified": null,
									"targetTermId": 100022
								},
								{
									"id": 51547,
									"vocabularyId": 0,
									"name": "AleuCol_E_1to5",
									"definition": null,
									"obsolete": false,
									"dateCreated": null,
									"dateLastModified": null,
									"targetTermId": 100022
								}
							],
							"definition": "{{51547}}+{{20369}}",
							"active": true,
							"name": "",
							"description": ""
						}
					},
					"hidden": false,
					"deletable": true,
					"extracted": true
				}
			}
		},
		TrialManagerDataServiceMock = jasmine.createSpyObj('TrialManagerDataService', ['extractSettings']),
		subObservationSet = {
			preview: false
		},
		columns = [
			{
				"termId": 8255,
				"name": "ENTRY_TYPE",
				"alias": "ENTRY_TYPE",
				"description": "Entry type (test/check)- assigned (type)",
				"scale": "Type of ENTRY_TYPE",
				"method": "Assigned",
				"property": "Entry type",
				"dataType": "Categorical",
				"value": null,
				"label": "",
				"dataTypeId": 1130,
				"possibleValues": [
					{
						"id": 10180,
						"name": "C",
						"description": "Check entry",
						"programUUID": null,
						"key": "10180",
						"displayDescription": "C= Check entry",
						"study": false,
						"folder": false
					},
					{
						"id": 10190,
						"name": "D",
						"description": "Disease check",
						"programUUID": null,
						"key": "10190",
						"displayDescription": "D= Disease check",
						"study": false,
						"folder": false
					},
					{
						"id": 10200,
						"name": "S",
						"description": "Stress check",
						"programUUID": null,
						"key": "10200",
						"displayDescription": "S= Stress check",
						"study": false,
						"folder": false
					},
					{
						"id": 10170,
						"name": "T",
						"description": "Test entry",
						"programUUID": null,
						"key": "10170",
						"displayDescription": "T= Test entry",
						"study": false,
						"folder": false
					}
				],
				"possibleValuesString": "",
				"minRange": null,
				"maxRange": null,
				"scaleMinRange": null,
				"scaleMaxRange": null,
				"required": false,
				"treatmentLabel": null,
				"operation": null,
				"role": null,
				"variableType": "GERMPLASM_DESCRIPTOR",
				"formula": null,
				"factor": true,
				"dataTypeCode": "C"
			},
			{
				"termId": 8240,
				"name": "GID",
				"alias": "GID",
				"description": "Germplasm identifier - assigned (DBID)",
				"scale": "Germplasm id",
				"method": "Assigned",
				"property": "Germplasm id",
				"dataType": "Germplasm List",
				"value": null,
				"label": "",
				"dataTypeId": 1135,
				"possibleValues": null,
				"possibleValuesString": null,
				"minRange": null,
				"maxRange": null,
				"scaleMinRange": null,
				"scaleMaxRange": null,
				"required": false,
				"treatmentLabel": null,
				"operation": null,
				"role": null,
				"variableType": "GERMPLASM_DESCRIPTOR",
				"formula": null,
				"factor": true,
				"dataTypeCode": "C"
			},
			{
				"termId": 8250,
				"name": "DESIGNATION",
				"alias": "DESIGNATION",
				"description": "Germplasm identifier - assigned (DBCV)",
				"scale": "Germplasm name",
				"method": "Assigned",
				"property": "Germplasm id",
				"dataType": "Germplasm List",
				"value": null,
				"label": "",
				"dataTypeId": 1135,
				"possibleValues": null,
				"possibleValuesString": null,
				"minRange": null,
				"maxRange": null,
				"scaleMinRange": null,
				"scaleMaxRange": null,
				"required": false,
				"treatmentLabel": null,
				"operation": null,
				"role": null,
				"variableType": "GERMPLASM_DESCRIPTOR",
				"formula": null,
				"factor": true,
				"dataTypeCode": "C"
			},
			{
				"termId": 8230,
				"name": "ENTRY_NO",
				"alias": "ENTRY_NO",
				"description": "Germplasm entry - enumerated (number)",
				"scale": "Number",
				"method": "Enumerated",
				"property": "Germplasm entry",
				"dataType": "Numeric",
				"value": null,
				"label": "",
				"dataTypeId": 1110,
				"possibleValues": null,
				"possibleValuesString": null,
				"minRange": null,
				"maxRange": null,
				"scaleMinRange": null,
				"scaleMaxRange": null,
				"required": false,
				"treatmentLabel": null,
				"operation": null,
				"role": null,
				"variableType": "GERMPLASM_DESCRIPTOR",
				"formula": null,
				"factor": true,
				"dataTypeCode": "N"
			},
			{
				"termId": 8201,
				"name": "OBS_UNIT_ID",
				"alias": "OBS_UNIT_ID",
				"description": "Field observation unit id - assigned (text)",
				"scale": "Text",
				"method": "Assigned",
				"property": "Field plot",
				"dataType": "Character",
				"value": null,
				"label": "",
				"dataTypeId": 1120,
				"possibleValues": null,
				"possibleValuesString": null,
				"minRange": null,
				"maxRange": null,
				"scaleMinRange": null,
				"scaleMaxRange": null,
				"required": false,
				"treatmentLabel": null,
				"operation": null,
				"role": null,
				"variableType": "GERMPLASM_DESCRIPTOR",
				"formula": null,
				"factor": true,
				"dataTypeCode": "T"
			},
			{
				"termId": 8210,
				"name": "REP_NO",
				"alias": "REP_NO",
				"description": "Replication - assigned (number)",
				"scale": "Number",
				"method": "Enumerated",
				"property": "Replication factor",
				"dataType": "Numeric",
				"value": null,
				"label": "",
				"dataTypeId": 1110,
				"possibleValues": null,
				"possibleValuesString": null,
				"minRange": null,
				"maxRange": null,
				"scaleMinRange": null,
				"scaleMaxRange": null,
				"required": false,
				"treatmentLabel": null,
				"operation": null,
				"role": null,
				"variableType": "EXPERIMENTAL_DESIGN",
				"formula": null,
				"factor": true,
				"dataTypeCode": "N"
			},
			{
				"termId": 8200,
				"name": "PLOT_NO",
				"alias": "PLOT_NO",
				"description": "Field plot - enumerated (number)",
				"scale": "Number",
				"method": "Enumerated",
				"property": "Field plot",
				"dataType": "Numeric",
				"value": null,
				"label": "",
				"dataTypeId": 1110,
				"possibleValues": null,
				"possibleValuesString": null,
				"minRange": null,
				"maxRange": null,
				"scaleMinRange": null,
				"scaleMaxRange": null,
				"required": false,
				"treatmentLabel": null,
				"operation": null,
				"role": null,
				"variableType": "EXPERIMENTAL_DESIGN",
				"formula": null,
				"factor": true,
				"dataTypeCode": "N"
			},
			{
				"termId": 8206,
				"name": "PLANT_NO",
				"alias": "PLANT_NO",
				"description": "Enumerator for the observed plant",
				"scale": "Number",
				"method": "Enumerated",
				"property": "Plants Observed",
				"dataType": "Numeric",
				"value": null,
				"label": "",
				"dataTypeId": 1110,
				"possibleValues": null,
				"possibleValuesString": null,
				"minRange": null,
				"maxRange": null,
				"scaleMinRange": null,
				"scaleMaxRange": null,
				"required": false,
				"treatmentLabel": null,
				"operation": null,
				"role": null,
				"variableType": "OBSERVATION_UNIT",
				"formula": null,
				"factor": true,
				"dataTypeCode": "N"
			},
			{
				"termId": 100022,
				"name": "nah_expected_range",
				"alias": "nah_expected_range",
				"description": "",
				"scale": "nah_expected_scale",
				"method": "200GW measurement",
				"property": "Aleurone color",
				"dataType": "Numeric",
				"value": null,
				"label": "",
				"dataTypeId": 1110,
				"possibleValues": null,
				"possibleValuesString": null,
				"minRange": 25,
				"maxRange": 50,
				"scaleMinRange": 25,
				"scaleMaxRange": 50,
				"required": false,
				"treatmentLabel": null,
				"operation": null,
				"role": null,
				"variableType": "TRAIT",
				"formula": null,
				"factor": false,
				"dataTypeCode": "N"
			},
			{
				"termId": 8630,
				"name": "GermiTest_date",
				"alias": "GermiTest_date",
				"description": "Date of germination test",
				"scale": "Date (yyyymmdd)",
				"method": "Assigned",
				"property": "Seed germination",
				"dataType": "Date",
				"value": null,
				"label": "",
				"dataTypeId": 1117,
				"possibleValues": null,
				"possibleValuesString": null,
				"minRange": null,
				"maxRange": null,
				"scaleMinRange": null,
				"scaleMaxRange": null,
				"required": false,
				"treatmentLabel": null,
				"operation": null,
				"role": null,
				"variableType": "TRAIT",
				"formula": null,
				"factor": false,
				"dataTypeCode": "D"
			},
			{
				"termId": 51547,
				"name": "AleuCol_E_1to5",
				"alias": "AleuCol_E_1to5",
				"description": "Aleurone color BY AleuCol - Estimation IN 1-5 Aleurone color scale",
				"scale": "1-5 Aleurone color scale",
				"method": "AleuCol - Estimation",
				"property": "Aleurone color",
				"dataType": "Categorical",
				"value": null,
				"label": "",
				"dataTypeId": 1130,
				"possibleValues": [
					{
						"id": 51650,
						"name": "4",
						"description": "purple",
						"programUUID": null,
						"key": "51650",
						"displayDescription": "4= purple",
						"study": false,
						"folder": false
					},
					{
						"id": 51605,
						"name": "1",
						"description": "colorless",
						"programUUID": null,
						"key": "51605",
						"displayDescription": "1= colorless",
						"study": false,
						"folder": false
					},
					{
						"id": 51662,
						"name": "5",
						"description": "other",
						"programUUID": null,
						"key": "51662",
						"displayDescription": "5= other",
						"study": false,
						"folder": false
					},
					{
						"id": 51621,
						"name": "2",
						"description": "bronze",
						"programUUID": null,
						"key": "51621",
						"displayDescription": "2= bronze",
						"study": false,
						"folder": false
					},
					{
						"id": 51636,
						"name": "3",
						"description": "red",
						"programUUID": null,
						"key": "51636",
						"displayDescription": "3= red",
						"study": false,
						"folder": false
					}
				],
				"possibleValuesString": "",
				"minRange": null,
				"maxRange": null,
				"scaleMinRange": null,
				"scaleMaxRange": null,
				"required": false,
				"treatmentLabel": null,
				"operation": null,
				"role": null,
				"variableType": "TRAIT",
				"formula": null,
				"factor": false,
				"dataTypeCode": "C"
			},
			{
				"termId": 20369,
				"name": "Aflatox_M_ppb",
				"alias": "Aflatox_M_ppb",
				"description": "Aflatoxin content BY Aflatox - Measurement IN Ppb",
				"scale": "PPB",
				"method": "Aflatox - Measurement",
				"property": "Aflatoxin content",
				"dataType": "Numeric",
				"value": null,
				"label": "",
				"dataTypeId": 1110,
				"possibleValues": null,
				"possibleValuesString": null,
				"minRange": null,
				"maxRange": null,
				"scaleMinRange": null,
				"scaleMaxRange": null,
				"required": false,
				"treatmentLabel": null,
				"operation": null,
				"role": null,
				"variableType": "TRAIT",
				"formula": null,
				"factor": false,
				"dataTypeCode": "N"
			}
		],
		serviceDataset = {
			instances: [{
				instanceDbId: 1
			}],
			"variables": [{
				"termId": 8206,
				"name": "PLANT_NO",
				"alias": "PLANT_NO",
				"description": "Enumerator for the observed plant",
				"scale": "Number",
				"method": "Enumerated",
				"property": "Plants Observed",
				"dataType": "Numeric",
				"value": null,
				"label": "",
				"dataTypeId": 1110,
				"possibleValues": null,
				"possibleValuesString": null,
				"minRange": null,
				"maxRange": null,
				"scaleMinRange": null,
				"scaleMaxRange": null,
				"required": false,
				"treatmentLabel": null,
				"operation": null,
				"role": null,
				"variableType": "OBSERVATION_UNIT",
				"formula": null,
				"factor": true,
				"dataTypeCode": "N"
			}, {
				"termId": 100022,
				"name": "nah_expected_range",
				"alias": "nah_expected_range",
				"description": "",
				"scale": "nah_expected_scale",
				"method": "200GW measurement",
				"property": "Aleurone color",
				"dataType": "Numeric",
				"value": null,
				"label": "",
				"dataTypeId": 1110,
				"possibleValues": null,
				"possibleValuesString": null,
				"minRange": 25.0,
				"maxRange": 50.0,
				"scaleMinRange": 25.0,
				"scaleMaxRange": 50.0,
				"required": false,
				"treatmentLabel": null,
				"operation": null,
				"role": null,
				"variableType": "TRAIT",
				"formula": {
					"formulaId": 9,
					"target": {
						"id": 100022,
						"vocabularyId": 0,
						"name": "nah_expected_range",
						"definition": null,
						"obsolete": false,
						"dateCreated": null,
						"dateLastModified": null,
						"targetTermId": null
					},
					"inputs": [{
						"id": 20369,
						"vocabularyId": 0,
						"name": "Aflatox_M_ppb",
						"definition": null,
						"obsolete": false,
						"dateCreated": null,
						"dateLastModified": null,
						"targetTermId": 100022
					}, {
						"id": 51547,
						"vocabularyId": 0,
						"name": "AleuCol_E_1to5",
						"definition": null,
						"obsolete": false,
						"dateCreated": null,
						"dateLastModified": null,
						"targetTermId": 100022
					}],
					"definition": "{{51547}}+{{20369}}",
					"active": true,
					"name": "",
					"description": ""
				},
				"factor": false,
				"dataTypeCode": "N"
			}, {
				"termId": 8630,
				"name": "GermiTest_date",
				"alias": "GermiTest_date",
				"description": "Date of germination test",
				"scale": "Date (yyyymmdd)",
				"method": "Assigned",
				"property": "Seed germination",
				"dataType": "Date",
				"value": null,
				"label": "",
				"dataTypeId": 1117,
				"possibleValues": null,
				"possibleValuesString": null,
				"minRange": null,
				"maxRange": null,
				"scaleMinRange": null,
				"scaleMaxRange": null,
				"required": false,
				"treatmentLabel": null,
				"operation": null,
				"role": null,
				"variableType": "TRAIT",
				"formula": null,
				"factor": false,
				"dataTypeCode": "D"
			}, {
				"termId": 51547,
				"name": "AleuCol_E_1to5",
				"alias": "AleuCol_E_1to5",
				"description": "Aleurone color BY AleuCol - Estimation IN 1-5 Aleurone color scale",
				"scale": "1-5 Aleurone color scale",
				"method": "AleuCol - Estimation",
				"property": "Aleurone color",
				"dataType": "Categorical",
				"value": null,
				"label": "",
				"dataTypeId": 1130,
				"possibleValues": [{
					"id": 51621,
					"name": "2",
					"description": "bronze",
					"programUUID": null,
					"key": "51621",
					"displayDescription": "2= bronze",
					"study": false,
					"folder": false
				}, {
					"id": 51636,
					"name": "3",
					"description": "red",
					"programUUID": null,
					"key": "51636",
					"displayDescription": "3= red",
					"study": false,
					"folder": false
				}, {
					"id": 51650,
					"name": "4",
					"description": "purple",
					"programUUID": null,
					"key": "51650",
					"displayDescription": "4= purple",
					"study": false,
					"folder": false
				}, {
					"id": 51605,
					"name": "1",
					"description": "colorless",
					"programUUID": null,
					"key": "51605",
					"displayDescription": "1= colorless",
					"study": false,
					"folder": false
				}, {
					"id": 51662,
					"name": "5",
					"description": "other",
					"programUUID": null,
					"key": "51662",
					"displayDescription": "5= other",
					"study": false,
					"folder": false
				}],
				"possibleValuesString": "",
				"minRange": null,
				"maxRange": null,
				"scaleMinRange": null,
				"scaleMaxRange": null,
				"required": false,
				"treatmentLabel": null,
				"operation": null,
				"role": null,
				"variableType": "TRAIT",
				"formula": null,
				"factor": false,
				"dataTypeCode": "C"
			}, {
				"termId": 20369,
				"name": "Aflatox_M_ppb",
				"alias": "Aflatox_M_ppb",
				"description": "Aflatoxin content BY Aflatox - Measurement IN Ppb",
				"scale": "PPB",
				"method": "Aflatox - Measurement",
				"property": "Aflatoxin content",
				"dataType": "Numeric",
				"value": null,
				"label": "",
				"dataTypeId": 1110,
				"possibleValues": null,
				"possibleValuesString": null,
				"minRange": null,
				"maxRange": null,
				"scaleMinRange": null,
				"scaleMaxRange": null,
				"required": false,
				"treatmentLabel": null,
				"operation": null,
				"role": null,
				"variableType": "TRAIT",
				"formula": {
					"formulaId": 10,
					"target": {
						"id": 20369,
						"vocabularyId": 0,
						"name": "Aflatox_M_ppb",
						"definition": null,
						"obsolete": false,
						"dateCreated": null,
						"dateLastModified": null,
						"targetTermId": null
					},
					"inputs": [{
						"id": 20369,
						"vocabularyId": 0,
						"name": "Aflatox_M_ppb",
						"definition": null,
						"obsolete": false,
						"dateCreated": null,
						"dateLastModified": null,
						"targetTermId": 20369
					}],
					"definition": "{{20369}}+1",
					"active": true,
					"name": "",
					"description": ""
				},
				"factor": false,
				"dataTypeCode": "N"
			}]
		},
		$stateParamsMock = {
			subObservationSet: subObservationSet
		},
		dTOptionsBuilderMock = {},
		dTColumnBuilderMock = {},
		$httpMock = {},
		$compileMock = {},
		environmentServiceMock = {},
		datasetServiceMock = jasmine.createSpyObj('datasetService', ['getDataset', 'getColumns', 'getObservationTableUrl']);

	beforeEach(function () {

		module(function ($provide) {
			$provide.value("datasetService", datasetServiceMock);
			$provide.value("TrialManagerDataService", TrialManagerDataServiceMock);
		});

		module('subObservation');
		module('datatables');
		module('datatables.buttons');
		module('datatables.colreorder');
	});

	beforeEach(inject(function (_$controller_, _$rootScope_, _$q_, $injector, _$timeout_) {
		$controller = _$controller_;
		$rootScope = _$rootScope_;
		$q = _$q_;
		$timeout = _$timeout_;

		scope = $rootScope.$new();
		scope.subObservationTab = {
			id: 1
		};

		datasetServiceMock = $injector.get('datasetService');
		datasetServiceMock.getDataset.and.returnValue($q.resolve(serviceDataset));
		datasetServiceMock.getColumns.and.returnValue($q.resolve(columns));
		datasetServiceMock.getObservationTableUrl.and.returnValue('');

		TrialManagerDataServiceMock = $injector.get('TrialManagerDataService');
		TrialManagerDataServiceMock.extractSettings.and.returnValue(extractedSettings);

		controller = $controller('SubObservationSetCtrl', {
			$scope: scope,
			$rootScope: rootScopeMock,
			TrialManagerDataService: TrialManagerDataServiceMock,
			$stateParams: $stateParamsMock,
			dTOptionsBuilder: dTOptionsBuilderMock,
			dTColumnBuilder: dTColumnBuilderMock,
			$http: $httpMock,
			$q: _$q_,
			$compile: $compileMock,
			environmentService: environmentServiceMock,
			datasetService: datasetServiceMock,
			$timeout: $timeout
		});

		scope.$apply();

	}));

	describe('initialization:', function () {
		describe('a dataset', function () {
			it('should have instances', function () {
				expect(scope.environments).toBeTruthy();
				expect(scope.environments.length).toBeTruthy();
			});
		});
	});

	// TODO continue

});
