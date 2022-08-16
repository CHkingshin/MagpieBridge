package cn.kingshin.rediscache.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.kingshin.rediscache.dto.Result;
import cn.kingshin.rediscache.entity.Shop;
import cn.kingshin.rediscache.mapper.ShopMapper;
import cn.kingshin.rediscache.service.IShopService;
import cn.kingshin.rediscache.utils.BloomFilterUtil;
import cn.kingshin.rediscache.utils.CacheClient;
import cn.kingshin.rediscache.utils.RedisData;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static cn.kingshin.rediscache.utils.RedisConstants.*;


/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author KingShin
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    public static long size = CACHE_BLOOMFILTER_SIZE;// 预期插入数量
    public static double fpp = CACHE_BLOOMFILTER_FPP; // 误判率


    private RBloomFilter<Long> bloomFilter = null;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private BloomFilterUtil bloomFilterUtil;//此处可以换成hutool的工具类 提供了封装成map的方法

    @Resource
    private CacheClient cacheClient;


    @PostConstruct // 项目启动的时候执行该方法，也可以理解为在spring容器初始化的时候执行该方法
    public void init() {
        // 启动项目时初始化bloomFilter
        List<Shop> shopList = this.list();
        bloomFilter = bloomFilterUtil.create("idWhiteList", size, fpp);
        shopList.forEach(shop ->
                bloomFilter.add(shop.getId())
        );
    }

    @Override
    public Result queryById(Long id) {
        //缓存穿透
        Shop shop = cacheClient.queryWithPassThrough(CACHE_SHOP_KEY,id,Shop.class,this::getById,CACHE_SHOP_TTL,TimeUnit.MINUTES);

        //互斥锁解决缓存击穿
        //Shop shop = quryWithMutex(id);

        //逻辑过期解决缓存击穿问题
        //Shop shop = cacheClient.queryWithLogicalExpire(CACHE_SHOP_KEY,id,Shop.class,this::getById,20L,TimeUnit.MINUTES);

        if (shop == null) {
            return Result.fail("店铺不存在！");
        }

        return Result.ok(shop);
    }

    /***
     * @description redis缓存setnx尝试获取锁
     * @param key
     * @return boolean
     * @author KingShin
     * @date 2022/8/3 15:20:02
     */
    private boolean tryLock(String key) {
        //setIfAbsent（如果存在）->setnx  value是setnx写入到redis里的数据  设置过期时间为10s
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    /***
     * @description redis缓存setnx删除锁
     * @param key
     * @return void
     * @author KingShin
     * @date 2022/8/3 15:19:49
     */
    private void unLock(String key) {
        stringRedisTemplate.delete(key);
    }

    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    /**
     * @description 逻辑过期解决缓存击穿问题
     * @param id
     * @return cn.kingshin.rediscache.entity.Shop
     * @author KingShin
     * @date 2022/8/4 23:25:09
     */
    public Shop quryWithLogicalExpire(Long id) {
        String key = CACHE_SHOP_KEY + id;
        //先查redis
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //命中 取
        if (StrUtil.isBlank(shopJson)) {
            return null;
        }
        //命中 判断是否过期 把json反序列化为对象
        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
        Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
        //LocalDateTime expireTime = redisData.getExpireTime();
        //判断是否过期
        if(redisData.getExpireTime().isAfter(LocalDateTime.now())){
            //未过期 直接返回
            return shop;
        }
        //已过期 重建缓存
        //获取互斥锁
        String lockKey = LOCK_SHOP_KEY + id;
        boolean isLock = tryLock(lockKey);
        //判断是否获取锁成功 再次判断缓存是否过期
        if(isLock ){
            //再次判断缓存是否过期
            if(redisData.getExpireTime().isBefore(LocalDateTime.now())){
                //成功 开启独立线程
                CACHE_REBUILD_EXECUTOR.submit(()->{
                    try {
                        this.saveShop2Redis(id,20L);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    } finally {
                        //释放锁
                        unLock(lockKey);
                    }
                });
            }
        }
        //返回过期的
         return shop;
    }
    /***
     * @description 缓存穿透 返回null的方式
     * @param id
     * @return cn.kingshin.rediscache.entity.Shop
     * @author KingShin
     * @date 2022/8/3 15:17:23
     */
    public Shop quryWithPassThrough(Long id) {
        String key = CACHE_SHOP_KEY + id;
        //先查redis
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //命中 取
        if (StrUtil.isNotBlank(shopJson)) {
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        //命中空值
        if (shopJson != null) {
            //返回一个错误信息
            return null;
        }
        //未命中 去DB查
        Shop shop = getById(id);
        //DB无 返回错误
        if (shop == null) {
            //设置null的方法：将null值写入redis
            stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        //DB有 则回写redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        return shop;
    }

    /**
     * @param id
     * @return cn.kingshin.rediscache.entity.Shop
     * @description 互斥锁缓存击穿
     * @author KingShin
     * @date 2022/8/3 15:24:17
     */
    public Shop quryWithMutex(Long id) {
        String key = CACHE_SHOP_KEY + id;
        //先查redis是否有缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //命中 取
        if (StrUtil.isNotBlank(shopJson)) {
            return JSONUtil.toBean(shopJson, Shop.class);
        }
        //命中空值
        if (shopJson != null) {
            //返回null
            return null;
        }

        //实现缓存重建  获取互斥锁->判断是否获取成功->失败则休眠并重试
        //根据id给每个店铺配一个锁 注意这里不是上面从redis里查出来的key
        String lockkey = "lock:shop:" + id;
        //拿锁
        Shop shop = null;
        try {
            boolean islock = tryLock(lockkey);
            if (!islock) {
                Thread.sleep(50);//获取不成功休眠50ms
                return quryWithPassThrough(id);//休眠完后递归再次查询
            }
            //成功拿锁 去DB查
            shop = getById(id);
            Thread.sleep(200);//模拟并发线程阻塞导致的延时
            //判断redis是否已经有缓存了，有则不重建缓存
            if (shopJson != null) {
                //返回一个错误信息
                return null;
            }
            //DB无 返回null
            if (shop == null) {
                //设置null的方法：将null值写入redis
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            //DB有 则回写redis
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            //释放互斥锁
            unLock(lockkey);
        }
        //返回
        return shop;
    }

    /**
     * @param id
     * @return cn.kingshin.rediscache.entity.Shop
     * @description TODO  布隆过滤器缓存穿透  未完成
     * @author KingShin
     * @date 2022/8/3 15:22:15
     */
    public Shop bloomFilterThrough(Long id) {
        if (id == null) {
            return null;
        }

        String key = CACHE_SHOP_KEY + id;
        //布隆过滤器查，又再进redis查，没有进DB查，还没有就返回null
        if (bloomFilter.contains(id)) {
            //过滤器里有 再进redsi
            String shopJson = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(shopJson)) {
                Shop shop = JSONUtil.toBean(shopJson, Shop.class);
                bloomFilter.add(shop.getId());
                return shop;

            }
            if (shopJson != null) {
                //返回一个错误信息
                return null;
            }

            //redis里没有 查数据库
            Shop shop = getById(id);
            //不存在 返回错误
            if (shop == null) {
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            //DB存在  回写
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
            bloomFilter.add(shop.getId());
            //返回
            return null;

        }
        return null;
    }

    /**
     * @description 批量添加店铺进redis预热
     * @param id                  店铺id
     * @param expireSeconds      过期时间
     * @return void
     * @author KingShin
     * @date 2022/8/4 23:13:46
     */
    public void saveShop2Redis(Long id,Long expireSeconds) throws InterruptedException {
        //1.查询店铺数据
        Shop shop = getById(id);
        Thread.sleep(200);
        //2.封装逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        //3.写入redis
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY +id,JSONUtil.toJsonStr(redisData));
    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id不存在！");
        }
        //更新数据库
        updateById(shop);
        //删除缓存
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);

        return Result.ok();
    }
}
