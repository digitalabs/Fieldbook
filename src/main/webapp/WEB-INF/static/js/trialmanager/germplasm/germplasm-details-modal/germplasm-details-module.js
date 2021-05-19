(function () {
    'use strict';
    const germplasmDetailsModule = angular.module('germplasmDetailsModule', ['ui.bootstrap']);
    germplasmDetailsModule.factory('germplasmDetailsModalService', ['$uibModal', '$rootScope', function ($uibModal, $rootScope) {
        const germplasmDetailsModalService = {};
        germplasmDetailsModalService.openGermplasmDetailsModal = function (gid, designation, callBackFunction) {
            germplasmDetailsModalService.modal = $uibModal.open({
                templateUrl: '/Fieldbook/static/js/trialmanager/germplasm/germplasm-details-modal/germplasm-details-modal.html',
                controller: function ($scope, $uibModalInstance, germplasmDetailsService, studyContext) {

                    const germplasmDetailsURL = '/ibpworkbench/main/app/#/germplasm-details/' + gid + '?cropName=' + studyContext.cropName + '&programUUID=' + studyContext.programId
                        + '&modal=true&authToken=' + JSON.parse(localStorage["bms.xAuthToken"]).token;
                    $scope.url = germplasmDetailsURL;
                    $scope.designation = designation;
                    $scope.gid = gid;

                    $scope.close = function () {
                        $uibModalInstance.close(null);
                    };

                    $rootScope.$on("reloadGermplasmDetailsModal", function (event) {
                        $scope.updateModal();
                    });

                    $scope.updateModal = function () {
                        germplasmDetailsService.getGermplasmByGid($scope.gid).then(function (response) {
                            const germplasm = response.data;
                            $scope.designation = germplasm.preferredName;
                        });
                    }
                },
                windowClass: 'modal-extra-large',
            }).result.finally(function() {
                setTimeout(function() {
                    if (callBackFunction) {
                        callBackFunction();
                    }
                }, 200);

            });
        };

        germplasmDetailsModalService.updateGermplasmDetailsModal = function () {
            $rootScope.$broadcast('reloadGermplasmDetailsModal');
        }
        return germplasmDetailsModalService;
    }]);

    germplasmDetailsModule.factory('germplasmDetailsService', ['studyContext', '$http',function (studyContext, $http) {
        const BASE_CROP_URL = '/bmsapi/crops/' + studyContext.cropName + '/germplasm/';
        const germplasmService = {};
        germplasmService.getGermplasmByGid = function (gid) {
            return $http.get(BASE_CROP_URL + gid);
        };
        return germplasmService;
    }]);
})();