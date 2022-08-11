package top.jackkke.rsocket.web;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import top.jackkke.rsocket.RouterMapping;
import top.jackkke.rsocket.dto.Message;

/**
 * @author jackkke
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class RsocketController {

  @MessageMapping(RouterMapping.REQUEST_RESPONSE_STR)
  public String str(String str) {
    log.info("Received {} request: {}", RouterMapping.REQUEST_RESPONSE_STR, str);
    return "str [" + str + "] 接收成功";
  }

  @MessageMapping(RouterMapping.REQUEST_RESPONSE_MESSAGE)
  public Message message(Message message) {
    log.info("Received {} request: {}", RouterMapping.REQUEST_RESPONSE_MESSAGE, message);
    return new Message("SERVER", "RESPONSE");
  }

  @MessageMapping("client-status")
  public Flux<String> statusUpdate(String status) {
    log.info("Connection {}", status);
    return Flux.interval(Duration.ofSeconds(5)).map(index -> String.valueOf(Runtime.getRuntime().freeMemory()));
  }

  @MessageMapping("request-response")
  Mono<String> reqResponse(@Payload String payload) {
    log.info("收到 RR 请求信息: {}", payload);
    return Mono.just("Hello, " + payload);
  }

  @MessageMapping("fire-forget")
  Mono<Void> fnf(@Payload String payload) {
    log.info("收到 FAF 请求信息: {}", payload);
    return Mono.empty();
  }

  @MessageMapping("stream")
  Flux<String> stream(@Payload String payload) {
    return Flux.interval(Duration.ofSeconds(1)).map(aLong -> payload + LocalDateTime.now());
  }

  @MessageMapping("channel")
  Flux<String> channel(Flux<String> settings) {
    return settings.map(s -> "你好 " + s + LocalDateTime.now());
  }
}
