package com.efficio.fieldbook.util;

import java.util.List;

import junit.framework.Assert;

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
}
