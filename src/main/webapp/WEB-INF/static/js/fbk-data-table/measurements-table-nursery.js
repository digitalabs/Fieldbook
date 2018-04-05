/**
 * Dedicated JS file for all Nursery Manager measurement table related functionality.
 */

if (typeof (BMS) === 'undefined') {
	BMS = {};
}
if (typeof (BMS.Fieldbook) === 'undefined') {
	BMS.Fieldbook = {};
}

/**
* Creates a new MeasurementsDataTable.
*
* @constructor
* @alias module:fieldbook-datatable
* @param {string} tableIdentifier the id of the table container
* @param {string} ajaxUrl the URL from which to retrieve table data
*/
BMS.Fieldbook.MeasurementsDataTableNursery = (function($) {
	
	var dataTableConstructor = function MeasurementsDataTableNursery(tableIdentifier, dataList) {
		'use strict';

		var columns = [],
			columnsDef = [],
			table;


		// Get the list of columsn (MeasurementVariable) from the server
		$.ajax({
			url: '/Fieldbook/NurseryManager/editNursery/columns',
			type: 'GET'
		}).done(function(displayColumns) {

			var columnsObj = getColumns(tableIdentifier, displayColumns);
			columns = columnsObj.columns;
			columnsDef = columnsObj.columnsDef;

			table = $(tableIdentifier).DataTable({
				destroy: true,
				data: dataList,
				columns: columns,
				scrollY: '500px',
				scrollX: '100%',
				scrollCollapse: true,
				columnDefs: columnsDef,
				lengthMenu: [[50, 75, 100, -1], [50, 75, 100, 'All']],
				bAutoWidth: true,
				deferRender: true,
				iDisplayLength: 100,
				fnRowCallback: function(nRow, aData, iDisplayIndex, iDisplayIndexFull) {

					var toolTip = 'GID: ' + aData.GID + ' Designation: ' + aData.DESIGNATION;
					// Assuming ID is in last column
					$(nRow).attr('id', aData.experimentId);
					$(nRow).data('row-index', this.fnGetPosition(nRow));
					$(nRow).attr('title', toolTip);
					$('td', nRow).attr('nowrap', 'nowrap');

					$(nRow).find('.accepted-value, .invalid-value, .numeric-variable').each(function() {

						var termId = $(this).data('term-id');
						var cellData = $(this).text();
						if (termId != undefined) {
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
							}else if (possibleValues != undefined) {
								var values = possibleValues.split('|');

								$(this).removeClass('accepted-value');
								$(this).removeClass('invalid-value');

								if (cellData !== '' && cellData !== 'missing') {
									if ($.inArray(cellData, values) === -1 && $(this).find("input[type='hidden']").val() !== 'true') {
										if ($(this).data('is-accepted') === '1') {
											$(this).addClass('accepted-value');
										}else if ($(this).data('is-accepted') === '0') {
											$(this).removeClass('invalid-value').removeClass('accepted-value');
										} else {
											$(this).addClass('invalid-value');
										}
										$(this).data('term-id', $(this).data('term-id'));
									} else {
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
					hasOutOfBoundValuesAsync().then(function (hasOutOfBound) {
						if (hasOutOfBound) {
							$('#review-out-of-bounds-data-list').show();
						} else {
							$('#review-out-of-bounds-data-list').hide();
						}
					});
				},
				language: {
					search: '<span class="mdt-filtering-label">Search:</span>'
				},
				dom: 'R<"mdt-header"rli<"mdt-filtering">r>tp',
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

			if ($('#studyId').val() != '') {
				// Activate an inline edit on click of a table cell
				$(tableIdentifier).on('click', 'tbody td:not(:first-child)', function(e) {
					if (isAllowedEditMeasurementDataCell()) {
						var $tdCell = $(this);
						var cellTdIndex =  $(this).index();
						var rowIndex = $(this).parent('tr').data('row-index');

						var $colHeader = $('#measurementsDiv .dataTables_scrollHead table th:eq(' + cellTdIndex + ')');
						$(tableIdentifier).data('show-inline-edit', '1');
						if ($colHeader.hasClass('variates')) {
							$('body').data('last-td-time-clicked', new Date().getTime());
						}
						if ($colHeader.hasClass('factors')) {
							//we should now submit it
							processInlineEditInputNursery();
						} else if ($colHeader.hasClass('variates') && $tdCell.data('is-inline-edit') !== '1') {
							processInlineEditInputNursery();
							if ($('#measurement-table').data('show-inline-edit') === '1') {
								$.ajax({
									url: '/Fieldbook/nursery/measurements/inlineinput/single/' + rowIndex + '/' + $colHeader.data('term-id'),
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
			$('#measurementsDiv .mdt-columns').detach().insertBefore('#measurementsDiv .mdt-filtering');
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

			var cols = $(tableIdentifier).dataTable().fnSettings().aoColumns;
			var plotIndex;
			for (var i = 0; i<cols.length; i++) {
				if (cols[i].termId == '8201') {
					plotIndex = i;
					break;
				}
			};
			if (plotIndex) {
				$(tableIdentifier).DataTable().column(plotIndex).visible(false);
			}

		});

	};
	return dataTableConstructor;

})(jQuery);

function getColumns(tableIdentifier, displayColumns) {

	var columns = [],
		columnsDef = [];

	// Create column and columnDefs for 'ACTION'
	columns.push({
		data: 'Action',
		defaultContent: ''
	});

	columnsDef.push({
		defaultContent: '',
		targets: columns.length - 1,
		data: 'Action',
		width: '50px',
		render: function(data, type, full, meta) {
			return '<a href="javascript: editExperiment(&quot;' + tableIdentifier + '&quot;,' +
				EscapeHTML.escape(data) + ',' + meta.row + ')" class="fbk-edit-experiment"></a>';
		}
	});

	jQuery.each(displayColumns, function(i, displayColumn) {

		columns.push({
			data: displayColumn.name,
			defaultContent: ''
		});
		if (displayColumn.dataTypeId === 1110) {
			// Column definition for Numeric data type

			var minVal = displayColumn.minRange;
			var maxVal = displayColumn.maxRange;
			var termId = displayColumn.termId;
			var isVariates = !displayColumn.factor;

			columnsDef.push({
				defaultContent: '',
				targets: columns.length - 1,
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
				},
				render: function(data, type, full, meta) {
					var displayData = EscapeHTML.escape(data[0] != null ? data[0] : '');
					var hiddenData = EscapeHTML.escape(data[1]);

					if (data !== undefined) {
						return displayData + '<input type="hidden" value="' + hiddenData + '" />';
					}
				}
			});
		} else if (displayColumn.dataTypeId === 1120) {
			// Column definition for Character data type

			columnsDef.push({
				defaultContent: '',
				targets: columns.length - 1,
				render: function(data, type, full, meta) {
					return EscapeHTML.escape(data);
				}
			});
		} else if (displayColumn.dataTypeId == 1130) {
			// Column definition for Categorical data type

			if (!displayColumn.possibleValuesString) {
				displayColumn.possibleValuesString =  '';
			}

			var possibleValues = displayColumn.possibleValuesString.split('|');

			var termId = displayColumn.termId;
			var isVariates = !displayColumn.factor;
			columnsDef.push({
				defaultContent: '',
				targets: columns.length - 1,
				createdCell: function(td, cellData, rowData, row, col) {
					if (isVariates) {
						// cellData[0] : categorical name
						// cellData[1] : categorical display description

						// current measurementData has no value thus no need to check if out-of-bounds
						if (cellData[1] === '') {
							return;
						}

						// look for that description in the list of possible values
						var found = $.grep(possibleValues, function(value, i) {
							if (value === cellData[1]) {
								// this is the case where a=x format is retrieved directly from ontology DB

								return true;
							} else if (value !== '' && value.indexOf('=') === -1) {
								// this is the case where categorical ref values (possibleValues) retrieved is not in a=x format

								// since currentValue contains both name and description, we need to retrieve
								// only the description by splitting from the first occurrence of the separator
								var currentValue = cellData[1].substring(cellData[1].indexOf('=') + 1).trim();

								return value === currentValue;
							}

							return false;
						}).length;

						// if not found we may change its class as accepted (blue) or invalid (red)
						// depending on the data
						if (found <= 0) {
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
				},
				render: function(data, type, full, meta) {
					if (data !== undefined) {
						// Use knowledge from session.isCategoricalDisplayView to render correct data
						// data[0] = name, data[1] = description, data[2] = accepted value
						var showDescription = window.isCategoricalDescriptionView ? 'style="display:none"' : '';
						var showName = !window.isCategoricalDescriptionView ? 'style="display:none"' : '';

						var categoricalNameDom = '<span class="fbk-measurement-categorical-name" '+ showName  + '>' + EscapeHTML.escape(data[1]) + '</span>';
						var categoricalDescDom = '<span class="fbk-measurement-categorical-desc" '+ showDescription  + '>' + EscapeHTML.escape(data[0]) + '</span>';

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
		}

	});

	return {
		columns: columns,
		columnsDef: columnsDef
	};

}

function onMeasurementsInlineEditConfirmationEventNursery() {
	'use strict';
	return function(e) {
		if (parseInt($(this).data('inline-edit'), 10) === 1) {
			//keep the changes
			saveInlineEditNursery(0);
		} else if (parseInt($(this).data('inline-edit'), 10) === 0) {
			//discard the changes
			saveInlineEditNursery(1);
		}
		$('#inlineEditConfirmationModal').modal('hide');
	};
}

function onMeasurementsObservationLoadNursery(isCategoricalDisplay) {
	'use strict';
	var $categoricalDisplayToggleBtn = $('.fbk-toggle-categorical-display');

	window.isCategoricalDescriptionView = isCategoricalDisplay;

	// update the toggle button text depending on what current session value is
	$categoricalDisplayToggleBtn.text(isCategoricalDisplay ? window.measurementObservationMessages.hideCategoricalDescription :
		window.measurementObservationMessages.showCategoricalDescription);

	// add event handlers
	$('.inline-edit-confirmation').off('click').on('click', onMeasurementsInlineEditConfirmationEventNursery());
	$categoricalDisplayToggleBtn.off('click').on('click', function() {
		// process any unedited saves before updating measurement table's categorical view
		processInlineEditInputNursery();

		switchCategoricalView();
	});

	// display the measurements table
	return $.ajax({
		url: '/Fieldbook/nursery/measurements',
		type: 'GET',
		data: '',
		cache: false
	}).done(function(response) {
		new BMS.Fieldbook.MeasurementsDataTableNursery('#measurement-table', response);
	});

}

function initializeTraitsPaginationNursery(isCategoricalDisplay) {
    'use strict';
    onMeasurementsObservationLoadNursery();

    $('#inlineEditConfirmationModalClose').on('click', function (e) {
        // When the confirmation popup is clicked, stop the bubbling of event to parent elements so that body.click is not executed.
        e.stopPropagation();
        // then manually close the confirmation popup.
        $('#inlineEditConfirmationModal').modal('hide');
    });
}

function editExperiment(tableIdentifier, expId, rowIndex) {
	'use strict';
	var needToSaveFirst = $('body').data('needToSave') === '1' ? true : false;

	if (angular.element('#mainApp').injector().get('TrialManagerDataService').applicationData.unappliedChangesAvailable) {
		angular.element('#mainApp').injector().get('TrialManagerDataService').warnAboutUnappliedChanges();
		return;
	}
	// We show the ajax page here
	if (needToSaveFirst) {
		showAlertMessage('', $.fieldbookMessages.measurementsTraitsChangeWarning);
	} else {
		$.ajax({
			url: '/Fieldbook/nursery/measurements/inlineinput/multiple/' + rowIndex,
			type: 'GET',
			cache: false,
			success: function(dataResp) {
				$('.edit-experiment-section').html(dataResp);
				$('.updateExperimentModal').modal({ backdrop: 'static', keyboard: true });
			},
			error: function() {
				//TODO Localise the message
				showErrorMessage('Update experiment error', 'Could not update the experiment.');
			}
		});
	}
}

function saveInlineEditNursery(isDiscard) {
	'use strict';

	$.ajax({
		url: '/Fieldbook/nursery/measurements/inlineinput/single?isDiscard=' + isDiscard,
		type: 'POST',
		async: false,
		data:   $('#measurement-table').data('json-inline-edit-val'),
		contentType: 'application/json',
		success: function(data) {
			var jsonData = $.parseJSON($('#measurement-table').data('json-inline-edit-val'));
			if (isDiscard === 0 && jsonData.isNew === '1' && jsonData.value !== 'missing') {
				$('.inline-input').parent('td').addClass('accepted-value').removeClass('invalid-value');
				$('.inline-input').parent('td').data('is-accepted', '1');
			} else if (jsonData.isNew === '0') {
				$('.inline-input').parent('td').removeClass('accepted-value').removeClass('invalid-value');
				$('.inline-input').parent('td').data('is-accepted', '0');
			}
			if (data.success === '1') {
				$('.inline-input').parent('td').data('is-inline-edit', '0');

				var oTable = $('#measurement-table').dataTable();
				oTable.fnUpdate(data.data, data.index, null, false); // Row
				oTable.fnAdjustColumnSizing();
				$('body').off('click');
			} else {
				$('#measurement-table').data('show-inline-edit', '0');
				showErrorMessage('page-update-experiment-message-modal', data.errorMessage);
			}
		},
		error: function() {
			//TODO Localise the message
			showErrorMessage('', 'Could not update the measurement');
		}
	});
}

function processInlineEditInputNursery() {
	'use strict';
	if ($('.inline-input').length !== 0) {
		var indexElem = $('.data-hidden-value-index').val();
		var indexTermId = $('.data-hidden-value-term-id').val();
		var indexDataVal = '';
		var isNew = '0';
		if ($('.data-value').hasClass('variates-select')) {
			if ($('.data-value').select2('data')) {
				indexDataVal = $('.data-value').select2('data').id;
				isNew  = $('.data-value').select2('data').status;
			} else {
				indexDataVal = '';
			}
		} else if ($('.data-value').hasClass('numeric-value')) {
			var minVal = ($('.data-value').data('min-range'));
			var maxVal = ($('.data-value').data('max-range'));
			var cellText = $('.data-value').val();
			if ($.trim(cellText.toLowerCase()) == 'missing') {
				if (minVal && maxVal) {
					isNew = '1';
				} else {
					isNew = '0';
				}
				$('.data-value').val('missing');
			} else if (minVal != null && maxVal != null && (parseFloat(minVal) > parseFloat(cellText) ||
				parseFloat(cellText) > parseFloat(maxVal))) {
				isNew = '1';
			}
			indexDataVal =  $('.data-value').val();
		} else {
			indexDataVal =  $('.data-value').val();
		}

		var currentInlineEdit = {
			index: indexElem,
			termId: indexTermId,
			value: indexDataVal,
			isNew: isNew
		};
		$('#measurement-table').data('json-inline-edit-val', JSON.stringify(currentInlineEdit));
		if (isNew === '1') {
			$('#inlineEditConfirmationModal').modal({
				backdrop: 'static',
				keyboard: true
			});
			$('#measurement-table').data('show-inline-edit', '0');
			return false;
		} else {
			saveInlineEditNursery(0);
		}
	}
	return true;
}

function markCellAsMissing(indexElem, indexTermId, indexDataVal, isNew, elem) {
	'use strict';
	var data = {
		index:indexElem,
		termId:indexTermId,
		value:indexDataVal,
		isNew: isNew
	};

	$.ajax({
		headers: {
			Accept: 'application/json',
			'Content-Type': 'application/json'
		},
		url: '/Fieldbook/nursery/measurements/inlineinput/single?isDiscard=0',
		type: 'POST',
		async: false,
		data:   JSON.stringify(data),
		contentType: 'application/json',
		success: function(data) {
			if (data.success === '1') {
				var oTable = $('#measurement-table').dataTable();
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
function markCellAsAccepted(indexElem, indexTermId, elem) {
	'use strict';

	var data = {
		index: indexElem,
		termId: indexTermId
	};

	$.ajax({
		headers: {
			Accept: 'application/json',
			'Content-Type': 'application/json'
		},
		url: '/Fieldbook/nursery/measurements/inlineinput/accepted',
		type: 'POST',
		async: false,
		data:   JSON.stringify(data),
		contentType: 'application/json',
		success: function(data) {
			if (data.success === '1') {
				var oTable = $('#measurement-table').dataTable();
				oTable.fnUpdate(data.data, data.index, null, false); // Row
				$(elem).removeClass('invalid-value');
				$(elem).addClass('accepted-value');
			} else {
				showErrorMessage('page-update-experiment-message-modal', data.errorMessage);
				$('#measurement-table').data('show-inline-edit', '0');
			}
		}
	});
}
function markAllCellAsAccepted() {
	'use strict';

	$.ajax({
		url: '/Fieldbook/nursery/measurements/inlineinput/accepted/all',
		type: 'GET',
		async: false,
		contentType: 'application/json',
		success: function(data) {
			if (data.success === '1') {
				reloadMeasurementTable();
				$('#reviewOutOfBoundsDataModal').modal('hide');
			} else {
				showErrorMessage('page-review-out-of-bounds-data-message-modal', data.errorMessage);
			}
		}
	});
}
function markAllCellAsMissing() {
	'use strict';

	$.ajax({
		url: '/Fieldbook/nursery/measurements/inlineinput/missing/all',
		type: 'GET',
		async: false,
		contentType: 'application/json',
		success: function(data) {
			if (data.success === '1') {
				reloadMeasurementTable();
				$('#reviewOutOfBoundsDataModal').modal('hide');
			} else {
				showErrorMessage('page-review-out-of-bounds-data-message-modal', data.errorMessage);
			}
		}
	});
}

function reloadMeasurementTable() {
	'use strict';
	if ($('#measurement-table').length !== 0) {
		$.ajax({
			url: '/Fieldbook/ImportManager/import/preview/nursery',
			type: 'POST',
			success: function(html) {
				$('#measurementsDiv').html(html);
				$('.import-study-data').data('data-import', '1');
			}
		});
	}
}
