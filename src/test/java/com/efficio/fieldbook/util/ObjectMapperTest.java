
package com.efficio.fieldbook.util;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.efficio.fieldbook.web.trial.bean.TrialData;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 7/10/2014 Time: 3:48 PM
 */
@RunWith(value = JUnit4.class)
public class ObjectMapperTest {

	@Test
	public void testMappingOfConsolidatedForm() {
		ObjectMapper mapper = new ObjectMapper();

		try {
			TrialData data =
					mapper.readValue(
							"{\"trialSettings\":{\"userInput\":{}},\"environments\":{\"environments\":[{\"managementDetailValues\":{\"8170\":null,\"8192\":\"2\",\"8194\":\"1\"},\""
									+ "trialDetailValues\":{}},{\"managementDetailValues\":{\"8170\":null,\"8192\":\"4\",\"8194\":\"3\"},\"trialDetailValues\":{}},{\"managementDetailValues\":{\"8170\":null,\"8192\":\""
									+ "6\",\"8194\":\"5\"},\"trialDetailValues\":{}}],\"noOfEnvironments\":\"3\"},\"basicDetails\":{\"basicDetails\":{\"8005\":\"new trial 6\",\"8007\":\"test\",\"8030\":\"\",\"8050\":\""
									+ "07/10/2014\",\"8060\":\"\"},\"folderId\":1,\"folderName\":\"Program Trials\",\"folderNameLabel\":\"Program Trials\",\"userID\":-1,\"userName\":\"Daniel Villafuerte\"}}",
							TrialData.class);

			Assert.assertNotNull(data);
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}

	}
}
