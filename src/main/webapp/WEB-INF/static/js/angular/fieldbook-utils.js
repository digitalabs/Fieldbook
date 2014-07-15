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
            if (list) {
                for (var i = 0; i < list.length; i++) {
                    this.m_keys.push(keyExtract(list[i]));
                    this.m_vals[keyExtract(list[i])] = list[i];
                }
            }
        };


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

        OrderedHash.prototype.remove = function(key) {
            this.m_keys.splice(this.m_keys.indexOf(key),1);
            delete this.m_vals[key];

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
                    $scope.removeSetting = function(key) {
                        $scope.settings.remove(key);
                        $.ajax({
                            url: '/Fieldbook/manageSettings/deleteVariable/' + $attrs.variableType + '/' + key,
                            type: 'POST',
                            cache: false,
                            data: '',
                            contentType: 'application/json',
                            success: function () {
                            }
                        });

                        $scope.$emit('deleteOccurred');

                    };

                    $scope.showDetailsModal = function(setting) {
                        // this function is currently defined in the fieldbook-common.js, loaded globally for the page
                        // TODO : move away from global function definitions
                        showBaselineTraitDetailsModal(setting.variable.cvTermId);
                    };

                    $scope.size = function() {
                        return Object.keys($scope.settings).length;
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

                controller : function($scope, $element, $attrs, VARIABLE_SELECTION_MODAL_SELECTOR, VARIABLE_SELECTED_EVENT_TYPE, TrialSettingsManager) {
                    $scope.processModalData = function (data) {
                        if (data.responseData) {
                            data = data.responseData;
                        }
                        if (data) {
                            // if retrieved data is an array of values
                            if (data.length && data.length > 0) {
                                $.each(data, function (key, value) {
                                    $scope.modeldata.push(value.variable.cvTermId,value);
                                });
                            } else {
                                // if retrieved data is a single object
                                $scope.modeldata.push(data.variable.cvTermId,data);
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

                                $.each($scope.modeldata.vals(), function(key, value) {
                                    selected.push(value.variable.cvTermId);
                                });

                                return selected;
                            }
                        };

                        $(VARIABLE_SELECTION_MODAL_SELECTOR).off(VARIABLE_SELECTED_EVENT_TYPE);
                        $(VARIABLE_SELECTION_MODAL_SELECTOR).on(VARIABLE_SELECTED_EVENT_TYPE, $scope.processModalData);

                        TrialSettingsManager.openVariableSelectionDialog(params);
                    });
                }
            };
        })
        .service('TrialSettingsManager', ['TRIAL_VARIABLE_SELECTION_LABELS', function(TRIAL_VARIABLE_SELECTION_LABELS) {
            var TrialSettingsManager = window.TrialSettingsManager;
            var settingsManager = new TrialSettingsManager(TRIAL_VARIABLE_SELECTION_LABELS);

            var service = {
                openVariableSelectionDialog : function(params) {
                    settingsManager._openVariableSelectionDialog(params);
                }
            };

            return service;
        }])
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
                    $scope.variableDefinition = $scope.settings.val($scope.targetkey);
                    $scope.widgetType = $scope.variableDefinition.variable.widgetType.$name ?
                        $scope.variableDefinition.variable.widgetType.$name : $scope.variableDefinition.variable.widgetType;
                    $scope.hasDropdownOptions = $scope.widgetType === 'DROPDOWN';


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
                        // TODO : add code that will recognize categorical variable dropdowns and change the displayed text accordingly
                        $scope.dropdownValues = $scope.variableDefinition.possibleValues;

                        $scope.computeMinimumSearchResults = function() {
                            return ($scope.dropdownValues.length > 0) ? 20 : -1;
                        };

                        $scope.dropdownOptions = {
                            data: function () {
                                return {results: $scope.dropdownValues};
                            },
                            formatResult: function (value) {
                                // TODO : add code that can handle display of methods
                                return value.description;
                            },
                            formatSelection: function (value) {
                                // TODO : add code that can handle display of methods
                                return value.description;
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

                                query.callback(data);
                            }

                        };

                        if ($scope.valuecontainer[$scope.targetkey]) {
                            $scope.dropdownOptions.initSelection = function(element, callback) {
                                angular.forEach($scope.dropdownValues, function(value) {
                                    if (value.id === $scope.valuecontainer[$scope.targetkey] ||
                                        value.description === $scope.valuecontainer[$scope.targetkey]) {
                                        callback(value);
                                        return false;
                                    }
                                });
                            };
                        }
                    }

                    // TODO : add code that can handle display of favorite methods, as well as update of possible values in case of click of manage methods
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
                    showReminder: '=',
                    enableUpdate: '=',
                    onUpdate: '&',
                    hideVariable : '=',

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

                    $scope.doClick = function() {
                        $scope.onUpdate({});
                    };
                }]

            };
        }])
        .directive('showBaselineTraitDetailsModalLink',function(){
            return {
                 scope: {
                     showBaselineTraitDetailsModalLink : '@'
                 },
                link: function (scope,elem,attrs) {
                    elem.click(function(e) {
                        showBaselineTraitDetailsModal(scope.cvTermId);
                    });
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
        })

        .filter('removeHiddenVariableFilter', function() {
            return function(settingKeys,settingVals) {
                var keys = [];

                angular.forEach(settingKeys,function(val,key) {
                    if (!settingVals[val].hidden) {
                        keys.push(val);
                    }
                });

                return keys;
            };
        });

})();