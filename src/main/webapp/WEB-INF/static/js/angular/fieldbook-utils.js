/*global angular*/
/*global showBaselineTraitDetailsModal */
/* global openManageLocations*/
(function() {
    'use strict';

    angular.OrderedHash = (function () {
        function OrderedHash() {
            this.m_keys = [];
            this.m_vals = {};
        }

        OrderedHash.prototype.addList = function (list, keyExtract) {
            for (var i = 0; i < list.length; i++) {
                OrderedHash.this.m_keys.push(keyExtract(list[i]));
                OrderedHash.this.m_vals[keyExtract(list[i])] = list[i];
            }
        }


        OrderedHash.prototype.push = function (k, v) {
            if (!this.m_vals[k]) {
                this.m_keys.push(k);
            }
            this.m_vals[k] = v;
            return v;
        };

        OrderedHash.prototype.length = function () {
            return this.m_keys.length;
        };

        OrderedHash.prototype.keys = function () {
            return this.m_keys;
        };

        OrderedHash.prototype.val = function (k) {
            return this.m_vals[k];
        };

        OrderedHash.prototype.vals = function () {
            return this.m_vals;
        };

        return OrderedHash;

    })();

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
                        var index = $scope.settings.indexOf(setting);
                        if (index !== -1){
                            $scope.settings.splice(index, 1);

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
                        if ($scope.settings instanceof Array) {
                            return $scope.settings.length;
                        } else if ($scope.settings instanceof Object) {
                            return Object.keys($scope.settings).length;
                        }
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
                                    $scope.modeldata.push(value);
                                });
                            } else {
                                // if retrieved data is a single object
                                $scope.modeldata.push(data);
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
                                var selected = [];

                                $.each($scope.modeldata, function(key, value) {
                                    selected.push(value.variable.cvTermId);
                                });

                                return selected;
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
                    $scope.findSetting = function(targetKey) {
                        var foundSetting = null;
                        $.each($scope.settings, function(key, value) {
                            if (value.variable.cvTermId == targetKey) {
                                foundSetting = value;
                                return false;
                            }
                        });

                        return foundSetting;
                    };

                    $scope.variableDefinition = $scope.findSetting($scope.targetkey);
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

        .directive('sectionContainer',['$parse',function($parse){
            return {
                restrict: 'E',
                scope : {
                    heading : '@',
                    reminder : '@',
                    helpTooltip : '@',
                    icon: '@',
                    iconImg: '@',
                    modelData: '=',
                    variableType: '@',
                    showReminder: '='

                },
                transclude : true,
                templateUrl: '/Fieldbook/static/angular-templates/sectionContainer.html',
                link : function (scope,elem,attrs) {
                    scope.collapsible = $parse(attrs.collapsible)();
                    scope.addVariable =  $parse(attrs.addVariable)();


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
        }])
        // filters
        .filter('range', function() {
            return function(input, total) {
                total = parseInt(total);
                for (var i=0; i<total; i++) { input.push(i); }

                return input;
            };
        });

})();