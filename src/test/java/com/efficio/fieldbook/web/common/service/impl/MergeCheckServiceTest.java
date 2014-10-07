package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.web.common.service.MergeCheckService;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;

public class MergeCheckServiceTest extends AbstractBaseIntegrationTest {

	@Resource
	private MergeCheckService mergeCheckService;
	
	@Test
	public void testMergeGermplasmList() throws Exception {
		System.out.println("IN TURN");
		List<ImportedGermplasm> primaryList = createGermplasmList("Primary", 9);
		List<ImportedGermplasm> checkList = createGermplasmList("C", 3);
		int startIndex = 1;
		int interval = 2;
		int manner = 1;
		List<ImportedGermplasm> newList = mergeCheckService.mergeGermplasmList(primaryList, checkList, startIndex, interval, manner, "");
		
		for (ImportedGermplasm newGerm : newList) {
			System.out.println(newGerm.getEntryId() + " " + newGerm.getDesig());
		}
	}
	
	@Test
	public void testMergeGermplasmListPerLocation() throws Exception {
		System.out.println("PER LOCATION");
		List<ImportedGermplasm> primaryList = createGermplasmList("Primary", 9);
		List<ImportedGermplasm> checkList = createGermplasmList("C", 3);
		int startIndex = 1;
		int interval = 2;
		int manner = 2;
		List<ImportedGermplasm> newList = mergeCheckService.mergeGermplasmList(primaryList, checkList, startIndex, interval, manner, "");
		
		for (ImportedGermplasm newGerm : newList) {
			System.out.println(newGerm.getEntryId() + " " + newGerm.getDesig());
		}
	}

	private List<ImportedGermplasm> createGermplasmList(String prefix, int size) {
		List<ImportedGermplasm> list = new ArrayList<ImportedGermplasm>();
		
		for (int i = 0; i < size; i++) {
			ImportedGermplasm germplasm = new ImportedGermplasm(i+1, prefix + (i+1), null);
			list.add(germplasm);
		}
		
		return list;
	}
}
