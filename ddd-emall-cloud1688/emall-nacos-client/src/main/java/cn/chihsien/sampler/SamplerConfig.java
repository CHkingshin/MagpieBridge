package cn.chihsien.sampler;

import brave.sampler.RateLimitingSampler;
import brave.sampler.Sampler;
import org.springframework.cloud.sleuth.sampler.ProbabilityBasedSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <h2>使用配置的方式设定抽样率</h2>
 *
 * @author KingShin
 */
@Configuration
public class SamplerConfig {

    /**
     * <h2>限速采集</h2>
     */
//    @Bean
//    public Sampler sampler() {
//        return RateLimitingSampler.create(100);
//    }

//    /**
//     * <h2>概率采集, 默认的采样策略, 默认值是 0.1</h2>
//     * */
//    @Bean
//    public Sampler defaultSampler() {
//        return ProbabilityBasedSampler.create(0.5f);
//    }
}
