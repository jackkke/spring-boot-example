package top.jackkke.redis.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.jackkke.redis.util.RedisService;

/**
 * @author jackkke
 */
@RestController
@RequiredArgsConstructor
public class TestController {

    private final RedisService redisService;

    @GetMapping("hello")
    public String hello() {
        String test = redisService.get("test", String.class);
        System.out.println("test = " + test);
        return test;
    }
}
