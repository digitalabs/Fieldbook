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
import org.generationcp.middleware.pojos.Germplasm;
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
        List<ImportedGermplasm> importedGermplasmList = generateGermplasmList(list, breedingMethodMap);
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
        assignGermplasms(list);
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
            int sourceGpid1, int sourceGpid2, String sourceMethodType, Method breedingMethod) {
        
        if (sourceMethodType != null && AppConstants.METHOD_TYPE_GEN.equals(sourceMethodType) 
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
            String newGermplasmName, int breedingMethodId, int index, String nurseryName, Map<Integer, Method> breedingMethodMap) 
    throws MiddlewareQueryException {
        
        Method breedingMethod = null; //fieldbookMiddlewareService.getBreedingMethodById(breedingMethodId);
        
        if(breedingMethodMap.get(breedingMethodId) != null){
        	breedingMethod = breedingMethodMap.get(breedingMethodId);
        }else{
        	breedingMethod = fieldbookMiddlewareService.getBreedingMethodById(breedingMethodId);
        	breedingMethodMap.put(breedingMethodId, breedingMethod);
        }

        ImportedGermplasm germplasm = new ImportedGermplasm(
                index
              , newGermplasmName
              , null /* gid */
              , source.getGermplasm().getCross()
              , nurseryName + ":" + source.getGermplasm().getEntryId() //GCP-7652 use the entry number of the originial : index
              , getEntryCode(index)
              , null /* check */
              , breedingMethod.getMid());
        
         
//         assignGermplasmAttributes(germplasm, Integer.valueOf(source.getGermplasm().getGid()), 
//                 source.getGermplasm().getGnpgs(), 
//                 source.getGermplasm().getGpid1(), source.getGermplasm().getGpid2(), 
//                 source.getMethodType(), breedingMethod);
         
         assignNames(germplasm, source);
         
         list.add(germplasm);
    }

    private String getEntryCode(int index) {
        return AppConstants.ENTRY_CODE_PREFIX.getString() + String.format("%04d", index);
    }
    
    private void assignGermplasms(AdvancingSourceList list) throws MiddlewareQueryException {
    	List<Integer> gidList = new ArrayList<Integer>();
    	
        if (list != null && list.getRows() != null && !list.getRows().isEmpty()) {
            for (AdvancingSource source : list.getRows()) {
                if (source.getGermplasm() != null && source.getGermplasm().getGid() != null 
                        && NumberUtils.isNumber(source.getGermplasm().getGid())) {
                	
                	gidList.add(Integer.valueOf(source.getGermplasm().getGid()));
                }
            }
            List<Germplasm> germplasmList = fieldbookMiddlewareService.getGermplasms(gidList);
            Map<String, Germplasm> germplasmMap = new HashMap<String, Germplasm>();
            for(Germplasm germplasm : germplasmList){
            	germplasmMap.put(germplasm.getGid().toString(), germplasm);
            }
            for (AdvancingSource source : list.getRows()) {
                if (source.getGermplasm() != null && source.getGermplasm().getGid() != null 
                        && NumberUtils.isNumber(source.getGermplasm().getGid())) {                	
                    Germplasm germplasm = germplasmMap.get(source.getGermplasm().getGid().toString());
                    source.getGermplasm().setGpid1(germplasm.getGpid1());
                    source.getGermplasm().setGpid2(germplasm.getGpid2());
                    source.getGermplasm().setGnpgs(germplasm.getGnpgs());
                    source.getGermplasm().setBreedingMethodId(germplasm.getMethodId());
                }
            }
            
        }
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

    public List<ImportedGermplasm> generateGermplasmList(AdvancingSourceList rows, Map<Integer, Method> breedingMethodMap) throws MiddlewareQueryException {
        List<ImportedGermplasm> list = new ArrayList<ImportedGermplasm>();
        String newGermplasmName;
        int index = 1;
        for (AdvancingSource row : rows.getRows()) {
            if (row.getGermplasm() != null && !row.isCheck() && row.getPlantsSelected() != null && row.getBreedingMethod() != null) {
            	Method method = row.getBreedingMethod();
            	
            }
        }
            
        return list;
    }
}
