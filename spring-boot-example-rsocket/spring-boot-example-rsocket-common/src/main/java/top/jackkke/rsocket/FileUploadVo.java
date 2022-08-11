package top.jackkke.rsocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.jackkke.rsocket.constant.UploadStatus;

/**
 * @author jackkke
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadVo {
  private String message;
  private UploadStatus status;

  private String idName;
  private Long position;
  private String originalFilePath;

}
