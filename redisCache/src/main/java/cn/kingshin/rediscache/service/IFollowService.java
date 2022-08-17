package cn.kingshin.rediscache.service;

import cn.kingshin.rediscache.dto.Result;
import cn.kingshin.rediscache.entity.Follow;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 */
public interface IFollowService extends IService<Follow> {

    Result follow(Long followUserId, Boolean isFollw);

    Result isFollow(Long followUserId);
}
