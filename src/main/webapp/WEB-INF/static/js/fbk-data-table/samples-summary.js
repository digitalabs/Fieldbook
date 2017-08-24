BMS.Fieldbook.SamplesSummaryDataTable = (function ($) {
	var dataTableConstructor = function SamplesSummaryDataTable(tableIdentifier, plotId, plotNumber) {
		'use strict';

		var xAuthToken = JSON.parse(localStorage["bms.xAuthToken"]).token;

		$(tableIdentifier).DataTable({
			destroy: true,
			// scrollX: true, // TODO add scroll and auto adjust header
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
				{data: 'takenBy'},
				{data: 'createdDate'},
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

