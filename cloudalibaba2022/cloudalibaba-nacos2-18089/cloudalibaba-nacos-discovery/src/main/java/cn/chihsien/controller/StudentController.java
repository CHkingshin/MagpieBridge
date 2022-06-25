package cn.chihsien.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @describe
 * @auther KingShin
 */
@Slf4j
@RestController
@RequestMapping("/student")
public class StudentController {
    /**
     * 获取学生信息
     * @param id
     * @return
     */
    @GetMapping("/detail/{id}")
    public Object getStudentInfo(@PathVariable("id")Integer id){
        Map<String,Object> info = new HashMap<>();
        info.put("id",id);
        info.put("userName","test");
        info.put("nickName","小明");
        return info;
    }
}
