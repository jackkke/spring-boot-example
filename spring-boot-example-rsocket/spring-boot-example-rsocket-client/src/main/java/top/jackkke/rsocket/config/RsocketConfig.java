package top.jackkke.rsocket.config;

import static io.netty.handler.ssl.SslProvider.JDK;
import static io.netty.handler.ssl.SslProvider.OPENSSL;

import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.OpenSsl;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.transport.netty.client.TcpClientTransport;
import java.security.KeyStore;
import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.security.rsocket.metadata.SimpleAuthenticationEncoder;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Mono;
import reactor.netty.tcp.SslProvider;
import reactor.netty.tcp.TcpClient;
import reactor.netty.tcp.TcpSslContextSpec;
import reactor.util.retry.Retry;
import top.jackkke.rsocket.constant.UploadConstants;

/**
 * @author jackkke
 */
@Slf4j
@Configuration
public class RsocketConfig {

  private static final MimeType SIMPLE_AUTH = MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString());

  /**
   * 配置策略，编码和解码
   */
  @Bean
  public RSocketStrategies getRsocketStrategies() {
    return RSocketStrategies.builder()
        .encoders(encoders -> encoders.add(new Jackson2CborEncoder()))
        .encoders(encoders -> encoders.add(new SimpleAuthenticationEncoder()))
        .decoders(decoders -> decoders.add(new Jackson2CborDecoder()))
        .build();
  }

  @Bean
  public RSocketRequester rsocketRequester(
      RSocketRequester.Builder builder, RSocketStrategies rSocketStrategies, SslProvider sslProvider) {

    UsernamePasswordMetadata user = new UsernamePasswordMetadata("user", "pass");
    TcpClient tcpClient = TcpClient.create()
        .host("localhost")
        .secure(sslProvider)
        .port(19712);
    String clientId = "ump-client-0001";
    return builder
        .rsocketStrategies(rSocketStrategies)
        .rsocketConnector(
            rSocketConnector ->
                rSocketConnector.reconnect(Retry.fixedDelay(2, Duration.ofSeconds(2))))
        .dataMimeType(MediaType.APPLICATION_CBOR)
        .setupMetadata(user, SIMPLE_AUTH)
        .setupMetadata(clientId, MimeType.valueOf(UploadConstants.MINE_CLIENT_ID))
        .transport(TcpClientTransport.create(tcpClient));
  }

  /**
   * 配置RSocket连接策略
   */
  @Bean
  public Mono<RSocketRequester> getRsocketRequester(RSocketRequester requester) {
    return Mono.just(requester).doOnSuccess(onSuccess -> {
      log.debug("onSuccess = {}", onSuccess);
      log.debug("RsocketConfig.getRsocketRequester.onSuccess");
    }).doOnError(onError -> {
      onError.printStackTrace();
      log.debug("RsocketConfig.getRsocketRequester.doOnError");
    }).doOnCancel(() -> {
      log.debug("RsocketConfig.getRsocketRequester.doOnCancel");
    }).doFinally(onFinally -> {
      log.debug("onFinally = {}", onFinally);
      log.debug("RsocketConfig.getRsocketRequester.doFinally");
    });
  }

  /**
   * 忽略 SSL 证书
   *
   * @return 自定义 ssl 策略
   */
  @Bean
  public SslProvider sslProvider() {
    String keystoreFile = "rsocket.p12";
    String keystorePass = "rsocketStore";
    try {
      MyX509TrustManager myX509TrustManager = new MyX509TrustManager(keystoreFile, keystorePass);
      TcpSslContextSpec tcpSslContextSpec = TcpSslContextSpec.forClient();
      tcpSslContextSpec.configure(sslCtxBuilder ->
          sslCtxBuilder.sslProvider(OpenSsl.isAvailable() ? OPENSSL : JDK)
              .trustManager(myX509TrustManager)
              .ciphers(null, IdentityCipherSuiteFilter.INSTANCE)
              .applicationProtocolConfig(null));
      return SslProvider.builder().sslContext(tcpSslContextSpec).build();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  class MyX509TrustManager implements X509TrustManager{
    X509TrustManager sunJSSEX509TrustManager;
    MyX509TrustManager(String keystoreFile,String pass) throws Exception {
      KeyStore ks = KeyStore.getInstance("PKCS12");
      ks.load(new ClassPathResource(keystoreFile).getInputStream(), pass.toCharArray());
      TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
      tmf.init(ks);
      TrustManager tms [] = tmf.getTrustManagers();
      for (int i = 0; i < tms.length; i++) {
        if (tms[i] instanceof X509TrustManager) {
          sunJSSEX509TrustManager = (X509TrustManager) tms[i];
          return;
        }
      }
      throw new Exception("Couldn't initialize");
    }
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
      log.debug("MyX509TrustManager.checkClientTrusted");
      sunJSSEX509TrustManager.checkClientTrusted(chain, authType);
    }
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
      log.debug("MyX509TrustManager.checkServerTrusted");
      sunJSSEX509TrustManager.checkServerTrusted(chain, authType);
    }
    @Override
    public X509Certificate[] getAcceptedIssuers() {
      log.debug("MyX509TrustManager.getAcceptedIssuers");
      System.out.println("MyX509TrustManager.getAcceptedIssuers");
      return sunJSSEX509TrustManager.getAcceptedIssuers();
    }
  }

  @Bean("trustAllCerts")
  public X509TrustManager trustAllCerts(){
    return new X509TrustManager() {
      @Override
      public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
        System.out.println("RsocketConfig.checkClientTrusted");
        System.out.println("x509Certificates = " + Arrays.toString(x509Certificates));
        System.out.println("s = " + s);
      }

      @Override
      public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
        Stream<String> stringStream = Arrays.stream(x509Certificates)
            .map(X509Certificate::getSubjectDN)
            .filter(Objects::nonNull)
            .map(Principal::getName)
            .filter(Objects::nonNull);
        boolean match = stringStream.anyMatch(o -> o.contains("=esafenet,"));
        if (!match) {
          log.error("ssl 证书 [{}] 有误", stringStream.collect(Collectors.joining("|")));
          throw new RuntimeException("ssl 证书 有误");
        }
      }

      @Override
      public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
      }
    };
  }
}
