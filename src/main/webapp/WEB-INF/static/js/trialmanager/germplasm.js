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

        $scope.updateDataTable = function() {
            var forSubmit = [];

            $.each($scope.data.settings, function(key, value) {
                forSubmit.push({
                    termId : value.variable.cvTermId + '',
                    termName : value.variable.name
                });
            });

            $.ajax({
                url: '/Fieldbook/manageSettings/refreshTrial',
                type: 'POST',
                cache: false,
                data: JSON.stringify(forSubmit),
                contentType: 'application/json',
                success: function (data) {
                    // evaluate data table
                }

            });
        };
    }]);
})();