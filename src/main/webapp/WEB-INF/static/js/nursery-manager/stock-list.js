var StockList = {};
 
(function() {
	'use strict';
	StockList.exportList = function(stockId) {		
		'use strict';		
		var formName = 'exportStockForm';
		$('#'+formName+' #exportStockListId').val(stockId);
		$('#'+formName).ajaxForm({dataType: 'text', success: StockList.showExportResponse}).submit();
	};
	
	StockList.showExportResponse  = function(responseText, statusText, xhr, $form) {
		'use strict';
		var resp = $.parseJSON(responseText);
		var formName = 'exportAdvanceStudyDownloadForm';
		$('#'+formName+' #outputFilename').val(resp.outputFilename);
		$('#'+formName+' #filename').val(resp.filename);
		$('#'+formName+' #contentType').val(resp.contentType);
		$('#'+formName).submit();
	};
})(); 

