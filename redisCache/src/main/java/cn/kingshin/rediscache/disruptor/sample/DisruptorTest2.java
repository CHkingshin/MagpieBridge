package cn.kingshin.rediscache.disruptor.sample;

import cn.kingshin.rediscache.disruptor.DisruptorQueue;
import cn.kingshin.rediscache.disruptor.DisruptorQueueFactory;
import cn.kingshin.rediscache.disruptor.ObjectEventFactory;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.concurrent.Executors;

public class DisruptorTest2 {
    public static void main(String[] args) throws InterruptedException {
        /**
         * 消费者依赖关系:
         * 消费者 C3 消费时，必须保证消费者 C1 和消费者 C2 已经完成对该消息的消费。
         * 举个例子，在处理实际的业务逻辑（C3）之前，需要校验数据（C1），以及将数据写入磁盘（C2）。
         * */
        // 创建两个消费者
        MyConsumer myConsumer1 = new MyConsumer("---->消费者C1");
        MyConsumer myConsumer2 = new MyConsumer("---->消费者C2");
        MyConsumer myConsumer3 = new MyConsumer("------->消费者C3");

        // 创建一个Disruptor对象
        Disruptor disruptor = new Disruptor(new ObjectEventFactory(),
                4, Executors.defaultThreadFactory(), ProducerType.SINGLE,
                new SleepingWaitStrategy());

        // 设置消费者依赖关系（先让C1和C2消费，再让C3消费）
        disruptor.handleEventsWith(myConsumer1, myConsumer2).then(myConsumer3);

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
