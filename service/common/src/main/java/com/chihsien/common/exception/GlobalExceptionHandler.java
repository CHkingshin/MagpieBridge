package com.chihsien.common.exception;


import com.chihsien.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author KingShin
 * 异常处理
 * 异常AOP进入该类
 * 如果有对应类型的异常则返回对应类型异常 如果没有则返回全局异常处理
 */
@ControllerAdvice  //aop
public class GlobalExceptionHandler {

    /**
     * @return com.atguigu.ggkt.result.Result
     * @describe 全局异常处理
     * @param: e
     * @author KingShin<br>
     * @version
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e) {
        System.out.println("全局.....");
        e.printStackTrace();
        return Result.fail(null).message("执行全局异常处理");
    }

    /**
     * @describe
     * 特定异常处理ArithmeticException
     * @param: e
     * @return com.atguigu.ggkt.result.Result
     * @author KingShin<br>
     * @version
     */
    @ExceptionHandler(ArithmeticException.class)
    @ResponseBody
    public Result error(ArithmeticException e) {
        System.out.println("特定.....");
        e.printStackTrace();
        return Result.fail(null).message("执行ArithmeticException异常处理");
    }

    /**
     * @describe
     * 自定义异常处理GgktException
     * @param: e
     * @return com.atguigu.ggkt.result.Result
     * @author KingShin<br>
     * @version
     */
    @ExceptionHandler(GgktException.class)
    @ResponseBody
    public Result error(GgktException e) {
        e.printStackTrace();
        return Result.fail(null).code(e.getCode()).message(e.getMsg());
    }

}
