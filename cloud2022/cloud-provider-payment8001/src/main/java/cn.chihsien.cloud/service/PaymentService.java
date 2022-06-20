package cn.chihsien.cloud.service;


import cn.chihsien.commons.entities.Payment;

/**
 * @describe
 * @auther KingShin
 */
public interface PaymentService {
    public int create(Payment payment);//写
    public Payment getPaymentById(Long id);  //读取
}
