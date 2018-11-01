/*global angular*/
'use strict';

(function() {
	var app = angular.module('config', []);

	app.service('configService', function() {

		var cropName = '',
			programId = '',
			selectedProjectId = '',
			authToken = '',
			loggedInUserId = '',
			selectedStudyId ='';

		return {
			setCropName: function(crop) {
				cropName = crop;
			},

			getCropName: function() {
				return cropName;
			},

			setProgramId: function(id) {
				programId = id;
			},

			getProgramId: function() {
				return programId;
			},

			setSelectedProjectId: function(projectId) {
				selectedProjectId = projectId;
			},

			getSelectedProjectId: function() {
				return selectedProjectId;
			},

			setAuthToken: function(token) {
				authToken = token;
			},

			getAuthToken: function() {
				return authToken;
			},

			setLoggedInUserId: function(userId) {
				loggedInUserId = userId;
			},

			getLoggedInUserId: function() {
				return loggedInUserId;
			},
			setStudyId: function(studyId){
				selectedStudyId = studyId;
			},
			getStudyId: function(){
				return selectedStudyId;
			}
		};
	});

	app.directive('smConfig', ['configService', function(configService) {
		return {
			restrict: 'A',
			scope: {
				cropName: '@smCrop',
				programId: '@smProgramId',
				selectedProjectId: '@smSelectedProjectId',
				authToken: '@smAuthToken',
				loggedInUserId: '@smLoggedInUserId',
				selectedStudyId: '@smSelectedStudyId'
			},
			link: function(scope, element, attrs) {
				configService.setCropName(attrs.smCrop);
				configService.setProgramId(attrs.smProgramId);
				configService.setSelectedProjectId(attrs.smSelectedProjectId);
				configService.setAuthToken(attrs.smAuthToken);
				configService.setLoggedInUserId(attrs.smLoggedInUserId);
				configService.setStudyId(attrs.smSelectedStudyId);
			}
		};
	}]);
}());
