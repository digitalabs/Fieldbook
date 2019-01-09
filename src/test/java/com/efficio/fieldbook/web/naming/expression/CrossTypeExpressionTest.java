package com.efficio.fieldbook.web.naming.expression;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.efficio.fieldbook.web.trial.bean.AdvancingSource;
import junit.framework.Assert;

@RunWith(MockitoJUnitRunner.class)
public class CrossTypeExpressionTest {

	public static final String CRSTYP = "[CRSTYP]";

	@Mock
	private ContextUtil contextUtil;
	@Mock
	private GermplasmDataManager germplasmDataManager;

	@InjectMocks
	private CrossTypeExpression crossTypeExpression;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		final Project testProject = new Project();
		testProject.setUniqueID("e8e4be0a-5d63-452f-8fde-b1c794ec7b1a");
		testProject.setCropType(new CropType("maize"));
		Mockito.when(this.contextUtil.getProjectInContext()).thenReturn(testProject);
	}

	@Test
	public void testResolveForNurseryWithSingleCrossBreedingMethod(){
		final AdvancingSource source = new AdvancingSource();
		final Method breedingMethod = this.generateBreedingMethod("Single cross");
		final ImportedGermplasm importedGermplasm = new ImportedGermplasm();
		source.setBreedingMethod(breedingMethod);
		source.setGermplasm(importedGermplasm);

		final List<StringBuilder> values = new ArrayList<>();
		values.add(new StringBuilder(CRSTYP));

		this.crossTypeExpression.apply(values, source, null);

		Assert.assertEquals("S", values.get(0).toString());
	}


	public void testResolveForNurseryWithDoubleCrossBreedingMethod() {
		final AdvancingSource source = new AdvancingSource();
		final Method breedingMethod = this.generateBreedingMethod("Double cross");
		final ImportedGermplasm importedGermplasm = new ImportedGermplasm();
		source.setBreedingMethod(breedingMethod);
		source.setGermplasm(importedGermplasm);

		final List<StringBuilder> values = new ArrayList<>();
		values.add(new StringBuilder(CRSTYP));

		this.crossTypeExpression.apply(values, source, null);

		Assert.assertEquals("D", values.get(0).toString());
	}

	private Method generateBreedingMethod(final String methodName){
		final Method breedingMethod = new Method();
		breedingMethod.setMname(methodName);
		return breedingMethod;
	}

}
