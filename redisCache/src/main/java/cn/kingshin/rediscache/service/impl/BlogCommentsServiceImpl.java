package cn.kingshin.rediscache.service.impl;

import cn.kingshin.rediscache.entity.BlogComments;
import cn.kingshin.rediscache.mapper.BlogCommentsMapper;
import cn.kingshin.rediscache.service.IBlogCommentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

}
