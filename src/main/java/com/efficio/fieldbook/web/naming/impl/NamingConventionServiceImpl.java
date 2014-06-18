package com.efficio.fieldbook.web.naming.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.FieldbookService;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.naming.service.NamingConventionService;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSourceList;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.util.AppConstants;

@Service
public class NamingConventionServiceImpl implements NamingConventionService {

    @Resource
    private FieldbookService fieldbookMiddlewareService;
    
    @Resource
    private AdvancingSourceListFactory factory;
    
    @Resource
    private ProcessCodeService processCodeService;

    @Override
	public List<ImportedGermplasm> advanceNursery(AdvancingNursery info,
			Workbook workbook) throws MiddlewareQueryException {
		
        Map<Integer, Method> breedingMethodMap = new HashMap<Integer, Method>();
        List<Method> methodList = fieldbookMiddlewareService.getAllBreedingMethods();
        for(Method method: methodList){
        	breedingMethodMap.put(method.getMid(), method);
        }
        AdvancingSourceList list = createAdvancingSourceList(info, workbook, breedingMethodMap);
        updatePlantsSelectedIfNecessary(list, info);
        List<ImportedGermplasm> importedGermplasmList = generateGermplasmList(list);
        return importedGermplasmList;
	}

    private AdvancingSourceList createAdvancingSourceList(AdvancingNursery advanceInfo, Workbook workbook, Map<Integer, Method> breedingMethodMap) 
    throws MiddlewareQueryException {
    	
        int nurseryId = advanceInfo.getStudy().getId();
        if(workbook == null){
        	workbook = fieldbookMiddlewareService.getNurseryDataSet(nurseryId);
        }
        Study nursery = advanceInfo.getStudy();
        
        AdvancingSourceList list = factory.create(workbook, advanceInfo, nursery, breedingMethodMap);
        return list;
    }
    
    private void updatePlantsSelectedIfNecessary(AdvancingSourceList list, AdvancingNursery info) {
        boolean lineChoiceSame = info.getLineChoice() != null && "1".equals(info.getLineChoice());
        boolean allPlotsChoice = info.getAllPlotsChoice() != null && "1".equals(info.getAllPlotsChoice());
        int plantsSelected = 0;
        if (info.getLineSelected() != null && NumberUtils.isNumber(info.getLineSelected())) {
            plantsSelected = Integer.valueOf(info.getLineSelected()); 
        }
        else {
            lineChoiceSame = false;
        }
        if (list != null && list.getRows() != null && !list.getRows().isEmpty() && (lineChoiceSame && plantsSelected > 0 || allPlotsChoice)) {
            for (AdvancingSource row : list.getRows()) {
            	if (!row.isBulk() && lineChoiceSame) {
            		row.setPlantsSelected(plantsSelected);
            	}
            	else if (row.isBulk() && allPlotsChoice) {
            		row.setPlantsSelected(1); //set it to 1, it does not matter since it's bulked
            	}
            }
        }
    }
    
    private void assignGermplasmAttributes(ImportedGermplasm germplasm, int sourceGid, int sourceGnpgs, 
            int sourceGpid1, int sourceGpid2, Method sourceMethod, Method breedingMethod) {
        
        if (sourceMethod.getMtype() != null && AppConstants.METHOD_TYPE_GEN.equals(sourceMethod.getMtype()) 
                || sourceGnpgs < 0 && sourceGpid1 == 0 && sourceGpid2 == 0) {
            
            germplasm.setGpid1(sourceGid);
        }
        else {
            germplasm.setGpid1(sourceGpid1);
        }
        
        germplasm.setGpid2(sourceGid);
        
        if (breedingMethod != null) {
            germplasm.setGnpgs(breedingMethod.getMprgn());
        }
    }

