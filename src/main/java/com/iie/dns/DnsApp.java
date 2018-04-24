package com.iie.dns;

//import com.iie.dns.tools.DnsKeepaliveException;
//import com.iie.dns.tools.DnsRegister;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.apache.log4j.Logger;
import org.apache.log4j.Logger;

public class DnsApp
{
//    org.apache.log4j.PropertyConfigurator.configure()
    private static final Logger LOG = Logger.getLogger(DnsApp.class);

    public static void main(String[] args)
    {
        Properties dnsProp = new Properties();
        try
        {
            LOG.info("------------------");
            dnsProp.load(new FileInputStream(new File(args[0])));
        }
        catch (FileNotFoundException e1)
        {
            e1.printStackTrace();
            LOG.info("无法dns.properties配置文件");
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }
        String name=dnsProp.getProperty("dns.domain.names");
//        System.out.println(name);
        String[] dnsDomainNames=name.split(",");
//        System.out.println(dnsDomainNames[0]);
//        String[] dnsDomainNames = dnsProp.getProperty("dns.domain.names").split(",");

        //亚信需要指定的服务器
//        String dnsKeepalivedAddr = dnsProp.getProperty("dns.keepalived.address");
//        int dnsKeepalivedPort = Integer.parseInt(dnsProp.getProperty("dns.keepalived.port"));
//        String key = dnsProp.getProperty("key");
        int interval = Integer.parseInt(dnsProp.getProperty("interval"));

        System.out.println(interval);
        String principal = dnsProp.getProperty("admin_principal");
        String keytab = dnsProp.getProperty("admin_keytab");
        String cluster1HdfsConfPath = dnsProp.getProperty("cluster1.hadoop.conf.path");
        String cluster2HdfsConfPath = dnsProp.getProperty("cluster2.hadoop.conf.path");
        int datanodeThreshold = Integer.parseInt(dnsProp.getProperty("datanode.dead.threshold"));

        String cluster1KafkaIPs = dnsProp.getProperty("cluster1.kafka.ips");
        String cluster2KafkaIPs = dnsProp.getProperty("cluster2.kafka.ips");
        int kafkaThreshold = Integer.parseInt(dnsProp.getProperty("kafka.dead.threshold"));

//        String cluster1ESIPs = dnsProp.getProperty("cluster1.es.ips");
//        String cluster2ESIPs = dnsProp.getProperty("cluster2.es.ips");
//        int ESDeadNum = Integer.parseInt(dnsProp.getProperty("es.dead.threshold"));

        String dnsAdminUser = dnsProp.getProperty("dns.admin.user");
        String dnsAdminPasswd = dnsProp.getProperty("dns.admin.password");
        String dnsServerUri = dnsProp.getProperty("dns.server.uri");

//        String dnsClientIP = dnsProp.getProperty("dns.client.ip");
//        Properties properties = new Properties();
//        properties.setProperty("service.ws.username", "lvyf");
//        properties.setProperty("service.ws.password", "123456");
//        properties.setProperty("service.ws.endpoint", "10.201.8.13:80");
//        properties.setProperty("client.ip", "10.220.129.238");
//        DnsRegister register = new DnsRegister(properties);
        System.out.println(dnsDomainNames.length);
        for (int i = 0; i < dnsDomainNames.length; i++)
        {
            System.out.println(dnsDomainNames[i]);
//            String dnsDomainIPs = dnsProp.getProperty("domian_" + dnsDomainNames[i] + "_registry.ips");
            String dnsDomainIPs = dnsProp.getProperty("domain_"+dnsDomainNames[i]+"_registry.ips");
            String s=dnsProp.getProperty("domain_"+dnsDomainNames[i]+"_registry.ips");
            System.out.println(dnsDomainIPs);
            System.out.println(s);
            int dnsDomainPort = Integer.parseInt(dnsProp.getProperty("domain_"+dnsDomainNames[i]+"_registry.port"));
            Boolean dnsDomainDatanode = Boolean.valueOf(Boolean.parseBoolean(dnsProp.getProperty("domain_"+dnsDomainNames[i]+"_hdfs")));
            Boolean dnsDomainKafka = Boolean.valueOf(Boolean.parseBoolean(dnsProp.getProperty("domain_"+dnsDomainNames[i]+"_kafka")));

            System.out.println(dnsDomainDatanode+"----***********-------"+dnsDomainKafka);

            Boolean dnsDomainES = Boolean.valueOf(Boolean.parseBoolean(dnsProp.getProperty("domain" + dnsDomainNames[i] + ".es")));
            DnsKeepalived test = new DnsKeepalived(dnsDomainNames[i], dnsDomainIPs, dnsDomainPort, interval, dnsDomainDatanode, principal, keytab,
                    cluster1HdfsConfPath, cluster2HdfsConfPath, datanodeThreshold, dnsDomainKafka, cluster1KafkaIPs, cluster2KafkaIPs, kafkaThreshold,
                    dnsDomainES,dnsAdminUser,dnsAdminPasswd,dnsServerUri);
            test.start();
            LOG.info("开启域名:" + dnsDomainNames[i] + "保活监控服务！");
        }
    }
}
