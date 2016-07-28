/*global angular:false */
angular.module('digitalabs-util', ['ui.bootstrap'])
    .controller('ListDisplayPaginatedModalController', ['$scope', '$modalInstance', 'label', 'initialItems', 'DISPLAY_ROWS',
        'RowRetrievalService', 'rowRetrievalParams', function ($scope, $modalInstance, label, initialItems, DISPLAY_ROWS, RowRetrievalService, rowRetrievalParams) {
        "use strict";
        $scope.items = initialItems;
        $scope.label = label;

        // changed implem to accept parameters when counting elements
        RowRetrievalService.countMaxElements(rowRetrievalParams).then(function (totalItems) {
            $scope.pagination = {
                currentPage: 1,
                maxSize: 7,
                pageItems: DISPLAY_ROWS,
                totalItems:  totalItems + 1
            };
        });

        $scope.setPage = function (pageNo) {
            RowRetrievalService.getPagedList(pageNo, rowRetrievalParams).then(function (data) {
                $scope.items = data;
                $scope.pagination.currentPage = pageNo;
            });
        };

        $scope.selectRow = function (selectedRow) {
            $modalInstance.close(selectedRow);
        };

        $scope.close = function () {
            $modalInstance.dismiss('Closed');
        };
    }])
    .service('Utilities', function () {
        "use strict";
        return {
            // this function returns a copy of the supplied object, stripped of extra variables used by Angular in processing, as well as replacing Promise objects with their resolved values
            remove : function (targetArray, itemForRemoval) {
                var index = targetArray.indexOf(itemForRemoval);
                targetArray.splice(index, 1);
            }
        };
    });


var datasetImporterDirectives = angular.module('dataset-importer-directives', []);

datasetImporterDirectives.directive('resizableaffix',function($window,$parse) {
    return {
        scope: true,
        restrict : 'A',
        link: function(scope,elem,attr) {
            scope.resizeComponent = function() {
                scope.width = $(attr.parent).width() - parseInt(elem.css('paddingLeft')) - parseInt(elem.css('paddingRight')) - parseInt(elem.css('marginLeft')) - parseInt(elem.css('marginRight')) - parseInt(elem.css('borderLeftWidth')) - parseInt(elem.css('borderRightWidth'));
                elem.css({width : scope.width});
            };


            scope.$watch('isReady',function(result) {
                if (result) {
                    scope.resizeComponent();
				}
            });



            angular.element($window).bind('resize', function() {
                scope.resizeComponent();
                scope.$apply();
            });

        }
    };
});

datasetImporterDirectives.directive('pageNotification',['$timeout',function($timeout){
    return {
        restrict: 'A',
        scope : {
            toggle : '='
        },
        link: function(scope,elem,attr) {
            var doNotify = function() {
                if (scope.toggle === true) {
                    $.blockUI({
                        message: $(elem),
                        fadeIn: 700,
                        fadeOut: 700,
                        timeout: 6000,
                        showOverlay: false,
                        centerY: false,
                        css: {
                            background: 'none',
                            top: '10px',
                            left: '',
                            right: '20px',
                            border: 'none'
                        }
                    });
				}
            };

            scope.$watch('toggle',doNotify);
        }
    };
}]);

