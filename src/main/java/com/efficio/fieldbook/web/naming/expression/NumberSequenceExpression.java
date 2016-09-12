
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
				/*
				 * Query from DB the next available sequence # for cross name when:
				 * 1) process code is [SEQUENCE] (the [NUMBER] process code does not require the count to be consecutive)
				 * 2) and the current max sequence is 0, meaning only for the first entry, to avoid multiple Middleware calls
				 * 3) and # of plants selected > 0 (for crosses it is always set to 1, for advancing it is set to value of NPSEL trait)
				 */
				if (this.getExpressionKey().equals(SequenceExpression.KEY) && source.getCurrentMaxSequence() == 0 && source.getPlantsSelected() != null && source.getPlantsSelected() > 0) {
					// Extract the root name. Sample value of first StringBuilder is <prefix>[SEQUENCE] 
					// so replace process code [SEQUENCE] with blank string
					final StringBuilder value = new StringBuilder(values.get(0));
					this.replaceExpressionWithValue(value, "");
					
					startCount = Integer.parseInt(this.germplasmDataManager.getNextSequenceNumberForCrossName(value.toString()));
					// The current max sequence is set to the last used sequence # before current entry, thus set to (startCount - 1)
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
