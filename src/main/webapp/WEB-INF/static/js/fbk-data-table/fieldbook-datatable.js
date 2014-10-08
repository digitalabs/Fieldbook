/**
 * @module measurements-datatable
 */

if (typeof (BMS) === 'undefined') {
	BMS = {};
}

if (typeof (BMS.Fieldbook) === 'undefined') {
	BMS.Fieldbook = {};
}

BMS.Fieldbook.MeasurementsDataTable = (function($) {

	// FIXME Refactor to remove some of this code from the constructor function

	/**
	 * Creates a new MeasurementsDataTable.
	 *
	 * @constructor
	 * @alias module:fieldbook-datatable
	 * @param {string} tableIdentifier the id of the table container
	 * @param {string} ajaxUrl the URL from which to retrieve table data
	 */
	var dataTableConstructor = function MeasurementsDataTable(tableIdentifier, dataList) {
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
					width: '50px',
					render: function(data, type, full, meta) {
						return '<a href="javascript: editExperiment(&quot;' + tableIdentifier + '&quot;,' +
							data + ',' + meta.row + ')" class="fbk-edit-experiment"></a>';
					}
				});
			}
		});
		table = $(tableIdentifier).DataTable({
			data: dataList,
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
				$('td', nRow).attr('nowrap','nowrap');
				return nRow;
			},
			fnInitComplete: function(oSettings, json) {
				$(tableIdentifier + '_wrapper .dataTables_length select').select2({minimumResultsForSearch: 10});
				// There is a bug in datatable for now				
				
				oSettings.oInstance.fnAdjustColumnSizing();
				oSettings.oInstance.api().colResize.init(oSettings.oInit.colResize);
				
			},
			language: {
				search: '<span class="mdt-filtering-label">Search:</span>'
			},
			dom: 'R<<"mdt-header"rli<"mdt-filtering">r><t>p>',
			// For column visibility
			colVis: {
				exclude: [0],
				restore: 'Restore',
				showAll: 'Show all'
			},
			// Problem with reordering plugin and fixed column for column re-ordering
			colReorder: {
				fixedColumns: 1
			}
		});

		
		$(tableIdentifier).dataTable().bind('sort', function() {
			$(tableIdentifier).dataTable().fnAdjustColumnSizing();
		});

		//new $.fn.dataTable.FixedColumns(table, {iLeftColumns: 3});

		$('#measurementsDiv .mdt-columns').detach().insertBefore('.mdt-filtering');
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


