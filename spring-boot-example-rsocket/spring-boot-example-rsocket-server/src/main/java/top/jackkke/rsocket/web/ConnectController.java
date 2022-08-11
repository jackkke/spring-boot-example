package top.jackkke.rsocket.web;

import io.rsocket.RSocket;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;
import top.jackkke.redis.util.RedisService;
import top.jackkke.redis.util.RedissonService;
import top.jackkke.rsocket.constant.UploadConstants;

/**
 * @author jackkke
 * TCP 连接配置
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ConnectController {

  private final RedisService redisService;

  private final RedissonService redissonService;

  private static final String LOCK_PREFIX = "WindowLock:";
  private static final String CONNECT_NUMS_PREFIX = "ConnectNums:";


  @ConnectMapping
  @PreAuthorize("hasRole('USER')")
  Mono<Void> handle(@Headers Map<String, Object> metadata, RSocketRequester requester) {
    String clientId = metadata.get(UploadConstants.CLIENT_ID).toString();
    log.info("客户端 {} 准备建立连接 ……", clientId);
    if (requester == null || requester.rsocket() == null) {
      throw new RuntimeException("客户端 " + clientId + " 连接建立失败");
    }
    RSocket rsocket = requester.rsocket();
    Objects.requireNonNull(rsocket).onClose()
        .doFirst(() -> {
          if (check(clientId)) {
            log.error("Client: {} CONNECTED error 连接数不能大于10", clientId);
            throw new RuntimeException("客户端 " + clientId + " 连接建立失败: 连接数不能大于10");
          }
          log.info("Client: {} CONNECTED.", clientId);
        })
        .doOnError(error -> {
          log.warn("Channel to client {} error {}", clientId, error.getMessage());
          if (!rsocket.isDisposed()) {
            rsocket.dispose();
          }
        })
        .doFinally(consumer -> {
          try {
            reset(clientId);
            log.info("Client {} DISCONNECTED", clientId);
          } catch (Exception e) {
            throw new RuntimeException("客户端 " + clientId + " 连接销毁失败: " + e.getMessage());
          }
        })
        .doOnCancel(() -> log.error("Client: {} CONNECTED cancel ", clientId))
        .subscribe();
    return Mono.empty();
  }

  private boolean check(String clientId) {
    while (true) {
      RLock testWindowLock = redissonService.getRLock(getName(LOCK_PREFIX, clientId));
      if (lock(testWindowLock)) {
        try {
          String key = getName(CONNECT_NUMS_PREFIX, clientId);
          redisService.incr(key, 1);
          return redisService.get(key, Integer.class) > 10;
        } finally {
          testWindowLock.unlock();
        }
      }
    }
  }

  private String getName(String prefix, String clientId){
    return prefix + clientId;
  }

  private void reset(String clientId) {
    while (true) {
      RLock testWindowLock = redissonService.getRLock(getName(LOCK_PREFIX, clientId));
      if (lock(testWindowLock)) {
        try {
          String key = getName(CONNECT_NUMS_PREFIX, clientId);
          redisService.decr(key, 1);
          return;
        } finally {
          testWindowLock.unlock();
        }
      }
    }
  }

  private boolean lock(RLock lock) {
    try {
      // 等待 100 秒，获得锁 100 秒后自动解锁
      return lock.tryLock(100, 100, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return false;
  }
}
