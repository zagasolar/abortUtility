package com.citi.pamutility;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.List;

import org.apache.http.impl.client.CloseableHttpClient;

public interface IProcessUtilMgt {
	
	public CloseableHttpClient getHttpClinet() 
							throws CertificateException, 
							IOException, KeyManagementException, 
							UnrecoverableKeyException, KeyStoreException,
							NoSuchAlgorithmException;
	
	public List<Long> getProcesses( CloseableHttpClient client , String processStatusCode) 
											throws CertificateException,
											IOException, KeyManagementException, 
											UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException ;
	
	
	
	public void abortProcesses( CloseableHttpClient client , List<Long> processInstanceIds  ) 
											throws CertificateException, IOException,
											KeyManagementException, UnrecoverableKeyException, 
											KeyStoreException, NoSuchAlgorithmException;
	
	public void abort() throws KeyManagementException, UnrecoverableKeyException, 
								CertificateException, KeyStoreException,
								NoSuchAlgorithmException, IOException;
	
	
	
}
