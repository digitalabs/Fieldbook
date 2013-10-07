
package com.efficio.fieldbook.service.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface FileService{

    /**
     * 
     * @param userFile
     *            The input stream of the file to be saved
     * @return
     */
    public String saveTemporaryFile(InputStream userFile) throws IOException;

    /**
     * Retrieves a File object based on the given file name
     * 
     * @param currentFilename
     * @return
     * @throws IOException
     */
    public File retrieveFileFromFileName(String currentFilename) throws IOException;

}
