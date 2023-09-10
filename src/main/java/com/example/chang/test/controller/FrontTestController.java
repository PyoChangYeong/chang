package com.example.chang.test.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class FrontTestController {
    @GetMapping("/hello")
    public String test(){
        return "프론트야 잘 도착했다.";
    }
}
