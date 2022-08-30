package cn.chihsien.advice;


import cn.chihsien.vo.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * <h2>全局异常捕获处理</h2>
 * 让代码即便发生错误也返回CommonResponse
 * @author KingShin
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {
    /***
     * @description 让代码即便发生错误也返回CommonResponse
     * @param req
     * @param ex
     * @return cn.chihsien.vo.CommonResponse<java.lang.String>
     * @author KingShin
     * @date 2022/8/30 02:15:29
     */
    @ExceptionHandler(value = Exception.class)
    public CommonResponse<String> handlerCommerceException(
            HttpServletRequest req, Exception ex
    ) {

        CommonResponse<String> response = new CommonResponse<>(
                -1, "systeam error"
        );
        response.setData(ex.getMessage());
        log.error("commerce service has error: [{}]", ex.getMessage(), ex);//ex 把异常栈也log
        return response;
    }
}
