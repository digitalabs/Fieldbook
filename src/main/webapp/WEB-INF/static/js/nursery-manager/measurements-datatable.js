/**
 * @module measurements-datatable
 */

if (typeof (BMS) === 'undefined') {
	BMS = {};
}

if (typeof (BMS.NurseryManager) === 'undefined') {
	BMS.NurseryManager = {};
}

BMS.NurseryManager.MeasurementsDataTable = (function($) {

	// FIXME Refactor to remove some of this code from the constructor function

	/**
	 * Creates a new MeasurementsDataTable.
	 *
	 * @constructor
	 * @alias module:measurements-datatable
	 * @param {string} tableIdentifier the id of the table container
	 * @param {string} ajaxUrl the URL from which to retrieve table data
	 */
	var dataTableConstructor = function MeasurementsDataTable(tableIdentifier, ajaxUrl) {
		'use strict';

		var columns = [],
			columnsDef = [],
			table;

		$(tableIdentifier + ' thead tr th').each(function() {
			columns.push({data: $(this).html()});
			if ($(this).data('term-id') == '8240') {
				// For GID
				columnsDef.push({
					targets: columns.length - 1,
					data: $(this).html(),
					width: '100px',
					render: function(data, type, full, meta) {
						return '<a class="gid-link" href="javascript: void(0)" ' +
							'onclick="javascript: openGermplasmDetailsPopopWithGidAndDesig(&quot;' +
							full.GID + '&quot;,&quot;' + full.DESIGNATION + '&quot;)">' + data + '</a>';
					}
				});
			} else if ($(this).data('term-id') == '8250') {
				// For designation
				columnsDef.push({
					targets: columns.length - 1,
					data: $(this).html(),
					render: function(data, type, full, meta) {
						return '<a class="desig-link" href="javascript: void(0)" ' +
							'onclick="javascript: openGermplasmDetailsPopopWithGidAndDesig(&quot;' +
							full.GID + '&quot;,&quot;' + full.DESIGNATION + '&quot;)">' + data + '</a>';
					}
				});
			} else if ($(this).data('term-id') == 'Action') {
				// For designation
				columnsDef.push({
					targets: columns.length - 1,
					data: $(this).html(),
					render: function(data, type, full, meta) {
						return '<a href="javascript: editExperiment(&quot;' + tableIdentifier + '&quot;,' +
							data + ',' + meta.row + ')" class="fbk-edit-experiment"></a>';
					}
				});
			}
		});

		table = $(tableIdentifier).DataTable({
			ajax: ajaxUrl,
			columns: columns,
			scrollY: '500px',
			scrollX: '100%',
			scrollCollapse: true,
			columnDefs: columnsDef,
			lengthMenu: [[50, 75, 100, -1], [50, 75, 100, 'All']],
			bAutoWidth: true,
			iDisplayLength: 100,
			fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
				var toolTip = 'GID: ' + aData.GID + ' Designation: ' + aData.DESIGNATION;
				// Assuming ID is in last column
				$(nRow).attr('id', aData.experimentId);
				$(nRow).attr('title', toolTip);
				return nRow;
			},
			fnInitComplete: function(oSettings, json) {
				$(tableIdentifier + '_wrapper .dataTables_length select').select2({minimumResultsForSearch: 10});
				// There is a bug in datatable for now
				setTimeout(function() {$(tableIdentifier).dataTable().fnAdjustColumnSizing();}, 1000);
			},
			language: {
				search: '<span class="mdt-filtering-label">Search:</span>'
			},
			dom: '<<"mdt-header"rli<"mdt-filtering"f>r><t>p>',
			// For column visibility
			colVis: {
				exclude: [0],
				restore: 'Restore',
				showAll: 'Show all'
			},
			// Problem with reordering plugin and fixed column for column re-ordering
			colReorder: {
				fixedColumns: 3
			}
		});

		$(tableIdentifier).dataTable().bind('sort', function() {
			$(tableIdentifier).dataTable().fnAdjustColumnSizing();
		});

		new $.fn.dataTable.FixedColumns(table, {iLeftColumns: 3});

		$('.mdt-columns').detach().insertBefore('.mdt-filtering');
		$('.measurement-dropdown-menu a').click(function(e) {
			var column;

			e.stopPropagation();
			if ($(this).parent().hasClass('fbk-dropdown-select-fade')) {
				$(this).parent().removeClass('fbk-dropdown-select-fade');
				$(this).parent().addClass('fbk-dropdown-select-highlight');

			} else {
				$(this).parent().addClass('fbk-dropdown-select-fade');
				$(this).parent().removeClass('fbk-dropdown-select-highlight');
			}

			// Get the column API object
			column = table.column($(this).attr('data-index'));
			// Toggle the visibility
			column.visible(!column.visible());
		});
	}

	return dataTableConstructor;

})(jQuery);
