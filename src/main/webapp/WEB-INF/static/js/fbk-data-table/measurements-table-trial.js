/*globals isCategoricalDisplay */
var getCurrentEnvironmentNumber = function() {
	var selEnv = $('#fbk-measurements-controller-div').scope().selectedEnvironment;

	if (!selEnv) {
		var envList;
		$.ajax({
			url: '/Fieldbook/trial/measurements/instanceMetadata/' + $('#studyId').val(),
			async: false,
			success: function(data) {
				envList = data;
			}
		});
		return envList[0].instanceDbId;
	} else {
		return selEnv.instanceDbId;
	}
};

var measurementsTableRowCallback = function(nRow, aData, iDisplayIndex, iDisplayIndexFull, tableIdentifier, _this) {
	var toolTip = 'GID: ' + aData.GID + ' Designation: ' + aData.DESIGNATION;
	// Assuming ID is in last column
	$(nRow).attr('id', aData.experimentId);
	$(nRow).data('row-index', _this.fnGetPosition(nRow));
	$(nRow).attr('title', toolTip);
	$('td', nRow).attr('nowrap', 'nowrap');

	$(nRow).find('.accepted-value, .invalid-value, .numeric-variable').each(function() {
		var termId = $(this).data('term-id');
		var cellData = $(this).text();
		//FIXME dataTypeID is always undefined
		if (termId !== undefined) {
			var possibleValues = $(tableIdentifier + " thead tr th[data-term-id='" + termId + "']").data('term-valid-values');
			var dataTypeId = $(tableIdentifier + " thead tr th[data-term-id='" + termId + "']").data('term-data-type-id');

			if (dataTypeId == '1110') {
				var minVal = ($(tableIdentifier + " thead tr th[data-term-id='" + termId + "']").data('min-range'));
				var maxVal = ($(tableIdentifier + " thead tr th[data-term-id='" + termId + "']").data('max-range'));
				var isVariates =  $(tableIdentifier + " thead tr th[data-term-id='" + termId + "']").hasClass('variates');

				if (isVariates) {
					$(this).removeClass('accepted-value');
					$(this).removeClass('invalid-value');
					if (minVal != null && maxVal != null && (parseFloat(minVal) > parseFloat(cellData) || parseFloat(cellData) > parseFloat(maxVal))) {
						if (cellData !== 'missing') {
							if ($(this).find("input[type='hidden']").val() === 'true') {
								$(this).addClass('accepted-value');
							} else {
								$(this).addClass('invalid-value');
							}
						}
					}
				}
			} else if (possibleValues !== undefined) {
				var values = possibleValues.split('|');

				$(this).removeClass('accepted-value');
				$(this).removeClass('invalid-value');

				if (cellData !== '' && cellData !== 'missing') {
					if ($.inArray(cellData, values) === -1 && $(this).find("input[type='hidden']").val() !== 'true') {
						if ($(this).data('is-accepted') === '1') {
							$(this).addClass('accepted-value');
						} else if ($(this).data('is-accepted') === '0') {
							$(this).removeClass('invalid-value').removeClass('accepted-value');
						} else {
							$(this).addClass('invalid-value');
						}
						$(this).data('term-id', $(this).data('term-id'));
					}
				}
			}
		}
	});
	$(nRow).find('.variates').each(function () {
		var colIndex = $(this).index() + 1;
		var varName = $(tableIdentifier + " thead tr th:nth-child(" + colIndex + ")").text();
		if (varName !== undefined) {
			var dataArray = aData[varName];
			if (dataArray !== undefined) {
				var status = dataArray[dataArray.length - 1];
				var cellData = $(this).text();
				var phenotypeId = $(this).data('phenotypeId');
				$(this).removeClass('manually-edited-value');
				$(this).removeClass('out-of-sync-value');
				$(this).removeAttr('title');
				if (!cellData && !phenotypeId) {
					return;
				}
				if (status == 'MANUALLY_EDITED') {
					$(this).attr('title', toolTip + ' manually-edited-value');
					$(this).addClass('manually-edited-value');
				} else if (status == 'OUT_OF_SYNC') {
					$(this).attr('title', toolTip + ' out-of-sync-value');
					$(this).addClass('out-of-sync-value');
				}
			}
		}
	});
	return nRow;
};

//Sortable columns: GID(8240), DESIGNATION(8250), ENTRY_NO(8230), ENTRY_TYPE(8255), ENTRY_CODE(8300), REP_NO(8210), PLOT_NO(8200), BLOCK_NO(8220), ROW(8581), COL(8582)
var sortableColumnIDs = [8240, 8250, 8230, 8255, 8300, 8210, 8200, 8220, 8581, 8582];

