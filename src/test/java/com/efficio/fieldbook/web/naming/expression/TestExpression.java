
package com.efficio.fieldbook.web.naming.expression;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public class TestExpression {

	public static final Logger LOG = LoggerFactory.getLogger(TestExpression.class);

	public void printResult(List<StringBuilder> values, AdvancingSource source) {
		LOG.debug("DESIG = " + source.getGermplasm().getDesig());
		LOG.debug("RESULTS=");
		for (StringBuilder value : values) {
			LOG.debug("\t" + value);
		}
	}

	public String buildResult(List<StringBuilder> values) {
		String result = "";
		for (StringBuilder value : values) {
			result = result + value;
		}
		return result;
	}

	public AdvancingSource createAdvancingSourceTestData(String name, String separator, String prefix, String count, String suffix,
			boolean isBulking) {

		Method method = new Method();
		method.setSeparator(separator);
		method.setPrefix(prefix);
		method.setCount(count);
		method.setSuffix(suffix);
		if (isBulking) {
			method.setGeneq(TermId.BULKING_BREEDING_METHOD_CLASS.getId());
		} else {
			method.setGeneq(TermId.NON_BULKING_BREEDING_METHOD_CLASS.getId());
		}

		ImportedGermplasm germplasm = new ImportedGermplasm();
		germplasm.setDesig(name);
		List<Name> names = new ArrayList<Name>();
		names.add(new Name(1, 1, 3, 0, 1, name + "_three", 0, 0, 0));
		names.add(new Name(1, 1, 5, 0, 1, name + "_five", 0, 0, 0));
		names.add(new Name(1, 1, 2, 1, 1, name + "_two", 0, 0, 0));

		AdvancingSource source = new AdvancingSource(germplasm, names, 2, method, false, "MNL", "1");
		source.setRootName(name);
		source.setSeason("Dry");
		source.setNurseryName("NurseryTest");
		return source;
	}

	public List<StringBuilder> createInitialValues(AdvancingSource source) {
		List<StringBuilder> builders = new ArrayList<StringBuilder>();

		StringBuilder builder = new StringBuilder();
		builder.append(source.getGermplasm().getDesig()).append(this.getNonNullValue(source.getBreedingMethod().getSeparator()))
		.append(this.getNonNullValue(source.getBreedingMethod().getPrefix()))
		.append(this.getNonNullValue(source.getBreedingMethod().getCount()))
		.append(this.getNonNullValue(source.getBreedingMethod().getSuffix()));
		builders.add(builder);

		return builders;
	}

	public String getNonNullValue(String value) {
		return value != null ? value : "";
	}
}
