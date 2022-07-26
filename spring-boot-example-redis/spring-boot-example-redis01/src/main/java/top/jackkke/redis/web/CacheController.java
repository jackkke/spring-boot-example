package top.jackkke.redis.web;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * @author jackkke
 */
@RestController
@CacheConfig(cacheNames = "jackkke")
public class CacheController {

  @GetMapping("day")
  @Cacheable(value = "day#3", key = "#id")
  public String day(@RequestParam(value = "id") String id) {
    return id + "-" + LocalDateTime.now();
  }

  @GetMapping("hour")
  @Cacheable(value = "hour!3", key = "#id")
  public String hour(@RequestParam(value = "id") String id) {
    return id + "-" + LocalDateTime.now();
  }
}
