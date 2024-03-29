package cn.kingshin.rediscache.disruptor.sample;

import cn.kingshin.rediscache.disruptor.ADisruptorConsumer;

import java.util.Calendar;
/**
 * @description
 * 消费者，每次获取到元素之后会等待个 1 秒钟，模拟实际业务处理耗时，也便于观察队列情况。
 * @author KingShin
 * @date 2022/8/15 19:27:34
 */
public class MyConsumer extends ADisruptorConsumer<String> {
    private String name;

    public MyConsumer(String name) {
        this.name = name;
    }

    public void consume(String data) {
        System.out.println(now() + this.name + "：拿到队列中的数据：" + data);
        //等待1秒钟
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 获取当前时间（分:秒）
    public String now() {
        Calendar now = Calendar.getInstance();
        return "[" + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND) + "] ";
    }
}