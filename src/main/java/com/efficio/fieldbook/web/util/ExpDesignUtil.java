
package com.efficio.fieldbook.web.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.trial.bean.BVDesignOutput;
import com.efficio.fieldbook.web.trial.bean.xml.ExpDesign;
import com.efficio.fieldbook.web.trial.bean.xml.ExpDesignParameter;
import com.efficio.fieldbook.web.trial.bean.xml.ListItem;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.google.common.base.Optional;

public class ExpDesignUtil {

	private static final Logger LOG = LoggerFactory.getLogger(ExpDesignUtil.class);

	private ExpDesignUtil() {
		// hide implicit public constructor
	}

	public static String getXmlStringForSetting(MainDesign mainDesign) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(MainDesign.class);
		Marshaller marshaller = context.createMarshaller();
		StringWriter writer = new StringWriter();
		marshaller.marshal(mainDesign, writer);
		return writer.toString();
	}

	public static MainDesign readXmlStringForSetting(String xmlString) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(MainDesign.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		return (MainDesign) unmarshaller.unmarshal(new StringReader(xmlString));
	}

	public static MeasurementVariable convertStandardVariableToMeasurementVariable(StandardVariable var, Operation operation,
			FieldbookService fieldbookService) {

		MeasurementVariable mvar =
				new MeasurementVariable(var.getName(), var.getDescription(), var.getScale().getName(), var.getMethod().getName(), var
						.getProperty().getName(), var.getDataType().getName(), null, "");

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
		} catch (MiddlewareException e) {
			ExpDesignUtil.LOG.error(e.getMessage(), e);
		}
		return mvar;
	}

	public static String cleanBVDesingKey(String key) {
		if (key != null) {
			return "_" + key.replace("-", "_");
		}
		return key;
	}
}
