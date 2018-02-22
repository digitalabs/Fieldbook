package com.efficio.fieldbook.service.internal;

import java.io.IOException;

public interface ProcessRunner {

	Integer run(String... command) throws IOException;

	void setDirectory(String directory);
}
