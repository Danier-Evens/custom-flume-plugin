package com.danier.custom.flume.plugin.ons;

import org.apache.flume.instrumentation.SinkCounter;

public class OnsSinkCounter extends SinkCounter {

    private static final String TIMER_ONS_EVENT_SEND =
            "channel.ons.event.send.time";

    private static final String COUNT_ROLLBACK =
            "channel.rollback.count";

    private static final String[] ATTRIBUTES = {COUNT_ROLLBACK, TIMER_ONS_EVENT_SEND};

    public OnsSinkCounter(String name) {
        super(name, ATTRIBUTES);
    }

    public long addToONSEventSendTimer(long delta) {
        return addAndGet(TIMER_ONS_EVENT_SEND, delta);
    }

    public long incrementRollbackCount() {
        return increment(COUNT_ROLLBACK);
    }

    public static String getTimerOnsEventSend() {
        return TIMER_ONS_EVENT_SEND;
    }

    public static String getCountRollback() {
        return COUNT_ROLLBACK;
    }
}