package com.efficio.fieldbook.util.labelprinting;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;
import com.google.common.collect.Maps;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import com.efficio.fieldbook.service.LabelPrintingServiceImpl;
import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.util.AppConstants;
import com.efficio.fieldbook.web.util.SettingsUtil;

public abstract class BaseLabelGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(BaseLabelGenerator.class);

    /** The message source. */
    @Resource
    protected MessageSource messageSource;

    @Resource
    protected FieldbookService fieldbookService;

    /** The delimiter. */
    protected final String delimiter = " | ";

    public abstract String generateLabels(final List<StudyTrialInstanceInfo> trialInstances, final UserLabelPrinting userLabelPrinting) throws LabelPrintingException;

    public abstract String generateLabelsForGermplasmList(final List<GermplasmListData> germplasmListDataList, final UserLabelPrinting
            userLabelPrinting) throws LabelPrintingException;

    protected Map<String, String> generateAddedInformationField(final FieldMapTrialInstanceInfo fieldMapTrialInstanceInfo,
                                                                final StudyTrialInstanceInfo trialInstance, final String barCode) {
        final Map<String, String> moreFieldInfo = new HashMap<String, String>();
        moreFieldInfo.put("locationName", fieldMapTrialInstanceInfo.getLocationName());
        moreFieldInfo.put("blockName", fieldMapTrialInstanceInfo.getBlockName());
        moreFieldInfo.put("fieldName", fieldMapTrialInstanceInfo.getFieldName());
        moreFieldInfo.put(LabelPrintingServiceImpl.SELECTED_NAME, trialInstance.getFieldbookName());
        moreFieldInfo.put("trialInstanceNumber", fieldMapTrialInstanceInfo.getTrialInstanceNo());
        moreFieldInfo.put(LabelPrintingServiceImpl.BARCODE, barCode);

        return moreFieldInfo;
    }

    /**
     * Returns the value of each field of the label data, based on the column required. 
     *
     * @param moreFieldInfo further information relating to the field
     * @param fieldMapLabel the field map label
     * @param headerID the barcode label
     * @return the value requested
     */
    protected String getValueFromSpecifiedColumn(final Map<String, String> moreFieldInfo, final FieldMapLabel fieldMapLabel, final Integer headerID,
                                   final Map<Integer, String> labelHeaders, final boolean includeHeaderLabel) {
        final StringBuilder buffer = new StringBuilder();

        try {

            final String headerName = this.getColumnHeader(headerID, labelHeaders);

            if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getInt()) {
                buffer.append(fieldMapLabel.getEntryNumber());
            } else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_GID.getInt()) {
                final String gidTemp = fieldMapLabel.getGid() == null ? "" : fieldMapLabel.getGid().toString();
                buffer.append(gidTemp);
            } else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_GERMPLASM_NAME.getInt()) {
                buffer.append(fieldMapLabel.getGermplasmName());
            } else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_YEAR.getInt()) {
                buffer.append(fieldMapLabel.getStartYear());
            } else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_SEASON.getInt()) {
                buffer.append(fieldMapLabel.getSeason());
            } else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME.getInt()) {
                buffer.append(moreFieldInfo.get(LabelPrintingServiceImpl.SELECTED_NAME));
            } else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_NAME.getInt()) {
                buffer.append(moreFieldInfo.get(LabelPrintingServiceImpl.SELECTED_NAME));
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
                buffer.append(moreFieldInfo.get(LabelPrintingServiceImpl.BARCODE));
            } else if (headerID == AppConstants.AVAILABLE_LABEL_SEED_INVENTORY_AMOUNT.getInt()) {
                buffer.append(fieldMapLabel.getInventoryAmount());
            } else if (headerID == AppConstants.AVAILABLE_LABEL_SEED_INVENTORY_SCALE.getInt()) {
                buffer.append(fieldMapLabel.getScaleName());
            } else if (headerID == AppConstants.AVAILABLE_LABEL_SEED_LOT_ID.getInt()) {
                buffer.append(fieldMapLabel.getLotId());
            } else {
                final String value = fieldMapLabel.getUserFields().get(headerID);
                if (value != null) {
                    buffer.append(value);
                }

            }

            String stemp = buffer.toString();
            if (stemp != null && "null".equalsIgnoreCase(stemp)) {
                stemp = " ";
            }

            if (includeHeaderLabel && headerName != null) {
                stemp = headerName + " : " + stemp;
            }

            return stemp;
        } catch (final NumberFormatException e) {
            BaseLabelGenerator.LOG.error(e.getMessage(), e);
            return "";
        }
    }

    /**
     * Gets the column header from the imported data
     *
     * @param headerID the header id
     * @return the header
     */
    protected String getColumnHeader(final Integer headerID, final Map<Integer, String> labelHeaders) {
        final Locale locale = LocaleContextHolder.getLocale();

        final StringBuilder buffer = new StringBuilder();

        try {
            if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_ENTRY_NUM.getInt()) {
                buffer.append(this.messageSource.getMessage("label.printing.available.fields.entry.num", null, locale));
            } else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_GID.getInt()) {
                buffer.append(this.messageSource.getMessage("label.printing.available.fields.gid", null, locale));
            } else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_GERMPLASM_NAME.getInt()) {
                buffer.append(this.messageSource.getMessage("label.printing.available.fields.germplasm.name", null, locale));
            } else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_YEAR.getInt()) {
                buffer.append(
                        this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_YEAR_KEY, null, locale));
            } else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_SEASON.getInt()) {
                buffer.append(
                        this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_SEASON_KEY, null, locale));
            } else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_NURSERY_NAME.getInt()) {
                buffer.append(this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_NURSERY_NAME_KEY, null,
                        locale));
            } else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_NAME.getInt()) {
                buffer.append(this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_TRIAL_NAME_KEY, null,
                        locale));
            } else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_TRIAL_INSTANCE_NUM.getInt()) {
                buffer.append(this.messageSource.getMessage("label.printing.available.fields.trial.instance.num", null, locale));
            } else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_REP.getInt()) {
                buffer.append(this.messageSource.getMessage("label.printing.available.fields.rep", null, locale));
            } else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_LOCATION.getInt()) {
                buffer.append(
                        this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_LOCATION_KEY, null, locale));
            } else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_BLOCK_NAME.getInt()) {
                buffer.append(this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_BLOCK_NAME_KEY, null,
                        locale));
            } else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_PLOT.getInt()) {
                buffer.append(
                        this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_KEY, null, locale));
            } else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_PARENTAGE.getInt()) {
                buffer.append(this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_PARENTAGE_KEY, null,
                        locale));
            } else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES.getInt()) {
                buffer.append(this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_PLOT_COORDINATES_KEY,
                        null, locale));
            } else if (headerID == AppConstants.AVAILABLE_LABEL_FIELDS_FIELD_NAME.getInt()) {
                buffer.append(this.messageSource.getMessage(LabelPrintingServiceImpl.LABEL_PRINTING_AVAILABLE_FIELDS_FIELD_NAME_KEY, null,
                        locale));
            } else if (headerID == AppConstants.AVAILABLE_LABEL_BARCODE.getInt()) {
                buffer.append(this.messageSource.getMessage("label.printing.available.fields.barcode", null, locale));
            } else if (headerID == AppConstants.AVAILABLE_LABEL_SEED_INVENTORY_AMOUNT.getInt()) {
                buffer.append(this.messageSource.getMessage("label.printing.seed.inventory.amount", null, locale));
            } else if (headerID == AppConstants.AVAILABLE_LABEL_SEED_INVENTORY_SCALE.getInt()) {
                buffer.append(this.messageSource.getMessage("label.printing.seed.inventory.scale", null, locale));
            } else if (headerID == AppConstants.AVAILABLE_LABEL_SEED_LOT_ID.getInt()) {
                buffer.append(this.messageSource.getMessage("label.printing.seed.inventory.lotid", null, locale));
            } else {
                String headerName = labelHeaders.get(headerID);
                if (headerName == null) {
                    headerName = "";
                }

                buffer.append(headerName);

            }

            return buffer.toString();
        } catch (final NumberFormatException e) {
            BaseLabelGenerator.LOG.error(e.getMessage(), e);
            return "";
        }
    }


    protected String appendBarcode(final boolean isBarcodeNeeded, final String mainSelectedFields) {
        String processed = mainSelectedFields;
        if (isBarcodeNeeded) {
            processed += "," + AppConstants.AVAILABLE_LABEL_BARCODE.getInt();
        }
        return processed;
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
    protected String generateBarcodeField(final Map<String, String> moreFieldInfo, final FieldMapLabel fieldMapLabel, final String firstField,
                                        final String secondField, final String thirdField, final Map<Integer, String> labelHeaders, final boolean includeLabel) {
        final StringBuilder buffer = new StringBuilder();
        final String fieldList = firstField + "," + secondField + "," + thirdField;

        final List<Integer> selectedFieldIDs = SettingsUtil.parseFieldListAndConvertToListOfIDs(fieldList);

        for (final Integer selectedFieldID : selectedFieldIDs) {
            if (!"".equalsIgnoreCase(buffer.toString())) {
                buffer.append(this.delimiter);
            }

            buffer.append(this.getValueFromSpecifiedColumn(moreFieldInfo, fieldMapLabel, selectedFieldID, labelHeaders, includeLabel));
        }

        return buffer.toString();
    }

    /**
     * This method returns retrieve Label headers to be printed in any of the trial instances having advance lines
     * @param trialInstances
     * @return Map<Integer, String> Map of label header with key as TermId and value as Label
     */
    protected Map<Integer, String> getLabelHeadersFromTrialInstances(final List<StudyTrialInstanceInfo> trialInstances){
        Map<Integer, String> labelHeaders = Maps.newHashMap();

        for(StudyTrialInstanceInfo trialInstanceInfo : trialInstances){
            if(trialInstanceInfo.getTrialInstance() != null && trialInstanceInfo.getTrialInstance().getLabelHeaders() != null
                    && !trialInstanceInfo.getTrialInstance().getLabelHeaders().isEmpty()){
                labelHeaders = trialInstanceInfo.getTrialInstance().getLabelHeaders();
                break;
            }
        }

        return labelHeaders;

    }

}