BMS.Fieldbook.GermplasmListDataTable = (function($) {

	// FIXME Refactor to remove some of this code from the constructor function	
	/**
	 * Creates a new MeasurementsDataTable.
	 *
	 * @constructor
	 * @alias module:fieldbook-datatable
	 * @param {string} tableIdentifier the id of the table container
	 * @param {string} parentDiv parentdiv of that contains the table
	 * @param {dataList} json representation of the data to be displayed
	 */
	var dataTableConstructor = function GermplasmListDataTable(tableIdentifier, parentDiv, dataList) {
		'use strict';
		
		var columns = [],
		columnsDef = [],
		germplasmDataTable;				

		$(tableIdentifier + ' thead tr th').each(function() {
			columns.push({data: $(this).data('col-name')});
			if ($(this).data('col-name') == 'gid') {
				// For GID
				columnsDef.push({
					targets: columns.length - 1,
					data: $(this).html(),
					width: '100px',
					render: function(data, type, full, meta) {
						return '<a class="gid-link" href="javascript: void(0)" ' +
							'onclick="javascript: openGermplasmDetailsPopopWithGidAndDesig(&quot;' +
							full.gid + '&quot;,&quot;' + full.desig + '&quot;)">' + data + '</a>';
					}
				});
			} else if ($(this).data('col-name') == 'desig') {
				// For designation
				columnsDef.push({
					targets: columns.length - 1,
					data: $(this).html(),
					render: function(data, type, full, meta) {
						return '<a class="desig-link" href="javascript: void(0)" ' +
							'onclick="javascript: openGermplasmDetailsPopopWithGidAndDesig(&quot;' +
							full.gid + '&quot;,&quot;' + full.desig + '&quot;)">' + data + '</a>';
					}
				});
			}
		});

		this.germplasmDataTable = $(tableIdentifier).dataTable({
			data: dataList,
			columns: columns,
			columnDefs: columnsDef,
			scrollY: '500px',
			scrollX: '100%',
			scrollCollapse: true,
		  dom: 'R<t><"fbk-page-div"p>',
		  iDisplayLength: 100,
		  fnDrawCallback: function( oSettings ) {
			  makeDraggable(true);
		    },
		  fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {			
				$(nRow).data('entry', aData.entry);
				$(nRow).data('gid', aData.gid);
				$(nRow).data('index', aData.position);
												
				$(nRow).addClass('draggable primaryRow');
				$('td', nRow).attr('nowrap','nowrap');
				return nRow;
			},
		  fnInitComplete: function(oSettings, json) {
				
				var totalPages = oSettings._iDisplayLength === -1 ? 0 : Math.ceil( oSettings.fnRecordsDisplay() / oSettings._iDisplayLength );
				if(totalPages === 1){
					$(parentDiv +' .fbk-page-div').addClass('fbk-hide');
				}
				$(parentDiv).removeClass('fbk-hide-opacity');		
				oSettings.oInstance.fnAdjustColumnSizing();
				oSettings.oInstance.api().colResize.init(oSettings.oInit.colResize);
				oSettings.oInstance.fnAdjustColumnSizing();
			}
		});
		
		GermplasmListDataTable.prototype.getDataTable = function()
		{
		    return this.germplasmDataTable;
		};
	};

	return dataTableConstructor;

})(jQuery);

