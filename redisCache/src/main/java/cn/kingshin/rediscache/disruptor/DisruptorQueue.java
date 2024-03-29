package cn.kingshin.rediscache.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import java.util.Iterator;
import java.util.List;
/**
 * @description
 * Disruptor 队列操作工具类 DisruptorQueue，用于初始化 disruptor 以及 ringBuffer 对象，并封装类一些常用的方法
 * @author KingShin
 * @date 2022/8/15 19:20:26
 */
public class DisruptorQueue<T> {
    private Disruptor<ObjectEvent<T>> disruptor;
    private RingBuffer<ObjectEvent<T>> ringBuffer;

    public DisruptorQueue(Disruptor<ObjectEvent<T>> disruptor) {
        this.disruptor = disruptor;
        this.ringBuffer = disruptor.getRingBuffer();
        this.disruptor.start();
    }

    public void add(T t) {
        if (t != null) {
            long sequence = this.ringBuffer.next();

            try {
                ObjectEvent<T> event = (ObjectEvent)this.ringBuffer.get(sequence);
                event.setObj(t);
            } finally {
                this.ringBuffer.publish(sequence);
            }
        }
    }

    public void addAll(List<T> ts) {
        if (ts != null) {
            Iterator<T> var2 = ts.iterator();

            while(var2.hasNext()) {
                T t = var2.next();
                if (t != null) {
                    this.add(t);
                }
            }
        }
    }

    public long cursor() {
        return this.disruptor.getRingBuffer().getCursor();
    }

    public void shutdown() {
        this.disruptor.shutdown();
    }

    public Disruptor<ObjectEvent<T>> getDisruptor() {
        return this.disruptor;
    }

    public void setDisruptor(Disruptor<ObjectEvent<T>> disruptor) {
        this.disruptor = disruptor;
    }

    public RingBuffer<ObjectEvent<T>> getRingBuffer() {
        return this.ringBuffer;
    }

    public void setRingBuffer(RingBuffer<ObjectEvent<T>> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }
}