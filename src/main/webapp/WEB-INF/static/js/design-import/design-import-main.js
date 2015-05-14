(function() {
    var app =  angular.module('designImportApp', ['ui.bootstrap', 'ngLodash', 'ngResource','ui.sortable']);


    app.controller('designImportCtrl', ['$scope','DesignMappingService',function(scope,DesignMappingService){
        // we can retrieve this from a service
        scope.unmappedHeaders = DesignMappingService.unmappedHeaders;
        scope.mappedEnvironmentalFactors = DesignMappingService.mappedEnvironmentalFactors;
        scope.mappedDesignFactors = DesignMappingService.mappedDesignFactors;
        scope.mappedGermplasmFactors = DesignMappingService.mappedGermplasmFactors;
        scope.mappedTraits = DesignMappingService.mappedTraits;

    }]);


    app.directive('mappingGroup', ['$parse',function ($parse) {
        return {
            restrict: 'E',
            scope: {
                name: '@',
                mappingData: '=data'
            },
            templateUrl: '/Fieldbook/static/angular-templates/designImport/mappingGroup.html',
            controller: ['$scope', '$attrs', function ($scope, $attrs) {
                // data structure
                $scope.variableType = $attrs.variableType;

                $scope.sortableOptions = {
                    connectWith: '.list-group',
                    update: function(e,ui) {
                        if (!ui.item.sortable.received) {
                                var originNgModel = ui.item.sortable.sourceModel;
                                var itemModel = originNgModel[ui.item.sortable.index];
                                var dropNgModel = ui.item.sortable.droptargetModel;

                            var exists = !!dropNgModel.filter(function(item) {
                                return item.name === itemModel.name;
                            }).length;

                            // note ui.item.sortable.cancel() will interrupt the dragging

                            if (!exists) {
                                itemModel.variable = null;
                            }

                        }
                    }
                };

                $scope.computeButtonLabel = function (header) {
                    if (header.variable) {
                        return 'Re-map';
                    } else {
                        return 'Apply Mapping';
                    }
                };

                $scope.onAdd = function(result) {

                };


            }]

        };
    }]);

    app.directive('selectStandardVariable', ['VARIABLE_SELECTION_MODAL_SELECTOR', 'VARIABLE_SELECTED_EVENT_TYPE', 'DesignOntologyService',
        function (VARIABLE_SELECTION_MODAL_SELECTOR, VARIABLE_SELECTED_EVENT_TYPE, DesignOntologyService) {
            return {
                restrict: 'A',
                scope: {
                    modeldata: '=',
                    callback: '&'
                },

                link: function (scope, elem, attrs) {
                    console.log(scope.modeldata);
                    scope.processData = function (data) {
                        scope.$apply(function () {
                            var out = {};

                            if (data.responseData) {
                                data = data.responseData;
                            }
                            if (data) {

                                // if retrieved data is an array of values
                                if (data.length && data.length > 0) {
                                    $.each(data, function (key, value) {
                                        //scope.modeldata.push(value.variable.cvTermId, TrialManagerDataService.transformViewSettingsVariable(value));
                                        //out[value.variable.cvTermId] = value;
                                        scope.callback({ result: { id: value.variable.cvTermId, variable: value.variable} });

                                        scope.modeldata.id = value.variable.cvTermId;
                                        scope.modeldata.variable = value.variable;

                                    });
                                }

                            }



                            $('#variableSelectionModal .modal').modal('hide');
                             setTimeout(function() {
                             $('#designMapModal').modal('show');
                             },600);


                            scope.$emit('variableAdded', out);
                        });
                    };

                    elem.on('click', function () {
                        // temporarily close the current modal
                        $('#designMapModal').modal('hide');

                        var params = {
                            variableType: attrs.group,
                            retrieveSelectedVariableFunction: function () {
                                return {};
                            },
                            callback: scope.processData
                        };


                        setTimeout(function() {
                            DesignOntologyService.openVariableSelectionDialog(params);
                        },600);
                    });
                }
            };
        }]);

    app.service('DesignMappingService',['$http','$q','UNMAPPED_HEADERS','MAPPED_ENVIRONMENTAL_FACTORS','MAPPED_DESIGN_FACTORS',
        'MAPPED_GERMPLASM_FACTORS', 'MAPPED_TRAITS',function($http,$q,UNMAPPED_HEADERS,MAPPED_ENVIRONMENTAL_FACTORS,MAPPED_DESIGN_FACTORS,
        MAPPED_GERMPLASM_FACTORS, MAPPED_TRAITS) {

            var service = {
                unmappedHeaders : UNMAPPED_HEADERS,
                mappedEnvironmentalFactors: MAPPED_ENVIRONMENTAL_FACTORS,
                mappedDesignFactors : MAPPED_DESIGN_FACTORS,
                mappedGermplasmFactors : MAPPED_GERMPLASM_FACTORS,
                mappedTraits : MAPPED_TRAITS
            };

            return service;

    }]);

    app.service('DesignOntologyService', ['VARIABLE_SELECTION_LABELS', function(VARIABLE_SELECTION_LABELS) {
        var TrialSettingsManager = window.TrialSettingsManager;
        var settingsManager = new TrialSettingsManager(VARIABLE_SELECTION_LABELS);

        return {
            openVariableSelectionDialog: function (params) {
                settingsManager._openVariableSelectionDialog(params);
            },

            // @param = this map contains variables of the pair that will be filtered
            addDynamicFilterObj: function (_map, group) {
                settingsManager._addDynamicFilter(_map, group);
            }

        };
    }]);


    /* non angularjs codes */

    /* we bind events on design map modal open, and when the design map modal workflow is done */
    $(function(){
        var $designMapModal = $('#designMapModal');
        var $body = $('body');
        $designMapModal.on('shown.bs.modal',function() {
            $body.data('designModalIsOpened',true);
        });

        var $designMapModalCloseTriggers = $designMapModal.find('.closeTrigger');
        $designMapModalCloseTriggers.on('click',function() {
           $body.data('designModalIsOpened',false);
        });
    });

})();