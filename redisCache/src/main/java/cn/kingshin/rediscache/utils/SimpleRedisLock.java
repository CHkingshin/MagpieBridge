package cn.kingshin.rediscache.utils;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.BooleanUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class SimpleRedisLock implements ILock {

    private String name;//指定锁的名称 不同的线程业务有独自的名称
    private StringRedisTemplate stringRedisTemplate;

    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    private static final String KEY_PREFIX = "lock:";//锁名前缀
    private static final String ID_PREFIX = UUID.randomUUID().toString(true) + "-";
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;//定义脚本
    //lua脚本初始化
    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));//脚本位置
        UNLOCK_SCRIPT.setResultType(Long.class);//返回值
    }

    @Override
    public boolean tryLock(long timeoutSec) {
        // 获取线程标示 线程ID+UUID 保证标识唯一性
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        // 获取锁
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + name, threadId, timeoutSec, TimeUnit.SECONDS);
        //spring帮我们封装了返回结果,boolean对应这nil和ok 但是！success是包装类 返回的是boolean 会自动拆箱 得做个判断
        return BooleanUtil.isTrue(success); //hutool的底层其实也就是 Boolean.TRUE.equals
        // return Boolean.TRUE.equals(success);
    }

    @Override   //lua脚本中进行了判断和删除 整个操作是一个原子操作 不存在并发安全问题
    public void unlock() {
        // 调用lua脚本 实际生产中lua脚本的维护十分令人难受 在redis7.0版本中用函数取代了Lua
        stringRedisTemplate.execute(
                UNLOCK_SCRIPT,//Lua脚本
                Collections.singletonList(KEY_PREFIX +name),//锁的key的集合
                ID_PREFIX + Thread.currentThread().getId()//线程标识
                );
    }
    //传统做法 操作之间不是原子操作 阻塞会导致线程安全问题
   /* @Override
    public void unlock() {

        // 获取线程标示
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        // 获取锁中的标示
        String id = stringRedisTemplate.opsForValue().get(KEY_PREFIX + name);
        // 判断标示是否一致
        if(threadId.equals(id)) {
            // 释放锁
            stringRedisTemplate.delete(KEY_PREFIX + name);
        }
    }*/
}
