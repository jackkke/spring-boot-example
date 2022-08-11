package top.jackkke.rsocket.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import top.jackkke.rsocket.dto.Message;
import top.jackkke.rsocket.service.MessageService;

/**
 * @author jackkke
 */
@Service
public class MessageServiceImpl implements MessageService {

  @Override
  public void echo(Message msg) {
    System.out.println("msg = " + msg);
  }

  @Override
  public List<Message> list() {
    return new ArrayList<>();
  }

  @Override
  public Message get(String titleInfo) {
    return null;
  }
}
