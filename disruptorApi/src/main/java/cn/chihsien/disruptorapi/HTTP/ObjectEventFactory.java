package cn.chihsien.disruptorapi.HTTP;

import com.lmax.disruptor.EventFactory;

/**
 * @describe 需要让 Disruptor 为我们创建事件，我们同时还声明了一个 EventFactory 来实例化 Event 对象：
 *           Disruptor 通过 EventFactory 在 RingBuffer 中预创建 Event 的实例。
 *           一个 Event 实例实际上被用作一个“数据槽”，发布者发布前，先从 RingBuffer 获得一个 Event 的实例，
 *           然后往 Event 实例中填充数据，之后再发布到 RingBuffer 中，
 *           之后由 Consumer 获得该 Event 实例并从中读取数据。
 * @auther chihsiencheng
 */
public class ObjectEventFactory<T> implements EventFactory<ObjectEvent> {

    public ObjectEventFactory() {
    }

    @Override
    public ObjectEvent newInstance() {
        return new ObjectEvent();
    }
}
