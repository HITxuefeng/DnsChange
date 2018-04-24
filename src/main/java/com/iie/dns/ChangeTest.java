package com.iie.dns;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ByteArrayEntity;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


import javax.net.ssl.*;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class ChangeTest {

    private static class TrustAnyTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }
    }

    private static class TrustAnyHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        String url="https://10.201.8.14:20120/global_update_domain_ip";
        String username="admin";
        String passwd="admin";
        String jsonstr ="{\"domain_names\":\"feng.test2.dc.pub.\", \"new_ips\":\"10.221.7.32\", \"need_ret_code\":\"yes\",\"current_user\":\"admin\"}";

//        HttpClient httpClient=new HttpClient();
//        PutMethod putMethod=new PutMethod(url);
//        httpClient.getState().setCredentials(
//                AuthScope.ANY,
//                new UsernamePasswordCredentials(username, passwd)
//        );
//        putMethod.addRequestHeader( "Content-Type","application/json" );
//        putMethod.setRequestBody(jsonstr);
//
//
//        int response=httpClient.executeMethod(putMethod);
//        System.out.println("success*****************="+response);

        URL console = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) console.openConnection();
        if (connection instanceof HttpsURLConnection)  {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new java.security.SecureRandom());
            ((HttpsURLConnection) connection).setSSLSocketFactory(sc.getSocketFactory());
            ((HttpsURLConnection) connection).setHostnameVerifier(new TrustAnyHostnameVerifier());
        }
        connection.setRequestMethod("PUT");
        connection.setRequestProperty("Content-Type", " application/json");

        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
//        OutputStream out = connection.getOutputStream();
        out.write(jsonstr.getBytes());
        out.flush();
        out.close();



    }
}
