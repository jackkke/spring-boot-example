package top.jackkke.rsocket.base;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferLimitException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import top.jackkke.rsocket.FileUploadVo;
import top.jackkke.rsocket.FileUploadVo.FileUploadVoBuilder;
import top.jackkke.rsocket.constant.UploadStatus;

/**
 * @author EST
 */
@Slf4j
public class FileControllerBase {

  private static final long MAX_SIZE = 2L * 1024 * 1024 * 1024;

  /**
   * 检查 上传文件块是否超长
   *
   * @param content 文件上传 Payload
   */
  public void checkContent(Flux<DataBuffer> content) {
    if (content == null) {
      throw new DataBufferLimitException("内容长度不可为空");
    }
    AtomicReference<Boolean> flag = new AtomicReference<>(null);
    Flux.from(content)
        .map(buffer -> {
          flag.set(Long.parseLong(String.valueOf(buffer.writePosition())) <= MAX_SIZE);
          return flag.get();
        }).subscribe();
    boolean res = BooleanUtils.isFalse(flag.get());
    if (res) {
      throw new DataBufferLimitException("内容长度超长，最大为 2G");
    }
  }

  public AsynchronousFileChannel createChannel(Path outputPath, Path path, boolean blank) throws IOException {
    Set<OpenOption> result = new HashSet<>(2);
    if (blank) {
      result.add(StandardOpenOption.CREATE);
    }
    result.add(StandardOpenOption.WRITE);
    return AsynchronousFileChannel.open(outputPath.resolve(path), result, null);
  }

  public Consumer<SignalType> doFinally(AsynchronousFileChannel channel) {
    return signalType -> {
      log.debug("FileController.doFinally signalType : {}", signalType.name());
      if (channel != null && channel.isOpen()) {
        try {
          channel.close();
        } catch (IOException ignored) {
        }
      }
    };
  }

  public Mono<FileUploadVo> success() {
    FileUploadVoBuilder builder = FileUploadVo.builder();
    return Mono.just(builder.message("上传成功").status(UploadStatus.COMPLETED).build());
  }

  public Function<Throwable, Mono<FileUploadVo>> onErrorResume(AtomicLong position) {
    return (throwable) -> {
      log.debug("FileController.onErrorResume = {}", throwable.getMessage());
      FileUploadVoBuilder builder = FileUploadVo.builder();
      return Mono.just(builder.message(throwable.getMessage()).position(position.get()).status(UploadStatus.FAILED).build());
    };
  }

  public Runnable doOnComplete(){
    return () -> log.debug("FileController.doOnComplete");
  }

  public Flux<FileUploadVo> error(Exception e) {
    FileUploadVoBuilder builder = FileUploadVo.builder();
    return Flux.just(builder.status(UploadStatus.FAILED).message(e.getMessage()).build());
  }

  public FileUploadVo chunkCompleted(AtomicLong position, DataBuffer dataBuffer){
    position.addAndGet(dataBuffer.writePosition());
    FileUploadVoBuilder builder = FileUploadVo.builder();
    return builder.position(position.get()).status(UploadStatus.CHUNK_COMPLETED).build();
  }
}
