package com.citi.http;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.http.ssl.SSLContexts;

public class HttpClient {

    public HttpClient() {
    }

    public CloseableHttpClient getHttpClientWithBasicAuth() {
        String userName = System.getProperty("USERNAME");
        String password = System.getProperty("PASSWORD");
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(userName, password));
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build();
        return httpClient;
    }

    public CloseableHttpClient getHttpClientWithCertAuth() throws IOException, CertificateException, KeyManagementException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
        CloseableHttpClient httpClient = null;
        SSLConnectionSocketFactory csf = null;
        
            csf = new SSLConnectionSocketFactory(
                    createSslContext(),
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        
        httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
        return httpClient;
    }

    public CloseableHttpClient getHttpClientWithCertAuth(PoolingHttpClientConnectionManager connManager) throws IOException, CertificateException, KeyManagementException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
        CloseableHttpClient httpClient = null;
        SSLConnectionSocketFactory csf = null;
        
            csf = new SSLConnectionSocketFactory(
                    createSslContext(),
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        
        httpClient = HttpClients.custom().setConnectionManager(connManager).setSSLSocketFactory(csf).build();
        return httpClient;
    }

    public SSLContext createSslContext() throws KeyStoreException, IOException, NoSuchAlgorithmException,
            CertificateException, KeyManagementException, UnrecoverableKeyException {

        // Trusted CA keystore
        final String CA_KEYSTORE_TYPE = KeyStore.getDefaultType(); // "JKS";
        final String CA_KEYSTORE_PATH = System.getProperty("CA_KEYSTORE_PATH", "/cacert.jks");
        final String CA_KEYSTORE_PASS = System.getProperty("CA_KEYSTORE_PASS", "changeit");
        KeyStore tks = KeyStore.getInstance(CA_KEYSTORE_TYPE);
        tks.load(new FileInputStream(CA_KEYSTORE_PATH), CA_KEYSTORE_PASS.toCharArray());
        SSLContext sslcontext = SSLContexts.custom()
                .loadTrustMaterial(tks, new TrustSelfSignedStrategy()) // use it to customize
                .loadKeyMaterial(tks, CA_KEYSTORE_PASS.toCharArray()) // load client certificate
                .build();
        return sslcontext;
    }
}
