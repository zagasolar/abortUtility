package com.citi.pamutility;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.management.RuntimeErrorException;

import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.citi.http.HttpClient;
import com.citi.pamutility.MultiThreadAbortProcess.GetThread;

public  class AbortProcessMultiThread extends  AbortCommon{
	
	
    private static final Logger logger = LogManager.getLogger(AbortProcessMultiThread.class);

	public AbortProcessMultiThread() {
		// TODO Auto-generated constructor stub
	}
	
	public AbortProcessMultiThread(String containerId) {
		super();
		super.setContainerId(containerId);
		//this.containerId = containerId;
	}

	@Override
	public String getContainerId() {
		// TODO Auto-generated method stub
		return super.getContainerId();
	}

	@Override
	public void setContainerId(String containerId) {
		// TODO Auto-generated method stub
		super.setContainerId(containerId);
	}
	
	@Override
	public CloseableHttpClient getHttpClinet() throws CertificateException, IOException, KeyManagementException,
			UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
		// TODO Auto-generated method stub
		return super.getHttpClinet();
	}

	@Override
	public List<Long> getProcesses(CloseableHttpClient client, String processStatusCode)
			throws CertificateException, IOException, KeyManagementException, UnrecoverableKeyException,
			KeyStoreException, NoSuchAlgorithmException {
		// TODO Auto-generated method stub
		return super.getProcesses(client, processStatusCode);
	}



	@Override
	public void abortProcesses(CloseableHttpClient client, List<Long> processInstanceIds)
			throws CertificateException, IOException, KeyManagementException, UnrecoverableKeyException,
			KeyStoreException, NoSuchAlgorithmException {
		
		
		 String auth = System.getProperty("AUTH", "cert");
		 PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
		 connManager.setMaxTotal(5);
		 connManager.setDefaultMaxPerRoute(4);

		 // establish connection for the multi thread
		 HttpClient httpClient = new HttpClient();
		 //CloseableHttpClient client = null;

	        if ("basic".equals(auth)) {
	            String userId = System.getProperty("USERNAME", "rhpamAdmin");
	            String password = System.getProperty("PASSWORD", "Surendhar3298");
	            logger.info("Basic Authentication establishing for multi-thread");
	            CredentialsProvider provider = new BasicCredentialsProvider();
	            provider.setCredentials(
	                    AuthScope.ANY,
	                    new UsernamePasswordCredentials(userId, password));
	            client = HttpClients.custom()
	                    .setConnectionManager(connManager)
	                    .setDefaultCredentialsProvider(provider)
	                    .build();
	        } else {
	            try {
	                client = httpClient.getHttpClientWithCertAuth(connManager);

	            } catch (CertificateException e) {
	                logger.info("CertificateException for multiabortProcess------>" + e.getMessage());
	                e.printStackTrace();
	            } catch (IOException e) {
	                logger.info("IOException for multiabortProcess------>" + e.getMessage());
	                e.printStackTrace();
	            }
	        }
	        List<Long> activeProcesses = getProcesses(client, "1");
	        
	        List<String> abortList;
	        
	        logger.info("Active Processes are " + activeProcesses);
	        if (activeProcesses.size() == 0) {
	            logger.info("No active processes to delete");
	        } else {
	            abortList = buildMicroAbortBatchs(activeProcesses);
	            try {
	                final GetThread[] threads = new GetThread[abortList.size()];
	                // finalAbortList(abortList);
	                for (String deleteUrl : abortList) {
	                    HttpDelete deleteRequest = new HttpDelete(deleteUrl);
	                    deleteRequest.addHeader("Accept", "application/json");
	                    deleteRequest.addHeader("Content-Type", "application/json");
	                    threads[abortList.indexOf(deleteUrl)] = new GetThread(deleteRequest, client);
	                }
	                for (GetThread thread : threads) {
	                    thread.start();
	                }
	                for (GetThread thread : threads) {
	                    thread.join();
	                }
	            } catch (Exception e) {
	                logger.info("IOException for multiabortProcess------>" + e.getMessage());
	                e.printStackTrace();
	                //throw new RuntimeException(e.getMessage());
	            }
	        }
		
	}

	
	
	@Override
	public void abort() throws KeyManagementException, UnrecoverableKeyException, CertificateException,
			KeyStoreException, NoSuchAlgorithmException, IOException {
		// TODO Auto-generated method stub
		super.abort();
	}

	public List<String> buildMicroAbortBatchs(List<Long> activeProcesses) throws IOException {
        String kieServerUrl = System.getProperty("KIE_SERVER_URL", "http://localhost:8080");
        logger.info("Aborting processes " + activeProcesses.size());
        Integer pid = activeProcesses.size();
        Integer subStringStart = 0;
        Integer subStringEnd = 10;
        List<String> abortList = new ArrayList<String>();
        
        
        while (pid > 0) {
            List<Long> processIds = null;
            if (pid > 10) {
                processIds = activeProcesses.subList(subStringStart, subStringEnd);
                // logger.info("Process Ids " + processIds);
                subStringStart = subStringEnd;
                subStringEnd = subStringEnd + 10;
                pid = pid - 10;
            } else {
                processIds = activeProcesses.subList(subStringStart, activeProcesses.size());
                pid = 0;
                // logger.info("Process Ids " + processIds);
            }
            String listString = "";

            for (Long processId : processIds) {

                listString += "instanceId=" + processId + "&";
            }

            listString = listString.substring(0, listString.length() - 1);

            String deleteUrl = kieServerUrl + "/kie-server/services/rest/server/containers/" +
                    getContainerId() + "/processes/instances?" + listString;

            logger.info("Aborting processes from URL " + deleteUrl);

            abortList.add(deleteUrl);

        }
        // logger.info("Aborted processes " + abortList);
        return abortList;
    }

    static class GetThread extends Thread {

        private HttpDelete deleteRequest;
        private CloseableHttpClient client;

        public GetThread(HttpDelete deleteRequest, CloseableHttpClient client) {

            this.deleteRequest = deleteRequest;
            this.client = client;
        }

        @Override
        public void run() {


            try {
                CloseableHttpResponse response = client.execute(deleteRequest);
                HttpEntity entity = response.getEntity();
                int status = response.getStatusLine().getStatusCode();
                if (status == 204){
                    logger.info("Aborted processes result successful--------> " + status);
                }
                else{
                    logger.info(" Some error happened during abort , please verify the log. status code ->  " + status);
                }
     

            } catch (IOException e) {
                logger.error("Error in aborting processes ---------->" + e.getMessage());
            }
        }

    }
	
    
public static void main(String[] args) throws IOException, KeyManagementException, UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
    	
		System.setProperty("AUTH", "basic");
		System.setProperty("USERNAME", "rhpamAdmin");
        //System.setProperty("PASSWORD", "Surendhar3298");
		System.setProperty("PASSWORD", "jboss123$");
        System.setProperty("KIE_SERVER_URL","http://localhost:8080");
        
        logger.info("Abort utility started ");

	    
    	String containerId =  "pimdemoprocess_1.0.0" ;
    	
    	logger.info("Continer id  " + containerId );
    	
    	IProcessUtilMgt abortProcesses = new AbortProcessMultiThread(containerId);
    	
    	abortProcesses.abort();
    	
        logger.info("Abort utility completed ");

    }

}
