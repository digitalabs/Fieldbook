package com.efficio.fieldbook.web.nursery.controller;

import java.util.Properties;

import com.efficio.fieldbook.service.api.ErrorHandlerService;
import com.efficio.fieldbook.service.api.WorkbenchService;
import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.service.MeasurementsGeneratorService;
import com.efficio.fieldbook.web.nursery.service.ValidationService;
import com.efficio.fieldbook.web.util.FieldbookProperties;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

/**
 * Created by cyrus on 27/10/2015.
 */
@Configuration
@EnableWebMvc
public class TestFieldbookWebContext extends WebMvcConfigurerAdapter {
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/static/**").addResourceLocations("/static/");
	}

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Bean
	public ViewResolver viewResolver() {
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();

		return viewResolver;
	}

	@Bean
	public SimpleMappingExceptionResolver exceptionResolver() {
		SimpleMappingExceptionResolver exceptionResolver = new SimpleMappingExceptionResolver();

		Properties exceptionMappings = new Properties();

		exceptionMappings.put("java.lang.Exception", "error/error");
		exceptionMappings.put("java.lang.RuntimeException", "error/error");

		exceptionResolver.setExceptionMappings(exceptionMappings);

		Properties statusCodes = new Properties();

		statusCodes.put("error/404", "404");
		statusCodes.put("error/error", "500");

		exceptionResolver.setStatusCodes(statusCodes);

		return exceptionResolver;
	}

	@Bean
	public WorkbenchService workbenchService() {
		return Mockito.mock(WorkbenchService.class);
	}

	@Bean
	public ContextUtil contextUtil() {
		return Mockito.mock(ContextUtil.class);
	}

	@Bean
	public WorkbenchDataManager workbenchDataManager() {
		return Mockito.mock(WorkbenchDataManager.class);
	}

	@Bean
	public FieldbookProperties fieldbookProperties() {
		return Mockito.mock(FieldbookProperties.class);
	}

	@Bean
	public PaginationListSelection paginationListSelection() {
		return Mockito.mock(PaginationListSelection.class);
	}

	@Bean
	public UserSelection userSelection() {
		return Mockito.mock(UserSelection.class);
	}

	@Bean
	public FieldbookService fildbookMiddlewareService() {
		return Mockito.mock(FieldbookService.class);
	}

	@Bean
	public com.efficio.fieldbook.service.api.FieldbookService fieldbookService() {
		return Mockito.mock(com.efficio.fieldbook.service.api.FieldbookService.class);
	}

	@Bean
	public OntologyService ontologyService() {
		return Mockito.mock(OntologyService.class);
	}

	@Bean
	public MeasurementsGeneratorService measurementsGeneratorService() {
		return  Mockito.mock(MeasurementsGeneratorService.class);
	}

	@Bean
	public AdvancingNursery advancingNursery() {
		return Mockito.mock(AdvancingNursery.class);
	}

	@Bean
	public GermplasmDataManager germplasmDataManager() {
		return Mockito.mock(GermplasmDataManager.class);
	}

	@Bean
	public OntologyDataManager ontologyDataManager() {
		return Mockito.mock(OntologyDataManager.class);
	}

	@Bean
	public ValidationService validationService() {
		return Mockito.mock(ValidationService.class);
	}

	@Bean
	public DataImportService dataImportService() {
		return Mockito.mock(DataImportService.class);
	}

	@Bean
	public ErrorHandlerService errorHandlerService() {
		return Mockito.mock(ErrorHandlerService.class);
	}
}
