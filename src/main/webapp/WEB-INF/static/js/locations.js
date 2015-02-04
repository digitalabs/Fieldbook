/* globals getJquerySafeId */

var LocationsFunctions = window.LocationsFunctions;

// undefined check is performed to avoid overwriting the state / functionality of the object
if (typeof (LocationsFunctions) === 'undefined') {
    LocationsFunctions = {
        isModalAvailableAndForOpening: function (modalID) {
            return $('#' + getJquerySafeId(modalID)).length !== 0 && $('#' + getJquerySafeId(modalID)).data('open') == '1';
        },

        openModal: function (modalID) {
            setTimeout(function () {$('#' + getJquerySafeId(modalID)).modal('show');}, 200);
            setTimeout(function () {$('#' + getJquerySafeId(modalID)).data('open', '0');}, 500);
        },

        sourceURL: '',

        // function for opening the Manage Locations modal that links to the IBPWorkbench application and ensuring that the proper modal window is re-opened on close of this one
        openLocationsModal: function () {
            $('#manageLocationModal').modal({backdrop: 'static', keyboard: true});

            LocationsFunctions.retrieveCurrentProjectID().done(function (projectID) {
                if (LocationsFunctions.sourceURL === '') {
                    LocationsFunctions.retrieveProgramLocationURL().done(function (data) {
                        LocationsFunctions.sourceURL = data;
                        $('#locationFrame').attr('src', LocationsFunctions.sourceURL + projectID);
                    });
                } else {
                    $('#locationFrame').attr('src', LocationsFunctions.sourceURL + projectID);
                }
            });

        },

        // function that prepares and initializes both the Select2 dropdown containing the location list as well as the checkbox that toggles between displaying only favorite
        // locations or no. locationConversionFunction is provided as a parameter in case developers wish to change the construction of each select2 item, tho built-in function
        // will be used by default if this is not provided
        processLocationDropdownAndFavoritesCheckbox : function(locationSelectID, favoritesCheckboxID, favoritesDefault, locationType,locationConversionFunction) {
            LocationsFunctions.retrieveLocations().done(function(data) {
                if (! locationConversionFunction) {
                    locationConversionFunction = LocationsFunctions.convertLocationToSelectItem;
                }

                var possibleValues;
                if (!locationType || locationType === 'breedingLocation') {
                    possibleValues = LocationsFunctions.convertLocationsToSelectItemList(data.allBreedingLocations, locationConversionFunction);
                } else if (locationType === 'seedStorageLocation') {
                    possibleValues = LocationsFunctions.convertLocationsToSelectItemList(data.allSeedStorageLocations, locationConversionFunction);
                }

                var favoritePossibleValues = LocationsFunctions.convertLocationsToSelectItemList(data.favoriteMethods, locationConversionFunction);

                if (favoritesDefault) {
                    $safeId('#' + favoritesCheckboxID).prop('checked', true);
                    LocationsFunctions.createSelect2Dropdown(favoritePossibleValues, locationSelectID);
                } else {
                    LocationsFunctions.createSelect2Dropdown(possibleValues, locationSelectID);
                }


                // attach change handler to checkbox for switching between favorite only and all breeding methods
                $('#' + getJquerySafeId(favoritesCheckboxID)).on('change', function() {
                    // get previous value of dropdown first
                    var currentSelected = $('#' + getJquerySafeId(locationSelectID)).select2('data');

                    if (this.checked) {
                        LocationsFunctions.createSelect2Dropdown(favoritePossibleValues, locationSelectID);
                    } else {
                        LocationsFunctions.createSelect2Dropdown(possibleValues, locationSelectID);
                    }

                    if (currentSelected && currentSelected.id) {
                        $('#' + getJquerySafeId(locationSelectID)).select2('val', currentSelected.id);
                    }

                });

                $(document).on('location-update', function() {
                    LocationsFunctions.processLocationDropdownAndFavoritesCheckbox(locationSelectID, favoritesCheckboxID, favoritesDefault, locationType, locationConversionFunction);
                })
            });
        },

        // FIXME : change declaration so function is not accessible to the outside
        createSelect2Dropdown : function(valuesData, targetID) {
            if (! valuesData) {
                valuesData = [];
            }
            var minResults = (valuesData.length > 0) ? 20 : -1;

            $('#' + getJquerySafeId(targetID)).select2(
                {
                    minimumResultsForSearch: minResults,
                    initSelection : function(element, callback) {
                        $.each(valuesData, function(index, value) {
                            if (value.id == element.val()) {
                                callback(value);
                            }
                        });
                    },
                    query: function (query) {
                        var data = {
                            results: valuesData
                        };
                        // return the array that matches
                        data.results = $.grep(data.results, function (item) {
                            return ($.fn.select2.defaults.matcher(query.term,
                                item.text));

                        });
                        query.callback(data);
                    }
                });
        },

        // FIXME : change declaration so function is not accessible to the outside
        convertLocationsToSelectItemList : function(locationList, locationConversionFunction) {

            var selectItemList = [];
            if (!locationList) {
                return selectItemList;
            }

            $.each(locationList, function(index, methodItem) {
                selectItemList.push(locationConversionFunction(methodItem));
            });

            return selectItemList;
        },


        // FIXME : change declaration so function is not accessible to the outside
        convertLocationToSelectItem : function(location) {
            return {
                id: location.locid,
                text: location.lname,
                abbr: location.labbr,
                description : location.labbr
            };
        },

        retrieveLocations : function() {
            return $.ajax({
                url: '/Fieldbook/locations/getLocations',
                type: 'GET',
                cache: false,
                data: '',
                async: false,
                success: function (data) {
                    if (data.success === '0') {
                        showErrorMessage('page-message', data.errorMessage);
                    }
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    console.log('The following error occurred: ' + textStatus, errorThrown);
                },
                complete: function () {
                }
            });
        },

        retrieveProgramLocationURL: function () {
            return $.get("/Fieldbook/locations/programLocationsURL");
        },

        retrieveCurrentProjectID: function () {
            return $.get("/Fieldbook/breedingMethod/programID");
        }

    };
}