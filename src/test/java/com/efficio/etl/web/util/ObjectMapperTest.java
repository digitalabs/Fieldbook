
package com.efficio.etl.web.util;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Created with IntelliJ IDEA. User: DanielV
 */

@RunWith(value = JUnit4.class)
public class ObjectMapperTest {

	@Test
	public void testMappingOfConsolidatedForm() {
		ObjectMapper mapper = new ObjectMapper();

		/*
		 * Current test method is empty. The purpose of this test is to provide developers a platform / way of verifying whether the JSON
		 * data sent by the client can be correctly parsed by the server. This provides better description on what exactly went wrong in the
		 * mapping
		 */

	}
}
