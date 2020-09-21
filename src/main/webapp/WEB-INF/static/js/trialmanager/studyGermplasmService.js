/*global angular */

(function () {
    'use strict';

    var manageTrialAppModule = angular.module('manageTrialApp');

    manageTrialAppModule.factory('studyGermplasmService', ['$http', 'serviceUtilities', '$uibModal', 'studyContext', function ($http, serviceUtilities, $uibModal, studyContext) {

        var BASE_STUDY_URL = '/bmsapi/crops/' + studyContext.cropName + '/programs/' + studyContext.programId + '/studies/';

        var selectedEntries = [];
        var studyGermplasmService = {};

        var successHandler = serviceUtilities.restSuccessHandler,
            failureHandler = serviceUtilities.restFailureHandler;

        studyGermplasmService.replaceStudyGermplasm = function (entryId, newGid) {
            var request = $http.put(BASE_STUDY_URL + studyContext.studyId + '/entries/' + entryId,
                {
                    germplasmId: newGid
                });
            return request.then(successHandler, failureHandler);
        };


        studyGermplasmService.toggleSelect = function (entryId) {
            var idx = selectedEntries.indexOf(entryId);
            if (idx > -1) {
                selectedEntries.splice(idx, 1)
            } else {
                selectedEntries.push(entryId);
            }
        };

        studyGermplasmService.getSelectedEntries = function() {
            return selectedEntries;
        };

        studyGermplasmService.resetSelectedEntries = function() {
            selectedEntries = [];
        };

        studyGermplasmService.openReplaceGermplasmModal = function() {
            $uibModal.open({
                templateUrl: '/Fieldbook/static/angular-templates/germplasm/replaceGermplasm.html',
                controller: "replaceGermplasmCtrl",
                size: 'md'
            });
        };

        return studyGermplasmService;

    }
    ]);


})();
