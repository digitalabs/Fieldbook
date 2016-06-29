
package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.ListDataProject;

public class ListDataProjectUtil {

	public static List<ListDataProject> createListDataProject(List<ImportedGermplasm> projectGermplasmList) {

		List<ListDataProject> listDataProject = new ArrayList<>();
		if (projectGermplasmList != null && !projectGermplasmList.isEmpty()) {
			// need a copy of the germplasm list and save
			for (ImportedGermplasm aData : projectGermplasmList) {
				ListDataProject listDataProj = new ListDataProject();
				listDataProj.setGermplasmId(Integer.valueOf(aData.getGid()));
				if (aData.getEntryTypeCategoricalID() != null) {
					listDataProj.setCheckType(aData.getEntryTypeCategoricalID());
				} else {
					listDataProj.setCheckType(0);
				}
				listDataProj.setDesignation(aData.getDesig());
				listDataProj.setEntryId(aData.getEntryId());
				listDataProj.setEntryCode(aData.getEntryCode());
				listDataProj.setSeedSource(aData.getSource());
				listDataProj.setGroupName(aData.getGroupName());

				listDataProject.add(listDataProj);
			}
		}
		return listDataProject;

	}

	public static List<ListDataProject> createListDataProjectFromGermplasmListData(List<GermplasmListData> germplasmListDatas) {

		List<ListDataProject> listDataProject = new ArrayList<>();
		if (germplasmListDatas != null && !germplasmListDatas.isEmpty()) {
			// need a copy of the germplasm list and save
			for (GermplasmListData aData : germplasmListDatas) {
				ListDataProject listDataProj = new ListDataProject();
				listDataProj.setGermplasmId(Integer.valueOf(aData.getGid()));

				listDataProj.setDesignation(aData.getDesignation());
				listDataProj.setEntryId(aData.getEntryId());
				listDataProj.setEntryCode(aData.getEntryCode());
				listDataProj.setSeedSource(aData.getSeedSource());
				listDataProj.setGroupName(aData.getGroupName());

				listDataProject.add(listDataProj);
			}
		}
		return listDataProject;

	}

	/**
	 * Transform germplasm list data to imported germplasm.
	 *
	 * @param data the data
	 * @return the list
	 */
	public static List<ImportedGermplasm> transformListDataProjectToImportedGermplasm(List<ListDataProject> data) {
		List<ImportedGermplasm> list = new ArrayList<>();
		int index = 1;
		if (data != null && !data.isEmpty()) {
			for (ListDataProject aData : data) {
				ImportedGermplasm germplasm = new ImportedGermplasm();
				germplasm.setEntryTypeValue(aData.getCheckType().toString());
				germplasm.setEntryTypeCategoricalID(aData.getCheckType());
				germplasm.setCross(aData.getGroupName());
				germplasm.setDesig(aData.getDesignation());
				germplasm.setEntryCode(aData.getEntryCode());
				germplasm.setEntryId(aData.getEntryId());
				germplasm.setGid(aData.getGermplasmId().toString());
				germplasm.setMgid(aData.getGroupId());
				germplasm.setSource(aData.getSeedSource());
				germplasm.setGroupName(aData.getGroupName());
				germplasm.setIndex(index++);

				list.add(germplasm);
			}
		}
		return list;
	}
}
