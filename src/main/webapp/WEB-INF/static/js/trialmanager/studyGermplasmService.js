/*global angular */

(function () {
    'use strict';

    var manageTrialAppModule = angular.module('manageTrialApp');

    manageTrialAppModule.factory('studyGermplasmService', ['$http', 'serviceUtilities', '$uibModal', 'studyContext', function ($http, serviceUtilities, $uibModal, studyContext) {

        var BASE_STUDY_URL = '/bmsapi/crops/' + studyContext.cropName + '/programs/' + studyContext.programId + '/studies/';

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

		studyGermplasmService.getStudyEntries = function() {
			return BASE_STUDY_URL + studyContext.studyId + '/entries';
        }

		studyGermplasmService.getEntryTableColumns = function() {
			var request = $http.get(BASE_STUDY_URL + studyContext.studyId + '/entries/table/columns');
			return request.then(successHandler, failureHandler);
		}

		studyGermplasmService.getEntryTypes = function() {
			var request = $http.get(BASE_STUDY_URL + studyContext.studyId + '/entryTypes');
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
			var request = $http.get(BASE_STUDY_URL + studyContext.studyId + '/hasStudyEntries');
			return request.then(successHandler, failureHandler);
		}

		studyGermplasmService.updateStudyEntry = function (entryId, newValue, studyEntryPropertyId) {
			var request = $http.put(BASE_STUDY_URL + studyContext.studyId + '/entries/' + entryId + '/properties/' + 8255, {
				"studyEntryPropertyId": studyEntryPropertyId,
				"value": newValue,
				"variableId": 8255
			});
			return request.then(successHandler, failureHandler);
		}

		studyGermplasmService.addOrUpdateStudyEntryType = function (studyEntryType) {
			var request = $http.put(BASE_STUDY_URL + studyContext.studyId + '/entryTypes/addOrUpdate/', studyEntryType);
			return request.then(successHandler, failureHandler);
		}

		studyGermplasmService.deleteStudyEntryType = function (studyEntryTypeId) {
			var request = $http.delete(BASE_STUDY_URL + studyContext.studyId + '/entryTypes/delete/' + studyEntryTypeId);
			return request.then(successHandler, failureHandler);
		}

		studyGermplasmService.isStudyEntryTypeUsed = function (studyEntryTypeId) {
			var request = $http.get(BASE_STUDY_URL + studyContext.studyId + '/entryTypes/isUsed/' + studyEntryTypeId);
			return request.then(successHandler, failureHandler);
		}

        return studyGermplasmService;

    }
    ]);


})();
