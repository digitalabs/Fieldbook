/**
 * Created by cyrus on 7/2/14.
 */

/*global angular, displayStudyGermplasmSection*/


(function () {
    'use strict';

    angular.module('manageTrialApp').controller('GermplasmCtrl',
        ['$scope', 'TrialManagerDataService', function ($scope, TrialManagerDataService) {

            $scope.settings = TrialManagerDataService.settings.germplasm;

            $scope.labels = {};
            $scope.labels.germplasmFactors = {
                label: 'Temp label here',
                placeholderLabel: 'Temp placeholder here'
            };

            $scope.addVariable = !TrialManagerDataService.trialMeasurement.hasMeasurement;
            displayStudyGermplasmSection(TrialManagerDataService.trialMeasurement.hasMeasurement,
                TrialManagerDataService.trialMeasurement.count);

            $scope.updateOccurred = false;

            $scope.$on('deleteOccurred', function () {
                $scope.updateOccurred = true;
            });

            $scope.$on('variableAdded', function () {
                $scope.updateOccurred = true;
            });

            $scope.$watch(function () {
                return TrialManagerDataService.settings.germplasm;
            }, function (newValue) {
                if ($scope.settings !== newValue) {
                    angular.copy(newValue, $scope.settings);
                }
            });

            $scope.updateDataTable = function () {
                $.ajax({
                    url: '/Fieldbook/ListManager/GermplasmList/refreshListDetails',
                    type: 'GET',
                    cache: false,
                    data: ''
                }).success(function (html) {
                    $('#imported-germplasm-list').html(html);
                    $('#entries-details').css('display', 'block');
                    $('#numberOfEntries').html($('#totalGermplasms').val());
                    $('#imported-germplasm-list-reset-button').css('opacity', '1');
                    $scope.updateOccurred = false;

                    if (!$scope.$$phase) {
                        $scope.$apply();
                    }

                });


            };
        }]);
})();

// README IMPORTANT: Code unmanaged by angular should go here

/* This will be called when germplasm details page is loaded */
(function () {
    'use strict';

    document.onLoadGermplasmDetails = function () {


        displayGermplasmListTree('germplasmTree', false, 0, function (node, event) {
            var currentFolderName = node.data.title;
            $(getDisplayedModalSelector() + ' #newGermplasmFolderName').val(currentFolderName);
        });

        changeBrowseGermplasmButtonBehavior(false);

        $('#listTreeModal').on('hide.bs.modal', function () {
            $('#' + getDisplayedTreeName()).dynatree('getTree').reloadStudyTree();
            changeBrowseGermplasmButtonBehavior(false);
            $(getDisplayedModalSelector() + ' #addGermplasmFolderDiv').hide();
            $(getDisplayedModalSelector() + ' #renameGermplasmFolderDiv').hide();
        });


        $('#manageCheckTypesModal').on('hidden.bs.modal', function () {
            reloadCheckTypeDropDown(false, 'checklist-select');
        });

        initializeCheckTypeSelect2(document.checkTypes, [], false, 0, 'comboCheckCode');
        $('#updateCheckTypes').hide();
        $('#deleteCheckTypes').hide();
        $('.show-germplasm-details').on('click', showGermplasmDetailsSection);
        //displayEditFactorsAndGermplasmSection();
    };

})();