package com.efficio.fieldbook.web.nursery.bean;

import org.junit.Assert;
import org.junit.Test;

public class AdvanceTypeTest {

	@Test
	public void testfromLowerCaseName() {
		Assert.assertEquals(AdvanceType.fromLowerCaseName("sample"), AdvanceType.SAMPLE);
		Assert.assertEquals(AdvanceType.fromLowerCaseName("trial"), AdvanceType.TRIAL);
		Assert.assertEquals(AdvanceType.fromLowerCaseName("nursery"), AdvanceType.NURSERY);
		Assert.assertEquals(AdvanceType.fromLowerCaseName(""), AdvanceType.NONE);
		Assert.assertEquals(AdvanceType.fromLowerCaseName(null), AdvanceType.NONE);
	}
}
