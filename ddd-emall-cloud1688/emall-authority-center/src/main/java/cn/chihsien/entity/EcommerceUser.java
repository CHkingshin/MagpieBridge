package cn.chihsien.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * <h1>用户表实体类定义</h1>
 *
 * @author KingShin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "t_ecommerce_user")
public class EcommerceUser implements Serializable {

    /**
     * 自增主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)//自增方式
    @Column(name = "id", nullable = false)//字段名称 不能为null
    private Long id;

    /**
     * 用户名
     */
    @Column(name = "username", nullable = false)
    private String username;

    /**
     * MD5 格式密码
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * 额外的信息, json 字符串存储
     */
    @Column(name = "extra_info", nullable = false)
    private String extraInfo;

    /**
     * 创建时间
     */
    @CreatedDate//JPA自动审计 启动类上加注解 JPA自动生成时间 不然会发生Null错误
    @Column(name = "create_time", nullable = false)
    private Date createTime;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "update_time", nullable = false)
    private Date updateTime;
}