    protected void addImportedGermplasmToList(List<ImportedGermplasm> list, AdvancingSource source, 
            String newGermplasmName, Method breedingMethod, int index, String nurseryName) 
    throws MiddlewareQueryException {

        ImportedGermplasm germplasm = new ImportedGermplasm(
                index
              , newGermplasmName
              , null /* gid */
              , source.getGermplasm().getCross()
              , nurseryName + ":" + source.getGermplasm().getEntryId() //GCP-7652 use the entry number of the originial : index
              , getEntryCode(index)
              , null /* check */
              , breedingMethod.getMid());
        
         
         assignGermplasmAttributes(germplasm, Integer.valueOf(source.getGermplasm().getGid()), 
                 source.getGermplasm().getGnpgs(), 
                 source.getGermplasm().getGpid1(), source.getGermplasm().getGpid2(), 
                 source.getSourceMethod(), breedingMethod);
         
         assignNames(germplasm, source);
         
         list.add(germplasm);
    }

    private String getEntryCode(int index) {
        return AppConstants.ENTRY_CODE_PREFIX.getString() + String.format("%04d", index);
    }
    

    protected void assignNames(ImportedGermplasm germplasm, AdvancingSource source) {
        List<Name> names = new ArrayList<Name>();
        
        Name name = new Name();
        name.setGermplasmId(Integer.valueOf(source.getGermplasm().getGid()));
        name.setTypeId(0);
        if (source.getGermplasm().getBreedingMethodId().equals(
                AppConstants.METHOD_UNKNOWN_DERIVATIVE_METHOD_SF.getInt())) {
            name.setTypeId(GermplasmNameType.DERIVATIVE_NAME.getUserDefinedFieldID());
        }
        else {
            if (source.getGermplasm().getBreedingMethodId().equals(
                    AppConstants.METHOD_UNKNOWN_GENERATIVE_METHOD_SF.getInt())) {
                    
                if (source.getGermplasm().getDesig().contains(AppConstants.NAME_SEPARATOR.getString())) {
                	name.setTypeId(GermplasmNameType.CROSS_NAME.getUserDefinedFieldID());
                }
                else {
                	name.setTypeId(GermplasmNameType.UNNAMED_CROSS.getUserDefinedFieldID());
                }
            }
        }
        name.setNval(germplasm.getDesig());
        name.setNstat(1);
        names.add(name);
        
        germplasm.setNames(names);
    }

    public List<ImportedGermplasm> generateGermplasmList(AdvancingSourceList rows) throws MiddlewareQueryException {
        List<ImportedGermplasm> list = new ArrayList<ImportedGermplasm>();
        for (AdvancingSource row : rows.getRows()) {
            if (row.getGermplasm() != null && !row.isCheck() && row.getPlantsSelected() != null && row.getBreedingMethod() != null) {
            	Method method = row.getBreedingMethod();
            	String germplasmName = getGermplasmRootName(method.getSnametype(), row);
            	String expression = germplasmName + method.getSeparator() + method.getPrefix() + method.getCount() + method.getSuffix();
            	row.setRootName(germplasmName);
            	List<String> names = processCodeService.applyToName(expression, row);
            	int index = 1;
            	for (String name : names) {
            		addImportedGermplasmToList(list, row, name, row.getBreedingMethod(), index++, row.getNurseryName());
            	}
            }
        }
            
        return list;
    }
    
    private String getGermplasmRootName(Integer snametype, AdvancingSource row) {
    	List<Name> names = row.getNames();
    	if (names != null && !names.isEmpty()) {
    		if (snametype != null) {
	    		for (Name name : names) {
	    			if (name.getTypeId() != null && name.getTypeId().equals(snametype)) {
	    				return name.getNval();
	    			}
	    		}
    		}
    		//if no sname type defined or if no name found that matched the snametype
    		for (Name name : names) {
    			if (name.getNstat() != null && name.getNstat().equals(1)) {
    				return name.getNval();
    			}
    		}
    	}
    	
    	return row.getGermplasm().getDesig();
    }
}
