package com.efficio.fieldbook.web.nursery.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.service.api.FieldbookService;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSourceList;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.service.NamingConventionService;
import com.efficio.fieldbook.web.util.AppConstants;

@Service
public abstract class AbstractNamingConventionServiceImpl 
implements NamingConventionService {
    
    @Resource
    private FieldbookService fieldbookMiddlewareService;

    @Override
    public List<ImportedGermplasm> advanceNursery(AdvancingNursery info) throws MiddlewareQueryException {
        AdvancingSourceList list = createAdvancingSourceList(info);
        updatePlantsSelectedIfNecessary(list, info);
        return generateGermplasmList(list);
    }

    protected abstract List<ImportedGermplasm> generateGermplasmList(AdvancingSourceList list) throws MiddlewareQueryException;
    
    protected abstract void assignNames(ImportedGermplasm germplasm, AdvancingSource source);

    private AdvancingSourceList createAdvancingSourceList(AdvancingNursery advanceInfo) throws MiddlewareQueryException {
        int nurseryId = advanceInfo.getStudy().getId();
        
        Workbook workbook = fieldbookMiddlewareService.getNurseryDataSet(nurseryId);
        Study nursery = fieldbookMiddlewareService.getStudy(nurseryId);
        
        AdvancingSourceList rows = new AdvancingSourceList(workbook, advanceInfo, nursery);
        
        assignGermplasms(rows);
        
        return rows;
    }
    
    private void updatePlantsSelectedIfNecessary(AdvancingSourceList list, AdvancingNursery info) {
        boolean lineChoiceSame = info.getLineChoice() != null && "1".equals(info.getLineChoice());
        int plantsSelected = 0;
        if (info.getLineSelected() != null && NumberUtils.isNumber(info.getLineSelected())) {
            plantsSelected = Double.valueOf(info.getLineSelected()).intValue(); 
        }
        else {
            lineChoiceSame = false;
        }
        if (list != null && list.getRows() != null && !list.getRows().isEmpty() && lineChoiceSame && plantsSelected > 0) {
            for (AdvancingSource row : list.getRows()) {
                row.setPlantsSelected(plantsSelected);
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
            String newGermplasmName, int breedingMethodId, int index, String nurseryName) 
    throws MiddlewareQueryException {
        
        Method breedingMethod = fieldbookMiddlewareService.getBreedingMethodById(breedingMethodId);
        
        ImportedGermplasm germplasm = new ImportedGermplasm(
                index
              , newGermplasmName
              , null /* gid */
              , source.getGermplasm().getCross()
              , nurseryName + ":" + index
              , getEntryCode(index)
              , null /* check */
              , breedingMethod.getMid());
         
         assignGermplasmAttributes(germplasm, Integer.valueOf(source.getGermplasm().getGid()), 
                 source.getGermplasm().getGnpgs(), 
                 source.getGermplasm().getGpid1(), source.getGermplasm().getGpid2(), 
                 source.getMethodType(), breedingMethod);
         
         assignNames(germplasm, source);
         
         list.add(germplasm);
    }

    private String getEntryCode(int index) {
        return AppConstants.ENTRY_CODE_PREFIX + String.format("%04d", index);
    }
    
    private void assignGermplasms(AdvancingSourceList list) throws MiddlewareQueryException {
        if (list != null && list.getRows() != null && !list.getRows().isEmpty()) {
            for (AdvancingSource source : list.getRows()) {
                if (source.getGermplasm() != null && source.getGermplasm().getGid() != null 
                        && NumberUtils.isNumber(source.getGermplasm().getGid())) {
                    
                    Germplasm germplasm = fieldbookMiddlewareService.getGermplasmByGID(Integer.valueOf(source.getGermplasm().getGid()));
                    source.getGermplasm().setGpid1(germplasm.getGpid1());
                    source.getGermplasm().setGpid2(germplasm.getGpid2());
                    source.getGermplasm().setGnpgs(germplasm.getGnpgs());
                    source.getGermplasm().setBreedingMethodId(germplasm.getMethodId());
                }
            }
        }
    }
}
