package cn.chihsien.converter;


import cn.chihsien.constant.GoodsStatus;

import javax.persistence.AttributeConverter;

/**
 * <h1>商品状态枚举属性转换器</h1>
 *  DB实体类和枚举的相互转换
 *  写这种转换器 关键是确定对应的泛型
 *  将我们DB里的实体数据GoodsStatus转换成我们想要存储进表里的类型 在这里是Integer 因为在GoodsStatus定义的状态码是Integer类型的
 * @author KingShin
 */
public class GoodsStatusConverter implements AttributeConverter<GoodsStatus, Integer> {

    /**
     * <h2>转换成可以存入数据表的基本类型</h2>
     */
    @Override
    public Integer convertToDatabaseColumn(GoodsStatus goodsStatus) {
        return goodsStatus.getStatus();
    }

    /**
     * <h2>还原数据表中的字段值到 Java 数据类型</h2>
     */
    @Override
    public GoodsStatus convertToEntityAttribute(Integer status) {
        return GoodsStatus.of(status);
    }
}
