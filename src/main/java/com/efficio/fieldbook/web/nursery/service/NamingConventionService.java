package com.efficio.fieldbook.web.nursery.service;

import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;

import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;


public interface NamingConventionService {

    List<ImportedGermplasm> advanceNursery(AdvancingNursery info) throws MiddlewareQueryException;
}
