
package com.efficio.fieldbook.service.internal;

import java.io.IOException;

import com.efficio.fieldbook.web.trial.bean.bvdesign.BVDesignOutput;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.FieldbookProperties;

public interface DesignRunner {

	public BVDesignOutput runBVDesign(FieldbookProperties fieldbookProperties, MainDesign design)
			throws IOException;

}
