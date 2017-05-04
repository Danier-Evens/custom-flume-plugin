package com.souche.flume.sources;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.SystemClock;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.instrumentation.SourceCounter;
import org.apache.flume.source.AbstractSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 自定义执行命令sources
 * Created by danier on 17/5/4.
 */
public class MyExecSources extends AbstractSource implements EventDrivenSource, Configurable {
    private static final Logger logger = LoggerFactory.getLogger(MyExecSources.class);

    @Override
    public void configure(Context context) {

    }

    @Override
    public void start() {
        System.out.println("start....");

        super.start();
    }

    @Override
    public void stop() {
        System.out.println("stop....");
        super.stop();
    }

    private static class MyExecRunnable implements Runnable {

        public MyExecRunnable(String shell, String command, ChannelProcessor channelProcessor,
                              SourceCounter sourceCounter, boolean restart, long restartThrottle,
                              boolean logStderr, int bufferCount, long batchTimeout, Charset charset) {
            this.command = command;
            this.channelProcessor = channelProcessor;
            this.sourceCounter = sourceCounter;
            this.restartThrottle = restartThrottle;
            this.bufferCount = bufferCount;
            this.batchTimeout = batchTimeout;
            this.restart = restart;
            this.logStderr = logStderr;
            this.charset = charset;
            this.shell = shell;
        }

        private final String shell;
        private final String command;
        private final ChannelProcessor channelProcessor;
        private final SourceCounter sourceCounter;
        private volatile boolean restart;
        private final long restartThrottle;
        private final int bufferCount;
        private long batchTimeout;
        private final boolean logStderr;
        private final Charset charset;
        private Process process = null;
        private SystemClock systemClock = new SystemClock();
        private Long lastPushToChannel = systemClock.currentTimeMillis();
        ScheduledExecutorService timedFlushService;
        ScheduledFuture<?> future;

        @Override
        public void run() {
            do {
                String exitCode = "unknown";
                BufferedReader reader = null;
                String line = null;
                final List<Event> eventList = new ArrayList<Event>();

                timedFlushService = Executors.newSingleThreadScheduledExecutor(
                        new ThreadFactoryBuilder().setNameFormat(
                                "timedFlushExecService" +
                                        Thread.currentThread().getId() + "-%d").build());
                try {
                    if (shell != null) {
                        String[] commandArgs = formulateShellCommand(shell, command);
                        process = Runtime.getRuntime().exec(commandArgs);
                    } else {
                        String[] commandArgs = command.split("\\s+");
                        process = new ProcessBuilder(commandArgs).start();
                    }
                    reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream(), charset));


                    future = timedFlushService.scheduleWithFixedDelay(new Runnable() {
                                                                          @Override
                                                                          public void run() {
                                                                              try {
                                                                                  synchronized (eventList) {
                                                                                      if (!eventList.isEmpty() && timeout()) {
                                                                                          flushEventBatch(eventList);
                                                                                      }
                                                                                  }
                                                                              } catch (Exception e) {
                                                                                  logger.error("Exception occured when processing event batch", e);
                                                                                  if (e instanceof InterruptedException) {
                                                                                      Thread.currentThread().interrupt();
                                                                                  }
                                                                              }
                                                                          }
                                                                      },
                            batchTimeout, batchTimeout, TimeUnit.MILLISECONDS);

                    while ((line = reader.readLine()) != null) {
                        synchronized (eventList) {
                            sourceCounter.incrementEventReceivedCount();
                            eventList.add(EventBuilder.withBody(line.getBytes(charset)));
                            if (eventList.size() >= bufferCount || timeout()) {
                                flushEventBatch(eventList);
                            }
                        }
                    }

                    synchronized (eventList) {
                        if (!eventList.isEmpty()) {
                            flushEventBatch(eventList);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed while running command: " + command, e);
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException ex) {
                            logger.error("Failed to close reader for exec source", ex);
                        }
                    }
                    exitCode = String.valueOf(kill());
                }
                if (restart) {
                    logger.info("Restarting in {}ms, exit code {}", restartThrottle,
                            exitCode);
                    try {
                        Thread.sleep(restartThrottle);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    logger.info("Command [" + command + "] exited with " + exitCode);
                }
            } while (restart);
        }

        private void flushEventBatch(List<Event> eventList) {
            channelProcessor.processEventBatch(eventList);
            sourceCounter.addToEventAcceptedCount(eventList.size());
            eventList.clear();
            lastPushToChannel = systemClock.currentTimeMillis();
        }

        private boolean timeout() {
            return (systemClock.currentTimeMillis() - lastPushToChannel) >= batchTimeout;
        }

        private static String[] formulateShellCommand(String shell, String command) {
            String[] shellArgs = shell.split("\\s+");
            String[] result = new String[shellArgs.length + 1];
            System.arraycopy(shellArgs, 0, result, 0, shellArgs.length);
            result[shellArgs.length] = command;
            return result;
        }

        public int kill() {
            if (process != null) {
                synchronized (process) {
                    process.destroy();

                    try {
                        int exitValue = process.waitFor();

                        // Stop the Thread that flushes periodically
                        if (future != null) {
                            future.cancel(true);
                        }

                        if (timedFlushService != null) {
                            timedFlushService.shutdown();
                            while (!timedFlushService.isTerminated()) {
                                try {
                                    timedFlushService.awaitTermination(500, TimeUnit.MILLISECONDS);
                                } catch (InterruptedException e) {
                                    logger.debug("Interrupted while waiting for exec executor service "
                                            + "to stop. Just exiting.");
                                    Thread.currentThread().interrupt();
                                }
                            }
                        }
                        return exitValue;
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
                return Integer.MIN_VALUE;
            }
            return Integer.MIN_VALUE / 2;
        }

        public void setRestart(boolean restart) {
            this.restart = restart;
        }
    }
}
