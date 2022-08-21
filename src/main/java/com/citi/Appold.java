package com.citi;

import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.citi.httpclient.HttpClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class Appold {

    private static final Logger logger = LogManager.getLogger(Appold.class);
    
    public static void main(String[] args) throws IOException {

        logger.debug("This is a debug message");
        logger.info("This is an info message");
        logger.warn("This is a warn message");
        logger.error("This is an error message");
        logger.fatal("This is a fatal message");
        logger.trace("This is a trace message");
        String authType = System.getProperty("AUTH", "cert");
        String containerId = System.getProperty("CONTAINERID", "Gateway_1.0.0-SNAPSHOT");
        String kieServerUrl = System.getProperty("KIE_SERVER_URL","http://localhost:8080");
        String getInstanceURL = kieServerUrl + "/kie-server/services/rest/server/containers/" + containerId + "/processes/instances?status=1&page=0&pageSize=1000&sortOrder=true";
        HttpGet request = new HttpGet(getInstanceURL);
        request.addHeader("Accept", "application/json");
        request.addHeader("Content-Type", "application/json");
        HttpClient httpClient = new HttpClient();
        CloseableHttpClient Client = null;
        if("basic".equals(authType)){
            Client = httpClient.getHttpClientWithBasicAuth();
        }
        else{
            try {
                Client = httpClient.getHttpClientWithCertAuth();
            } catch (CertificateException e) {
                System.out.println("CertificateException-------->" + e);
                e.printStackTrace();
            }
        }
        try (
                CloseableHttpResponse response = Client.execute(request)) {
            // 401 if wrong user/password
            System.out.println(response.getStatusLine().getStatusCode());
            System.out.println("----------------------------------------");
            HttpEntity entity = response.getEntity();
            List<Integer> processInstanceIds = new ArrayList<Integer>();
            if (entity != null) {
                // return it as a String
                String result = EntityUtils.toString(entity);
                // System.out.println(result);
                try {
                    JsonObject jsonObject = JsonParser.parseString(result).getAsJsonObject();
                    JsonArray jsonArray = jsonObject.getAsJsonArray("process-instance");
                    System.out.println("ArraySize----> " + jsonArray.size());
                    if (jsonArray.size() > 0) {
                        for (int i = 0; i < jsonArray.size(); i++) {
                            JsonObject jsonObject2 = jsonArray.get(i).getAsJsonObject();
                            // System.out.println(jsonObject2.get("process-instance-id"));
                            processInstanceIds.add(jsonObject2.get("process-instance-id").getAsInt());
                        }
                        // System.out.println("processInstanceIds"+processInstanceIds);
                        String listString = "";
                        for (Integer processInstanceId : processInstanceIds) {
                            listString += "instanceId=" + processInstanceId + "&";
                        }
                        listString = listString.substring(0, listString.length() - 1);
                        // System.out.println("listString----------> " + listString);
                        String deleteUrl = "http://localhost:8080/kie-server/services/rest/server/containers/" + containerId + "/processes/instances?"
                                + listString;
                        HttpDelete deleteRequest = new HttpDelete(deleteUrl);
                        deleteRequest.addHeader("Accept", "application/json");
                        deleteRequest.addHeader("Content-Type", "application/json");
                        try {
                            CloseableHttpResponse deleteResponse = Client.execute(deleteRequest);
                            System.out.println("DELETE STATUS CODE "+deleteResponse.getStatusLine().getStatusCode());
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    } else {
                        System.out.println("No Process Instances to delete");
                    }
                } catch (Exception e) {
                    System.out.println("Exception Might be kie server is not in active " + e);
                }

            }

        }

    }

}