BMS.Fieldbook.TrialGermplasmListDataTable = (function($) {

	var dataTableConstructor = function TrialGermplasmListDataTable(tableIdentifier, parentDiv, dataList) {
		'use strict';
		
		var columns = [],
		columnsDef = [],
		defaultOrdering = [],
		table;				
		$(tableIdentifier + ' thead tr th').each(function() {
			columns.push({data: $(this).data('col-name')});
			if($(this).data('col-name') == '8230-key'){
				defaultOrdering = [columns.length - 1, 'asc'];
			}
			else if ($(this).data('col-name') == '8240-key') {
				// For GID
				columnsDef.push({
					targets: columns.length - 1,
					data: $(this).data('col-name'),
					render: function(data, type, full, meta) {
						return '<a class="gid-link" href="javascript: void(0)" ' +
							'onclick="javascript: openGermplasmDetailsPopopWithGidAndDesig(&quot;' +
							full.gid + '&quot;,&quot;' + full.desig + '&quot;)">' + data + '</a>';
					}
				});
			} else if ($(this).data('col-name') == '8250-key') {
				// For designation
				columnsDef.push({
					targets: columns.length - 1,
					data: $(this).data('col-name'),
					render: function(data, type, full, meta) {
						return '<a class="desig-link" href="javascript: void(0)" ' +
							'onclick="javascript: openGermplasmDetailsPopopWithGidAndDesig(&quot;' +
							full.gid + '&quot;,&quot;' + full.desig + '&quot;)">' + data + '</a>';
					}
				});
			}else if ($(this).data('col-name') == '8255-key') {
				// For check
				columnsDef.push({
					targets: columns.length - 1,
					data: $(this).data('col-name'),
					render: function(data, type, full, meta) {
						var fieldName = 'selectedCheck',
							count = 0,							
							actualVal = '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;',
							actualCode = '',
							domElem = '';
													
						for(count = 0 ; count < full.checkOptions.length ; count++){							
							if(full.checkOptions[count].id == full['8255-key']){
								actualVal = full.checkOptions[count].description;
								actualCode = full.checkOptions[count].name;
								domElem = '<input class="check-hidden" type="hidden"  data-code="'+actualCode+'" value="'+full['8255-key']+'" id="selectedCheck'+(meta.row)+'" name="'+fieldName+'">';
								break;
							}							
						}
						if(domElem === ''){
							domElem = '<input data-index="'+meta.row+'" class="check-hidden" type="hidden"  data-code="'+actualCode+'" value="'+full['8255-key']+'" id="selectedCheck'+(meta.row)+'" name="'+fieldName+'">';
						}
						
						return '<a data-index="'+meta.row+'" class="check-href edit-check'+meta.row+'" data-code="'+actualCode+'" href="javascript: showPopoverCheck(&quot;'+(meta.row)+'&quot;, &quot;.germplasm-list-items&quot;, &quot;edit-check'+meta.row+'&quot;)">'+actualVal+'</a>' + domElem;
					}
				});
			}
		});
		this.table = $(tableIdentifier).dataTable({
			data: dataList,
			columns: columns,
			columnDefs: columnsDef,
			scrollY: '500px',
			scrollX: '100%',
			scrollCollapse: true,
			order: defaultOrdering,
			// Problem with reordering plugin and fixed column for column re-ordering
			colReorder: {
				fixedColumns: 1
			},
		  dom: 'R<t><"fbk-page-div"p>',
		  iDisplayLength: 100,
		  fnDrawCallback: function( oSettings ) {
			 
		  },
		  fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {			
				$(nRow).data('entry', aData.entry);
				$(nRow).data('gid', aData.gid);
				$(nRow).data('index', aData.position);
												
				$(nRow).addClass('primaryRow');
				$('td', nRow).attr('nowrap','nowrap');
								
				return nRow;
			},
		  fnInitComplete: function(oSettings, json) {
				
				var totalPages = oSettings._iDisplayLength === -1 ? 0 : Math.ceil( oSettings.fnRecordsDisplay() / oSettings._iDisplayLength );
				if(totalPages === 1){
					$(parentDiv +' .fbk-page-div').addClass('fbk-hide');
				}
				$(parentDiv).removeClass('fbk-hide-opacity');		
				oSettings.oInstance.fnAdjustColumnSizing();
				oSettings.oInstance.api().colResize.init(oSettings.oInit.colResize);
				oSettings.oInstance.fnAdjustColumnSizing();
			}
		});
		
		TrialGermplasmListDataTable.prototype.getDataTable = function()
		{
		    return this.table;
		};
		
		TrialGermplasmListDataTable.prototype.getDataTableColumnIndex = function(colName)
		{
			var colNames = this.table.fnSettings().aoColumns;
			for(var counter = 0 ; counter < colNames.length ; counter++){
				if(colNames[counter].data === colName){
					return colNames[counter].idx;
				}
			}
			return -1;
		};
		
		TrialGermplasmListDataTable.prototype.getDataTableColumn = function(colName)
		{
			var colNames = this.table.fnSettings().aoColumns;
			for(var counter = 0 ; counter < colNames.length ; counter++){
				if(colNames[counter].data === colName){
					return colNames[counter];
				}
			}
			return null;
		};
		
		
		
		
		$('.col-show-hide').html('');
		$('.col-show-hide').html($(parentDiv + ' .mdt-columns').clone().removeClass('fbk-hide'));
		
		$('.germplasm-dropdown-menu a').click(function(e) {
			
			e.stopPropagation();
			if ($(this).parent().hasClass('fbk-dropdown-select-fade')) {
				$(this).parent().removeClass('fbk-dropdown-select-fade');
				$(this).parent().addClass('fbk-dropdown-select-highlight');

			} else {
				$(this).parent().addClass('fbk-dropdown-select-fade');
				$(this).parent().removeClass('fbk-dropdown-select-highlight');
			}
			
			// hide germplasm column
            (function(colName){
                var column = null;
                // Get the column API object                
                if(germplasmDataTable != null){
                    column = germplasmDataTable.getDataTableColumn(colName);
                    // Toggle the visibility
                    if(column !== null){
                        germplasmDataTable.getDataTable().fnSetColumnVis( column.idx, !column.bVisible );
                    }
                }
            })($(this).attr('data-column-name'));

        });
	};

	return dataTableConstructor;

})(jQuery);



