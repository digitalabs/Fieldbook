package com.efficio.fieldbook.web.util;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte

 */
public class PaginationUtil {
    public static int calculateStartRow(int pageNumber, int rowsPerPage) {
        return (pageNumber - 1) * rowsPerPage;
    }

    public static int calculateEndRow(int pageNumber, int rowsPerPage) {
        return (pageNumber * rowsPerPage) - 1;
    }

    public static String calculatePageFunction(String pageFunction, String updateTarget, String clickFunction, int startRow, int endRow) {
        String function = "javascript:" + pageFunction;
        function += "('" + updateTarget + "', '" + clickFunction + "', " + startRow + "," + endRow + ")";

        return function;
    }
}
