(function() {
    var app =  angular.module('designImportApp', ['leafnode-utils', 'fieldbook-utils',
        'ui.bootstrap', 'ngLodash', 'ngResource']);


    app.controller('designImportCtrl', ['$scope',function($scope){

        $scope.testscopevar = 'hello world';

    }]);


    app.directive('mappingGroup', ['$parse', function ($parse) {
        return {
            restrict: 'E',
            scope: {
                mappingData: '='
            },
            transclude: true,
            templateUrl: '/Fieldbook/static/angular-templates/designImport/mapping-group.html',
            link: function (scope, elem, attrs) {
                scope.addVariable = $parse(attrs.addVariable)();


                attrs.$observe('helpTooltip', function (value) {
                    if (value) {
                        scope.hasHelpTooltip = true;
                    }
                });


            },
            controller: ['$scope', '$attrs', function (scope, attrs) {
                // data structure
                /*
                 var mappingData = [
                 {
                 id : 0,
                 name : 'header',
                 hasError : false,
                 variable : {
                 name : 'variable name',
                 property : 'property 01',
                 method : 'method 01',
                 scale : 'scale 01',
                 }
                 },
                 {
                 id : 1,
                 name : 'header-2',
                 hasError : true,
                 }
                 ];
                 */

                scope.computeButtonLabel = function (header) {
                    if (header.variable) {
                        return 'Re-map';
                    } else {
                        return 'Apply Mapping';
                    }
                };


            }]

        };
    }]);



})();