package com.iie.dns;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.HAUtil;
import org.apache.hadoop.hdfs.protocol.ClientProtocol;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;
import org.apache.hadoop.hdfs.protocol.HdfsConstants;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.List;
import java.util.Properties;

public class HdfsTest {
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
            if(isHdfsServiceNormal(cluster1HdfsConfPath, datanodeThreshold, principal, keytab, dnsDomainDatanode)){
                System.out.println("Cluster1 OKOKOK!");
            }
            else {
                System.out.println("Cluster1 error");
            }

            if(isHdfsServiceNormal(cluster2HdfsConfPath, datanodeThreshold, principal, keytab, dnsDomainDatanode)){
                System.out.println("Cluster2 OKOKOK!");
            }
            else {
                System.out.println("Cluster2 error");
            }

        }

    }

    public static boolean isHdfsServiceNormal(String hdfsConfPath, int threshold, String principal, String keytab, Boolean hdfsFlag)
    {
        if (hdfsFlag.booleanValue())
        {
            Configuration conf = new Configuration();
            conf.set("fs.hdfs.impl", DistributedFileSystem.class.getName());
            conf.addResource(new Path(hdfsConfPath + "/core-site.xml"));
            conf.addResource(new Path(hdfsConfPath + "/hdfs-site.xml"));
//            conf.set("hadoop.security.authentication", "kerberos");

            UserGroupInformation.setConfiguration(conf);
            FileSystem fs = null;
            try
            {
//                UserGroupInformation.loginUserFromKeytab(principal, keytab);

                fs = FileSystem.get(conf);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            DistributedFileSystem hdfs = (DistributedFileSystem)fs;
            if (isActiveNameNode(hdfs))
            {
                int count = 0;
                try
                {
                    DatanodeInfo[] dataNodeStats = hdfs.getDataNodeStats(HdfsConstants.DatanodeReportType.ALL);
                    for (int i = 0; i < dataNodeStats.length; i++)
                    {
                        String datanodeIP = dataNodeStats[i].getName().split(":")[0];
                        int datanodePort = Integer.parseInt(dataNodeStats[i].getName().split(":")[1]);

                        System.out.println("***********:"+datanodeIP+":"+datanodePort);
                        if (!isHostConnectable(datanodeIP, datanodePort))
                        {
                            System.out.println("datanode 节点" + datanodeIP + "端口" + datanodePort + "不通,此节点datanode服务dead");
                            count++;
                        }
                    }
                    hdfs.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                if (count >= threshold)
                {
                    System.out.println("------dead datanode number :" + count + " 大于阈值: " + threshold + "--------");
                    return false;
                }
            }
            else
            {
                return false;
            }
        }
        return true;
    }


    public static boolean isActiveNameNode(DistributedFileSystem hdfs)
    {
        URI dfsUri = hdfs.getUri();
        String nsId = dfsUri.getHost();
        List<ClientProtocol> namenodes = null;
        try
        {
            namenodes = HAUtil.getProxiesForAllNameNodesInNameservice(hdfs.getConf(), nsId);
            if (!HAUtil.isAtLeastOneActive(namenodes)) {
                return false;
            }
        }
        catch (IOException e)
        {
            System.out.println(" no namenode is actived!");
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
