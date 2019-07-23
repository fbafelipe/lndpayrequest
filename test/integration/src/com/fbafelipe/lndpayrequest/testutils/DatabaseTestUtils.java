package com.fbafelipe.lndpayrequest.testutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fbafelipe.lndpayrequest.data.ServerConfig;

public class DatabaseTestUtils {
	private static Pattern CREATE_TABLE_PATTERN = Pattern.compile("CREATE TABLE IF NOT EXISTS (\\w+) ?\\(?");
	
	public static void resetDatabase(ServerConfig serverConfig) throws ClassNotFoundException, SQLException, IOException {
		Class.forName(serverConfig.getJdbcDriver());
		
		Connection connection = DriverManager.getConnection(serverConfig.getJdbcUrl(), serverConfig.getJdbcUsername(), serverConfig.getJdbcPassword());
		
		Statement statement = connection.createStatement();
		
		for (String table : getAllTables())
			statement.execute("DROP TABLE IF EXISTS " + table + ";");
		
		connection.close();
	}
	
	private static List<String> getAllTables() throws IOException {
		List<String> tables = new ArrayList<>();
		
		try (InputStream resourceInput = Thread.currentThread().getContextClassLoader().getResourceAsStream("create_tables.sql")) {
			BufferedReader input = new BufferedReader(new InputStreamReader(resourceInput));
			
			String line;
			while ((line = input.readLine()) != null) {
				Matcher matcher = CREATE_TABLE_PATTERN.matcher(line);
				if (matcher.matches())
					tables.add(matcher.group(1));
			}
		}
		
		return tables;
	}
}
