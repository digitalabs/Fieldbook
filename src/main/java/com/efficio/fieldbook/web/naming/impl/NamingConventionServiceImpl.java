package com.efficio.fieldbook.web.naming.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.util.TimerWatch;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.web.common.bean.AdvanceGermplasmChangeDetail;
import com.efficio.fieldbook.web.common.bean.AdvanceResult;
import com.efficio.fieldbook.web.naming.expression.RootNameExpression;
import com.efficio.fieldbook.web.naming.expression.SequenceExpression;
import com.efficio.fieldbook.web.naming.rules.RuleException;
import com.efficio.fieldbook.web.naming.service.NamingConventionService;
import com.efficio.fieldbook.web.naming.service.ProcessCodeService;
import com.efficio.fieldbook.web.naming.service.RulesService;
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
    private RulesService rulesService;
    
    @Resource
    private GermplasmDataManager germplasmDataManger;
    
    @Resource
    private AdvancingSourceListFactory factory;
    
    @Resource
    private ProcessCodeService processCodeService;

	@Resource
	private ResourceBundleMessageSource messageSource;
	
    @Override
	public AdvanceResult advanceNursery(AdvancingNursery info, Workbook workbook) throws MiddlewareQueryException, RuleException {
		
        Map<Integer, Method> breedingMethodMap = new HashMap<Integer, Method>();
        Map<String, Method> breedingMethodCodeMap = new HashMap<String, Method>();
        List<Method> methodList = fieldbookMiddlewareService.getAllBreedingMethods(false);

        for(Method method: methodList){
        	breedingMethodMap.put(method.getMid(), method);
        	breedingMethodCodeMap.put(method.getMcode(), method);
        }

        AdvancingSourceList list = createAdvancingSourceList(info, workbook, breedingMethodMap, breedingMethodCodeMap);
        updatePlantsSelectedIfNecessary(list, info);
        List<ImportedGermplasm> importedGermplasmList = generateGermplasmList(list, info.isCheckAdvanceLinesUnique());

        List<AdvanceGermplasmChangeDetail> changeDetails = new ArrayList<AdvanceGermplasmChangeDetail>();
        for (AdvancingSource source : list.getRows()) {
        	if (source.getChangeDetail() != null) {
        		changeDetails.add(source.getChangeDetail());
        	}
        }
        
        AdvanceResult result = new AdvanceResult();
        result.setAdvanceList(importedGermplasmList);
        result.setChangeDetails(changeDetails);
        
        return result;
	}

    private AdvancingSourceList createAdvancingSourceList(AdvancingNursery advanceInfo, Workbook workbook, 
    		Map<Integer, Method> breedingMethodMap, Map<String, Method> breedingMethodCodeMap) 
    throws MiddlewareQueryException {
    	
        int nurseryId = advanceInfo.getStudy().getId();
        if(workbook == null){
        	workbook = fieldbookMiddlewareService.getNurseryDataSet(nurseryId);
        }
        Study nursery = advanceInfo.getStudy();
        
        return factory.create(workbook, advanceInfo, nursery, breedingMethodMap, breedingMethodCodeMap);        
    }
    
    private void updatePlantsSelectedIfNecessary(AdvancingSourceList list, AdvancingNursery info) {
        boolean lineChoiceSame = info.getLineChoice() != null && "1".equals(info.getLineChoice());
        boolean allPlotsChoice = info.getAllPlotsChoice() != null && "1".equals(info.getAllPlotsChoice());
        int plantsSelected = 0;
        if (info.getLineSelected() != null && NumberUtils.isNumber(info.getLineSelected())) {
            plantsSelected = Integer.valueOf(info.getLineSelected()); 
        } else {
            lineChoiceSame = false;
        }
        if (list != null && list.getRows() != null && !list.getRows().isEmpty() && (lineChoiceSame && plantsSelected > 0 || allPlotsChoice)) {
            for (AdvancingSource row : list.getRows()) {
            	if (!row.isBulk() && lineChoiceSame) {
            		row.setPlantsSelected(plantsSelected);
            	} else if (row.isBulk() && allPlotsChoice) {
            		//set it to 1, it does not matter since it's bulked
            		row.setPlantsSelected(1); 
            	}
            }
        }
    }
    
    private void assignGermplasmAttributes(ImportedGermplasm germplasm, int sourceGid, int sourceGnpgs, 
            int sourceGpid1, int sourceGpid2, Method sourceMethod, Method breedingMethod) {
        
        if (sourceMethod != null && sourceMethod.getMtype() != null && AppConstants.METHOD_TYPE_GEN.getString().equals(sourceMethod.getMtype()) 
                || sourceGnpgs < 0 && sourceGpid1 == 0 && sourceGpid2 == 0) {
            
            germplasm.setGpid1(sourceGid);
        } else {
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
        name.setTypeId(GermplasmNameType.DERIVATIVE_NAME.getUserDefinedFieldID());
        name.setNval(germplasm.getDesig());
        name.setNstat(1);
        names.add(name);
        
        germplasm.setNames(names);
    }

    public List<ImportedGermplasm> generateGermplasmList(AdvancingSourceList rows, boolean isCheckForDuplicateName) throws MiddlewareQueryException, RuleException {
        List<ImportedGermplasm> list = new ArrayList<ImportedGermplasm>();
        int index = 1;
        TimerWatch timer = new TimerWatch("advance");

        for (AdvancingSource row : rows.getRows()) {
            if (row.getGermplasm() != null && !row.isCheck() && row.getPlantsSelected() != null && row.getBreedingMethod() != null
            		&& row.getPlantsSelected() > 0 && row.getBreedingMethod().isBulkingMethod() != null) {

            	List<String> names;
				try {
					rulesService.setInitObject(row);
					names = rulesService.runRules();
					for (String name : names) {
						addImportedGermplasmToList(list, row, name, row.getBreedingMethod(), index++, row.getNurseryName());					
					}
				} catch (RuleException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// FIXME : needs rewriting to perform, and should be encapsulated into it's own rule
//            	int lastCount = -1;
//            	for (String evaluatedCount : names) {
//            		String name = countPrefix + evaluatedCount + countSuffix;
//            		if(!isCheckForDuplicateName){           			
//                		addImportedGermplasmToList(list, row, name, row.getBreedingMethod(), index++, row.getNurseryName());
//            		}else{
//            			// this is for checking of duplicate name always in the DB, (checking of standardized and un-standardized)
//	            		Integer currentCount = getCount(evaluatedCount);
//	            		if (currentCount != null) {
//	            			if (currentCount <= lastCount) {
//	            				currentCount = lastCount + 1;
//	            				name = countPrefix + currentCount + countSuffix;
//	            			}
//		        			boolean isMatch = false;
//		        			do {
//		            			isMatch = germplasmDataManger.checkIfMatches(name);
//			            		if (isMatch) {
//			            			if (countExpression.equalsIgnoreCase(SequenceExpression.KEY)) {
//			            				currentCount++;
//			            				name = countPrefix + currentCount + countSuffix;
//			            			} else {
//			            				if (row.getChangeDetail() == null) {
//				                    		row.setChangeDetail(new AdvanceGermplasmChangeDetail());
//				                    		// index in java (starts at 0)
//				                			row.getChangeDetail().setIndex(index-1); 
//				                			row.getChangeDetail().setOldAdvanceName(name);
//				                			Locale locale = LocaleContextHolder.getLocale();
//				                			row.getChangeDetail().setQuestionText(messageSource.getMessage("advance.nursery.duplicate.question.text", 
//				                					new String[] {name}, locale));
//			            				}
//			            				name = row.getChangeDetail().getOldAdvanceName() + "(" + currentCount + ")"; 
//			            				currentCount++;
//			            			}
//			            		} else if (row.getChangeDetail() != null) {
//			            			row.getChangeDetail().setNewAdvanceName(name);
//		                			Locale locale = LocaleContextHolder.getLocale();
//		                			row.getChangeDetail().setAddSequenceText(messageSource.getMessage("advance.nursery.duplicate.add.sequence.text", 
//		                					new String[] {row.getChangeDetail().getNewAdvanceName()}, locale));
//			            		}
//		        			} while (isMatch);
//		        			addImportedGermplasmToList(list, row, name, row.getBreedingMethod(), index++, row.getNurseryName());		
//		        			lastCount = currentCount;
//	            		}	            		
//            		} // end else
//            	}
            }
        }
            
        timer.stop();
        return list;
    }
    private String getNonNullValue(String value) {
    	return value != null ? value : "";
    }
    
    // 1. RootNameGeneratorRule
    // FIXME : breedingMethodNameType NOT USED : hard coded 1 in the 'Expression'
    protected String getGermplasmRootName(Integer breedingMethodSnameType, AdvancingSource row) throws MiddlewareQueryException {
    	
    	RootNameExpression expression = new RootNameExpression();
    	List<StringBuilder> builders = new ArrayList<StringBuilder>();
    	builders.add(new StringBuilder());
    	expression.apply(builders, row);
    	String name = builders.get(0).toString();
    	if (name.length() == 0) {
    		throw new MiddlewareQueryException(messageSource.getMessage("error.advancing.nursery.no.root.name.found", 
    				new Object[] {row.getGermplasm().getEntryId()}, LocaleContextHolder.getLocale())); 
    	}
    	return name;
    	
    }
    
    private Integer getCount(String countStr) {
    	if (("").equals(countStr)) {
    		return 1;
    	}
    	String[] countArray = countStr.split("\\D");
    	if (countArray.length > 0) {
	    	String count = countArray[countArray.length-1];
	    	if (count.equals("")) {
	    		return 1;
	    	}
	    	if (NumberUtils.isNumber(count)) {
	    		return Integer.valueOf(count);
	    	}
    	}
   		return null;
    }

	public void setMessageSource(ResourceBundleMessageSource messageSource) {
		this.messageSource = messageSource;
	}
    
    
}
