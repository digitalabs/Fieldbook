
package com.efficio.fieldbook.service.internal.impl;

import java.io.IOException;
import java.util.HashMap;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.service.internal.DesignRunner;
import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.trial.bean.BVDesignOutput;
import com.efficio.fieldbook.web.trial.bean.xml.ExpDesign;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;
import com.efficio.fieldbook.web.util.ExpDesignUtil;
import com.efficio.fieldbook.web.util.FieldbookProperties;

@Component("RESTDesignRunner")
public class RESTDesignRunnerImpl implements DesignRunner {
	
	@Autowired
	Environment env;
	
	@Value("${design.runner.url}")
	String bvDesignRestUrl;

	private static final Logger LOG = LoggerFactory.getLogger(RESTDesignRunnerImpl.class);

	private static String CSV_EXTENSION = ".csv";
	private static String BV_PREFIX = "-bv";
	private static String OUTPUT_FILE_PARAMETER_NAME = "outputfile";
	private static String SEED_VALUE = "seed";

	@Override
	public BVDesignOutput runBVDesign(WorkbenchService workbenchService, FieldbookProperties fieldbookProperties, MainDesign design)
			throws IOException, FieldbookException {

		String outputFilePath = System.currentTimeMillis() + RESTDesignRunnerImpl.BV_PREFIX + RESTDesignRunnerImpl.CSV_EXTENSION;
		
		// output file
		design.getDesign().setParameterValue(RESTDesignRunnerImpl.OUTPUT_FILE_PARAMETER_NAME, outputFilePath);
		
		// seed value
		String seedValue = String.valueOf(System.currentTimeMillis());
		if (Long.parseLong(seedValue) > Integer.MAX_VALUE) {
			seedValue = seedValue.substring(seedValue.length() - 9);
		}
		design.getDesign().setParameterValue(SEED_VALUE, seedValue);

		String xml = "";
		try {
			xml = ExpDesignUtil.getXmlStringForSetting(design);
			System.out.println("XML :" + xml);
		} catch (JAXBException e) {
			RESTDesignRunnerImpl.LOG.error(e.getMessage(), e);
		}

		// params should be in the MainDesign instance

		ExpDesign expDesign = design.getDesign();
		RESTDesignRunnerImpl.LOG.info("REST Design for " + expDesign.getName());
				
		//String url = "http://192.168.1.100:19080/experimentdesign/generate/";
		//String bvDesignRestUrl = env.getProperty("design.runner.url");
		if(bvDesignRestUrl == null){
			// FIXME : messaging
			throw new FieldbookException("You do not have a URL sepcified in fieldbook.properties for the Deign Engine");
		}
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_XML);
		HttpEntity<String> entity = new HttpEntity<String>(xml,headers);
		String output2 = restTemplate.postForObject(bvDesignRestUrl, entity, String.class);
		//BVDesignOutput output2 = restTemplate.postForObject(url, entity, BVDesignOutput.class);
		System.out.println("OUT2: " + output2);
		ResponseEntity<BVDesignOutput> output = restTemplate.exchange(bvDesignRestUrl, HttpMethod.POST, entity, BVDesignOutput.class, new HashMap<>());
		System.out.println("OUT3: " + output.getBody());
		for (String header : output.getBody().getBvHeaders()) {
			System.out.print(header + ":");
		}
		System.out.println();
		for (String[] resultList : output.getBody().getBvResultList()) {
			for (int i = 0; i < resultList.length; i++) {
				System.out.print(resultList[i] + ":");				
			}
			System.out.println();
		}
		
		return output.getBody();

	}

}
