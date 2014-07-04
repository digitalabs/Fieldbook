/**
 * Created by cyrus on 7/2/14.
 */

/*global angular*/


(function(){
    'use strict';

    angular.module('manageTrialApp').controller('GermplasmCtrl', ['$scope', function($scope) {

        $scope.data = {};
        $scope.data.settings = {};

        $scope.labels = {};
        $scope.labels.germplasmFactors = {
            label: 'Temp label here',
            placeholderLabel: 'Temp placeholder here'
        };

        $scope.updateOccurred = false;

        $scope.$on('deleteOccurred', function() {
            $scope.updateOccurred = true;
        });

        $scope.$on('variableAdded', function() {
            $scope.updateOccurred = true;
        });

        $scope.updateDataTable = function() {
            $.ajax({
                url: '/Fieldbook/TrialManager/GermplasmList/refreshListDetails',
                type: 'POST',
                cache: false,
                data: '',
                success: function (html) {
                    $('#imported-germplasm-list').html(html);
                    $('#entries-details').css('display', 'block');
                    $('#numberOfEntries').html($('#totalGermplasms').val());
                    $('#imported-germplasm-list-reset-button').css('opacity', '1');
                    $scope.updateOccurred = false;

                    if (!$scope.$$phase) {
                        $scope.$apply();
                    }
                }

            });
        };
    }]);
})();