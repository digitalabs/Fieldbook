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
                url: '/Fieldbook/TrialManager/GermplasmList/refreshListDetails',
                type: 'POST',
                cache: false,
                data: JSON.stringify(forSubmit),
                contentType: 'application/json',
                success: function (html) {
                	$('#imported-germplasm-list').html(html);
					$('#entries-details').css('display', 'block');
					$('#numberOfEntries').html($('#totalGermplasms').val());
					$('#imported-germplasm-list-reset-button').css('opacity', '1');
                }

            });
        };
    }]);
})();