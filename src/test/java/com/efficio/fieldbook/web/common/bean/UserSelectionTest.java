package com.efficio.fieldbook.web.common.bean;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class UserSelectionTest {
	@Test
	public void testAddImportedCrossesId(){
		UserSelection userSelection = new UserSelection();
		List<Integer> originalCrossesIds = new ArrayList<Integer>();
		originalCrossesIds.add(1);
		originalCrossesIds.add(2);
		for(Integer crossesId : originalCrossesIds){
			userSelection.addImportedCrossesId(crossesId);
		}
		List<Integer> crossesIds = userSelection.getImportedCrossesId();
		Assert.assertEquals("Should have 2 entries only", originalCrossesIds.size() , crossesIds.size());
	}
}
