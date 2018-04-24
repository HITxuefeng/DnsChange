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
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

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
import org.apache.log4j.Logger;

public class DnsChange {
    private static final Logger LOG = Logger.getLogger(DnsChange.class);
    private static PoolingHttpClientConnectionManager connMgr;
    private static RequestConfig requestConfig;
    private static final int MAX_TIMEOUT = 7000;
    public String dnsAdminUser;
    public String dnsAdminPasswd;
    public String dnsServerUri;
    public String dnsDomainName;
    public String updateIP;

    public DnsChange (String dnsAdminUser, String dnsAdminPasswd, String dnsServerUri,String dnsDomainName,String updateIP){
        this.dnsAdminUser=dnsAdminUser;
        this.dnsAdminPasswd=dnsAdminPasswd;
        this.dnsServerUri=dnsServerUri;
        this.dnsDomainName=dnsDomainName;
        this.updateIP=updateIP;
    }
    public void run() throws KeyStoreException, NoSuchAlgorithmException, IOException {
        String jsonstr = "{\"domain_names\":\""+dnsDomainName+".\", \"new_ips\":\""+updateIP+"\", \"need_ret_code\":\"yes\",\"current_user\":\"admin\"}";
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                AuthScope.ANY,
                // 认证用户名和密码
                new UsernamePasswordCredentials(dnsAdminUser, dnsAdminPasswd));

        CloseableHttpClient httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).setSSLSocketFactory(createSSLConnSocketFactory())
                .setConnectionManager(connMgr).setDefaultRequestConfig(requestConfig).build();
        HttpPut httpPut = new HttpPut(dnsServerUri);
        CloseableHttpResponse response = null;
        httpPut.setConfig(requestConfig);
        StringEntity stringEntity = new StringEntity(jsonstr, "UTF-8");
        stringEntity.setContentEncoding("UTF-8");
        stringEntity.setContentType("application/json");
        httpPut.setEntity(stringEntity);
        response = httpClient.execute(httpPut);
        int statusCode = response.getStatusLine().getStatusCode();
//        System.out.println("http状态码：" + statusCode);
        //输出更新日志
        LOG.info("http状态码：" + statusCode+"域名对应IP地址更新成功！");
    }
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
