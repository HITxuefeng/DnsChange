package com.iie.dns;

//import com.iie.dns.tools.DnsRegister;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.hdfs.HAUtil;
import org.apache.hadoop.hdfs.protocol.ClientProtocol;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;
import org.apache.hadoop.hdfs.protocol.HdfsConstants;
import org.apache.hadoop.hdfs.protocol.HdfsConstants.DatanodeReportType;
import org.apache.hadoop.security.UserGroupInformation;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.apache.log4j.Logger;
public class DnsKeepalived extends Thread{
    private static final Logger LOG = Logger.getLogger(DnsKeepalived.class);
    public static String sendMessage_IP1Alived = null;
    public static String sendMessage_IP2Alived = null;
    public static String sendMessage_IP3Alived = null;
    public static String sendMessage_IP4Alived = null;
    public static Boolean flag1;
    public static Boolean flag2;
    public static Boolean flag3;
    public static Boolean flag4;
    public String domainIP1 = "";
    public String domainIP2 = "";
    public String domainIP3 = "";
    public String domainIP4 = "";
    public String dnsDomainName;
    public String dnsDomainIPs;
    public int dnsDomainPort;
    public String dnsKeepalivedAddr;
    public int dnsKeepalivedPort;
    public String key;
    public int interval;
    public Boolean dnsDomainKafka;
    public String cluster1KafkaIPs = null;
    public String cluster2KafkaIPs = null;
    public Boolean dnsDomainES;
    public String cluster1ESIPs = null;
    public String cluster2ESIPs = null;
    public int ESDeadNum;
    public Boolean dnsDomainDatanode;
    public String cluster1HdfsConfPath;
    public String cluster2HdfsConfPath;
    public int datanodeThreshold;
    public int kafkaThreshold;
    public String principal;
    public String keytab;
//    public DnsRegister register;
    public String dnsAdminUser;
    public String dnsAdminPasswd;
    public String dnsServerUri;

    public DnsKeepalived(String dnsDomainName, String dnsDomainIPs, int dnsDomainPort, int interval, Boolean dnsDomainDatanode,
                         String principal, String keytab, String cluster1HdfsConfPath, String cluster2HdfsConfPath, int datanodeThreshold,
                         Boolean dnsDomainKafka, String cluster1KafkaIPs, String cluster2KafkaIPs, int kafkaThreshold, Boolean dnsDomainES,
                         String dnsAdminUser, String dnsAdminPasswd, String dnsServerUri) {
//        this.register = register;
        this.dnsDomainName = dnsDomainName;
        this.dnsDomainIPs = dnsDomainIPs;
        this.dnsDomainPort = dnsDomainPort;
        this.dnsDomainDatanode = dnsDomainDatanode;
//        this.dnsKeepalivedAddr = dnsKeepalivedAddr;
//        this.dnsKeepalivedPort = dnsKeepalivedPort;
//        this.key = key;
        this.interval = interval;
        this.cluster1HdfsConfPath = cluster1HdfsConfPath;
        this.cluster2HdfsConfPath = cluster2HdfsConfPath;
        this.datanodeThreshold = datanodeThreshold;
        this.principal = principal;
        this.keytab = keytab;
        this.cluster1KafkaIPs = cluster1KafkaIPs;
        this.cluster2KafkaIPs = cluster2KafkaIPs;
        this.kafkaThreshold = kafkaThreshold;
        this.dnsDomainKafka = dnsDomainKafka;
//        this.cluster1ESIPs = cluster1ESIPs;
//        this.cluster2ESIPs = cluster2ESIPs;
//        this.ESDeadNum = ESDeadNum;
        this.dnsDomainES = dnsDomainES;
        this.dnsAdminUser=dnsAdminUser;
        this.dnsAdminPasswd=dnsAdminPasswd;
        this.dnsServerUri=dnsServerUri;
    }

