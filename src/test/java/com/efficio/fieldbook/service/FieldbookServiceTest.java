package com.efficio.fieldbook.service;

import javax.annotation.Resource;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.fieldbook.AbstractBaseIntegrationTest;
import com.efficio.fieldbook.service.api.FieldbookService;

public class FieldbookServiceTest extends AbstractBaseIntegrationTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(FieldbookServiceTest.class);
    
    @Resource
    private FieldbookService fieldbookService;
    
    @Test
    public void testAdvanceNursery() throws Exception {
    	
    }
}
