package com.fbafelipe.lndpayrequest.data;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerConfig {
	private static final Logger LOGGER = Logger.getLogger(ServerConfig.class.getSimpleName());
	
	private static final String CONFIG_PATH_ENV = "LNDPAYREQUEST_CONFIG";
	
	private Properties mProperties;
	
	public String getJdbcDriver() {
		return getProperty("jdbc.driver");
	}
	
	public String getJdbcUrl() {
		return getProperty("jdbc.url");
	}
	
	public String getJdbcUsername() {
		return getProperty("jdbc.username");
	}
	
	public String getJdbcPassword() {
		return getProperty("jdbc.password");
	}
	
	public String getLndRestHost() {
		return getProperty("lnd.resthost");
	}
	
	public int getLndRestPort() {
		return Integer.parseInt(getProperty("lnd.restport"));
	}
	
	public String getLndMacaroon() {
		return getProperty("lnd.macaroon");
	}
	
	public String getLndHttpsCert() {
		return getProperty("lnd.httpscert");
	}
	
	// return the timeout in milliseconds
	public long getPaymentRequestTimeout() {
		return Long.valueOf(getProperty("paymentRequestTimeoutMs"));
	}
	
	public boolean isDebug() {
		return Boolean.parseBoolean(getProperty("debug"));
	}
	
	private String getProperty(String property) {
		synchronized (this) {
			if (mProperties == null) {
				try {
					String path = System.getenv(CONFIG_PATH_ENV);
					if (path == null || path.isEmpty())
						throw new IOException("Error reading config file");
					
					mProperties = new Properties();
					mProperties.load(new FileReader(new File(path)));
				}
				catch (IOException e) {
					mProperties = null;
					LOGGER.log(Level.SEVERE, "Failed to load config.cfg", e);
					throw new RuntimeException();
				}
			}
		}
		
		return mProperties.getProperty(property);
	}
}
