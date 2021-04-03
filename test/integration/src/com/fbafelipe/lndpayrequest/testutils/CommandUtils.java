package com.fbafelipe.lndpayrequest.testutils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class CommandUtils {
	public static String runCommand(File workingDir, File scriptFile, String ... args) {
		String cmdArray[] = new String[args.length + 1];
		cmdArray[0] = scriptFile.getAbsolutePath();
		
		for (int i = 0; i < args.length; ++i)
			cmdArray[i + 1] = args[i];
		
		return runCommand(workingDir, cmdArray);
	}
	
	public static String runCommand(File workingDir, String ... cmdArray) {
		try {
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(cmdArray, null, workingDir);
			
			process.waitFor();
			
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			byte buffer[] = new byte[1024];
			int length;
			
			try (InputStream input = process.getInputStream()) {
				while ((length = input.read(buffer)) > 0)
					output.write(buffer, 0, length);
			}
			
			try (InputStream input = process.getErrorStream()) {
				while ((length = input.read(buffer)) > 0)
					output.write(buffer, 0, length);
			}
			
			return new String(output.toByteArray(), StandardCharsets.UTF_8.name());
		}
		catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
