/**
 * @module measurements-datatable
 */
if (typeof (BMS) === 'undefined') {
	BMS = {};
}
if (typeof (BMS.Fieldbook) === 'undefined') {
	BMS.Fieldbook = {};
}

BMS.Fieldbook.MeasurementsTable = {
		getColumnOrdering : function(tableName, forceGet){
			var orderedColumns = [];
			var hasOrderingChange = false;
			if($('body').data('columnReordered') === '1'){
				hasOrderingChange = true;
			}
			if($('#'+tableName).dataTable() !== null &&  $('#'+tableName).dataTable().fnSettings() !== null){
				var cols = $('#'+tableName).dataTable().fnSettings().aoColumns;				
				$(cols).each(function(index){  
				  var termId = $($(cols[index].nTh)[0]).attr('data-term-id');
				  var prevIndex = $('#'+tableName).dataTable().fnSettings().aoColumns[index]._ColReorder_iOrigCol;
				  
				  if(termId != 'Action'){
					  if(index != prevIndex){
						  hasOrderingChange = true;
					  }
					  orderedColumns[orderedColumns.length] = termId;
				  }
				});
			}
			if(forceGet || hasOrderingChange){
				return orderedColumns;
			}
			//we return blank if there is no ordering change
			return [];
		},
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
			if ($(this).data('term-data-type-id') == '1110'){
				var minVal = ($(this).data('min-range'));
				var maxVal = ($(this).data('max-range'));
				
				var termId = $(this).data('term-id');
				var isVariates = $(this).hasClass('variates');
				columnsDef.push({
					targets: columns.length - 1,
					createdCell: function (td, cellData, rowData, row, col) {
						if(isVariates){
							$(td).addClass('numeric-variable');
							var cellText = $(td).text();
							if (minVal != null && maxVal != null && (parseFloat(minVal) > parseFloat(cellText) || parseFloat(cellText) > parseFloat(maxVal))){
								$(td).removeClass('accepted-value');
						    	$(td).removeClass('invalid-value');
						    	
						    	if ($(td).text() !== 'missing' ){
						    		
						    		if ($(td).find("input[type='hidden']").val() === 'true'){
										$(td).addClass('accepted-value');
									}else{
										$(td).addClass('invalid-value');
									}
						    	}															
						    }
						}
					    $(td).data('term-id', termId);
					},
					render: function ( data, type, full, meta ) {
					      return ((data[0] != null) ? data[0] :  '') + "<input type='hidden' value='" + data[1] + "' />";

					}
				});
				
			}else if ($(this).data('term-data-type-id') == '1130'){
				if($(this).data('term-valid-values') == null){
					$(this).data('term-valid-values', '');
				}
				var possibleValues = $(this).data('term-valid-values').split('|');
				var termId = $(this).data('term-id');
				var isVariates = $(this).hasClass('variates');
				columnsDef.push({
					targets: columns.length - 1,
					createdCell: function (td, cellData, rowData, row, col) {
						if(isVariates){
							var cellText = $(td).text();
							if ($.inArray(cellText, possibleValues) === -1){
								
								$(td).removeClass('accepted-value');
						    	$(td).removeClass('invalid-value');
						    	
						    	if ($(td).text() !== 'missing' ){
						    		if ($(td).find("input[type='hidden']").val() === 'true'){
										$(td).addClass('accepted-value');
									}else{
										$(td).addClass('invalid-value');
									}
						    	}
								
								
						    }
						}
					    $(td).data('term-id', termId);
					},
					render: function ( data, type, full, meta ) {
					      return ((data[0] != null) ? data[0] :  '') + "<input type='hidden' value='" + data[1] + "' />";

					}
				});
			}
			
			
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
				$(nRow).data('row-index', this.fnGetPosition( nRow ));
				$(nRow).attr('title', toolTip);
				$('td', nRow).attr('nowrap','nowrap');
				
				$(nRow).find('.accepted-value, .invalid-value, .numeric-variable').each(function (){

					var termId = $(this).data('term-id');
					var cellData = $(this).text();
					if (termId != undefined) {
						var possibleValues = $(tableIdentifier + " thead tr th[data-term-id='" + termId + "']").data('term-valid-values');
						var dataTypeId = $(tableIdentifier + " thead tr th[data-term-id='" + termId + "']").data('term-data-type-id');
						if(dataTypeId == '1110'){
							var minVal = ( $(tableIdentifier + " thead tr th[data-term-id='" + termId + "']").data('min-range'));
							var maxVal = ( $(tableIdentifier + " thead tr th[data-term-id='" + termId + "']").data('max-range'));														
							var isVariates =  $(tableIdentifier + " thead tr th[data-term-id='" + termId + "']").hasClass('variates');
							
							if(isVariates){
								$(this).removeClass('accepted-value');
								$(this).removeClass('invalid-value');
								if (minVal != null && maxVal != null && (parseFloat(minVal) > parseFloat(cellData) || parseFloat(cellData) > parseFloat(maxVal))){															    	
							    	if (cellData !== 'missing' ){
							    		
							    		if ($(this).find("input[type='hidden']").val() === 'true'){
							    			$(this).addClass('accepted-value');
										}else{
											$(this).addClass('invalid-value');
										}
							    	}														
						    	}
							}
								
						}else if (possibleValues != undefined){
							  var values = possibleValues.split('|');
							  
							  $(this).removeClass('accepted-value');
					    	  $(this).removeClass('invalid-value');
					    	  
					    	  if (cellData !== '' && cellData !== 'missing'){
					    		  if ($.inArray(cellData, values) === -1 && $(this).find("input[type='hidden']").val() !== 'true') {
					    			  	if($(this).data('is-accepted') === '1'){
					    			  		$(this).addClass('accepted-value');
										}else if($(this).data('is-accepted') === '0'){
											$(this).removeClass('invalid-value').removeClass('accepted-value');
										}else{
											$(this).addClass('invalid-value');
										}							    	  
							    	  $(this).data('term-id', $(this).data('term-id'));
							      }else{
							    	  $(this).addClass('accepted-value');
							      }
					    	  }
							  
						}
						
					}
					
				});
				
				return nRow;
			},
			fnInitComplete: function(oSettings, json) {
				$(tableIdentifier + '_wrapper .dataTables_length select').select2({minimumResultsForSearch: 10});
				oSettings.oInstance.fnAdjustColumnSizing();
				oSettings.oInstance.api().colResize.init(oSettings.oInit.colResize);
				if(this.$('.invalid-value').length !== 0) {
					$('#review-out-of-bounds-data-list').show();
				} else {
					$('#review-out-of-bounds-data-list').hide();
				}
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
		
		if($('#studyId').val() != ''){
			// Activate an inline edit on click of a table cell
		    $(tableIdentifier).on( 'click', 'tbody td:not(:first-child)', function (e) {
		    	
		    	if(isAllowedEditMeasurementDataCell(false)){
			    	var $tdCell = $(this);	    	
			        var cellTdIndex =  $(this).index();
			        var rowIndex = $(this).parent('tr').data('row-index');	         
			        
			        var $colHeader = $('#measurementsDiv .dataTables_scrollHead table th:eq('+cellTdIndex+')');	        
			        $(tableIdentifier).data('show-inline-edit', '1');	
			        if($colHeader.hasClass('variates')){
			        	$('body').data('last-td-time-clicked', new Date().getTime());
			    	}
			        if($colHeader.hasClass('factors')){
			        	//we should now submit it
			        	processInlineEditInput();
			        }else if($colHeader.hasClass('variates') && $tdCell.data('is-inline-edit') !== '1'){
			        	processInlineEditInput();
			        	if($('#measurement-table').data('show-inline-edit') === '1'){
					        $.ajax({
								url: '/Fieldbook/Common/addOrRemoveTraits/update/experiment/cell/'+rowIndex+'/'+$colHeader.data('term-id'),
								type: 'GET',
								success: function(data) {
									$tdCell.html(data);
									$tdCell.data('is-inline-edit', '1');
								}
					        });
			        	}
			        }
		    	}
		    } );
		}
		$(tableIdentifier).dataTable().bind('sort', function() {
			$(tableIdentifier).dataTable().fnAdjustColumnSizing();
		});
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
			var colIndex = $(this).attr('data-index');
			 
			
			var cols = $(tableIdentifier).dataTable().fnSettings().aoColumns;				
			$(cols).each(function(index){  
			  var prevIndex = $(tableIdentifier).dataTable().fnSettings().aoColumns[index]._ColReorder_iOrigCol;
			  if(colIndex == prevIndex){
				  column = table.column(index);
					// Toggle the visibility
				  column.visible(!column.visible());			
			  }			  
			});
			
		});
	};
	return dataTableConstructor;

})(jQuery);

