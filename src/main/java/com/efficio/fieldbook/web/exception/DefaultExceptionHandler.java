
package com.efficio.fieldbook.web.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ControllerAdvice
public class DefaultExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultExceptionHandler.class);

	@Autowired
	ResourceBundleMessageSource messageSource;

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(Exception.class)
	@ResponseStatus(value = INTERNAL_SERVER_ERROR)
	@ResponseBody
	public String handleUncaughtException(Exception ex) {
		LOG.error("Error in service", ex);
		return ex.getMessage();
	}

	@RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
	@ExceptionHandler(FieldbookRequestValidationException.class)
	@ResponseStatus(value = NOT_ACCEPTABLE)
	@ResponseBody
	public String handleValidationException(FieldbookRequestValidationException ex) {
		LOG.error("Error in service validation", ex.getErrorCode());
		String message = this.messageSource.getMessage(ex.getErrorCode(), null, LocaleContextHolder.getLocale());
		return message;
	}
}
