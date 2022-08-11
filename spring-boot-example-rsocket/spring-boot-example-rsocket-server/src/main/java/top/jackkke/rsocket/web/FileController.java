package top.jackkke.rsocket.web;

import com.alibaba.fastjson.JSONObject;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.core.util.UuidUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import top.jackkke.rsocket.FileUploadVo;
import top.jackkke.rsocket.FileUploadVo.FileUploadVoBuilder;
import top.jackkke.rsocket.RouterMapping;
import top.jackkke.rsocket.UploadVO;
import top.jackkke.rsocket.base.FileControllerBase;
import top.jackkke.rsocket.constant.UploadConstants;
import top.jackkke.rsocket.service.MessageService;

/**
 * @author jackkke
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class FileController extends FileControllerBase {

  private final MessageService messageService;

  @Value("${output.file.path.upload:D:\\future\\spring-boot-example\\spring-boot-example-rsocket\\spring-boot-example-rsocket-server}")
  private Path outputPath;


  @MessageMapping(RouterMapping.MESSAGE_UPLOAD)
  public Flux<FileUploadVo> upload(
      @Headers Map<String, Object> metadata, @Payload Flux<DataBuffer> content) {
    FileUploadVoBuilder builder = FileUploadVo.builder();

    // 检查文本内容是否合法
    try {
      checkContent(content);
    } catch (Exception e) {
      return error(e);
    }

    // 首次上传 记录上传文件信息 -> 文件名 文件大小 文件后缀 文件原始位置
    // 自动生成一个唯一文件ID -> UUID ，并以此为文件名+文件后缀写入到磁盘
    // 断点续传 读取唯一文件ID + 继续上传位置 继续上传
    String filePath = null;
    String idName = null;
    try {
      UploadVO uploadVO = JSONObject.parseObject((String) metadata.get(UploadConstants.FILE_DATA), UploadVO.class);
      if (uploadVO.getPosition() != 0 && StringUtils.isNotBlank(uploadVO.getIdName())) {
        filePath = uploadVO.getIdName();
        idName = filePath;
        log.info("【文件续传】idName={}、position = {}", idName, uploadVO.getPosition());
      } else {
        var fileName = uploadVO.getFileName();
        var fileExt = uploadVO.getFileExt();
        filePath = fileName + "." + fileExt;
        idName = UuidUtil.getTimeBasedUuid() + "." + fileExt;
        log.info("【文件上传】fileName={}、fileExt = {}", fileName, fileExt);
      }
      var path = Paths.get(idName);
      builder.idName(idName).originalFilePath(uploadVO.getOriginalFilePath());

      AsynchronousFileChannel channel = createChannel(outputPath, path, StringUtils.isBlank(uploadVO.getIdName()));
      AtomicLong position = new AtomicLong(uploadVO.getPosition());
      Flux<FileUploadVo> concat = Flux.concat(
          DataBufferUtils.write(content, channel, position.get()).map(dataBuffer -> chunkCompleted(position, dataBuffer)),
          success()
      );
      return concat.doOnComplete(doOnComplete()).onErrorResume(onErrorResume(position)).doFinally(doFinally(channel));
    } catch (Exception e) {
      return error(e);
    }
  }
}
