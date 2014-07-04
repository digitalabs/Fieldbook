/* global angular */
(function(){
    'use strict';
    angular.module('leafnode-utils', [])
        .directive('jqDatepicker', function () {
            return {
                require: '?ngModel',
                link: function (scope, el, attr, ngModel) {
                    if (!ngModel) {
                        $(el).datepicker();
                        return;
                    }

                    $(el).datepicker().on('changeDate', function () {
                        scope.$apply(function () {
                            ngModel.$setViewValue(el.val());
                        });
                    });
                    ngModel.$render = function () {
                        $(el).datepicker('update', ngModel.$viewValue);
                    };


                }
            };
        })
        .directive('slideToggle',function() {
            return {
                link: function(scope,el,attr) {
                    $(el).click(function() {
                        $(el).find('.icn.section-expanded').toggle();
                        $(el).find('.icn.section-collapsed').toggle();

                        $(attr.section).slideToggle();
                    });
                }
            };
        });
})();