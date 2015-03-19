var ImportAdvanceInventoryFunctions  = window.ImportAdvanceInventoryFunctions;

if (typeof ImportAdvanceInventoryFunctions === 'undefined') {
    ImportAdvanceInventoryFunctions = {
        openImportAdvanceInventoryModal : function() {
            $('#importAdvanceInventoryModal').modal({backdrop: 'static', keyboard: true});

            $('#advance-import-list-id').val(getCurrentAdvanceTabListIdentifier());
            $('#advanceImportUploadClearButton').click();
            $('#importAdvanceInventorySubmitButton').off('click');
            $('#importAdvanceInventorySubmitButton').on('click', ImportAdvanceInventoryFunctions.processImport);
        },

        processImport: function () {
            'use strict';

            if ($('#fileupload-import-advance-inventory').val() === '') {
                showErrorMessage('', 'Please choose a file to import');
                return false;
            }

            ImportAdvanceInventoryFunctions.submitImport().done(function (response) {

                if (!response.isSuccess) {
                    createErrorNotification('', response.error);
                    return;
                }

                if (response.isOverwrite) {
                    createWarningNotification('', 'Data will be overwritten')
                }

                $('#importAdvanceInventoryModal').modal('hide');

                ImportAdvanceInventoryFunctions.displayUpdatedAdvanceGermplasmDetails($('#advance-import-list-id').val());

                // this is to prevent the user from performing other activities even tab navigation prior to saving the imported data
                $('.import-advance-inventory').data('data-import', '1');
            });

        },

        submitImport : function() {
            'use strict';
            var deferred = $.Deferred();
            $('#importAdvanceInventoryForm').ajaxForm({
                dataType: 'json',
                success: function (response) {
                    deferred.resolve(response);
                },
                error: function (response) {

                    createErrorNotification('', 'Error occurred while importing file');

                    deferred.reject(response);
                }
            }).submit();

            return deferred.promise();
        },

        saveImport : function() {
            $.post( '/Fieldbook/importAdvanceInventory/save', function( result ) {
              if (result.isSuccess) {
                  ImportAdvanceInventoryFunctions.displayFinalizedAdvancedInventoryDetails(getCurrentAdvanceTabListIdentifier());
              }
            })
        },

        discardImport : function() {
            ImportAdvanceInventoryFunctions.displayFinalizedAdvancedInventoryDetails(getCurrentAdvanceTabListIdentifier());
        },

        displayFinalizedAdvancedInventoryDetails : function(listId) {
            'use strict';
            $.ajax({
                url: "/Fieldbook/SeedStoreManager/advance/displayGermplasmDetails/" + listId,
                type: "GET",
                cache: false,
                success: function (html) {
                    $('.import-advance-inventory').data('data-import', '0');
                    $('#advance-list' + getCurrentAdvanceTabTempIdentifier()).html(html);
                }
            });
        },

        displayUpdatedAdvanceGermplasmDetails : function(listId) {
        	'use strict';
        	$.ajax({
        		url: "/Fieldbook/importAdvanceInventory/displayGermplasmDetails/" + listId,
        		type: "GET",
        		cache: false,
        		success: function(html) {
        			$('#advance-list' + getCurrentAdvanceTabTempIdentifier()).html(html);
        		}
        	});
        }
    };
}