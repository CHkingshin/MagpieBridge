package cn.chihsien.controller;

import cn.chihsien.commons.entities.CommonResult;
import cn.chihsien.commons.entities.Payment;
import cn.chihsien.service.PaymentFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @describe
 * @auther KingShin
 */
@RestController
public class OrderFeignController {

    @Autowired
    PaymentFeignService paymentFeignService; //Feign接口，用于远程调用。

    @GetMapping("/consumer/payment/get/{id}")
    public CommonResult get(@PathVariable("id") Long id){
        return paymentFeignService.getPaymentById(id);
    }

    @PostMapping("/consumer/payment/create")
    public CommonResult create(@RequestBody Payment payment){
        CommonResult result = paymentFeignService.create(payment);

        return result;
    }
}
