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

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.generationcp.middleware.domain.oms.TermId;

import com.efficio.fieldbook.web.common.bean.ChoiceKeyVal;

// TODO: Auto-generated Javadoc
/**
 * The Class DateUtil.
 */
public class DateUtil {
    
    /** The Constant DATE_FORMAT. */
    public static String DB_DATE_FORMAT = "yyyyMMdd";
	public static String UI_DATE_FORMAT = "yyyy-MM-dd";
	public static SimpleDateFormat DB_DATE_FORMATTER = new SimpleDateFormat(DB_DATE_FORMAT);
	public static SimpleDateFormat UI_DATE_FORMATTER = new SimpleDateFormat(UI_DATE_FORMAT);
    
    /**
     * Gets the current date.
     *
     * @return the current date
     */
    public static String getCurrentDate(){
        return new SimpleDateFormat(DB_DATE_FORMAT).format(new Date());        
    }
    
    public static String getCurrentDateInUIFormat(){
        return new SimpleDateFormat(UI_DATE_FORMAT).format(new Date());        
    }
    
    /**
     * Parses the date.
     *
     * @param date the date
     * @return the date
     * @throws ParseException the parse exception
     */
    public static Date parseDate(String date) throws ParseException{
        return new SimpleDateFormat(DB_DATE_FORMAT).parse(date);
    }
    
    /**
     * Generate year choices.
     *
     * @param currentYear the current year
     * @return the list
     */
    public static List<ChoiceKeyVal> generateYearChoices(int currentYear){
    	List<ChoiceKeyVal> yearList = new ArrayList();
    	int startYear = AppConstants.START_YEAR.getInt();
    	for(int i = startYear ; i <= currentYear ; i++){
    		yearList.add(new ChoiceKeyVal(Integer.toString(i), Integer.toString(i)));
    	}
    	return yearList;
    }
    
    /**
     * Generate month choices.
     *
     * @return the list
     */
    public static List<ChoiceKeyVal> generateMonthChoices(){
    	List<ChoiceKeyVal> monthList = new ArrayList();
    	DecimalFormat df2 = new DecimalFormat( "00" );
    	for(double i = 1 ; i <= 12 ; i++){
    		monthList.add(new ChoiceKeyVal(df2.format(i), df2.format(i)));
    	}
    	return monthList;
    }
    
    public static String convertToUIDateFormat(Integer dataTypeId, String val){
		if(dataTypeId != null && dataTypeId == TermId.DATE_VARIABLE.getId() && val != null && !val.equalsIgnoreCase("")) {
			
			try {
				Date dbDate = DB_DATE_FORMATTER.parse(val);
				return UI_DATE_FORMATTER.format(dbDate);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return val;
	}
	public static String convertToDBDateFormat(Integer dataTypeId, String val){
		if(dataTypeId != null && dataTypeId == TermId.DATE_VARIABLE.getId() && val != null && !val.equalsIgnoreCase("")) {
			
			try {
				Date dbDate = UI_DATE_FORMATTER.parse(val);
				return DB_DATE_FORMATTER.format(dbDate);
			} catch (ParseException e) {
			}
		}
		return val;
	}
	
	public static boolean isValidDate(String dateString) {
	    if (dateString == null || dateString.length() != DateUtil.DB_DATE_FORMAT.length()) {
	        return false;
	    }

	    int date;
	    try {
	        date = Integer.parseInt(dateString);
	    } catch (NumberFormatException e) {
	        return false;
	    }

	    int year = date / 10000;
	    int month = (date % 10000) / 100;
	    int day = date % 100;

	    // leap years calculation not valid before 1581
	    boolean yearOk = (year >= 1581);
	    boolean monthOk = (month >= 1) && (month <= 12);
	    boolean dayOk = (day >= 1) && (day <= daysInMonth(year, month));

	    return (yearOk && monthOk && dayOk);
	}

	private static int daysInMonth(int year, int month) {
	    int daysInMonth;
	    switch (month) {
	        case 1: // fall through
	        case 3: // fall through
	        case 5: // fall through
	        case 7: // fall through
	        case 8: // fall through
	        case 10: // fall through
	        case 12:
	            daysInMonth = 31;
	            break;
	        case 2:
	            if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
	                daysInMonth = 29;
	            } else {
	                daysInMonth = 28;
	            }
	            break;
	        default:
	            // returns 30 even for nonexistant months 
	            daysInMonth = 30;
	    }
	    return daysInMonth;
	}

}
