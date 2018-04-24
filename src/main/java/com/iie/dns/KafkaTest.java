package com.iie.dns;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Properties;

public class KafkaTest {
    private static final Logger LOG = Logger.getLogger(DnsKeepalived.class);
    public static void main(String args[]) throws IOException {


        Properties properties=new Properties();
//        properties.load(new FileInputStream(new File("C:\\xxxxx\\dns.properties")));
        properties.load(new FileInputStream(new File(args[0])));
        String name=properties.getProperty("dns.domain.names");
        System.out.println(name);
        String[] dnsDomainNames=name.split(",");
        System.out.println(dnsDomainNames[0]);

        int interval = Integer.parseInt(properties.getProperty("interval"));
        String principal = properties.getProperty("admin_principal");
        String keytab = properties.getProperty("admin_keytab");
        String cluster1HdfsConfPath = properties.getProperty("cluster1.hadoop.conf.path");
        String cluster2HdfsConfPath = properties.getProperty("cluster2.hadoop.conf.path");
        int datanodeThreshold = Integer.parseInt(properties.getProperty("datanode.dead.threshold"));

        System.out.println(cluster1HdfsConfPath);

        String cluster1KafkaIPs = properties.getProperty("cluster1.kafka.ips");
        String cluster2KafkaIPs = properties.getProperty("cluster2.kafka.ips");
        int kafkaThreshold = Integer.parseInt(properties.getProperty("kafka.dead.threshold"));

        String dnsAdminUser = properties.getProperty("dns.admin.user");
        String dnsAdminPasswd = properties.getProperty("dns.admin.password");
        String dnsServerUri = properties.getProperty("dns.server.uri");

        System.out.println(dnsAdminUser+dnsAdminPasswd+dnsServerUri);

        for (int i = 0; i < dnsDomainNames.length; i++)
        {
            System.out.println(dnsDomainNames[i]);
//            String dnsDomainIPs = dnsProp.getProperty("domian_" + dnsDomainNames[i] + "_registry.ips");
            String dnsDomainIPs = properties.getProperty("domain_feng.test2.dc.pub_registry.ips");
            String s=properties.getProperty("domain_feng.test2.dc.pub_registry.ips");
            System.out.println(dnsDomainIPs);
            System.out.println(s);
            int dnsDomainPort = Integer.parseInt(properties.getProperty("domain_feng.test2.dc.pub_registry.port"));
            Boolean dnsDomainDatanode = Boolean.valueOf(Boolean.parseBoolean(properties.getProperty("domain_feng.test2.dc.pub_hdfs")));
            Boolean dnsDomainKafka = Boolean.valueOf(Boolean.parseBoolean(properties.getProperty("domain_feng.test2.dc.pub_kafka")));


//            String s1=properties.getProperty("domain_feng.test2.dc.pub_hdfs");
//            String s2=properties.getProperty("domain_feng.test2.dc.pub_kafka");
//            Boolean dnsDomainDatanode1 = Boolean.valueOf(properties.getProperty("domain_feng.test2.dc.pub_hdfs"));
//            Boolean dnsDomainKafka1 =Boolean.valueOf(properties.getProperty("domain_feng.test2.dc.pub_kafka"));
//            System.out.println(s1+"----***********-------"+s2);
//            System.out.println(dnsDomainDatanode1+"----***********-------"+dnsDomainKafka1);
            System.out.println(dnsDomainDatanode+"----***********-------"+dnsDomainKafka);

//            Boolean dnsDomainES = Boolean.valueOf(Boolean.parseBoolean(dnsProp.getProperty("domian_" + dnsDomainNames[i] + ".es")));
//            DnsKeepalived test = new DnsKeepalived(dnsDomainNames[i], dnsDomainIPs, dnsDomainPort, interval, dnsDomainDatanode, principal, keytab,
//                    cluster1HdfsConfPath, cluster2HdfsConfPath, datanodeThreshold, dnsDomainKafka, cluster1KafkaIPs, cluster2KafkaIPs, kafkaThreshold,
//                    dnsDomainES,dnsAdminUser,dnsAdminPasswd,dnsServerUri);
//            test.start();
//            LOG.info("开启域名:" + dnsDomainNames[i] + "保活监控服务！");
            if(isKafkaServiceNormal("cluster1", cluster1KafkaIPs, kafkaThreshold, dnsDomainKafka)){
                System.out.println("Cluster1 kafka OKOKOK!");
            }
            else {
                System.out.println("Cluster1 kafka error");
            }

            if(isKafkaServiceNormal("cluster2", cluster2KafkaIPs, kafkaThreshold, dnsDomainKafka)){
                System.out.println("Cluster2 kafka OKOKOK!");
            }
            else {
                System.out.println("Cluster2 kafka error");
            }


        }
    }

    public static boolean isKafkaServiceNormal(String clusterName, String clusterKafkaIPs, int kafkaThreshold, Boolean kafkaFlag)
    {
        if (kafkaFlag.booleanValue())
        {
            String[] cluster = clusterKafkaIPs.split(";");
            for (int i = 0; i < cluster.length; i++)
            {
                int count = 0;
                String[] clusterIPs = cluster[i].split(",");
                for (int j = 0; j < clusterIPs.length; j++)
                {
                    Boolean flag = Boolean.valueOf(isHostConnectable(clusterIPs[j], 9092));
                    if (!flag.booleanValue())
                    {
                        LOG.info("cluster:" + clusterName + "'s kafka ip:" + clusterIPs[j] + ",port:" + 9092 + "连接失败!");
                        count++;
                    }
                }


                System.out.println("阈值："+kafkaThreshold);
                System.out.println("断点数目："+count);


                if (count >= kafkaThreshold)
                {
                    LOG.info(" 集群" + clusterName + "内kafka服务异常节点数为：" + count + ",大于等于阈值：" + kafkaThreshold + ",域名服务切换到另一集群");
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isHostConnectable(String host, int port)
    {
        Socket socket = new Socket();
        try
        {
            socket.connect(new InetSocketAddress(host, port), 200);
            System.out.println("--------"+host+":"+port+"telnet IP+端口是通的");
            return true;
        }
        catch (IOException e)
        {
            return false;
        }
        finally
        {
            try
            {
                socket.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