var getColumns = function(displayColumns, displayTrialInstance) {
	var columns = [],
		columnsDef = [];

	jQuery.each(displayColumns, function(i, displayColumn) {
		columns.push({
			title: displayColumn.name,
			data: displayColumn.termId < 0 ? displayColumn.termId : displayColumn.name, // FIXME BMS-4397 handle collisions between real and virtual variables when they have the same name. I.e: special column SAMPLES
			termId: displayColumn.termId,
			defaultContent: '',
			orderable: displayColumn.variableType === "TRAIT" ? true : $.inArray(displayColumn.termId, sortableColumnIDs) > -1,
			className: displayColumn.factor === true ? 'factors' : 'variates',
			isDerivedTrait: displayColumn.formula != null,
			possibleValues: displayColumn.possibleValues,
			dataTypeCode: displayColumn.dataTypeCode
		});

		var termId = displayColumn.termId;
		var isVariates = displayColumn.factor !== true;

		if (displayColumn.dataTypeId === 1110) {
			// Column definition for Numeric data type
			var minVal = displayColumn.minRange;
			var maxVal = displayColumn.maxRange;

			columnsDef.push({
				defaultContent: '',
				targets: columns.length - 1,
				visible: termId === 8170 && !displayTrialInstance ? false : true, // do not display TRIAL_INSTANCE column, [0] column
				createdCell: function(td, cellData, rowData, row, col) {
					if (isVariates) {
						$(td).addClass('numeric-variable');
						var cellText = $(td).text();
						if (minVal != null && maxVal != null && (parseFloat(minVal) > parseFloat(cellText) || parseFloat(cellText) > parseFloat(maxVal))) {
							$(td).removeClass('accepted-value');
							$(td).removeClass('invalid-value');
							if ($(td).text() !== 'missing') {
								if ($(td).find("input[type='hidden']").val() === 'true') {
									$(td).addClass('accepted-value');
								} else {
									$(td).addClass('invalid-value');
								}
							}
						}
					}
					$(td).data('term-id', termId);
					$(td).data('phenotype-id', cellData[2]);
				},
				render: function(data, type, full, meta) {
					if (data !== undefined) {
						var displayData = getDisplayValueForNumericalValue(data[0]);
						var hiddenData = EscapeHTML.escape(data[1]);
						return displayData + '<input type="hidden" value="' + hiddenData + '" />';
					}
				}
			});
		} else if (displayColumn.dataTypeId === 1120 || displayColumn.dataTypeId === 1117) {
			var isObsUnitId = displayColumn.termId === 8201;
			// Column definition for Character and date data type
			columnsDef.push({
				defaultContent: '',
				targets: columns.length - 1,
				visible: !isObsUnitId,
				createdCell: function(td, cellData, rowData, row, col) {
					if (cellData[cellData.length-1] == 'MANUALLY_EDITED') {
						$(td).addClass('manually-edited-value');
					} if (cellData[cellData.length-1] == 'OUT_OF_SYNC') {
						$(td).addClass('out-of-sync-value');
					}
					$(td).data('term-id', termId);
					$(td).data('phenotype-id', cellData[1]);
				},
				render: function(data, type, full, meta) {
					if (data) {
						return EscapeHTML.escape(data[0]);
					}
				}
			});
		} else if (displayColumn.dataTypeId === 1130) {
			// Column definition for Categorical data type

			if (!displayColumn.possibleValuesString) {
				displayColumn.possibleValuesString =  '';
			}

			var possibleValues = displayColumn.possibleValuesString.split('|');
			columnsDef.push({
				defaultContent: '',
				targets: columns.length - 1,
				createdCell: function(td, cellData, rowData, row, col) {
					// cellData[0] : categorical name
					// cellData[1] : categorical display description

					if (cellData != "" || cellData[3] != "") {
						$(td).data('phenotype-id', cellData[3]);
					}

					// current measurementData has no value thus no need to check if out-of-bounds
					if (cellData === "" || cellData[1] === "") {
						return;
					}

					// look for that description in the list of possible values
					var found = $.grep(possibleValues, function(value, i) {
						if (value === cellData[1]) {
							// this is the case where a=x format is retrieved directly from ontology DB
							return true;
						} else if (value !== '' && value.indexOf('=') === -1 && cellData[1]) {
							// this is the case where categorical ref values (possibleValues) retrieved is not in a=x format
							// since currentValue contains both name and description, we need to retrieve
							// only the description by splitting from the first occurrence of the separator
							var currentValue = cellData[1].substring(cellData[1].indexOf('=') + 1).trim()
							return value === currentValue;
						}
						return false;
					}).length;
					// if not found we may change its class as accepted (blue) or invalid (red)
					// depending on the data
					if (found <= 0) {
						$(td).removeClass('accepted-value');
						$(td).removeClass('invalid-value');

						var categoricalNameText = $(td).find('.fbk-measurement-categorical-name').text();

						if (categoricalNameText !== 'missing') {
							if ($(td).find("input[type='hidden']").val() === 'true') {
								$(td).addClass('accepted-value');
							} else {
								$(td).addClass('invalid-value');
							}
						}
					}

					$(td).data('term-id', termId);
					$(td).data('term-valid-values', displayColumn.possibleValuesString);
				},
				render: function(data, type, full, meta) {
					if (data !== undefined) {
						// Use knowledge from session.isCategoricalDisplayView to render correct data
						// data[0] = name, data[1] = description, data[2] = accepted value
						var showDescription = window.isCategoricalDescriptionView ? 'style="display:none"' : '';
						var showName = !window.isCategoricalDescriptionView ? 'style="display:none"' : '';
						var categoricalNameDom = '<span class="fbk-measurement-categorical-name" ' + showName  + '>' + EscapeHTML.escape(data[1]) + '</span>';
						var categoricalDescDom = '<span class="fbk-measurement-categorical-desc" ' + showDescription  + '>' + EscapeHTML.escape(data[0]) + '</span>';

						return categoricalNameDom + categoricalDescDom +
							'<input type="hidden" value="' + EscapeHTML.escape(data[2]) + '" />';
					}
				}
			});
		}

		if (displayColumn.termId === 8240) {
			// For GID
			columnsDef.push({
				defaultContent: '',
				targets: columns.length - 1,
				data: displayColumn.name,
				width: '100px',
				render: function(data, type, full, meta) {
					return '<a class="gid-link" href="javascript: void(0)" ' +
						'onclick="openGermplasmDetailsPopopWithGidAndDesig(&quot;' +
						full.GID + '&quot;,&quot;' + full.DESIGNATION + '&quot;)">' + EscapeHTML.escape(data) + '</a>';
				}
			});
		} else if (displayColumn.termId === 8250) {
			// For designation
			columnsDef.push({
				defaultContent: '',
				targets: columns.length - 1,
				data: displayColumn.name,
				render: function(data, type, full, meta) {
					return '<a class="desig-link" href="javascript: void(0)" ' +
						'onclick="openGermplasmDetailsPopopWithGidAndDesig(&quot;' +
						full.GID + '&quot;,&quot;' + full.DESIGNATION + '&quot;)">' + EscapeHTML.escape(data) + '</a>';
				}
			});
		} else if (displayColumn.termId === -2) {
			// For samples
			columnsDef.push({
				defaultContent: '',
				targets: columns.length - 1,
				data: displayColumn.termId,
				render: function(data, type, full, meta) {
					if (data && full) {
						if (data[0] == "-") {
							return data[0];
						}
						return '<a class="desig-link" href="javascript: void(0)" ' +
							'onclick="openSampleSummary(\'' + full.OBS_UNIT_ID[0] + '\',\'' +  + full.PLOT_NO[0] + '\''
							+ ')">' + data[0] + '</a>';
					}
				}
			});
		}
	});
	return {
		columns: columns,
		columnsDef: columnsDef
	};
};

