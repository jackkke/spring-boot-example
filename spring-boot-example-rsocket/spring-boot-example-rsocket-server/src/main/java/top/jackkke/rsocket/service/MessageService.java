package top.jackkke.rsocket.service;

import java.util.List;
import top.jackkke.rsocket.dto.Message;

/**
 * @author jackkke
 */
public interface MessageService {

  void echo(Message msg);

  List<Message> list();

  Message get(String titleInfo);
}
