package cn.chihsien.cloud.service.impl;

import cn.chihsien.cloud.dao.PaymentDao;
import cn.chihsien.cloud.service.PaymentService;
import cn.chihsien.commons.entities.Payment;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author KingShin
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    //@Autowired
    @Resource
    PaymentDao paymentDao;

    @Override
    public int create(Payment payment) {
        return paymentDao.create(payment);
    }

    @Override
    public Payment getPaymentById(Long id) {
        return paymentDao.getPaymentById(id);
    }
}
