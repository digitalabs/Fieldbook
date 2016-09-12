
package com.efficio.fieldbook.web.naming.expression;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.generationcp.middleware.manager.api.GermplasmDataManager;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;

public abstract class NumberSequenceExpression extends BaseExpression {

	@Resource
	GermplasmDataManager germplasmDataManager;

	protected void applyNumberSequence(final List<StringBuilder> values, final AdvancingSource source) {
		if (source.isForceUniqueNameGeneration()) {
			for (final StringBuilder container : values) {
				this.replaceExpressionWithValue(container, "(" + Integer.toString(source.getCurrentMaxSequence() + 1) + ")");

			}

			return;
		}

		if (source.isBulk()) {
			for (final StringBuilder container : values) {
				if (source.getPlantsSelected() != null && source.getPlantsSelected() > 1) {
					final Integer newValue = source.getPlantsSelected();
					this.replaceExpressionWithValue(container, newValue != null ? newValue.toString() : "");
				} else {
					this.replaceExpressionWithValue(container, "");
				}
			}
		} else {
			final List<StringBuilder> newNames = new ArrayList<StringBuilder>();
			int startCount = 1;

			if (source.getCurrentMaxSequence() > -1) {
				if (source.getPlantsSelected() != null && source.getPlantsSelected() > 0
						&& this.getExpressionKey().equals(SequenceExpression.KEY)) {
				if (this.getExpressionKey().equals(SequenceExpression.KEY) && source.getCurrentMaxSequence() == 0 && source.getPlantsSelected() != null && source.getPlantsSelected() > 0) {
					final StringBuilder value = new StringBuilder(values.get(0));
					this.replaceExpressionWithValue(value, "");
					
					startCount = Integer.parseInt(this.germplasmDataManager.getNextSequenceNumberForCrossName(value.toString()));
					// The Next Sequence Number  is startCount, so the current max sequence is (startCount - 1)
					source.setCurrentMaxSequence(startCount - 1);
				} else {
					startCount = source.getCurrentMaxSequence() + 1;
				}
			}

			for (final StringBuilder value : values) {
				if (source.getPlantsSelected() != null && source.getPlantsSelected() > 0) {

					for (int i = startCount; i < startCount + source.getPlantsSelected(); i++) {
						final StringBuilder newName = new StringBuilder(value);
						this.replaceExpressionWithValue(newName, String.valueOf(i));
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

	public void setGermplasmDataManager(final GermplasmDataManager germplasmDataManager) {
		this.germplasmDataManager = germplasmDataManager;
	}
}
