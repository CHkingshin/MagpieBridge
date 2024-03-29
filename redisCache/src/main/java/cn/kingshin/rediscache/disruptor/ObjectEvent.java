package cn.kingshin.rediscache.disruptor;

/**
 * 事件对象
 * @author KingShin*/
public class ObjectEvent<T> {
    private T obj;

    public ObjectEvent() {
    }

    public T getObj() {
        return this.obj;
    }

    public void setObj(T obj) {
        this.obj = obj;
    }
}