BMS.Fieldbook.SelectedCheckListDataTable = (function($) {

	// FIXME Refactor to remove some of this code from the constructor function	
	/**
	 * Creates a new MeasurementsDataTable.
	 *
	 * @constructor
	 * @alias module:fieldbook-datatable
	 * @param {string} tableIdentifier the id of the table container
	 * @param {string} parentDiv parentdiv of that contains the table
	 * @param {dataList} json representation of the data to be displayed
	 */
	var dataTableConstructor = function SelectedCheckListDataTable(tableIdentifier, parentDiv, dataList) {
		'use strict';
		
		var columns = [],
		columnsDef = [],
		checkDataTable;		
		
		$(tableIdentifier + ' thead tr th').each(function() {
			columns.push({data: $(this).data('col-name')});
			if ($(this).data('col-name') == 'desig') {
				// For designation
				columnsDef.push({
					targets: columns.length - 1,
					data: $(this).html(),					
					render: function(data, type, full, meta) {
						return '<a class="desig-link" href="javascript: void(0)" ' +
							'onclick="javascript: openGermplasmDetailsPopopWithGidAndDesig(&quot;' +
							full.gid + '&quot;,&quot;' + full.desig + '&quot;)">' + data + '</a>';
					}
				});
			}else if ($(this).data('col-name') == 'check') {
				// For designation
				columnsDef.push({
					targets: columns.length - 1,
					data: $(this).html(),					
					render: function(data, type, full, meta) {
						var fieldName = 'selectedCheck',
							count = 0,
							isSelected = '',
							actualVal = '',
							actualCode = '',
							domElem = '';
							
						
						for(count = 0 ; count < full.checkOptions.length ; count++){
							isSelected = '';
							if(full.checkOptions[count].id == full.check){
								actualVal = full.checkOptions[count].description;
								actualCode = full.checkOptions[count].name;
								domElem = '<input data-index="'+meta.row+'" class="check-hidden" type="hidden"  data-code="'+actualCode+'" value="'+full.check+'" id="selectedCheck'+(meta.row)+'" name="'+fieldName+'">';
								break;
							}							
						}
						
						return '<a data-index="'+meta.row+'" class="check-href edit-check'+meta.row+'" data-code="'+actualCode+'" href="javascript: showPopoverCheck(&quot;'+(meta.row)+'&quot;, &quot;.check-germplasm-list-items&quot;, &quot;edit-check'+meta.row+'&quot;)">'+actualVal+'</a>' + domElem;
					}
				});
			} else if ($(this).data('col-name') == 'action') {
				// For delete
				columnsDef.push({
					targets: columns.length - 1,
					width: '20px',
					data: $(this).html(),
					render: function(data, type, full, meta) {
						return '<span class="delete-icon delete-check" data-index="'+meta.row+'"></span>';
					}
				});
			}
		});


		this.checkDataTable = $(tableIdentifier).dataTable({	
			data: dataList,
			columns: columns,
			columnDefs: columnsDef,			
			scrollY: '500px',
			scrollX: '100%',
			bSort: false,
			scrollCollapse: true,
		  dom: 'R<t><"fbk-page-div"p>',
		  iDisplayLength: 100,		  
		  fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {		
		  		$(nRow).addClass('checkRow');
		  		$(nRow).data('entry', aData.entry);
				$(nRow).data('gid', aData.gid);
				$(nRow).data('index', aData.index);

				
			  	$('td', nRow).attr('nowrap','nowrap');			  	
			  	
			  	setTimeout(function(){makeCheckDraggable(makeCheckDraggableBool);}, 300);
				return nRow;
			},
		  fnInitComplete: function(oSettings, json) {
				
				var totalPages = oSettings._iDisplayLength === -1 ? 0 : Math.ceil( oSettings.fnRecordsDisplay() / oSettings._iDisplayLength );
				if(totalPages === 1){
					$(parentDiv +' .fbk-page-div').addClass('fbk-hide');
				}
				setTimeout(function(){oSettings.oInstance.fnAdjustColumnSizing();}, 1);
				//hide delete icon for read only view
				if ($('#chooseGermplasmAndChecks').data('replace') !== undefined && parseInt($('#chooseGermplasmAndChecks').data('replace')) === 0 && measurementRowCount > 0) {
					oSettings.oInstance.$('.delete-check').hide();
				}
				
		  	}
		});
		$(parentDiv + ' div.dataTables_scrollBody').scroll( 
				function(){
						$(parentDiv + ' .popover').remove();
					} 
				);
		this.checkDataTable.$('.delete-check').on('click', function(){
			
			var entryNumber = $(this).parent().parent().data('entry'),
			gid = '' + $(this).parent().parent().data('gid');
			deleteCheckGermplasmList(entryNumber, gid, $(this).parent().parent());	
			
	  	});	
		SelectedCheckListDataTable.prototype.getDataTable = function()
		{
		    return this.checkDataTable;
		};
		SelectedCheckListDataTable.prototype.getDataTableColumnIndex = function(colName)
		{
			var colNames = this.checkDataTable.fnSettings().aoColumns;
			for(var counter = 0 ; counter < colNames.length ; counter++){
				if(colNames[counter].data === colName){
					return colNames[counter].idx;
				}
			}
			return -1;
		};
	};

	return dataTableConstructor;

})(jQuery);

