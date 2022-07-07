package cn.chihsien.disruptorApi;

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
