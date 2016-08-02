
package com.efficio.etl.web.listener;

import java.io.IOException;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.generationcp.commons.util.SpringAppContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.efficio.etl.service.FileService;
import com.efficio.etl.web.bean.UserSelection;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 * 
 * This class provides clean up operations to ensure that the temp file will always be deleted
 */

@Deprecated
public class DatasetImporterSessionListener implements HttpSessionListener {

  	private static final Logger LOG = LoggerFactory.getLogger(DatasetImporterSessionListener.class);

	@Override
	public void sessionCreated(HttpSessionEvent httpSessionEvent) {
		// do nothing
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
		HttpSession session = httpSessionEvent.getSession();
		ApplicationContext ctx = SpringAppContextProvider.getApplicationContext();

		Object obj = session.getAttribute("scopedTarget.userSelection");
		if (obj != null) {
			UserSelection userSelection = (UserSelection) obj;
			FileService fileService = (FileService) ctx.getBean("fileService");
			try {
				if (userSelection.getServerFileName() != null) {
					fileService.deleteTempFile(userSelection.getServerFileName());
				}
			} catch (IOException e) {
			  	DatasetImporterSessionListener.LOG.error(e.getMessage(), e);
			}
		}

	}
}
