
package com.efficio.etl.web.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */

@RunWith(JUnit4.class)
public class PaginationUtilTest {

	private final static int ROWS_PER_PAGE = 15;

	private final static String SAMPLE_UPDATE_TARGET = "rowModalBody";
	private final static String SAMPLE_CLICK_FUNCTION = "onchangeSelectedRow";
	private final static String SAMPLE_PAGE_FUNCTION = "replaceRowData";

	@Test
	public void testCalculateStartRowFirstPage() {
		int startRow = PaginationUtil.calculateStartRow(1, PaginationUtilTest.ROWS_PER_PAGE);

		Assert.assertEquals(0, startRow);
	}

	@Test
	public void testCalculateStartRowSecondPage() {
		int startRow = PaginationUtil.calculateStartRow(2, PaginationUtilTest.ROWS_PER_PAGE);

		Assert.assertEquals(15, startRow);
	}

	@Test
	public void testCalculateStartRowFourthPage() {
		int startRow = PaginationUtil.calculateStartRow(4, PaginationUtilTest.ROWS_PER_PAGE);

		Assert.assertEquals(45, startRow);
	}

	@Test
	public void testCalculateEndRowFirstPage() {
		int endRow = PaginationUtil.calculateEndRow(1, PaginationUtilTest.ROWS_PER_PAGE);

		Assert.assertEquals(14, endRow);
	}

	@Test
	public void testCalculateEndRowSecondPage() {
		int endRow = PaginationUtil.calculateEndRow(2, PaginationUtilTest.ROWS_PER_PAGE);

		Assert.assertEquals(29, endRow);
	}
}
