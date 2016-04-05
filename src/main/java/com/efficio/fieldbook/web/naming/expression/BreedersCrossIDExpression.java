package com.efficio.fieldbook.web.naming.expression;

import java.util.List;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import org.generationcp.commons.service.impl.BreedersCrossIDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BreedersCrossIDExpression extends BaseExpression{

	@Autowired
	private BreedersCrossIDGenerator breedersCrossIDGenerator;

	public static final String KEY = "[CIMCRS]";

	public BreedersCrossIDExpression(){
	}

	@Override
	public void apply(List<StringBuilder> values, AdvancingSource source) {
		for (StringBuilder container : values) {
			String newValue = this.breedersCrossIDGenerator.generateBreedersCrossID( source.getStudyType(), source.getConditions(),
					source.getTrailInstanceObservation());
			this.replaceExpressionWithValue(container, newValue);
		}
	}

	@Override
	public String getExpressionKey() {
		return BreedersCrossIDExpression.KEY;
	}
}
