/* globals getJquerySafeId */

var BreedingMethodsFunctions = window.BreedingMethodsFunctions;

// undefined check is performed to avoid overwriting the state / functionality of the object
if (typeof (BreedingMethodsFunctions) === 'undefined') {
    BreedingMethodsFunctions = {
        isModalAvailableAndForOpening: function (modalID) {
            return $('#' + getJquerySafeId(modalID)).length !== 0 && $('#' + getJquerySafeId(modalID)).data('open') == '1';
        },

        openModal: function (modalID) {
            setTimeout(function () {$('#' + getJquerySafeId(modalID)).modal('show');}, 200);
            setTimeout(function () {$('#' + getJquerySafeId(modalID)).data('open', '0');}, 200);
        },

        sourceURL: '',

        // function for opening the Manage Methods modal that links to the IBPWorkbench application and ensuring that the proper modal window is re-opened on close of this one
        openMethodsModal: function () {
            $('#manageMethodModal').modal({backdrop: 'static', keyboard: true});

            BreedingMethodsFunctions.retrieveCurrentProjectID().done(function (projectID) {
                if (BreedingMethodsFunctions.sourceURL === '') {
                    BreedingMethodsFunctions.retrieveProgramMethodURL().done(function (data) {
                        BreedingMethodsFunctions.sourceURL = data;
                        $('#methodFrame').attr('src', BreedingMethodsFunctions.sourceURL + projectID);
                    });
                } else {
                    $('#methodFrame').attr('src', BreedingMethodsFunctions.sourceURL + projectID);
                }
            });

        },

        // function that prepares and initializes both the Select2 dropdown containing the method list as well as the checkbox that toggles between displaying only favorite
        // methods or no. methodConversionFunction is provided as a parameter in case developers wish to change the construction of each select2 item, tho built-in function
        // will be used by default if this is not provided
        processMethodDropdownAndFavoritesCheckbox : function(methodSelectID, favoritesCheckboxID, methodConversionFunction) {
            BreedingMethodsFunctions.retrieveBreedingMethods().done(function(data) {
                if (! methodConversionFunction) {
                    methodConversionFunction = BreedingMethodsFunctions.convertMethodToSelectItem;
                }

                var possibleValues = BreedingMethodsFunctions.convertMethodsToSelectItemList(data.allMethods, methodConversionFunction);
                var favoritePossibleValues = BreedingMethodsFunctions.convertMethodsToSelectItemList(data.favoriteMethods, methodConversionFunction);

                BreedingMethodsFunctions.createSelect2Dropdown(possibleValues, methodSelectID);

                // attach change handler to checkbox for switching between favorite only and all breeding methods
                $('#' + getJquerySafeId(favoritesCheckboxID)).on('change', function() {
                    // get previous value of dropdown first
                    var currentSelected = $('#' + getJquerySafeId(methodSelectID)).select2('data');

                    if (this.checked) {
                        BreedingMethodsFunctions.createSelect2Dropdown(favoritePossibleValues, methodSelectID);
                    } else {
                        BreedingMethodsFunctions.createSelect2Dropdown(possibleValues, methodSelectID);
                    }

                    $('#' + getJquerySafeId(methodSelectID)).select2('val', currentSelected.id);
                });

                $(document).on('breeding-method-update', function() {
                    BreedingMethodsFunctions.processMethodDropdownAndFavoritesCheckbox(methodSelectID, favoritesCheckboxID, methodConversionFunction);
                })
            });
        },

        // FIXME : change declaration so function is not accessible to the outside
        createSelect2Dropdown : function(valuesData, targetID) {
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
                        data.results = $.grep(data.results, function (item, index) {
                            return ($.fn.select2.defaults.matcher(query.term,
                                item.text));

                        });
                        query.callback(data);
                    }
                });
        },

        // FIXME : change declaration so function is not accessible to the outside
        convertMethodsToSelectItemList : function(methodList, methodConversionFunction) {
            var selectItemList = [];
            $.each(methodList, function(index, methodItem) {
                selectItemList.push(methodConversionFunction(methodItem));
            });

            return selectItemList;
        },


        // FIXME : change declaration so function is not accessible to the outside
        convertMethodToSelectItem : function(method) {
            return {
                'id': method.mid,
                'text': method.mname + (method.mcode !== undefined ? ' - ' + method.mcode : ''),
                'description': method.mdesc
            };
        },

        retrieveBreedingMethods : function() {
            return $.ajax({
                url: '/Fieldbook/breedingMethod/getBreedingMethods',
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

        retrieveProgramMethodURL: function () {
            return $.get("/Fieldbook/breedingMethod/programMethodURL");
        },

        retrieveCurrentProjectID: function () {
            return $.get("/Fieldbook/breedingMethod/programID");
        }

    };
}