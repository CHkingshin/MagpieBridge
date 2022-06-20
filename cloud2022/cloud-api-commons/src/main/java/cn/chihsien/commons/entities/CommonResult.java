package cn.chihsien.commons.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author KingShin
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonResult<T> implements Serializable {

    private Integer code;
    private String message;
    private T data;


        public CommonResult(Integer code, String message) {
            this(code, message, null);
        }

}
