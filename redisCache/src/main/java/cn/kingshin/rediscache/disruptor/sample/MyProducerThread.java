package cn.kingshin.rediscache.disruptor.sample;

import cn.kingshin.rediscache.disruptor.DisruptorQueue;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * @description
 * 生产者 使用 disruptorQueue 对象的 add() 方法插入元素，
 *      当队列未满时，该方法会直接插入没有返回值；
 *      队列满时会阻塞等待，一直等到队列未满时再插入。
 * @author KingShin
 * @date 2022/8/15 19:26:43
 */
public class MyProducerThread implements Runnable {
    private String name;
    private DisruptorQueue disruptorQueue;
    private volatile boolean flag = true;
    private static AtomicInteger count = new AtomicInteger();

    public MyProducerThread(String name, DisruptorQueue disruptorQueue) {
        this.name = name;
        this.disruptorQueue = disruptorQueue;
    }

    @Override
    public void run() {
        try {
            System.out.println(now() + this.name + "：线程启动。");
            while (flag) {
                String data = count.incrementAndGet()+"";
                // 将数据存入队列中
                disruptorQueue.add(data);
                System.out.println(now() + this.name + "：存入" + data + "到队列中。");
            }
        } catch (Exception e) {

        } finally {
            System.out.println(now() + this.name + "：退出线程。");
        }
    }

    public void stopThread() {
        this.flag = false;
    }

    // 获取当前时间（分:秒）
    public String now() {
        Calendar now = Calendar.getInstance();
        return "[" + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND) + "] ";
    }
}