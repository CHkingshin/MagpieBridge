package com.chihsien.service.order.service.impl;



import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chihsien.model.model.order.OrderDetail;
import com.chihsien.service.order.mapper.OrderDetailMapper;
import com.chihsien.service.order.service.OrderDetailService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单明细 订单明细 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2022-07-18
 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {

}
