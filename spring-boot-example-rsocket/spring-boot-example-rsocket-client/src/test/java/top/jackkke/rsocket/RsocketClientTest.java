package top.jackkke.rsocket;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import reactor.core.publisher.Mono;
import top.jackkke.rsocket.dto.Message;

@WebAppConfiguration
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = RsocketClientApplication.class)
public class RsocketClientTest {
  @Autowired
  private Mono<RSocketRequester> requesterMono; //来进行服务调用

  @Test
  public void testEchoMessage(){ //测试服务响应
    this.requesterMono.map(r->r.route("message.echo")
            .data(new Message("pshdhx","fighting")))
        .flatMap(r->r.retrieveMono(Message.class))
        .doOnNext(System.out::println).block();
  }

  @Test
  public void testDeleteMessage(){
    this.requesterMono.map(r->r.route("message.delete")
            .data("pshdhx"))
        .flatMap(RSocketRequester.RetrieveSpec::send).block();
  }

  @Test
  public void testListMessage(){
    this.requesterMono.map(r->r.route("message.list"))
        .flatMapMany(r->r.retrieveFlux(Message.class))
        .doOnNext(System.out::println).blockLast();
  }

}
