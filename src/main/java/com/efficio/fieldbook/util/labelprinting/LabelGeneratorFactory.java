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

    @Resource
    private CSVSeedPreparationLabelGenerator csvSeedPreparationLabelGenerator;

    public BaseLabelGenerator retrieveLabelGenerator(String labelType) {
        if (labelType.equalsIgnoreCase(AppConstants.LABEL_PRINTING_PDF.getString())) {
            return this.pdfLabelGenerator;
        } else if (labelType.equalsIgnoreCase(AppConstants.LABEL_PRINTING_EXCEL.getString())) {
            return this.excelLabelGenerator;
        } else {
            return this.csvLabelGenerator;
        }
    }

    public CSVSeedPreparationLabelGenerator getCSVSeedPreparationLabelGenerator() {
        return this.csvSeedPreparationLabelGenerator;
    }
}