BMS.Fieldbook.AdvancedGermplasmListDataTable = (function($) {

	// FIXME Refactor to remove some of this code from the constructor function	
	/**
	 * Creates a new AdvancedGermplasmListDataTable.
	 *
	 * @constructor
	 * @alias module:fieldbook-datatable
	 * @param {string} tableIdentifier the id of the table container
	 * @param {string} parentDiv parentdiv of that contains the table
	 * @param {dataList} json representation of the data to be displayed
	 */
	var dataTableConstructor = function AdvancedGermplasmListDataTable(tableIdentifier, parentDiv, dataList) {
		'use strict';
		
		var columns = [],
		columnsDef = [],
		germplasmDataTable;				

		$(tableIdentifier + ' thead tr th').each(function(index) {
			columns.push({data: $(this).data('col-name')});
			
		});

		this.germplasmDataTable = $(tableIdentifier).dataTable({
			data: dataList,
			columns: columns,
			columnDefs: columnsDef,			
			scrollY: '500px',
			scrollX: '100%',
			scrollCollapse: true,
		  dom: 'R<t><"fbk-page-div"p>',
		  iDisplayLength: 100,
		  fnDrawCallback: function( oSettings ) {
			  makeDraggable(true);
		    },
		  fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {			
				$(nRow).data('entry', aData.entry);
				$(nRow).data('gid', aData.gid);
												
				$('td', nRow).attr('nowrap','nowrap');
				return nRow;
			},
		  fnInitComplete: function(oSettings, json) {
				
				var totalPages = oSettings._iDisplayLength === -1 ? 0 : Math.ceil( oSettings.fnRecordsDisplay() / oSettings._iDisplayLength );
				if(totalPages === 1){
					$(parentDiv +' .fbk-page-div').addClass('fbk-hide');
				}
				$(parentDiv).removeClass('fbk-hide-opacity');		
				oSettings.oInstance.fnAdjustColumnSizing();
				oSettings.oInstance.api().colResize.init(oSettings.oInit.colResize);
				oSettings.oInstance.fnAdjustColumnSizing();
			}
		});
		
		AdvancedGermplasmListDataTable.prototype.getDataTable = function()
		{
		    return this.germplasmDataTable;
		};
	};

	return dataTableConstructor;

})(jQuery);

