/* global angular */
angular.module('leafnode-utils', [])
        .directive('jqDatepicker', function () {
        'use strict';
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
        });
