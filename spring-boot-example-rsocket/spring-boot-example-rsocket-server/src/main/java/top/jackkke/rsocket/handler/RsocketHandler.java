package top.jackkke.rsocket.handler;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.util.DefaultPayload;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author jackkke
 */
@Slf4j
@Component
public class RsocketHandler implements RSocket {
  @Override
  public Mono<Void> fireAndForget(Payload payload) {
    String message = payload.getDataUtf8();
    log.info("[fireAndForget]接收请求数据:{}", message);
    return Mono.empty();
  }

  @Override
  public Mono<Payload> requestResponse(Payload payload) {
    String message = payload.getDataUtf8();
    log.info("[RequestAndResponse]接收请求数据:{}", message);
    return Mono.just(DefaultPayload.create("[echo]" + message));
  }

  @Override
  public Flux<Payload> requestStream(Payload payload) {
    String message = payload.getDataUtf8();
    log.info("[RequestStream]接收请求数据:{}", message);
    return Flux.fromStream(
        message
            .chars()
            .mapToObj(Character::toUpperCase)
            .map(Object::toString)
            .map(DefaultPayload::create));
  }

  @Override
  public Flux<Payload> requestChannel(Publisher<Payload> publisher) { // 双向流

    return Flux.from(publisher)
        .map(Payload::getDataUtf8)
        .map(
            msg -> {
              log.info("【RequestChannel】接收请求数据:{}", msg);
              return msg;
            })
        .map(DefaultPayload::create);
  }

  @Override
  public Mono<Void> metadataPush(Payload payload) {
    System.out.println("RSocketHandler.metadataPush");
    return null;
  }

  @Override
  public Mono<Void> onClose() {
    System.out.println("RSocketHandler.onClose");
    return null;
  }

  @Override
  public void dispose() {
    System.out.println("RSocketHandler.dispose");
  }
}
