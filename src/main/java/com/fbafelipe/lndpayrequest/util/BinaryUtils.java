package com.fbafelipe.lndpayrequest.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


public class BinaryUtils {
	public static String binToHex(byte bin[]) {
		StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < bin.length; ++i) {
            String hex = Integer.toHexString(0xff & bin[i]);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
	}
	
	public static String formatFingerprint(byte bin[]) {
		StringBuilder fingerprint = new StringBuilder();

        for (int i = 0; i < bin.length; ++i) {
        	if (i > 0)
        		fingerprint.append(':');
        	
            String hex = Integer.toHexString(0xff & bin[i]);
            if (hex.length() == 1)
            	fingerprint.append('0');
            fingerprint.append(hex);
        }

        return fingerprint.toString().toUpperCase();
	}
	
	public static String sha256Fingerprint(byte data[]) {
		try {
			return formatFingerprint(MessageDigest.getInstance("SHA-256").digest(data));
		}
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	public static String base64ToHex(String base64) {
		return binToHex(Base64.getDecoder().decode(base64));
	}
}
