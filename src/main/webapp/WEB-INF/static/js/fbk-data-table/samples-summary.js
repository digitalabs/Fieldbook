BMS.Fieldbook.SamplesSummaryDataTable = (function ($) {
	var dataTableConstructor = function SamplesSummaryDataTable(tableIdentifier, plotId, plotNumber) {
		'use strict';

		var xAuthToken = JSON.parse(localStorage["bms.xAuthToken"]).token;

		$(tableIdentifier).DataTable({
			destroy: true,
			ajax: {
				url: '/bmsapi/sample/' + cropName + '/samples?plotId=' + plotId,
				dataSrc: '',
				beforeSend: function (xhr) {
					xhr.setRequestHeader('X-Auth-Token', xAuthToken);
				}
			},
			columns: [
				{data: 'sampleName'},
				{data: 'sampleBusinessKey'},
				{data: 'takenBy',
					render: function (data) {
						if (!data) {
							return '-';
						}
						return data;
					}},
				{data: 'samplingDate',
					render: function (data, type, row) {
						if (!data) {
							return '-';
						}
						if (type === 'sort') {
							return data.split("/").reverse().join();
						}
						return data;
					}
				},
				{data: 'sampleList'},
				{data: 'plantNumber'},
				{data: 'plantBusinessKey'}
			],
			// dom: "t" // display only the table, without pagination
		});

		$(tableIdentifier).closest('.modal').find('.modal-title span').text(plotNumber);
	};
	return dataTableConstructor;
})(jQuery);

