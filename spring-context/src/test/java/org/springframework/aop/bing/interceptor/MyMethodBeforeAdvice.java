package org.springframework.aop.bing.interceptor;

import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;

/**
 * Created by oopcoder at 2022/6/22 8:52 .
 */

public class MyMethodBeforeAdvice implements MethodBeforeAdvice {

  @Override
  public void before(Method method, Object[] args, Object target) throws Throwable {
    System.out.println("MyMethodBeforeAdvice before(): ");
  }
}
