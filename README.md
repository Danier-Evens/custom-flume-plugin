# datacenter-flume

## flume自定义插件测试样例

* 步骤一、打包： mvn clean install
* 步骤二、copy flume-ng-sources/target/datacenter-flume-ng-sources-1.0-SNAPSHOT.jar > $FLUME_HOME/lib
* 步骤三、配置flume配置文件  $FLUME_HOME/conf/myexplame.conf
```
  # myexplame.conf: A single-node Flume configuration
  # Name the components on this agent
  a1.sources = r1
  a1.sinks = k1
  a1.channels = c1

  # Describe/configure the source
  a1.sources.r1.type = com.souche.flume.sources.MyTestSources

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
* 启动脚本命令： $FLUME_HOME/bin/flume-ng agent --conf $FLUME_HOME/conf --conf-file $FLUME_HOME/conf/myexplame.conf --name a1 -Dflume.root.logger=INFO,console

