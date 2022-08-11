package top.jackkke.rsocket.aop;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author jackkke
 */
@Slf4j
@Scope
@Aspect
@Component
public class RateLimitAop {

  private final RateLimiter rateLimiter = RateLimiter.create(5.0);

  @Pointcut("@annotation(org.springframework.messaging.handler.annotation.MessageMapping)")
  public void serviceLimit() {

  }

  @Around("serviceLimit()")
  public Object around(ProceedingJoinPoint joinPoint) {
    String className = joinPoint.getSignature().getDeclaringTypeName();
    String methodName = joinPoint.getSignature().getName();
    Boolean flag = rateLimiter.tryAcquire();
    Object obj = null;
    try {
      if (flag) {
        log.debug("请求访问 class: {}, method: {} 成功", className, methodName);
        obj = joinPoint.proceed();
      }else{
        log.debug("请求访问 class: {}, method: {} 失败，未能获取到锁", className, methodName);
        return null;
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return obj;
  }
}
