package com.fbafelipe.lndpayrequest.data;

import java.io.IOException;
import java.util.Properties;

public class ServerConfig {
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
					mProperties = new Properties();
					mProperties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("config.cfg"));
				}
				catch (IOException e) {
					mProperties = null;
					throw new RuntimeException();
				}
			}
		}
		
		return mProperties.getProperty(property);
	}
}
