
package com.efficio.fieldbook.web.util;

import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.ListDataProject;

import java.util.ArrayList;
import java.util.List;

public class ListDataProjectUtil {

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
				listDataProj.setNotes(aData.getNotes());
				listDataProj.setCrossingDate(aData.getCrossingDate());

				listDataProject.add(listDataProj);
			}
		}
		return listDataProject;

	}

}