BMS.Fieldbook.ReviewDetailsOutOfBoundsDataTable = (function($) {
	// FIXME Refactor to remove some of this code from the constructor function
	/**
	 * Creates a new ReviewDetailsOutOfBoundsDataTable.
	 *
	 * @constructor
	 * @alias module:fieldbook-datatable
	 * @param {string} tableIdentifier the id of the table container
	 * @param {string} ajaxUrl the URL from which to retrieve table data
	 */
	var dataTableConstructor = function ReviewDetailsOutOfBoundsDataTable(tableIdentifier, dataList) {
		'use strict';

		var columns = [],
			columnsDef = [],
			table;

		$(tableIdentifier + ' thead tr th').each(function() {
			
			if (($(this).data('term-id') === 'Check')){
				columns.push({
		            data:   "active",
		            render: function ( data, type, row ) {
		            	return '<input data-row-index="' + row.MEASUREMENT_ROW_INDEX + '" type="checkbox" class="editor-active" data-binding>';
		            },
					className:"fbk-center"
		        });
			} else if (($(this).data('term-id') === 'NewValue')){
				columns.push({
		            data:   "newValue",
		            render: function ( data, type, row ) {
		            	return '<input data-row-index="' + row.MEASUREMENT_ROW_INDEX + '" type="text" class="form-control" data-binding />';
		            }
		        });
			} else {
				columns.push({data: $(this).html()});
			}
			
			if ($(this).data('term-data-type-id') == '1130'){
				columnsDef.push({
					targets: columns.length - 1,
					render: function ( data, type, full, meta ) {
					    return ((data[0] != null) ? data[0] :  '');
					}
				});
			}
		});
		table = $(tableIdentifier).DataTable({
			data: dataList,
			columns: columns,
			scrollY: '400px',
			scrollX: '100%',
			scrollCollapse: true,
			columnDefs: columnsDef,
			lengthMenu: [[50, 75, 100, -1], [50, 75, 100, 'All']],
			bAutoWidth: true,
			iDisplayLength: 100,
			fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {

				// Assuming ID is in last column
				$(nRow).attr('id', aData.experimentId);
				$(nRow).data('row-index', this.fnGetPosition( nRow ));

				$('td', nRow).attr('nowrap','nowrap');
				$('td', nRow).attr('nowrap','nowrap');
				
				return nRow;
			},
			fnInitComplete: function(oSettings, json) {
				$(tableIdentifier + '_wrapper .dataTables_length select').select2({minimumResultsForSearch: 10});
				oSettings.oInstance.fnAdjustColumnSizing();
				oSettings.oInstance.api().colResize.init(oSettings.oInit.colResize);
			},
			language: {
				search: '<span class="mdt-filtering-label">Search:</span>'
			},
			dom: 'R<<"mdt-header"rli<"mdt-filtering">r><t>p>',
			// Problem with reordering plugin and fixed column for column re-ordering
			colReorder: {
				fixedColumns: 1
			}
		});
		
		$(tableIdentifier).dataTable().bind('sort', function() {
			$(tableIdentifier).dataTable().fnAdjustColumnSizing();
		});

	};

	return dataTableConstructor;

})(jQuery);

