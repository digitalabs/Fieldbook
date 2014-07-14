package com.efficio.fieldbook.web.util;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.efficio.fieldbook.web.trial.bean.xml.ExpDesign;
import com.efficio.fieldbook.web.trial.bean.xml.MainDesign;

public class ExpDesignUtil {
	public static String getXmlStringForSetting(MainDesign mainDesign) throws JAXBException{
		JAXBContext context = JAXBContext.newInstance(MainDesign.class);
        Marshaller marshaller = context.createMarshaller();
        StringWriter writer = new StringWriter();
        marshaller.marshal(mainDesign, writer);
        return writer.toString();
    }
	
	public static MainDesign readXmlStringForSetting(String xmlString) throws JAXBException{
		JAXBContext context = JAXBContext.newInstance(MainDesign.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		MainDesign mainDesign = (MainDesign) unmarshaller.unmarshal(new StringReader(xmlString));
        return mainDesign;
	}
}
