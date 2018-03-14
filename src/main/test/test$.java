
class test {

  public static void main(String[] args) {
      //"test-consumer-group112"为groupid 50:__consumer_offsets的partition数量 输出结果为该groupid对应__consumer_offsets的分区号
      System.out.println(Math.abs("test-consumer-group112".hashCode()) % 50);
  }

}
