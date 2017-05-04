package com.souche.flume.sources;

import org.apache.flume.Context;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.PollableSource;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.instrumentation.SourceCounter;
import org.apache.flume.source.AbstractSource;
import org.apache.flume.source.ExecSourceConfigurationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.UUID;

/**
 * 自定义测试sources
 * Created by danier on 17/5/4.
 */
public class MyTestSources extends AbstractSource implements PollableSource, Configurable {
    private static final Logger logger = LoggerFactory.getLogger(MyTestSources.class);

    private SourceCounter sourceCounter;
    private Charset charset;
    private final HashMap<String, String> header = new HashMap<String, String>() {{
        put("id", "1234567");
    }};

    @Override
    public synchronized void start() {
        logger.info("Starting source");
        super.start();
        sourceCounter.start();
    }

    @Override
    public void stop() {
        logger.info("Stopping source");

        sourceCounter.stop();
        super.stop();
    }

    @Override
    public void configure(Context context) {
        logger.info("MyTestSources configure context {}", context.getParameters());
        charset = Charset.forName(context.getString(ExecSourceConfigurationConstants.CHARSET,
                ExecSourceConfigurationConstants.DEFAULT_CHARSET));
        if (sourceCounter == null) {
            sourceCounter = new SourceCounter(getName());
        }
    }

    @Override
    public Status process() throws EventDeliveryException {
        Status status = Status.READY;
        try {

            while (true) {
                String uuid = UUID.randomUUID().toString();
                sourceCounter.incrementAppendAcceptedCount();
                getChannelProcessor().processEvent(EventBuilder.withBody(uuid, charset, header));
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            logger.error("process error {}", e);
        }
        return status;
    }

    @Override
    public long getBackOffSleepIncrement() {
        //TODO
        return 0;
    }

    @Override
    public long getMaxBackOffSleepInterval() {
        //TODO
        return 0;
    }
}
