package cn.kingshin.rediscache.service.impl;

import cn.kingshin.rediscache.entity.SeckillVoucher;
import cn.kingshin.rediscache.mapper.SeckillVoucherMapper;
import cn.kingshin.rediscache.service.ISeckillVoucherService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 服务实现类
 * </p>
 */
@Service
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucher> implements ISeckillVoucherService {

}
