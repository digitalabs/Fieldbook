package com.efficio.fieldbook.web.naming.service;

import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.service.GermplasmOriginGenerationParameters;
import org.generationcp.middleware.domain.etl.Workbook;


public interface GermplasmOriginParameterBuilder {

	GermplasmOriginGenerationParameters build(Workbook workbook, String plotNumber, String plantOrEarNumber);
	
	GermplasmOriginGenerationParameters build(Workbook workbook, ImportedCrosses cross);
}