BMS.Fieldbook.MeasurementsDataTable = (function($) {

	/**
 	* Creates a new MeasurementsDataTable.
 	*
 	* @constructor
 	* @alias module:fieldbook-datatable
 	* @param {string} tableIdentifier the id of the table container
 	* @param {string} ajaxUrl the URL from which to retrieve table data
 	*/
	var dataTableConstructor = function MeasurementsDataTable(tableIdentifier) {
		'use strict';

		var columns = [],
		columnsDef = [],
		table;

		var studyId = $('#studyId').val(),
		environmentId = getCurrentEnvironmentNumber();

		// recreate a table if exists
		if ($(tableIdentifier).html() && !!$(tableIdentifier).html().trim()) {
			$(tableIdentifier).dataTable().fnDestroy();
			$(tableIdentifier).empty();
		}

		var trialManagerDataService = angular.element('#mainApp').injector().get('TrialManagerDataService');

		$.ajax({
			url: '/Fieldbook/TrialManager/openTrial/columns',
			type: 'POST',
			data: 'variableList=' + trialManagerDataService.settings.measurements.m_keys.concat(trialManagerDataService.settings.selectionVariables.m_keys).join()
		}).done(function(displayColumns) {

			var columnsObj = getColumns(displayColumns, false);
			columns = columnsObj.columns;
			columnsDef = columnsObj.columnsDef;
			// column index is usually 6 but not always. 
			// Depends on how many germplasm factors are there. So we determine actual one from the column list.
			var plotNoIndex = 6; 
			for (var i = 0; i < displayColumns.length; i++) {
				if (displayColumns[i].name === "PLOT_NO") {
					plotNoIndex = i;
					break;
				}
			}

			table = $(tableIdentifier).DataTable({
				destroy: true,
				columns: columns,
				scrollY: '500px',
				scrollX: '100%',
				scrollCollapse: true,
				columnDefs: columnsDef,
				lengthMenu: [[50, 75, 100], [50, 75, 100]],
				bAutoWidth: true,
				iDisplayLength: 100,
				serverSide: true,
				aaSorting: [[plotNoIndex, 'asc']], 
				processing: true,
				deferRender: true,
				ajax: {
					url: '/Fieldbook/trial/measurements/plotMeasurements/' + studyId + '/' + environmentId,
					type: 'GET',
					cache: false,
					data: function(d) {						
						var sortedColIndex = $(tableIdentifier).dataTable().fnSettings().aaSorting[0][0];
						var sortDirection = $(tableIdentifier).dataTable().fnSettings().aaSorting[0][1];
						var sortedColTermId = displayColumns[sortedColIndex].termId;
						
						return {
							draw: d.draw,
							pageSize: d.length,
							pageNumber: d.length === 0 ? 1 : d.start / d.length + 1,
							sortBy : sortedColTermId,
							sortOrder : sortDirection
						};
					}
				},
				headerCallback: function(thead, data, start, end, display) {
					table.columns().iterator('column', function(settings, column) {
						if (settings.aoColumns[ column ].termId !== undefined) {
							$(table.column(column).header()).attr('data-term-id', settings.aoColumns[ column ].termId);
						}
						$(table.column(column).header()).addClass(settings.aoColumns[ column ].factor === true ? 'factors' : 'variates');
						if (settings.aoColumns[ column ].isDerivedTrait == true) {
							$(table.column(column).header()).addClass('derived-trait-column-header');
						}
					});
				},
				fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
					measurementsTableRowCallback(nRow, aData, iDisplayIndex, iDisplayIndexFull, tableIdentifier, this);
				},
				fnInitComplete: function(oSettings, json) {
					$(tableIdentifier + '_wrapper .mdt-length .dataTables_length select').select2({minimumResultsForSearch: 10});
					oSettings.oInstance.fnAdjustColumnSizing();
					oSettings.oInstance.api().colResize.init(oSettings.oInit.colResize);
					hasOutOfBoundValuesAsync().then(function (hasOutOfBound) {
						if (hasOutOfBound) {
							$('#review-out-of-bounds-data-list').show();
						} else {
							$('#review-out-of-bounds-data-list').hide();
						}
					});
				},
				dom: '<"mdt-header"<"mdt-length dataTables_info"l>ir<"mdt-filtering dataTables_info"B>>tp',
				//TODO localise messages
				language: {
					processing: '<span class="throbber throbber-2x"></span>',
					lengthMenu: 'Records per page: _MENU_',
					paginate: {
						next: '>',
						previous: '<',
						first: '<<',
						last: '>>'
					}
				},
				paginationType: 'full_numbers',
				// For column visibility
				buttons: [
				{
					extend: 'colvis',
					columns: ':not(:first-child)',
					className: 'fbk-buttons-no-border fbk-colvis-button',
					text:'<i class="glyphicon glyphicon-th dropdown-toggle fbk-show-hide-grid-column"></i>'
				}
				]
			});

			try {
				new $.fn.dataTable.ColReorder(table);
			} catch (e) {
				console.log(e)
			}

			if (studyId !== '') {
				// Activate an inline edit on click of a table cell

				$(tableIdentifier).off().on('click', 'tbody td:not(:first-child)', function(e) {
					if (isAllowedEditMeasurementDataCell()) {
						var $tdCell = $(this);
						var cellTdIndex =  $(this).index();
						var rowIndex = $(this).parent('tr').data('row-index');
						var $colHeader = $('#measurementsDiv .dataTables_scrollHead table th:eq(' + cellTdIndex + ')');

						$(tableIdentifier).data('show-inline-edit', '1');

						var experimentId = $(this).parent('tr').attr('id');
						var phenotypeId = $(this).data('phenotype-id') !== undefined ? $(this).data('phenotype-id') : "";

						if ($colHeader.hasClass('variates')) {
							$('body').data('last-td-time-clicked', new Date().getTime());
						}
						if ($colHeader.hasClass('factors')) {
							//we should now submit it
							processInlineEditInput();
						} else if ($colHeader.hasClass('variates') && $tdCell.data('is-inline-edit') !== '1') {
							processInlineEditInput();
							if ($('#measurement-table').data('show-inline-edit') === '1') {
								$.ajax({
									url: '/Fieldbook/trial/measurements/edit/experiment/cell/' + experimentId + '/' + $colHeader.data('term-id') + '?phenotypeId=' + phenotypeId,
									type: 'GET',
									success: function(data) {
										$tdCell.html(data);
										$tdCell.data('is-inline-edit', '1');
									},
									error: function() {
										//TODO localise the message
										showErrorMessage('Server Error', 'Could not update the measurement');
									}
								});
							}
						}
					}
				});
			}

			if ($('.mdt-measurements-table-panel .fbk-toggle-categorical-display').length < 1) {
				$('#mdt-toggle-categorical-display-hidden-original').clone(true, true).removeClass('fbk-hide')
					.insertAfter('.mdt-measurements-table-panel .mdt-filtering');
			}
			$('[name="measurement-table_length"]').addClass('inline-select mdt-table-length-selector');
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
				$(cols).each(function(index) {
					var prevIndex = $(tableIdentifier).dataTable().fnSettings().aoColumns[index]._ColReorder_iOrigCol;
					if (colIndex == prevIndex) {
						column = table.column(index);
						// Toggle the visibility
						column.visible(!column.visible());
					}
				});
			});
		});

		/*$(tableIdentifier).dataTable().bind('sort', function() {
		$(tableIdentifier).dataTable().fnAdjustColumnSizing();
		});*/
	};
	return dataTableConstructor;
})(jQuery);

