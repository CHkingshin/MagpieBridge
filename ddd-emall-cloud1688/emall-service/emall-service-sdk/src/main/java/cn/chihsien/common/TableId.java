package cn.chihsien.common;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * <h1>主键 ids</h1>
 *  前端请求到后端服务后创建用户地址 通常会返回数据表记录的id给用户 用户通过该id去查询用户地址信息
 *  所有的数据表都有这个ID主键字段 所以这个id是通用的
 *  这里单拧出来通过该对象进行包裹 不将该ID直接暴露给用户  还是对象的方式暴露出去 安全
 *  核心思想就是套娃 把一个高频字段包裹进一个对象来进行传输的时候  更利于后面的扩展（比如在该包裹类里添加字段要优于直接返回一个ID）
 *  所有的接口都应该返回一个对象 而不是一个单独的基础类型
 * @author KingShin
 */
@ApiModel(description = "通用 id 对象")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableId {

    @ApiModelProperty(value = "数据表记录主键")
    private List<Id> ids;
    /**
     * 将真正的ID字段进行存储
     */
    @ApiModel(description = "数据表记录主键对象")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id {
        @ApiModelProperty(value = "数据表记录主键")
        private Long id;
    }
}
