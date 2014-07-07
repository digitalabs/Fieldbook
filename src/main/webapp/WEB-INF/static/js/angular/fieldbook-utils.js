/*global angular*/
/*global showBaselineTraitDetailsModal */
/* global openManageLocations, ChooseSettings*/
(function() {
    'use strict';

    angular.module('fieldbook-utils', ['ui.select2'])
        .constant('VARIABLE_SELECTION_MODAL_SELECTOR', '.nrm-var-selection-modal-container')
        .constant('VARIABLE_SELECTED_EVENT_TYPE', 'nrm-variable-select')
        .directive('displaySettings', function() {
            return {
                restrict : 'E',
                scope : {
                    settings : '='
                },
                templateUrl : '/Fieldbook/static/angular-templates/displaySettings.html',
                controller : function($scope, $element, $attrs) {
                    $scope.removeSetting = function(setting) {
                        if ($scope.settings[setting.variable.cvTermId]) {
                            delete $scope.settings[setting.variable.cvTermId];

                            $.ajax({
                                url: '/Fieldbook/manageSettings/deleteVariable/' + $attrs.variableType + '/' + setting.variable.cvTermId,
                                type: 'POST',
                                cache: false,
                                data: '',
                                contentType: 'application/json',
                                success: function () {
                                }
                            });

                            $scope.$emit('deleteOccurred');
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
                    modeldata : '='
                },

                controller : function($scope, $element, $attrs, VARIABLE_SELECTION_MODAL_SELECTOR, VARIABLE_SELECTED_EVENT_TYPE) {
                    $scope.processModalData = function (data) {
                        if (data.responseData) {
                            data = data.responseData;
                        }
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

                            $scope.$emit('variableAdded');
                        }
                    };

                    $element.on('click',  function() {

                        var params = {
                            variableType : $attrs.variableType,
                            retrieveSelectedVariableFunction: function () {
                                var currentIds = [];
                                $.each($scope.modeldata, function(key) {
                                    currentIds.push(key);
                                });

                                return currentIds;
                            }
                        };

                        $(VARIABLE_SELECTION_MODAL_SELECTOR).off(VARIABLE_SELECTED_EVENT_TYPE);
                        $(VARIABLE_SELECTION_MODAL_SELECTOR).on(VARIABLE_SELECTED_EVENT_TYPE, $scope.processModalData);

                        var TrialSettingsManager = window.TrialSettingsManager;
                        var settingsManager = new TrialSettingsManager({});
                        settingsManager._openVariableSelectionDialog(params);
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
                                data.results = $.grep(data.results, function (item) {
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

                    if ($scope.isLocation) {
                        $scope.updateLocationValues = function () {
                            if (!$scope.variableDefinition.locationUpdated) {
                                $.ajax({
                                    url: '/Fieldbook/NurseryManager/advance/nursery/getLocations',
                                    type: 'GET',
                                    cache: false,
                                    data: '',
                                    success: function (data) {
                                        if (data.success === '1') {
                                            $scope.variableDefinition.locationUpdated = true;
                                            $scope.variableDefinition.possibleValues = $scope.convertLocationsToPossibleValues(
                                                $.parseJSON(data.allBreedingLocations));
                                            $scope.variableDefinition.possibleValuesFavorite = $scope.convertLocationsToPossibleValues(
                                                $.parseJSON(data.favoriteLocations));

                                            if (!$scope.$$phase) {
                                                $scope.$apply();
                                            }
                                        }
                                    }

                                });
                            }
                        };

                        $scope.convertLocationsToPossibleValues = function (locations) {
                            var possibleValues = [];

                            $.each(locations, function (key, value) {
                                possibleValues.push({
                                    id: value.locid,
                                    name: value.lname,
                                    description: value.lname
                                });
                            });

                            return possibleValues;
                        };

                        $scope.initiateManageLocationModal = function () {
                            $scope.variableDefinition.locationUpdated = false;
                            openManageLocations();
                        };

                        $(document).off('location-update');
                        $(document).on('location-update', $scope.updateLocationValues);
                    }
                }
            };
        })

        .directive("sectionContainer",function(){
            return {
                restrict: 'E',
                scope : {
                    heading : '@',
                    reminder : '@',
                    helpTooltip : '@'
                },
                transclude : true,
                templateUrl: '/Fieldbook/static/angular-templates/sectionContainer.html',
                link : function (scope,elem,attrs) {
                    scope.collapsible = scope.$eval(attrs.collapsible);

                    attrs.$observe('reminder',function(value){
                        if (value) {
                            scope.showReminder = true;
                        }
                    });

                    attrs.$observe('helpTooltip',function(value){
                        if (value) {
                            scope.hasHelpTooltip = true;
                        }
                    });


                },
                controller : ['$scope','$attrs',function($scope,$attrs) {
                    $scope.toggleCollapse = false;
                    $scope.doCollapse = function() {
                        if ($scope.collapsible) {
                            $scope.toggleSection = !$scope.toggleSection;
                        }
                    };
                }]

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