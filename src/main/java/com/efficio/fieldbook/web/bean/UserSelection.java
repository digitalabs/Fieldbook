package com.efficio.fieldbook.web.bean;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * This bean models the various input that the user builds up over time to perform the actual loading operation
 */
public class UserSelection implements Serializable {
    private String actualFileName;
    private String serverFileName;

    public String getActualFileName() {
        return actualFileName;
    }

    public void setActualFileName(String actualFileName) {
        this.actualFileName = actualFileName;
    }

    public String getServerFileName() {
        return serverFileName;
    }

    public void setServerFileName(String serverFileName) {
        this.serverFileName = serverFileName;
    }

}
