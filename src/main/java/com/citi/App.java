package com.citi;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.citi.pamutility.AbortProcessMultiThread;
import com.citi.pamutility.AbortProcesses;
import com.citi.pamutility.IProcessUtilMgt;

public class App {

    private static final Logger logger = LogManager.getLogger(App.class);

    
	public App() {
		// TODO Auto-generated constructor stub
	}
	
    public static void main(String[] args) throws IOException, KeyManagementException, UnrecoverableKeyException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
    
    	logger.info("Abort utility started ");
    	
    	System.setProperty("AUTH", "basic");
		System.setProperty("USERNAME", "rhpamAdmin");
        //System.setProperty("PASSWORD", "Surendhar3298");
		System.setProperty("PASSWORD", "jboss123$");
        System.setProperty("KIE_SERVER_URL","http://localhost:8080");
    	System.setProperty("CONTAINERID", "pimdemoprocess_1.0.0");
    	System.setProperty("EXECUTION", "M");

        
    	String containerId = System.getProperty("CONTAINERID");
    	
    	String executionType = System.getProperty("EXECUTION");
    	
     	
     	logger.info("Continer id  " + containerId );
     	
     	
     	IProcessUtilMgt abortProcesses = null;
     	
     	if ( "S".equalsIgnoreCase(executionType)  ) {
     		logger.info("S ");
     		
     		abortProcesses = new AbortProcesses(containerId);
     		
     	}else if  ( "M".equalsIgnoreCase(executionType)  ) {
     		abortProcesses = new AbortProcessMultiThread(containerId);
     	}
     	
     	if ( abortProcesses != null) {
     		abortProcesses.abort();
     		
     	}else {
     		
     		logger.error("Execution type not specified  ");
     	}
     	
     	
     	 
        logger.info("Abort utility completed ");
    }

}
