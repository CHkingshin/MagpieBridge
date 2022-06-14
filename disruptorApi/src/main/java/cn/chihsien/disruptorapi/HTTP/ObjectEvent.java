package cn.chihsien.disruptorapi.HTTP;

/**
 * @describe 事件对象类，里面包含要传递的数据：
 * @auther chihsiencheng
 */
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