/**
 *   A copy of Measurement table for now with necessary changes for now. Modifications in progress
 */

BMS.Fieldbook.PreviewMeasurementsDataTable = (function($) {
	/**
	 * Creates a new PreviewMeasurementsDataTable, it doesn't have the inline editting of the values in the table. Preview ONLY.
	 *
	 * @constructor
	 * @alias module:fieldbook-datatable
	 * @param {string} tableIdentifier the id of the table container
	 * @param {string} ajaxUrl the URL from which to retrieve table data
	 */
	var dataTableConstructor = function PreviewMeasurementsDataTable(tableIdentifier, columnsOrder) {
		'use strict';

		var columns = [],
		columnsDef = [],
		table;

		// recreate a table if exists
		if ($(tableIdentifier).html() && !!$(tableIdentifier).html().trim()) {
			$(tableIdentifier).dataTable().fnDestroy();
			$(tableIdentifier).empty();
		}

		var trialManagerDataService = angular.element('#mainApp').injector().get('TrialManagerDataService');

		$.ajax({
			url: '/Fieldbook/TrialManager/createTrial/columns',
			type: 'POST',
			data: 'variableList=' + trialManagerDataService.settings.measurements.m_keys.concat(trialManagerDataService.settings.selectionVariables.m_keys).join() +
                '&columnOrders=' + columnsOrder
		}).done(function(displayColumns) {

			var columnsObj = getColumns(displayColumns, true);
			columns = columnsObj.columns;
			columnsDef = columnsObj.columnsDef;

			table = $(tableIdentifier).DataTable({
				destroy: true,
				columns: columns,
				scrollY: '500px',
				scrollX: '100%',
				scrollCollapse: true,
				columnDefs: columnsDef,
				lengthMenu: [[50, 75, 100], [50, 75, 100]],
				bAutoWidth: true,
				iDisplayLength: 100,
				serverSide: false, // the preview table is entirely client-side, until saving
				processing: true,
				deferRender: true,
				sAjaxDataProp: '',
				ajax: {
					url: '/Fieldbook/trial/measurements/plotMeasurements/preview',
					type: 'GET',
					cache: false
				},
				headerCallback: function(thead, data, start, end, display) {
					setTimeout( function (){
						table.columns().iterator('column', function(settings, column) {
							if (settings.aoColumns[ column ].isDerivedTrait == true) {
								$(table.column(column).header()).addClass('derived-trait-column-header');
							}
						});
					});
				},
				fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
					measurementsTableRowCallback(nRow, aData, iDisplayIndex, iDisplayIndexFull, tableIdentifier, this);
				},
				fnInitComplete: function(oSettings, json) {
					$(tableIdentifier + '_wrapper .mdt-length .dataTables_length select').select2({minimumResultsForSearch: 10});
					oSettings.oInstance.fnAdjustColumnSizing();
					oSettings.oInstance.api().colResize.init(oSettings.oInit.colResize);
					hasOutOfBoundValuesAsync().then(function (hasOutOfBound) {
						if (hasOutOfBound) {
							$('#review-out-of-bounds-data-list').show();
						} else {
							$('#review-out-of-bounds-data-list').hide();
						}
					});
				},
				dom: '<"mdt-header"<"mdt-length dataTables_info"l>ir<"mdt-filtering dataTables_info"B>>tp',
				//TODO localise messages
				language: {
					processing: '<span class="throbber throbber-2x"></span>',
					lengthMenu: 'Records per page: _MENU_'
				},
				// For column visibility
				buttons: [
				{
					extend: 'colvis',
					columns: ':not(:first-child)',
					className: 'fbk-buttons-no-border fbk-colvis-button',
					text:'<i class="glyphicon glyphicon-th dropdown-toggle fbk-show-hide-grid-column"></i>'
				}
				]
			});

			$(tableIdentifier).dataTable().bind('sort', function() {
				$(tableIdentifier).dataTable().fnAdjustColumnSizing();
			});

			$('[name="preview-measurement-table_length"]').addClass('inline-select mdt-table-length-selector');

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
				$(cols).each(function(index) {
					var prevIndex = $(tableIdentifier).dataTable().fnSettings().aoColumns[index]._ColReorder_iOrigCol;
					if (colIndex == prevIndex) {
						column = table.column(index);
						// Toggle the visibility
						column.visible(!column.visible());
					}
				});
			});
		});
	};
	return dataTableConstructor;
})(jQuery);

