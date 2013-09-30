package org.generationcp.commons.util;

import static org.junit.Assert.*;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 *
 * Test file
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration

public class PoiUtilTest {
    @Resource
    private Sheet sheet;


    private final static int HEADER_ROW = 0;
    private final static int GID_COLUMN = 1;
    private final static int BLANK_DATA_ROW = 37;
    private final static int BLANK_DATA_COLUMN = 12;


    @Test
    public void testEmptyCellNegative() {
        assertFalse(PoiUtil.isEmpty(sheet, HEADER_ROW, GID_COLUMN));
    }

    @Test
    public void testEmptyCellPositive() {
        assertTrue(PoiUtil.isEmpty(sheet, BLANK_DATA_ROW, BLANK_DATA_COLUMN));
    }
}
