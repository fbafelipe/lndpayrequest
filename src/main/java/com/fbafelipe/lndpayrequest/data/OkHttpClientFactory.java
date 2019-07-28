package com.fbafelipe.lndpayrequest.data;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

import com.fbafelipe.lndpayrequest.util.BinaryUtils;

public class OkHttpClientFactory {
	private static final TrustManager TRUST_MANAGERS[] = {new TrustManagerImpl()};
	
	public OkHttpClient createOkHttpClient() {
		return new OkHttpClient.Builder().build();
	}
	
	public OkHttpClient createOkHttpClientWithPinnedCert(Set<String> trustedCertificates) throws NoSuchAlgorithmException, KeyManagementException {
			SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, TRUST_MANAGERS, new SecureRandom());
			SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
			
			OkHttpClient.Builder builder = new OkHttpClient.Builder();
			builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) TRUST_MANAGERS[0]);
			builder.hostnameVerifier(new HostnameVerifierImpl(trustedCertificates));
			
			return builder.build();
	}
	
	private static class HostnameVerifierImpl implements HostnameVerifier {
		private Set<String> mTrustedCertificates;
		
		public HostnameVerifierImpl(Set<String> trustedCertificates) {
			mTrustedCertificates = trustedCertificates;
		}
		
		@Override
		public boolean verify(String hostname, SSLSession session) {
			try {
				for (Certificate cert : session.getPeerCertificates()) {
					String fingerprint = BinaryUtils.sha256Fingerprint(cert.getEncoded());
					if (mTrustedCertificates.contains(fingerprint))
						return true;
				}
			}
			catch (Exception e) {}
			
			return false;
		}
		
	}
	
	private static class TrustManagerImpl implements X509TrustManager {
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
		
		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
		
		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[]{};
		}
	}
}
