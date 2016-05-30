// the following line instructs JSHint that angular is a global variable, and should not be part of its validation regarding undeclared variables
/*global angular:false */
/*global alert:false */
angular.module('digitalabs-data', [])
    .constant('APPLICATION_BASE', '')
    .constant('DISPLAY_ROWS', '10')
    .config(['$httpProvider', function($httpProvider) {
        //initialize get if not there
            if (!$httpProvider.defaults.headers.get) {
                $httpProvider.defaults.headers.get = {};
            }
            //disable IE ajax request caching
            $httpProvider.defaults.headers.get['Cache-Control'] = 'no-cache';
            $httpProvider.defaults.headers.get['Pragma'] = 'no-cache';
    }])
    .factory('resourceFactory', ['myHttp', '$q', 'DISPLAY_ROWS', 'APPLICATION_BASE', function (myHttp, $q, DISPLAY_ROWS, APPLICATION_BASE) {
        // this factory allows the creation of resource factories, capable of performing standard search and retrieval operations given a base URL and optional parameters for accessing
        // pattern is somewhat REST style, with some additional conventions

        //TODO: extend current 'Resource' functions by providing general error handler functions
        "use strict";
        return function (baseUrl, params) {

            if (baseUrl.indexOf('http') === -1 && APPLICATION_BASE !== '') {
                baseUrl = APPLICATION_BASE + '/' + baseUrl;
            }

            var Resource = function (data) {
                angular.extend(this, data);
            };

            // provides a method to retrieve items of a particular resource in pages. Search parameters to refine the list retrieved is optional
            // It assumes that the backend accepts the ff parameters : startRowIndex, lastRowIndex, and expects a list=true param-value pair to recognize processing
            Resource.getPagedList = function (pageNumber, searchParams) {
                var lastRowIndex = (pageNumber * DISPLAY_ROWS) - 1;
                var startRowIndex = (pageNumber - 1) * DISPLAY_ROWS;

                var pagedQueryParams = {
                    startRowIndex: startRowIndex,
                    lastRowIndex: lastRowIndex,
                    list: true
                };

                if (params !== null && params !== undefined) {
                    angular.extend(pagedQueryParams, params);
                }

                if (searchParams !== null && searchParams !== undefined) {
                    angular.extend(pagedQueryParams, searchParams);
                }

                return myHttp.get(baseUrl, {
                    params: pagedQueryParams
                }).then(function (response) {
                    var result = [];
                    angular.forEach(response.data, function (value, key) {
                        result[key] = new Resource(value);
                    });

                    return result;
                });
            };

            Resource.retrieveAll = function () {
                return myHttp.get(baseUrl, {
                    params: {listAll : true}
                }).then(function (response) {
                    var result = [];
                    angular.forEach(response.data, function (value, key) {
                        result.push(new Resource(value));
                    });

                    return result;
                });
            };

            // provides a method to retrieve an item of the particular resource by its ID. It relies on a REST-style convention of using HTTP GET and supplying the id as a path variable immediately after the base URL
            Resource.getById = function (elementId) {
                return myHttp.get(baseUrl + '/' + elementId, {
                    params: params
                }).then(function (data) {
                    return new Resource(data);
                });
            };

            // provides a method to retrieve items from the database given search parameters.
            Resource.find = function (searchParams) {
                if (params !== null && params !== undefined) {
                    angular.extend(searchParams, params);
                }

                // this assumes a convention where the receiving method on the server side expects a param-value pair of query=true
                searchParams.query = true;

                return myHttp.get(baseUrl, {
                    params: searchParams
                }).then(function (response) {
                    var result = [];
                    angular.forEach(response.data, function (value, key) {
                        if (value.id) {
                            result[value.id] = value;
                        } else {
                            result[key] = new Resource(value);
                        }
                    });

                    return result;
                });
            };

            // provides a method to save data. It assumes a REST style service, wherein the server will accept requests to the base URL, but with the POST method type, indicates saving of new data
            Resource.save = function (data) {
                // use Promise API for asynchronous handling
                var deferred = $q.defer();
                myHttp.post(baseUrl, data, {
                    headers : {
                        'Content-Type' : 'application/json;charset=utf-8',
                        'Accept' : 'application/json'
                    }
                }).then(function (response) {
                    if (response.data.success) {
                        deferred.resolve(new Resource(data));
                    } else {

                        // standardize the error message (if present) to be of array type
                        if (response.data.message) {
                            var array = [];
                            array.push(response.data.message);
                            deferred.reject(array);
                        } else if (response.data.messages) {
                            deferred.reject(response.data.messages);
                        } else {
                            /*TODO improve default error message in case of unsuccessful save attempt from server*/
                            var array = [];
                            array.push('Error from server');
                            deferred.reject(array);
                        }

                    }
                }, function () {
                    /*TODO improve default error message in case of unsuccessful attempt from server*/
                    var array = [];
                    array.push('Error from server');
                    deferred.reject(array);
                });
                return deferred.promise;
            };

            Resource.prototype.$save = function () {
                return Resource.save(this);
            };

            // provides a method to retrieve from the server a count of the maximum number of elements for the given resource. It assumes that there is a corresponding
            // method on the server side that accepts requests on the base URL, with an additional param-value pair of count=true
            // 03/14/2014 DMV
            // changed implem to be able to accept an optional set of parameters to be used when counting elements
            Resource.countMaxElements = function (searchParams) {
                var countParams = {
                    count: true
                };

                if (params !== null && params !== undefined) {
                    angular.extend(countParams, params);
                }

                if (searchParams) {
                    angular.extend(countParams, searchParams);
                }

                return myHttp.get(baseUrl, {
                    params: countParams
                }).then(function (response) {
                    return response.data.value;
                });
            };
            return Resource;
        };
    }])
    .service('myHttp', ['$http', function ($http) {
        "use strict";
        //TODO: standardize error handling across http based operations, probably through the creation of an ErrorService
        // where errors can be published and then broadcast to interested widgets
        var service = {

            activeItems : [],

            startSpinnerIfNecessary : function () {
                if (!Spinner.is_on) {
                    Spinner.play();
                }
            },

            stopSpinnerIfPossible : function () {
                if (service.activeItems.length === 0) {
                    Spinner.stop();
                }
            },

            // this function prepares data for sending to server by iterating over elements or properties and replacing Angular Promise objects with their resolved value
            prepareObject : function (data) {
                if (typeof data !== 'object') {
                    return data;
                } else if (data instanceof Array) {
                    angular.forEach(data, function (value, key) {
                        data[key] = service.prepareObject(value);
                    });

                    return data;
                } else {
                    if (data) {
                        angular.forEach(data, function (value, key) {
                            if (value && value.$$v) {
                                data[key] = value.$$v;
                            }
                        });
                        return angular.copy(data);
                    } else {
                        return data;
                    }
                }
            },

            get : function (url, config) {
                service.preHttpActivity();

                // include a random value each time to avoid getting cached by IE
                config.params.randomizer = new Date().getTime();
                var promise = $http.get(url, config);
                promise.then(service.postHttpActivity, service.postHttpActivity);

                return promise;
            },

            post : function (url, data, config) {
                service.preHttpActivity();
                var preparedData = service.prepareObject(data);

                var promise = $http.post(url, preparedData, config);
                promise.then(service.postHttpActivity, service.postHttpActivity);

                return promise;
            },

            postHttpActivity : function () {
                service.activeItems.pop();
                service.stopSpinnerIfPossible();
            },

            preHttpActivity : function () {
                service.activeItems.push(1);
                service.startSpinnerIfNecessary();
            }
        };

        return service;
    }]);
