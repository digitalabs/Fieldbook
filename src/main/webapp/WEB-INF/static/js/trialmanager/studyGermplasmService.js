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
                    gid: newGid
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

		studyGermplasmService.getStudyEntries = function() {
			return BASE_STUDY_URL + studyContext.studyId + '/entries';
        }

		studyGermplasmService.getEntryTableColumns = function() {
			var request = $http.get(BASE_STUDY_URL + studyContext.studyId + '/entries/table/columns');
			return request.then(successHandler, failureHandler);
		}

		studyGermplasmService.saveStudyEntries = function(listId) {
			var request = $http.post(BASE_STUDY_URL + studyContext.studyId + '/entries/generation/', {
				germplasmListId: listId
            });

			return request.then(successHandler, failureHandler);
        }

		studyGermplasmService.deleteEntries = function() {
			var request = $http.delete(BASE_STUDY_URL + studyContext.studyId + '/entries');
			return request.then(successHandler, failureHandler);
		}

		studyGermplasmService.hasStudyEntries = function() {
			var request = $http.delete(BASE_STUDY_URL + studyContext.studyId + '/hasStudyEntries');
			return request.then(successHandler, failureHandler);
		}


        return studyGermplasmService;

    }
    ]);


})();
