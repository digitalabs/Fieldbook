/*global angular*/
/*global showBaselineTraitDetailsModal */

(function() {
    'use strict';

    angular.module('fieldbook-utils', ['ui.select2'])
        .constant('ONTOLOGY_TREE_ID', 'ontologyBrowserTree')
        .directive('displaySettings', function() {
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

                    $scope.size = function() {
                        var size = 0, key;
                        for (key in $scope.settings) {
                            if ($scope.settings.hasOwnProperty(key)) { size++; }
                        }
                        return size;
                    };
                }
            };
        })
        .directive('validNumber', function() {

            return {
                require: '?ngModel',
                link: function(scope, element, attrs, ngModelCtrl) {
                    if(!ngModelCtrl) {
                        return;
                    }

                    ngModelCtrl.$parsers.push(function(val) {
                        var clean = val.replace( /[^0-9]+/g, '');
                        if (val !== clean) {
                            ngModelCtrl.$setViewValue(clean);
                            ngModelCtrl.$render();
                        }
                        return clean;
                    });

                    element.bind('keypress', function(event) {
                        if(event.keyCode === 32) {
                            event.preventDefault();
                        }
                    });
                }
            };
        })
        .directive('selectStandardVariable', function() {

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
        .directive('openOntologyBrowserPopup', function() {
            // FIXME : Note this also retrives the possible_values of the selected variables, modify this as not all screens need this particular variable.

            return {
                restrict : 'A',
                scope : {
                    onSelectVariables : '&'
                },

                controller : function($scope, $element, $attrs,ONTOLOGY_TREE_ID) {
                    $scope.processModalData = function (data) {
                        var resultData = {};
                        if (data) {
                            // if retrieved data is an array of values
                            if (data.length && data.length > 0) {
                                $.each(data, function (key, value) {
                                    resultData[value.variable.cvTermId] = value;
                                });
                            } else {
                                // if retrieved data is a single object
                                resultData[data.variable.cvTermId] = data;
                            }

                            // perform the passed function
                            $scope.onSelectVariables({ result : resultData });

                            if (!$scope.$$phase) {
                                $scope.$apply();
                            }

                        }
                    };

                    $element.on('click',  function() {
                        var labels = { 'label' : $attrs.label, 'placeholderLabel' : $attrs.placeholder };

                        // TODO change modal such that it no longer requires id / class-based DOM manipulation
                        // FIXME
                        window.ChooseSettings.getStandardVariables(labels, $attrs.variableType,
                            ONTOLOGY_TREE_ID, $scope.processModalData);

                    });
                }
            };
        })
        .directive('showSettingFormElement', function() {
            return {
                require: '?uiSelect2',
                restrict : 'E',
                scope : {
                    settings : '=',
                    targetkey : '@targetkey',
                    valuecontainer : '='
                },

                templateUrl:  '/Fieldbook/static/angular-templates/showSettingFormElement.html',
                compile : function(tElement, tAttrs, transclude, uiSelect2) {
                    if (uiSelect2) {
                        uiSelect2.compile(tElement, tAttrs);
                    }
                },
                controller : function($scope, LOCATION_ID, BREEDING_METHOD_ID, BREEDING_METHOD_CODE) {
                    $scope.variableDefinition = $scope.settings[$scope.targetkey];
                    $scope.hasDropdownOptions = $scope.variableDefinition.variable.widgetType === 'DROPDOWN';

                    $scope.isLocation = $scope.variableDefinition.variable.cvTermId == LOCATION_ID;

                    $scope.isBreedingMethod = ($scope.variableDefinition.variable.cvTermId == BREEDING_METHOD_ID ||
                        $scope.variableDefinition.variable.cvTermId == BREEDING_METHOD_CODE);

                    $scope.localData = {};
                    $scope.localData.useFavorites = false;

                    $scope.updateDropdownValues = function() {
                        if ($scope.localData.useFavorites) {
                            $scope.dropdownValues = $scope.variableDefinition.possibleValuesFavorite;
                        } else {
                            $scope.dropdownValues = $scope.variableDefinition.possibleValues;
                        }
                    };

                    if ($scope.hasDropdownOptions) {
                        $scope.dropdownValues = $scope.variableDefinition.possibleValues;

                        $scope.computeMinimumSearchResults = function() {
                            return ($scope.dropdownValues.length > 0) ? 20 : -1;
                        };

                        $scope.dropdownOptions = {
                            data: function () {
                                return {results: $scope.dropdownValues};
                            },
                            formatResult: function (value) {
                                return value.name;
                            },
                            formatSelection: function (value) {
                                return value.name;
                            },
                            minimumResultsForSearch : $scope.computeMinimumSearchResults(),
                            query : function (query) {
                                var data = {
                                    results: $scope.dropdownValues
                                };

                                // return the array that matches
                                data.results = $.grep(data.results, function (item, index) {
                                    return ($.fn.select2.defaults.matcher(query.term,
                                        item.name));

                                });
                                /*
                                 * if (data.results.length === 0){
                                 * data.results.unshift({id:query.term,text:query.term}); }
                                 */
                                query.callback(data);
                            }

                        };
                    }
                }
            };
        })

        // filters

        .filter('range', function() {
            return function(input, total) {
                total = parseInt(total);
                for (var i=0; i<total; i++) { input.push(i); }

                return input;
            };
        });

})();