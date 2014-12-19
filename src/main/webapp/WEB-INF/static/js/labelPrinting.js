var LabelPrinting = {
  allPresets : [],
  isTrial : '',
  excelOption : ''
};

(function() {
    'use strict';

    /**
     * This is called when LabelPrinting page is initialized
     */
    LabelPrinting.onPageLoad = function(isTrial,excelOption) {
        LabelPrinting.isTrial = isTrial;
        LabelPrinting.excelOption = excelOption;

        LabelPrinting.initializeUserPresets();
        LabelPrinting.showOrHideBarcodeFields();
        $('.loadSavedSettings').on('change', function(){
            if($(this).is(':checked')){
                $('.saved-settings').removeClass('fbk-hide');
                LabelPrinting.showDeleteSavedSettings();
            } else {
                $('.saved-settings').addClass('fbk-hide');
            }
        });
        $('.saved-settings').on('change', LabelPrinting.showDeleteSavedSettings);

        $('.fb-delete-settings').on('click', function(){
            var savedSettingsVal = $('#savedSettings').val();
            var settings = savedSettingsVal.split(':');
        });

        $('.includeTrial').on('click', function(){
            LabelPrinting.setTotalLabels($(this));
        });

        $('input[name='+getJquerySafeId('userLabelPrinting.barcodeNeeded')+']').on('change',function(){
            LabelPrinting.showOrHideBarcodeFields();
        });

        $('#export-label-data').on('click', function(){
            LabelPrinting.validateEnterLabelFieldsPage($('#label-format').val());
        });

        $('#label-format').on('change', function(){
            if($('#label-format').val() === '1'){
                //means pdf
                $('.pdf-fields').show();
                $('.non-pdf-fields').hide();
                $('.label-printing-details').show();
            }else if($('#label-format').val() !== ''){
                $('.pdf-fields').hide();
                $('.non-pdf-fields').show();
                $('.label-printing-details').show();
                if($('#label-format').val() === '2'){
                    $('.non-pdf-type').html('XLS');
                }else if($('#label-format').val() === '3'){
                    $('.non-pdf-type').html('CSV');
                }
            }else{
                $('.label-printing-details').hide();
            }
        });

        setSelectedTrialsAsDraggable();

        $( 'ul.droptrue' ).sortable({
            connectWith: 'ul',
            receive: function(event, ui) {
                // so if > 10
                if ($(this).hasClass('pdf-selected-fields') &&
                    $(this).children().length > 5) {
                    //ui.sender: will cancel the change.
                    //Useful in the 'receive' callback.
                    $(ui.sender).sortable('cancel');
                }
            }
        });

        if($('.includeTrial').length === 1) {
            $('.includeTrial').trigger('click');
        }
    };

    /**
     *  initialize program presets
     */
    LabelPrinting.initializeUserPresets = function(){
        $.ajax({
            url: '/Fieldbook/LabelPrinting/specifyLabelDetails/presets/list',
            type: 'GET',
            data: '',
            success: function(data){
                LabelPrinting.allPresets = data;

                $('#savedSettings').append(new Option('Please Choose', ''));
                var type =  null;
                for(var i = 0 ; i < data.length ; i++){
                    if(i !== 0 && type !== data[i].type){
                        type = data[i].type;
                        $('#savedSettings').append(new Option('-----------------------------------', ''));
                    }else if(i === 0){
                        type = data[i].type;
                    }
                    $('#savedSettings').append(new Option(data[i].name, data[i].type + ':' + data[i].id ));
                }

            }
        });
    };

    /**
     * Toggle for barcode fields
     */
    LabelPrinting.showOrHideBarcodeFields = function(){
        var barcodeNeeded = $('input[name='+getJquerySafeId('userLabelPrinting.barcodeNeeded')+']:checked').val();
        if(barcodeNeeded === '1'){
            $('.barcode-fields').show();
        }else{
            $('.barcode-fields').hide();
        }
    };

    /**
     *  Display delete saved settings button
     */
    LabelPrinting.showDeleteSavedSettings = function(){
        var savedSettingsVal = $('#savedSettings').val();
        if(savedSettingsVal === ''){
            $('.fb-delete-settings').addClass('fbk-hide');
        } else{
            var settings = savedSettingsVal.split(':');
            if(settings[0] === '1'){
                //meaning user preset, we show the delete
                $('.fb-delete-settings').removeClass('fbk-hide');
            }else{
                $('.fb-delete-settings').addClass('fbk-hide');
            }
        }
    };

    /**
     * Set the total labels value to UI
     * @param checkbox
     */
    LabelPrinting.setTotalLabels = function(checkbox) {
        //count total number of labels to be generated based on selected trial/nursery instance
        var labelCount = '';
        if (LabelPrinting.isTrial) {
            labelCount = $(checkbox).parents('tr').find('td.plot-count').html();
        } else {
            labelCount = $(checkbox).parents('tr').find('td.entry-count').html();
        }
        var totalCount = $('#totalLabelCount').text();

        if ($(checkbox).is(':checked')) {
            $('#totalLabelCount').text(parseInt(totalCount, 10) + parseInt(labelCount, 10));
        } else {
            $('#totalLabelCount').text(parseInt(totalCount, 10) - parseInt(labelCount, 10));
        }
    };

    /**
     *  Label Printing label fields validation
     */
    LabelPrinting.validateEnterLabelFieldsPage = function(type) {
        //we do the validation
        //we do the selected fields
        var leftSelectedFields = '';
        $('#leftSelectedFields li').each(function(){
            leftSelectedFields += $(this).attr('id');
            leftSelectedFields += ',';
        });

        if(leftSelectedFields !== ''){
            leftSelectedFields = leftSelectedFields.substring(0,leftSelectedFields.length-1);
        }

        var rightSelectedFields = '';
        $('#rightSelectedFields li').each(function(){
            rightSelectedFields += $(this).attr('id');
            rightSelectedFields += ',';
        });

        if(rightSelectedFields !== ''){
            rightSelectedFields = rightSelectedFields.substring(0,rightSelectedFields.length-1);
        }

        var mainSelectedFields = '';
        $('#mainSelectedFields li').each(function(){

            mainSelectedFields += $(this).attr('id');
            mainSelectedFields += ',';
        });

        if(mainSelectedFields !== ''){
            mainSelectedFields = mainSelectedFields.substring(0,mainSelectedFields.length-1);
        }

        if(leftSelectedFields === '' && rightSelectedFields === '' && parseInt(type, 10) === labelPrintingPDF){
            showInvalidInputMessage(selectedFieldsError);
            moveToTopScreen();
            return false;
        }else if(mainSelectedFields === '' && (parseInt(type, 10) === labelPrintingCsv || parseInt(type, 10) === labelPrintingExcel )){
            showInvalidInputMessage(selectedFieldsError);
            moveToTopScreen();
            return false;
        }

        $('#'+getJquerySafeId('userLabelPrinting.leftSelectedLabelFields')).val(leftSelectedFields);
        $('#'+getJquerySafeId('userLabelPrinting.rightSelectedLabelFields')).val(rightSelectedFields);
        $('#'+getJquerySafeId('userLabelPrinting.mainSelectedLabelFields')).val(mainSelectedFields);


        var barcodeNeeded = $('input[type="radio"]:checked').length;
        if (barcodeNeeded == 0) {
            showInvalidInputMessage(barcodeNeededError);
            moveToTopScreen();
            return false;
        }

        //we checked if something was checked
        if($('#'+getJquerySafeId('userLabelPrinting.barcodeNeeded1')).is(':checked')){
            //we need to check if either one is chosen in the drop downs
            if($('#'+getJquerySafeId('userLabelPrinting.firstBarcodeField')).val() == ''
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
            && LabelPrinting.hasFieldMapFieldsSelected()) {
            showAlertMessage('', generateLabelsWarningMessage);
        }

        $('#'+getJquerySafeId('userLabelPrinting.generateType')).val(type);
        LabelPrinting.setSelectedTrialInstanceOrder();

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

    };

    LabelPrinting.hasFieldMapFieldsSelected = function() {
        var hasFieldNameLabel = false;
        //check available and selected label lists
        hasFieldNameLabel = LabelPrinting.checkFieldMapLabelInSelectedFields(hasFieldNameLabel, '#leftSelectedFields li');
        hasFieldNameLabel = LabelPrinting.checkFieldMapLabelInSelectedFields(hasFieldNameLabel, '#rightSelectedFields li');
        hasFieldNameLabel = LabelPrinting.checkFieldMapLabelInBarcodeFields(hasFieldNameLabel, 'userLabelPrinting.firstBarcodeField');
        hasFieldNameLabel = LabelPrinting.checkFieldMapLabelInBarcodeFields(hasFieldNameLabel, 'userLabelPrinting.secondBarcodeField');
        hasFieldNameLabel = LabelPrinting.checkFieldMapLabelInBarcodeFields(hasFieldNameLabel, 'userLabelPrinting.thirdBarcodeField');
        return hasFieldNameLabel;
    };

    LabelPrinting.checkFieldMapLabelInSelectedFields = function(hasFieldNameLabel, listSelector) {
        //if not yet found, check label in the current list
        if (!hasFieldNameLabel) {
            $.each($(listSelector), function(index, label) {
                hasFieldNameLabel = LabelPrinting.isLabelInFieldMapLabels(hasFieldNameLabel, $(label).attr('id'));
                if (hasFieldNameLabel) {
                    return false;
                }
            });
        }
        return hasFieldNameLabel;
    };

    LabelPrinting.checkFieldMapLabelInBarcodeFields = function(hasFieldNameLabel, bardcodeId) {
        //if not yet found, check if selected barcode is a fieldmap label
        if (!hasFieldNameLabel) {
            hasFieldNameLabel = LabelPrinting.isLabelInFieldMapLabels(hasFieldNameLabel, $('#'+getJquerySafeId(bardcodeId)).val());
        }
        return hasFieldNameLabel;
    };

    LabelPrinting.isLabelInFieldMapLabels = function(hasFieldNameLabel, label) {
        $.each(fieldMapLabelFields.split(','), function(index, fieldMapLabel) {
            if (parseInt(label, 10) === parseInt(fieldMapLabel, 10)) {
                hasFieldNameLabel = true;
                return false;
            }
        });
        return hasFieldNameLabel;
    };

    LabelPrinting.setSelectedTrialInstanceOrder = function() {
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
    };

    /**
     * UNUSED:
     * show export modal
     */
    LabelPrinting.showExportModal = function() {
        var selectedData = {'id': labelPrintingExcel, 'text': LabelPrinting.excelOption};
        $('#export-type').select2('data', selectedData);
        $('#export-label-data-modal').modal('show');
    };

    /**
     * Retrieve LabelPrinting presets via service call
     * returns a promise obj
     */
    LabelPrinting.getLabelPrintingSettings = function(presetType,presetId) {
        var url = 'http://localhost:8080/Fieldbook/LabelPrinting/specifyLabelDetails/presets/' + presetType + '/' + presetId;
        return $.getJSON(url);
    };

    /**
     * Update LabelPrinting UI via jquery when a preset has been selected
     */
    LabelPrinting.onPresetSelect = function() {
        // retrieve via jquery the current presetId and presetType

        // FIX-ME: Placeholder values
        var presetType = 1;
        var presetId = 1;

        LabelPrinting.getLabelPrintingSettings(presetType,presetId).done(
            function(data) {
                // FIX-ME: UI manpulation: jquery here to update all UI setting fields
            }
        );
    };

})();