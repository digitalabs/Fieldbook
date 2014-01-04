function validateEnterLabelFieldsPage(type){
	//we do the validation
	//we do the selected fields
	var leftSelectedFields = "";
	$('#leftSelectedFields li').each(function(){
		
		leftSelectedFields += $(this).attr('id');
		leftSelectedFields += ",";
	});
	
	if(leftSelectedFields != ''){
		leftSelectedFields = leftSelectedFields.substring(0,leftSelectedFields.length-1); 
	}
	
	var rightSelectedFields = "";
	$('#rightSelectedFields li').each(function(){
		
		rightSelectedFields += $(this).attr('id');
		rightSelectedFields += ",";
	})
	
	if(rightSelectedFields != ''){
		rightSelectedFields = rightSelectedFields.substring(0,rightSelectedFields.length-1); 
	}
	
	if(leftSelectedFields == "" && rightSelectedFields == ""){
		showMessage(selectedFieldsError);
		moveToTopScreen();
		return false;
	}
	
	//console.log(selectedFields);
	$('#'+getJquerySafeId('userLabelPrinting.leftSelectedLabelFields')).val(leftSelectedFields);
	$('#'+getJquerySafeId('userLabelPrinting.rightSelectedLabelFields')).val(rightSelectedFields);
	
	
	var barcodeNeeded = $("input[type='radio']:checked").length;
	if (barcodeNeeded == 0) {
		showMessage(barcodeNeededError);
		moveToTopScreen();
		return false;
	}
	
	//we checked if something was checked
	if($("#"+getJquerySafeId('userLabelPrinting.barcodeNeeded1')).is(":checked")){
		//we need to check if either one is chosen in the drop downs
		if($('#'+getJquerySafeId('userLabelPrinting.firstBarcodeField')).val() == ""
				&& $('#'+getJquerySafeId('userLabelPrinting.secondBarcodeField')).val() == ""
				&& $('#'+getJquerySafeId('userLabelPrinting.thirdBarcodeField')).val() == ""){
			showMessage(barcodeFieldNeededError);
			moveToTopScreen();
			return false;
		}
	}

	if ($("#selectedTrials .includeTrial:checked").length == 0 && $("#selectedTrials .includeTrial").length > 0) {
		showMessage(trialInstanceRequired);
		moveToTopScreen();
		return false;
	}
	
	if($("#"+getJquerySafeId('userLabelPrinting.filename')).val() == ''){
		//we need to check if either one is chosen in the drop downs
		
			showMessage(barcodeFieldNeededError);
			moveToTopScreen();
			return false;
		
	}
	
	
	
	$("#"+getJquerySafeId('userLabelPrinting.generateType')).val(type);
	
	setSelectedTrialInstanceOrder();
	
Spinner.toggle();
var $form = $("#specifyLabelDetailsForm");
serializedData = $form.serialize();
		$.ajax({
			url: $('#specifyLabelDetailsForm').attr('action'),
			type: "post",
			data: serializedData,
		    success: function(data){
			    $('#specifyLabelDetailsDownloadForm').submit();
		   },
		   complete: function(){ 
			   Spinner.toggle();
		   } 
		});
	
}
function moveToTopScreen(){
	 $('html').scrollTop(0);
}
function setSelectedTrialInstanceOrder() {
	var order = [];
	var notIncluded = 0;
	$("#selectedTrials .trialOrder").each(function(){
		var checked = false;
		$(this).parent().prev().find(":checked").each(function() {
				checked = true;
		});
		if (!checked) {
			notIncluded++;
		}
		if (checked) {
			var orderId = $(this).parent().parent().attr("id");
			orderId = parseInt(orderId) - notIncluded;
			order.push(orderId+"|"+$(this).val());
		}
	});
	$("#"+getJquerySafeId("userLabelPrinting.order")).val(order.join(","));
}