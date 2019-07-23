package com.fbafelipe.lndpayrequest.testutils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import com.fbafelipe.lndpayrequest.data.ServerConfig;

public class TestEnv {
	private static final File BASE_ENV_FOLDER = new File("test/integration/base");
	private static final File TMP_ENV_FOLDER = new File("test/integration/tmp");
	
	private static TestEnv sInstance = new TestEnv();
	
	private LndTestNode mClientNode;
	private LndTestNode mServerNode;
	
	public static TestEnv getInstance() {
		return sInstance;
	}
	
	private TestEnv() {
		reset();
	}
	
	public void reset() {
		File stopScript = new File(TMP_ENV_FOLDER, "stop");
		if (stopScript.exists())
			CommandUtils.runCommand(TMP_ENV_FOLDER, stopScript);
		if (TMP_ENV_FOLDER.exists())
			CommandUtils.runCommand(TMP_ENV_FOLDER, "rm", "-rf", TMP_ENV_FOLDER.getAbsolutePath());
		
		try {
			DatabaseTestUtils.resetDatabase(new ServerConfig());
		}
		catch (ClassNotFoundException | SQLException | IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	public LndTestNode getClientNode() {
		installTmpEnv();
		return mClientNode;
	}
	
	public LndTestNode getServerNode() {
		installTmpEnv();
		return mServerNode;
	}
	
	private void installTmpEnv() {
		File startScript = new File(TMP_ENV_FOLDER, "start");
		if (!startScript.exists()) {
			CommandUtils.runCommand(BASE_ENV_FOLDER, "cp", "-r", BASE_ENV_FOLDER.getAbsolutePath(), TMP_ENV_FOLDER.getAbsolutePath());
			CommandUtils.runCommand(TMP_ENV_FOLDER, startScript);
		}
	}
}
