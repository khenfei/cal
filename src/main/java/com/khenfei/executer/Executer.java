package com.khenfei.executer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public interface Executer {
	boolean execute(final Map<String, String> args) throws FileNotFoundException, IOException;
}