BMS.Fieldbook.ImportPreviewMeasurementsDataTable = (function($) {
	/**
	 * Creates a new ImportPreviewMeasurementsDataTable, it doesn't have the inline editting of the values in the table. Preview ONLY.
	 *
	 * @constructor
	 * @alias module:fieldbook-datatable
	 * @param {string} tableIdentifier the id of the table container
	 * @param {string} ajaxUrl the URL from which to retrieve table data
	 */

	var dataTableConstructor = function ImportPreviewMeasurementsDataTable(tableIdentifier, columnsOrder) {
		'use strict';

		var columns = [],
		columnsDef = [],
		table;

		var studyId = $('#studyId').val(),
		environmentId = getCurrentEnvironmentNumber();

		// recreate a table if exists
		if ($(tableIdentifier).html() && !!$(tableIdentifier).html().trim()) {
			$(tableIdentifier).dataTable().fnDestroy();
			$(tableIdentifier).empty();
		}

		var trialManagerDataService = angular.element('#mainApp').injector().get('TrialManagerDataService');

		$.ajax({
			url: '/Fieldbook/TrialManager/createTrial/columns',
			type: 'POST',
			data: 'variableList=' + trialManagerDataService.settings.measurements.m_keys.concat(trialManagerDataService.settings.selectionVariables.m_keys).join() +
			'&columnOrders=' + columnsOrder
		}).done(function(displayColumns) {

			var columnsObj = getColumns(displayColumns, true);
			columns = columnsObj.columns;
			columnsDef = columnsObj.columnsDef;

			table = $(tableIdentifier).DataTable({
				destroy: true,
				columns: columns,
				scrollY: '500px',
				scrollX: '100%',
				scrollCollapse: true,
				columnDefs: columnsDef,
				lengthMenu: [[50, 75, 100], [50, 75, 100]],
				bAutoWidth: true,
				iDisplayLength: 100,
				serverSide: false, // the preview table is entirely client-side, until saving
				processing: true,
				deferRender: true,
				sAjaxDataProp: '',
				ajax: {
					url: '/Fieldbook/ImportManager/import/preview',
					type: 'POST',
					cache: false
				},
				headerCallback: function(thead, data, start, end, display) {
					setTimeout( function (){
						table.columns().iterator('column', function(settings, column) {
							if (settings.aoColumns[ column ].isDerivedTrait == true) {
								$(table.column(column).header()).addClass('derived-trait-column-header');
							}
						});
					});
				},
				fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
					measurementsTableRowCallback(nRow, aData, iDisplayIndex, iDisplayIndexFull, tableIdentifier, this);
				},
				fnInitComplete: function(oSettings, json) {
					$(tableIdentifier + '_wrapper .mdt-length .dataTables_length select').select2({minimumResultsForSearch: 10});
					oSettings.oInstance.fnAdjustColumnSizing();
					oSettings.oInstance.api().colResize.init(oSettings.oInit.colResize);
					hasOutOfBoundValuesAsync().then(function (hasOutOfBound) {
						if (hasOutOfBound) {
							$('#review-out-of-bounds-data-list').show();
						} else {
							$('#review-out-of-bounds-data-list').hide();
						}
					});
				},
				dom: '<"mdt-header"<"mdt-length dataTables_info"l>ir<"mdt-filtering dataTables_info"B>>tp',
				//TODO localise messages
				language: {
					processing: '<span class="throbber throbber-2x"></span>',
					lengthMenu: 'Records per page: _MENU_'
				},
				// For column visibility
				buttons: [
				{
					extend: 'colvis',
					columns: ':not(:first-child)',
					className: 'fbk-buttons-no-border fbk-colvis-button',
					text:'<i class="glyphicon glyphicon-th dropdown-toggle fbk-show-hide-grid-column"></i>'
				}
				]
			});

			if ($('#studyId').val() !== '') {
				// Activate an inline edit on click of a table cell
				$(tableIdentifier).on('click', 'tbody td:not(:first-child)', function(e) {
					if (isAllowedEditMeasurementDataCell()) {
						var $tdCell = $(this);
						var cellTdIndex =  $(this).index();
						var rowIndex = $(this).parent('tr').data('row-index');

						var $colHeader = $('.import-preview-measurements-table table th:eq(' + cellTdIndex + ')');
						$(tableIdentifier).data('show-inline-edit', '1');
						if ($colHeader.hasClass('variates')) {
							$('body').data('last-td-time-clicked', new Date().getTime());
						}
						if ($colHeader.hasClass('factors')) {
							//we should now submit it
							processInlineEditInput();
						} else if ($colHeader.hasClass('variates') && $tdCell.data('is-inline-edit') !== '1') {
							processInlineEditInput();
							if ($(tableIdentifier).data('show-inline-edit') === '1') {
								$.ajax({
									url: '/Fieldbook/trial/measurements/update/experiment/cell/' + rowIndex + '/' + $tdCell.data('term-id'),
									type: 'GET',
									success: function(data) {
										$tdCell.html(data);
										$tdCell.data('is-inline-edit', '1');
									},
									error: function() {
										//TODO localise the message
										showErrorMessage('Server Error', 'Could not update the measurement');
									}
								});
							}
						}
					}
				});
			}

			$(tableIdentifier).dataTable().bind('sort', function() {
				$(tableIdentifier).dataTable().fnAdjustColumnSizing();
			});

			$('[name="import-preview-measurement-table_length"]').addClass('inline-select mdt-table-length-selector');
			if ($('.import-preview-measurements-table .fbk-toggle-categorical-display').length < 1) {
				$('#mdt-toggle-categorical-display-hidden-original').clone(true, true).removeClass('fbk-hide')
						.insertAfter('.import-preview-measurements-table .mdt-filtering');
			}

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
				$(cols).each(function(index) {
					var prevIndex = $(tableIdentifier).dataTable().fnSettings().aoColumns[index]._ColReorder_iOrigCol;
					if (colIndex == prevIndex) {
						column = table.column(index);
						// Toggle the visibility
						column.visible(!column.visible());
					}
				});
			});
		});
	};
	return dataTableConstructor;
})(jQuery);

