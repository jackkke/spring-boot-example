package top.jackkke.rsocket.client;

import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.util.MimeType;
import reactor.core.publisher.Mono;
import top.jackkke.rsocket.RouterMapping;
import top.jackkke.rsocket.constant.UploadConstants;
import top.jackkke.rsocket.dto.Message;

/**
 * @author jackkke
 */
@Slf4j
@ShellComponent
public class RsocketShellClient {

  @Resource
  private RSocketRequester rsocketRequester;

  @Resource
  private Mono<RSocketRequester> requesterMono;

  @ShellMethod("简单消息发送与接收")
  public void str(String name) {
    log.info("简单消息发送与接收...");
    String message =
        rsocketRequester
            .route(RouterMapping.REQUEST_RESPONSE_STR)
            .metadata("ump-client-0006", MimeType.valueOf(UploadConstants.MINE_CLIENT_ID))
            .data("我是" + name)
            .retrieveMono(String.class)
            .block();
    log.info("返回结果: {}", message);
  }

  @ShellMethod("消息体发送与接收")
  public void msg(String name) {
    log.info("消息体发送与接收...");
    Message message =
        rsocketRequester
            .route(RouterMapping.REQUEST_RESPONSE_MESSAGE)
            .metadata("ump-client-0006", MimeType.valueOf(UploadConstants.MINE_CLIENT_ID))
            .data(new Message(name, name))
            .retrieveMono(Message.class)
            .block();
    log.info("返回结果: {}", message);
  }
}
