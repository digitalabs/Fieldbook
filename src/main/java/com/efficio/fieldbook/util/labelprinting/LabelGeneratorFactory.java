package com.efficio.fieldbook.util.labelprinting;

import com.efficio.fieldbook.web.util.AppConstants;

import javax.annotation.Resource;

public class LabelGeneratorFactory {

    @Resource
    private CSVLabelGenerator csvLabelGenerator;

    @Resource
    private PDFLabelGenerator pdfLabelGenerator;

    @Resource
    private ExcelLabelGenerator excelLabelGenerator;

    public BaseLabelGenerator retrieveLabelGenerator(String labelType) {
        if (labelType.equalsIgnoreCase(AppConstants.LABEL_PRINTING_PDF.getString())) {
            return pdfLabelGenerator;
        } else if (labelType.equalsIgnoreCase(AppConstants.LABEL_PRINTING_EXCEL.getString())) {
            return excelLabelGenerator;
        } else {
            return csvLabelGenerator;
        }
    }
}
