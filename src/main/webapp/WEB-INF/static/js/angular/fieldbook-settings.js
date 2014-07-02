/*global angular*/
/*global showBaselineTraitDetailsModal */

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
                    if ($scope.settings[setting.variable.cvTermId]) {
                        delete $scope.settings[setting.variable.cvTermId];
                    }
                };

                $scope.showDetailsModal = function(setting) {
                    // this function is currently defined in the fieldbook-common.js, loaded globally for the page
                    // TODO : move away from global function definitions
                    showBaselineTraitDetailsModal(setting.variable.cvTermId);
                };
            }
        };
    })
    .directive('selectStandardVariable', function() {
        'use strict';

        return {
            restrict : 'A',
            scope : {
                modeldata : '=',
                labels : '='
            },

            controller : function($scope, $element, $attrs,ONTOLOGY_TREE_ID) {
                $scope.processModalData = function (data) {
                    if (data) {
                        // if retrieved data is an array of values
                        if (data.length && data.length > 0) {
                            $.each(data, function (key, value) {
                                $scope.modeldata[value.variable.cvTermId] = value;
                            });
                        } else {
                            // if retrieved data is a single object
                            $scope.modeldata[data.variable.cvTermId] = data;
                        }

                        if (!$scope.$$phase) {
                            $scope.$apply();
                        }

                    }
                };

                $element.on('click',  function() {
                    // TODO change modal such that it no longer requires id / class-based DOM manipulation
                    // FIXME
                    window.ChooseSettings.getStandardVariables($scope.labels, $attrs.variableType,
                        ONTOLOGY_TREE_ID, $scope.processModalData);

                });
            }
        };
    })
    .directive('showSettingFormElement', function() {
        'use strict';

        return {
            restrict : 'E',
            scope : {
                settings : '=',
                targetkey : '@targetkey',
                valuecontainer : '='
            },

            templateUrl:  '/Fieldbook/static/angular-templates/showSettingFormElement.html',
            controller : function($scope) {
                $scope.variableDefinition = $scope.settings[$scope.targetkey];
                $scope.hasDropdownOptions = $scope.variableDefinition.possibleValues && $scope.variableDefinition.possibleValues.length > 0;

                if ($scope.hasDropdownOptions) {
                    $scope.dropdownOptions = {
                        data: function () {
                            return {results: $scope.variableDefinition.possibleValues};
                        },
                        formatResult: function (value) {
                            if (value.id !== undefined) {
                                return value.description;
                            } else if (value.locid !== undefined) {
                                return value.lname;
                            } else {
                                return value.mname;
                            }
                        },
                        formatSelection: function (value) {
                            if (value.id !== undefined) {
                                return value.description;
                            } else if (value.locid !== undefined) {
                                return value.lname;
                            } else {
                                return value.mname;
                            }
                        }

                    };
                }
            }
        };
    });