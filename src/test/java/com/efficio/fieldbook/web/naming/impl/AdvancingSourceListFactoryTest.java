
package com.efficio.fieldbook.web.naming.impl;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.naming.expression.dataprocessor.ExpressionDataProcessor;
import com.efficio.fieldbook.web.naming.expression.dataprocessor.ExpressionDataProcessorFactory;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSource;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSourceList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import junit.framework.Assert;

import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.List;
import java.util.Map;

public class AdvancingSourceListFactoryTest {

	private static final String REPLICATION_NUMBER = "replicationNumber2";

	private static final String ENV_NUMBER = "1";

	@Mock
	ContextUtil contextUtil;

	@Mock
	OntologyVariableDataManager ontologyVariableDataManager;

    @Mock
    private FieldbookService fieldbookMiddlewareService;

    @Mock
    private ResourceBundleMessageSource messageSource;

    @Mock
    private ExpressionDataProcessorFactory dataProcessorFactory;

	@InjectMocks
	AdvancingSourceListFactory factory = new AdvancingSourceListFactory();

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@SuppressWarnings("unchecked")
	@Test
    public void testCreateAdvancingSourceListSuccess() throws FieldbookException {
        ExpressionDataProcessor expressionDataProcessor = Mockito.mock(ExpressionDataProcessor.class);
        Mockito.when(dataProcessorFactory.retrieveExecutorProcessor()).thenReturn(expressionDataProcessor);

        Mockito.doNothing().when(expressionDataProcessor).processEnvironmentLevelData(Mockito.isA(AdvancingSource.class),Mockito.isA(Workbook.class),
                Mockito.isA(AdvancingNursery.class),Mockito.isNull(Study.class));
        Map<Integer,List<Name>> mapNames = Maps.newHashMap();
        List<Name> nameList = Lists.newArrayList();
        Name name = new Name();
        name.setNid(32);
        name.setNval("nVal");
        name.setTypeId(23);
        name.setNstat(11);

        nameList.add(name);
        mapNames.put(13, nameList);

        Mockito.when(this.fieldbookMiddlewareService.getNamesByGids(Mockito.isA(List.class))).thenReturn(mapNames);

        List<Germplasm> germplasmList = Lists.newArrayList();
        Germplasm germplasm = new Germplasm();
        germplasm.setGid(13);
        germplasm.setGpid1(401);
        germplasm.setGpid2(402);
        germplasm.setGnpgs(403);
        germplasm.setMethodId(13);

        germplasmList.add(germplasm);
        Mockito.when(this.fieldbookMiddlewareService.getGermplasms(Mockito.isA(List.class))).thenReturn(germplasmList);

        Workbook workBook = new Workbook();
        StudyDetails studyDetails = new StudyDetails();
        studyDetails.setStudyType(StudyType.N);
        workBook.setStudyDetails(studyDetails);
        workBook.setObservations(generateMeasurementRows());

        AdvancingNursery advanceInfo = new AdvancingNursery();
        advanceInfo.setMethodVariateId(8262);
        advanceInfo.setLineVariateId(2);
        advanceInfo.setPlotVariateId(3);

        Study study = null;

        Map<Integer, Method > breedingMethodMap = Maps.newConcurrentMap();
        Method bulkMethod = new Method();
        bulkMethod.setGeneq(1490);
        bulkMethod.setCount("1");
        bulkMethod.setIsnew(true);
        bulkMethod.setMcode("mCode");
        bulkMethod.setMdesc("mDEsc");
        bulkMethod.setMgrp("mGrp");
        bulkMethod.setMid(30);
        bulkMethod.setMname("Breeding Method");
        bulkMethod.setPrefix("prefix");

        breedingMethodMap.put(13,bulkMethod);
        Map<String, Method > breedingMethodCodeMap = Maps.newConcurrentMap();

        advanceInfo.setSelectedTrialInstances(Sets.newHashSet(ENV_NUMBER));
        advanceInfo.setSelectedReplications(Sets.newHashSet(REPLICATION_NUMBER));
        
        AdvancingSourceList advancingSourceList = factory.createAdvancingSourceList(workBook, advanceInfo, study, breedingMethodMap, breedingMethodCodeMap);

        Assert.assertEquals("Expected number of advancing source rows were not generated.", 1, advancingSourceList.getRows().size());
        AdvancingSource source = advancingSourceList.getRows().get(0);

        Assert.assertNotNull(source);
        Assert.assertEquals("13",source.getGermplasm().getGid());
        Assert.assertEquals(13,source.getGermplasm().getBreedingMethodId().intValue());
        Assert.assertEquals(401,source.getGermplasm().getGpid1().intValue());
        Assert.assertEquals(402,source.getGermplasm().getGpid2().intValue());
        Assert.assertEquals(403,source.getGermplasm().getGnpgs().intValue());
        Assert.assertEquals(32,source.getNames().get(0).getNid().intValue());
        Assert.assertEquals("nVal",source.getNames().get(0).getNval());
        Assert.assertEquals(11,source.getNames().get(0).getNstat().intValue());
        Assert.assertEquals(15,source.getPlantsSelected().intValue());

        Assert.assertEquals(1490,source.getBreedingMethod().getGeneq().intValue());
        Assert.assertEquals("1",source.getBreedingMethod().getCount());
        Assert.assertTrue(source.getBreedingMethod().getIsnew());
        Assert.assertEquals("mCode",source.getBreedingMethod().getMcode());
        Assert.assertEquals("mDEsc",source.getBreedingMethod().getMdesc());
        Assert.assertEquals("mGrp",source.getBreedingMethod().getMgrp());
        Assert.assertEquals(30,source.getBreedingMethod().getMid().intValue());
        Assert.assertEquals("Breeding Method",source.getBreedingMethod().getMname());
        Assert.assertEquals("prefix",source.getBreedingMethod().getPrefix());
        
        Assert.assertEquals(ENV_NUMBER,source.getTrialInstanceNumber());
        Assert.assertEquals(REPLICATION_NUMBER,source.getReplicationNumber());

        Assert.assertTrue(source.isCheck());
        Assert.assertEquals("plotNumber14",source.getPlotNumber());

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateAdvancingSourceListSuccessWithSelectionVariateMethod() throws FieldbookException {
        ExpressionDataProcessor expressionDataProcessor = Mockito.mock(ExpressionDataProcessor.class);
        Mockito.when(dataProcessorFactory.retrieveExecutorProcessor()).thenReturn(expressionDataProcessor);

        Mockito.doNothing().when(expressionDataProcessor).processEnvironmentLevelData(Mockito.isA(AdvancingSource.class),Mockito.isA(Workbook.class),
                Mockito.isA(AdvancingNursery.class),Mockito.isNull(Study.class));
        Map<Integer,List<Name>> mapNames = Maps.newHashMap();
        List<Name> nameList = Lists.newArrayList();
        Name name = new Name();
        name.setNid(32);
        name.setNval("nVal");
        name.setTypeId(23);
        name.setNstat(11);

        nameList.add(name);
        mapNames.put(13, nameList);

        Mockito.when(this.fieldbookMiddlewareService.getNamesByGids(Mockito.isA(List.class))).thenReturn(mapNames);

        List<Germplasm> germplasmList = Lists.newArrayList();
        Germplasm germplasm = new Germplasm();
        germplasm.setGid(13);
        germplasm.setGpid1(401);
        germplasm.setGpid2(402);
        germplasm.setGnpgs(403);
        germplasm.setMethodId(14);

        germplasmList.add(germplasm);
        Mockito.when(this.fieldbookMiddlewareService.getGermplasms(Mockito.isA(List.class))).thenReturn(germplasmList);

        Workbook workBook = new Workbook();
        StudyDetails studyDetails = new StudyDetails();
        studyDetails.setStudyType(StudyType.N);
        workBook.setStudyDetails(studyDetails);

        List<MeasurementRow> measurementRows = generateMeasurementRows();
        MeasurementRow row = new MeasurementRow();
        List<MeasurementData> rowData = Lists.newArrayList();

        MeasurementData measurementData = new MeasurementData();
        measurementData.setValue("DSP");
        MeasurementVariable measurementVariable = new MeasurementVariable();
        measurementVariable.setTermId(8252);
        measurementData.setMeasurementVariable(measurementVariable);
        rowData.add(measurementData);

        row.setDataList(rowData);

        measurementRows.add(row);
        workBook.setObservations(measurementRows);

        AdvancingNursery advanceInfo = new AdvancingNursery();
        advanceInfo.setMethodVariateId(8252);
        advanceInfo.setLineVariateId(2);
        advanceInfo.setPlotVariateId(3);

        Study study = null;

        Map<Integer, Method > breedingMethodMap = Maps.newConcurrentMap();
        Method variateMethod = new Method();
        variateMethod.setGeneq(1490);
        variateMethod.setCount("1");
        variateMethod.setIsnew(true);
        variateMethod.setMcode("DSP");
        variateMethod.setMdesc("Single Selection");
        variateMethod.setMgrp("methodGroup");
        variateMethod.setMid(30);
        variateMethod.setMname("Single Selection Variate");
        variateMethod.setPrefix("prefix");

        breedingMethodMap.put(30,variateMethod);

        Map<String, Method > breedingMethodCodeMap = Maps.newConcurrentMap();
        breedingMethodCodeMap.put("DSP",variateMethod);

        advanceInfo.setSelectedTrialInstances(Sets.newHashSet(ENV_NUMBER));
        advanceInfo.setSelectedReplications(Sets.newHashSet(REPLICATION_NUMBER));

        AdvancingSourceList advancingSourceList = factory.createAdvancingSourceList(workBook, advanceInfo, study, breedingMethodMap, breedingMethodCodeMap);

        Assert.assertEquals("Expected number of advancing source rows were not generated.", 1, advancingSourceList.getRows().size());
        AdvancingSource source = advancingSourceList.getRows().get(0);

        Assert.assertNotNull(source);


        Assert.assertEquals(1490,source.getBreedingMethod().getGeneq().intValue());
        Assert.assertEquals("1",source.getBreedingMethod().getCount());
        Assert.assertTrue(source.getBreedingMethod().getIsnew());
        Assert.assertEquals("DSP",source.getBreedingMethod().getMcode());
        Assert.assertEquals("Single Selection",source.getBreedingMethod().getMdesc());
        Assert.assertEquals("methodGroup",source.getBreedingMethod().getMgrp());
        Assert.assertEquals(30,source.getBreedingMethod().getMid().intValue());
        Assert.assertEquals("Single Selection Variate",source.getBreedingMethod().getMname());
        Assert.assertEquals("prefix",source.getBreedingMethod().getPrefix());

    }

    private List<MeasurementRow> generateMeasurementRows(){
        List<MeasurementRow> observations = Lists.newArrayList();
        MeasurementRow row1 = new MeasurementRow();

        List<MeasurementData> rowData1 = Lists.newArrayList();

        MeasurementData measurementData11 = new MeasurementData();
        measurementData11.setcValueId("13");
        MeasurementVariable measurementVariable11 = new MeasurementVariable();
        measurementVariable11.setTermId(8262);
        measurementData11.setMeasurementVariable(measurementVariable11);
        rowData1.add(measurementData11);

        MeasurementData measurementData12 = new MeasurementData();
        measurementData12.setcValueId("13");
        MeasurementVariable measurementVariable12 = new MeasurementVariable();
        measurementVariable12.setTermId(8240);
        measurementData12.setMeasurementVariable(measurementVariable12);
        rowData1.add(measurementData12);

        MeasurementData measurementData13 = new MeasurementData();
        measurementData13.setcValueId("13");
        MeasurementVariable measurementVariable13 = new MeasurementVariable();
        measurementVariable13.setTermId(8255);
        List<ValueReference> possibleVaues13 = Lists.newArrayList();
        possibleVaues13.add(new ValueReference(13,"valueReference13"));
        measurementVariable13.setPossibleValues(possibleVaues13);
        measurementData13.setMeasurementVariable(measurementVariable13);
        rowData1.add(measurementData13);

        MeasurementData measurementData14 = new MeasurementData();
        measurementData14.setcValueId("13");
        measurementData14.setValue("plotNumber14");
        MeasurementVariable measurementVariable14 = new MeasurementVariable();
        measurementVariable14.setTermId(8200);
        measurementData14.setMeasurementVariable(measurementVariable14);
        rowData1.add(measurementData14);

        MeasurementData measurementData15 = new MeasurementData();
        measurementData15.setcValueId("15");
        measurementData15.setValue("plotNumber15");
        MeasurementVariable measurementVariable15 = new MeasurementVariable();
        measurementVariable15.setTermId(3);
        measurementData15.setMeasurementVariable(measurementVariable15);
        rowData1.add(measurementData15);
        
        MeasurementData measurementData16 = new MeasurementData();
        measurementData16.setValue(ENV_NUMBER);
        MeasurementVariable measurementVariable16 = new MeasurementVariable();
        measurementVariable16.setTermId(TermId.TRIAL_INSTANCE_FACTOR.getId());
        measurementData16.setMeasurementVariable(measurementVariable16);
        rowData1.add(measurementData16);
        
        MeasurementData measurementData17 = new MeasurementData();
        measurementData17.setValue(REPLICATION_NUMBER);
        MeasurementVariable measurementVariable17 = new MeasurementVariable();
        measurementVariable17.setTermId(TermId.REP_NO.getId());
        measurementData17.setMeasurementVariable(measurementVariable17);
        rowData1.add(measurementData17);

        row1.setDataList(rowData1);

        observations.add(row1);
        return observations;
    }
}
