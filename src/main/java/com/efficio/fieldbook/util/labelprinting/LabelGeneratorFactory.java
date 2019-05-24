package com.efficio.fieldbook.util.labelprinting;

import org.generationcp.commons.constant.AppConstants;

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

    @Resource
    private ExcelSeedPreparationLabelGenerator excelSeedPreparationLabelGenerator;

    @Resource
    private PDFSeedPreparationLabelGenerator pdfSeedPreparationLabelGenerator;

    public LabelGenerator retrieveLabelGenerator(final String labelType) {

        if (labelType.equalsIgnoreCase(AppConstants.LABEL_PRINTING_CSV.getString())) {
            return this.csvLabelGenerator;
        } else if (labelType.equalsIgnoreCase(AppConstants.LABEL_PRINTING_EXCEL.getString())) {
            return this.excelLabelGenerator;
        } else if (labelType.equalsIgnoreCase(AppConstants.LABEL_PRINTING_PDF.getString())) {
            return this.pdfLabelGenerator;
        }
        throw new IllegalArgumentException("Could not recognise the following label type: " + labelType);
    }

    public SeedPreparationLabelGenerator retrieveSeedPreparationLabelGenerator(final String labelType) {

        if (labelType.equalsIgnoreCase(AppConstants.LABEL_PRINTING_CSV.getString())) {
            return this.csvSeedPreparationLabelGenerator;
        } else if (labelType.equalsIgnoreCase(AppConstants.LABEL_PRINTING_EXCEL.getString())) {
            return this.excelSeedPreparationLabelGenerator;
        } else if (labelType.equalsIgnoreCase(AppConstants.LABEL_PRINTING_PDF.getString())) {
            return this.pdfSeedPreparationLabelGenerator;
        }
        throw new IllegalArgumentException("Could not recognise the following label type: " + labelType);
    }

}
