package com.efficio.fieldbook.web.trial;

import org.generationcp.middleware.domain.ontology.DataType;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.utils.test.UnitTestDaoIDGenerator;

/**
 * Helper class for Study controller test to build test data
 */
public class TestDataHelper {

	public static Method createMethod() {
		final Method method = new Method();
		method.setId(UnitTestDaoIDGenerator.generateId(Method.class));
		method.setName("Method Name");
		return method;
	}

	public static Property createProperty() {
		final Property property = new Property();
		property.setName("Property Name");
		property.setCropOntologyId("CO:501");
		property.addClass("Class1");
		property.addClass("Class2");

		return property;
	}

	public static Scale createScale() {
		final Scale scale = new Scale();
		scale.setId(UnitTestDaoIDGenerator.generateId(Scale.class));
		scale.setName("Scale Name");
		scale.setDataType(DataType.NUMERIC_VARIABLE);
		scale.setMinValue("5");
		scale.setMaxValue("500");

		return scale;
	}
}
