
package org.generationcp.commons.util;

import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Resource;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.middleware.util.PoiUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 *
 * Test file
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class PoiUtilTest {

	@Resource
	private Sheet sheet;

	@Resource
	private Workbook workbook_VESA031;

	@Resource
	private Workbook workbook_modifiedTemplateFile;

	private final static int HEADER_ROW = 0;
	private final static int GID_COLUMN = 1;
	private final static int BLANK_DATA_ROW = 37;
	private final static int BLANK_DATA_COLUMN = 12;

	@Test
	public void testEmptyCellNegative() {
		Assert.assertFalse(PoiUtil.isEmpty(this.sheet, PoiUtilTest.HEADER_ROW, PoiUtilTest.GID_COLUMN));
	}

	@Test
	public void testEmptyCellPositive() {
		Assert.assertTrue(PoiUtil.isEmpty(this.sheet, PoiUtilTest.BLANK_DATA_ROW, PoiUtilTest.BLANK_DATA_COLUMN));
	}

	@Test
	public void testLastRowNum() {
		// Using default test sheet

		Integer result = PoiUtil.getLastRowNum(this.sheet);

		Assert.assertNotNull("Using default sheet: Should return a value", result);
		Assert.assertTrue(
				"Using default sheet: Should be greater than 0, I'm assuming that the test sheet is not blank, will fail otherwise",
				result > 0);
		Assert.assertTrue("Using default sheet: Should be less than or equal to what sheet.getLastRowNum() reported",
				result <= this.sheet.getLastRowNum());

		// Using workbook_modifiedTemplateFile
		AtomicReference<Sheet> modifiedTemplateFileObservationSheet =
				new AtomicReference<Sheet>(this.workbook_modifiedTemplateFile.getSheetAt(1)); // get Observation Row
		result = PoiUtil.getLastRowNum(modifiedTemplateFileObservationSheet.get());

		Assert.assertNotNull("Using modifiedTemplateFileObservationSheet: Should return a value", result);
		Assert.assertEquals("Using modifiedTemplateFileObservationSheet: Should equal to 4", Integer.valueOf(4), result);
		Assert.assertTrue("Using modifiedTemplateFileObservationSheet: Should be less than to what sheet.getLastRowNum() reported",
				result < this.sheet.getLastRowNum());

		// Using Vesa Master Sheet
		AtomicReference<Sheet> vesaMasterSheet = new AtomicReference<Sheet>(this.workbook_VESA031.getSheetAt(2)); // get Observation Row
		result = PoiUtil.getLastRowNum(vesaMasterSheet.get());

		Assert.assertNotNull("Using vesaMasterSheet: Should return a value", result);
		Assert.assertEquals("Using vesaMasterSheet: Should equal to 392", Integer.valueOf(392), result);
		Assert.assertTrue("Using vesaMasterSheet: Should be less or equal than to what sheet.getLastRowNum() reported",
				result <= this.sheet.getLastRowNum());

	}

}
