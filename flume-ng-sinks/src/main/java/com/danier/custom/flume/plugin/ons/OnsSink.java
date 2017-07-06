package com.danier.custom.flume.plugin.ons;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.Producer;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.google.common.base.Throwables;
import org.apache.flume.*;
import org.apache.flume.conf.Configurable;
import org.apache.flume.conf.ConfigurationException;
import org.apache.flume.instrumentation.SinkCounter;
import org.apache.flume.sink.AbstractSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by danier on 17/7/6.
 */
public class OnsSink extends AbstractSink implements Configurable {

    private static final Logger logger = LoggerFactory.getLogger(OnsSink.class);

    private String topic;
    private Producer producer;
    private Properties onsProps;
    private SinkCounter counter;
    private List<Message> messageList;
    public static final String KEY_HDR = "key";
    public static final String TOPIC_HDR = "topic";
    private String tag = OnsConfigConstants.DEFAULT_TAG;
    private int batchSize = OnsConfigConstants.DEFAULT_BATCH_SIZE;

    @Override
    public synchronized void start() {
        producer = ONSFactory.createProducer(onsProps);
        producer.start();
        super.start();
        logger.info("ons Sink {} start", getName());
    }

    @Override
    public synchronized void stop() {
        producer.shutdown();
        counter.stop();
        super.stop();
        logger.info("ons Sink {} stopped. Metrics: {}", getName(), counter);
    }

    /**
     * 读取配置，设置ons发送者配置。
     *
     * @param context
     */
    @Override
    public void configure(Context context) {
        Map<String, String> configProperties =
                context.getSubProperties(OnsConfigConstants.PROPERTY_PREFIX);
        logger.info("context onsProperties: {" + configProperties + "}");
        if (configProperties.containsKey(OnsConfigConstants.BATCH_SIZE)) {
            batchSize =
                    Integer.valueOf(configProperties.get(OnsConfigConstants.BATCH_SIZE));
        }
        if (configProperties.containsKey(OnsConfigConstants.TAG)) {
            tag = configProperties.get(OnsConfigConstants.TAG);
        }

        topic = configProperties.get(OnsConfigConstants.TOPIC);
        String pid = configProperties.get(OnsConfigConstants.PID);
        String accessKey = configProperties.get(OnsConfigConstants.ACCESS_KEY);
        String secretKey = configProperties.get(OnsConfigConstants.SECRET_KEY);
        if (topic == null ||
                pid == null ||
                accessKey == null ||
                secretKey == null) {
            throw new ConfigurationException("The Property 'ons.topic' or 'ons.pid' " +
                    "or 'ons.accesskey' or 'ons.secretkey' is not set. ");
        }
        onsProps = new Properties();
        onsProps.put(PropertyKeyConst.ProducerId, pid);
        onsProps.put(PropertyKeyConst.AccessKey, accessKey);
        onsProps.put(PropertyKeyConst.SecretKey, secretKey);
        messageList = new ArrayList<Message>(batchSize);
        if (counter == null) {
            counter = new SinkCounter(getName());
        }
    }

    @Override
    public Status process() throws EventDeliveryException {
        Status result = Status.READY;
        Channel channel = getChannel();
        Transaction transaction = null;
        Event event = null;
        String eventTopic = null;
        String eventKey = null;
        try {
            long processedEvents = 0;
            transaction = channel.getTransaction();
            transaction.begin();
            messageList.clear();
            for (; processedEvents < batchSize; processedEvents += 1) {
                event = channel.take();
                if (event == null) {
                    // no events available in channel
                    break;
                }

                byte[] eventBody = event.getBody();
                Map<String, String> headers = event.getHeaders();
                if ((eventTopic = headers.get(TOPIC_HDR)) == null) {
                    eventTopic = topic;
                }
                eventKey = headers.get(KEY_HDR);
                if (logger.isDebugEnabled()) {
                    logger.debug("{Event} " + eventTopic + " : " + eventKey + " : "
                            + new String(eventBody, "UTF-8"));
                    logger.debug("event #{}", processedEvents);
                }

                // create a message and add to buffer
                Message msg = new Message(topic, tag, eventBody);
                messageList.add(msg);
            }

            // publish batch and commit.
            if (processedEvents > 0) {
//                long startTime = System.nanoTime();
                for (Message message : messageList) {
                    producer.send(message);
                }
//                long endTime = System.nanoTime();
//                counter.addToONSEventSendTimer((endTime - startTime) / (1000 * 1000));
                counter.addToEventDrainSuccessCount(Long.valueOf(messageList.size()));
            }
            transaction.commit();
        } catch (Exception ex) {
            String errorMsg = "Failed to publish events";
            logger.error("Failed to publish events", ex);
            result = Status.BACKOFF;
            if (transaction != null) {
                try {
                    transaction.rollback();
//                    counter.incrementRollbackCount();
                } catch (Exception e) {
                    logger.error("Transaction rollback failed", e);
                    throw Throwables.propagate(e);
                }
            }
            throw new EventDeliveryException(errorMsg, ex);
        } finally {
            if (transaction != null) {
                transaction.close();
            }
        }
        return result;
    }
}
