package com.chihsien.service.order.service.impl;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chihsien.model.model.order.OrderDetail;
import com.chihsien.model.model.order.OrderInfo;
import com.chihsien.model.vo.order.OrderInfoQueryVo;
import com.chihsien.service.order.mapper.OrderInfoMapper;
import com.chihsien.service.order.service.OrderDetailService;
import com.chihsien.service.order.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单表 订单表 服务实现类
 * </p>
 *
 * @author atguigu
 * @since 2022-07-18
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {
    @Autowired
    private OrderDetailService orderDetailService;//订单详情service

    /**
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @describe 订单列表
     * @param: pageParam
     * @param: orderInfoQueryVo
     * @author KingShin<br>
     * @version
     */
    @Override
    public Map<String, Object> findPageOrderInfo(Page<OrderInfo> pageParam,
                                                 OrderInfoQueryVo orderInfoQueryVo) {
        //orderInfoQueryVo获取查询条件
      /*  Long userId = orderInfoQueryVo.getUserId();//ID
        String outTradeNo = orderInfoQueryVo.getOutTradeNo();//交易号
        String phone = orderInfoQueryVo.getPhone();//交易手机号
        String createTimeEnd = orderInfoQueryVo.getCreateTimeEnd();//订单开始时间
        String createTimeBegin = orderInfoQueryVo.getCreateTimeBegin();//订单结束时间
        Integer orderStatus = orderInfoQueryVo.getOrderStatus();//订单状态*/

        //判断条件值是否为空，不为空，进行条件封装
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(orderInfoQueryVo.getOrderStatus())) {
            wrapper.eq("order_status", orderInfoQueryVo.getOrderStatus());
        }
        if (!StringUtils.isEmpty(orderInfoQueryVo.getUserId())) {
            wrapper.eq("user_id", orderInfoQueryVo.getUserId());
        }
        if (!StringUtils.isEmpty(orderInfoQueryVo.getOutTradeNo())) {
            wrapper.eq("out_trade_no", orderInfoQueryVo.getOutTradeNo());
        }
        if (!StringUtils.isEmpty(orderInfoQueryVo.getPhone())) {
            wrapper.eq("phone", orderInfoQueryVo.getPhone());
        }
        if (!StringUtils.isEmpty(orderInfoQueryVo.getCreateTimeBegin())) {
            wrapper.ge("create_time", orderInfoQueryVo.getCreateTimeBegin());
        }
        if (!StringUtils.isEmpty(orderInfoQueryVo.getCreateTimeEnd())) {
            wrapper.le("create_time", orderInfoQueryVo.getCreateTimeEnd());
        }
        //调用实现条件分页查询
        Page<OrderInfo> pages = baseMapper.selectPage(pageParam, wrapper);
        long totalCount = pages.getTotal();//总记录
        long pageCount = pages.getPages();//总页数
        List<OrderInfo> records = pages.getRecords();
        //订单里面包含详情内容，封装详情数据，根据订单id查询详情
        records.forEach(this::getOrderDetail);

        //所有需要数据封装map集合，最终返回
        Map<String, Object> map = new HashMap<>();
        map.put("total", totalCount);
        map.put("pageCount", pageCount);
        map.put("records", records);
        return map;
    }

    //查询订单详情数据
    private OrderInfo getOrderDetail(OrderInfo orderInfo) {
        //订单id
        Long id = orderInfo.getId();
        //查询订单详情
        OrderDetail orderDetail = orderDetailService.getById(id);
        if (orderDetail != null) {
            String courseName = orderDetail.getCourseName();
            orderInfo.getParam().put("courseName", courseName);
        }
        return orderInfo;
    }


}
