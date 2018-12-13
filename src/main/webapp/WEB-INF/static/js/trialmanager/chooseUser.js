/*global angular*/

(function() {
	'use strict';

	var manageTrialApp = angular.module('manageTrialApp');
	
	manageTrialApp.controller('selectUserCtrl', ['$scope', '$http', '_', function($scope, $http, _) {
        $scope.currentProgramMembers = [];

        $http.get('/Fieldbook/crosses/getCurrentUser', {timeout: 3000}).success(function(data) {
            $scope.selectedUserId = data;
        }).error(function() {
            showErrorMessage('', $.fieldbookMessages.errorNoCurrentUser);
        });

        $http.get('/Fieldbook/crosses/getCurrentProgramMembers', {timeout: 3000}).success(function(data) {
            $scope.currentProgramMembers = data;
            if (!$scope.selectedUserId) {
                $scope.selectedUserId = _.keys($scope.currentProgramMembers[0]);
            }
        }).error(function() {
            showErrorMessage('', $.fieldbookMessages.errorNoProgramMembers);
        });

        $scope.saveSelectedUser = function() {
            $http.post('/Fieldbook/crosses/submitListOwner', $scope.selectedUserId, {timeout: 3000}).success(function(data) {
                if (data.isSuccess === 0) {
                    showErrorMessage('', data.error);
                }
            }).error(function() {
                showErrorMessage('', $.fieldbookMessages.errorSubmittingListOwner);
            });
        };
    }]);

})();
