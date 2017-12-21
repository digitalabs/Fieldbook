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

                var isChecked = $('#' + sectionContainerDiv + ' .selectAllStock').prop('checked');
                $('#' + inventoryTableId + ' tr').removeClass('selected');
                $('#' + inventoryTableId + ' tr').removeClass('manual-selected');
                $('#' + sectionContainerDiv + ' input.stockListEntryId').prop('checked', isChecked);
                if (isChecked) {
                    $('#' + inventoryTableId + ' tr').addClass('selected');
                    $('#' + inventoryTableId + ' tr').addClass('manual-selected');
                }

                var oTable = $('#' + inventoryTableId).dataTable();

                $('#' + sectionContainerDiv + ' .numberOfAdvanceSelected').html(oTable.api().rows(':has(input.stockListEntryId:checked)').data().length);

            }, 10);

        });

        $('#' + inventoryTableId).tableSelect({
            onClick: function(row) {

                //we clear all check
                if ($('#' + inventoryTableId).data('check-click') === '1') {
                    $('#' + inventoryTableId).data('check-click', '0');
                    $('#' + inventoryTableId + ' tr.manual-selected').addClass('selected');
                    $('#' + inventoryTableId + ' tr:not(.manual-selected) input.stockListEntryId:checked').parent().parent().addClass('selected');
                    $('#' + inventoryTableId + ' tr.selected input.stockListEntryId:not(:checked)').parent().parent().removeClass('selected');
                } else {
                    $('#' + inventoryTableId + ' input.stockListEntryId').prop('checked', false);
                    $('#' + inventoryTableId + ' tr.manual-selected').removeClass('manual-selected');
                    if ($(row).hasClass('selected')) {
                        $(row).find('input.stockListEntryId').prop('checked', true);
                    } else {
                        $(row).find('input.stockListEntryId').prop('checked', false);
                    }
                }

                var oTable = $('#' + inventoryTableId).dataTable();

                $('#' + getJquerySafeId(sectionContainerDiv) + ' .numberOfAdvanceSelected').html(oTable.api().rows(':has(input.stockListEntryId:checked)').data().length);
            },
            onCtrl: function(row) {

                $('#' + inventoryTableId + ' tr.selected input.stockListEntryId').prop('checked', true);
                $('#' + inventoryTableId + ' tr:not(.selected) input.stockListEntryId').prop('checked', false);
                if ($(row).hasClass('manual-selected') || $(row).hasClass('selected')) {
                    $(row).find('input.stockListEntryId').prop('checked', true);
                    $(row).addClass('selected');
                } else {
                    $(row).find('input.stockListEntryId').prop('checked', false);
                    $(row).removeClass('selected');
                }
                if ($('#' + inventoryTableId).data('check-click') === '1') {
                    $('#' + inventoryTableId).data('check-click', '0');
                    if ($(row).hasClass('selected') && $(row).hasClass('manual-selected') === false) {
                        $(row).find('input.stockListEntryId').prop('checked', false);
                        $(row).removeClass('selected');
                    }
                }

                var oTable = $('#' + inventoryTableId).dataTable();

                $('#' + sectionContainerDiv + ' .numberOfAdvanceSelected').html(oTable.api().rows(':has(input.stockListEntryId:checked)').data().length);
            },
            onShift: function() {
                $('#' + inventoryTableId + ' tr.manual-selected-dummy').addClass('selected');
                $('#' + inventoryTableId + ' tr.selected input.stockListEntryId').prop('checked', true);
                $('#' + inventoryTableId + ' tr:not(.selected) input.stockListEntryId').prop('checked', false);

                var oTable = $('#' + inventoryTableId).dataTable();

                $('#' + sectionContainerDiv + ' .numberOfAdvanceSelected').html(oTable.api().rows(':has(input.stockListEntryId:checked)').data().length);
            }

        });

        $('#' + inventoryTableId + ' input.stockListEntryId').on('click', function() {
            $('#' + inventoryTableId).data('check-click', '1');
            if ($(this).is(':checked')) {
                //we highlight
                $(this).parent().parent().addClass('selected');
                $(this).parent().parent().addClass('manual-selected');
            } else {
                $(this).parent().parent().removeClass('selected');
                $(this).parent().parent().removeClass('manual-selected');
                // Deselect "Select All" check box from header as well as from bottom while selecting any one row from table
                $('#' + sectionContainerDiv + ' .selectAllStock').prop('checked', false);
            }
            $('#' + inventoryTableId + ' tr.manual-selected input.stockListEntryId').prop('checked', true);
            $('#' + inventoryTableId + ' tr.manual-selected').addClass('selected');
            $('#' + inventoryTableId + ' tr:not(.manual-selected)').remove('selected');

            var oTable = $('#' + inventoryTableId).dataTable();

            $('#' + sectionContainerDiv + ' .numberOfAdvanceSelected').html(oTable.api().rows(':has(input.stockListEntryId:checked)').data().length);
        });

        new BMS.Fieldbook.StockListDataTable('#' + inventoryTableId, '#' + sectionContainerDiv, null, false);
        $('#' + sectionContainerDiv + ' .main-inventory').css('opacity', 1);
    }
};

$(document).ready(function() {
    'use strict';
    setTimeout(InventoryPage.setupPage, 100);
});
