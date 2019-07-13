package com.fbafelipe.lndpayrequest.test;

import java.io.File;

public class TestEnv {
	private static final File BASE_ENV_FOLDER = new File("test/integration/base");
	private static final File TMP_ENV_FOLDER = new File("test/integration/tmp");
	
	private static TestEnv sInstance = new TestEnv();
	
	private LndNode mClientNode;
	private LndNode mServerNode;
	
	public static TestEnv getInstance() {
		return sInstance;
	}
	
	private TestEnv() {
		reset();
	}
	
	public void reset() {
		File stopScript = new File(TMP_ENV_FOLDER, "stop");
		if (stopScript.exists()) {
			CommandUtils.runCommand(TMP_ENV_FOLDER, stopScript);
			CommandUtils.runCommand(TMP_ENV_FOLDER, "rm", "-rf", TMP_ENV_FOLDER.getAbsolutePath());
		}
	}
	
	public LndNode getClientNode() {
		installTmpEnv();
		return mClientNode;
	}
	
	public LndNode getServerNode() {
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
