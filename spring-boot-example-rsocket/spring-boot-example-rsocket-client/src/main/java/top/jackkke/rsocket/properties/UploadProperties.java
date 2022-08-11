package top.jackkke.rsocket.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author jackkke
 */
@Data
@ConfigurationProperties("ump.rsocket.upload")
public class UploadProperties {

  /**
   * 缓冲区大小
   */
  private int bufferSize = 15 * 1024 * 1024;

  /**
   * 上传文件大小限制
   */
  private int maxSize = 500 * 1024 * 1024;
}
