/**
 * Created by cyrus on 7/2/14.
 */

/*global angular, displayStudyGermplasmSection, openListTree*/


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

            
            
            $scope.trialMeasurement = TrialManagerDataService.trialMeasurement;

            displayStudyGermplasmSection(TrialManagerDataService.trialMeasurement.hasMeasurement,
                TrialManagerDataService.trialMeasurement.count);

            $scope.updateOccurred = false;

            $scope.$on('deleteOccurred', function () {
                $scope.updateOccurred = true;
            });

            $scope.$on('variableAdded', function () {
                $scope.updateOccurred = true;
            });

            $scope.handleSaveEvent = function() {
                $scope.updateOccurred = false;

                TrialManagerDataService.specialSettings.experimentalDesign.germplasmTotalListCount = $scope.getTotalListNo();
            };

            // function called whenever the user has successfully selected a germplasm list
            $scope.germplasmListSelected = function() {
                TrialManagerDataService.indicateUnappliedChangesAvailable(true);
            };

            $(document).on('germplasmListUpdated', function() {
                var entryHtml = $('#numberOfEntries').html();

                TrialManagerDataService.trialMeasurement.count = parseInt(entryHtml, 10);
            });

            $scope.germplasmListCleared = function() {
                TrialManagerDataService.clearUnappliedChangesFlag();
                TrialManagerDataService.trialMeasurement.count = 0;
            };

            $scope.openGermplasmTree = function() {
                openListTree(1, $scope.germplasmListSelected);
            };

            TrialManagerDataService.registerSaveListener('germplasmUpdate', $scope.handleSaveEvent);

            $scope.displayUpdateButton = function() {
                return $scope.updateOccurred && $scope.listAvailable();
            };

            $scope.listAvailable = function() {
                var entryHtml = $('#numberOfEntries').html();
                return (entryHtml !== '' && parseInt(entryHtml,10) > 0);
            };

            $scope.getTotalListNo = function() {
                return (parseInt($('#totalGermplasms').val())) ? parseInt($('#totalGermplasms').val()) : 0;
            };

            $scope.updateSettings = function(newValue) {
                angular.copy(newValue, $scope.settings);
                $scope.updateOccurred = true;
            };

            TrialManagerDataService.registerSetting('germplasm', $scope.updateSettings);

            /*$scope.$watch(function () {
                return TrialManagerDataService.settings.germplasm;
            }, function (newValue) {
                if ($scope.settings !== newValue) {
                    angular.copy(newValue, $scope.settings);
                    $scope.updateOccurred = true;
                }
            });*/

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

                    TrialManagerDataService.specialSettings.experimentalDesign.germplasmTotalListCount = $scope.getTotalListNo();

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


        // this is the handler for when user clicks on the Replace button
        $('.show-germplasm-details').on('click', function() {
            showGermplasmDetailsSection();

            $('#imported-germplasm-list').html('<H3></H3>');
            $('#imported-germplasm-list').show();
            $('#entries-details').hide();
        });
        //displayEditFactorsAndGermplasmSection();
    };

})();