package top.jackkke.rsocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.jackkke.redis.limiter.window.SlidingWindowLimiterFactory;

/**
 * @author jackkke
 */
@Configuration
public class BeanConfig {

  @Bean
  public SlidingWindowLimiterFactory slidingWindowLimiterFactory() {
    return new SlidingWindowLimiterFactory();
  }
}
