(function (){
    'use strict';

    const studyInfoApp = angular.module('studyInfoApp',['germplasmDetailsModule', 'auth', 'bmsAuth']);
    studyInfoApp.config(['$httpProvider', function ($httpProvider) {
        $httpProvider.interceptors.push('authInterceptor');
        $httpProvider.interceptors.push('authExpiredInterceptor');
    }]);

    studyInfoApp.controller('StudyInfoCtrl', ['studyContext', '$scope', '$window', 'germplasmDetailsModalService', function (studyContext, $scope, $window, germplasmDetailsModalService) {
        $window.addEventListener("message", (event) => {
            if (event.data === 'germplasm-details-changed') {
                germplasmDetailsModalService.updateGermplasmDetailsModal();
            }
        }, false);
    }]);

    studyInfoApp.config(['localStorageServiceProvider', function (localStorageServiceProvider) {
        localStorageServiceProvider.setPrefix('bms');
    }]);

    studyInfoApp.filter('trustAsResourceUrl', ['$sce', function($sce) {
        return function(val) {
            return $sce.trustAsResourceUrl(val);
        };
    }]);
})();