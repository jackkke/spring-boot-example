package top.jackkke.rsocket.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;
import top.jackkke.rsocket.constant.UploadConstants;
import top.jackkke.rsocket.service.ClientService;

/**
 * @author jackkke
 */
@Service("clientService")
public class ClientServiceImpl implements ClientService {

  /**
   * 检查客户端ID
   *
   * @param clientId 客户端ID
   * @return 检查结果
   */
  @Override
  public boolean checkClientId(String clientId) {
    List<String> fromDb = selectFromDb();
    return fromDb.contains(clientId);
  }

  /**
   * 检查客户端ID
   * @param headers TCP 连接请求头
   * @return 检查结果
   */
  @Override
  public boolean checkClientId(Map<String, Object> headers) {
    // headers = {dataBufferFactory=
    // NettyDataBufferFactory (PooledByteBufAllocator(directByDefault: true)),
    // rsocketRequester=org.springframework.messaging.rsocket.DefaultRSocketRequester@5b350825, lookupDestination=, contentType=application/cbor,
    // rsocketFrameType=SETUP}
    if (MapUtils.isEmpty(headers)) {
      return false;
    }
    if (!headers.containsKey(UploadConstants.CLIENT_ID)) {
      return false;
    }
    String clientId = headers.get(UploadConstants.CLIENT_ID).toString();
    return checkClientId(clientId);
  }

  private List<String> selectFromDb(){
    return new ArrayList<>(){{
      add("ump-client-0001");
      add("ump-client-0002");
      add("ump-client-0003");
      add("ump-client-0004");
      add("ump-client-0005");
      add("ump-client-0006");
    }};
  }
}
