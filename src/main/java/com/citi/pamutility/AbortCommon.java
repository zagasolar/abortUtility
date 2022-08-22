package com.citi.pamutility;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.citi.http.HttpClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class AbortCommon implements IProcessUtilMgt {

    private static final Logger logger = LogManager.getLogger(AbortCommon.class);

    private String containerId;

    
	public String getContainerId() {
		return containerId;
	}

	public void setContainerId(String containerId) {
		this.containerId = containerId;
	}

	public AbortCommon() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public CloseableHttpClient getHttpClinet() throws CertificateException, IOException, KeyManagementException,
			UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
		// TODO Auto-generated method stub
		
		String authType = System.getProperty("AUTH", "cert");

		HttpClient httpClient = new HttpClient();
      
		CloseableHttpClient Client = null;
      
	    if("basic".equals(authType)){
	    	
	    	Client = httpClient.getHttpClientWithBasicAuth();
	          
	    }else{
	
	    	Client = httpClient.getHttpClientWithCertAuth();
		      
			
	    }
		return Client;       
	}
	
	public List<Long> getProcesses( CloseableHttpClient client , String processStatusCode) 
			throws CertificateException,
			IOException, KeyManagementException, 
			UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
		
	        boolean active = true;
	        List<Long> activeProcesses = new ArrayList<Long>();
	        String kieServerUrl = System.getProperty("KIE_SERVER_URL", "http://localhost:8080");
	        Integer page_count = 0;
	        Integer page_size = 100;
	        while (active) {
	        	
	            //logger.info("Getting active processes from page " + page_count);
	            //logger.info("Getting active processes from size " + page_size);
	            
	            String getInstanceURL = kieServerUrl + "/kie-server/services/rest/server/containers/"
	                    + this.containerId + "/processes/instances?status=" + processStatusCode
	                    + "&page=" + page_count + "&pageSize=" + page_size + "&sortOrder=true";
	           logger.info("Getting active processes from URL " + getInstanceURL);
	            
	            HttpGet getUrl = new HttpGet(getInstanceURL);
	            
	            getUrl.setHeader("Accept", "application/json");
	            getUrl.setHeader("Content-Type", "application/json");
	            
	            CloseableHttpResponse response = client.execute(getUrl);
	            
	            HttpEntity entity = response.getEntity();
	            if (entity != null) {

	                String result = EntityUtils.toString(entity);

	                JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();

	                JsonArray jsonArray = jsonObject.getAsJsonArray("process-instance");
	                if (jsonArray.size() > 0) {
	                    for (int i = 0; i < jsonArray.size(); i++) {
	                        JsonObject jsonObject2 = jsonArray.get(i).getAsJsonObject();
	                        activeProcesses.add(jsonObject2.get("process-instance-id").getAsLong());
	                    }
	                    page_count += 1;
	                } else {
	                    active = false;
	                }

	            }
	        }
	        return activeProcesses;
	    }

	@Override
	public void abort() throws KeyManagementException, UnrecoverableKeyException, CertificateException,
			KeyStoreException, NoSuchAlgorithmException, IOException {
			CloseableHttpClient htplClient = null;
    	
			try {
				
				//get http connection
				
				htplClient = getHttpClinet();
				
				//get active instance based on status code 1
				List<Long> processInstanceIds = getProcesses(htplClient,"1");
				
				int noPId = processInstanceIds.size();
				
			    
	
				
				if (noPId > 0) {
					
					logger.info(" number of active process beig Aborted ---->  "  + noPId);
	
					// abort all active instance 
					abortProcesses(htplClient,processInstanceIds);
					
					//mkae sure that all of the aborted 
					List<Long> abortedPprocessInstanceIds1 = getProcesses(htplClient,"3");
					
					logger.info(" number of active process  Aborted ---->  "  + abortedPprocessInstanceIds1.size());
	
		    	
				}else {
					logger.info(" There is no active process to be Aborted  " );
					
				}
	
			} finally {
				
				htplClient.close();
			}
		
		}

	
}
