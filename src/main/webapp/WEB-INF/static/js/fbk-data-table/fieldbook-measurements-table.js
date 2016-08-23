var getCurrentEnvironmentNumber = function () {
	return $("#fbk-measurements-controller-div").scope().selectedEnvironment ? $("#fbk-measurements-controller-div")
    	.scope().selectedEnvironment.id : 1;
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

		$(tableIdentifier + ' thead tr th').each(function() {
			columns.push({
				data: $(this).html(),
				defaultContent: ''
			});
			if ($(this).data('term-data-type-id') === 1110) {
				// Column definition for Numeric data type

				var minVal = ($(this).data('min-range'));
				var maxVal = ($(this).data('max-range'));
				var termId = $(this).data('term-id');
				var isVariates = $(this).hasClass('variates');
				columnsDef.push({
					defaultContent: '',
					targets: columns.length - 1,
					visible: termId === 8170 ? false : true, // do not display TRIAL_INSTANCE column, [0] column
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
			} else if ($(this).data('term-data-type-id') === 1120) {
				// Column definition for Character data type


				columnsDef.push({
					defaultContent: '',
					targets: columns.length - 1,
					render: function(data, type, full, meta) {
						return EscapeHTML.escape(data);
					}
				});
			} else if ($(this).data('term-data-type-id') === 1130) {
				// Column definition for Categorical data type

				if ($(this).data('term-valid-values') == null) {
					$(this).data('term-valid-values', '');
				}
				var possibleValues = $(this).data('term-valid-values').split('|');
				var termId = $(this).data('term-id');
				var isVariates = $(this).hasClass('variates');
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

							return (isVariates ? categoricalNameDom + categoricalDescDom : EscapeHTML.escape(data[1])) +
								'<input type="hidden" value="' + EscapeHTML.escape(data[2]) + '" />';
						}
					}
				});
			}

			if ($(this).data('term-id') == '8240') {
				// For GID
				columnsDef.push({
					defaultContent: '',
					targets: columns.length - 1,
					data: $(this).html(),
					width: '100px',
					render: function(data, type, full, meta) {
						return '<a class="gid-link" href="javascript: void(0)" ' +
							'onclick="openGermplasmDetailsPopopWithGidAndDesig(&quot;' +
							full.GID + '&quot;,&quot;' + full.DESIGNATION + '&quot;)">' + EscapeHTML.escape(data) + '</a>';
					}
				});
			} else if ($(this).data('term-id') == '8250') {
				// For designation
				columnsDef.push({
					defaultContent: '',
					targets: columns.length - 1,
					data: $(this).html(),
					render: function(data, type, full, meta) {
						return '<a class="desig-link" href="javascript: void(0)" ' +
							'onclick="openGermplasmDetailsPopopWithGidAndDesig(&quot;' +
							full.GID + '&quot;,&quot;' + full.DESIGNATION + '&quot;)">' + EscapeHTML.escape(data) + '</a>';
					}
				});
			}
		});

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
			processing: true,
			ajax: {
				url: '/Fieldbook/Common/addOrRemoveTraits/plotMeasurements/' + studyId + '/' + getCurrentEnvironmentNumber(),
				type: 'GET',
				cache: false,
				data: function (d) {
					var parameters = {
						draw: d.draw,
						pageSize: d.length,
						pageNumber: d.length === 0 ? 1 : d.start/d.length + 1
					};
                    return parameters;
                }
			},
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
				if (this.$('.invalid-value').length !== 0) {
					$('#review-out-of-bounds-data-list').show();
				} else {
					$('#review-out-of-bounds-data-list').hide();
				}
			},
			dom: '<"mdt-header"lir<"mdt-filtering dataTables_info"B>>tp',
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


		if (studyId != '') {
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
						processInlineEditInput();
					} else if ($colHeader.hasClass('variates') && $tdCell.data('is-inline-edit') !== '1') {
						processInlineEditInput();
						if ($('#measurement-table').data('show-inline-edit') === '1') {
							$.ajax({
								url: '/Fieldbook/Common/addOrRemoveTraits/update/experiment/cell/' + rowIndex + '/' + $colHeader.data('term-id'),
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
		$('#measurementsDiv .mdt-columns').detach().insertAfter('.mdt-filtering');
		$('.dataTables_length').prepend($('#mdt-environment-list').detach());
		$('.dataTables_length').before($('#mdt-environment-list-label').detach());
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
	};
	return dataTableConstructor;

})(jQuery);

function reloadMeasurementDataTable() {
	'use strict';
	var studyId = $('#studyId').val();
	$('#measurement-table').DataTable().ajax.url('/Fieldbook/Common/addOrRemoveTraits/plotMeasurements/' + studyId + '/' + getCurrentEnvironmentNumber()).load();
}
