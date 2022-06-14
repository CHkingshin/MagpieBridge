package cn.chihsien.disruptorApi;

import com.lmax.disruptor.RingBuffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @describe 构造Mqservice和实现类-生产者
 * @auther chihsiencheng
 */
public interface DisruptorMqService  {
    /**
     * 消息
     * @param message
     */
    void sayHelloMq(String message);
}
