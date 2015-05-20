/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.WorkbookUtil;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.constant.ToolSection;
import org.generationcp.commons.pojo.ExportColumnHeader;
import org.generationcp.commons.pojo.ExportColumnValue;
import org.generationcp.commons.service.ExportService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.PresetDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.presets.ProgramPreset;
import org.generationcp.middleware.pojos.presets.StandardPreset;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.InventoryService;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.LabelPrintingService;
import com.efficio.fieldbook.service.api.SettingsService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.util.LabelPaperFactory;
import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.LabelFields;
import com.efficio.fieldbook.web.label.printing.bean.LabelPrintingPresets;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.efficio.fieldbook.web.label.printing.template.LabelPaper;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;
import com.efficio.pojos.labelprinting.LabelPrintingProcessingParams;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

/**
 * The Class LabelPrintingServiceImpl.
 */
@Service
public class LabelPrintingServiceImpl implements LabelPrintingService{

	private static final String ADVANCED = "ADVANCED";
	/** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(LabelPrintingServiceImpl.class);
    public static final String BARCODE = "barcode";
    public static final String SELECTED_NAME = "selectedName";
    public static final String LABEL_PRINTING_AVAILABLE_FIELDS_FIELD_NAME_KEY = "label.printing.available.fields.field.name";
    public static final String LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_KEY = "label.printing.available.fields.plot";
    public static final String LABEL_PRINTING_AVAILABLE_FIELDS_PARENTAGE_KEY = "label.printing.available.fields.parentage";
    public static final String LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_COORDINATES_KEY = "label.printing.available.fields.plot.coordinates";
    public static final String LABEL_PRINTING_AVAILABLE_FIELDS_YEAR_KEY = "label.printing.available.fields.year";
    public static final String LABEL_PRINTING_AVAILABLE_FIELDS_SEASON_KEY = "label.printing.available.fields.season";
    public static final String LABEL_PRINTING_AVAILABLE_FIELDS_NURSERY_NAME_KEY = "label.printing.available.fields.nursery.name";
    public static final String LABEL_PRINTING_AVAILABLE_FIELDS_TRIAL_NAME_KEY = "label.printing.available.fields.trial.name";
    public static final String LABEL_PRINTING_AVAILABLE_FIELDS_LOCATION_KEY = "label.printing.available.fields.location";
    public static final String LABEL_PRINTING_AVAILABLE_FIELDS_BLOCK_NAME_KEY = "label.printing.available.fields.block.name";

    public static final Integer[] BASE_LABEL_PRINTING_FIELD_IDS = new Integer[] {
            AppConstants.AVAILABLE_LABEL_FIELDS_PARENTAGE.getInt(),
            AppConstants.AVAILABLE_LABEL_FIELDS_YEAR.getInt(),
            AppConstants.AVAILABLE_LABEL_FIELDS_SEASON.getInt(),
            AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION.getInt(),
            AppConstants.AVAILABLE_LABEL_FIELDS_PLOT.getInt()
    };

    public static final Integer[] BASE_LABEL_PRINTING_FIELD_MAP_LABEL_IDS = new Integer[] {
            AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt(),
            AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt(),
            AppConstants.AVAILABLE_LABEL_FIELDS_FIELD_NAME.getInt()
    };

    /** The delimiter. */
    private String delimiter = " | ";
    
    /** The message source. */
    @Resource
    private MessageSource messageSource;
    
    @Resource
    private ExportService exportService;


	@Resource
	private WorkbenchService workbenchService;

	@Resource
	private PresetDataManager presetDataManager;

    @Resource
    private org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService;

    @Resource
    private SettingsService settingsService;
    
	@Resource
	protected FieldbookService fieldbookService;
    
    @Resource
    private ContextUtil contextUtil;
    
    @Resource
    private InventoryService inventoryMiddlewareService;
    
    @Resource 
    private PedigreeService pedigreeService;
    
    @Resource
    private CrossExpansionProperties crossExpansionProperties;

    
    @Resource
    private OntologyDataManager ontologyDataManager;
    
    private static final String UNSUPPORTED_CHARSET_IMG = "unsupported-char-set.png";
    
    private static final String ARIAL_UNI = "arialuni.ttf";

	/* (non-Javadoc)
	 * @see com.efficio.fieldbook.service.api.LabelPrintingService#generateLabels(com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap)
	 */
    
    public LabelPrintingServiceImpl(){
    	super();
    }
    
    protected BitMatrix encodeBarcode(String barcodeLabelForCode, int width, int height){
    	BitMatrix bitMatrix = null;
		try {
			bitMatrix = new Code128Writer().encode(barcodeLabelForCode,
			        BarcodeFormat.CODE_128, width, height, null);
		} catch (WriterException e) {
			LOG.debug(e.getMessage(), e);
		}catch(IllegalArgumentException e){
        	LOG.debug(e.getMessage(), e);
        }
    	return bitMatrix;
    }
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.service.api.LabelPrintingService#generateLabels(com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap)
     */
    @Override
    public String generatePDFLabels(List<StudyTrialInstanceInfo> trialInstances
            , UserLabelPrinting userLabelPrinting,
            ByteArrayOutputStream baos) throws LabelPrintingException {

        int pageSizeId = Integer.parseInt(userLabelPrinting.getSizeOfLabelSheet());
        int numberOfLabelPerRow = Integer.parseInt(userLabelPrinting.getNumberOfLabelPerRow());
        int numberofRowsPerPageOfLabel = Integer.parseInt(userLabelPrinting.getNumberOfRowsPerPageOfLabel());
        int totalPerPage = numberOfLabelPerRow * numberofRowsPerPageOfLabel;
        String leftSelectedFields = userLabelPrinting.getLeftSelectedLabelFields();
        String rightSelectedFields = userLabelPrinting.getRightSelectedLabelFields();
        String barcodeNeeded = userLabelPrinting.getBarcodeNeeded();

        String firstBarcodeField = userLabelPrinting.getFirstBarcodeField();
        String secondBarcodeField = userLabelPrinting.getSecondBarcodeField();
        String thirdBarcodeField = userLabelPrinting.getThirdBarcodeField();

        String fileName = userLabelPrinting.getFilenameDLLocation();

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);

            LabelPaper paper = LabelPaperFactory
                    .generateLabelPaper(numberOfLabelPerRow, numberofRowsPerPageOfLabel,
                            pageSizeId);

            Rectangle pageSize = PageSize.LETTER;

            if (pageSizeId == AppConstants.SIZE_OF_PAPER_A4.getInt()) {
                pageSize = PageSize.A4;
            }

            Document document = new Document(pageSize);

            //float marginLeft, float marginRight, float marginTop, float marginBottom
            document.setMargins(paper.getMarginLeft(), paper.getMarginRight(), paper.getMarginTop(),
                    paper.getMarginBottom());

            PdfWriter.getInstance(document, fileOutputStream);

            // step 3
            document.open();

            int i = 0;
            int fixTableRowSize = numberOfLabelPerRow;
            PdfPTable table = new PdfPTable(fixTableRowSize);

            float columnWidthSize = 265f;
            float[] widthColumns = new float[fixTableRowSize];

            for (int counter = 0; counter < widthColumns.length; counter++) {
                widthColumns[counter] = columnWidthSize;
            }

            table.setWidths(widthColumns);
            table.setWidthPercentage(100);
            int width = 600;
            int height = 75;

            List<File> filesToBeDeleted = new ArrayList<File>();
            float cellHeight = paper.getCellHeight();

