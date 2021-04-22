package com.efficio.fieldbook.web.util;

import com.efficio.fieldbook.service.api.FieldbookService;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.ExperimentalDesignVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpDesignUtil {

	private static final Logger LOG = LoggerFactory.getLogger(ExpDesignUtil.class);

	private ExpDesignUtil() {
		// hide implicit public constructor
	}

	public static MeasurementVariable convertStandardVariableToMeasurementVariable(final StandardVariable var, final Operation operation,
			final FieldbookService fieldbookService) {

		final MeasurementVariable mvar =
				new MeasurementVariable(var.getName(), var.getDescription(), var.getScale().getName(), var.getMethod().getName(),
						var.getProperty().getName(), var.getDataType().getName(), null, "");

		mvar.setFactor(true);
		mvar.setOperation(operation);
		mvar.setTermId(var.getId());
		mvar.setDataTypeId(var.getDataType().getId());

		if (var.getPhenotypicType() != null) {
			mvar.setRole(var.getPhenotypicType());
			mvar.setLabel(var.getPhenotypicType().getLabelList().get(0));
		}

		try {
			mvar.setPossibleValues(fieldbookService.getAllPossibleValues(var.getId()));
		} catch (final MiddlewareException e) {
			ExpDesignUtil.LOG.error(e.getMessage(), e);
		}
		return mvar;
	}

	public static int getExperimentalDesignValueFromExperimentalDesignDetails(final ExperimentalDesignVariable experimentalDesignVariable) {
		if (experimentalDesignVariable != null && experimentalDesignVariable.getExperimentalDesign() != null) {
			return experimentalDesignVariable.getExperimentalDesign().getValue() == null ? 0 :
					Integer.parseInt(experimentalDesignVariable.getExperimentalDesign().getValue());
		}
		return 0;
	}

}
