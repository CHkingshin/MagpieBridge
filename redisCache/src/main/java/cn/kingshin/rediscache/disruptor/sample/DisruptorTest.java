package cn.kingshin.rediscache.disruptor.sample;

import cn.kingshin.rediscache.disruptor.DisruptorQueue;
import cn.kingshin.rediscache.disruptor.DisruptorQueueFactory;
/**
 * @description
 * 一个生产者以及一个消费者进行测试，并且 3 秒种之后通知生产者线程退出。
 * 注意：RingBuffer 大小（即队列大小）必须是 2 的 N 次方，实际项目中我们通常将其设置为 1024 * 1024。
 * @author KingShin
 * @date 2022/8/15 19:28:28
 */
public class DisruptorTest {
    public static void main(String[] args) throws InterruptedException {

        // 创建消费者1
        MyConsumer myConsumer1 = new MyConsumer("---->消费者1");
        MyConsumer myConsumer2 = new MyConsumer("---->消费者2");

        // 创建一个Disruptor队列操作类对象（RingBuffer大小为4，false表示只有一个生产者,true表示多生产者）
        DisruptorQueue disruptorQueue = DisruptorQueueFactory.getHandleEventsQueue(
                4,
                true,
                myConsumer1,
                myConsumer2);
        // 创建多个生产者，开始模拟生产数据
        MyProducerThread myProducerThread1 = new MyProducerThread("11111生产者1", disruptorQueue);
        Thread t1 = new Thread(myProducerThread1);
        t1.start();

        MyProducerThread myProducerThread2 = new MyProducerThread("22222生产者2", disruptorQueue);
        Thread t2 = new Thread(myProducerThread1);
        t2.start();

        // 执行3s后，生产者不再生产
        Thread.sleep(3 * 1000);
        myProducerThread1.stopThread();
        myProducerThread2.stopThread();
        System.out.println("______________________________________________________");
        //通过 DisruptorQueueFactory.getWorkPoolQueue 方法创建“点对点”模式的操作队列，这样同一事件只会被一组消费者其中之一消费：
        DisruptorQueue<String> workPoolQueue = DisruptorQueueFactory.getWorkPoolQueue(4, true, myConsumer1, myConsumer2);
        MyProducerThread myProducerThread3 = new MyProducerThread("11111生产者1", disruptorQueue);
        Thread t3 = new Thread(myProducerThread1);
        t3.start();

        MyProducerThread myProducerThread4 = new MyProducerThread("22222生产者2", disruptorQueue);
        Thread t4 = new Thread(myProducerThread1);
        t4.start();

        // 执行3s后，生产者不再生产
        Thread.sleep(3 * 1000);
        myProducerThread3.stopThread();
        myProducerThread4.stopThread();
    }
}
