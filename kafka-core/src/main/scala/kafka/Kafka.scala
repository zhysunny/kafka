/**
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package kafka

import java.util.Properties

import joptsimple.{ArgumentAcceptingOptionSpec, OptionParser}
import kafka.server.{KafkaServer, KafkaServerStartable}
import kafka.utils.{CommandLineUtils, Logging}
import org.apache.kafka.common.utils.Utils
import scala.collection.JavaConversions._

object Kafka extends Logging {

  def getPropsFromArgs(args: Array[String]): Properties = {
    val optionParser = new OptionParser
    val overrideOpt: ArgumentAcceptingOptionSpec[String] = optionParser.accepts("override", "Optional property that should override values set in server.properties file")
      .withRequiredArg()
      .ofType(classOf[String])
    if (args.length == 0) {
      CommandLineUtils.printUsageAndDie(optionParser, "USAGE: java [options] %s server.properties [--override property=value]*".format(classOf[KafkaServer].getSimpleName()))
    }
    // 加载server.properties配置
    val props = Utils.loadProps(args(0))

    if (args.length > 1) {
      // 后面参数必须是[--override property=value]*
      val options = optionParser.parse(args.slice(1, args.length): _*)
      if (options.nonOptionArguments().size() > 0) {
        // 不符合上面的格式这里会报错并退出进程
        CommandLineUtils.printUsageAndDie(optionParser, "Found non argument parameters: " + options.nonOptionArguments().toArray.mkString(","))
      }
      // 增加配置项
      props.putAll(CommandLineUtils.parseKeyValueArgs(options.valuesOf(overrideOpt)))
    }
    props
  }

  def main(args: Array[String]): Unit = {
    try {
      val serverProps: Properties = getPropsFromArgs(args)
      val kafkaServerStartable = KafkaServerStartable.fromProps(serverProps)

      // 附加关闭处理程序来捕获control-c
      Runtime.getRuntime().addShutdownHook(new Thread() {
        override def run() = {
          kafkaServerStartable.shutdown
        }
      })

      kafkaServerStartable.startup
      kafkaServerStartable.awaitShutdown
    }
    catch {
      case e: Throwable =>
        fatal(e)
        System.exit(1)
    }
    System.exit(0)
  }
}
