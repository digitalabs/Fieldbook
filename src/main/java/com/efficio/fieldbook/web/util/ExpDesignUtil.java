package com.efficio.fieldbook.web.util;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;

public class ExpDesignUtil {

	private static final Logger LOG = LoggerFactory.getLogger(ExpDesignUtil.class);

	private ExpDesignUtil() {
		// hide implicit public constructor
	}

	public static String getXmlStringForSetting(final MainDesign mainDesign) throws JAXBException {
		final JAXBContext context = JAXBContext.newInstance(MainDesign.class);
		final Marshaller marshaller = context.createMarshaller();
		final StringWriter writer = new StringWriter();
		marshaller.marshal(mainDesign, writer);
		return writer.toString();
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

	public static String cleanBVDesingKey(final String key) {
		if (key != null) {
			return "_" + key.replace("-", "_");
		}
		return key;
	}
}
