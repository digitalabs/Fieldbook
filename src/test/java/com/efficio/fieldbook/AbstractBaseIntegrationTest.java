
package com.efficio.fieldbook;

import java.util.UUID;

import org.generationcp.commons.spring.util.ContextUtil;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:Fieldbook-servlet-test.xml"})
@WebAppConfiguration
public abstract class AbstractBaseIntegrationTest {

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private ContextUtil contextUtil;

	protected MockMvc mockMvc;

	protected final String PROGRAM_UUID = UUID.randomUUID().toString();

	@Before
	public void setUp() throws Exception {
		this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();

		this.contextUtil = Mockito.mock(ContextUtil.class);
		Mockito.when(this.contextUtil.getCurrentProgramUUID()).thenReturn(this.PROGRAM_UUID);

	}
}
