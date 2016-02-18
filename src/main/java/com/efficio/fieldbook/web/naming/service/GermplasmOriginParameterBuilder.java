package com.efficio.fieldbook.web.naming.service;

import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.service.GermplasmOriginGenerationParameters;
import org.generationcp.middleware.domain.etl.Workbook;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public interface GermplasmOriginParameterBuilder {

	GermplasmOriginGenerationParameters build(Workbook workbook, AdvancingSource advancingSource, String selectionNumber);
	
	GermplasmOriginGenerationParameters build(Workbook workbook, ImportedCrosses cross);
}
