## 前言
* 因Flume通过配置的方式去采集日志，所以存在不灵活的情况，但Flume日志模型中除了日志body部分，还提供header，以及一些通配符的操作让配置更加灵活。
* 此篇文档讲介绍通用配置。

## 场景
* 配置一个通用的服务端，用于接收各个客户端agent日志，按照客户端不同日志类型，存储至hdfs不同路径，以及不同的kafka topic。
* 配置一个通用的服务端，用于接收各个客户端agent日志，按照客户端不同日志类型，存储至hdfs不同路径。

## 样例
* 场景1服务端配置
```
# Name the  components on this agent
general_log_2_hdfs_kafka.sources  = r1
general_log_2_hdfs_kafka.sinks =   k1 k2
general_log_2_hdfs_kafka.channels  =  c1 c2
 
#  Describe/configure the source
general_log_2_hdfs_kafka.sources.r1.type  = avro
general_log_2_hdfs_kafka.sources.r1.bind  = xxxx
general_log_2_hdfs_kafka.sources.r1.port  = xxxx
general_log_2_hdfs_kafka.sources.r1.selector.type=replicating
general_log_2_hdfs_kafka.sources.r1.channels  = c1 c2
 
# to hdfs 文件6小时回滚一次
general_log_2_hdfs_kafka.sinks.k1.type = hdfs
general_log_2_hdfs_kafka.sinks.k1.channel  = c1    
general_log_2_hdfs_kafka.sinks.k1.hdfs.path=xxxx/%{file_key}/%Y-%m-%d       
general_log_2_hdfs_kafka.sinks.k1.hdfs.writeFormat = Text
general_log_2_hdfs_kafka.sinks.k1.hdfs.fileType = DataStream
general_log_2_hdfs_kafka.sinks.k1.hdfs.useLocalTimeStamp = true
general_log_2_hdfs_kafka.sinks.k1.hdfs.filePrefix=%{file_key}
general_log_2_hdfs_kafka.sinks.k1.hdfs.fileSuffix=.log
general_log_2_hdfs_kafka.sinks.k1.hdfs.rollSize = 0
general_log_2_hdfs_kafka.sinks.k1.hdfs.rollCount = 0
general_log_2_hdfs_kafka.sinks.k1.hdfs.rollInterval = 21600
general_log_2_hdfs_kafka.sinks.k1.hdfs.round = true
general_log_2_hdfs_kafka.sinks.k1.hdfs.roundValue = 6
general_log_2_hdfs_kafka.sinks.k1.hdfs.roundUnit = hour
 
# to kafka
general_log_2_hdfs_kafka.sinks.k2.type = org.apache.flume.sink.kafka.KafkaSink
general_log_2_hdfs_kafka.sinks.k2.channel  = c2
general_log_2_hdfs_kafka.sinks.k2.brokerList = xxxx:xxxx,xxxx:xxxx,xxxx:xxxx
general_log_2_hdfs_kafka.sinks.k2.requiredAcks = 1
general_log_2_hdfs_kafka.sinks.k2.batchSize = 10
# 可不配置，默认topic为default-flume-topic，如果event的header中包含topic字段则以它为准。
general_log_2_hdfs_kafka.sinks.k2.topic = xxxx
 
# Use a  channel which buffers events in memory
general_log_2_hdfs_kafka.channels.c1.checkpointDir = /home/flume/data/flume_data/checkpoint
general_log_2_hdfs_kafka.channels.c1.dataDirs = /home/flume/data/flume_data/data
general_log_2_hdfs_kafka.channels.c1.type = file
general_log_2_hdfs_kafka.channels.c1.maxFileSize=1073741824
general_log_2_hdfs_kafka.channels.c1.capacity=10000000
general_log_2_hdfs_kafka.channels.c2.checkpointDir = /home/flume/data/flume_kafka/checkpoint
general_log_2_hdfs_kafka.channels.c2.dataDirs = /home/flume/data/flume_kafka/data
general_log_2_hdfs_kafka.channels.c2.type = file
general_log_2_hdfs_kafka.channels.c2.maxFileSize=1073741824
general_log_2_hdfs_kafka.channels.c2.capacity=10000000
``` 
* 发送至kafka时，topic可不配置，需要数据来源的数据中将topic加入header中。[参考](http://flume.apache.org/FlumeUserGuide.html#kafka-sink)
> Note Kafka Sink uses the topic and key properties from the FlumeEvent headers to send events to Kafka. If topic exists in the headers, the event will be sent to that specific topic, overriding the topic configured for the Sink. If key exists in the headers, the key will used by Kafka to partition the data between the topic partitions. Events with same key will be sent to the same partition. If the key is null, events will be sent to random partitions.

* 场景2服务端配置
```
# Name the  components on this agent
general_log_2_hdfs.sources  = r1
general_log_2_hdfs.sinks =   k1
general_log_2_hdfs.channels  =  c1
 
#  Describe/configure the source
general_log_2_hdfs.sources.r1.type  = avro
general_log_2_hdfs.sources.r1.bind  = xxxx
general_log_2_hdfs.sources.r1.port  = xxxx
general_log_2_hdfs.sources.r1.channels  = c1
 
# to hdfs 文件6小时回滚一次
general_log_2_hdfs.sinks.k1.type = hdfs
general_log_2_hdfs.sinks.k1.channel  = c1
general_log_2_hdfs.sinks.k1.hdfs.path=xxxx/%{file_key}/%Y-%m-%d
general_log_2_hdfs.sinks.k1.hdfs.writeFormat = Text
general_log_2_hdfs.sinks.k1.hdfs.fileType = DataStream
general_log_2_hdfs.sinks.k1.hdfs.useLocalTimeStamp = true
general_log_2_hdfs.sinks.k1.hdfs.filePrefix=%{file_key}
general_log_2_hdfs.sinks.k1.hdfs.fileSuffix=.log
general_log_2_hdfs.sinks.k1.hdfs.rollSize = 0
general_log_2_hdfs.sinks.k1.hdfs.rollCount = 0
general_log_2_hdfs.sinks.k1.hdfs.rollInterval = 21600
general_log_2_hdfs.sinks.k1.hdfs.round = true
general_log_2_hdfs.sinks.k1.hdfs.roundValue = 6
general_log_2_hdfs.sinks.k1.hdfs.roundUnit = hour
 
 
# Use a  channel which buffers events in memory
general_log_2_hdfs.channels.c1.checkpointDir = /home/flume/data/flume_data/checkpoint
general_log_2_hdfs.channels.c1.dataDirs = /home/flume/data/flume_dat/data
general_log_2_hdfs.channels.c1.type = file
general_log_2_hdfs.channels.c1.maxFileSize=1073741824
general_log_2_hdfs.channels.c1.capacity=10000000
```

### 客户端agent配置样例
* 针对场景1的配置
```
# demo.conf: A single-node Flume configuration
 
# Name the components on this agent
demo.sources = r1
demo.channels = c1
# sink配置方式为负载均衡
demo.sinks = k1 k2
demo.sinkgroups = g1
demo.sinkgroups.g1.sinks = k1 k2
demo.sinkgroups.g1.processor.type = load_balance
demo.sinkgroups.g1.processor.backoff = true
demo.sinkgroups.g1.processor.selector = random
 
# Describe/configure the source
demo.sources.r1.type = exec
demo.sources.r1.command = tail -F xxxxx
demo.sources.r1.interceptors = i1 i2
# 此配置表示采集的数据需要存储至hdfs的文件名称配置
demo.sources.r1.interceptors.i1.type = static
demo.sources.r1.interceptors.i1.preserveExisting = true
demo.sources.r1.interceptors.i1.key = file_key
demo.sources.r1.interceptors.i1.value = xxxx
# 此配置表示采集的数据需要发送到topic配置
demo.sources.r1.interceptors.i2.type = static
demo.sources.r1.interceptors.i2.preserveExisting = true
demo.sources.r1.interceptors.i2.key = topic
demo.sources.r1.interceptors.i2.value = xxxx
 
# Describe the sink 此处配置表示服务端配置有两台做负载均衡，可按照你的实际情况进行配置。
demo.sinks.k1.type = avro
demo.sinks.k1.hostname=xxxx
demo.sinks.k1.port=xxxx
demo.sinks.k2.type = avro
demo.sinks.k2.hostname=xxxx
demo.sinks.k2.port=xxxx
 
# Use a channel which buffers events in file
demo.channels.c1.type = file
demo.channels.c1.checkpointDir = demo/checkpoint
demo.channels.c1.dataDirs = demo/data
demo.channels.c1.maxFileSize=134217728
 
# Bind the source and sink to the channel
demo.sources.r1.channels = c1
demo.sinks.k1.channel = c1
demo.sinks.k2.channel = c1
```

* 针对场景2的配置

```
# demo.conf: A single-node Flume configuration
 
# Name the components on this agent
demo.sources = r1
demo.channels = c1
# sink配置方式为负载均衡
demo.sinks = k1 k2
demo.sinkgroups = g1
demo.sinkgroups.g1.sinks = k1 k2
demo.sinkgroups.g1.processor.type = load_balance
demo.sinkgroups.g1.processor.backoff = true
demo.sinkgroups.g1.processor.selector = random
 
# Describe/configure the source
demo.sources.r1.type = exec
demo.sources.r1.command = tail -F xxxxx
demo.sources.r1.interceptors = i1
# 此配置表示采集的数据需要存储至hdfs的文件名称配置
demo.sources.r1.interceptors.i1.type = static
demo.sources.r1.interceptors.i1.preserveExisting = true
demo.sources.r1.interceptors.i1.key = file_key
demo.sources.r1.interceptors.i1.value = xxxx
 
# Describe the sink 此处配置表示服务端配置有两台做负载均衡，可按照你的实际情况进行配置。
demo.sinks.k1.type = avro
demo.sinks.k1.hostname=xxxx
demo.sinks.k1.port=xxxx
demo.sinks.k2.type = avro
demo.sinks.k2.hostname=xxxx
demo.sinks.k2.port=xxxx
 
# Use a channel which buffers events in file
demo.channels.c1.type = file
demo.channels.c1.checkpointDir = demo/checkpoint
demo.channels.c1.dataDirs = demo/data
demo.channels.c1.maxFileSize=134217728
 
# Bind the source and sink to the channel
demo.sources.r1.channels = c1
demo.sinks.k1.channel = c1
demo.sinks.k2.channel = c1
```

* 对于大多数场景下，日志文件基本都是按天存储，Flume提供了Taildir Source、Spooling Directory Source、以及Exec Source。此处以Exec Source为例

```
# demo.conf: A single-node Flume configuration
 
# Name the components on this agent
demo.sources = r1
demo.channels = c1
# sink配置方式为负载均衡
demo.sinks = k1 k2
demo.sinkgroups = g1
demo.sinkgroups.g1.sinks = k1 k2
demo.sinkgroups.g1.processor.type = load_balance
demo.sinkgroups.g1.processor.backoff = true
demo.sinkgroups.g1.processor.selector = random
 
# Describe/configure the source
demo.sources.r1.type = exec
demo.sources.r1.shell = /bin/bash -c
demo.sources.r1.command = tail -F xxxxx/`date +%Y-%m-%d`/xxxx.log
demo.sources.r1.interceptors = i1
# 此配置表示采集的数据需要存储至hdfs的文件名称配置
demo.sources.r1.interceptors.i1.type = static
demo.sources.r1.interceptors.i1.preserveExisting = true
demo.sources.r1.interceptors.i1.key = file_key
demo.sources.r1.interceptors.i1.value = xxxx
 
# Describe the sink 此处配置表示服务端配置有两台做负载均衡，可按照你的实际情况进行配置。
demo.sinks.k1.type = avro
demo.sinks.k1.hostname=xxxx
demo.sinks.k1.port=xxxx
demo.sinks.k2.type = avro
demo.sinks.k2.hostname=xxxx
demo.sinks.k2.port=xxxx
 
# Use a channel which buffers events in file
demo.channels.c1.type = file
demo.channels.c1.checkpointDir = demo/checkpoint
demo.channels.c1.dataDirs = demo/data
demo.channels.c1.maxFileSize=134217728
 
# Bind the source and sink to the channel
demo.sources.r1.channels = c1
demo.sinks.k1.channel = c1
demo.sinks.k2.channel = c1
```
