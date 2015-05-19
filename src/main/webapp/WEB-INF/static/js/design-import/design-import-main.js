(function() {
    var app =  angular.module('designImportApp', ['ui.bootstrap', 'ngLodash', 'ngResource','ui.sortable']);

    app.controller('designImportCtrl', ['$scope','DesignMappingService','ImportDesign','$modal','Messages',function(scope,DesignMappingService,ImportDesign,$modal,Messages){
        // we can retrieve this from a service
        scope.data = DesignMappingService.data;
        scope.validateAndSend = function() {
            var onValidate = DesignMappingService.validateMapping();

            onValidate.then(function(result) {
                if (!result.success) {
                    createErrorNotification(Messages.DESIGN_MAPPING_ERROR_HEADER,result.error);
                    return;
                }

                if (result.warning) {
                    showAlertMessage('', result.warning);
                }

                ImportDesign.showReviewPopup();
            },function (failResult) {
                var msg= Messages.DESIGN_IMPORT_MISSING_MAPPING.replace('{0}',failResult);

                createErrorNotification(Messages.DESIGN_MAPPING_ERROR_HEADER,msg);
            });

        };

        scope.launchOntologyBrowser = function () {
            var $designMapModal = $('#designMapModal');
            $designMapModal.one('hidden.bs.modal',function() {
                setTimeout(function() {
                    scope.$apply(function() {
                        var title = 'Ontology Browser';
                        var url = '/Fieldbook/OntologyManager/manage/variable';

                        $modal.open({
                            windowClass: 'modal-very-huge',
                            controller: 'OntologyBrowserController',
                            templateUrl: '/Fieldbook/static/angular-templates/ontologyBrowserPopup.html',
                            resolve: {
                                title: function () {
                                    return title;
                                },

                                url: function () {
                                    return url;
                                }
                            }
                        }).result.finally(function() {
                                // do something after this modal closes
                                // TODO: refresh cached content for the variable selection

                                setTimeout(function() {
                                    $designMapModal.modal('show');
                                },200);

                            });

                    });
                },200);

            }).modal('hide');

        };

        scope.designType = '';
        scope.onDesignTypeSelect = function() {

          if (scope.designType === '3') {
              // warning popup here
              showAlertMessage('', Messages.OWN_DESIGN_SELECT_WARNING);
          }

        };



    }]);

    app.controller('OntologyBrowserController', ['$scope', '$modalInstance', 'title', 'url',
        function ($scope, $modalInstance, title, url) {
            $scope.title = title;
            $scope.url = url;
            $scope.close = function () {
                $modalInstance.dismiss('Cancelled');
            };

    }]);

    app.directive('mappingGroup', ['Messages',function (Messages) {
        return {
            restrict: 'E',
            scope: {
                name: '@',
                mappingData: '=data'
            },
            templateUrl: '/Fieldbook/static/angular-templates/designImport/mappingGroup.html',
            controller: ['$scope', '$attrs', function ($scope, $attrs) {
                // data structure
                $scope.Messages = Messages;
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
                                delete itemModel.variable;
                                delete itemModel.required;
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

    app.directive('designMapVariableSelection', ['VARIABLE_SELECTION_MODAL_SELECTOR', 'DesignOntologyService',
        function (VARIABLE_SELECTION_MODAL_SELECTOR, DesignOntologyService) {
            return {
                restrict: 'A',
                scope: {
                    modeldata: '=',
                    callback: '&'
                },

                link: function (scope, elem, attrs) {
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
                                        scope.callback({ result: { id: value.variable.cvTermId, variable: value.variable} });

                                        scope.modeldata.id = value.variable.cvTermId;
                                        scope.modeldata.variable = value.variable;

                                        // process propery, scale + method
                                        scope.modeldata.variable.property = {
                                            name : value.variable.property
                                        };

                                        scope.modeldata.variable.scale = {
                                            name : value.variable.scale
                                        };

                                        scope.modeldata.variable.method = {
                                            name : value.variable.method
                                        };

                                    });
                                }

                            }

                            $(VARIABLE_SELECTION_MODAL_SELECTOR).modal('hide');

                            scope.$emit('variableAdded', out);
                        });
                    };

                    elem.on('click', function () {
                        // temporarily close the current modal
                        var $designMapModal = $('#designMapModal');

                        var params = {
                            variableType: attrs.group,
                            retrieveSelectedVariableFunction: function () {
                                return {};
                            },
                            callback: scope.processData,
                            onHideCallback : function() {
                                setTimeout(function() {
                                    $designMapModal.modal('show');
                                },200);
                            },
                            apiUrl: '/Fieldbook/OntologyBrowser/getVariablesByPhenotype?phenotypeStorageId=' + attrs.group
                        };

                        $designMapModal.one('hidden.bs.modal',function() {
                            setTimeout(function() {
                                DesignOntologyService.openVariableSelectionDialog(params);
                            },200);
                        }).modal('hide');

                    });
                }
            };
        }]);

    app.service('DesignMappingService',['$http','$q','_',function($http,$q,_) {

            function validateMapping() {

                var postData = angular.copy(service.data);
                delete postData.unmappedHeaders;
                var allMapped = true;
                var deferred = $q.defer();

                // lets grab all variables that are in groups but does not have mapped variables
                _.forEach(postData,function(value,key) {
                    var results = _.filter(value,function(item,key2) {
                        return !_.has(item,'variable');
                    });

                    if (results.length > 0) {
                        allMapped = false;

                        deferred.reject(results[0].name);

                        return false;
                    }
                });


                if (!allMapped) {
                    return deferred.promise;
                }

                // transform postData into simpler list of standard variable ids
                // output should be in the format
                // result : { mappedDesignFactors : [ { name : header_name, id : std_var_id } ] }

                _.forIn(postData,function(value,key) {
                    for (var i = 0; i < value.length; i++) {

                        if (_.has(value[i],'variable')) {
                            if (_.has(value[i].variable,'id') && value[i].variable.id) {
                                value[i].id = value[i].variable.id;
                            } else if (_.has(value[i].variable,'cvTermId') && value[i].variable.cvTermId) {
                                value[i].id = value[i].variable.cvTermId;
                            } else {
                                value[i].id = 0;
                            }

                            value[i] = _.pick(value[i],['id','name','columnIndex']);
                        }

                    }
                });

                return $http.post('/Fieldbook/DesignImport/validateAndSaveNewMapping',postData).then(function(result) {
                    return result.data;
                });

            }

            var service = {
                data : {
                    unmappedHeaders : [],
                    mappedEnvironmentalFactors: [],
                    mappedDesignFactors : [],
                    mappedGermplasmFactors : [],
                    mappedTraits : []
                },
                validateMapping : validateMapping
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

    app.service('ImportDesign',function() {
       return ImportDesign;
    });

    /* non angularjs codes */

    /* we bind events on design map modal open, and when the design map modal workflow is done */
    $(function(){
    });

})();