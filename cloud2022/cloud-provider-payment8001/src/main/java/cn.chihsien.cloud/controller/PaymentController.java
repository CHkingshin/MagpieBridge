package cn.chihsien.cloud.controller;


import cn.chihsien.cloud.service.PaymentService;
import cn.chihsien.commons.entities.CommonResult;
import cn.chihsien.commons.entities.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author KingShin
 */
@RestController
@Slf4j
public class PaymentController {
    @Resource
    private PaymentService paymentService;

    @RequestMapping(value = "/payment/create")
    public CommonResult<Payment> create(@RequestBody Payment payment) { //埋雷
        int result = paymentService.create(payment);
        log.info("*****插入结果：" + result);
        if (result > 0) {  //成功


            return new CommonResult(200, "插入数据库成功", result);
        } else {
            return new CommonResult(444, "插入数据库失败", null);
        }
    }

    @GetMapping(value = "/payment/get/{id}")
    public CommonResult<Payment> getPaymentById(@PathVariable("id") Long id) {
        Payment payment = paymentService.getPaymentById(id);
        log.info("*****查询结果：" + payment);
        if (payment != null) {  //说明有数据，能查询成功
            return new CommonResult(200, "查询成功", payment);
        } else {
            return new CommonResult(444, "没有对应记录，查询ID：" + id, null);
        }
    }

}
