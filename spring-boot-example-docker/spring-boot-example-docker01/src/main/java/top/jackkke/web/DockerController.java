package top.jackkke.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jackkke
 */
@RestController
public class DockerController {

  @Value("${spring.application.name}")
  private String name;

  @GetMapping(value = "hello")
  public Object sayHello() {
    return "hello! I'm from " + name;
  }
}
