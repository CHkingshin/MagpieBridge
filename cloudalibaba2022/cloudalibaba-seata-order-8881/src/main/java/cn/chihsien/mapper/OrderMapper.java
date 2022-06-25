package cn.chihsien.mapper;

import org.apache.ibatis.annotations.Insert;

/**
 * @describe
 * @auther KingShin
 */
public interface OrderMapper {
    @Insert("insert into order_at(count) values (1)")
    void create();
}
