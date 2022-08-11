import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import top.jackkke.redis.limiter.window.SlidingWindowLimiter;
import top.jackkke.redis.limiter.window.SlidingWindowLimiterConfig;
import top.jackkke.redis.limiter.window.SlidingWindowLimiterFactory;
import top.jackkke.redis.util.RedisService;
import top.jackkke.redis.util.RedissonService;
import top.jackkke.rsocket.RsocketServerApplication;


@SpringBootTest(classes = RsocketServerApplication.class)
public class SlidingWindowLimiterFactoryTest {

  @Resource
  RedisService redisService;

  @Resource
  RedissonService redissonService;

  @Resource
  SlidingWindowLimiterFactory windowFactory;

  private static Map<Boolean, String> map = new ConcurrentHashMap<>();

  static {
    map.putIfAbsent(true, "passed");
    map.putIfAbsent(false, "failed");
  }

  @Test
  public void getSlidingWindowLimiter() {
    String key = "testSlidingWindow";
    SlidingWindowLimiterConfig config = new SlidingWindowLimiterConfig(
        key,
        2,
        10,
        redissonService.getRLock(key),
        redisService
    );
    SlidingWindowLimiter limiter = windowFactory.getSlidingWindowLimiter(config);
    System.out.println("Main thread " + map.get(limiter.acquire()) + " at first time."); // passed
    try {
      System.out.println("After sleep 500 millis--------");
      Thread.sleep(500);
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("Main thread " + map.get(limiter.acquire()) + " at second time."); // passed
    System.out.println("Main thread " + map.get(limiter.acquire()) + " at third time."); // failed
    try {
      System.out.println("After sleep 600 millis--------");
      Thread.sleep(600);
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("Main thread " + map.get(limiter.acquire()) + " at forth time."); // passed
    try {
      System.out.println("After sleep 500 millis--------");
      Thread.sleep(500);
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("Main thread " + map.get(limiter.acquire()) + " at fifth time."); // passed
    System.out.println("Main thread " + map.get(limiter.acquire()) + " at sixth time."); // failed
    System.out.println("Main thread " + map.get(limiter.acquire()) + " at seventh time."); // failed
    try {
      System.out.println("After sleep 1100 millis--------");
      Thread.sleep(1100);
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.out.println("Main thread " + map.get(limiter.acquire()) + " at eighth time."); // passed
    System.out.println("Main thread " + map.get(limiter.acquire()) + " at ninth time."); //passed
  }
}
