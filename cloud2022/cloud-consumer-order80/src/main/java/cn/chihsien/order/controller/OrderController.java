package cn.chihsien.order.controller;

import cn.chihsien.commons.entities.CommonResult;
import cn.chihsien.commons.entities.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * @describe
 * @auther KingShin
 */
@RestController
@Slf4j
public class OrderController {
    public static final String PAYMENT_URL = "http://localhost:8888";
    @Resource
    private RestTemplate restTemplate;//远程调用


    @RequestMapping("/consumer/payment/create")
    public CommonResult<Payment>   create(@RequestBody Payment payment){
        return restTemplate.postForObject(PAYMENT_URL+"/payment/create",payment, CommonResult.class);  //写操作
    }

    @RequestMapping("/consumer/payment/get/{id}")
    public CommonResult<Payment> getPayment(@PathVariable("id") Long id){
        return restTemplate.getForObject(PAYMENT_URL+"/payment/get/"+id,CommonResult.class);
    }


}
