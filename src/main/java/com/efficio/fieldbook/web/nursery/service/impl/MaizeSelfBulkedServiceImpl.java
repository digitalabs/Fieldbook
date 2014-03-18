package com.efficio.fieldbook.web.nursery.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.service.api.FieldbookService;

import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSourceList;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.service.MaizeSelfBulkedService;

public class MaizeSelfBulkedServiceImpl extends
		AbstractNamingConventionServiceImpl implements MaizeSelfBulkedService {

	@Resource
	private FieldbookService fieldbookMiddlewareService;
	
	@Override
	protected List<ImportedGermplasm> generateGermplasmList(AdvancingSourceList rows) throws MiddlewareQueryException {
        List<ImportedGermplasm> list = new ArrayList<ImportedGermplasm>();
        String newGermplasmName = null;
        Integer breedingMethodId = null;
        boolean putBrackets = (rows.getPutBrackets() != null ? true : false);
        int index = 1;
        Map<String, Method> breedingMethodMap = new HashMap<String, Method>();
        List<Method> methodList = fieldbookMiddlewareService.getAllBreedingMethods();
        for(Method method: methodList){
        	breedingMethodMap.put(method.getMid().toString(), method);
        }
        for (AdvancingSource row : rows.getRows()) {
            if (row.getGermplasm() != null && !row.isCheck() && row.getPlantsSelected() != null && row.getPlantsSelected() > 0) {
        		breedingMethodId = 206;
        		if (putBrackets) {
        			newGermplasmName = "(" + row.getGermplasm().getDesig() + ")" + "-B";
        		}
        		else {
        			newGermplasmName = row.getGermplasm().getDesig() + "-B";
        		}
        		addImportedGermplasmToList(list, row, newGermplasmName, breedingMethodId, index++, rows.getNurseryName(), breedingMethodMap);
            }
        }
            
        return list;
	}

}
