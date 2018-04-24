package com.iie.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class GetIP {
    public static void main(String[] args) throws UnknownHostException {
        InetAddress ip=InetAddress.getByName("www.google.com");
        System.out.println(ip.toString());
        System.out.println("Address:"+ip.getHostAddress());
        System.out.println("Name"+ip.getHostName());
    }
}
