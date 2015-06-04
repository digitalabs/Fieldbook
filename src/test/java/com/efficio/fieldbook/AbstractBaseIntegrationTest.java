
package com.efficio.fieldbook;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:Fieldbook-servlet-test.xml"})
public abstract class AbstractBaseIntegrationTest extends AbstractJUnit4SpringContextTests {

}
