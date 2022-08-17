package cn.kingshin.rediscache.service.impl;

import cn.kingshin.rediscache.dto.Result;
import cn.kingshin.rediscache.entity.Follow;
import cn.kingshin.rediscache.mapper.FollowMapper;
import cn.kingshin.rediscache.service.IFollowService;
import cn.kingshin.rediscache.utils.UserHolder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    /**
     * @description 关注 取关
     * @param followUserId
     * @param isFollw
     * @return cn.kingshin.rediscache.dto.Result
     * @author KingShin
     * @date 2022/8/16 19:19:32
     */
    @Override
    public Result follow(Long followUserId, Boolean isFollw) {
        //获取当前登录的用户
        Long userId = UserHolder.getUser().getId();
        //1.判断到底是关注还是取关
        if(isFollw){
            //关注 新增当前用户与目标用户的关系
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            save(follow);
        }else {
            //取关 删除当前用户与目标用户的关系
            remove(new QueryWrapper<Follow>().eq("user_id",userId)
                    .eq("follow_user_id",followUserId));
        }
        return Result.ok();
    }

    @Override
    public Result isFollow(Long followUserId) {
        Long userId = UserHolder.getUser().getId();
        //查询是否关注
        Integer count = query().eq("user_id", userId)
                .eq("follow_user_id", followUserId).count();

        return Result.ok(count > 0);
    }
}