BMS.Fieldbook.PreviewCrossesDataTable = (function($) {
	// FIXME Refactor to remove some of this code from the constructor function
	/**
	 * Creates a new PreviewCrossesDataTable.
	 *
	 * @constructor
	 * @alias module:fieldbook-datatable
	 * @param {string} tableIdentifier the id of the table container
	 * @param {string} ajaxUrl the URL from which to retrieve table data
	 */
	var dataTableConstructor = function ReviewDetailsOutOfBoundsDataTable(tableIdentifier, dataList) {
		'use strict';

		var columns = [],
			columnsDef = [],
			table;

		$(tableIdentifier + ' thead tr th').each(function() {
			columns.push({data: $(this).html()});
		});
		table = $(tableIdentifier).DataTable({
			data: dataList,
			columns: columns,
			scrollY: '400px',
			scrollX: '100%',
			scrollCollapse: true,
			columnDefs: columnsDef,
			lengthMenu: [[50, 75, 100, -1], [50, 75, 100, 'All']],
			bAutoWidth: true,
			iDisplayLength: 100,
			fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {

				// Assuming ID is in last column
				$(nRow).attr('id', aData.experimentId);
				$(nRow).data('row-index', this.fnGetPosition( nRow ));

				$('td', nRow).attr('nowrap','nowrap');
				$('td', nRow).attr('nowrap','nowrap');
				
				return nRow;
			},
			fnInitComplete: function(oSettings, json) {
				$(tableIdentifier + '_wrapper .dataTables_length select').select2({minimumResultsForSearch: 10});
				oSettings.oInstance.fnAdjustColumnSizing();
				oSettings.oInstance.api().colResize.init(oSettings.oInit.colResize);
			},
			language: {
				search: '<span class="mdt-filtering-label">Search:</span>'
			},
			dom: 'R<<"mdt-header"rli<"mdt-filtering">r><t>p>',
			// Problem with reordering plugin and fixed column for column re-ordering
			colReorder: {
				fixedColumns: 1
			}
		});
		
		$(tableIdentifier).dataTable().bind('sort', function() {
			$(tableIdentifier).dataTable().fnAdjustColumnSizing();
		});

	};

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
			  $(parentDiv + ' #selectAllAdvance').prop('checked', false);
			  $(parentDiv + ' #selectAllAdvance').change();
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

$(function(){
	$(document).contextmenu({
	    delegate: ".dataTable td[class*='invalid-value']",
	    menu: [
	      {title: "Accept Value", cmd: "markAccepted"},
	      {title: "Mark Missing", cmd: "markMissing"}
		],
		select: function(event, ui) {
			var colvindex = $(ui.target.parent()).data('row-index');
			var termId = $(ui.target).data('term-id');
			switch(ui.cmd){
				case "markAccepted":
					markCellAsAccepted(colvindex, termId, ui.target);
					break;
				case "markMissing":
					markCellAsMissing(colvindex, termId , 'missing' , 1 , ui.target);
					break;
			}
		},
		beforeOpen: function(event, ui) {
			var $menu = ui.menu,
				$target = ui.target,
				extraData = ui.extraData;
			ui.menu.zIndex(9999);
	    }
	  });
});

