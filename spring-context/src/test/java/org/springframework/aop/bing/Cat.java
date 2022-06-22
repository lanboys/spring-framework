package org.springframework.aop.bing;

import org.springframework.aop.bing.service.CatService;

public class Cat implements CatService {

  private long age;
  private String name;

  @Override
  public void go() {
    System.out.println(name + "猫在go...");
  }

  public long getAge() {
    return age;
  }

  public void setAge(long age) {
    this.age = age;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "Dog{" +
        "age=" + age +
        ", name='" + name + '\'' +
        '}';
  }
}
