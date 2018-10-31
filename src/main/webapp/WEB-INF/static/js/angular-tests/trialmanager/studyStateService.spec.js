'use strict';

describe('Study State Service', function() {

    var service;

    beforeEach(function() {

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

    beforeEach(function() {
        inject(function(studyStateService) {
            service = studyStateService;
        });
    });

    describe('updateOccurred', function() {

        it('should update hasUnsavedData to true', function() {

            service.updateOccurred();

            expect(service.hasUnsavedData()).toEqual(true);

        });

    });

    describe('resetState', function() {

        it('should reset hasUnsavedData value to false', function() {

            service.updateOccurred();
            service.resetState();

            expect(service.hasUnsavedData()).toEqual(false);

        });

    });

});