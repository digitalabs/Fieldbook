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

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */
public class PaginationUtil {
    
    /**
     * Calculate start row.
     *
     * @param pageNumber the page number
     * @param rowsPerPage the rows per page
     * @return the int
     */
    public static int calculateStartRow(int pageNumber, int rowsPerPage) {
        return (pageNumber - 1) * rowsPerPage;
    }

    /**
     * Calculate end row.
     *
     * @param pageNumber the page number
     * @param rowsPerPage the rows per page
     * @return the int
     */
    public static int calculateEndRow(int pageNumber, int rowsPerPage) {
        return (pageNumber * rowsPerPage) - 1;
    }

    /**
     * Calculate page function.
     *
     * @param pageFunction the page function
     * @param updateTarget the update target
     * @param clickFunction the click function
     * @param startRow the start row
     * @param endRow the end row
     * @return the string
     */
    public static String calculatePageFunction(String pageFunction, String updateTarget
            , String clickFunction, int startRow, int endRow) {
        String function = "javascript:" + pageFunction;
        function += "('" + updateTarget + "', '" + clickFunction + "', " + startRow + "," + endRow + ")";

        return function;
    }
}
