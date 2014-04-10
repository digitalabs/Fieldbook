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
package com.efficio.fieldbook.web.util;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum AppConstants {

    // Label Printing
      SIZE_OF_PAPER_A4
    , SIZE_OF_PAPER_LETTER

    , AVAILABLE_LABEL_FIELDS_ENTRY_NUM
    , AVAILABLE_LABEL_FIELDS_GID
    , AVAILABLE_LABEL_FIELDS_GERMPLASM_NAME
    , AVAILABLE_LABEL_FIELDS_YEAR
    , AVAILABLE_LABEL_FIELDS_SEASON
    , AVAILABLE_LABEL_FIELDS_TRIAL_NAME
    , AVAILABLE_LABEL_FIELDS_TRIAL_INSTANCE_NUM
    , AVAILABLE_LABEL_FIELDS_REP
    , AVAILABLE_LABEL_FIELDS_LOCATION
    , AVAILABLE_LABEL_FIELDS_BLOCK_NAME
    , AVAILABLE_LABEL_FIELDS_PLOT
    , AVAILABLE_LABEL_FIELDS_NURSERY_NAME
    , AVAILABLE_LABEL_FIELDS_PARENTAGE
    , AVAILABLE_LABEL_FIELDS_PLOT_COORDINATES
    
    // Field Map   
    , ROW_COLUMN
    , SERPENTINE
    , PLANTING_ORDER_ROW_COLUMN
    , PLANTING_ORDER_SERPENTINE


    // Nursery Manager
    
    , GERMPLASM_LIST_CHOOSE_SPECIFY_CHECK_SELECT_ONE_OR_MORE_IN_THE_LIST
    
    , NAMING_CONVENTION_CIMMYT_WHEAT
    , NAMING_CONVENTION_CIMMYT_MAIZE
    , NAMING_CONVENTION_OTHER_CROPS
    
    , SINGLE_PLANT_SELECTION_SF

    , METHOD_CHOICE_YES
    , METHOD_CHOICE_NO
    , LINE_CHOICE_YES
    , LINE_CHOICE_NO
    
    , SELECTED_BULK_SF
    , RANDOM_BULK_SF
    , RANDOM_BULK_CF
    , HALF_MASS_SELECTION
    , DOUBLE_HAPLOID_LINE
    
    , METHOD_TYPE_GEN
    
    , METHOD_UNKNOWN_DERIVATIVE_METHOD_SF 
    , METHOD_UNKNOWN_GENERATIVE_METHOD_SF 
    
    , ENTRY_CODE_PREFIX
    
    , NAME_SEPARATOR
    
    ,GERMPLASM_LIST_TYPE_HARVEST
    
    , SEGMENT_STUDY
    , SEGMENT_PLOT
    , SEGMENT_TRAITS
    , SEGMENT_TRIAL_ENVIRONMENT
    
    , TOOL_NAME_NURSERY_MANAGER_WEB
    , TOOL_NAME_TRIAL_MANAGER_WEB
    , TOOL_NAME_OLD_FIELDBOOK
    , GERMPLASM_LIST_LOCAL
    , GERMPLASM_LIST_CENTRAL
    
    , PRINCIPAL_INVESTIGATOR
    , LOCATION
    , STUDY_NAME
    , STUDY_TITLE
    , OBJECTIVE
        
    // Import Germplasm
    , CONDITION
    , DESCRIPTION
    , PROPERTY
    , SCALE
    , METHOD
    , DATA_TYPE
    , VALUE
    , FACTOR
    , ENTRY
    , DESIGNATION
    , DESIG
    , GID
    , CROSS
    , SOURCE
    , ENTRY_CODE
    , PLOT
    , CHECK
    , TYPE_OF_ENTRY
    , CODE
    , ASSIGNED
    , C
    , CONSTANT
    , VARIATE
    
    ,EXPORT_NURSERY_FIELDLOG_FIELDROID
    ,EXPORT_NURSERY_R
    ,EXPORT_NURSERY_EXCEL
    ,EXPORT_DATAKAPTURE
    ,IMPORT_NURSERY_FIELDLOG_FIELDROID
    ,IMPORT_NURSERY_EXCEL
    ,IMPORT_DATAKAPTURE
    
    ,LIST_DATE
    ,LIST_TYPE
    
    ,FILE_NOT_EXCEL_ERROR
    ,FILE_NOT_CSV_ERROR
    ,FILE_NOT_FOUND_ERROR
    
    ,EXPORT_FIELDLOG_SUFFIX
    ,EXPORT_R_SUFFIX
    ,EXPORT_XLS_SUFFIX
    ,DATAKAPTURE_TRAITS_SUFFIX
    ,EXPORT_CSV_SUFFIX
    
    ,CREATE_NURSERY_REQUIRED_FIELDS
    ,CREATE_PLOT_REQUIRED_FIELDS
    ,HIDE_NURSERY_FIELDS
    ,HIDE_PLOT_FIELDS
    ,ID_NAME_COMBINATION
    ,CREATE_TRIAL_REQUIRED_FIELDS
    ,CREATE_TRIAL_ENVIRONMENT_REQUIRED_FIELDS
    ,CREATE_TRIAL_PLOT_REQUIRED_FIELDS
    ,HIDE_TRIAL_ENVIRONMENT_FIELDS
    
    ,LOCATION_ID
    ,BREEDING_METHOD_ID
    ,START_DATE_ID
    ,END_DATE_ID
    ,STUDY_NAME_ID
    ,COOPERATOR_ID
    
    ,LOCATION_URL
    ,BREEDING_METHOD_URL
    ,IMPORT_GERMPLASM_URL
    
    ,LABEL
    
    ,NUMERIC_DATA_TYPE
    
    ,CROP_WHEAT
    ,CROP_MAIZE
    
    ,MAIZE_BREEDING_METHOD_SELFED_SHELLED
    ,MAIZE_BREEDING_METHOD_SELFED_BULKED
    ,MAIZE_BREEDING_METHOD_SIB_INCREASED
    ,MAIZE_BREEDING_METHOD_COLCHICINIZE
    
    ,ZIP_FILE_SUFFIX
    ,TRIAL_INSTANCE_FACTOR
    ,TRIAL_ENVIRONMENT_DEFAULT_VARIABLES
    ,EXPERIMENTAL_DESIGN_VALUES
    ,REPLICATES_VALUES
    ,BLOCK_SIZE_VALUES
    ,BLOCK_PER_REPLICATE_VALUES
    ,VALUES
    ,BLOCK_PER_REPLICATE
    ,REPLICATES
    ,BLOCK_SIZE
    ,EXPERIMENTAL_DESIGN
    ,TRIAL_ENVIRONMENT_ORDER
    
    ,MANNER_IN_TURN
    ,MANNER_PER_LOCATION
    ;
    
    private static final Logger LOG = LoggerFactory.getLogger(AppConstants.class);
    
    private static final String PROPERTY_FILE = "appconstants.properties";
    
    public int getInt(){
        int appConstant = -1;
        String value = getString().trim();
        if (value != null) {
            appConstant = Integer.valueOf(value);
        }
        return appConstant;
    }


    public boolean isInt(){
        String value = getString().trim();
        if (value != null) {
            try { 
                Integer.valueOf(value);
            } catch (NumberFormatException e){
                return false;
            }
        }
        return true;
    }
    
    public String getString(){
        Properties configFile = new Properties();
        String value = null;
        try {
            configFile.load(AppConstants.class.getClassLoader().getResourceAsStream(PROPERTY_FILE));
            value = configFile.getProperty(this.toString());
        } catch (NumberFormatException e) {
            LOG.error("Value not numeric.", e);
        } catch (IOException e) {
            LOG.error("Error accessing property file: " + PROPERTY_FILE, e);
        }
        return value;
    }
    
    public static String getString(String labelKey){
        Properties configFile = new Properties();
        String value = null;
        try {
            configFile.load(AppConstants.class.getClassLoader().getResourceAsStream(PROPERTY_FILE));
            value = configFile.getProperty(labelKey);
        } catch (NumberFormatException e) {
            LOG.error("Value not numeric.", e);
        } catch (IOException e) {
            LOG.error("Error accessing property file: " + PROPERTY_FILE, e);
        }
        return value;
    }
    
}
