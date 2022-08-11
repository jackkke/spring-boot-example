package top.jackkke.rsocket.service;

import java.util.Map;

/**
 * @author jackkke
 */
public interface ClientService {

  /**
   * 检查客户端ID
   * @param clientId 客户端ID
   * @return 检查结果
   */
  boolean checkClientId(String clientId);

  /**
   * 检查客户端ID
   * @param headers TCP 连接请求头
   * @return 检查结果
   */
  boolean checkClientId(Map<String, Object> headers);
}
