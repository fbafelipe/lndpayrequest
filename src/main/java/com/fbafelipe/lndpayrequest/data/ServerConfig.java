package com.fbafelipe.lndpayrequest.data;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ServerConfig {
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
					// TODO log exception
					throw new RuntimeException();
				}
			}
		}
		
		return mProperties.getProperty(property);
	}
}
