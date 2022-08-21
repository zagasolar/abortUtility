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
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.citi.http.HttpClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AbortProcesses {
	
    private static final Logger logger = LogManager.getLogger(AbortProcesses.class);

	private String containerId ;
	
	
	public AbortProcesses(String containerId) {
		super();
		this.containerId = containerId;
	}

	public AbortProcesses() {
		// TODO Auto-generated constructor stub
	}
	
	public CloseableHttpClient getHttpClinet() throws CertificateException, IOException, KeyManagementException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException{
		
		
        

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
	
	public List<Long> getActiveProcesses( CloseableHttpClient client , String processStatusCode) throws CertificateException, IOException, KeyManagementException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
		
		String authType = System.getProperty("AUTH", "cert");
       // String containerId = System.getProperty("CONTAINERID", "Gateway_1.0.0-SNAPSHOT");
        String kieServerUrl = System.getProperty("KIE_SERVER_URL","http://localhost:8080");
       
        String getInstanceURL = kieServerUrl + "/kie-server/services/rest/server/containers/" 
	    	     + this.containerId + "/processes/instances?status="+processStatusCode+"&page=0&pageSize=1000&sortOrder=true";
	    	
    	
        List<Long> processInstanceIds = new ArrayList<Long>();
        
		//CloseableHttpClient client = null;
		
		client = getHttpClinet();
		
          
        HttpGet request = new HttpGet(getInstanceURL);
        request.addHeader("Accept", "application/json");
        request.addHeader("Content-Type", "application/json");
        	
        CloseableHttpResponse response;
		
        response = client.execute(request);
		
        HttpEntity entity = response.getEntity();
		
        if (entity != null) {

        	String result = EntityUtils.toString(entity);
		  
		    JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
		        
		   JsonArray jsonArray = jsonObject.getAsJsonArray("process-instance");
		        

		   if (jsonArray.size() > 0) {
			   for (int i = 0; i < jsonArray.size(); i++) {
		                JsonObject jsonObject2 = jsonArray.get(i).getAsJsonObject();
		                processInstanceIds.add(jsonObject2.get("process-instance-id").getAsLong());
		            }
		        }
		    					
		}


		return processInstanceIds;
            
       

	}	
	
	public void abortProcesses( CloseableHttpClient client , List<Long> processInstanceIds  ) throws CertificateException, IOException, KeyManagementException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
		
       // String containerId = System.getProperty("CONTAINERID", "Gateway_1.0.0-SNAPSHOT");

       
        
        
        
        String listString = "";
        
        for (Long processInstanceId : processInstanceIds) {
        	
            listString += "instanceId=" + processInstanceId + "&";
        }
        
        listString = listString.substring(0, listString.length() - 1);
        
        String deleteUrl = "http://localhost:8080/kie-server/services/rest/server/containers/" + 
        					this.containerId + "/processes/instances?" + listString;
        
        HttpDelete deleteRequest = new HttpDelete(deleteUrl);

        deleteRequest.addHeader("Accept", "application/json");
        deleteRequest.addHeader("Content-Type", "application/json");
       	
        CloseableHttpResponse deleteResponse = client.execute(deleteRequest);
        
        HttpEntity entity = deleteResponse.getEntity();
        
        int status = deleteResponse.getStatusLine().getStatusCode();
        
        if (status >= 200 && status < 300) {
        
        	logger.info(" Aborted job completed succesfully with status code --> " + status);
        	
        }else {
        	
        	
        	logger.info(" Some error happened during abort , please verify the log. Sattus code ->  " + status);

        }
        
        if (entity != null) {

        	String result = EntityUtils.toString(entity);
		  
		  //  JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
		        
		 //  JsonArray jsonArray = jsonObject.getAsJsonArray("process-instance");
		        
		   logger.info(" Aborted process infor detail --------------------------- ");
		   
		   logger.info(" Aborted process infor detail --> " + result );
		   
		   logger.info(" Aborted process infor detail --------------------------- ");

		   
		  
		    					
		}
            
       // System.out.println("DELETE STATUS CODE "+deleteResponse.getStatusLine().getStatusCode());
        
        
            
        
		
	}
	
	public void abort() throws KeyManagementException, UnrecoverableKeyException, CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException {
		
		
    	
    	
    	CloseableHttpClient htplClient = null;
    	
		try {
			
			//get http connection
			
			htplClient = getHttpClinet();
			
			//get active instance based on status code 1
			List<Long> processInstanceIds = getActiveProcesses(htplClient,"1");
			
			int noPId = processInstanceIds.size();
			
		    

			
			if (noPId > 0) {
				
				logger.info(" number of active process beig Aborted ---->  "  + noPId);

				// abort all active instance 
				abortProcesses(htplClient,processInstanceIds);
				
				//mkae sure that all of the aborted 
				List<Long> abortedPprocessInstanceIds1 = getActiveProcesses(htplClient,"3");
				
				logger.info(" number of active process  Aborted ---->  "  + abortedPprocessInstanceIds1.size());

	    	
			}else {
				logger.info(" There is no active process to be Aborted  " );
				
			}

		} finally {
			
			htplClient.close();
		}
		
	}
	
    public static void main(String[] args) throws IOException, KeyManagementException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
    	
		System.setProperty("AUTH", "basic");
		System.setProperty("USERNAME", "rhpamAdmin");
        System.setProperty("PASSWORD", "jboss123$");
        
        
        logger.info("Abort utility started ");

	    
    	String containerId =  "pimdemoprocess_1.0.0" ;
    	
    	logger.info("Continer id  " + containerId );
    	
    	AbortProcesses abortProcesses = new AbortProcesses(containerId);
    	
    	abortProcesses.abort();
    	
        logger.info("Abort utility completed ");

    }


}