function getDisplayValueForNumericalValue(numericValue) {
	if(numericValue === "missing" || numericValue === "") {
		return numericValue;
	} else {
        return EscapeHTML.escape( numericValue ? Number(Math.round(numericValue+'e4')+'e-4'): '');
	}
}

function markCellAsAccepted(indexElem, indexTermId, elem) {
	'use strict';

	var data = {
		index: indexElem,
		termId: indexTermId
	};

	var tableIdentifier = $('body').hasClass('import-preview-measurements') ? '#import-preview-measurement-table' : '#measurement-table';

	$.ajax({
		headers: {
			Accept: 'application/json',
			'Content-Type': 'application/json'
		},
		url: '/Fieldbook/trial/measurements/update/experiment/cell/accepted',
		type: 'POST',
		async: false,
		data:   JSON.stringify(data),
		contentType: 'application/json',
		success: function(data) {
			if (data.success === '1') {
				var oTable = $(tableIdentifier).dataTable();
				oTable.fnUpdate(data.data, data.index, null, false); // Row
				$(elem).removeClass('invalid-value');
				$(elem).addClass('accepted-value');
			} else {
				showErrorMessage('page-update-experiment-message-modal', data.errorMessage);
				$(tableIdentifier).data('show-inline-edit', '0');
			}
		}
	});
}

