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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The Class DateUtil.
 */
public class DateUtil {
    
    private static final String DATE_FORMAT = "yyyyMMdd";
    
    public static String getCurrentDate(){
        return new SimpleDateFormat(DATE_FORMAT).format(new Date());        
    }
    
    public static Date parseDate(String date) throws ParseException{
        return new SimpleDateFormat(DATE_FORMAT).parse(date);
    }

}
