package cn.kingshin.rediscache.service.impl;

import cn.kingshin.rediscache.dto.Result;
import cn.kingshin.rediscache.entity.SeckillVoucher;
import cn.kingshin.rediscache.entity.VoucherOrder;
import cn.kingshin.rediscache.mapper.VoucherOrderMapper;
import cn.kingshin.rediscache.service.ISeckillVoucherService;
import cn.kingshin.rediscache.service.IVoucherOrderService;
import cn.kingshin.rediscache.utils.RedisIdWorker;
import cn.kingshin.rediscache.utils.UserHolder;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 * @author KingShin
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {
    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public Result seckillVoucher(Long voucherId) {
        //查询优惠券信息
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
        //判断是否在秒杀时间内
        if (voucher.getBeginTime().isAfter(LocalDateTime.now())) {
            return Result.fail("秒杀活动尚未开始！");
        }
        if (voucher.getEndTime().isBefore(LocalDateTime.now())) {
            return Result.fail("秒杀活动已经结束！");
        }
        //库存是否充足
        if(voucher.getStock()<1){
            return Result.fail("库存不足！");
        }

        //用户id
        Long userId = UserHolder.getUser().getId();
        //创建锁对象 拼接业务标识 保证每个线程进来携带key的唯一性 保证锁的是用户 "order:" + userId 就是我们要锁的对象
        //SimpleRedisLock lock = new SimpleRedisLock("order:" + userId, stringRedisTemplate);
        RLock lock = redissonClient.getLock("lock:order:" + userId);
        //获取锁
        boolean islock = lock.tryLock();//失败不等待
        //判断是否获得到锁  一般先写反 避免后续代码嵌套判断未获得锁
        if(!islock){
            //拿锁失败 返回错误或重试 这里能进来还是一个用户 肯定是jio本 直接封杀！
            return Result.fail("小逼崽子喜欢开脚本是吧？不允许重复下单！再次查获直接封IP");
        }

        try {
            //获取代理对象（事务）
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.creatVoucherOder(voucherId);
        } finally {
            //释放锁
            lock.unlock();
        }


        /**  用synchronized的版本 可以参考 但是效果极差
         * 细粒度的锁 锁常量保证请求来的ID是一个用户的
         * intern->返回字符串对象的规范表示:
         * 去常量池里找和userId一样的引用或者地址返回
         * 这样不论userId来多少次只要是同一个值那都是同一个常量池的引用 达到只锁当前用户的目的
         * 这里不能放在creatVoucherOder里去加锁！！！
         * 在加了@Transactional之后事务的提交是交给spring执行的
         * 这样在锁ID之后 订单创建返回订单ID之后锁就释放了  其他的线程就可以进来
         * 如果此时订单还没落到DB上 那新进来的线程查到的订单数据是还没写进DB的订单数据
         * 应该锁在事务提交之后 这样函数结束之后 订单必定已经落在DB上！
         */
      /*  synchronized (userId.toString().intern()){
            *//** 获取代理对象（事务）保证事务提交之后释放锁
                注意这里的creatVoucherOder方法是当前对象VoucherOrderServiceImpl 即this调用的
                并非代理对象 这样会导致@Transactional失效
                记得加入aspectjweaver依赖
                同时在启动类上暴露代理对象 @EnableAspectJAutoProxy(exposeProxy = true)
            *//*
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.creatVoucherOder(voucherId);
        }*/
    }
    /**
     * @description 创建订单逻辑
     * @param voucherId
     * @return cn.kingshin.rediscache.dto.Result
     * @author KingShin
     * @date 2022/8/5 04:33:01
     */
    @Transactional
    public Result creatVoucherOder(Long voucherId) {
        //一人一单
        //用户id
        Long userId = UserHolder.getUser().getId();
        //查询订单数量
        int count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        //判断是否存在
        if(count > 0){
            //用户已经购买过了
            return Result.fail("您已购买过一次！");
        }
        //扣减库存
        boolean success = seckillVoucherService
                .update()
                .setSql("stock = stock - 1")//set stock =stock -1
                .eq("voucher_id", voucherId).gt("stock",0) //where id = ? and stock >0 只要库存大于0就继续卖
                .update();
        if(!success){
            //扣减库存失败
            return Result.fail("库存不足！");
        }
        // 没买过 再允许他创建订单
        VoucherOrder voucherOrder = new VoucherOrder();
        //返回订单id
        //生成全局唯一ID
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setId(orderId);
        voucherOrder.setUserId(userId);
        //代金券id
        voucherOrder.setVoucherId(voucherId);
        save(voucherOrder);
        //返回订单Id
        return Result.ok(orderId);
    }
}