BMS.Fieldbook.FinalAdvancedGermplasmListDataTable = (function($) {

	// FIXME Refactor to remove some of this code from the constructor function	
	/**
	 * Creates a new AdvancedGermplasmListDataTable.
	 *
	 * @constructor
	 * @alias module:fieldbook-datatable
	 * @param {string} tableIdentifier the id of the table container
	 * @param {string} parentDiv parentdiv of that contains the table
	 * @param {dataList} json representation of the data to be displayed
	 */
	var dataTableConstructor = function FinalAdvancedGermplasmListDataTable(tableIdentifier, parentDiv, dataList) {
		'use strict';
		
		var columns = [],
		columnsDef = [],
		aoColumnsDef = [],
		germplasmDataTable;				

		$(tableIdentifier + ' thead tr th').each(function(index) {
			columns.push({data: $(this).data('col-name')});
			if(index === 0){
				aoColumnsDef.push({bSortable: false});
			}else{
				aoColumnsDef.push(null);
			}
			
			if ($(this).data('col-name') == 'gid') {
				// For GID
				columnsDef.push({
					targets: columns.length - 1,
					data: $(this).html(),
					width: '100px',
					render: function(data, type, full, meta) {
						return '<a class="gid-link" href="javascript: void(0)" ' +
							'onclick="javascript: openGermplasmDetailsPopopWithGidAndDesig(&quot;' +
							full.gid + '&quot;,&quot;' + full.desig + '&quot;)">' + data + '</a>';
					}
				});
			} else if ($(this).data('col-name') == 'desig') {
				// For designation
				columnsDef.push({
					targets: columns.length - 1,
					data: $(this).html(),
					render: function(data, type, full, meta) {
						return '<a class="desig-link" href="javascript: void(0)" ' +
							'onclick="javascript: openGermplasmDetailsPopopWithGidAndDesig(&quot;' +
							full.gid + '&quot;,&quot;' + full.desig + '&quot;)">' + data + '</a>';
					}
				});
			}
		});
		this.germplasmDataTable = $(tableIdentifier).dataTable({
			scrollY: '500px',
			scrollX: '100%',
			scrollCollapse: true,
			aoColumns: aoColumnsDef,
			lengthMenu: [[50, 75, 100, -1], [50, 75, 100, 'All']],
		  dom: 'R<"mdt-header" rli><t><"fbk-page-div"p>',

		  iDisplayLength: 100,
		  fnDrawCallback: function( oSettings ) {
			  $(parentDiv + ' #selectAllAdvance').prop('checked', false)
			  $(parentDiv + ' #selectAllAdvance').change()
			  $(parentDiv + ' input.advancingListGid:checked').parent().parent().addClass('selected');
			  $(parentDiv + ' .numberOfAdvanceSelected').html($(parentDiv + ' tr.primaryRow.selected').length);			
		    },
		  fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {			
				$(nRow).data('entry', aData.entry);
				$(nRow).data('gid', aData.gid);
												
				$('td', nRow).attr('nowrap','nowrap');
				return nRow;
			},
		  fnInitComplete: function(oSettings, json) {
				
				var totalPages = oSettings._iDisplayLength === -1 ? 0 : Math.ceil( oSettings.fnRecordsDisplay() / oSettings._iDisplayLength );
				if(totalPages === 1){
					$(parentDiv +' .fbk-page-div').addClass('fbk-hide');
				}
				$(parentDiv).removeClass('fbk-hide-opacity');		
				oSettings.oInstance.fnAdjustColumnSizing();
				oSettings.oInstance.api().colResize.init(oSettings.oInit.colResize);
				$(parentDiv + ' .dataTables_length select').select2({minimumResultsForSearch: 10});
				oSettings.oInstance.fnAdjustColumnSizing();
			}
		});
		
		FinalAdvancedGermplasmListDataTable.prototype.getDataTable = function()
		{
		    return this.germplasmDataTable;
		};
	};

	return dataTableConstructor;

})(jQuery);
