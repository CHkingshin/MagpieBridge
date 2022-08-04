package cn.kingshin.rediscache.service;

import cn.kingshin.rediscache.dto.Result;
import cn.kingshin.rediscache.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 * @author KingShin
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    Result seckillVoucher(Long voucherId);

    Result creatVoucherOder(Long voucherId);
}
