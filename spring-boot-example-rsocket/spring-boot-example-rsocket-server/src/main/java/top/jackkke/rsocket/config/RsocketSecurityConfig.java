package top.jackkke.rsocket.config;

import io.rsocket.metadata.WellKnownMimeType;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.MetadataExtractor;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.messaging.handler.invocation.reactive.AuthenticationPrincipalArgumentResolver;
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Mono;
import top.jackkke.rsocket.service.ClientService;

/**
 * @author jackkke
 */
@Slf4j
@Configuration
@EnableRSocketSecurity
@RequiredArgsConstructor
@EnableReactiveMethodSecurity
public class RsocketSecurityConfig {

  private final ClientService clientService;

  private final RSocketStrategies rSocketStrategies;

  @Bean
  RSocketMessageHandler messageHandler(RSocketStrategies strategies) {
    RSocketMessageHandler handler = new RSocketMessageHandler();
    handler.getArgumentResolverConfigurer().addCustomResolver(new AuthenticationPrincipalArgumentResolver());
    handler.setRSocketStrategies(strategies);
    return handler;
  }

  @Bean
  MapReactiveUserDetailsService authentication() {
    UserDetails user = User.withDefaultPasswordEncoder()
        .username("user")
        .password("pass")
        .roles("USER")
        .build();
    UserDetails admin = User.withDefaultPasswordEncoder()
        .username("test")
        .password("pass")
        .roles("NONE")
        .build();
    return new MapReactiveUserDetailsService(user, admin);
  }

  @Bean
  PayloadSocketAcceptorInterceptor authorization(RSocketSecurity security) {
    security.authorizePayload(authorize ->
        authorize
            .anyExchange().access((a, ctx) -> {
              // 处理自定义权限过滤
              MessageHeaderAccessor headers = new MessageHeaderAccessor();
              headers.setLeaveMutable(true);
              MimeType mimeType = MimeTypeUtils.parseMimeType(
                  WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.getString());
              MetadataExtractor metadataExtractor = rSocketStrategies.metadataExtractor();
              Map<String, Object> metadataValues = metadataExtractor.extract(ctx.getExchange().getPayload(), mimeType);
              boolean checkClientId = clientService.checkClientId(metadataValues);
              if (!checkClientId) {
                log.error("tcp请求失败，请求头 client id 信息不存在或有误！");
              }
              log.error(" TODO 在此 处理全局请求数限速");
              log.error(" TODO 在此 处理client请求数限速");
              return Mono.just(new AuthorizationDecision(checkClientId));
            })
            .anyExchange().authenticated()
    ).simpleAuthentication(Customizer.withDefaults());
    return security.build();
  }
}
