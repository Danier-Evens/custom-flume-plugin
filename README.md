# custom-flume-plugin

## 环境依赖
```
   java版本   7以上包含7
   flume版本  1.6以上包含1.6
```

## flume自定义插件测试样例

* 步骤一、打包： `mvn clean install`
* 步骤二、`export FLUME_HOME=xxxx` （可不配置，步骤三、四、五的$FLUME_HOME变量可以显示声明）
* 步骤三、`cp flume-ng-sources/target/custom-flume-ng-sources-1.0-SNAPSHOT.jar > $FLUME_HOME/lib`
* 步骤四、配置flume配置文件  `$FLUME_HOME/conf/myexplame.conf`
```
  # myexplame.conf: A single-node Flume configuration
  # Name the components on this agent
  a1.sources = r1
  a1.sinks = k1
  a1.channels = c1

  # Describe/configure the source
  a1.sources.r1.type = com.danier.custom.flume.plugin.sources.MyTestSources

  # Describe the sink
  a1.sinks.k1.type = logger

  # Use a channel which buffers events in memory
  a1.channels.c1.type = memory
  a1.channels.c1.capacity = 1000
  a1.channels.c1.transactionCapacity = 100

  # Bind the source and sink to the channel
  a1.sources.r1.channels = c1
  a1.sinks.k1.channel = c1
```
* 步骤五、启动脚本命令： `$FLUME_HOME/bin/flume-ng agent --conf $FLUME_HOME/conf --conf-file $FLUME_HOME/conf/myexplame.conf --name a1 -Dflume.root.logger=INFO,console`
```
  2017-05-04 17:53:43,344 (SinkRunner-PollingRunner-DefaultSinkProcessor) [INFO - org.apache.flume.sink.LoggerSink.process(LoggerSink.java:70)] Event: { headers:{id=1234567} body: 39 36 65 36 62 35 32 65 2D 64 35 64 66 2D 34 62 96e6b52e-d5df-4b }
  2017-05-04 17:53:44,344 (SinkRunner-PollingRunner-DefaultSinkProcessor) [INFO - org.apache.flume.sink.LoggerSink.process(LoggerSink.java:70)] Event: { headers:{id=1234567} body: 33 30 31 32 32 63 30 62 2D 38 36 38 35 2D 34 39 30122c0b-8685-49 }
  2017-05-04 17:53:45,349 (SinkRunner-PollingRunner-DefaultSinkProcessor) [INFO - org.apache.flume.sink.LoggerSink.process(LoggerSink.java:70)] Event: { headers:{id=1234567} body: 37 36 30 37 34 32 61 33 2D 31 30 38 33 2D 34 35 760742a3-1083-45 }
  2017-05-04 17:53:46,353 (SinkRunner-PollingRunner-DefaultSinkProcessor) [INFO - org.apache.flume.sink.LoggerSink.process(LoggerSink.java:70)] Event: { headers:{id=1234567} body: 32 37 65 31 63 62 37 39 2D 65 35 65 63 2D 34 34 27e1cb79-e5ec-44 }
  2017-05-04 17:53:47,357 (SinkRunner-PollingRunner-DefaultSinkProcessor) [INFO - org.apache.flume.sink.LoggerSink.process(LoggerSink.java:70)] Event: { headers:{id=1234567} body: 66 39 37 39 62 66 65 65 2D 32 39 65 61 2D 34 39 f979bfee-29ea-49 }
  2017-05-04 17:53:48,360 (SinkRunner-PollingRunner-DefaultSinkProcessor) [INFO - org.apache.flume.sink.LoggerSink.process(LoggerSink.java:70)] Event: { headers:{id=1234567} body: 30 36 33 65 66 31 33 38 2D 37 39 30 65 2D 34 37 063ef138-790e-47 }
  2017-05-04 17:53:49,365 (SinkRunner-PollingRunner-DefaultSinkProcessor) [INFO - org.apache.flume.sink.LoggerSink.process(LoggerSink.java:70)] Event: { headers:{id=1234567} body: 64 34 65 36 62 32 35 63 2D 30 39 39 36 2D 34 36 d4e6b25c-0996-46 }
  2017-05-04 17:53:50,370 (SinkRunner-PollingRunner-DefaultSinkProcessor) [INFO - org.apache.flume.sink.LoggerSink.process(LoggerSink.java:70)] Event: { headers:{id=1234567} body: 33 38 32 63 37 65 64 63 2D 39 63 62 39 2D 34 33 382c7edc-9cb9-43 }
  2017-05-04 17:53:51,375 (SinkRunner-PollingRunner-DefaultSinkProcessor) [INFO - org.apache.flume.sink.LoggerSink.process(LoggerSink.java:70)] Event: { headers:{id=1234567} body: 61 39 65 32 65 37 61 30 2D 62 34 66 63 2D 34 33 a9e2e7a0-b4fc-43 }
```

## flume自定义sink，数据发送至阿里云ONS

* 步骤一、打包： mvn clean install
* 步骤二、export FLUME_HOME=xxxx （可不配置，步骤三、四、五的$FLUME_HOME变量可以显示声明）
* 步骤三、cp flume-ng-sinks/target/custom-flume-ng-sinks-1.0-SNAPSHOT.jar > $FLUME_HOME/lib； 以及需要把ons-api、ons-clinet的包也复制到$FLUME_HOME/lib，我当前使用的版本是1.2.6。
* 步骤四、配置flume配置文件  $FLUME_HOME/conf/myexplame.conf
```
  # Name the components on this agent
  a1.sources = r1
  a1.sinks = k1
  a1.channels = c1

  # Describe/configure the source
  a1.sources.r1.type = com.danier.custom.flume.plugin.sources.MyTestSources

  # Describe the sink
  a1.sinks.k1.type = com.danier.custom.flume.plugin.ons.OnsSink
  # 可选项，默认为100
  a1.sinks.k1.ons.batchSize=1
  # 可选项，默认为*
  a1.sinks.k1.ons.tag=flume
  a1.sinks.k1.ons.topic=xxxx
  a1.sinks.k1.ons.pid=xxxx
  a1.sinks.k1.ons.accesskey=xxxx
  a1.sinks.k1.ons.secretkey=xxxx

  # Use a channel which buffers events in memory
  a1.channels.c1.type = memory
  a1.channels.c1.capacity = 1000
  a1.channels.c1.transactionCapacity = 100

  # Bind the source and sink to the channel
  a1.sources.r1.channels = c1
  a1.sinks.k1.channel = c1
```
* 步骤五、启动脚本命令： $FLUME_HOME/bin/flume-ng agent --conf $FLUME_HOME/conf --conf-file $FLUME_HOME/conf/myexplame.conf --name a1 -Dflume.root.logger=INFO,console
