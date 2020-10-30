/*global angular */

(function () {
    'use strict';

    var manageTrialAppModule = angular.module('manageTrialApp');

    manageTrialAppModule.factory('studyEntryService', ['$http', 'serviceUtilities', '$uibModal', 'studyContext', function ($http, serviceUtilities, $uibModal, studyContext) {

		var BASE_PROGRAM_URL = '/bmsapi/crops/' + studyContext.cropName + '/programs/' + studyContext.programId;
        var BASE_STUDY_URL = BASE_PROGRAM_URL + '/studies/';

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
			var request = $http.get(BASE_PROGRAM_URL + '/entry-types');
			return request.then(successHandler, failureHandler);
		}

		studyEntryService.saveStudyEntries = function(listId, entryTypeId, itemIds) {
			var request = $http.post(BASE_STUDY_URL + studyContext.studyId + '/entries/generation/', {
				"entryTypeId": entryTypeId,
				"listId": listId,
				"searchComposite": {
					"itemIds": itemIds,
					"searchRequest": null
				}
			});

			return request.then(successHandler, failureHandler);
        }

		studyEntryService.deleteEntries = function() {
			var request = $http.delete(BASE_STUDY_URL + studyContext.studyId + '/entries');
			return request.then(successHandler, failureHandler);
		}

		studyEntryService.updateStudyEntryProperty = function (entryId, newValue, studyEntryPropertyId, variableId) {
			var request = $http.put(BASE_STUDY_URL + studyContext.studyId + '/entries/' + entryId + '/properties/' + variableId, {
				"studyEntryPropertyId": studyEntryPropertyId,
				"value": newValue,
				"variableId": variableId
			});
			return request.then(successHandler, failureHandler);
		}

		studyEntryService.getStudyEntriesMetadata = function () {
			var request = $http.get(BASE_STUDY_URL + studyContext.studyId + '/entries/metadata');
			return request.then(successHandler, failureHandler);
		};

        return studyEntryService;

    }
    ]);


})();