function markCellAsMissing(indexElem, indexTermId, indexDataVal, isNew, elem) {
	'use strict';
	var data = {
		index:indexElem,
		termId:indexTermId,
		value:indexDataVal,
		isNew: isNew
	};

	var isImportPreviewMeasurementsView = $('body').hasClass('import-preview-measurements');
	var tableIdentifier = isImportPreviewMeasurementsView ? '#import-preview-measurement-table' : '#measurement-table';

	$.ajax({
		headers: {
			Accept: 'application/json',
			'Content-Type': 'application/json'
		},
		url: '/Fieldbook/trial/measurements/' +
		(isImportPreviewMeasurementsView ? 'updateByIndex' : 'update') + '/experiment/cell/data?isDiscard=0',
		type: 'POST',
		async: false,
		data:   JSON.stringify(data),
		contentType: 'application/json',
		success: function(data) {
			if (data.success === '1') {
				var oTable = $(tableIdentifier).dataTable();
				oTable.fnUpdate(data.data, data.index, null, false); // Row
				$(elem).removeClass('invalid-value');
			} else {
				showErrorMessage('page-update-experiment-message-modal', data.errorMessage);
			}
		},
		error: function() {
			//TODO Localise the message
			showErrorMessage('Server error', 'Could not update the measurement');
		}
	});
}

