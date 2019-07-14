package com.fbafelipe.lndpayrequest.util;

import java.io.Closeable;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

public class Utils {
	public static void safeClose(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			}
			catch (IOException e) {
				// ignore
			}
		}
	}
	
	public static void prepareResponse(HttpServletResponse response) {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
	}
}
