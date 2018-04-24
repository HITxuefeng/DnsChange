package com.iie.dns;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangeTest2 {
    private static final Logger LOG = LoggerFactory.getLogger(ChangeTest2.class);
    private static PoolingHttpClientConnectionManager connMgr;
    private static RequestConfig requestConfig;
    private static final int MAX_TIMEOUT = 7000;

    public static void main(String[] args) throws IOException, KeyStoreException, NoSuchAlgorithmException {
        String ip=args[0];
        String url = "https://10.201.8.14:20120/global_update_domain_ip";
        String username = "admin";
        String passwd = "admin";
        String jsonstr = "{\"domain_names\":\"feng.test2.dc.pub\", \"new_ips\":\""+ip+"\", \"need_ret_code\":\"yes\",\"current_user\":\"admin\"}";

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                AuthScope.ANY,
                // 认证用户名和密码
                new UsernamePasswordCredentials(username, passwd));

        CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).setSSLSocketFactory(createSSLConnSocketFactory())
                .setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).build();
        HttpPut httpPut = new HttpPut(url);
        CloseableHttpResponse response = null;
        httpPut.setConfig(requestConfig);
        StringEntity stringEntity = new StringEntity(jsonstr, "UTF-8");
        stringEntity.setContentEncoding("UTF-8");
        stringEntity.setContentType("application/json");
        httpPut.setEntity(stringEntity);
        response = httpClient.execute(httpPut);
        int statusCode = response.getStatusLine().getStatusCode();
        System.out.println("http状态码：" + statusCode);
        LOG.info("-----------------------");
    }

        //httpclient发送https的ssl认证
    private static SSLConnectionSocketFactory createSSLConnSocketFactory() throws KeyStoreException, NoSuchAlgorithmException {
        SSLConnectionSocketFactory sslsf = null;
        try {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {

                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            sslsf = new SSLConnectionSocketFactory(sslContext, new X509HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
                @Override
                public void verify(String host, SSLSocket ssl) throws IOException {
                }
                @Override
                public void verify(String host, X509Certificate cert) throws SSLException {
                }
                @Override
                public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
                }
            });
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return sslsf;
    }
}
