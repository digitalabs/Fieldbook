/*globals angular, alert */
/*global showBaselineTraitDetailsModal */
/*global ChooseSettings */
angular.module('fieldbook-settings', [])
    .constant('ONTOLOGY_TREE_ID', 'ontologyBrowserTree')
    .directive('displaySettings', function() {
        'use strict';

        return {
            restrict : 'E',
            scope : {
                settings : '='
            },
            templateUrl : '/Fieldbook/static/angular-templates/displaySettings.html',
            controller : function($scope) {
                $scope.removeSetting = function(setting) {
                    if ($scope.settings[setting.id]) {
                        delete $scope.settings[setting.id];
                    }
                };

                $scope.showDetailsModal = function(setting) {
                    // this function is currently defined in the fieldbook-common.js, loaded globally for the page
                    // TODO : move away from global function definitions
                    showBaselineTraitDetailsModal(setting.id);
                };
            }
        };
    })
    .directive('selectStandardVariable', function() {
        'use strict';

        return {
            restrict : 'A',
            scope : {
                dataModel : '=',
                labels : '@labels'
            },

            controller : function($scope, $element, $attrs,ONTOLOGY_TREE_ID) {
                $scope.promise = null;

                $scope.handleModalDisplay = function() {
                    alert('here');
                };

                $element.on('click',  function() {
                    // TODO change modal such that it no longer requires id / class-based DOM manipulation
                    $('.nrm-vs-modal .fbk-modal-title').text($scope.labels.label);
                    $('.nrm-vs-modal .nrm-vs-hint-placeholder').html($scope.labels.placeholderLabel);

                    $('#ontology-detail-tabs').empty().html($('.variable-detail-info').html());
                    $('#variable-details').html('');

                    // FIXME
                    window.ChooseSettings.getStandardVariables($attrs.variableType, ONTOLOGY_TREE_ID, $scope.handleModalDisplay);

                });
            }
        };
    });