function markAllCellAsAccepted() {
	'use strict';

	$.ajax({
		url: '/Fieldbook/trial/measurements/update/experiment/cell/accepted/all',
		type: 'GET',
		async: false,
		contentType: 'application/json',
		success: function(data) {
			if (data.success === '1') {
				reloadImportPreviewMeasurementsDataTable();
				$('#reviewOutOfBoundsDataModal').modal('hide');
			} else {
				showErrorMessage('page-review-out-of-bounds-data-message-modal', data.errorMessage);
			}
		}
	});

}

function markAllCellAsMissing() {
	'use strict';

	var tableIdentifier = $('body').hasClass('import-preview-measurements') ? '#import-preview-measurement-table' :
    		'#measurement-table';
    var oTable = $(tableIdentifier).dataTable();

	$.ajax({
		url: '/Fieldbook/trial/measurements/update/experiment/cell/missing/all',
		type: 'GET',
		async: false,
		contentType: 'application/json',
		success: function(data) {
			if (data.success === '1') {
				reloadImportPreviewMeasurementsDataTable();
				$('#reviewOutOfBoundsDataModal').modal('hide');
			} else {
				showErrorMessage('page-review-out-of-bounds-data-message-modal', data.errorMessage);
			}
		}
	});
}

function reloadImportPreviewMeasurementsDataTable() {
	var columnsOrder = BMS.Fieldbook.MeasurementsTable.getColumnOrdering('measurement-table');
	new BMS.Fieldbook.ImportPreviewMeasurementsDataTable('#import-preview-measurement-table', JSON.stringify(columnsOrder));
}

function onMeasurementsObservationLoad(isCategoricalDisplay) {
	'use strict';
	if ($('#measurement-table') && $('#measurement-table').length !== 0) {
		var $categoricalDisplayToggleBtn = $('.fbk-toggle-categorical-display');

		// update the toggle button text depending on what current session value is
		$categoricalDisplayToggleBtn.text(isCategoricalDisplay ? window.measurementObservationMessages.hideCategoricalDescription :
			window.measurementObservationMessages.showCategoricalDescription);
	
		// add event handlers
		$('.inline-edit-confirmation').off('click').on('click', onMeasurementsInlineEditConfirmationEvent());
		$categoricalDisplayToggleBtn.off('click').on('click', function() {
			// process any unedited saves before updating measurement table's categorical view
			processInlineEditInput();
	
			switchCategoricalView();
		});
	
		// display the measurements table
		return new BMS.Fieldbook.MeasurementsDataTable('#measurement-table');
	}
}

function initializeTraitsPagination(isCategoricalDisplay) {
    'use strict';
    onMeasurementsObservationLoad(isCategoricalDisplay);

    $('#inlineEditConfirmationModalClose').on('click', function (e) {
        // When the confirmation popup is clicked, stop the bubbling of event to parent elements so that body.click is not executed.
        e.stopPropagation();
        // then manually close the confirmation popup.
        $('#inlineEditConfirmationModal').modal('hide');
    });
}
