package cn.chihsien.controller;

import cn.chihsien.service.OrderService;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @describe
 * @auther KingShin
 */
@RestController
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GlobalTransactional
    @GetMapping("/order/create")
    public String create(){
        orderService.create();
        return "生成订单";
    }

}
