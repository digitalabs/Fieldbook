package com.efficio.fieldbook.util;

import java.util.List;

import junit.framework.Assert;

import org.generationcp.middleware.domain.etl.Workbook;
import org.junit.Test;

public class FieldbookUtilTest {
	@Test
	public void testGetColumnOrderListIfThereAreParameters(){
		String columnOrderDelimited = "[\"1100\", \"1900\"]";
		List<Integer> columnOrderList =  FieldbookUtil.getColumnOrderList(columnOrderDelimited);
		Assert.assertEquals("Should have 2 integer list", 2, columnOrderList.size());
	}
	
	@Test
	public void testGetColumnOrderListIfThereAreNoParameters(){
		String columnOrderDelimited = "[ ]";
		List<Integer> columnOrderList =  FieldbookUtil.getColumnOrderList(columnOrderDelimited);
		Assert.assertEquals("Should have 0 integer list", 0, columnOrderList.size());
	}
	
	@Test
	public void testSetColumnOrderingOnWorkbook(){
		Workbook workbook = new Workbook();
		String columnOrderDelimited = "[\"1100\", \"1900\"]";
		FieldbookUtil.setColumnOrderingOnWorkbook(workbook, columnOrderDelimited);
		List<Integer> orderedTermIds = workbook.getColumnOrderedLists();
		Assert.assertEquals("1st element should have term id 1100", 1100, orderedTermIds.get(0).intValue());
		Assert.assertEquals("2nd element should have term id 1900", 1900, orderedTermIds.get(1).intValue());
	}
}
