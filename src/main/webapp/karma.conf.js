// Karma configuration
module.exports = function (config) {
	'use strict';
	config.set({

		// base path that will be used to resolve all patterns (eg. files, exclude)
		basePath: '',

		// frameworks to use
		// available frameworks: https://npmjs.org/browse/keyword/karma-adapter
		frameworks: ['jasmine'],

		// list of files / patterns to load in the browser
		files: [

			'WEB-INF/static/js/lib/jquery/jquery-1.11.3.js',
			'WEB-INF/static/js/angular-tests/mocks/globals-mock.js',

			// AngularJS plugins and dependencies
			'WEB-INF/static/js/lib/angular/angular.min.js',
			'WEB-INF/static/js/lib/angular/angular-local-storage.min.js',
			'WEB-INF/static/js/lib/angular/angular-resource.min.js',
			'WEB-INF/static/js/lib/angular/angular-route.min.js',
			'WEB-INF/static/js/lib/angular/angular-sanitize.js',
			'WEB-INF/static/js/lib/angular/angular-select2.js',
			'WEB-INF/static/js/lib/angular/angular-ui-router.min.js',
			'WEB-INF/static/js/lib/angular/ct-ui-router-extras.min.js',
			'WEB-INF/static/js/lib/angular/ng-lodash.min.js',
			'WEB-INF/static/js/lib/angular/sanitize.js',
			'WEB-INF/static/js/lib/angular/select.js',
			'WEB-INF/static/js/lib/angular/sortable.js',
			'WEB-INF/static/js/lib/angular/ui-bootstrap-tpls-0.11.0.js',
			'WEB-INF/static/js/lib/angular/ui-bootstrap-tpls-0.14.3.js',
			'WEB-INF/static/js/lib/datatable/jquery.dataTables.js',
			'WEB-INF/static/js/lib/datatable/angular-datatables.js',
			'WEB-INF/static/js/lib/datatable/angular-datatables.buttons.js',
			'WEB-INF/static/js/lib/datatable/angular-datatables.fixedcolumns.js',
			'WEB-INF/static/js/lib/datatable/buttons.colVis.min.js',
			'WEB-INF/static/js/lib/datatable/dataTables.bootstrap.js',
			'WEB-INF/static/js/lib/datatable/dataTables.buttons.min.js',
			'WEB-INF/static/js/lib/datatable/dataTables.colReorder.js',
			'WEB-INF/static/js/lib/datatable/dataTables.colResize.js',
			'WEB-INF/static/js/lib/datatable/dataTables.colVis.js',
			'WEB-INF/static/js/lib/datatable/dataTables.fixedColumns.js',
			'WEB-INF/static/js/lib/datatable/dataTables.scroller.js',

			// Modules to test
			'WEB-INF/static/js/angular/angular-utilities.js',
			'WEB-INF/static/js/angular/fieldbook-utils.js',
			'WEB-INF/static/js/trialmanager/manageTrial.js',
			'WEB-INF/static/js/trialmanager/selectEnvironmentToSampleList.js',
			'WEB-INF/static/js/trialmanager/trial-settings-manager.js',
			'WEB-INF/static/js/trialmanager/selectEnvironmentModal.js',
			'WEB-INF/static/js/trialmanager/studyStateService.js',
			'WEB-INF/static/js/trialmanager/environmentService.js',
			'WEB-INF/static/js/trialmanager/executeCalculatedVariableModal.js',
			'WEB-INF/static/js/trialmanager/trial-data-manager.js',
			'WEB-INF/static/js/trialmanager/saveSampleList.js',
			'WEB-INF/static/js/trialmanager/exportSampleList.js',
			'WEB-INF/static/js/trialmanager/trialSettings.js',
			'WEB-INF/static/js/trialmanager/treatment.js',
			'WEB-INF/static/js/trialmanager/environments.js',
			'WEB-INF/static/js/trialmanager/selectSelectionVariableToSampleList.js',
			'WEB-INF/static/js/trialmanager/experimentalDesign.js',
			'WEB-INF/static/js/trialmanager/measurement.js',
			'WEB-INF/static/js/trialmanager/showSettingFormElementNew.js',
			'WEB-INF/static/js/trialmanager/germplasm.js',
			'WEB-INF/static/js/trialmanager/datasetService.js',
			'WEB-INF/static/js/design-import/design-import-main.js',

			// unit test files
			'WEB-INF/static/js/angular-tests/lib/angular-mocks.js',
			'WEB-INF/static/js/angular-tests/trialmanager/*.spec.js'
		],
		exclude: [],
		preprocessors: {},
		reporters: ['dots'],
		port: 9876,
		colors: false,
		logLevel: config.LOG_INFO,
		autoWatch: false,
		browsers: ['PhantomJS'],
		singleRun: true
	});
};