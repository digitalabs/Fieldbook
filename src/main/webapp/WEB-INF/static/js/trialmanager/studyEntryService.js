/*global angular */

(function () {
    'use strict';

    var manageTrialAppModule = angular.module('manageTrialApp');

    manageTrialAppModule.factory('studyEntryService', ['$http', 'serviceUtilities', '$uibModal', 'studyContext', function ($http, serviceUtilities, $uibModal, studyContext) {

        var BASE_STUDY_URL = '/bmsapi/crops/' + studyContext.cropName + '/programs/' + studyContext.programId + '/studies/';

        var studyEntryService = {};

        var successHandler = serviceUtilities.restSuccessHandler,
            failureHandler = serviceUtilities.restFailureHandler;

        studyEntryService.replaceStudyGermplasm = function (entryId, newGid) {
            var request = $http.put(BASE_STUDY_URL + studyContext.studyId + '/entries/' + entryId,
                {
                    gid: newGid
                });
            return request.then(successHandler, failureHandler);
        };

		studyEntryService.getStudyEntries = function() {
			return BASE_STUDY_URL + studyContext.studyId + '/entries';
        }

		studyEntryService.getEntryTableColumns = function() {
			var request = $http.get(BASE_STUDY_URL + studyContext.studyId + '/entries/table/columns');
			return request.then(successHandler, failureHandler);
		}

		studyEntryService.getEntryTypes = function() {
			var request = $http.get(BASE_STUDY_URL + studyContext.studyId + '/entryTypes');
			return request.then(successHandler, failureHandler);
		}

		studyEntryService.saveStudyEntries = function(listId) {
			var request = $http.post(BASE_STUDY_URL + studyContext.studyId + '/entries/generation/', {
				germplasmListId: listId
            });

			return request.then(successHandler, failureHandler);
        }

		studyEntryService.deleteEntries = function() {
			var request = $http.delete(BASE_STUDY_URL + studyContext.studyId + '/entries');
			return request.then(successHandler, failureHandler);
		}

		studyEntryService.hasStudyEntries = function() {
			var request = $http.get(BASE_STUDY_URL + studyContext.studyId + '/hasStudyEntries');
			return request.then(successHandler, failureHandler);
		}

		studyEntryService.updateStudyEntry = function (entryId, newValue, studyEntryPropertyId) {
			var request = $http.put(BASE_STUDY_URL + studyContext.studyId + '/entries/' + entryId + '/properties/' + 8255, {
				"studyEntryPropertyId": studyEntryPropertyId,
				"value": newValue,
				"variableId": 8255
			});
			return request.then(successHandler, failureHandler);
		}

		studyEntryService.addOrUpdateStudyEntryType = function (studyEntryType) {
			var request = $http.put(BASE_STUDY_URL + studyContext.studyId + '/entryTypes/addOrUpdate/', studyEntryType);
			return request.then(successHandler, failureHandler);
		}

		studyEntryService.deleteStudyEntryType = function (studyEntryTypeId) {
			var request = $http.delete(BASE_STUDY_URL + studyContext.studyId + '/entryTypes/delete/' + studyEntryTypeId);
			return request.then(successHandler, failureHandler);
		}

		studyEntryService.isStudyEntryTypeUsed = function (studyEntryTypeId) {
			var request = $http.get(BASE_STUDY_URL + studyContext.studyId + '/entryTypes/isUsed/' + studyEntryTypeId);
			return request.then(successHandler, failureHandler);
		}

        return studyEntryService;

    }
    ]);


})();
