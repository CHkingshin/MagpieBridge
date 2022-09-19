package cn.chihsien.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * <h1>商品状态枚举类</h1>
 *
 * @author KingShin
 */
@Getter
@AllArgsConstructor
public enum GoodsStatus {

    ONLINE(101, "上线"),
    OFFLINE(102, "下线"),
    STOCK_OUT(103, "缺货"),
    ;

    /**
     * 状态码
     */
    private final Integer status;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * <h2>根据 code 获取到 GoodsStatus</h2>
     */
    public static GoodsStatus of(Integer status) {

        Objects.requireNonNull(status);

        return Stream.of(values())//将数组变成Stream对象（GoodsStatus）
                .filter(bean -> bean.status.equals(status))//对每一个枚举对象进行对比
                .findAny() //找到任何一个
                .orElseThrow(
                        //找不到任何匹配得上的枚举对象则抛出异常
                        () -> new IllegalArgumentException(status + " not exists")
                );
    }
}
