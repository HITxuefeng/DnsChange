package com.iie.dns;


import org.apache.log4j.Logger;

public class LogTest {
    private static Logger logger = Logger.getLogger(LogTest.class);
    public static void main(String[] args){
        logger.debug("222222222222");
        logger.info("-------------");
        logger.error("error");
    }
}
