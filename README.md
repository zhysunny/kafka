## kafka源码解析

### kafka关键概念

* **Topic** 用于划分Message的逻辑概念，一个Topic可以分布在多个Broker上。
* **Partition** 是Kafka中横向扩展和一切并行化的基础，每个Topic都至少被切分为1个Partition。
* **Offset** 消息在Partition中的编号，编号顺序不跨Partition(在Partition内有序)。
* **Consumer** 用于从Broker中取出/消费Message。
* **Producer** 用于往Broker中发送/生产Message。
* **Replication** Kafka支持以Partition为单位对Message进行冗余备份，每个Partition都可以配置至少1个Replication(当仅1个Replication时即仅该Partition本身)。
* **Leader** 每个Replication集合中的Partition都会选出一个唯一的Leader，所有的读写请求都由Leader处理。其他Replicas从Leader处把数据更新同步到本地。
* **Broker** Kafka中使用Broker来接受Producer和Consumer的请求，并把Message持久化到本地磁盘。每个Cluster当中会选举出一个Broker来担任Controller，负责处理Partition的Leader选举，协调Partition迁移等工作。
* **ISR** In-Sync Replica,是Replicas的一个子集，表示目前Alive且与Leader能够“Catch-up”的Replicas集合。由于读写都是首先落到Leader上，所以一般来说通过同步机制从Leader上拉取数据的Replica都会和Leader有一些延迟(包括了延迟时间和延迟条数两个维度)，任意一个超过阈值都会把该Replica踢出ISR。每个Leader Partition都有它自己独立的ISR。

### kafka-core

* **admin**	kafka的管理员模块，操作和管理其topic，partition相关，包含创建，删除topic，或者拓展分区等。
* **api**	主要负责数据交互，客户端与服务端交互数据的编码与解码。
* **client**	该模块下就一个类，producer读取kafka broker元数据信息，topic和分区，以及leader。
* **cluster**	这里包含多个实体类，有Broker，Cluster，Partition，Replica。其中一个Cluster由多个Broker组成，一个Broker包含多个Partition，一个Topic的所有Partition分布在不同的Broker中，一个Replica包含都个Partition。
* **common**	这是一个通用模块，其只包含各种异常类以及错误验证。
* **consumer**	消费者处理模块，负责所有的客户端消费者数据和逻辑处理。
* **controller**	此模块负责中央控制器的选举，分区的Leader选举，Replica的分配或其重新分配，分区和副本的扩容等。
* **coordinator**	负责管理部分consumer group和他们的offset。
* **javaapi**	提供Java语言的producer和consumer的API接口。
* **log**	这是一个负责Kafka文件存储模块，负责读写所有的Kafka的Topic消息数据。
* **message**	封装多条数据组成一个数据集或者压缩数据集。
* **metrics**	负责内部状态的监控模块。
* **network**	该模块负责处理和接收客户端连接，处理网络时间模块。
* **producer**	生产者的细节实现模块，包括的内容有同步和异步的消息发送。
* **security**	负责Kafka的安全验证和管理模块。
* **serializer**	序列化和反序列化当前消息内容。
* **server**	该模块涉及的内容较多，有Leader和Offset的checkpoint，动态配置，延时创建和删除Topic，Leader的选举，Admin和Replica的管理，以及各种元数据的缓存等内容。
* **tools**	阅读该模块，就是一个工具模块，涉及的内容也比较多。有导出对应consumer的offset值；导出LogSegments信息，以及当前Topic的log写的Location信息；导出Zookeeper上的offset值等内容。
* **utils**	各种工具类，比如Json，ZkUtils，线程池工具类，KafkaScheduler公共调度器类，Mx4jLoader监控加载器，ReplicationUtils复制集工具类，CommandLineUtils命令行工具类，以及公共日志类等内容。
