package cn.kingshin.rediscache.controller;


import cn.kingshin.rediscache.dto.Result;
import cn.kingshin.rediscache.service.IFollowService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 *  前端控制器
 * </p>
 * @author KingShin
 */
@RestController
@RequestMapping("/follow")
public class FollowController {
    @Resource
    private IFollowService followService;

    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable("id") Long followUserId,@PathVariable("isFollow") Boolean isFollw){
        return followService.follow(followUserId,isFollw);
    }

    @GetMapping("/or/not/{id}")
    public Result isFollow(@PathVariable("id") Long followUserId){
        return followService.isFollow(followUserId);

    }

    @GetMapping("/common/{id}")
    public Result followCommons(@PathVariable("id") Long id){
        return followService.followCommons(id);

    }

}