    public void run()
    {
        final String[] domainIPs = this.dnsDomainIPs.split(",");
        final int domainIPNum = domainIPs.length;
        final int servicePort = this.dnsDomainPort;
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        this.domainIP1 = domainIPs[0];
        this.domainIP2 = domainIPs[1];
        flag1 = Boolean.valueOf(isHostConnectable(this.domainIP1, servicePort));
        flag2 = Boolean.valueOf(isHostConnectable(this.domainIP2, servicePort));


        System.out.println(flag1+"-------------"+flag2);

        TimerTask task = null;
        try
        {
            task = new TimerTask()
            {
                public void run()
                {
                    String currentTime = sdf.format(new Date());
                    InetAddress returnIP = null;
                    String IP=null;
                    try
                    {
                        returnIP = InetAddress.getByName(DnsKeepalived.this.dnsDomainName);
                        IP=returnIP.getHostAddress();
//                        String s= String.valueOf(returnIP);
//                        System.out.println("----------------"+returnIP.getHostAddress()+"--------------------------------");
//                        System.out.println(s);
//                        System.out.println(DnsKeepalived.this.domainIP1);
//                        System.out.println(DnsKeepalived.this.domainIP2);
                        System.out.println("---------解析域名" + DnsKeepalived.this.dnsDomainName + "当前返回的IP：" + IP + "-------");
                        DnsKeepalived.LOG.info(currentTime + " the domain name " + DnsKeepalived.this.dnsDomainName + " resolution is " + IP);
                    }
                    catch (UnknownHostException e)
                    {
                        e.printStackTrace();
                        DnsKeepalived.LOG.error(currentTime + " the domain name " + DnsKeepalived.this.dnsDomainName + " resolution is error !");
                    }
                    if (returnIP.getHostAddress().equals(DnsKeepalived.this.domainIP1))
                    {
                        DnsKeepalived.LOG.info("----------------------当前域名对应主集群IP----------------------------------------------");
                        DnsKeepalived.LOG.info(currentTime + " the domain " + DnsKeepalived.this.dnsDomainName + "'s registered ip is domainIP1:" + DnsKeepalived.this.domainIP1);
                        if ((DnsKeepalived.flag1.booleanValue())
                                && (DnsKeepalived.this.isHdfsServiceNormal(DnsKeepalived.this.cluster1HdfsConfPath, DnsKeepalived.this.datanodeThreshold, DnsKeepalived.this.principal, DnsKeepalived.this.keytab, DnsKeepalived.this.dnsDomainDatanode))
                                && (DnsKeepalived.this.isKafkaServiceNormal("cluster1", DnsKeepalived.this.cluster1KafkaIPs, DnsKeepalived.this.kafkaThreshold, DnsKeepalived.this.dnsDomainKafka)))
                        {
                            System.out.println("cluster1 HDFS参数信息"+DnsKeepalived.this.cluster1HdfsConfPath+DnsKeepalived.this.datanodeThreshold+DnsKeepalived.this.principal+DnsKeepalived.this.keytab+ DnsKeepalived.this.dnsDomainDatanode);
                            System.out.println("cluster1 KAFKA参数信息"+DnsKeepalived.this.cluster1KafkaIPs+DnsKeepalived.this.kafkaThreshold+DnsKeepalived.this.dnsDomainKafka);

                            DnsKeepalived.LOG.info(currentTime + " 域名" + DnsKeepalived.this.dnsDomainName + "当前注册IP domainIP1：" + DnsKeepalived.this.domainIP1 + "状态正常");
                            DnsKeepalived.LOG.info(currentTime + " the domain " + DnsKeepalived.this.dnsDomainName + "'s registered ip domainIP1:" + DnsKeepalived.this.domainIP1 + " is alived !");
                        }
                        else if ((DnsKeepalived.flag2.booleanValue())
                                && (DnsKeepalived.this.isHdfsServiceNormal(DnsKeepalived.this.cluster2HdfsConfPath, DnsKeepalived.this.datanodeThreshold, DnsKeepalived.this.principal, DnsKeepalived.this.keytab, DnsKeepalived.this.dnsDomainDatanode))
                                && (DnsKeepalived.this.isKafkaServiceNormal("cluster2", DnsKeepalived.this.cluster2KafkaIPs, DnsKeepalived.this.kafkaThreshold, DnsKeepalived.this.dnsDomainKafka)))
                        {
                            DnsKeepalived.LOG.info(currentTime + " 域名" + DnsKeepalived.this.dnsDomainName + "当前注册IP domainIP1：" + DnsKeepalived.this.domainIP1 + "状态异常,域名切换到domainIP2：" + DnsKeepalived.this.domainIP2);
                            DnsChange dnsChange=new DnsChange(dnsAdminUser,dnsAdminPasswd,dnsServerUri,DnsKeepalived.this.dnsDomainName,DnsKeepalived.this.domainIP2);
                            try {
                                dnsChange.run();
                            } catch (KeyStoreException e) {
                                e.printStackTrace();
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            DnsKeepalived.LOG.info(currentTime + " the domain " + DnsKeepalived.this.dnsDomainName + "'s registered ip domainIP2:" + DnsKeepalived.this.domainIP2 + " is alived !");
                        }
                        else
                        {
                            DnsKeepalived.LOG.error(currentTime + " error: 域名" + DnsKeepalived.this.dnsDomainName + "当前注册IP domainIP1：" + DnsKeepalived.this.domainIP1 + ",domainIP2：" + DnsKeepalived.this.domainIP2 + "状态都异常!!!");
                        }
                    }
                    else {
                        DnsKeepalived.LOG.info("----------------------当前域名对应备集群IP----------------------------------------------");
                        DnsKeepalived.LOG.info(currentTime + " the domain " + DnsKeepalived.this.dnsDomainName + "'s registered ip is domainIP2:" + DnsKeepalived.this.domainIP2);
                        if ((DnsKeepalived.flag1.booleanValue())
                                && (DnsKeepalived.this.isHdfsServiceNormal(DnsKeepalived.this.cluster1HdfsConfPath, DnsKeepalived.this.datanodeThreshold, DnsKeepalived.this.principal, DnsKeepalived.this.keytab, DnsKeepalived.this.dnsDomainDatanode))
                                && (DnsKeepalived.this.isKafkaServiceNormal("cluster1", DnsKeepalived.this.cluster1KafkaIPs, DnsKeepalived.this.kafkaThreshold, DnsKeepalived.this.dnsDomainKafka)))
                        {
                            DnsKeepalived.LOG.info("当前主集群恢复正常，将域名切换到对应的主集群IP："+DnsKeepalived.this.domainIP1);
                            DnsChange dnsChange=new DnsChange(dnsAdminUser,dnsAdminPasswd,dnsServerUri,DnsKeepalived.this.dnsDomainName,DnsKeepalived.this.domainIP1);
                            try {
                                dnsChange.run();
                            } catch (KeyStoreException e) {
                                e.printStackTrace();
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            DnsKeepalived.LOG.info(currentTime + " 域名" + DnsKeepalived.this.dnsDomainName + "当前注册IP domainIP1：" + DnsKeepalived.this.domainIP1 + "状态正常");
                            DnsKeepalived.LOG.info(currentTime + " the domain " + DnsKeepalived.this.dnsDomainName + "'s registered ip domainIP1:" + DnsKeepalived.this.domainIP1 + " is alived !");
                        }
                        else if ((DnsKeepalived.flag2.booleanValue())
                                && (DnsKeepalived.this.isHdfsServiceNormal(DnsKeepalived.this.cluster2HdfsConfPath, DnsKeepalived.this.datanodeThreshold, DnsKeepalived.this.principal, DnsKeepalived.this.keytab, DnsKeepalived.this.dnsDomainDatanode))
                                && (DnsKeepalived.this.isKafkaServiceNormal("cluster2", DnsKeepalived.this.cluster2KafkaIPs, DnsKeepalived.this.kafkaThreshold, DnsKeepalived.this.dnsDomainKafka)))
                        {
                            DnsKeepalived.LOG.info("主集群异常未恢复");
                            DnsKeepalived.LOG.info(currentTime + " the domain " + DnsKeepalived.this.dnsDomainName + "'s registered ip domainIP2:" + DnsKeepalived.this.domainIP2 + " is alived !");
                        }
                        else {
                            DnsKeepalived.LOG.error(currentTime + " error: 域名" + DnsKeepalived.this.dnsDomainName + "当前注册IP domainIP1：" + DnsKeepalived.this.domainIP1 + ",domainIP2：" + DnsKeepalived.this.domainIP2 + "状态都异常!!!");
                        }
                    }
                }
            };
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Timer timer = new Timer();
        long delay = 0L;

        timer.scheduleAtFixedRate(task, delay, this.interval);
    }



    //测试能否ping通主机
    public boolean isHostConnectable(String host, int port)
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

//二进制转十六进制
    public String bytesToHex(byte[] bytes)
    {
        StringBuffer hexStr = new StringBuffer();
        for (int i = 0; i < bytes.length; i++)
        {
            int num = bytes[i];
            if (num < 0) {
                num += 256;
            }
            if (num < 16) {
                hexStr.append("0");
            }
            hexStr.append(Integer.toHexString(num));
        }
        return hexStr.toString().toUpperCase();
    }

    public boolean isHdfsServiceNormal(String hdfsConfPath, int threshold, String principal, String keytab, Boolean hdfsFlag)
    {
        if (hdfsFlag.booleanValue())
        {
            Configuration conf = new Configuration();
            conf.set("fs.hdfs.impl", DistributedFileSystem.class.getName());
            conf.addResource(new Path(hdfsConfPath + "/core-site.xml"));
            conf.addResource(new Path(hdfsConfPath + "/hdfs-site.xml"));
            conf.set("hadoop.security.authentication", "kerberos");

            UserGroupInformation.setConfiguration(conf);
            FileSystem fs = null;
            try
            {
                UserGroupInformation.loginUserFromKeytab(principal, keytab);

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


    public boolean isActiveNameNode(DistributedFileSystem hdfs)
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


    public boolean isKafkaServiceNormal(String clusterName, String clusterKafkaIPs, int kafkaThreshold, Boolean kafkaFlag)
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

                if (count >= kafkaThreshold)
                {
                    LOG.info(" 集群" + clusterName + "内kafka服务异常节点数为：" + count + ",大于等于阈值：" + kafkaThreshold + ",域名服务切换到另一集群");
                    return false;
                }
            }
        }
        return true;
    }

}
