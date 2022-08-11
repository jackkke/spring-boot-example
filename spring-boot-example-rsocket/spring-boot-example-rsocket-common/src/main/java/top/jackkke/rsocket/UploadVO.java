package top.jackkke.rsocket;

import com.alibaba.fastjson2.JSONObject;
import java.io.File;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.messaging.rsocket.RSocketRequester.MetadataSpec;
import org.springframework.util.MimeType;
import top.jackkke.rsocket.constant.UploadConstants;

/**
 * @author jackkke
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadVO {

  // TODO Last-Modified

  private static final String EXT_SEPARATOR = ".";

  private String fileExt;
  private String fileName;
  private String originalFilePath;
  private Long fileSize;
  private Long position;
  private String idName;

  public UploadVO(String filePath, String fileWholeName) {
    checkFile(filePath, fileWholeName);
    this.fileExt = StringUtils.substringAfterLast(fileWholeName, EXT_SEPARATOR);
    this.fileName = StringUtils.substringBeforeLast(fileWholeName, EXT_SEPARATOR);
    this.originalFilePath = filePath + File.separator + fileWholeName;
    File file = new File(this.originalFilePath);
    if (!file.exists()) {
      throw new RuntimeException("源文件不存在");
    }
    this.fileSize = file.length();
    this.position = 0L;
    this.idName = null;
  }

  public UploadVO(long position, String idName, String originalFilePath) {
    checkGoon(position, idName);
    this.position = position;
    this.idName = idName;
    this.originalFilePath = originalFilePath;
  }

  public void improveMeta(MetadataSpec metadataSpec){
    /*metadataSpec.metadata(
        this.fileName, MimeType.valueOf(UploadConstants.MINE_FILE_NAME));
    metadataSpec.metadata(
        this.fileExt, MimeType.valueOf(UploadConstants.MINE_FILE_EXTENSION));
    metadataSpec.metadata(
        String.valueOf(this.getPosition()), MimeType.valueOf(UploadConstants.MINE_FILE_POSITION));
    metadataSpec.metadata(
        this.remoteFileId, MimeType.valueOf(UploadConstants.MINE_FILE_ID));*/
    metadataSpec.metadata(
        JSONObject.toJSONString(this), MimeType.valueOf(UploadConstants.MINE_FILE_DATA));
  }

  private void checkFile(String filePath, String fileWholeName) {
    if (!ObjectUtils.allNotNull(filePath, fileWholeName)) {
      throw new RuntimeException("上传参数有误！");
    }
    if (StringUtils.isBlank(fileWholeName)) {
      throw new RuntimeException("文件名有误！");
    }
  }

  private void checkGoon(long position, String idName) {
    if (!ObjectUtils.allNotNull(position, idName)) {
      throw new RuntimeException("续传参数有误！");
    }
  }
}
