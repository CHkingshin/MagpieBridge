package cn.chihsien.dao;


import cn.chihsien.entity.EcommerceOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * <h1>EcommerceOrder Dao 接口定义</h1>
 * 继承JPA分页和排序的基类
 * @author KingShin
 */
public interface EcommerceOrderDao extends PagingAndSortingRepository<EcommerceOrder, Long> {

    /**
     * <h2>根据 userId 查询分页订单</h2>
     * select * from t_ecommerce_order where user_id = ? order by ... desc/asc limit x offset y
     */
    Page<EcommerceOrder> findAllByUserId(Long userId, Pageable pageable);
}
