package com.fbafelipe.lndpayrequest.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

public class PostPayloadReader {
	public String readStringPayload(HttpServletRequest request) throws IOException {
		byte buffer[] = new byte[1024];
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		try (InputStream input = request.getInputStream()) {
			int length;
			while ((length = input.read(buffer)) > 0)
				output.write(buffer, 0, length);
		}
		
		return new String(output.toByteArray(), StandardCharsets.UTF_8.name());
	}
}
