package cn.kingshin.rediscache.service.impl;

import cn.kingshin.rediscache.entity.Blog;
import cn.kingshin.rediscache.mapper.BlogMapper;
import cn.kingshin.rediscache.service.IBlogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

}
