package com.fbafelipe.lndpayrequest.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;

import com.fbafelipe.lndpayrequest.exception.ServerException;

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
	
	public static void errorResponse(HttpServletResponse response, Exception error) throws IOException {
		if (error instanceof ServerException) {
			switch (((ServerException) error).getError()) {
				case BAD_REQUEST:
					response.sendError(HttpServletResponse.SC_BAD_REQUEST);
					break;
				case DATABASE_ERROR:
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					break;
				case LND_ERROR:
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					break;
			}
		}
		else
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}
	
	public static String readResource(String resource) {
		byte buffer[] = new byte[1024];
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
			int length;
			while ((length = input.read(buffer)) > 0)
				output.write(buffer, 0, length);
			
			return new String(output.toByteArray(), StandardCharsets.UTF_8.name());
		}
		catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
