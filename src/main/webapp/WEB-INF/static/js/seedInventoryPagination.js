// This js file is used in seedInventoryPagination.html.
var InventoryPage = {
    setupPage: function() {
        var listDivIdentifier = $('#manage-trial-tab-headers .active').children('a').attr('tab-data');
        var sectionContainerDiv = 'stock-content-pane' + listDivIdentifier;
        var inventoryTableId = 'inventory-table' + listDivIdentifier;

        $('#' + getJquerySafeId(sectionContainerDiv) + ' .selectAllStock').on('change', function(event) {

            //select all the checkbox in the section container div
            //needed set time out since chrme is not able to rnder properly the checkbox if its checked or not
            setTimeout(function() {
            	var rows = $(".final-advance-list").DataTable().rows().nodes();
            	$('input[type="checkbox"]', rows).prop('checked', false).parent('td').parent('tr').removeClass('selected').removeClass('manual-selected');
                var isChecked = $('#' + getJquerySafeId(sectionContainerDiv) + ' .selectAllStock').prop('checked');
                if (isChecked) {
                	$('input[type="checkbox"]', rows).prop('checked', 'checked').parent('td').parent('tr').addClass('selected').addClass('manual-selected');
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
                	var rows = $(".final-advance-list").DataTable().rows().nodes();
                	$('input[type="checkbox"]', rows).prop('checked', false).removeClass('manual-selected');
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
            	var selectedRows = $(".final-advance-list").DataTable().rows(['.selected']).nodes();
				$('input[type="checkbox"]', selectedRows).prop('checked', true).parent('td').parent('tr').addClass('selected').addClass('manual-selected');
				var unselectedRows = $(".final-advance-list").DataTable().rows([':not(.selected)']).nodes();
				$('input[type="checkbox"]', unselectedRows).prop('checked', false).removeClass('manual-selected');

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

$(document).ready(function() {
    'use strict';
    setTimeout(InventoryPage.setupPage, 3);
});
