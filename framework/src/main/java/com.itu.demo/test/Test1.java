package com.itu.demo.test;
import com.itu.demo.annotations.HandleURL;
import com.itu.demo.annotation.Controller;

@Controller
public class Test1 {
    @HandleURL("/stringT11")
    public String sayHello() {
        return "<h1>Hello from Test1!</h1>";
    }
    
    @HandleURL("/stringT12")
    public String welcome() {
        return "<html><body><h1>Welcome!</h1>";
    }
}
