package com.efficio.fieldbook.web.study.germplasm;

import org.generationcp.commons.data.initializer.ImportedGermplasmTestDataInitializer;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class StudyGermplasmTransformerTest {

	private final StudyGermplasmTransformer studyGermplasmTransformer = new StudyGermplasmTransformer();

	@Test
	public void testTransformToStudyGermplasmDto() {

		final ImportedGermplasm importedGermplasm = ImportedGermplasmTestDataInitializer.createImportedGermplasm();

		final List<StudyGermplasmDto> studyGermplasmDtoList =
			this.studyGermplasmTransformer.transformToStudyGermplasmDto(Arrays.asList(importedGermplasm));

		final StudyGermplasmDto result = studyGermplasmDtoList.get(0);

		Assert.assertEquals(result.getDesignation(), importedGermplasm.getDesig());
		Assert.assertEquals(result.getGermplasmId(), Integer.valueOf(importedGermplasm.getGid()));
		Assert.assertEquals(result.getEntryCode(), importedGermplasm.getEntryCode());
		Assert.assertEquals(result.getEntryNumber(), importedGermplasm.getEntryNumber());
		Assert.assertEquals(result.getEntryType(), importedGermplasm.getEntryTypeCategoricalID().toString());
		Assert.assertEquals(result.getSeedSource(), importedGermplasm.getSource());
		Assert.assertEquals(result.getCross(), importedGermplasm.getCross());
		Assert.assertEquals(result.getGroupId(), importedGermplasm.getGroupId());
	}

}
