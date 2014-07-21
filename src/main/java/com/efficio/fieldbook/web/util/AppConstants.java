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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.math.NumberUtils;
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
    , SEGMENT_TREATMENT_FACTORS
    , SEGMENT_SELECTION_VARIATES
    , SEGMENT_NURSERY_CONDITIONS
    , SEGMENT_GERMPLASM
    
    , TOOL_NAME_NURSERY_MANAGER_WEB
    , TOOL_NAME_TRIAL_MANAGER_WEB
    , TOOL_NAME_OLD_FIELDBOOK
    , TOOL_NAME_BREEDING_VIEW
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
    ,EXPORT_KSU_EXCEL
    ,EXPORT_KSU_CSV
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
    ,EXPORT_KSU_TRAITS_SUFFIX
    
    ,CREATE_NURSERY_REQUIRED_FIELDS
    ,CREATE_PLOT_REQUIRED_FIELDS
    ,HIDE_NURSERY_FIELDS
    ,HIDE_TRIAL_FIELDS
    ,HIDE_TRIAL_VARIABLE_DBCV_FIELDS
    ,HIDE_PLOT_FIELDS
    ,FILTER_NURSERY_FIELDS
    ,ID_NAME_COMBINATION
    ,ID_CODE_NAME_COMBINATION_STUDY
    ,ID_CODE_NAME_COMBINATION_VARIATE
    ,HIDE_ID_VARIABLES
    ,REMOVE_FACTORS_IN_USE_PREVIOUS_STUDY
    ,CREATE_TRIAL_REQUIRED_FIELDS
    ,HIDE_TRIAL_MANAGEMENT_SETTINGS_FIELDS
    ,CREATE_TRIAL_ENVIRONMENT_REQUIRED_FIELDS
    ,CREATE_TRIAL_PLOT_REQUIRED_FIELDS
    ,CREATE_TRIAL_DEFAULT_PLOT_FIELDS
    ,CREATE_TRIAL_EXP_DESIGN_DEFAULT_FIELDS
    ,CREATE_TRIAL_REMOVE_TREATMENT_FACTOR_IDS
    ,HIDE_TRIAL_ENVIRONMENT_FIELDS
    ,HIDE_TRIAL_ENVIRONMENT_FIELDS_FROM_POPUP
    ,DEFAULT_NO_OF_ENVIRONMENT_COUNT
    
    ,LOCATION_ID
    ,BREEDING_METHOD_ID
    ,BREEDING_METHOD_CODE
    ,START_DATE_ID
    ,END_DATE_ID
    ,STUDY_NAME_ID
    ,COOPERATOR_ID
    
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
    //,TRIAL_ENVIRONMENT_DEFAULT_VARIABLES
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
    ,EXPERIMENTAL_DESIGN_POSSIBLE_VALUES
    
    ,MANNER_IN_TURN
    ,MANNER_PER_LOCATION
    
    ,DESIGN_LAYOUT_SAME_FOR_ALL
    ,DESIGN_LAYOUT_INDIVIDUAL
    
    ,OBJECTIVE_ID
    ,OCC
    
    ,PROGRAM_NURSERIES
    ,PROGRAM_TRIALS
    ,PUBLIC_NURSERIES
    ,PUBLIC_TRIALS
    
    ,FOLDER_ICON_PNG
    ,STUDY_ICON_PNG
    ,BASIC_DETAILS_PNG

    ,NURSERY_BASIC_REQUIRED_FIELDS
    ,Entries_LABEL
    ,HasMeasurements_LABEL
    ,HasFieldMap_LABEL
    ,HIDDEN_FIELDS
    ,SPFLD_ENTRIES
    ,SPFLD_COUNT_VARIATES
    ,SPFLD_HAS_FIELDMAP
    ,SPFLD_PLOT_COUNT
    ,SELECTION_VARIATES_PROPERTIES
    ,FIXED_NURSERY_VARIABLES
    
    ,PROPERTY_BREEDING_METHOD
    ,PROPERTY_PLANTS_SELECTED

    ,START_YEAR
    ,CHAR_LIMIT
    ,PLEASE_CHOOSE
    
    ,BM_CODE
    ,DBID
    ,DBCV
    ,ID_SUFFIX
    
    ,TABLE_HEADER_KEY_SUFFIX
    
    ,TRIAL_BASIC_REQUIRED_FIELDS
    ,EXP_DESIGN_TIME_LIMIT
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
    
    public Map<String, String> getMapOfValues() {
    	String constantValue = getString();
    	Map<String, String> map = new HashMap<String, String>();
    	String[] pairs = constantValue.split(",");
    	if (pairs != null) {
    		for (String pair : pairs) {
    			String[] separated = pair.split("\\|");
    			if (separated != null && separated.length == 2) {
    				map.put(separated[0], separated[1]);
    			}
    		}
    	}
    	return map;
    }
    
    public List<String> getList() {
    	String[] arr = getString().split(",");
    	if (arr != null) {
    		return Arrays.asList(arr);
    	}
    	return new ArrayList<String>();
    }

    public List<Integer> getIntegerList() {
    	List<Integer> list = new ArrayList<Integer>();
    	String[] arr = getString().split(",");
    	if (arr != null) {
    		for (String rec : arr) {
    			if (NumberUtils.isNumber(rec)) {
    				list.add(Integer.valueOf(rec));
    			}
    		}
    	}
    	return list;
    }
}
