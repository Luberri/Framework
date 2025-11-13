package com.itu.demo.test;
import com.itu.demo.annotations.HandleURL;
import com.itu.demo.annotation.Controller;

@Controller
public class Test3 {
    @HandleURL("/string31")
    public String sayHello() {
        return "<h1>Hello from Test3!</h1>";
    }
}
