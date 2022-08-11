package top.jackkke.rsocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeType;
import top.jackkke.rsocket.constant.UploadConstants;

/**
 * @author jackkke
 */
@Configuration
public class RsocketServerConfig {
  @Bean
  public RSocketStrategies getRsocketStrategies() {
    return RSocketStrategies.builder()
        .encoders(encoders -> encoders.add(new Jackson2CborEncoder()))
        .decoders(decoders -> decoders.add(new Jackson2CborDecoder()))
        .metadataExtractorRegistry(
            metadataExtractorRegistry -> {
              metadataExtractorRegistry.metadataToExtract(
                  MimeType.valueOf(UploadConstants.MINE_FILE_DATA),
                  String.class,
                  UploadConstants.FILE_DATA);
              metadataExtractorRegistry.metadataToExtract(
                  MimeType.valueOf(UploadConstants.MINE_USERNAME),
                  String.class,
                  UploadConstants.USERNAME);
              metadataExtractorRegistry.metadataToExtract(
                  MimeType.valueOf(UploadConstants.MINE_PASSWORD),
                  String.class,
                  UploadConstants.PASSWORD);
              metadataExtractorRegistry.metadataToExtract(
                  MimeType.valueOf(UploadConstants.MINE_CLIENT_ID),
                  String.class,
                  UploadConstants.CLIENT_ID);
            })
        .build();
  }
}
