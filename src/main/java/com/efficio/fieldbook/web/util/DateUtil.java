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

import com.efficio.fieldbook.web.common.bean.ChoiceKeyVal;

// TODO: Auto-generated Javadoc
/**
 * The Class DateUtil.
 */
public class DateUtil {
    
    /** The Constant DATE_FORMAT. */
    private static final String DATE_FORMAT = "yyyyMMdd";
    
    /**
     * Gets the current date.
     *
     * @return the current date
     */
    public static String getCurrentDate(){
        return new SimpleDateFormat(DATE_FORMAT).format(new Date());        
    }
    
    /**
     * Parses the date.
     *
     * @param date the date
     * @return the date
     * @throws ParseException the parse exception
     */
    public static Date parseDate(String date) throws ParseException{
        return new SimpleDateFormat(DATE_FORMAT).parse(date);
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

}
