package cn.kingshin.rediscache.dto;

import lombok.Data;

import java.util.List;

/**
 * @author KingShin
 * 滚动分页结果返回类
 */
@Data
public class ScrollResult {
    private List<?> list;//元素不指定 以后查其他的也能用
    private Long minTime;//上一次的时间脚标 最小的时间
    private Integer offset;//偏移量
}
