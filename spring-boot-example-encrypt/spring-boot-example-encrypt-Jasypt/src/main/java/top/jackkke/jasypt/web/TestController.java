package top.jackkke.jasypt.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jackkke
 */
@RestController
public class TestController {

  @Value("${encode.msg}")
  private String msg;

  @GetMapping("get")
  public String getMsg() {
    System.out.println("msg = " + msg);
    return msg;
  }
}
