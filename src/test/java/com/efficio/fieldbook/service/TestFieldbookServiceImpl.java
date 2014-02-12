package com.efficio.fieldbook.service;

import java.util.List;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.dms.Study;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.efficio.fieldbook.service.api.FieldbookService;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/Fieldbook-servlet-test.xml"})
public class TestFieldbookServiceImpl{
    
    private static final Logger LOG = LoggerFactory.getLogger(TestFieldbookServiceImpl.class);
    
    @Resource
    private FieldbookService fieldbookService;
    
    @Test
    public void testAdvanceNursery() throws Exception {
        AdvancingNursery advanceInfo = new AdvancingNursery();
        advanceInfo.setNamingConvention("CIMMYT-WHEAT");
        advanceInfo.setHarvestDate("10102010");
        advanceInfo.setHarvestLocationAbbreviation("SUN");
        advanceInfo.setHarvestLocationId("1");
        advanceInfo.setLineChoice("1");
        advanceInfo.setLineSelected("3");
        advanceInfo.setMethodChoice("1");
        advanceInfo.setSuffixConvention("SUF");
        advanceInfo.setStudy(new Study());
        advanceInfo.getStudy().setId(-157);
        
        List<ImportedGermplasm> glist = fieldbookService.advanceNursery(advanceInfo);
        if (glist != null && !glist.isEmpty()) {
            for (ImportedGermplasm g : glist) {
                LOG.info(g.getGid() + "  " + g.getDesig());
            }
        }
    }
}
