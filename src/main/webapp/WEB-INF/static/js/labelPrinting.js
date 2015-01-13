var LabelPrinting;

LabelPrinting = {
    allPresets: [],
    isTrial: '',
    excelOption: '',
    availableFieldIds : [],
    availableFieldMap : {},
    labelFormat: {
        'PDF': 1,
        'EXCEL': 2,
        'CSV': 3
    },

    pdfLabelSheet : {
        'A4' : 1,
        'Letter' : 2
    },

    TYPES: {
        STANDARD : 0,
        PROGRAM : 1
    }
};

(function() {
    'use strict';

    /**
     * This is called when LabelPrinting page is initialized
     */
    LabelPrinting.onPageLoad = function(isTrial,excelOption,availableFields) {
        LabelPrinting.isTrial = isTrial;
        LabelPrinting.excelOption = excelOption;

        // pluck only the ids
        for (var i = 0; i < availableFields.length; i++) {
            LabelPrinting.availableFieldIds.push(availableFields[i].id);
            LabelPrinting.availableFieldMap[availableFields[i].id] = availableFields[i].name;
        }

        addToUIFieldsList($('#non-pdf-available-fields'),LabelPrinting.availableFieldMap,LabelPrinting.availableFieldIds);
        addToUIFieldsList($('#pdf-available-fields'),LabelPrinting.availableFieldMap,LabelPrinting.availableFieldIds);

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

        $('.saved-settings').on('change', LabelPrinting.doSelectPreset);

        $('.fb-delete-settings').on('click', function(){
            var selectedPreset = LabelPrinting.getSelectedPreset();

            if (selectedPreset.length > 1 && LabelPrinting.TYPES.PROGRAM.toString() === selectedPreset[0]) {
                var presetNameInput = $safeId('input[name=userLabelPrinting.settingsName]').val();

                var deleteModalElm = $('#fbk-lbl-printing-delete-preset-confirm');
                var deleteModalDialogTxtElm = deleteModalElm.find('.modal-dialog-text');

                // replace {0} with the proper name
                deleteModalDialogTxtElm.text(labelPrintingDeletePresetText.replace('{0}',presetNameInput));

                deleteModalElm.modal('show');
            }

        });

        $('#fbk-lbl-printing-delete-preset-confirm .yes').on('click',LabelPrinting.onDeletePresetOk);

        $('#fbk-lbl-printing-delete-preset-confirm .no').on('click',function() {
            $('#fbk-lbl-printing-delete-preset-confirm').modal('hide');
        });

        $('.includeTrial').on('click', function(){
            LabelPrinting.setTotalLabels($(this));
        });

        $safeId('input[name=userLabelPrinting.barcodeNeeded]').on('change',function(){
            LabelPrinting.showOrHideBarcodeFields();
        });

        $('#export-label-data').on('click', function(){
            LabelPrinting.doExportLabel($('#label-format').val());
        });

        $('#fbk-lbl-printing-save-preset').on('click',function() {
            LabelPrinting.onSavePreset();
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

        $('#fbk-lbl-printing-save-preset-override-modal .yes').on('click',function() {
            LabelPrinting.doSavePreset($('#label-format').val()).done(function() {
                // close modal
                $('#fbk-lbl-printing-save-preset-override-modal').modal('hide');
                moveToTopScreen();
            });
        });

        $('#fbk-lbl-printing-save-preset-override-modal .no').on('click',function() {
            // close modal
            $('#fbk-lbl-printing-save-preset-override-modal').modal('hide');
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
     * action on saving presets
     */
    LabelPrinting.onSavePreset = function() {
        var presetNameInput = $safeId('input[name=userLabelPrinting.settingsName]').val();

        // 1. validate preset settings
        if (!presetNameInput || 0 === presetNameInput.length) {
            showErrorMessage('',labelPrintingPresetNameIsEmpty);
            return false;
        }

        if ( !LabelPrinting.validateEnterLabelFieldsPage($('#label-format').val()) ) {
            return false;
        }

        // 2. call service to check if preset name already exists
        LabelPrinting.searchLabelPrintingPresetByName(presetNameInput).done(function(data) {
            // we have existing data view an overwrite modal
            if (data.length > 0 && LabelPrinting.TYPES.PROGRAM === data[0].type) {
                var presetOverwriteModalElm = $('#fbk-lbl-printing-save-preset-override-modal');
                var presetOverwriteModalTxt = presetOverwriteModalElm.find('.modal-dialog-text');
                var updatedModalText = labelPrintingConfirmationPresetText.replace('{0}',$safeId('input[name=userLabelPrinting.settingsName]').val());

                presetOverwriteModalTxt.text(updatedModalText);

                $('#fbk-lbl-printing-save-preset-override-modal').modal('show');
            } else if (data.length > 0 && LabelPrinting.TYPES.STANDARD === data[0].type) {
                showErrorMessage('',labelPrintingCannotSaveStandardPreset);
            } else {
                // no existing preset, we add a new one
                LabelPrinting.doSavePreset($('#label-format').val()).done(function(data) {
                    LabelPrinting.initializeUserPresets().done(function(data) {
                        var settingsListElm = $('#savedSettings');
                        // select the newly added preset
                        doUISafeSelect(settingsListElm,settingsListElm.children('option:last-child').val());

                        moveToTopScreen();

                    });

                });
            }
        });
    };

    /**
     * Perform delete preset
     */
    LabelPrinting.onDeletePresetOk = function() {
        var deleteModalElm = $('#fbk-lbl-printing-delete-preset-confirm');
        var selectedPresetId = LabelPrinting.getSelectedPreset()[1];

        // delete the label printing preset
        LabelPrinting.deleteLabelPrintingPreset(selectedPresetId).done(function(data) {

            if (true === data) {
                // refresh the preset drop-down
                LabelPrinting.initializeUserPresets().done(function(data) {

                    doUISafeSelect($('#savedSettings'),'');
                    deleteModalElm.modal('hide');

                    showSuccessfulMessage('', labelPrintingDeletePresetSuccess);
                });

            }
        });


    };

    /**
     * Saves the preset, also returns the promise so we can chain
     */
    LabelPrinting.doSavePreset = function(type) {
        // update form for necessary details
        LabelPrinting.updateAdditionalLabelSettingsFormDetails(type);

        // prepare the from that we will be saving, should be similar with label export
        return LabelPrinting.saveLabelPrintingSetting($('#specifyLabelDetailsForm').serialize()).done(function(data) {
            if (!data) {
                showErrorMessage('',ajaxGenericErrorMsg);
                return;
            }

            showSuccessfulMessage('', labelPrintingSavePresetSuccess);

        });
    };

    /**
     *  initialize program presets
     */
    LabelPrinting.initializeUserPresets = function(){
        return $.ajax({
            url: '/Fieldbook/LabelPrinting/specifyLabelDetails/presets/list',
            type: 'GET',
            data: '',
            cache: false,
            success: function(data){
                LabelPrinting.allPresets = data;

                $('#savedSettings').empty().append(new Option('Please Choose', ''));
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
        var barcodeNeeded = $safeId('input[name=userLabelPrinting.barcodeNeeded]:checked').val();
        if(barcodeNeeded === '1'){
            $('.barcode-fields').show();
        }else{
            $('.barcode-fields').hide();
        }
    };

    /**
     * returns the currently selected values already parsed into preset type and id (still string)
     * @returns {Array}
     */
    LabelPrinting.getSelectedPreset = function() {
        var savedSettingsVal = $('#savedSettings').val();
        return ('' === savedSettingsVal) ? [] : savedSettingsVal.split(':');
    };

    /**
     *  Display delete saved settings button
     */
    LabelPrinting.showDeleteSavedSettings = function(){
        var savedSettingsVal = LabelPrinting.getSelectedPreset();
        if (0 === savedSettingsVal.length) {
            $('.fb-delete-settings').addClass('fbk-hide');
        } else if ('1' === savedSettingsVal[0]) {
            //meaning user preset, we show the delete
            $('.fb-delete-settings').removeClass('fbk-hide');
        } else {
            $('.fb-delete-settings').addClass('fbk-hide');
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

        $safeId('#userLabelPrinting.leftSelectedLabelFields').val(leftSelectedFields);
        $safeId('#userLabelPrinting.rightSelectedLabelFields').val(rightSelectedFields);
        $safeId('#userLabelPrinting.mainSelectedLabelFields').val(mainSelectedFields);


        var barcodeNeeded = $('input[type="radio"]:checked').length;
        if (barcodeNeeded == 0) {
            showInvalidInputMessage(barcodeNeededError);
            moveToTopScreen();
            return false;
        }

        //we checked if something was checked
        if($safeId('#userLabelPrinting.barcodeNeeded1').is(':checked')){
            //we need to check if either one is chosen in the drop downs
            if($safeId('#userLabelPrinting.firstBarcodeField').val() == ''
                && $safeId('#userLabelPrinting.secondBarcodeField').val() == ''
                && $safeId('#userLabelPrinting.thirdBarcodeField').val() == ''){
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

        if($safeId('#userLabelPrinting.filename').val() == ''){
            //we need to check if either one is chosen in the drop downs

            showInvalidInputMessage(filenameError);
            moveToTopScreen();
            return false;

        }
        var data = $safeId('#userLabelPrinting.filename').val();
        var isValid = /^[ A-Za-z0-9_@.\.&''@{}$!\-#()%.+~_=^\s]*$/i.test(data);


        if (!isValid){
            showInvalidInputMessage(filenameErrorCharacter);
            moveToTopScreen();
            return false;
        }

        if ($safeId('#userLabelPrinting.fieldMapsExisting').val().toString() === 'false'
            && LabelPrinting.hasFieldMapFieldsSelected()) {
            showAlertMessage('', generateLabelsWarningMessage);
        }

        return true;
    } ;

    LabelPrinting.doExportLabel = function(type) {
        // 1. validate
        if (!LabelPrinting.validateEnterLabelFieldsPage(type)) {
            return false;
        }

        // 2. update #specifyLabelDetailsForm for other hidden details
        LabelPrinting.updateAdditionalLabelSettingsFormDetails(type);

        // perform export
        var formElm = $('#specifyLabelDetailsForm');
        LabelPrinting.exportLabel(formElm.attr('action'),formElm.serialize()).done(function(data) {
            if(data.isSuccess === 1){
                $('#specifyLabelDetailsDownloadForm').submit();
            }else{
                showErrorMessage('', data.message);
            }
        });
    };

    /**
     * Update update #specifyLabelDetailsForm hidden fields for additional details details
     * @param type
     */
    LabelPrinting.updateAdditionalLabelSettingsFormDetails = function(type) {
        $safeId('#userLabelPrinting.generateType').val(type);
        LabelPrinting.setSelectedTrialInstanceOrder();
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
            hasFieldNameLabel = LabelPrinting.isLabelInFieldMapLabels(hasFieldNameLabel, $safeId('#' + bardcodeId).val());
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
        $safeId('#userLabelPrinting.order').val(order.join(','));
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
     * Search for all presets (standard and program) given presetName
     * Returns list of presets as array
     * @param presetName
     *
     */
    LabelPrinting.searchLabelPrintingPresetByName = function(presetName) {
        var url = '/Fieldbook/LabelPrinting/specifyLabelDetails/presets/searchLabelPrintingPresetByName';
        return $.getJSON(url,{'name' : presetName});

    };

    /**
     * Deletes a label printing program preset
     * @param presetId
     * @returns {*}
     */
    LabelPrinting.deleteLabelPrintingPreset = function(presetId) {
        var url = '/Fieldbook/LabelPrinting/specifyLabelDetails/presets/delete';
        return $.getJSON(url,{'programPresetId':presetId});

    };

    /**
     * Perform a save preset ajax operation
     * @param formSerializedData
     * @returns {*}
     */
    LabelPrinting.saveLabelPrintingSetting = function(formSerializedData) {
        var url = '/Fieldbook/LabelPrinting/specifyLabelDetails/presets/save';

        return $.post(url, formSerializedData,'json');
    };

    /**
     * Do Export label printing service call
     * @param formUrl
     * @param formSerialData
     * @returns {*} jquery promise
     */
    LabelPrinting.exportLabel = function(formUrl,formSerialData) {
        return $.ajax({url: formUrl, type: 'POST', data: formSerialData});
    };

    /**
     * Retrieve LabelPrinting presets via service call
     * returns a promise obj
     */
    LabelPrinting.getLabelPrintingSettings = function(presetType,presetId) {
        var url = '/Fieldbook/LabelPrinting/specifyLabelDetails/presets/' + presetType + '/' + presetId;
        return $.getJSON(url);
    };

    /**
     * Update LabelPrinting UI via jquery when a preset has been selected
     */
    LabelPrinting.doSelectPreset = function() {
        // show delete btn if applicable
        LabelPrinting.showDeleteSavedSettings();

        // retrieve via jquery the current presetId and presetType
        var selectedPreset = LabelPrinting.getSelectedPreset();

        if (selectedPreset.length === 0) {
            $('#label-format').val('').change();
            return;
        }

        LabelPrinting.getLabelPrintingSettings(selectedPreset[0],selectedPreset[1]).done(
            function(data) {
                /** @namespace data.name */
                /** @namespace data.outputType */
                /** @namespace data.csvExcelSetting */
                /** @namespace data.pdfSetting */
                /** @namespace data.barcodeSetting */

                // set the label output
                $('#label-format').val(LabelPrinting.labelFormat[data.outputType]).change();

                if (data.outputType === 'PDF') {
                    LabelPrinting.updatePDFFields(data.pdfSetting);
                } else {
                    LabelPrinting.updateCSVExcelFields(data.csvExcelSetting);
                }

                // set the barcode options
                LabelPrinting.updateBarcodeOptions(data.barcodeSetting);

                // set the setting name
                $safeId('input[name=userLabelPrinting.settingsName]').val(data.name);


            }
        );
    };

    /**
     * Updates PDF form fields
     * @param pdfSetting
     */
    LabelPrinting.updatePDFFields = function(pdfSetting) {
        /** @namespace pdfSetting.sizeOfLabelSheet */
        /** @namespace pdfSetting.numberOfRowsPerPage */
        /** @namespace pdfSetting.selectedLeftFieldsList */
        /** @namespace pdfSetting.selectedRightFieldsList */

        $safeId('#userLabelPrinting.sizeOfLabelSheet').val(LabelPrinting.pdfLabelSheet[pdfSetting.sizeOfLabelSheet]).change();
        $safeId('#userLabelPrinting.numberOfRowsPerPageOfLabel').val(pdfSetting.numberOfRowsPerPage).change();

        var diff = $(LabelPrinting.availableFieldIds).not(pdfSetting.selectedLeftFieldsList).get();
        diff = $(diff).not(pdfSetting.selectedRightFieldsList).get();

        //add diff to the pdf available fields list
        addToUIFieldsList($('#pdf-available-fields'),LabelPrinting.availableFieldMap,diff);
        addToUIFieldsList($('#leftSelectedFields'),LabelPrinting.availableFieldMap,pdfSetting.selectedLeftFieldsList);
        addToUIFieldsList($('#rightSelectedFields'),LabelPrinting.availableFieldMap,pdfSetting.selectedRightFieldsList);

    };

    /**
     * Updates CSV/Excel form fields
     * @param pdfSetting
     */
    LabelPrinting.updateCSVExcelFields = function(setting) {
        /** @namespace setting.includeColumnHeadingsInOutput */
        /** @namespace setting.selectedFieldsList */

        // toggle the column heading radio btn
        var selectedValue = (setting.includeColumnHeadingsInOutput) ? '1' : '0';
        $('input[name="userLabelPrinting.includeColumnHeadinginNonPdf"][value="' + selectedValue + '"]').prop('checked', true).change();

        var diff = $(LabelPrinting.availableFieldIds).not(setting.selectedFieldsList).get();

        addToUIFieldsList($('#non-pdf-available-fields'),LabelPrinting.availableFieldMap,diff);
        addToUIFieldsList($('#mainSelectedFields'),LabelPrinting.availableFieldMap,setting.selectedFieldsList);

    };

    /**
     * Update Barcode form fields
     * @param barcodeSetting
     */
    LabelPrinting.updateBarcodeOptions = function(barcodeSetting) {
        /** @namespace barcodeSetting.barcodeFieldsList */
        /** @namespace barcodeSetting.barcodeFormat */
        /** @namespace barcodeSetting.barcodeNeeded */

        //set the radio btns
        var selectedValue = (barcodeSetting.barcodeNeeded) ? '1' : '0';
        $('input[name="userLabelPrinting.barcodeNeeded"][value="' + selectedValue + '"]').prop('checked', true).change();

        // set the fields
        doUISafeSelect($safeId('#userLabelPrinting.firstBarcodeField'),barcodeSetting.barcodeFieldsList[0]);
        doUISafeSelect($safeId('#userLabelPrinting.secondBarcodeField'),barcodeSetting.barcodeFieldsList[1]);
        doUISafeSelect($safeId('#userLabelPrinting.thirdBarcodeField'),barcodeSetting.barcodeFieldsList[2]);
    };

    // private functions

    /**
     * Adds '<li/>' items to the UI given a map and the list
     * @param listElem
     * @param listMap
     * @param fieldsList
     */
    function addToUIFieldsList(listElem,listMap,fieldsList) {
        listElem.empty();

        $.each(fieldsList, function(i,item) {
            if ('undefined' === typeof listMap[item]) {
                // continue
                return;
            }

            $('<li/>').addClass('list-group-item').attr('id',item).text(listMap[item]).appendTo(listElem);
        });
    }

    /**
     * Checks if value is existing in the '<select>' before changing the value
     * @param selectElem
     * @param value
     */
    function doUISafeSelect(selectElem,value) {
        if (selectElem.children('option[value="' + value + '"]').length > 0) {
            selectElem.val(value).change();
        }
    }

})();