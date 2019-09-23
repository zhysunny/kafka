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