            for (StudyTrialInstanceInfo trialInstance : trialInstances) {
                FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo = trialInstance
                        .getTrialInstance();

                Map<String, String> moreFieldInfo = generateAddedInformationField(
                        fieldMapTrialInstanceInfo, trialInstance, "");

                for (FieldMapLabel fieldMapLabel : fieldMapTrialInstanceInfo.getFieldMapLabels()) {
                	
                	if(userLabelPrinting.isStockList() 
                			&& !userLabelPrinting.getInventoryDetailsMap().containsKey(fieldMapLabel.getEntryNumber().toString())){
                		continue;
                	}

                    i++;
                    String barcodeLabelForCode = "";
                    String barcodeLabel = "";

                    if ("0".equalsIgnoreCase(barcodeNeeded)) {
                        barcodeLabel = " ";
                        barcodeLabelForCode = " ";
                    } else {
                        barcodeLabel = generateBarcodeField(moreFieldInfo, fieldMapLabel,
                                firstBarcodeField, secondBarcodeField, thirdBarcodeField,
                                fieldMapTrialInstanceInfo.getLabelHeaders(), false);
                        barcodeLabelForCode = generateBarcodeField(
                                moreFieldInfo, fieldMapLabel, firstBarcodeField,
                                secondBarcodeField, thirdBarcodeField,
                                fieldMapTrialInstanceInfo.getLabelHeaders(), true);
                    }

                    if (barcodeLabelForCode != null && barcodeLabelForCode.length() > 80) {
                        throw new LabelPrintingException("label.printing.label.too.long",
                                barcodeLabelForCode, "label.printing.label.too.long");
                    }
                    
                    Image mainImage = Image.getInstance(LabelPrintingServiceImpl.class.getClassLoader().getResource(UNSUPPORTED_CHARSET_IMG));
                    FileOutputStream fout = null;
                    
                	BitMatrix bitMatrix = encodeBarcode(barcodeLabelForCode, width, height);
                	if(bitMatrix != null){
	                	String imageLocation = System.getProperty("user.home")
	                        + "/" + Math.random() + ".png";
	                	File imageFile = new File(imageLocation);
	                    fout = new FileOutputStream(imageFile);
	                    MatrixToImageWriter.writeToStream(bitMatrix, "png", fout);
	                    filesToBeDeleted.add(imageFile);
	
	                    mainImage = Image.getInstance(imageLocation);
                	}
                   
                    
                    
                    PdfPCell cell = new PdfPCell();
                    cell.setFixedHeight(cellHeight);
                    cell.setNoWrap(false);
                    cell.setPadding(5f);
                    cell.setPaddingBottom(1f);

                    PdfPTable innerImageTableInfo = new PdfPTable(1);
                    innerImageTableInfo.setWidths(new float[] { 1 });
                    innerImageTableInfo.setWidthPercentage(82);
                    PdfPCell cellImage = new PdfPCell();
                    if ("1".equalsIgnoreCase(barcodeNeeded)) {
                        cellImage.addElement(mainImage);
                    } else {
                        cellImage.addElement(new Paragraph(" "));
                    }
                    cellImage.setBorder(Rectangle.NO_BORDER);
                    cellImage.setBackgroundColor(Color.white);
                    cellImage.setPadding(1.5f);

                    innerImageTableInfo.addCell(cellImage);

                    float fontSize = paper.getFontSize();

                    BaseFont unicode = BaseFont.createFont(ARIAL_UNI, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                    Font fontNormal = new Font(unicode, fontSize) ;
                    fontNormal.setStyle(Font.NORMAL);
                    
                    cell.addElement(innerImageTableInfo);
                    cell.addElement(new Paragraph());
                    for (int row = 0; row < 5; row++) {
                        if (row == 0) {
                            PdfPTable innerDataTableInfo = new PdfPTable(1);
                            innerDataTableInfo.setWidths(new float[] { 1 });
                            innerDataTableInfo.setWidthPercentage(85);
                            
                            Font fontNormalData = new Font(unicode, 5.0f) ;
                            fontNormal.setStyle(Font.NORMAL);
                            
                                    
                            PdfPCell cellInnerData = new PdfPCell(
                                    new Phrase(barcodeLabel, fontNormalData));

                            cellInnerData.setBorder(Rectangle.NO_BORDER);
                            cellInnerData.setBackgroundColor(Color.white);
                            cellInnerData.setPaddingBottom(0.2f);
                            cellInnerData.setPaddingTop(0.2f);
                            cellInnerData.setHorizontalAlignment(Element.ALIGN_MIDDLE);

                            innerDataTableInfo.addCell(cellInnerData);
                            innerDataTableInfo.setHorizontalAlignment(Element.ALIGN_MIDDLE);
                            cell.addElement(innerDataTableInfo);
                        }
                        PdfPTable innerTableInfo = new PdfPTable(2);
                        innerTableInfo.setWidths(new float[] { 1, 1 });
                        innerTableInfo.setWidthPercentage(85);
                        List<Integer> leftSelectedFieldIDs = SettingsUtil
                                .parseFieldListAndConvert(leftSelectedFields);
                        String leftText = generateBarcodeLabel(
                                moreFieldInfo, fieldMapLabel, leftSelectedFieldIDs,
                                fieldMapTrialInstanceInfo.getLabelHeaders(), row);
                        PdfPCell cellInnerLeft = new PdfPCell(
                                new Paragraph(leftText, fontNormal));

                        cellInnerLeft.setBorder(Rectangle.NO_BORDER);
                        cellInnerLeft.setBackgroundColor(Color.white);
                        cellInnerLeft.setPaddingBottom(0.5f);
                        cellInnerLeft.setPaddingTop(0.5f);

                        innerTableInfo.addCell(cellInnerLeft);

                        List<Integer> rightSelectedFieldIDs = SettingsUtil
                                .parseFieldListAndConvert(rightSelectedFields);
                        String rightText = generateBarcodeLabel(
                                moreFieldInfo, fieldMapLabel, rightSelectedFieldIDs,
                                fieldMapTrialInstanceInfo.getLabelHeaders(),
                                row);
                        PdfPCell cellInnerRight = new PdfPCell(
                                new Paragraph(rightText, fontNormal));

                        cellInnerRight.setBorder(Rectangle.NO_BORDER);
                        cellInnerRight.setBackgroundColor(Color.white);
                        cellInnerRight.setPaddingBottom(0.5f);
                        cellInnerRight.setPaddingTop(0.5f);

                        innerTableInfo.addCell(cellInnerRight);

                        cell.addElement(innerTableInfo);
                    }

                    cell.setBorder(Rectangle.NO_BORDER);
                    cell.setBackgroundColor(Color.white);

                    table.addCell(cell);

                    if (i % numberOfLabelPerRow == 0) {
                        // we go the next line
                        int needed = fixTableRowSize - numberOfLabelPerRow;

                        for (int neededCount = 0; neededCount < needed; neededCount++) {
                            PdfPCell cellNeeded = new PdfPCell();

                            cellNeeded.setBorder(Rectangle.NO_BORDER);
                            cellNeeded.setBackgroundColor(Color.white);

                            table.addCell(cellNeeded);
                        }

                        table.completeRow();
                        if (numberofRowsPerPageOfLabel == 10) {
                            table.setSpacingAfter(paper.getSpacingAfter());
                        }

                        document.add(table);

                        table = new PdfPTable(fixTableRowSize);
                        table.setWidths(widthColumns);
                        table.setWidthPercentage(100);

                    }
                    if (i % totalPerPage == 0) {
                        // we go the next page
                        document.newPage();
                    }
                    if(fout != null){
	                    fout.flush();
	                    fout.close();
                    }

                }
            }
            // we need to add the last row
            if (i % numberOfLabelPerRow != 0) {
                // we go the next line

                int remaining = numberOfLabelPerRow - (i % numberOfLabelPerRow);
                for (int neededCount = 0; neededCount < remaining; neededCount++) {
                    PdfPCell cellNeeded = new PdfPCell();

                    cellNeeded.setBorder(Rectangle.NO_BORDER);
                    cellNeeded.setBackgroundColor(Color.white);

                    table.addCell(cellNeeded);
                }

                table.completeRow();
                if (numberofRowsPerPageOfLabel == 10) {

                    table.setSpacingAfter(paper.getSpacingAfter());
                }

                document.add(table);

                table = new PdfPTable(fixTableRowSize);
                table.setWidths(widthColumns);
                table.setWidthPercentage(100);

            }

            document.close();
            for (File file : filesToBeDeleted) {
                file.delete();
            }
            fileOutputStream.close();

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return fileName;
    }
    
    /**
     * Generate barcode field.
     *
     * @param moreFieldInfo the more field info
     * @param fieldMapLabel the field map label
     * @param firstField the first field
     * @param secondField the second field
     * @param thirdField the third field
     * @return the string
     */
    private String generateBarcodeField(Map<String,String> moreFieldInfo
            , FieldMapLabel fieldMapLabel, String firstField, String secondField
            , String thirdField, Map<Integer, String> labelHeaders, boolean includeLabel){
        StringBuilder buffer = new StringBuilder();
        String fieldList = firstField + "," + secondField + "," + thirdField;

        List<Integer> selectedFieldIDs = SettingsUtil.parseFieldListAndConvert(fieldList);

        for (Integer selectedFieldID : selectedFieldIDs) {
            if (!("").equalsIgnoreCase(buffer.toString())) {
                buffer.append(delimiter);
            }

            buffer.append(
                    getSpecificInfo(moreFieldInfo, fieldMapLabel, selectedFieldID, labelHeaders,includeLabel));
        }


        return buffer.toString();
    }
    
    /**
     * Generate barcode label.
     *
     * @param moreFieldInfo the more field info
     * @param fieldMapLabel the field map label
     * @param selectedFieldIDs the selected fields
     * @param rowNumber the row number
     * @return the string
     */
    private String generateBarcodeLabel(Map<String,String> moreFieldInfo, 
            FieldMapLabel fieldMapLabel, List<Integer> selectedFieldIDs, Map<Integer, String> labelHeaders,int rowNumber){
        StringBuilder buffer = new StringBuilder();

        int i = 0;

        for (Integer selectedFieldID : selectedFieldIDs) {
            if (i == rowNumber) {
                buffer.append(getSpecificInfo(moreFieldInfo, fieldMapLabel, selectedFieldID, labelHeaders, true));
                break;
            }
            i++;
        }

        return buffer.toString();
    }
    
