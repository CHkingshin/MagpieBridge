package cn.chihsien.disruptorapi.HTTP;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;

/**
 * @describe 定义一个消费者抽象类，后面我们所有自定义的消费者都需要继承这个抽象类，
 * 并实现 consume 方法（对获取的数据进行业务处理）：
 * @auther chihsiencheng
 */
public abstract class ADisruptorConsumer<T> implements EventHandler<ObjectEvent<T>>, WorkHandler<ObjectEvent<T>> {
    public ADisruptorConsumer() {
    }

    public void onEvent(ObjectEvent<T> event, long sequence, boolean endOfBatch) throws Exception {
        this.onEvent(event);
    }

    public void onEvent(ObjectEvent<T> event) throws Exception {
        this.consume(event.getObj());
    }

    public abstract void consume(T var1);
}
