package com.efficio.fieldbook.service;

import java.util.List;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.manager.DatabaseConnectionParameters;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.util.AppConstants;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class TestFieldbookServiceImpl{
    
    private static final Logger LOG = LoggerFactory.getLogger(TestFieldbookServiceImpl.class);
    
    private static ManagerFactory      factory;
    private static StudyDataManager    manager;
    private static OntologyDataManager ontologyManager;

   // @BeforeClass
    public static void setUp() throws Exception {
        DatabaseConnectionParameters local = new DatabaseConnectionParameters("testDatabaseConfig.properties", "local");
        DatabaseConnectionParameters central = new DatabaseConnectionParameters("testDatabaseConfig.properties",
                "central");
        factory = new ManagerFactory(local, central);
        manager = factory.getNewStudyDataManager();
        ontologyManager = factory.getNewOntologyDataManager();
    }

    @Resource
    private FieldbookService fieldbookService;
    
    @Test
    public void testAdvanceNursery() throws Exception {
    	/* need to replace this with a valid nursery fit for advancing
    	 * currently we don't have this data in our central db.
    	 * uncomment to test
    	 */
    	/*
    	int nurseryId = -32;
    	
    	AdvancingNursery advanceInfo = new AdvancingNursery();
        advanceInfo.setNamingConvention(AppConstants.NAMING_CONVENTION_CIMMYT_WHEAT.toString());
        advanceInfo.setHarvestDate("10102010");
        advanceInfo.setHarvestLocationAbbreviation("SUN");
        advanceInfo.setHarvestLocationId("1");
        advanceInfo.setLineChoice("1");
        advanceInfo.setLineSelected("3");
        advanceInfo.setMethodChoice("1");
        advanceInfo.setSuffixConvention("SUF");
        advanceInfo.setStudy(new Study());
        advanceInfo.getStudy().setId(nurseryId);
        advanceInfo.setBreedingMethodId("206");
        
        List<ImportedGermplasm> glist = fieldbookService.advanceNursery(advanceInfo);
        if (glist != null && !glist.isEmpty()) {
            for (ImportedGermplasm g : glist) {
                LOG.info(g.getGid() + "  " + g.getDesig());
                System.out.println(g.getGid() + "  " + g.getDesig());
            }
        }*/
    }
}
