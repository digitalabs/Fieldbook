package com.efficio.fieldbook.web.naming.expression;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;

public class TestExpression {

	public void printResult(List<StringBuilder> values, AdvancingSource source) {
		System.out.println("DESIG = " + source.getGermplasm().getDesig());
		System.out.println("RESULTS=");
		for (StringBuilder value : values) {
			System.out.println("\t" + value);
		}
		System.out.println();
	}
	
	public AdvancingSource createAdvancingSourceTestData(String name, String separator,
			String prefix, String count, String suffix, boolean isBulking) {
		
		Method method = new Method();
		method.setSeparator(separator);
		method.setPrefix(prefix);
		method.setCount(count);
		method.setSuffix(suffix);
		if (isBulking) {
			method.setGeneq(TermId.BULKING_BREEDING_METHOD_CLASS.getId());
		}
		else {
			method.setGeneq(TermId.NON_BULKING_BREEDING_METHOD_CLASS.getId());
		}
		
		ImportedGermplasm germplasm = new ImportedGermplasm();
		germplasm.setDesig(name);
		List<Name> names = new ArrayList<Name>();
		names.add(new Name(1, 1, 3, 0, 1, name + "_three", 0, 0, 0));
		names.add(new Name(1, 1, 5, 0, 1, name + "_five", 0, 0, 0));
		names.add(new Name(1, 1, 2, 1, 1, name + "_two", 0, 0, 0));
		
		AdvancingSource source = new AdvancingSource(
				germplasm
				, names
				, 2
				, method
				, false
				, "NurseryTest"
				, "Dry"
				, "MNL");
		source.setRootName(name);
		return source;
	}
	
	public List<StringBuilder> createInitialValues(AdvancingSource source) {
		List<StringBuilder> builders = new ArrayList<StringBuilder>();
		
		StringBuilder builder = new StringBuilder();
		builder.append(source.getGermplasm().getDesig())
				.append(getNonNullValue(source.getBreedingMethod().getSeparator()))
				.append(getNonNullValue(source.getBreedingMethod().getPrefix()))
				.append(getNonNullValue(source.getBreedingMethod().getCount()))
				.append(getNonNullValue(source.getBreedingMethod().getSuffix()))
				;
		builders.add(builder);
		
		return builders;
	}
	
	public String getNonNullValue(String value) {
		return value != null ? value : "";
	}
}
