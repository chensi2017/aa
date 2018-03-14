import kafka.serializer.StringDecoder
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{KafkaManager, SparkConf}
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}

object BaseContext {
 /* def main(args: Array[String]): Unit = {
    var kafkaParam = Map("metadata.broker.list" -> "192.168.0.83:6667",
      "group.id" -> "offsettest",
      "zookeeper.connect" -> "192.168.0.79:2181/kafka",
      "auto.offset.reset" -> "smallest",
    )
    var sparkConf = new SparkConf().setMaster("yarn").setAppName("offsetApp")
      .set("spark.cores.max", "30")
      .set("spark.executor.cores", "2")
      .set("spark.executor.memory", "1500m")
      .set("spark.defalut.parallelism", "200")
      .set("spark.streaming.blockInterval", "200")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.locality.wait.node","100")
      .set("spark.locality.wait","1500")
      .set("spark.streaming.kafka.maxRatePerPartition", "200")
      .set("spark.kryo.registrationRequired", "true")
    var ssc = new StreamingContext(sparkConf,Seconds(5))
    var topics = Set("store.input")
    var stream = KafkaUtils.createDirectStream[String,String,StringDecoder,StringDecoder](ssc,kafkaParam,topics)

  }*/

  /*  def dealLine(line: String): String = {
      val list = line.split(',').toList
  //    val list = AnalysisUtil.dealString(line, ',', '"')// 把dealString函数当做split即可
      list.get(0).substring(0, 10) + "-" + list.get(26)
    }*/

  def processRdd(rdd: RDD[(String, String)]): Unit = {
    /*val lines = rdd.map(_._2).map(x => (1,1)).reduceByKey(_+_)

    lines.foreach(println)*/
    rdd.foreach(println)
  }

  def main(args: Array[String]) {


    Logger.getLogger("org").setLevel(Level.WARN)

    var brokers = "192.168.0.79:6667,192.168.0.81:6667,192.168.0.85:6667"
    var topics = "store.input"
    var groupId = "offsettest04"

    // Create context with 2 second batch interval
    val sparkConf = new SparkConf().setAppName("DirectKafkaWordCount")
    sparkConf.setMaster("local[3]")
    sparkConf.set("spark.streaming.kafka.maxRatePerPartition", "5")
    sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

    val ssc = new StreamingContext(sparkConf, Seconds(5))
    ssc.sparkContext.setLogLevel("WARN")

    // Create direct kafka stream with brokers and topics
    val topicsSet = topics.split(",").toSet
    val kafkaParams = Map[String, String](
      "metadata.broker.list" -> brokers,
      "group.id" -> groupId,
      "auto.offset.reset" -> "smallest"
    )

    val km = new KafkaManager(kafkaParams)

    val messages = km.createDirectStream[String, String, StringDecoder, StringDecoder](
      ssc, topicsSet)


    messages.foreachRDD(rdd => {
      if (!rdd.isEmpty()) {
        // 先处理消息
        processRdd(rdd)
        // 再更新offsets
        km.updateOffsets(rdd)
      }
    })

    ssc.start()
    ssc.awaitTermination()
  }


}
