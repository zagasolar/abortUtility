package com.citi.httpclient;

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
import org.apache.http.util.EntityUtils;
import org.apache.http.ssl.SSLContexts;

public class HttpClient {

    public HttpClient() {
    }

    public CloseableHttpClient getHttpClientWithBasicAuth() {
        String userName = System.getProperty("USERNAME", "rhpamAdmin");
        String password = System.getProperty("PASSWORD", "Surendhar3298");
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(userName, password));
        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build();
        return httpClient;
    }

    public CloseableHttpClient getHttpClientWithCertAuth() throws IOException, CertificateException {
        CloseableHttpClient httpClient = null;
        SSLConnectionSocketFactory csf = null;
        try {
            csf = new SSLConnectionSocketFactory(
                    createSslContext(),
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        } catch (KeyManagementException e) {
            System.out.println("KeyManagementException------->" + e);
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            System.out.println("UnrecoverableKeyException-------->" + e);
            e.printStackTrace();
        } catch (KeyStoreException e) {
            System.out.println("KeyStoreException---------->" + e);
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException-------->" + e);
            e.printStackTrace();
        } catch (CertificateException e) {
            System.out.println("CertificateException--------->" + e);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("IOException--------->" + e);
            e.printStackTrace();
        }
        httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
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
