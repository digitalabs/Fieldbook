/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.annotation.Resource;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import com.efficio.fieldbook.service.api.ErrorHandlerService;

@Service
public class ErrorHandlerServiceImpl implements ErrorHandlerService{

    @Resource
    private ResourceBundleMessageSource messageSource;
    
    @Override
    public List<String> getErrorMessagesAsList(String errorCodes, Object[] parameters) {
        List<String> errorMessages = new ArrayList<String>();
        if (errorCodes != null) {
            Locale locale = LocaleContextHolder.getLocale();
            
            String[] errorCodesArray = errorCodes.split(",");
            for (String errorCode : errorCodesArray) {
                String message = null;
                try {
                    message = messageSource.getMessage(errorCode, parameters, locale);
                } catch(Exception e) {
                    //do nothing
                }
                if (message != null) {
                    errorMessages.add(message);
                }
                else {
                    errorMessages.add(errorCode);
                }
            }
        }
        return errorMessages;
    }
    
    @Override
    public String getErrorMessagesAsString(String errorCodes, Object[] parameters, String nextLine) {
        StringBuilder message = new StringBuilder();
        List<String> errorCodeList = getErrorMessagesAsList(errorCodes, parameters);
        for (String errorCode : errorCodeList) {
            if (message.length() > 0) {
                message.append(nextLine);
            }
            message.append(errorCode);
        }
        return message.toString();
    }

}