    /**
     * Gets the header.
     *
     * @param headerID the header id
     * @return the header
     */
    private String getHeader(Integer headerID, Map<Integer, String> labelHeaders){
        Locale locale = LocaleContextHolder.getLocale();

        StringBuilder buffer = new StringBuilder();

        try {
            if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getInt()) {
				buffer.append(messageSource.getMessage(
						"label.printing.available.fields.entry.num", null, locale));
			} else if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_GID.getInt()) {
				buffer.append(messageSource.getMessage(
						"label.printing.available.fields.gid", null, locale));
			} else if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_GERMPLASM_NAME.getInt()) {
				buffer.append(messageSource.getMessage(
						"label.printing.available.fields.germplasm.name", null, locale));
			} else if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_YEAR.getInt()) {
				buffer.append(messageSource.getMessage(
                        LABEL_PRINTING_AVAILABLE_FIELDS_YEAR_KEY, null, locale));
			} else if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_SEASON.getInt()) {
				buffer.append(messageSource.getMessage(
                        LABEL_PRINTING_AVAILABLE_FIELDS_SEASON_KEY, null, locale));
			} else if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME.getInt()) {
				buffer.append(messageSource.getMessage(
                        LABEL_PRINTING_AVAILABLE_FIELDS_NURSERY_NAME_KEY, null, locale));
			} else if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_NAME.getInt()) {
				buffer.append(messageSource.getMessage(
                        LABEL_PRINTING_AVAILABLE_FIELDS_TRIAL_NAME_KEY, null, locale));
			} else if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_INSTANCE_NUM.getInt()) {
				buffer.append(messageSource.getMessage(
						"label.printing.available.fields.trial.instance.num", null, locale));
			} else if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_REP.getInt()) {
				buffer.append(messageSource.getMessage(
						"label.printing.available.fields.rep", null, locale));
			} else if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION.getInt()) {
				buffer.append(messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_LOCATION_KEY, null, locale));
			} else if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt()) {
				buffer.append(messageSource.getMessage(
                        LABEL_PRINTING_AVAILABLE_FIELDS_BLOCK_NAME_KEY, null, locale));
			} else if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_PLOT.getInt()) {
				buffer.append(messageSource.getMessage(
                        LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_KEY, null, locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_PARENTAGE.getInt()) {
				buffer.append(messageSource.getMessage(
                        LABEL_PRINTING_AVAILABLE_FIELDS_PARENTAGE_KEY, null, locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt()) {
				buffer.append(messageSource.getMessage(
                        LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_COORDINATES_KEY, null, locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_FIELD_NAME.getInt()) {
				buffer.append(messageSource.getMessage(
                        LABEL_PRINTING_AVAILABLE_FIELDS_FIELD_NAME_KEY, null, locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_BARCODE.getInt()) {
				buffer.append(messageSource.getMessage(
                        "label.printing.available.fields.barcode", null, locale));
			}else if (headerID == AppConstants.AVAILABLE_LABEL_SEED_INVENTORY_AMOUNT.getInt()) {
				buffer.append(messageSource.getMessage(
                        "label.printing.seed.inventory.amount", null, locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_SEED_INVENTORY_SCALE.getInt()) {
				buffer.append(messageSource.getMessage(
                        "label.printing.seed.inventory.scale", null, locale));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_SEED_LOT_ID.getInt()) {
				buffer.append(messageSource.getMessage(
                        "label.printing.seed.inventory.lotid", null, locale));
			} else {
				String headerName = labelHeaders.get(headerID);
                if (headerName == null) {
                    headerName = "";
                }

                buffer.append(headerName);
                
			}

            return buffer.toString();
        } catch (NumberFormatException e) {
            LOG.error(e.getMessage(),e);
            return "";
        }
    }
    
    /**
     * Gets the specific info.
     *
     * @param moreFieldInfo the more field info
     * @param fieldMapLabel the field map label
     * @param headerID the barcode label
     * @return the specific info
     */
    private String getSpecificInfo(
            Map<String,String> moreFieldInfo, FieldMapLabel fieldMapLabel, Integer headerID, Map<Integer, String> labelHeaders, boolean includeHeaderLabel){
        StringBuilder buffer = new StringBuilder();

        try {

            String headerName = getHeader(headerID, labelHeaders);

            if (headerID ==  AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getInt()) {
				buffer.append(fieldMapLabel.getEntryNumber());
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_GID.getInt()) {
				String gidTemp = fieldMapLabel.getGid() == null
								? "" : fieldMapLabel.getGid().toString();
				buffer.append(gidTemp);
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_GERMPLASM_NAME.getInt()) {
				buffer.append(fieldMapLabel.getGermplasmName());
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_YEAR.getInt()) {
				buffer.append(fieldMapLabel.getStartYear());
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_SEASON.getInt()) {
				buffer.append(fieldMapLabel.getSeason());
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME.getInt()) {
				buffer.append(moreFieldInfo.get(SELECTED_NAME));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_NAME.getInt()) {
				buffer.append(moreFieldInfo.get(SELECTED_NAME));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_INSTANCE_NUM.getInt()) {
				buffer.append(moreFieldInfo.get("trialInstanceNumber"));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_REP.getInt()) {
				buffer.append(fieldMapLabel.getRep());
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION.getInt() || headerID == TermId.LOCATION_ID.getId()) {
				buffer.append(moreFieldInfo.get("locationName"));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt()) {
				buffer.append(moreFieldInfo.get("blockName"));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_PLOT.getInt()) {
				buffer.append(fieldMapLabel.getPlotNo());
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_PARENTAGE.getInt()) {
				buffer.append(fieldMapLabel.getPedigree() == null ? "" : fieldMapLabel.getPedigree());
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt()) {
				buffer.append(fieldMapLabel.getPlotCoordinate());
			} else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_FIELD_NAME.getInt()) {
				buffer.append(moreFieldInfo.get("fieldName"));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_BARCODE.getInt()) {
				buffer.append(moreFieldInfo.get(BARCODE));
			} else if (headerID == AppConstants.AVAILABLE_LABEL_SEED_INVENTORY_AMOUNT.getInt()) {
				buffer.append(fieldMapLabel.getInventoryAmount());	
			} else if (headerID == AppConstants.AVAILABLE_LABEL_SEED_INVENTORY_SCALE.getInt()) {
				buffer.append(fieldMapLabel.getScaleName());	
			} else if (headerID == AppConstants.AVAILABLE_LABEL_SEED_LOT_ID.getInt()) {
				buffer.append(fieldMapLabel.getLotId());
			} else {
				String value = fieldMapLabel.getUserFields().get(headerID);
				if (value != null) {
					buffer.append(value);
				}

			}

            String stemp = buffer.toString();
            if(stemp != null && "null".equalsIgnoreCase(stemp)) {
				stemp = " ";
			}

            if(includeHeaderLabel && headerName != null){
				stemp = headerName + " : " + stemp;
			}

            return stemp;
        } catch (NumberFormatException e) {
            LOG.error(e.getMessage(),e);
            return "";
        }
    }
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.service.api.LabelPrintingService#generateXlSLabels(org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo, com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting, java.io.ByteArrayOutputStream)
     */
    @Override
    public String generateXlSLabels(List<StudyTrialInstanceInfo> trialInstances,
            UserLabelPrinting userLabelPrinting, ByteArrayOutputStream baos)
            throws MiddlewareQueryException {

        String mainSelectedFields = userLabelPrinting.getMainSelectedLabelFields();
        boolean includeHeader = "1".equalsIgnoreCase(userLabelPrinting.getIncludeColumnHeadinginNonPdf()) ? true : false;
        boolean isBarcodeNeeded = "1".equalsIgnoreCase(userLabelPrinting.getBarcodeNeeded()) ? true : false;
        String fileName = userLabelPrinting.getFilenameDLLocation();
        String firstBarcodeField = userLabelPrinting.getFirstBarcodeField();
        String secondBarcodeField = userLabelPrinting.getSecondBarcodeField();
        String thirdBarcodeField = userLabelPrinting.getThirdBarcodeField();

        try {

            HSSFWorkbook workbook = new HSSFWorkbook();
            String sheetName = WorkbookUtil.createSafeSheetName(userLabelPrinting.getName());
            if (sheetName == null) {
                sheetName = "Labels";
            }            
            Sheet labelPrintingSheet = workbook.createSheet(sheetName);

            CellStyle labelStyle = workbook.createCellStyle();
            HSSFFont font = workbook.createFont();
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            labelStyle.setFont(font);

            CellStyle wrapStyle = workbook.createCellStyle();
            wrapStyle.setWrapText(true);
            wrapStyle.setAlignment(CellStyle.ALIGN_CENTER);

            CellStyle mainHeaderStyle = workbook.createCellStyle();

            HSSFPalette palette = workbook.getCustomPalette();
            // get the color which most closely matches the color you want to use
            HSSFColor myColor = palette.findSimilarColor(179, 165, 165);
            // get the palette index of that color
            short palIndex = myColor.getIndex();
            // code to get the style for the cell goes here
            mainHeaderStyle.setFillForegroundColor(palIndex);
            mainHeaderStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

            CellStyle mainSubHeaderStyle = workbook.createCellStyle();

            HSSFPalette paletteSubHeader = workbook.getCustomPalette();
            // get the color which most closely matches the color you want to use
            HSSFColor myColorSubHeader = paletteSubHeader.findSimilarColor(190, 190, 186);
            // get the palette index of that color
            short palIndexSubHeader = myColorSubHeader.getIndex();
            // code to get the style for the cell goes here
            mainSubHeaderStyle.setFillForegroundColor(palIndexSubHeader);
            mainSubHeaderStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            mainSubHeaderStyle.setAlignment(CellStyle.ALIGN_CENTER);

            int rowIndex = 0;
            int columnIndex = 0;

            // Create Header Information

            // Row 1: SUMMARY OF TRIAL, FIELD AND PLANTING DETAILS
            Row row = null;
            mainSelectedFields = appendBarcode(isBarcodeNeeded, mainSelectedFields);

            List<Integer> selectedFieldIDs = SettingsUtil.parseFieldListAndConvert(mainSelectedFields);

            if (includeHeader) {
                row = labelPrintingSheet.createRow(rowIndex++);
                //we add all the selected fields header
                printHeaderFields(trialInstances.get(0).getTrialInstance().getLabelHeaders(),includeHeader, selectedFieldIDs, row, columnIndex, labelStyle);
            }

            //we populate the info now
            for (StudyTrialInstanceInfo trialInstance : trialInstances) {
                FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo =
                        trialInstance.getTrialInstance();

                Map<String, String> moreFieldInfo = generateAddedInformationField(
                        fieldMapTrialInstanceInfo, trialInstance, "");

                for (FieldMapLabel fieldMapLabel : fieldMapTrialInstanceInfo.getFieldMapLabels()) {
                	
                	if(userLabelPrinting.isStockList() 
                			&& !userLabelPrinting.getInventoryDetailsMap().containsKey(fieldMapLabel.getEntryNumber().toString())){
                		continue;
                	}
                	
                    row = labelPrintingSheet.createRow(rowIndex++);
                    columnIndex = 0;

                    String barcodeLabelForCode = generateBarcodeField(
                            moreFieldInfo, fieldMapLabel, firstBarcodeField,
                            secondBarcodeField, thirdBarcodeField,fieldMapTrialInstanceInfo.getLabelHeaders(), false);
                    moreFieldInfo.put(BARCODE, barcodeLabelForCode);

                    for (Integer selectedFieldID : selectedFieldIDs) {
                        String leftText = getSpecificInfo(moreFieldInfo, fieldMapLabel, selectedFieldID,
                                fieldMapTrialInstanceInfo.getLabelHeaders(),false);
                        Cell summaryCell = row.createCell(columnIndex++);
                        summaryCell.setCellValue(leftText);
                    }
                }
            }

            for (int columnPosition = 0; columnPosition < columnIndex; columnPosition++) {
                labelPrintingSheet.autoSizeColumn((short) (columnPosition));
            }

            //Write the excel file
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            workbook.write(fileOutputStream);
            fileOutputStream.close();

        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        } 
        return fileName;
    }

    protected String appendBarcode(boolean isBarcodeNeeded, String mainSelectedFields){
        String processed = mainSelectedFields;
    	if(isBarcodeNeeded){
    		processed += "," + AppConstants.AVAILABLE_LABEL_BARCODE.getInt();
    	}
    	return processed;
    }

    protected void printHeaderFields(Map<Integer, String> labelHeaders, boolean includeHeader, List<Integer> selectedFieldIDs, Row row, int columnIndex, CellStyle labelStyle){
    	if(includeHeader){
            int currentIndex = columnIndex;
            for (Integer selectedFieldID : selectedFieldIDs) {
                String headerName = getHeader(selectedFieldID, labelHeaders);
                Cell summaryCell = row.createCell(currentIndex++);
                summaryCell.setCellValue(headerName);
                summaryCell.setCellStyle(labelStyle);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see com.efficio.fieldbook.service.api.LabelPrintingService#generateCSVLabels(org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo, com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting, java.io.ByteArrayOutputStream)
     */
    @Override
    public String generateCSVLabels(List<StudyTrialInstanceInfo> trialInstances,
            UserLabelPrinting userLabelPrinting, ByteArrayOutputStream baos)
            throws IOException {
    	String fileName = userLabelPrinting.getFilenameDLLocation();
    	String mainSelectedFields = userLabelPrinting.getMainSelectedLabelFields();
        boolean includeHeader = "1".equalsIgnoreCase(userLabelPrinting.getIncludeColumnHeadinginNonPdf()) ? true : false;            	    
        boolean isBarcodeNeeded = "1".equalsIgnoreCase(userLabelPrinting.getBarcodeNeeded()) ? true : false;        
        
        mainSelectedFields = appendBarcode(isBarcodeNeeded, mainSelectedFields);

        List<Integer> selectedFieldIDs = SettingsUtil.parseFieldListAndConvert(mainSelectedFields);
    	List<ExportColumnHeader> exportColumnHeaders = generateColumnHeaders(selectedFieldIDs, trialInstances.get(0).getTrialInstance().getLabelHeaders());
    	
		List<Map<Integer, ExportColumnValue>> exportColumnValues = generateColumnValues(trialInstances, selectedFieldIDs, userLabelPrinting);
		
		exportService.generateCSVFile(exportColumnValues, exportColumnHeaders, fileName, includeHeader);
    	
    	return fileName;
    }
    
    private List<Map<Integer, ExportColumnValue>> generateColumnValues(List<StudyTrialInstanceInfo> trialInstances, List<Integer> selectedFieldIDs, UserLabelPrinting userLabelPrinting) {
    	List<Map<Integer, ExportColumnValue>> columnValues = new ArrayList<>();
    	
        String firstBarcodeField = userLabelPrinting.getFirstBarcodeField();
        String secondBarcodeField = userLabelPrinting.getSecondBarcodeField();
        String thirdBarcodeField = userLabelPrinting.getThirdBarcodeField();
        
    	//we populate the info now
        for(StudyTrialInstanceInfo trialInstance : trialInstances){
            FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo = trialInstance.getTrialInstance();
            
            Map<String,String> moreFieldInfo = generateAddedInformationField(fieldMapTrialInstanceInfo, trialInstance, "");
            for(FieldMapLabel fieldMapLabel : fieldMapTrialInstanceInfo.getFieldMapLabels()){
            	
            	if(userLabelPrinting.isStockList() 
            			&& !userLabelPrinting.getInventoryDetailsMap().containsKey(fieldMapLabel.getEntryNumber().toString())){
            		continue;
            	}
            	
            	String barcodeLabelForCode = generateBarcodeField(
                        moreFieldInfo, fieldMapLabel, firstBarcodeField,
                        secondBarcodeField, thirdBarcodeField, fieldMapTrialInstanceInfo.getLabelHeaders(), false);
                moreFieldInfo.put(BARCODE, barcodeLabelForCode);
                
            	Map<Integer, ExportColumnValue> rowMap = generateRowMap(fieldMapTrialInstanceInfo.getLabelHeaders(),selectedFieldIDs, moreFieldInfo, fieldMapLabel);
            	columnValues.add(rowMap);
            }
        }
        
        return columnValues;
	}

    @SuppressWarnings("unchecked")
	public void populateUserSpecifiedLabelFields(List<FieldMapTrialInstanceInfo> trialFieldMap, Workbook workbook, 
    		String selectedFields, boolean isTrial, boolean isStockList) {

        LabelPrintingProcessingParams params = new LabelPrintingProcessingParams();
        params.setVariableMap(convertToMap(workbook.getConditions(), workbook.getFactors()));
        params.setSelectedFieldIDs(SettingsUtil.parseFieldListAndConvert(selectedFields));
        
        if(isStockList){
        	GermplasmList stockList = trialFieldMap.get(0).getStockList();
        	params.setAllFieldIDs(convertToListInteger(this.getAvailableLabelFieldsForStockList(
        			getStockListType(stockList.getType()), Locale.ENGLISH, workbook.getStudyId())));
        } else {
        	params.setAllFieldIDs(convertToListInteger(this.getAvailableLabelFieldsForStudy(isTrial, true, Locale.ENGLISH, workbook.getStudyId())));
        }
        
        Map<String, List<MeasurementRow>> measurementData = null;
        Map<String, MeasurementRow> environmentData = null;

        if (isTrial) {
            measurementData = extractMeasurementRowsPerTrialInstance(
                    workbook.getObservations());
            environmentData = extractEnvironmentMeasurementDataPerTrialInstance(
                    workbook);
        }

        for (FieldMapTrialInstanceInfo instanceInfo : trialFieldMap) {
            params.setInstanceInfo(instanceInfo);
            
            if(isStockList){
                params.setStockList(instanceInfo.getStockList());
                params.setIsStockList(true);
            }
            
            if (isTrial) {
                params.setInstanceMeasurements(measurementData.get(instanceInfo.getTrialInstanceNo()));
                params.setEnvironmentData(environmentData.get(instanceInfo.getTrialInstanceNo()));
            } else {
            	params.setInstanceMeasurements(workbook.getObservations());
            }
            
            processUserSpecificLabelsForInstance(params, workbook);
            processInventorySpecificLabelsForInstance(params,workbook);
        }
    }

	private List<Integer> convertToListInteger(List<LabelFields> availableLabelFields) {
		List<Integer> list = new ArrayList<Integer>();
		for (LabelFields field : availableLabelFields){
			list.add(field.getId());
		}
		return list;
	}

	private void processInventorySpecificLabelsForInstance(
			LabelPrintingProcessingParams params, Workbook workbook) {
		Integer studyId = workbook.getStudyDetails().getId();
		Map<Integer,InventoryDetails> inventoryDetailsMap = retrieveInventoryDetailsMap(studyId,workbook);
		
		for (MeasurementRow measurement : params.getInstanceMeasurements()) {
            FieldMapLabel label = params.getInstanceInfo().getFieldMapLabel(
                    measurement.getExperimentId());
            
            InventoryDetails inventoryDetails = inventoryDetailsMap.get(label.getGid());
            if(inventoryDetails != null){
                label.setInventoryAmount(inventoryDetails.getAmount());
                label.setScaleName(inventoryDetails.getScaleName());
                label.setLotId(inventoryDetails.getLotId());
            }
        }
	}

	private Map<Integer, InventoryDetails> retrieveInventoryDetailsMap(Integer studyId, Workbook workbook) {
		Map<Integer,InventoryDetails> inventoryDetailsMap = new HashMap<Integer,InventoryDetails>(); 
				
		try {
    		GermplasmList germplasmList = null;
    		GermplasmListType listType = (workbook.isNursery())? GermplasmListType.NURSERY : GermplasmListType.TRIAL;
			List<GermplasmList> germplasmLists = fieldbookMiddlewareService.getGermplasmListsByProjectId(studyId, listType);
			if(!germplasmLists.isEmpty()){
				germplasmList = germplasmLists.get(0);
			}
			
			if(germplasmList != null){
				Integer listId = germplasmList.getId();
				String germplasmListType = germplasmList.getType();
				List<InventoryDetails> inventoryDetailList = inventoryMiddlewareService.getInventoryDetailsByGermplasmList(listId,germplasmListType);
				
				for(InventoryDetails inventoryDetails : inventoryDetailList){
					if(inventoryDetails.getLotId() != null){
						inventoryDetailsMap.put(inventoryDetails.getGid(), inventoryDetails);
					}
				}
			}
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(),e);
		}
		
		return inventoryDetailsMap;
	}

	@Override
    public void deleteProgramPreset(Integer programPresetId)
            throws MiddlewareQueryException {

        presetDataManager.deleteProgramPreset(programPresetId);

    }

    protected Map<String, MeasurementRow> extractEnvironmentMeasurementDataPerTrialInstance(Workbook workbook) {
        Map<String, MeasurementRow> data = new HashMap<>();

        for (MeasurementRow row : workbook.getTrialObservations()) {
            String trialInstance = row.getMeasurementData(TermId.TRIAL_INSTANCE_FACTOR.getId()).getValue();
            data.put(trialInstance, row);
        }

        return data;
    }

    protected void processUserSpecificLabelsForInstance(LabelPrintingProcessingParams params, 
    		Workbook workbook) {

        params.setLabelHeaders(new HashMap<Integer, String>());
        boolean firstEntry = true;
        
        if(params.isStockList()){
        	params.setInventoryDetailsMap(getInventoryDetailsMap(params.getStockList()));
        } 
        
    	for (MeasurementRow measurement : params.getInstanceMeasurements()) {
    		String entryNo = measurement.getMeasurementData(TermId.ENTRY_NO.getId()).getValue();
    		if(params.getInventoryDetailsMap().containsKey(entryNo)){
                FieldMapLabel label = params.getInstanceInfo().getFieldMapLabel(
                        measurement.getExperimentId());
                
                Map<Integer, String> userSpecifiedLabels = extractDataForUserSpecifiedLabels(params, measurement, firstEntry, workbook);
                
                params.setUserSpecifiedLabels(userSpecifiedLabels);

                label.setUserFields(userSpecifiedLabels);

                if (firstEntry) {
                    firstEntry = false;
                }

                params.getInstanceInfo().setLabelHeaders(params.getLabelHeaders());
    		}
        }
    }
    
    public Map<String,InventoryDetails> getInventoryDetailsMap(GermplasmList stockList){
    	Map<String,InventoryDetails> inventoryDetailsMap = new HashMap<String, InventoryDetails>();
		List<InventoryDetails> listDataProjects;
		try {
			listDataProjects = inventoryMiddlewareService.getInventoryListByListDataProjectListId(stockList.getId(),getStockListType(stockList.getType()));

			for(InventoryDetails entry : listDataProjects){
				setCross(entry);
				inventoryDetailsMap.put(entry.getEntryId().toString(), entry);
			}
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(),e);
		}
		return inventoryDetailsMap;
    }

	private void setCross(InventoryDetails entry) {
		Integer gid = entry.getGid();
		try {
			String cross = pedigreeService.getCrossExpansion(gid, crossExpansionProperties);
			entry.setCross(cross);
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(),e);
		}
	}

	protected Map<Integer, MeasurementVariable> convertToMap(List<MeasurementVariable>... variables) {
        Map<Integer, MeasurementVariable> map = new HashMap<>();

        for (List<MeasurementVariable> variableList : variables) {
            for (MeasurementVariable variable : variableList) {
                map.put(variable.getTermId(), variable);
            }
        }

        return map;
    }

    protected Map<Integer, String> extractDataForUserSpecifiedLabels(LabelPrintingProcessingParams params, MeasurementRow measurementRow, boolean populateHeaders, Workbook workbook) {
    	
    	Map<Integer, String> values = new HashMap<>();

    	for (Integer termID : params.getAllFieldIDs()) {
    		
    		if(params.isStockList()){
    			
    			String entryNo = measurementRow.getMeasurementData(TermId.ENTRY_NO.getId()).getValue();
    			populateValuesForStockList(params,entryNo,termID,values,populateHeaders);
    			populateValuesForNurseryManagement(params, workbook, termID, values, populateHeaders);
    			
    		} else if (!populateValuesFromMeasurement(params, measurementRow, termID, values, populateHeaders)){
    		
    			if (workbook.isNursery()){

        			populateValuesForNursery(params, workbook, termID, values, populateHeaders);

        		} else {

        			populateValuesForTrial(params, workbook, termID, values, populateHeaders);

        		}
    			
    		}

    	}

    	return values;

    }

	private void populateValuesForNurseryManagement(
			LabelPrintingProcessingParams params, Workbook workbook,
			Integer termID, Map<Integer, String> values, boolean populateHeaders) {
    	List<MeasurementVariable> variables = new ArrayList<>();
    	variables.addAll(workbook.getConditions());
    	
    	Integer newTermId = getCounterpartTermId(termID);
    	
    	MeasurementVariable factorVariable = getMeasurementVariableByTermId(newTermId, variables);
    	
    	if (factorVariable != null){
    		values.put(newTermId, factorVariable.getValue());

    		if (populateHeaders){
    			params.getLabelHeaders().put(newTermId, factorVariable.getName());
    		}

    	}
	}

	private void populateValuesForStockList(
			LabelPrintingProcessingParams params, String entryNo,
			Integer termID, Map<Integer, String> values, boolean populateHeaders) {
		
		InventoryDetails row = params.getInventoryDetailsMap().get(entryNo);		
		String value = null;

		value = populateStockListFromGermplasmDescriptorVariables(termID,row);
		
		if(value == null){
			value = populateStockListFromInventoryVariables(termID,row);
		}
		
		if(value == null){
			value = populateStockListFromCrossingVariables(termID,row);
		}
		
		if(value != null){
			
			values.put(termID, value);

			if (populateHeaders) {
				try {
					params.getLabelHeaders().put(termID, ontologyDataManager.getTermById(termID).getName());
				} catch (MiddlewareQueryException e) {
					LOG.error(e.getMessage(),e);
				}
			}
		}

	}

	private String populateStockListFromGermplasmDescriptorVariables(
			Integer termID, InventoryDetails row) {
		String value = null;
		if(termID.equals(TermId.GID.getId())){
			value = getValueForStockList(row.getGid());
		} else if(termID.equals(TermId.DESIG.getId())){
			value = getValueForStockList(row.getGermplasmName());
		} else if(termID.equals(TermId.ENTRY_NO.getId())){
			value = getValueForStockList(row.getEntryId());
		} else if(termID.equals(TermId.CROSS.getId())){
			value = getValueForStockList(row.getCross());
		} else if(termID.equals(TermId.SEED_SOURCE.getId())){
			value = getValueForStockList(row.getSource());
		} 
		return value;
	}

	private String populateStockListFromInventoryVariables(Integer termID,
			InventoryDetails row) {
		String value = null;
		if(termID.equals(TermId.STOCKID.getId())){
			value = getValueForStockList(row.getInventoryID());
		} else if(termID.equals(TermId.LOT_LOCATION_INVENTORY.getId())){
			value = getValueForStockList(row.getLocationName());
		} else if(termID.equals(TermId.AMOUNT_INVENTORY.getId())){
			value = getValueForStockList(row.getAmount());
		} else if(termID.equals(TermId.SCALE_INVENTORY.getId())){
			value = getValueForStockList(row.getScaleName());
		} else if(termID.equals(TermId.COMMENT_INVENTORY.getId())){
			value = getValueForStockList(row.getComment());
		} 
		return value;
	}

	private String populateStockListFromCrossingVariables(Integer termID, InventoryDetails row) {
		String value = null;
		if(termID.equals(TermId.DUPLICATE.getId())){
			value = getValueForStockList(row.getDuplicate());
		} else if(termID.equals(TermId.BULK_WITH.getId())){
			value = getValueForStockList(row.getBulkWith());
		} else if(termID.equals(TermId.BULK_COMPL.getId())){
			value = getValueForStockList(row.getBulkCompl());
		}
		return value;
	}

	private String getValueForStockList(Object value) {
		if(value != null){
			return value.toString();
		}
		return "";
	}
	
	@Override
	public GermplasmListType getStockListType(String type){
		return type.equalsIgnoreCase(ADVANCED)? GermplasmListType.ADVANCED : GermplasmListType.CROSSES;
	}

	protected Boolean populateValuesFromMeasurement(LabelPrintingProcessingParams params,
    		MeasurementRow measurementRow, Integer termID, Map<Integer, String> values,
    		boolean populateHeaders) {

    	try{

    		MeasurementData data = measurementRow.getMeasurementData(termID);

    		if (data != null){
    			
    			String value = data.getDisplayValue();
        		values.put(termID, value);

        		if (populateHeaders) {
        			params.getLabelHeaders().put(termID, data.getMeasurementVariable().getName());
        		}
        		return true;
    		}

    	} catch (NumberFormatException e) {
    		LOG.error(e.getMessage(),e);
    	}

    	return false;
    }
    
    protected Integer getCounterpartTermId(Integer termId){
    	
    	String nameTermId = SettingsUtil.getNameCounterpart(termId, AppConstants.ID_NAME_COMBINATION.getString());
    	
    	if (!StringUtils.isEmpty(nameTermId)){
    		return Integer.valueOf(nameTermId);
    	}else{
    		return termId;
    	}
    }

    protected void populateValuesForTrial(LabelPrintingProcessingParams params, Workbook workbook, Integer termID,
    		Map<Integer, String> values, boolean populateHeaders) {

    	
    	Integer newTermId = getCounterpartTermId(termID);
    	
    	MeasurementVariable conditionData = params.getVariableMap().get(newTermId);
    	
    	if (conditionData != null){
    		values.put(newTermId, conditionData.getValue());

    		if (populateHeaders){
    			params.getLabelHeaders().put(newTermId, conditionData.getName());
    		}
    	}

    	MeasurementData enviromentData = params.getEnvironmentData().getMeasurementData(newTermId);

    	if (enviromentData != null){
    		values.put(newTermId, enviromentData.getValue());

    		if (populateHeaders){
    			params.getLabelHeaders().put(newTermId, enviromentData.getLabel());
    		}
    	}
    	  
    }

    protected void populateValuesForNursery(LabelPrintingProcessingParams params,
    		Workbook workbook, Integer termID, Map<Integer, String> values, boolean populateHeaders) {
    	
    	List<MeasurementVariable> variables = new ArrayList<>();
    	variables.addAll(workbook.getFactors());
    	variables.addAll(workbook.getConditions());
    	variables.addAll(workbook.getConstants());
    	
    	Integer newTermId = getCounterpartTermId(termID);
    	
    	MeasurementVariable factorVariable = getMeasurementVariableByTermId(newTermId, variables);
    	
    	if (factorVariable != null){
    		values.put(newTermId, factorVariable.getValue());

    		if (populateHeaders){
    			params.getLabelHeaders().put(newTermId, factorVariable.getName());
    		}

    	}
    	
    }

    private MeasurementVariable getMeasurementVariableByTermId(Integer termId, List<MeasurementVariable> measumentVariables){
    	for (MeasurementVariable measurementVariable : measumentVariables){
    		if (measurementVariable.getTermId() == termId){
    			return measurementVariable;
    		}
    	}
    	return null;
    }

    protected Map<String, List<MeasurementRow>> extractMeasurementRowsPerTrialInstance(List<MeasurementRow> dataRows) {
        // sort the observations by instance number, and then by experiment ID to simplify later process
        Collections.sort(dataRows, new Comparator<MeasurementRow>() {
            @Override public int compare(MeasurementRow o1, MeasurementRow o2) {
                String instanceID1 = o1.getMeasurementData(TermId.TRIAL_INSTANCE_FACTOR.getId()).getValue();
                String instanceID2 = o2.getMeasurementData(TermId.TRIAL_INSTANCE_FACTOR.getId()).getValue();

                if (instanceID1.equals(instanceID2)) {
                    return new Integer(o1.getExperimentId()).compareTo(new Integer(o2.getExperimentId()));
                } else {
                    return instanceID1.compareTo(instanceID2);
                }
            }
        });

        Map<String, List<MeasurementRow>> measurements = new HashMap<>();

        for (MeasurementRow row : dataRows) {
            String trialInstance = row.getMeasurementData(TermId.TRIAL_INSTANCE_FACTOR.getId()).getValue();
            List<MeasurementRow> list = measurements.get(trialInstance);

            if (list == null) {
                list = new ArrayList<>();
                measurements.put(trialInstance, list);
            }

            list.add(row);
        }

        return measurements;
    }



    protected Map<String, String> generateAddedInformationField(FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo, StudyTrialInstanceInfo trialInstance, String barCode){
    	 Map<String,String> moreFieldInfo = new HashMap<String, String>();
         moreFieldInfo.put("locationName", fieldMapTrialInstanceInfo.getLocationName());
         moreFieldInfo.put("blockName", fieldMapTrialInstanceInfo.getBlockName());
         moreFieldInfo.put("fieldName", fieldMapTrialInstanceInfo.getFieldName());
         moreFieldInfo.put(SELECTED_NAME, trialInstance.getFieldbookName());
         moreFieldInfo.put("trialInstanceNumber", 
                 fieldMapTrialInstanceInfo.getTrialInstanceNo());
         moreFieldInfo.put(BARCODE, barCode);
         
         return moreFieldInfo;
    }

	private Map<Integer, ExportColumnValue> generateRowMap(Map<Integer, String> labelHeaders,List<Integer> selectedFieldIDs,
			Map<String, String> moreFieldInfo, FieldMapLabel fieldMapLabel) {
		Map<Integer, ExportColumnValue> rowMap = new HashMap<Integer, ExportColumnValue>();

        for (Integer selectedFieldID : selectedFieldIDs) {

            try {

                String value = getSpecificInfo(moreFieldInfo, fieldMapLabel, selectedFieldID, labelHeaders,false);
                ExportColumnValue columnValue = new ExportColumnValue(selectedFieldID, value);
                rowMap.put(selectedFieldID, columnValue);
            } catch (NumberFormatException e) {
                LOG.error(e.getMessage(),e);
            }
        }

        return rowMap;
	}

	private List<ExportColumnHeader> generateColumnHeaders(List<Integer> selectedFieldIDs, Map<Integer, String> labelHeaders) {
    	List<ExportColumnHeader> columnHeaders = new ArrayList<ExportColumnHeader>();

        for (Integer selectedFieldID : selectedFieldIDs) {
            String headerName = getHeader(selectedFieldID, labelHeaders);
            ExportColumnHeader columnHeader = new ExportColumnHeader(selectedFieldID,
                    headerName, true);
            columnHeaders.add(columnHeader);
        }


    	return columnHeaders;
	}

	/**
	 * Gets the available label fields.
	 * @param isTrial     the is trial
	 * @param hasFieldMap the has field map
	 * @param locale      the locale
	 * @return
	 */
	public List<LabelFields> getAvailableLabelFieldsForFieldMap(boolean isTrial, boolean hasFieldMap, Locale locale){
        List<LabelFields> labelFieldsList = new ArrayList<LabelFields>();
        
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage("label.printing.available.fields.entry.num", null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getInt(),true));
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage("label.printing.available.fields.gid", null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_GID.getInt(), true));
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage("label.printing.available.fields.germplasm.name", null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_GERMPLASM_NAME.getInt(), true));
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_PARENTAGE_KEY, null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_PARENTAGE.getInt(), true));
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_YEAR_KEY, null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_YEAR.getInt(), false));
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_SEASON_KEY, null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_SEASON.getInt(), false));
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_LOCATION_KEY, null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION.getInt(), false));
        
        if(isTrial){
            labelFieldsList.add(new LabelFields(
                    messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_TRIAL_NAME_KEY, null, locale)
                    , AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_NAME.getInt(), false));
            labelFieldsList.add(new LabelFields(
                    messageSource.getMessage("label.printing.available.fields.trial.instance.num", null, locale)
                    , AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_INSTANCE_NUM.getInt(), false));
            labelFieldsList.add(new LabelFields(
                    messageSource.getMessage("label.printing.available.fields.rep", null, locale)
                    , AppConstants.AVAILABLE_LABEL_FIELDS_REP.getInt(), false));
        }else{
            labelFieldsList.add(new LabelFields(
                    messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_NURSERY_NAME_KEY, null, locale)
                    , AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME.getInt(), false));
        }
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_KEY, null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_PLOT.getInt(), false));
        
        addAvailableFieldsForFieldMap(hasFieldMap, locale, labelFieldsList);
        
        return labelFieldsList;
    }


    public List<LabelFields> getAvailableLabelFieldsForStudy(boolean isTrial, boolean hasFieldMap, Locale locale, int studyID) {
        List<LabelFields> labelFieldsList = new ArrayList<>();

     	labelFieldsList.add(new LabelFields(
                messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_PARENTAGE_KEY, null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_PARENTAGE.getInt(), true));
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_YEAR_KEY, null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_YEAR.getInt(), false));
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_SEASON_KEY, null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_SEASON.getInt(), false));
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_LOCATION_KEY, null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION.getInt(), false));
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_KEY, null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_PLOT.getInt(), false));
        
		Workbook workbook = null;
		if (isTrial) {
            labelFieldsList.add(new LabelFields(
                                messageSource.getMessage(
                                        LABEL_PRINTING_AVAILABLE_FIELDS_TRIAL_NAME_KEY, null, locale)
                                , AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_NAME.getInt(), false));

			try {
                workbook = fieldbookMiddlewareService.getTrialDataSet(studyID);

                labelFieldsList.addAll(settingsService.retrieveTrialSettingsAsLabels(workbook));
                labelFieldsList.addAll(settingsService.retrieveTrialEnvironmentAndExperimentalDesignSettingsAsLabels(workbook));
                labelFieldsList.addAll(settingsService.retrieveGermplasmDescriptorsAsLabels(workbook));

            } catch (MiddlewareQueryException e) {
                LOG.error(e.getMessage(), e);
            }
			
        } else {
        	
            labelFieldsList.add(new LabelFields(
                                messageSource.getMessage(
                                        LABEL_PRINTING_AVAILABLE_FIELDS_NURSERY_NAME_KEY, null, locale)
                                , AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME.getInt(), false));
            try {
                workbook = fieldbookMiddlewareService.getNurseryDataSet(studyID);

                labelFieldsList.addAll(settingsService.retrieveNurseryManagementDetailsAsLabels(workbook));
                labelFieldsList.addAll(settingsService.retrieveGermplasmDescriptorsAsLabels(workbook));
                
            } catch (MiddlewareQueryException e) {
                LOG.error(e.getMessage(), e);
            }
        }
		
		// add trait fields
		labelFieldsList.addAll(settingsService.retrieveTraitsAsLabels(workbook));
		
		// add field map fields
		addAvailableFieldsForFieldMap(hasFieldMap, locale, labelFieldsList);
		
		// add inventory fields if any
		if(hasInventoryValues(studyID,workbook.isNursery())){
			labelFieldsList.addAll(addInventoryRelatedLabelFields(studyID,locale));
		}
		
        return labelFieldsList;
    }
    
	@Override
	public List<LabelFields> getAvailableLabelFieldsForStockList(
			GermplasmListType listType, Locale locale, int studyID) {
		List<LabelFields> labelFieldsList = new ArrayList<>();
		
		// Nursery Management Fields
        labelFieldsList.add(new LabelFields(
                messageSource.getMessage(
                        LABEL_PRINTING_AVAILABLE_FIELDS_NURSERY_NAME_KEY, null, locale)
                , AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME.getInt(), false));
        
        Workbook workbook = null;
        try {
            workbook = fieldbookMiddlewareService.getNurseryDataSet(studyID);

            labelFieldsList.addAll(settingsService.retrieveNurseryManagementDetailsAsLabels(workbook));
            
        } catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
        }
        
        // Stock List Specific Fields
        labelFieldsList.addAll(addStockListDetailsFields(locale, listType));
		
		return labelFieldsList;
	}

	private List<LabelFields> addStockListDetailsFields(Locale locale, GermplasmListType listType) {
		List<LabelFields> labelFieldList = new ArrayList<LabelFields>();

		labelFieldList.addAll(getGermplasmDescriptors());

		labelFieldList.add(new LabelFields(
				ColumnLabels.STOCKID.getTermNameFromOntology(ontologyDataManager)
                , TermId.STOCKID.getId(), true));
    	
    	labelFieldList.add(new LabelFields(
    			ColumnLabels.LOT_LOCATION.getTermNameFromOntology(ontologyDataManager)
                , TermId.LOT_LOCATION_INVENTORY.getId(), true));
    	
    	labelFieldList.add(new LabelFields(
    			ColumnLabels.AMOUNT.getTermNameFromOntology(ontologyDataManager)
                , TermId.AMOUNT_INVENTORY.getId(), true));
				
		labelFieldList.add(new LabelFields(
				ColumnLabels.SCALE.getTermNameFromOntology(ontologyDataManager)
                , TermId.SCALE_INVENTORY.getId(), true));
		
    	labelFieldList.add(new LabelFields(
    			ColumnLabels.COMMENT.getTermNameFromOntology(ontologyDataManager)
                , TermId.COMMENT_INVENTORY.getId(), true));
    	
    	if(listType.equals(GermplasmListType.CROSSES)){
    		
    		labelFieldList.add(new LabelFields(
        			ColumnLabels.DUPLICATE.getTermNameFromOntology(ontologyDataManager)
                    , TermId.DUPLICATE.getId(), true));
    		
    		labelFieldList.add(new LabelFields(
        			ColumnLabels.BULK_WITH.getTermNameFromOntology(ontologyDataManager)
                    , TermId.BULK_WITH.getId(), true));
    		
        	labelFieldList.add(new LabelFields(
        			ColumnLabels.BULK_COMPL.getTermNameFromOntology(ontologyDataManager)
                    , TermId.BULK_COMPL.getId(), true));
        	
    	}

    	
		return labelFieldList;	
	}

	private List<LabelFields> getGermplasmDescriptors() {
		 List<LabelFields> germplasmDescriptors = new ArrayList<LabelFields>();
		 
		try {
			List<StandardVariableReference> stdVars = fieldbookService.filterStandardVariablesForSetting(AppConstants.SEGMENT_GERMPLASM.getInt(), new ArrayList<SettingDetail>());
			
			for(StandardVariableReference stdVar : stdVars){
				if(stdVar.getId() == TermId.GID.getId() 
						|| stdVar.getId() == TermId.DESIG.getId()
						|| stdVar.getId() == TermId.ENTRY_NO.getId()
						|| stdVar.getId() == TermId.CROSS.getId()
						|| stdVar.getId() == TermId.SEED_SOURCE.getId()){
					germplasmDescriptors.add(new LabelFields(stdVar.getName(), stdVar.getId(), true));
				}
			}
			
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(),e);
		}
		return germplasmDescriptors;
	}

	private void addAvailableFieldsForFieldMap(boolean hasFieldMap, 
			Locale locale, List<LabelFields> labelFieldsList) {
		if (hasFieldMap) {
            labelFieldsList.add(new LabelFields(
                    messageSource
                            .getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_BLOCK_NAME_KEY, null, locale)
                    , AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt(), false));
            labelFieldsList.add(new LabelFields(
                    messageSource
                            .getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_COORDINATES_KEY, null,
                                    locale)
                    , AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt(), false));
            labelFieldsList.add(new LabelFields(
                    messageSource
                            .getMessage(LABEL_PRINTING_AVAILABLE_FIELDS_FIELD_NAME_KEY, null, locale)
                    , AppConstants.AVAILABLE_LABEL_FIELDS_FIELD_NAME.getInt(), false));
        }
	}
    
	/***
	 * Returned true if the current study's germplasm list has inventory details
	 * @param studyID
	 * @param isNursery 
	 * @return
	 */
    protected boolean hasInventoryValues(int studyID, boolean isNursery){    	
    	try {
    		GermplasmList germplasmList = null;
    		GermplasmListType listType = (isNursery)? GermplasmListType.NURSERY : GermplasmListType.TRIAL;
			List<GermplasmList> germplasmLists = fieldbookMiddlewareService.getGermplasmListsByProjectId(studyID, listType);
			if(!germplasmLists.isEmpty()){
				germplasmList = germplasmLists.get(0);
			}
			
			if(germplasmList != null){
				Integer listId = germplasmList.getId();
                String germplasmListType = germplasmList.getType();
                List<InventoryDetails> inventoryDetailList = inventoryMiddlewareService.getInventoryDetailsByGermplasmList(listId,germplasmListType);
				
                	for(InventoryDetails inventoryDetails : inventoryDetailList){
    					if(inventoryDetails.getLotId() != null){
    						return true;
    					}
    				}
                }
		} catch (MiddlewareQueryException e) {
			LOG.error(e.getMessage(),e);
		}
    	
    	return false;
    }


    protected List<LabelFields> addInventoryRelatedLabelFields(int studyID, Locale locale) {
    	List<LabelFields> labelFieldList = new ArrayList<LabelFields>();
    	
    	labelFieldList.add(new LabelFields(
				messageSource.getMessage("label.printing.seed.inventory.amount", null, locale)
                , AppConstants.AVAILABLE_LABEL_SEED_INVENTORY_AMOUNT.getInt(), false));
				
		labelFieldList.add(new LabelFields(
				messageSource.getMessage("label.printing.seed.inventory.scale", null, locale)
                , AppConstants.AVAILABLE_LABEL_SEED_INVENTORY_SCALE.getInt(), false));
		
		labelFieldList.add(new LabelFields(
				messageSource.getMessage("label.printing.seed.inventory.lotid", null, locale)
                , AppConstants.AVAILABLE_LABEL_SEED_LOT_ID.getInt(), false));
		
		return labelFieldList;
	}

    public boolean checkAndSetFieldmapProperties(UserLabelPrinting userLabelPrinting, FieldMapInfo fieldMapInfoDetail) {
    	//if there are datasets with fieldmap, check if all trial instances of the study have fieldmaps
        if (!fieldMapInfoDetail.getDatasetsWithFieldMap().isEmpty()) {
        	for (FieldMapDatasetInfo dataset : fieldMapInfoDetail.getDatasetsWithFieldMap()) {
        		if (dataset.getTrialInstances().size() == dataset.getTrialInstancesWithFieldMap().size()) {
        			userLabelPrinting.setFieldMapsExisting(true);
        		} else {
        			userLabelPrinting.setFieldMapsExisting(false);
        		}
        	}
        	return true;
        } else {
        	userLabelPrinting.setFieldMapsExisting(false);
        	return false;
        }
    }

    @Override
    public LabelPrintingPresets getLabelPrintingPreset(Integer presetId, Integer presetType)
            throws MiddlewareQueryException {
        if (LabelPrintingPresets.STANDARD_PRESET == presetType) {
            StandardPreset standardPreset = workbenchService.getStandardPresetById(presetId);

            return new LabelPrintingPresets(presetId,standardPreset.getName(),LabelPrintingPresets.STANDARD_PRESET);

        } else {
            ProgramPreset programPreset = presetDataManager.getProgramPresetById(presetId);

            return new LabelPrintingPresets(presetId,programPreset.getName(),LabelPrintingPresets.PROGRAM_PRESET);
        }
    }

    @Override
    public ProgramPreset getLabelPrintingProgramPreset(Integer programPresetId)
            throws MiddlewareQueryException {
        return  presetDataManager.getProgramPresetById(programPresetId);
    }

    @Override
    public List<LabelPrintingPresets> getAllLabelPrintingPresetsByName(String presetName,
            Integer programId, Integer presetType)
            throws MiddlewareQueryException {
        final List<LabelPrintingPresets> out = new ArrayList<>();

        final Project project = workbenchService.getProjectById(programId.longValue());

        if (LabelPrintingPresets.PROGRAM_PRESET == presetType) {
            List<ProgramPreset> presets = presetDataManager.getProgramPresetFromProgramAndToolByName(
                    presetName, contextUtil.getCurrentProgramUUID(), workbenchService.getFieldbookWebTool().getToolId().intValue(), ToolSection.FBK_LABEL_PRINTING.name());

            for (ProgramPreset preset : presets) {
                out.add(new LabelPrintingPresets(preset.getProgramPresetId(),preset.getName(),LabelPrintingPresets.PROGRAM_PRESET));
            }
        } else {
            final String cropName = project.getCropType().getCropName();

            List<StandardPreset> standardPresets = workbenchService.getStandardPresetByCropAndPresetName(presetName,workbenchService.getFieldbookWebTool().getToolId().intValue(),cropName,ToolSection.FBK_LABEL_PRINTING.name());

            for (StandardPreset preset : standardPresets) {
                out.add(new LabelPrintingPresets(preset.getStandardPresetId(),preset.getName(),LabelPrintingPresets.STANDARD_PRESET));
            }
        }

        return out;
    }

	@Override
	public List<LabelPrintingPresets> getAllLabelPrintingPresets(Integer programId)
			throws LabelPrintingException {
		try {
			List<LabelPrintingPresets> allLabelPrintingPresets = new ArrayList<LabelPrintingPresets>();

			// 1. get the crop name of the particular programId,
			final Project project = workbenchService.getProjectById(programId.longValue());
			final String cropName = project.getCropType().getCropName();
            final Integer fieldbookToolId = workbenchService.getFieldbookWebTool().getToolId().intValue();

            // 2. retrieve the standard presets
			for (StandardPreset preset : workbenchService.getStandardPresetByCrop(
                    fieldbookToolId,cropName,ToolSection.FBK_LABEL_PRINTING.name())) {
				allLabelPrintingPresets.add(new LabelPrintingPresets(preset.getStandardPresetId(), preset.getName(),
						LabelPrintingPresets.STANDARD_PRESET));
			}

            // 3. add all program presets for fieldbook
			for (ProgramPreset preset : presetDataManager.getProgramPresetFromProgramAndTool(
					contextUtil.getCurrentProgramUUID(), fieldbookToolId,
					ToolSection.FBK_LABEL_PRINTING.name())) {
				allLabelPrintingPresets.add(new LabelPrintingPresets(preset.getProgramPresetId(), preset.getName(),
						LabelPrintingPresets.PROGRAM_PRESET));
			}

			return allLabelPrintingPresets;

		} catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
			throw new LabelPrintingException("label.printing.cannot.retrieve.presets",
					"database.connectivity.error", e.getMessage());

		}
	}

	@Override
	public String getLabelPrintingPresetConfig(int presetId,int presetType) throws LabelPrintingException {
		try {
			if (LabelPrintingPresets.STANDARD_PRESET == presetType) {
				return workbenchService.getStandardPresetById(presetId).getConfiguration();
			} else {
				return presetDataManager.getProgramPresetById(presetId).getConfiguration();
			}
		} catch (MiddlewareQueryException e) {
            LOG.error(e.getMessage(), e);
			throw new LabelPrintingException("label.printing.cannot.retrieve.presets",
					"database.connectivity.error",e.getMessage());
		} catch (NullPointerException e) {
            LOG.error(e.getMessage(), e);
			throw new LabelPrintingException("label.printing.preset.does.not.exists",
					"label.printing.preset.does.not.exists",e.getMessage());
		}
	}

    @Override
    public void saveOrUpdateLabelPrintingPresetConfig(String settingsName,
            String xmlConfig,
            Integer programId) throws MiddlewareQueryException {
        // check if exists, override if true else add new
        List<LabelPrintingPresets> searchPresetList = this.getAllLabelPrintingPresetsByName(
                settingsName, programId, LabelPrintingPresets.PROGRAM_PRESET);

        if (!searchPresetList.isEmpty()) {
            // update
            ProgramPreset currentLabelPrintingPreset = this.getLabelPrintingProgramPreset(
                    searchPresetList.get(0).getId());
            currentLabelPrintingPreset.setConfiguration(xmlConfig);

            presetDataManager.saveOrUpdateProgramPreset(currentLabelPrintingPreset);
        } else {
            // add new
            ProgramPreset preset = new ProgramPreset();
            preset.setName(settingsName);
            preset.setProgramUuid(contextUtil.getCurrentProgramUUID());
            preset.setToolId(workbenchService.getFieldbookWebTool().getToolId().intValue());
            preset.setToolSection(ToolSection.FBK_LABEL_PRINTING.name());
            preset.setConfiguration(xmlConfig);

            presetDataManager.saveOrUpdateProgramPreset(preset);
        }
    }

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void setFieldbookMiddlewareService(
			org.generationcp.middleware.service.api.FieldbookService fieldbookMiddlewareService) {
		this.fieldbookMiddlewareService = fieldbookMiddlewareService;
	}

	public void setInventoryMiddlewareService(
			InventoryService inventoryMiddlewareService) {
		this.inventoryMiddlewareService = inventoryMiddlewareService;
	}

	public void setContextUtil(ContextUtil contextUtil) {
		this.contextUtil = contextUtil;
	}
}