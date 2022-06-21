package org.springframework.circular;


/**
 * Created by oopcoder at 2022/6/21 13:25 .
 */

public class BeanB {

  public BeanA beanA;

  public BeanA getBeanA() {
    return beanA;
  }

  public void setBeanA(BeanA beanA) {
    this.beanA = beanA;
  }
}
