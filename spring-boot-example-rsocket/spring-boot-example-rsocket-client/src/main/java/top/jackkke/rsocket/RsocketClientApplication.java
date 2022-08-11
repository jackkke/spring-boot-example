package top.jackkke.rsocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import top.jackkke.rsocket.properties.UploadProperties;

/**
 * @author jackkke
 */
@SpringBootApplication
@EnableConfigurationProperties(UploadProperties.class)
public class RsocketClientApplication {

  public static void main(String[] args) {
    SpringApplication.run(RsocketClientApplication.class, args);
  }
}
