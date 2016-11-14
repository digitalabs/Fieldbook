
package com.efficio.fieldbook.web.naming.expression;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.service.api.KeySequenceRegisterService;
import org.springframework.beans.factory.annotation.Autowired;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public abstract class NumberSequenceExpression extends BaseExpression {

	@Autowired
	protected KeySequenceRegisterService keySequenceRegisterService;

	protected void applyNumberSequence(List<StringBuilder> values, AdvancingSource source) {
		if (source.isForceUniqueNameGeneration()) {
			for (StringBuilder container : values) {
				this.replaceExpressionWithValue(container, "(" + Integer.toString(source.getCurrentMaxSequence() + 1) + ")");

			}

			return;
		}

		if (source.isBulk()) {
			for (StringBuilder container : values) {
				if (source.getPlantsSelected() != null && source.getPlantsSelected() > 1) {
                    Integer newValue = source.getPlantsSelected();
					this.replaceExpressionWithValue(container, newValue != null ? newValue.toString() : "");
				} else {
					this.replaceExpressionWithValue(container, "");
				}
			}
		} else {
			List<StringBuilder> newNames = new ArrayList<StringBuilder>();
			int startCount = 1;

			if (source.getCurrentMaxSequence() > -1) {
				startCount = source.getCurrentMaxSequence() + 1;
			}

			String prefix = source.getBreedingMethod().getPrefix();

			if (prefix == null) {
				prefix = "";
			}

			for (StringBuilder value : values) {
				if (this.getExpressionKey().equals(SequenceExpression.KEY) && source.getPlantsSelected() != null && source.getPlantsSelected
						() > 0) {

					for (int i = startCount; i < startCount + source.getPlantsSelected(); i++) {
						StringBuilder newName = new StringBuilder(value);
						int nextSequence = this.keySequenceRegisterService.incrementAndGetNextSequence(prefix);
                        this.replaceExpressionWithValue(newName, String.valueOf(nextSequence));
						newNames.add(newName);
					}
				} else {
                    this.replaceExpressionWithValue(value, "");
					newNames.add(value);
				}
			}

			values.clear();
			values.addAll(newNames);
		}
	}
}
