package com.fbafelipe.lndpayrequest.util;

import java.io.Closeable;
import java.io.IOException;

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
}
