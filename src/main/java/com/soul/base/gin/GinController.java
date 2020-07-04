package com.soul.base.gin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GinController {

    @GetMapping("/test")
    public String test(String str){
        return "test: " + str;
    }

}
