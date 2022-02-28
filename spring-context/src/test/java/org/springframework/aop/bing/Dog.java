package org.springframework.aop.bing;

import org.springframework.aop.bing.service.DogService;

public class Dog implements DogService {

  private long age;
  private String name;

  @Override
  public void run() {
    System.out.println(name + "狗在跑...");
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
