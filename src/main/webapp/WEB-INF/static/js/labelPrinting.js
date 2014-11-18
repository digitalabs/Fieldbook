function validateEnterLabelFieldsPage(type){
	//we do the validation
	//we do the selected fields
	'use strict';
	var leftSelectedFields = '';
	$('#leftSelectedFields li').each(function(){
		
		leftSelectedFields += $(this).attr('id');
		leftSelectedFields += ',';
	});
	
	if(leftSelectedFields != ''){
		leftSelectedFields = leftSelectedFields.substring(0,leftSelectedFields.length-1); 
	}
	
	var rightSelectedFields = '';
	$('#rightSelectedFields li').each(function(){
		
		rightSelectedFields += $(this).attr('id');
		rightSelectedFields += ',';
	});
	
	if(rightSelectedFields != ''){
		rightSelectedFields = rightSelectedFields.substring(0,rightSelectedFields.length-1); 
	}
	
	if(leftSelectedFields == '' && rightSelectedFields == ''){
		showInvalidInputMessage(selectedFieldsError);
		moveToTopScreen();
		return false;
	}
	
	$('#'+getJquerySafeId('userLabelPrinting.leftSelectedLabelFields')).val(leftSelectedFields);
	$('#'+getJquerySafeId('userLabelPrinting.rightSelectedLabelFields')).val(rightSelectedFields);
	
	
	var barcodeNeeded = $('input[type="radio"]:checked').length;
	if (barcodeNeeded == 0) {
		showInvalidInputMessage(barcodeNeededError);
		moveToTopScreen();
		return false;
	}
	
	//we checked if something was checked
	if($('#'+getJquerySafeId('userLabelPrinting.barcodeNeeded1')).is(':checked')){
		//we need to check if either one is chosen in the drop downs
		if($('#'+getJquerySafeId('userLabelPrinting.firstBarcodeField')).val() == ""
				&& $('#'+getJquerySafeId('userLabelPrinting.secondBarcodeField')).val() == ''
				&& $('#'+getJquerySafeId('userLabelPrinting.thirdBarcodeField')).val() == ''){
			showInvalidInputMessage(barcodeFieldNeededError);
			moveToTopScreen();
			return false;
		}
	}

	if ($('#selectedTrials .includeTrial:checked').length == 0 && $('#selectedTrials .includeTrial').length > 0) {
		showMessage(trialInstanceRequired);
		moveToTopScreen();
		return false;
	}
	
	if($('#'+getJquerySafeId('userLabelPrinting.filename')).val() == ''){
		//we need to check if either one is chosen in the drop downs
		
		showInvalidInputMessage(filenameError);
			moveToTopScreen();
			return false;
		
	}
	var data = $('#'+getJquerySafeId('userLabelPrinting.filename')).val();
     var isValid = /^[ A-Za-z0-9_@.\.&''@{}$!\-#()%.+~_=^\s]*$/i.test(data);
	    
	
	if (!isValid){
		showInvalidInputMessage(filenameErrorCharacter);
		moveToTopScreen();
		return false;
	}
	
	if ($('#'+getJquerySafeId('userLabelPrinting.fieldMapsExisting')).val().toString() === 'false' 
		&& hasFieldMapFieldsSelected()) {
		showAlertMessage('', generateLabelsWarningMessage);
	}
	
	if (type === labelPrintingExcel) {
		$('#'+getJquerySafeId('userLabelPrinting.generateType')).val($('#export-type').val());
	} else {
		$('#'+getJquerySafeId('userLabelPrinting.generateType')).val(type);
	}
	
	setSelectedTrialInstanceOrder();
	
	var $form = $('#specifyLabelDetailsForm'),
	serializedData = $form.serialize();
		$.ajax({
			url: $('#specifyLabelDetailsForm').attr('action'),
			type: 'POST',
			data: serializedData,
		    success: function(data){
		    	if(data.isSuccess === 1){
		    		$('#specifyLabelDetailsDownloadForm').submit();
		    	}else{		    		
		    		showErrorMessage('', data.message);
		    	}
			    
		   }
		});
	
}

function hasFieldMapFieldsSelected() {
	var hasFieldNameLabel = false;
	//check available and selected label lists
	hasFieldNameLabel = checkFieldMapLabelInSelectedFields(hasFieldNameLabel, '#leftSelectedFields li');
	hasFieldNameLabel = checkFieldMapLabelInSelectedFields(hasFieldNameLabel, '#rightSelectedFields li');
	hasFieldNameLabel = checkFieldMapLabelInBarcodeFields(hasFieldNameLabel, 'userLabelPrinting.firstBarcodeField');
	hasFieldNameLabel = checkFieldMapLabelInBarcodeFields(hasFieldNameLabel, 'userLabelPrinting.secondBarcodeField');
	hasFieldNameLabel = checkFieldMapLabelInBarcodeFields(hasFieldNameLabel, 'userLabelPrinting.thirdBarcodeField');
	return hasFieldNameLabel;
}

function checkFieldMapLabelInSelectedFields(hasFieldNameLabel, listSelector) {
	//if not yet found, check label in the current list
	if (!hasFieldNameLabel) {
		$.each($(listSelector), function(index, label) {
			hasFieldNameLabel = isLabelInFieldMapLabels(hasFieldNameLabel, $(label).attr('id'));
			if (hasFieldNameLabel) {
				return false;
			}
		});
	}
	return hasFieldNameLabel;
}

function checkFieldMapLabelInBarcodeFields(hasFieldNameLabel, bardcodeId) {
	//if not yet found, check if selected barcode is a fieldmap label
	if (!hasFieldNameLabel) {
		hasFieldNameLabel = isLabelInFieldMapLabels(hasFieldNameLabel, $('#'+getJquerySafeId(bardcodeId)).val());
	}
	return hasFieldNameLabel;
}

function isLabelInFieldMapLabels(hasFieldNameLabel, label) {
	$.each(fieldMapLabelFields.split(','), function(index, fieldMapLabel) {
		if (parseInt(label, 10) === parseInt(fieldMapLabel, 10)) {
			hasFieldNameLabel = true;
			return false;
		} 
	});
	return hasFieldNameLabel;
}

function setSelectedTrialInstanceOrder() {
	var order = [];
	var notIncluded = 0;
	//check if instance is selected and include in the list
	$('#selectedTrials .trialOrder').each(function(){
		var checked = false;
		$(this).parent().prev().find(':checked').each(function() {
				checked = true;
		});
		if (!checked) {
			notIncluded++;
		}
		if (checked) {
			var orderId = $(this).parent().parent().attr('id');
			orderId = parseInt(orderId) - notIncluded;
			order.push(orderId+'|'+$(this).val());
		}
	});
	$('#'+getJquerySafeId('userLabelPrinting.order')).val(order.join(','));
}

function showExportModal() {
	var selectedData = {'id': labelPrintingExcel, 'text': excelOption};
	$('#export-type').select2('data', selectedData);
	$('#export-label-data-modal').modal('show');
}