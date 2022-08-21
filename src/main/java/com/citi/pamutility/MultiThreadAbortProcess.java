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
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.citi.http.HttpClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MultiThreadAbortProcess {

    private static final Logger logger = LogManager.getLogger(MultiThreadAbortProcess.class);

    private String containerId;

    public MultiThreadAbortProcess(String containerId) {
        super();
        this.containerId = containerId;
    }

    public List<Long> getActiveProcessesInstances(CloseableHttpClient client,
            String processStatusCode) throws IOException {
        boolean active = true;
        List<Long> activeProcesses = new ArrayList<Long>();
        String kieServerUrl = System.getProperty("KIE_SERVER_URL", "http://localhost:8080");
        Integer page_count = 0;
        Integer page_size = 10;
        while (active) {
            logger.info("Getting active processes from page " + page_count);
            logger.info("Getting active processes from size " + page_size);
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

    public List<String> abortProcessesList(List<Long> activeProcesses) throws IOException {
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
                    this.containerId + "/processes/instances?" + listString;

            logger.info("Aborting processes from URL " + deleteUrl);

            abortList.add(deleteUrl);

        }
        // logger.info("Aborted processes " + abortList);
        return abortList;
    }


    public void multiabortProcess() throws IOException, KeyManagementException, UnrecoverableKeyException,
            CertificateException, KeyStoreException, NoSuchAlgorithmException {

        String auth = System.getProperty("AUTH", "basic");
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(5);
        connManager.setDefaultMaxPerRoute(4);

        // establish connection for the multi thread
        HttpClient httpClient = new HttpClient();
        CloseableHttpClient client = null;

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
        List<Long> activeProcesses = getActiveProcessesInstances(client, "1");
        List<String> abortList;
        logger.info("Active Processes are " + activeProcesses);
        if (activeProcesses.size() == 0) {
            logger.info("No active processes to delete");
        } else {
            abortList = abortProcessesList(activeProcesses);
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
            }
        }
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

    public static void main(String[] args) throws Exception {
        String containerId = System.getProperty("CONTAINERID", "Gateway_1.0.0-SNAPSHOT");
        MultiThreadAbortProcess multiabortobject = new MultiThreadAbortProcess(containerId);
        multiabortobject.multiabortProcess();
    }

}
