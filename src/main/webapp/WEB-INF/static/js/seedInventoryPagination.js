// This js file is used in seedInventoryPagination.html.
var InventoryPage = {
    setupPage: function() {
        var listDivIdentifier  = '';
        if(isNursery()) {
            if ($('#create-nursery-tab-headers .tabdrop').hasClass('active')) {
                //means the active is in the tab drop
                listDivIdentifier = $('#create-nursery-tab-headers .tabdrop li.active .fbk-close-tab').attr('id');
            } else {
                listDivIdentifier = $('#create-nursery-tab-headers li.active .fbk-close-tab').attr('id');
            }
        } else {
            listDivIdentifier = $('#manage-trial-tab-headers .active').children('a').attr('tab-data');
        }

        var sectionContainerDiv = 'stock-content-pane' + listDivIdentifier;
        var inventoryTableId = 'inventory-table' + listDivIdentifier;

        $('#' + getJquerySafeId(sectionContainerDiv) + ' .selectAllStock').on('change', function(event) {

            //select all the checkbox in the section container div
            //needed set time out since chrme is not able to rnder properly the checkbox if its checked or not
            setTimeout(function() {

                var isChecked = $('#' + getJquerySafeId(sectionContainerDiv) + ' .selectAllStock').prop('checked');
                $('#' + getJquerySafeId(inventoryTableId) + ' tr').removeClass('selected');
                $('#' + getJquerySafeId(inventoryTableId) + ' tr').removeClass('manual-selected');
                $('#' + getJquerySafeId(sectionContainerDiv) + ' input.stockListEntryId').prop('checked', isChecked);
                $('#' + getJquerySafeId(sectionContainerDiv) + ' .review-select-all').prop('checked', isChecked);

                if (isChecked) {
                    $('#' + getJquerySafeId(inventoryTableId) + ' tr').addClass('selected');
                    $('#' + getJquerySafeId(inventoryTableId) + ' tr').addClass('manual-selected');
                }

                var oTable = $('#' + getJquerySafeId(inventoryTableId)).dataTable();

                $('#' + getJquerySafeId(sectionContainerDiv) + ' .numberOfAdvanceSelected').html(oTable.api().rows(':has(input.stockListEntryId:checked)').data().length);

            }, 10);

        });

        $('#' + getJquerySafeId(inventoryTableId)).tableSelect({
            onClick: function(row) {

                //we clear all check
                if ($('#' + getJquerySafeId(inventoryTableId)).data('check-click') === '1') {
                    $('#' + getJquerySafeId(inventoryTableId)).data('check-click', '0');
                    $('#' + getJquerySafeId(inventoryTableId) + ' tr.manual-selected').addClass('selected');
                    $('#' + getJquerySafeId(inventoryTableId) + ' tr:not(.manual-selected) input.stockListEntryId:checked').parent().parent().addClass('selected');
                    $('#' + getJquerySafeId(inventoryTableId) + ' tr.selected input.stockListEntryId:not(:checked)').parent().parent().removeClass('selected');
                } else {
                    $('#' + getJquerySafeId(inventoryTableId) + ' input.stockListEntryId').prop('checked', false);
                    $('#' + getJquerySafeId(inventoryTableId) + ' tr.manual-selected').removeClass('manual-selected');
                    if ($(row).hasClass('selected')) {
                        $(row).find('input.stockListEntryId').prop('checked', true);
                    } else {
                        $(row).find('input.stockListEntryId').prop('checked', false);
                    }
                }

                var oTable = $('#' + getJquerySafeId(inventoryTableId)).dataTable();

                $('#' + getJquerySafeId(sectionContainerDiv) + ' .numberOfAdvanceSelected').html(oTable.api().rows(':has(input.stockListEntryId:checked)').data().length);
            },
            onCtrl: function(row) {

                $('#' + getJquerySafeId(inventoryTableId) + ' tr.selected input.stockListEntryId').prop('checked', true);
                $('#' + getJquerySafeId(inventoryTableId) + ' tr:not(.selected) input.stockListEntryId').prop('checked', false);
                if ($(row).hasClass('manual-selected') || $(row).hasClass('selected')) {
                    $(row).find('input.stockListEntryId').prop('checked', true);
                    $(row).addClass('selected');
                } else {
                    $(row).find('input.stockListEntryId').prop('checked', false);
                    $(row).removeClass('selected');
                }
                if ($('#' + getJquerySafeId(inventoryTableId)).data('check-click') === '1') {
                    $('#' + getJquerySafeId(inventoryTableId)).data('check-click', '0');
                    if ($(row).hasClass('selected') && $(row).hasClass('manual-selected') === false) {
                        $(row).find('input.stockListEntryId').prop('checked', false);
                        $(row).removeClass('selected');
                    }
                }

                var oTable = $('#' + getJquerySafeId(inventoryTableId)).dataTable();

                $('#' + getJquerySafeId(sectionContainerDiv) + ' .numberOfAdvanceSelected').html(oTable.api().rows(':has(input.stockListEntryId:checked)').data().length);
            },
            onShift: function() {
                $('#' + getJquerySafeId(inventoryTableId) + ' tr.manual-selected-dummy').addClass('selected');
                $('#' + getJquerySafeId(inventoryTableId) + ' tr.selected input.stockListEntryId').prop('checked', true);
                $('#' + getJquerySafeId(inventoryTableId) + ' tr:not(.selected) input.stockListEntryId').prop('checked', false);

                var oTable = $('#' + getJquerySafeId(inventoryTableId)).dataTable();

                $('#' + getJquerySafeId(sectionContainerDiv) + ' .numberOfAdvanceSelected').html(oTable.api().rows(':has(input.stockListEntryId:checked)').data().length);
            }

        });

        $('#' + getJquerySafeId(inventoryTableId) + ' input.stockListEntryId').on('click', function() {
            $('#' + getJquerySafeId(inventoryTableId)).data('check-click', '1');
            if ($(this).is(':checked')) {
                //we highlight
                $(this).parent().parent().addClass('selected');
                $(this).parent().parent().addClass('manual-selected');
            } else {
                $(this).parent().parent().removeClass('selected');
                $(this).parent().parent().removeClass('manual-selected');
                // Deselect "Select All" check box from header as well as from bottom while selecting any one row from table
                $('#' + getJquerySafeId(sectionContainerDiv) + ' .review-select-all').prop('checked', false);
                $('#' + getJquerySafeId(sectionContainerDiv) + ' .selectAllStock').prop('checked', false);
            }
            $('#' + getJquerySafeId(inventoryTableId) + ' tr.manual-selected input.stockListEntryId').prop('checked', true);
            $('#' + getJquerySafeId(inventoryTableId) + ' tr.manual-selected').addClass('selected');
            $('#' + getJquerySafeId(inventoryTableId) + ' tr:not(.manual-selected)').remove('selected');

            var oTable = $('#' + getJquerySafeId(inventoryTableId)).dataTable();

            $('#' + getJquerySafeId(sectionContainerDiv) + ' .numberOfAdvanceSelected').html(oTable.api().rows(':has(input.stockListEntryId:checked)').data().length);
        });

        new BMS.Fieldbook.StockListDataTable('#' + getJquerySafeId(inventoryTableId), '#' + getJquerySafeId(sectionContainerDiv), null, false);
        $('#' + sectionContainerDiv + ' .main-inventory').css('opacity', 1);
    }
};

