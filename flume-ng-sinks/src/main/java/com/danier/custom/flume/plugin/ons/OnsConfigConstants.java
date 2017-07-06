package com.danier.custom.flume.plugin.ons;

/**
 * 阿里云ons消息 操作配置类
 * Created by danier on 17/7/6.
 */
public class OnsConfigConstants {

    public static final String PROPERTY_PREFIX = "ons.";

    public static final String PID = "pid";
    public static final String TOPIC = "topic";
    public static final String BATCH_SIZE = "batchSize";
    public static final String ACCESS_KEY = "accesskey";
    public static final String SECRET_KEY = "secretkey";
    public static final String TAG = "tag";


    public static final int DEFAULT_BATCH_SIZE = 100;
    public static final String DEFAULT_TAG = "*";
}
