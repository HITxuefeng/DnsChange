package com.iie.dns;

//
//import com.sun.jersey.api.client.Client;
//import com.sun.jersey.api.client.ClientResponse;
//import com.sun.jersey.api.client.WebResource;
//import org.apache.commons.httpclient.*;
//import org.apache.commons.httpclient.methods.GetMethod;
//import org.apache.commons.httpclient.methods.PostMethod;
//import org.apache.commons.httpclient.methods.PutMethod;
//import org.apache.commons.httpclient.params.HttpMethodParams;
import net.sf.json.JSONObject;

import java.io.IOException;

public class IpChange {
    public static void main(String args[]) throws IOException {
//        Client client = new Client();
//        WebResource webResource=client.resource("");

//        String jsonstr ="{\"domain_name\":\"feng.test2.dc.pub\",\"new_ip\":\"10.221.7.33\",\"old_ip\":\"10.221.7.32\",\"\"zone_name\":\"dc.pub\",\"view_names\":\"default\"}";
//
//        HttpClient htpClient = new HttpClient();
//        PutMethod putMethod = new PutMethod("https://10.201.8.12:20120/global_update_domain_ip");
//        putMethod.addRequestHeader( "Content-Type","application/json" );
//
//
////        JSONObject json = JSONObject.fromObject(jsonstr);
////        putMethod.getParams().setParameter( HttpMethodParams.HTTP_CONTENT_CHARSET, HttpConstants.HTTP_ELEMENT_CHARSET);
//        putMethod.setRequestBody(jsonstr);
//        htpClient.executeMethod( putMethod );
//        byte[] responseBody = putMethod.getResponseBody();
//        String resStr = new String(responseBody.toString());
//        putMethod.releaseConnection();
    }
}
