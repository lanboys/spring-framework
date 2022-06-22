package org.springframework.aop.bing.aspect;

import org.aspectj.lang.ProceedingJoinPoint;

public class TxAspect {

  public void begin() {
    System.out.println("begin(): 开启事务");
  }

  public void commit() {
    System.out.println("commit(): 正常结束之后 提交事务");
  }

  public void finallyInvoke() {
    System.out.println("finallyInvoke(): finally 调用");
  }

  public void rollback(Throwable throwable) {
    System.out.println("rollback(): 出现异常 事务回滚");
    System.out.println("rollback(): 异常是：" + throwable.getLocalizedMessage());
  }

  public void around(ProceedingJoinPoint pjp) {
    System.out.println("around(): 环绕通知");
    begin();
    try {
      pjp.proceed();
      commit();
    } catch (Throwable throwable) {
      throwable.printStackTrace();
      rollback(throwable);
    } finally {
      finallyInvoke();
    }
  }
}
