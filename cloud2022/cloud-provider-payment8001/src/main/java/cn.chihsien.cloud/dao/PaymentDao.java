package cn.chihsien.cloud.dao;


import cn.chihsien.commons.entities.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/**
 * @author KingShin
 * @describe
 */
@Component       //代替@Repository声明bean
@Mapper  //mybatis提供的，等价：@MapperScan("cn.chihsien.cloud.dao")
//@Repository     //spring提供的。在此，只是为了声明bean对象
public interface PaymentDao {

    public int create(Payment payment);
    public Payment getPaymentById(@Param("id") Long id);

}
