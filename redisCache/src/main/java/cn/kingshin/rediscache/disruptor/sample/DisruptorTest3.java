package cn.kingshin.rediscache.disruptor.sample;

import cn.kingshin.rediscache.disruptor.DisruptorQueue;
import cn.kingshin.rediscache.disruptor.DisruptorQueueFactory;
import cn.kingshin.rediscache.disruptor.ObjectEventFactory;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.Executors;

public class DisruptorTest3 {
    /*
    * 复杂的消费者依赖关系：
        消费者 C1B 消费时，必须保证消费者 C1A 已经完成对该消息的消费；
        消费者 C2B 消费时，必须保证消费者 C2A 已经完成对该消息的消费；
        消费者 C3 消费时，必须保证消费者 C1B 和 C2B 已经完成对该消息的消费。
    * */
    public static void main(String[] args) throws InterruptedException {
        // 创建两个消费者
        MyConsumer myConsumerC1A = new MyConsumer("---->消费者C1A");
        MyConsumer myConsumerC1B = new MyConsumer("------->消费者C1B");
        MyConsumer myConsumerC2A = new MyConsumer("---->消费者C2A");
        MyConsumer myConsumerC2B = new MyConsumer("------->消费者C2B");
        MyConsumer myConsumerC3 = new MyConsumer("----------->消费者C3");

        // 创建一个Disruptor对象
        Disruptor disruptor = new Disruptor(new ObjectEventFactory(),
                4, Executors.defaultThreadFactory(), ProducerType.SINGLE,
                new SleepingWaitStrategy());

        // 设置消费者依赖关系
        disruptor.handleEventsWith(myConsumerC1A, myConsumerC2A);
        disruptor.after(myConsumerC1A).then(myConsumerC1B);
        disruptor.after(myConsumerC2A).then(myConsumerC2B);
        disruptor.after(myConsumerC1B, myConsumerC2B).then(myConsumerC3);

        // 创建一个Disruptor队列操作类对象
        DisruptorQueue disruptorQueue = DisruptorQueueFactory.getQueue(disruptor);

        // 创建一个生产者，开始模拟生产数据
        MyProducerThread myProducerThread1 = new MyProducerThread("11111生产者1", disruptorQueue);
        Thread t1 = new Thread(myProducerThread1);
        t1.start();

        // 执行3s后，生产者不再生产
        Thread.sleep(3 * 1000);
        myProducerThread1.stopThread();
    }
}
