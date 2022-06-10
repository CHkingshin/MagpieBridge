package cn.chihsien.controller;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @describe
 * @auther CHkingshin
 */
@RestController
public class GoodController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    public static final String KEY = "CHkingshin";
    @Value("${server.port}")
    private String serverPort;
    //redisson实现分布式锁
    @Autowired
    private Redisson redisson;
    @GetMapping("/buy_goods")
    public String buy_Goods() {
        RLock redissonLock = redisson.getLock(KEY);
        redissonLock.lock();
        try {
            String result = stringRedisTemplate.opsForValue().get("goods:001");
            int goodsNumber = result == null ? 0 : Integer.parseInt(result);
            if (goodsNumber > 0) {
                int realNumber = goodsNumber - 1;
                stringRedisTemplate.opsForValue().set("goods:001", realNumber + "");
                System.out.println("你已经成功秒杀商品，此时还剩余：" + realNumber + "件" + "\t 服务器端口：" + serverPort);
                return "你已经成功秒杀商品，此时还剩余：" + realNumber + "件" + "\t 服务器端口：" + serverPort;
            } else {
                System.out.println("商品已经售罄/活动结束/调用超时，欢迎下次光临" + "\t 服务器端口：" + serverPort);
            }
            return "商品已经售罄/活动结束/调用超时，欢迎下次光临" + "\t 服务器端口：" + serverPort;
        } finally {
            //避免高QPS乱入锁，判断是否是当前线程还在持有锁
             if ( redissonLock.isLocked()&&redissonLock.isHeldByCurrentThread()){
                 redissonLock.unlock();
             }
        }
    }
}
