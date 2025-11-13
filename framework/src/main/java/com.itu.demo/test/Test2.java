package com.itu.demo.test;
import com.itu.demo.annotations.HandleURL;
public class Test2 {
    @HandleURL("/string21")
    public String sayHello() {
        return "<h1>Hello from Test2!</h1>";
    }
}
