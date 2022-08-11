package top.jackkke.rsocket.web;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import top.jackkke.rsocket.dto.Message;
import top.jackkke.rsocket.service.MessageService;

/**
 * @author jackkke
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class RsocketFluxController {

  private final MessageService messageService;

  @MessageMapping("message.echo")
  public Mono<Message> echoMessage(Mono<Message> message) {
    return message
        .doOnNext(this.messageService::echo)
        .doOnNext(msg -> log.info("消息接收{}", message));
  }

  @MessageMapping("message.delete")
  public void deleteMessage(Mono<String> title) {
    title.doOnNext(msg -> log.info("消息删除{}", msg)).subscribe();
  }

  @MessageMapping("message.list")
  public Flux<Message> listMessage() {
    return Flux.fromStream(this.messageService.list().stream());
  }

  @MessageMapping("message.get")
  public Flux<Message> getMessage(Flux<String> title) {
    return title
        .doOnNext(t -> log.info("消息查询{}", t))
        .map(String::toLowerCase)
        .map(this.messageService::get)
        .delayElements(Duration.ofSeconds(1));
  }
}
