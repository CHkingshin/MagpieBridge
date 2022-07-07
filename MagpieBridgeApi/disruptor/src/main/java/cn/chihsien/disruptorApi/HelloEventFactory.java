package cn.chihsien.disruptorApi;

import com.lmax.disruptor.EventFactory;

/**
 * @describe 构造EventFactory
 * @auther chihsiencheng
 */
public class HelloEventFactory implements EventFactory<MessageModel> {
    @Override
    public MessageModel newInstance() {
        return new MessageModel();
    }
}