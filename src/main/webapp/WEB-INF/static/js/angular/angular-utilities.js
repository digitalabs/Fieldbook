/* global angular */
(function(){
    'use strict';

    angular.module('leafnode-utils', [])
        .directive('jqDatepicker', function () {
            return {
                require: '^?ngModel',
                link: function (scope, el, attr, ngModel) {
                    if (!ngModel) {
                        $(el).datepicker({
                            format : 'yyyy-mm-dd'
                        });
                        return;
                    }

                    $(el).datepicker({
                        format: 'yyyy-mm-dd'
                    }).on('changeDate', function () {
                        scope.$apply(function () {
                            ngModel.$setViewValue(el.val());
                        });
                        $(this).datepicker('hide');
                    });

                    ngModel.$render = function () {
                        $(el).datepicker('setDate', ngModel.$viewValue);
                    };

                    if (attr.withImage === 'true') {
                        var labelElement = angular.element('<label class="btn"></label>');
                        var imageElement = angular.element('<img style="padding-bottom:3px;" src="' + attr.imageSrc + '"/>');

                        imageElement.on('click', function() {
                            $(el).datepicker('show');
                        });

                        labelElement.html(imageElement);
                        $(el).parent().append(labelElement);
                    }
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

(function(){
    'use strict';
    var getInheritedData = function(element, names){
        // If element is the document object work with the html element instead
        // this makes $(document).scope() possible
        if(element[0].nodeType === 9) {
            element = angular.element('html');
        }

        var value;
        while (element.length) {
            for (var i = 0, ii = names.length; i < ii; i++) {
                if ((value = element.data(names[i])) !== undefined) {return value;}
            }

            // If dealing with a document fragment node with a host element, and no parent, use the host
            // element as the parent. This enables directives within a Shadow DOM or polyfilled Shadow DOM
            // to lookup parent controllers.
            var node = element[0];
            element = angular.element(node.parentNode || (node.nodeType === 11 && node.host));
        }
    };

    angular.module('leafnode-utils').directive('ngMultiTemplate', [
        function(){
            return {
                controller: function(){
                    this.ngMultiTransclude = null;
                },
                templateUrl: function($element, $attrs){
                    return $attrs.ngMultiTemplate;
                },
                transclude: true
            };
        }
    ]).directive('ngMultiTranscludeController', [
        function(){
            return {
                controller: function(){
                    this.ngMultiTransclude = null;
                }
            };
        }
    ]).directive('ngMultiTransclude', [
        function(){
            return {
                link: function(scope, element, attrs, unusedCtrls, transcludeFn){
                    // Ensure we're transcluding or nothing will work.
                    if(!transcludeFn){
                        throw new Error(
                            'ngMultiTransclude')('orphan',
                                'Illegal use of ngMultiTransclude directive in the template! ' +
                                'No parent directive that requires a transclusion found. '
                        );
                    }

                    // Find the controller that wraps related multi-transclusions.
                    var ctrl = getInheritedData(element, [
                        '$ngMultiTranscludeControllerController',
                        '$ngMultiTemplateController'
                    ]);

                    if(!ctrl){
                        throw new Error(
                                'Illegal use of ngMultiTransclude directive in the template! ' +
                                'No parent directive that defines a multi-transclusion controller found. '
                        );
                    }

                    // Replace this element's HTML with the correct
                    // part of the clone.
                    var attach = function(clone){
                        var el;
                        for(var i = 0; i < clone.length; i++){
                            el = angular.element(clone[i]);

                            // Uses the argument as the `name` attribute directly, but we could
                            // evaluate it or interpolate it or whatever.
                            if(el.attr('name') === attrs.ngMultiTransclude){
                                element.append(el);
                                return;
                            }
                        }
                    };

                    // Only link the clone if we haven't already; store
                    // the already-linked clone on the controller so that
                    // it can be referenced by all relevant instances of
                    // the `ng-multi-transclude` directive.
                    if(ctrl.ngMultiTransclude){
                        attach(ctrl.ngMultiTransclude);
                    }
                    else {
                        transcludeFn(function(clone){
                            ctrl.ngMultiTransclude = clone;
                            attach(clone);
                        });
                    }
                }
            };
        }
    ]);
})();