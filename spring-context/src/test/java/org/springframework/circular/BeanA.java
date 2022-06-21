package org.springframework.circular;

/**
 * Created by oopcoder at 2022/6/21 13:25 .
 */

public class BeanA {

  public BeanB beanB;

  public BeanB getBeanB() {
    return beanB;
  }

  public void setBeanB(BeanB beanB) {
    this.beanB = beanB;
  }
}
