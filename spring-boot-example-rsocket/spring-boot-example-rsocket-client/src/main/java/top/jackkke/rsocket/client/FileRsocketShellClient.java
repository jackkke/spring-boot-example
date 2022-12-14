package top.jackkke.rsocket.client;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import top.jackkke.redis.util.RedisService;
import top.jackkke.rsocket.FileUploadVo;
import top.jackkke.rsocket.RouterMapping;
import top.jackkke.rsocket.UploadVO;
import top.jackkke.rsocket.constant.UploadStatus;
import top.jackkke.rsocket.properties.UploadProperties;

/**
 * @author jackkke
 */
@Slf4j
@ShellComponent
@RequiredArgsConstructor
public class FileRsocketShellClient {

  private final UploadProperties uploadProperties;
  private final RedisService redisService;

  @Value("${uploadPath:D:\\future\\spring-boot-example\\spring-boot-example-rsocket\\spring-boot-example-rsocket-client}")
  private String uploadPath;

  @Resource
  private Mono<RSocketRequester> requesterMono;

  @ShellMethod("Send one request. One response will be printed.")
  public void upload(@NotNull String fileName, Integer type) {
    try {
      UploadVO uploadVO;
      if (type != null && type.equals(1)) {
        FileUploadVo fileUploadVo = redisService.get(fileName, FileUploadVo.class);
        redisService.del(fileName);
        uploadVO = new UploadVO(fileUploadVo.getPosition(), fileUploadVo.getIdName(), fileUploadVo.getOriginalFilePath());
      } else {
        uploadVO = new UploadVO(uploadPath, fileName);
      }
      System.out.println("uploadVO = " + uploadVO);
      FileUrlResource fileUrlResource = new FileUrlResource(uploadVO.getOriginalFilePath());
      Flux<FileUploadVo> uploadStatusFlux = requesterMono
          .map(r -> r.route(RouterMapping.MESSAGE_UPLOAD).metadata(uploadVO::improveMeta).data(
              DataBufferUtils.read(
                  fileUrlResource,
                  uploadVO.getPosition(),
                  new DefaultDataBufferFactory(),
                  uploadProperties.getBufferSize()
              )
//                  .doOnNext(s -> System.out.println("????????????:" + s))
          ))
          .flatMapMany(r -> r.retrieveFlux(FileUploadVo.class))
          .doOnNext(this::checkResult);
      uploadStatusFlux.blockLast();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  void checkResult(FileUploadVo res) {
    if (res == null) {
      log.info("??????????????????");
    } else if (res.getStatus().equals(UploadStatus.FAILED)) {
      log.error("???????????? {}", res);
      if (StringUtils.isNotBlank(res.getIdName())) {
        // ??????redis???
        redisService.set(res.getIdName(), res);
      }
    } else if (res.getStatus().equals(UploadStatus.COMPLETED)) {
      log.info("???????????? \n ?????????????????? {}", res);
    } else if (res.getStatus().equals(UploadStatus.CHUNK_COMPLETED)) {
      log.info("???????????????????????? {}, id {}, ?????????????????? {}", res.getOriginalFilePath(), res.getIdName(), res.getPosition());
    } else {
      log.info("??????????????? {}", res);
    }
  }

  // StepVerifier.create(result).verifyErrorMessage("Denied");
}
