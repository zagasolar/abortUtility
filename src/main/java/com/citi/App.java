package com.citi;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.citi.pamutility.AbortProcesses;

public class App {

    private static final Logger logger = LogManager.getLogger(App.class);

    
	public App() {
		// TODO Auto-generated constructor stub
	}
	
    public static void main(String[] args) throws IOException, KeyManagementException, UnrecoverableKeyException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
    
    	logger.info("Abort utility started ");
        
    	String containerId = System.getProperty("CONTAINERID", "Gateway_1.0.0-SNAPSHOT");

 	    
     	
     	logger.info("Continer id  " + containerId );
     	
     	AbortProcesses abortProcesses = new AbortProcesses(containerId);
     	
     	 abortProcesses.abort();
     	 
         logger.info("Abort utility completed ");
    }

}