function selectAllEntries() {
    // Select all entries of table based on the bottom select all checkbox
    var listDivIdentifier = '';
    if (isNursery()) {
        if ($('#create-nursery-tab-headers .tabdrop').hasClass('active')) {
            //means the active is in the tab drop
            listDivIdentifier = $('#create-nursery-tab-headers .tabdrop li.active .fbk-close-tab').attr('id');
        } else {
            listDivIdentifier = $('#create-nursery-tab-headers li.active .fbk-close-tab').attr('id');
        }
    } else {
        listDivIdentifier = $('#manage-trial-tab-headers .active').children('a').attr('tab-data');
    }

    var sectionContainerDiv = 'stock-content-pane' + listDivIdentifier;
    var inventoryTableId = 'inventory-table' + listDivIdentifier;

    var isChecked = $('#' + getJquerySafeId(sectionContainerDiv) + ' .review-select-all').prop('checked');

    $('#' + getJquerySafeId(sectionContainerDiv) + ' input.stockListEntryId').prop('checked', isChecked);
    $('#' + getJquerySafeId(sectionContainerDiv) + ' .selectAllStock').prop('checked', isChecked);

    if (isChecked) {
        $('#' + getJquerySafeId(inventoryTableId) + ' tr').addClass('selected');
        $('#' + getJquerySafeId(inventoryTableId) + ' tr').addClass('manual-selected');
    } else {
        $('#' + getJquerySafeId(inventoryTableId) + ' tr').removeClass('selected');
        $('#' + getJquerySafeId(inventoryTableId) + ' tr').removeClass('manual-selected');
    }

    var oTable = $('#' + getJquerySafeId(inventoryTableId)).dataTable();
    // Display the total number of records selected
    $('#' + getJquerySafeId(sectionContainerDiv) + ' .numberOfAdvanceSelected').html(oTable.api().rows(':has(input.stockListEntryId:checked)').data().length);
}

$(document).ready(function() {
    'use strict';
    setTimeout(InventoryPage.setupPage, 3);
});
