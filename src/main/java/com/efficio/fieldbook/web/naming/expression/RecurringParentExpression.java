package com.efficio.fieldbook.web.naming.expression;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.generationcp.middleware.manager.api.PedigreeDataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RecurringParentExpression extends BaseExpression {

	public static final String KEY = "[RCRPRNT]";
	static final String MALE_RECURRENT_SUFFIX = "M";
	static final String FEMALE_RECURRENT_SUFFIX = "F";

	@Autowired
	private PedigreeDataManager pedigreeDataManager;

	@Override
	public void apply(final List<StringBuilder> values, final AdvancingSource source) {

		String output = "";

		final int computation = pedigreeDataManager.calculateRecurrentParent(source.getMaleGid(), source.getFemaleGid());

		if (PedigreeDataManager.FEMALE_RECURRENT == computation) {
			output += FEMALE_RECURRENT_SUFFIX;
		} else if (PedigreeDataManager.MALE_RECURRENT == computation) {
			output += MALE_RECURRENT_SUFFIX;
		}

		for (StringBuilder value : values) {

			this.replaceExpressionWithValue(value, output);

		}

	}

	@Override
	public String getExpressionKey() {
		return RecurringParentExpression.KEY;
	}
}
