package cn.chihsien.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <h1>登录用户信息</h1>
 *
 * @author KingShin*/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserInfo {

    /** 用户 id */
    private Long id;

    /** 用户名 */
    private String username;
}
